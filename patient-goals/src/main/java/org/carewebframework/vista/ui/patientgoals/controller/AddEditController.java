/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.GoalType;
import org.carewebframework.vista.ui.patientgoals.model.Review;
import org.carewebframework.vista.ui.patientgoals.model.Step;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;

/**
 * Controller for adding or editing goals and steps
 */
public class AddEditController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PATH + "add-edit.zul";
    
    private static final String[] TYPES = { "GOAL", "STEP", "ACTIVE", "INACTIVE", "DECLINED", "ADD", "REVIEW" };
    
    public enum ActionType {
        ADD, REVIEW, DELETE;
        
        /**
         * Formats the label to be used for the tab caption based on the action type and the goal or
         * step.
         * 
         * @param goalBase The goal or step.
         * @return The formatted label.
         */
        public String getLabel(GoalBase goalBase) {
            Goal goal = goalBase instanceof Goal ? (Goal) goalBase : ((Step) goalBase).getGoal();
            Step step = goalBase instanceof Step ? (Step) goalBase : null;
            return StrUtil.getLabel("vistaPatientGoals.addedit.tab.label." + (step != null ? "step" : "goal") + "."
                    + goalBase.getGroup().name().toLowerCase() + "." + this.name().toLowerCase(), goal.getName(),
                step == null ? null : step.getNumberAsString());
        }
    }
    
    private ActionType actionType;
    
    private GoalBase goalBase;
    
    private Goal goal;
    
    private Step step;
    
    private boolean isStep;
    
    private boolean hasChanged;
    
    private boolean deleting;
    
    private String requiredMessage;
    
    private Component wrongValueTarget;
    
    private final GoalService service;
    
    private final Set<Component> changeSet = new HashSet<>();
    
    // Start of auto-wired members
    
    private Button btnOK;
    
    private Tab tab;
    
    private Component form;
    
    private Textbox txtName;
    
    private Textbox txtReason;
    
    private Textbox txtNoteHistory;
    
    private Textbox txtNote;
    
    private Datebox datStart;
    
    private Datebox datFollowup;
    
    private Label lblTypes;
    
    private Radiogroup rgTypes;
    
    private Radiogroup rgStatus;
    
    private Radiogroup rgDeleteReason;
    
    private Row rowDeleteReason;
    
    private Label lblDeleteReason;
    
    private Textbox txtDeleteReason;
    
    // End of auto-wired members.
    
    /**
     * Creates a new tab for the specified action type. If a tab already exists for the action type
     * and goal/step, then that tab is revealed and no further action is taken.
     * 
     * @param tabbox The tab box.
     * @param goalBase The step or goal.
     * @param actionType The type of action.
     * @return Returns the created tab.
     */
    public static Tab execute(Tabbox tabbox, GoalBase goalBase, ActionType actionType) {
        Tab tab = findTab(tabbox, goalBase, actionType);
        
        if (tab != null) {
            tabbox.setSelectedTab(tab);
            return tab;
        }
        
        tab = new Tab();
        tab.setClosable(true);
        Tabpanel panel = new Tabpanel();
        tabbox.getTabs().appendChild(tab);
        tabbox.getTabpanels().appendChild(panel);
        tabbox.setSelectedTab(tab);
        Map<Object, Object> args = new HashMap<>();
        args.put("goalBase", goalBase);
        args.put("actionType", actionType);
        args.put("tab", tab);
        ZKUtil.loadZulPage(DIALOG, panel, args);
        return tab;
    }
    
    /**
     * Searches for an existing tab for the action type and step/goal.
     * 
     * @param tabbox The tab box.
     * @param goalBase The step or goal.
     * @param actionType The type of action.
     * @return An existing tab corresponding to the action type and step/goal, or null if none
     *         found.
     */
    private static Tab findTab(Tabbox tabbox, GoalBase goalBase, ActionType actionType) {
        String id = createId(goalBase, actionType);
        List<Tab> tabs = tabbox.getTabs().getChildren();
        
        for (int i = 1; i < tabs.size(); i++) {
            if (id.equals(tabs.get(i).getValue())) {
                return tabs.get(i);
            }
        }
        
        return null;
    }
    
    /**
     * Returns a unique id to be used to identify a tab servicing an action type for a specific
     * goal/step.
     * 
     * @param goalBase The step or goal.
     * @param actionType The type of action.
     * @return A unique id.
     */
    private static String createId(GoalBase goalBase, ActionType actionType) {
        return actionType + "." + goalBase.getGroup() + "." + goalBase.getIEN()
                + (goalBase instanceof Step ? "." + ((Step) goalBase).getGoal().getIEN() : "");
    }
    
    /**
     * Creates the controller with the goal service to use.
     * 
     * @param service The goal service.
     */
    public AddEditController(GoalService service) {
        this.service = service;
    }
    
    /**
     * Populates the goal types and initializes the input elements with values from the step or
     * goal. Also, wires change events for all input elements.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        populateGoalTypes();
        populateControls();
        requiredMessage = getLabel("required");
        wireChangeEvents(comp);
        
        if (goalBase.getGroup() == GoalGroup.INACTIVE) {
            ZKUtil.disableChildren(form, true);
        }
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
        arg = Executions.getCurrent().getArg();
        goalBase = (GoalBase) arg.get("goalBase");
        actionType = (ActionType) arg.get("actionType");
        tab = (Tab) arg.get("tab");
        tab.addForward(Events.ON_CLOSE, comp, "onCloseTab");
        isStep = goalBase instanceof Step;
        
        if (actionType == ActionType.REVIEW) {
            GoalBase temp = isStep ? new Step(((Step) goalBase).getGoal()) : new Goal();
            temp.copyFrom(goalBase);
            goalBase = temp;
        }
        
        if (isStep) {
            step = (Step) goalBase;
        } else {
            goal = (Goal) goalBase;
        }
        
        tab.setLabel(actionType.getLabel(goalBase));
        tab.setValue(createId(goalBase, actionType));
    }
    
    /**
     * Recursively wires input elements to a common event handler for the detection of changes.
     * 
     * @param parent The parent component.
     */
    private void wireChangeEvents(Component parent) {
        for (Component child : parent.getChildren()) {
            String sourceEvent = null;
            
            if (child instanceof Datebox) {
                sourceEvent = Events.ON_CHANGE;
            } else if (child instanceof Textbox) {
                sourceEvent = Events.ON_CHANGING;
            } else if (child instanceof Checkbox) {
                sourceEvent = Events.ON_CHECK;
            }
            
            if (sourceEvent != null) {
                child.addForward(sourceEvent, root, Events.ON_CHANGE);
            }
            
            wireChangeEvents(child);
        }
        
    }
    
    private void populateControls() {
        txtReason.setText(goalBase.getReason());
        txtName.setText(goalBase.getName());
        
        if (!isStep) {
            StringBuilder sb = new StringBuilder();
            
            for (Review review : goal.getReviews()) {
                sb.append(review).append('\n');
            }
            
            txtNoteHistory.setText(sb.toString());
        }
        
        if (datStart != null && goalBase.getStartDate() != null) {
            datStart.setValue(goalBase.getStartDate());
        }
        
        if (datFollowup != null && goalBase.getFollowupDate() != null) {
            datFollowup.setValue(goalBase.getFollowupDate());
        }
        
        if (rgTypes != null) {
            Checkbox chk = null;
            
            while ((chk = ZKUtil.findChild(rgTypes, Checkbox.class, chk)) != null) {
                chk.setChecked(goalBase.getTypes().contains((chk.getValue())));
            }
        }
        
        if (txtDeleteReason != null) {
            txtDeleteReason.setText(goalBase.getDeleteReason());
        }
        
        initRadio(rgStatus, goalBase.getStatusCode(), 0);
        initRadio(rgDeleteReason, goalBase.getDeleteCode(), -1);
        updateDeleteState();
    }
    
    private void populateGoalBase() {
        if (!isStep && changeSet.contains(txtNote)) {
            String note = txtNote.getText();
            
            if (note != null && !note.isEmpty()) {
                Review review = new Review(FMDate.today(), note);
                goal.getReviews().add(review);
            } else {
                changeSet.remove(txtNote);
            }
        }
        
        goalBase.setReason(txtReason.getText());
        goalBase.setName(txtName.getText());
        
        if (datStart != null) {
            goalBase.setStartDate(datStart.getValue() == null ? null : new FMDate(datStart.getValue()));
        }
        
        if (datFollowup != null) {
            goalBase.setFollowupDate(datFollowup.getValue() == null ? null : new FMDate(datFollowup.getValue()));
        }
        
        if (rgStatus != null) {
            goalBase.setStatus(rgStatus.getSelectedItem().getValue().toString());
        }
        
        if (rgTypes != null) {
            goalBase.getTypes().clear();
            Checkbox chk = null;
            
            while ((chk = ZKUtil.findChild(rgTypes, Checkbox.class, chk)) != null) {
                if (chk.isChecked()) {
                    goalBase.getTypes().add((GoalType) chk.getValue());
                }
            }
        }
        
        if (deleting && rgDeleteReason.getSelectedItem() != null) {
            goalBase.setDelete(rgDeleteReason.getSelectedItem().getValue().toString());
        }
        
        if (deleting && txtDeleteReason.isVisible()) {
            goalBase.setDeleteReason(txtDeleteReason.getText());
        }
    }
    
    private void populateGoalTypes() {
        if (rgTypes != null) {
            for (GoalType goalType : service.getGoalTypes()) {
                Checkbox chk = isStep ? new Radio() : new Checkbox();
                rgTypes.appendChild(chk);
                chk.setLabel(goalType.toString());
                chk.setValue(goalType);
                chk.setDisabled(actionType == ActionType.REVIEW);
            }
        }
    }
    
    public void updateDeleteState() {
        if (rgStatus != null) {
            deleting = rgStatus.getSelectedIndex() == 4;
            lblDeleteReason.setVisible(deleting);
            rgDeleteReason.setVisible(deleting);
            boolean isOther = rgDeleteReason != null && rgDeleteReason.getSelectedIndex() == 2;
            rowDeleteReason.setVisible(isOther);
            txtDeleteReason.setFocus(isOther);
            Clients.resize(root);
        }
    }
    
    public void onChange(Event event) {
        Component target = ZKUtil.getEventOrigin(event).getTarget();
        changeSet.add(target);
        wrongValue(null, null);
        
        if (!hasChanged) {
            hasChanged = true;
            btnOK.setDisabled(false);
            tab.setLabel("*" + tab.getLabel());
            tab.setAttribute("changed", hasChanged);
        }
    }
    
    /**
     * Returns true if the goal/step is of one of the specified types. Used by zul page to modify
     * the view.
     * 
     * @param types Comma-delimited string of types.
     * @return True if goal/step is one of the specified types.
     */
    public boolean isType(String types) {
        for (String type : types.split("\\,")) {
            boolean result = false;
            int idx = Arrays.asList(TYPES).indexOf(type.trim().toUpperCase());
            
            switch (idx) {
                case 0: // GOAL
                case 1: // STEP
                    result = idx == 0 ? !isStep : isStep;
                    break;
                
                case 2: // ACTIVE
                case 3: // INACTIVE
                case 4: // DECLINED
                    result = goalBase.getGroup() == GoalGroup.values()[idx - 2];
                    break;
                
                case 5: // ADD
                case 6: // REVIEW
                    result = actionType == ActionType.values()[idx - 5];
                    break;
            }
            
            if (result) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Returns the label for the current add/edit mode. Used by zul page to select appropriate
     * labels.
     * 
     * @param key The label key
     * @param args Optional arguments
     * @return The label text.
     */
    public String getLabel(String key, Object... args) {
        String type = isStep ? "step" : "goal";
        return StrUtil.getLabel("vistaPatientGoals.addedit." + key + ".label." + type, args);
    }
    
    private boolean hasRequired() {
        if (rgTypes != null) {
            Checkbox chk = null;
            boolean hasType = false;
            
            while ((chk = ZKUtil.findChild(rgTypes, Checkbox.class, chk)) != null) {
                if (chk.isChecked()) {
                    hasType = true;
                    break;
                }
            }
            
            if (!hasType) {
                return isRequired(lblTypes);
            }
        }
        
        if (datFollowup != null && datFollowup.getValue() == null) {
            return isRequired(datFollowup);
        }
        
        if (deleting && rgDeleteReason.getSelectedItem() == null) {
            return isRequired(rgDeleteReason);
        }
        
        if (rowDeleteReason.isVisible() && txtDeleteReason.getText().trim().isEmpty()) {
            return isRequired(txtDeleteReason);
        }
        
        return true;
    }
    
    private boolean isRequired(Component target) {
        wrongValue(target, requiredMessage);
        return false;
    }
    
    private void wrongValue(Component target, String message) {
        if (wrongValueTarget != null) {
            Clients.clearWrongValue(wrongValueTarget);
        }
        
        wrongValueTarget = target;
        
        if (target != null && message != null) {
            Clients.wrongValue(target, message);
        }
    }
    
    private void initRadio(Radiogroup rg, String code, int defaultIndex) {
        if (rg == null) {
            return;
        }
        
        for (Radio radio : rg.getItems()) {
            if (StrUtil.piece(radio.getValue().toString(), ";").equals(code)) {
                rg.setSelectedItem(radio);
                return;
            }
        }
        
        rg.setSelectedIndex(defaultIndex);
    }
    
    public void onClick$btnOK() {
        if (commit()) {
            close(true);
        }
    }
    
    public void onClick$btnCancel() {
        close(false);
    }
    
    public void onCheck$rgStatus() {
        updateDeleteState();
    }
    
    public void onCheck$rgDeleteReason() {
        updateDeleteState();
    }
    
    public void onCloseTab(Event event) {
        ZKUtil.getEventOrigin(event).stopPropagation();
        close(false);
    }
    
    private boolean commit() {
        if (!hasRequired()) {
            return false;
        }
        
        if (deleting
                && !PromptDialog
                        .confirm(getLabel("confirmDelete.text"), getLabel("confirmDelete.title", goalBase.getName()))) {
            return false;
        }
        
        populateGoalBase();
        
        try {
            switch (deleting ? ActionType.DELETE : actionType) {
                case ADD:
                    if (isStep) {
                        service.addStep(step);
                        step.getGoal().getSteps().add(step);
                    } else {
                        service.addGoal(goal);
                    }
                    break;
                
                case REVIEW:
                    if (isStep) {
                        service.updateStep(step);
                    } else {
                        service.updateGoal(goal, changeSet.contains(txtNote));
                    }
                    break;
                
                case DELETE:
                    if (isStep) {
                        service.deleteStep(step);
                    } else {
                        service.deleteGoal(goal);
                    }
                    break;
            }
            
            if (actionType == ActionType.REVIEW) {
                GoalBase original = (GoalBase) arg.get("goalBase");
                original.copyFrom(goalBase);
            }
            
        } catch (Exception e) {
            PromptDialog.showError(e);
            return false;
        }
        
        changeSet.clear();
        Events.postEvent("onCommit", tab.getFellow("root", true), actionType == ActionType.ADD ? goalBase : null);
        return true;
    }
    
    private void close(boolean force) {
        if (!force
                && !changeSet.isEmpty()
                && !PromptDialog.confirm("If you continue, you will lose unsaved changes.  Continue?",
                    "Closing " + tab.getLabel())) {
            return;
        }
        
        tab.getTabbox().setSelectedIndex(0);
        tab.getLinkedPanel().detach();
        tab.detach();
    }
}
