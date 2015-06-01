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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.GoalType;
import org.carewebframework.vista.ui.patientgoals.model.Step;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;

/**
 * Controller adding or editing goals and steps
 */
public class AddEditController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = "~./org/carewebframework/vista/ui/patientgoals/add-edit.zul";
    
    public enum ActionType {
        ADD_GOAL_ACTIVE("New Goal"), REVIEW_GOAL_ACTIVE("Review Active Goal %s"), REVIEW_GOAL_INACTIVE(
                "Review Inactive Goal %s"), ADD_GOAL_DECLINED("New Declined"), REVIEW_GOAL_DECLINED(
                "Review Declined Goal %s"), ADD_STEP_ACTIVE("New Step"), REVIEW_STEP_ACTIVE("Review Active Step %s"), REVIEW_STEP_INACTIVE(
                "Review Inactive Step %s");
        
        private String label;
        
        ActionType(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    private ActionType actionType;
    
    private GoalBase goalBase;
    
    private final GoalService service;
    
    private boolean isStep;
    
    // Auto-wired members
    
    private Component goalTypes;
    
    private Tab tab;
    
    private Component form;
    
    public static boolean execute(Tabbox tabbox, GoalBase goalBase, ActionType actionType) {
        Tab tab = findTab(tabbox, goalBase, actionType);
        
        if (tab != null) {
            tabbox.setSelectedTab(tab);
            return false;
        }
        
        tab = new Tab();
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
        return goalBase.getIEN() + actionType;
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
        isStep = goalBase instanceof Step;
        populateGoalTypes();
        Goal goal = isStep ? ((Step) goalBase).getGoal() : ((Goal) goalBase);
        tab.setLabel(StrUtil.formatMessage(actionType.getLabel(), goal.getName()));
        tab.setValue(createId(goalBase, actionType));
        
        if (goalBase.getGroup() == GoalGroup.INACTIVE) {
            ZKUtil.disableChildren(form, true);
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
            action = action.trim();
            
            if (!action.contains("_")) {
                return goalBase.getGroup() == GoalGroup.valueOf(action);
            }
            
            if (actionType == ActionType.valueOf(action)) {
                return true;
            }
        }
        
        return false;
    }
    
    public void onClick$btnOK() {
        close();
    }
    
    public void onClick$btnCancel() {
        close();
    }
    
    private void close() {
        tab.getTabbox().setSelectedIndex(0);
        tab.getLinkedPanel().detach();
        tab.detach();
    }
}
