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
import java.util.List;
import java.util.Map;

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
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;

/**
 * Controller adding or editing goals and steps
 */
public class AddEditController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = "~./org/carewebframework/vista/ui/patientgoals/add-edit.zul";
    
    private static final String[] TYPES = { "GOAL", "STEP", "ACTIVE", "INACTIVE", "DECLINED", "ADD", "REVIEW" };
    
    public enum ActionType {
        ADD, REVIEW;
        
        public String getLabel(GoalBase goalBase) {
            Goal goal = goalBase instanceof Goal ? (Goal) goalBase : ((Step) goalBase).getGoal();
            Step step = goalBase instanceof Step ? (Step) goalBase : null;
            return StrUtil.formatMessage("@vistaPatientGoals.addedit.tab.label." + (step != null ? "step" : "goal") + "."
                    + goalBase.getGroup().name().toLowerCase() + "." + this.name().toLowerCase(), goal.getName(),
                step == null ? null : step.getNumberString());
        }
    }
    
    private ActionType actionType;
    
    private GoalBase goalBase;
    
    private Goal goal;
    
    private Step step;
    
    private final GoalService service;
    
    private boolean isStep;
    
    // Auto-wired members
    
    private Component goalTypes;
    
    private Tab tab;
    
    private Component form;
    
    private Textbox txtName;
    
    private Textbox txtReason;
    
    private Textbox txtNotesHistory;
    
    private Textbox txtNote;
    
    private Datebox datStart;
    
    private Datebox datFollowup;
    
    private Radiogroup rgStatus;
    
    public static boolean execute(Tabbox tabbox, GoalBase goalBase, ActionType actionType) {
        Tab tab = findTab(tabbox, goalBase, actionType);
        
        if (tab != null) {
            tabbox.setSelectedTab(tab);
            return false;
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
        return true;
    }
    
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
    
    private static String createId(GoalBase goalBase, ActionType actionType) {
        return actionType + "." + goalBase.getGroup() + "." + goalBase.getIEN()
                + (goalBase instanceof Step ? "." + ((Step) goalBase).getGoal().getIEN() : "");
    }
    
    public AddEditController(GoalService service) {
        this.service = service;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        goalBase = (GoalBase) arg.get("goalBase");
        actionType = (ActionType) arg.get("actionType");
        tab = (Tab) arg.get("tab");
        tab.addForward(Events.ON_CLOSE, comp, "onCloseTab");
        isStep = goalBase instanceof Step;
        
        if (isStep) {
            step = (Step) goalBase;
        } else {
            goal = (Goal) goalBase;
        }
        
        tab.setLabel(actionType.getLabel(goalBase));
        tab.setValue(createId(goalBase, actionType));
        populateGoalTypes();
        populateControls();
        
        if (goalBase.getGroup() == GoalGroup.INACTIVE) {
            ZKUtil.disableChildren(form, true);
        }
    }
    
    private void populateControls() {
        txtReason.setText(goalBase.getReason());
        
        if (!isStep) {
            txtName.setText(goal.getName());
            StringBuilder sb = new StringBuilder();
            
            for (Review review : goal.getReviews()) {
                sb.append(review).append('\n');
            }
            
            txtNotesHistory.setText(sb.toString());
        }
        
        if (goalBase.getStartDate() != null) {
            datStart.setValue(goalBase.getStartDate());
        }
        
        if (goalBase.getFollowupDate() != null) {
            datFollowup.setValue(goalBase.getFollowupDate());
        }
        
        findRadio(goalBase.getStatusCode()).setChecked(true);
        Checkbox chk = null;
        
        while ((chk = ZKUtil.findChild(goalTypes, Checkbox.class, chk)) != null) {
            chk.setChecked(goal.getTypes().contains((chk.getValue())));
        }
    }
    
    private void populateGoalBase() {
        goalBase.setReason(txtReason.getText());
        
        if (!isStep) {
            goal.setName(txtName.getText());
            goal.setStartDate(datStart.getValue() == null ? null : new FMDate(datStart.getValue()));
            goal.setFollowupDate(datFollowup.getValue() == null ? null : new FMDate(datFollowup.getValue()));
            String note = txtNote.getText();
            
            if (note != null && !note.isEmpty()) {
                Review review = new Review(new FMDate(), note);
                goal.getReviews().add(review);
            }
        }
        
        goalBase.setStatus(rgStatus.getSelectedItem().getValue().toString());
        goalBase.getTypes().clear();
        goalBase.setStatus(rgStatus.getSelectedItem().getValue().toString());
        Checkbox chk = null;
        
        while ((chk = ZKUtil.findChild(goalTypes, Checkbox.class, chk)) != null) {
            if (chk.isChecked()) {
                goalBase.getTypes().add((GoalType) chk.getValue());
            }
        }
    }
    
    private void populateGoalTypes() {
        Component parent;
        
        if (isStep) {
            parent = new Radiogroup();
            goalTypes.appendChild(parent);
        } else {
            parent = goalTypes;
        }
        
        for (GoalType goalType : service.getGoalTypes()) {
            Checkbox chk = isStep ? new Radio() : new Checkbox();
            parent.appendChild(chk);
            chk.setLabel(goalType.toString());
            chk.setValue(goalType);
        }
    }
    
    public boolean isType(String actions) {
        for (String action : actions.split("\\,")) {
            boolean result = false;
            int type = Arrays.asList(TYPES).indexOf(action.trim().toUpperCase());
            
            switch (type) {
                case 0: // GOAL
                case 1: // STEP
                    result = type == 0 ? !isStep : isStep;
                    break;
                
                case 2: // ACTIVE
                case 3: // INACTIVE
                case 4: // DECLINED
                    result = goalBase.getGroup() == GoalGroup.values()[type - 2];
                    break;
                
                case 5: // ADD
                case 6: // REVIEW
                    result = actionType == ActionType.values()[type - 5];
                    break;
            }
            
            if (result) {
                return true;
            }
        }
        
        return false;
    }
    
    private boolean hasRequired() {
        return true;
    }
    
    private Radio findRadio(String statusCode) {
        for (Radio radio : rgStatus.getItems()) {
            if (StrUtil.piece(radio.getValue().toString(), ";").equals(statusCode)) {
                return radio;
            }
        }
        
        return rgStatus.getItemAtIndex(0);
    }
    
    public void onClick$btnOK() {
        if (commit()) {
            close(true);
        }
    }
    
    public void onClick$btnCancel() {
        close(false);
    }
    
    public void onCloseTab(Event event) {
        ZKUtil.getEventOrigin(event).stopPropagation();
        close(false);
    }
    
    private boolean commit() {
        populateGoalBase();
        
        if (!hasRequired()) {
            return false;
        }
        
        try {
            switch (actionType) {
                case ADD:
                    if (isStep) {
                        // service.addStep(step);
                    } else {
                        service.addGoal(goal);
                    }
                    break;
                
                case REVIEW:
                    if (isStep) {
                        // service.updateStep(step);
                    } else {
                        // service.updateGoal(goal);
                    }
            }
        } catch (Exception e) {
            PromptDialog.showError(e);
            return false;
        }
        
        return true;
    }
    
    private void close(boolean force) {
        tab.getTabbox().setSelectedIndex(0);
        tab.getLinkedPanel().detach();
        tab.detach();
    }
}
