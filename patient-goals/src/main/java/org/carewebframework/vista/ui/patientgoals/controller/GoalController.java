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

import java.util.Date;

import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.apache.commons.lang.ObjectUtils;

import org.carewebframework.api.query.AbstractQueryFilter;
import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.cal.ui.reporting.query.DateQueryFilter.DateType;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.HybridModel.IGrouper;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.controller.AddEditController.ActionType;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.Step;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Group;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Toolbar;

/**
 * Controller for patient goals list.
 */
public class GoalController extends AbstractGridController<Goal> {
    
    private static final long serialVersionUID = 1L;
    
    private static abstract class BaseFilter<T> extends AbstractQueryFilter<T> {
        
        protected GoalGroup group;
        
        protected Checkbox chkActive;
        
        public void updateState(Event event) {
            event = ZKUtil.getEventOrigin(event);
            Checkbox chkActive = (Checkbox) event.getTarget();
            String group = chkActive.isChecked() ? (String) chkActive.getValue() : null;
            
            if (this.chkActive != null && this.chkActive != chkActive) {
                this.chkActive.setChecked(false);
            }
            
            this.chkActive = chkActive;
            this.group = group == null ? null : GoalGroup.values()["AID".indexOf(group)];
            notifyListener();
        }
        
        @Override
        public boolean updateContext(IQueryContext context) {
            return false;
        }
        
    }
    
    protected static class GoalFilter extends BaseFilter<Goal> {
        
        @Override
        public boolean include(Goal goal) {
            return group == null || goal.getGroup() == group;
        }
    }
    
    protected static class StepFilter extends BaseFilter<Step> {
        
        @Override
        public boolean include(Step step) {
            return group == null || step.getGroup() == group;
        }
        
    }
    
    public static class GrouperGroup {
        
        private final String label;
        
        private final GoalGroup group;
        
        public GrouperGroup(String label, GoalGroup group) {
            this.label = label;
            this.group = group;
        }
        
        public GoalGroup getGroup() {
            return group;
        }
        
    }
    
    private static IGrouper<Goal, GrouperGroup> goalGrouper = new IGrouper<Goal, GrouperGroup>() {
        
        @Override
        public GrouperGroup getGroup(Goal goal) {
            switch (goal.getGroup()) {
                case ACTIVE:
                    return groupActive;
                case INACTIVE:
                    return groupInactive;
                case DECLINED:
                    return groupDeclined;
                default:
                    return null;
            }
        }
        
        @Override
        public String getGroupName(GrouperGroup group) {
            return group.label;
        }
        
        @Override
        public int compareElement(Goal goal1, Goal goal2) {
            return ObjectUtils.compare(goal1.getName(), goal2.getName());
        }
        
        @Override
        public int compareGroup(GrouperGroup group1, GrouperGroup group2) {
            return group1.group.ordinal() - group2.group.ordinal();
        }
        
    };
    
    private static GrouperGroup groupActive = new GrouperGroup("Active Goals", GoalGroup.ACTIVE);
    
    private static GrouperGroup groupInactive = new GrouperGroup("Inactive Goals", GoalGroup.INACTIVE);
    
    private static GrouperGroup groupDeclined = new GrouperGroup("Declined Goals", GoalGroup.DECLINED);
    
    private static GoalFilter goalFilter = new GoalFilter();
    
    private static StepFilter stepFilter = new StepFilter();
    
    private Toolbar toolbar;
    
    private Tabbox tabbox;
    
    /**
     * Allows the step controller to find the enclosing goal controller.
     * 
     * @param stepRoot Root component of the step view.
     * @return The goal controller.
     */
    public static GoalController findController(Component stepRoot) {
        return (GoalController) FrameworkController.getController(stepRoot.getParent(), true);
    }
    
    public GoalController(GoalService service) {
        super(service, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, true, goalGrouper);
        setPaging(false);
    }
    
    public String getLabelClass(int i) {
        return "vistaPatientGoals-bold " + Constants.LABEL_SCLASS[i];
    }
    
    public String getGroupClass(int i) {
        return Constants.GROUP_SCLASS[i];
    }
    
    @Override
    protected void initializeController() {
        super.initializeController();
        registerQueryFilter(goalFilter);
    }
    
    /**
     * Determine goal filter change from onCheck event.
     * 
     * @param event The onCheck event.
     */
    public void onGoalFilterChange(Event event) {
        goalFilter.updateState(event);
    }
    
    /**
     * Determine step filter change from onCheck event.
     * 
     * @param event The onCheck event.
     */
    public void onStepFilterChange(Event event) {
        stepFilter.updateState(event);
    }
    
    public void registerStepController(StepController controller) {
        controller.registerQueryFilter(stepFilter);
    }
    
    public void onClick$btnNewGoal() {
        newGoal(ActionType.ADD_GOAL_ACTIVE);
    }
    
    public void onClick$btnNewDeclined() {
        newGoal(ActionType.ADD_GOAL_DECLINED);
    }
    
    private void newGoal(ActionType actionType) {
        Goal goal = new Goal();
        goal.setPatient(getPatient());
        goal.setName(actionType.getLabel());
        goal.setDeclined(actionType == ActionType.ADD_GOAL_DECLINED);
        goal.setStatus("A;ACTIVE");
        AddEditController.execute(tabbox, goal, actionType);
    }
    
    public void onClick$btnExpandAll() {
        openAll(true);
    }
    
    public void onClick$btnCollapseAll() {
        openAll(false);
    }
    
    private void openAll(boolean open) {
        for (Row row : getGrid().getRows().<Row> getChildren()) {
            Detail detail = row.getDetailChild();
            
            if (row instanceof Group) {
                ((Group) row).setOpen(open);
            }
            
            if (detail != null) {
                detail.setOpen(open);
            }
        }
    }
    
    @Override
    public Date getDateByType(Goal goal, DateType dateType) {
        return dateType == DateType.UPDATED ? goal.getLastUpdated() : goal.getFollowupDate();
    }
    
    @Override
    public void onPatientChanged(Patient patient) {
        super.onPatientChanged(patient);
        ZKUtil.disableChildren(toolbar, patient == null);
    }
    
    @Override
    public void refresh() {
        super.refresh();
        Clients.resize(root);
    }
    
}
