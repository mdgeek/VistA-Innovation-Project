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
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalStatus;
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
        ADD, REVIEW;
        
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
            return StrUtil.getLabel(
                "vistaPatientGoals.addedit.tab.label." + (step != null ? "step" : "goal") + "."
                        + goalBase.getGroup().name().toLowerCase() + "." + this.name().toLowerCase(),
                goal.getName(), step == null ? null : step.getNumberAsString());
        }
    }
    
    private ActionType actionType;
    
    private GoalBase goalBase;
    
    private Goal goal;
    
    private Step step;
    
    private boolean isStep;
    
    private boolean hasChanged;
    
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
     * Closes any tabs associated with a goal or step.
     * 
     * @param tabbox The tab box.
     * @param goalBase The step or goal. If a goal, any tabs associated with its steps are also
     *            closed.
     */
    protected static void closeTabs(Tabbox tabbox, GoalBase goalBase) {
        Tab tab = findTab(tabbox, goalBase, ActionType.REVIEW);
        
        if (tab != null) {
            tab.close();
            
            if (goalBase instanceof Goal) {
                for (Step step : ((Goal) goalBase).getSteps()) {
                    closeTabs(tabbox, step);
                }
            }
        }
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
        populateGoalStatus();
        populateControls();
        requiredMessage = getLabel("required");
        ZKUtil.wireChangeEvents(comp, comp, Events.ON_CHANGE);
        
        if (goalBase.getGroup() == GoalGroup.INACTIVE) {
            ZKUtil.disableChildren(form, true);
        } else {
            ZKUtil.focusFirst(comp, true);
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
     * Updates input elements from the goal/step.
     */
    private void populateControls() {
        txtReason.setText(goalBase.getReason());
        txtName.setText(goalBase.getName());
        
        if (!isStep && txtNoteHistory != null) {
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
        
        initRadio(rgStatus, goalBase.getStatus(), 0);
        
        if (goalBase.getGroup() == GoalGroup.DECLINED && actionType == ActionType.ADD) {
            Events.postEvent("onChanging", txtReason, null);
        }
    }
    
    /**
     * Updates the goal/stop from input elements.
     */
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
            goalBase.setStatus((GoalStatus) rgStatus.getSelectedItem().getValue());
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
        
    }
    
    /**
     * Populates radio group of goal types.
     */
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
    
    /**
     * Populates radio group of goal statuses.
     */
    private void populateGoalStatus() {
        if (rgStatus != null) {
            for (GoalStatus goalStatus : GoalStatus.values()) {
                if (goalStatus != GoalStatus.D) {
                    Radio radio = new Radio(goalStatus.toString());
                    radio.setValue(goalStatus);
                    rgStatus.appendChild(radio);
                }
            }
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
     * @param types Comma-delimited string of types. Each entry in the list may consist of one or
     *            more space-delimited types all of which must be present for the entry to be
     *            considered present. In other words, the comma delimiter represents an "OR"
     *            operation and the space delimiter represents an "AND" operation.
     * @return True if goal/step is one of the specified types.
     */
    public boolean isType(String types) {
        for (String type : types.split("\\,")) {
            boolean result = true;
            type = type.trim().toUpperCase();
            
            for (String type1 : type.split("\\ ")) {
                int idx = Arrays.asList(TYPES).indexOf(type1);
                
                switch (idx) {
                    case -1: // invalid
                        result = false;
                        break;
                        
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
                
                if (!result) {
                    break;
                }
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
    
    /**
     * Returns true if all required inputs are present.
     * 
     * @return True if all required inputs are present.
     */
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
                return isMissing(lblTypes);
            }
        }
        
        if (datFollowup != null && datFollowup.getValue() == null) {
            return isMissing(datFollowup);
        }
        
        return true;
    }
    
    /**
     * Displays the validation error for a required element.
     * 
     * @param target The target input element.
     * @return Always false.
     */
    private boolean isMissing(Component target) {
        wrongValue(target, requiredMessage);
        return false;
    }
    
    /**
     * Clears any current validation error and displays a new validation error for the specified
     * input element.
     * 
     * @param target The target input element.
     * @param message The validation error message.
     */
    private void wrongValue(Component target, String message) {
        if (wrongValueTarget != null) {
            Clients.clearWrongValue(wrongValueTarget);
        }
        
        wrongValueTarget = target;
        
        if (target != null && message != null) {
            Clients.wrongValue(target, message);
        }
    }
    
    /**
     * Sets the initial selection state of a radio group.
     * 
     * @param rg The radio group.
     * @param value The value used to identify the radio button to select.
     * @param defaultIndex The default selection index to use if match fails.
     */
    private void initRadio(Radiogroup rg, Object value, int defaultIndex) {
        if (rg == null) {
            return;
        }
        
        if (value != null) {
            for (Radio radio : rg.getItems()) {
                if (value.equals(radio.getValue())) {
                    rg.setSelectedItem(radio);
                    return;
                }
            }
        }
        
        rg.setSelectedIndex(defaultIndex);
    }
    
    /**
     * Commit changes and close the form when OK button is clicked.
     */
    public void onClick$btnOK() {
        if (commit()) {
            close(true);
        }
    }
    
    /**
     * Close the form when Cancel button is clicked, ignoring any changes.
     */
    public void onClick$btnCancel() {
        close(false);
    }
    
    /**
     * Clicking the close button on the tab is equivalent to clicking the Cancel button.
     * 
     * @param event The tab closure event.
     */
    public void onCloseTab(Event event) {
        ZKUtil.getEventOrigin(event).stopPropagation();
        close(false);
    }
    
    /**
     * Commits all changes.
     * 
     * @return True if the operation was successful.
     */
    private boolean commit() {
        if (!hasRequired()) {
            return false;
        }
        
        populateGoalBase();
        
        try {
            switch (actionType) {
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
        if (!force && !changeSet.isEmpty() && !PromptDialog
                .confirm("If you continue, you will lose unsaved changes.  Continue?", "Closing " + tab.getLabel())) {
            return;
        }
        
        tab.getTabbox().setSelectedIndex(0);
        tab.getLinkedPanel().detach();
        tab.detach();
    }
}
