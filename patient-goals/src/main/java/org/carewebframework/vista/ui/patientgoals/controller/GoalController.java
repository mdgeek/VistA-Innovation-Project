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

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.api.query.AbstractQueryFilter;
import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.api.security.ISecurityDomain;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.HybridModel.IGrouper;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.patientgoals.controller.AddEditController.ActionType;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.GoalType;
import org.carewebframework.vista.ui.patientgoals.model.Review;
import org.carewebframework.vista.ui.patientgoals.model.Step;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;
import org.carewebframework.vista.ui.patientgoals.view.GoalRenderer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Group;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Toolbar;

/**
 * Controller for patient goals list.
 */
public class GoalController extends AbstractGridController<Goal> {
    
    private static final long serialVersionUID = 1L;
    
    private static class QueryFilter<T extends GoalBase> extends AbstractQueryFilter<T> {
        
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
            notifyListeners();
        }
        
        @Override
        public boolean updateContext(IQueryContext context) {
            return false;
        }
        
        @Override
        public boolean include(T goalBase) {
            return group == null || goalBase.getGroup() == group;
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
            return Float.compare(goal1.getNumber(), goal2.getNumber());
        }
        
        @Override
        public int compareGroup(GrouperGroup group1, GrouperGroup group2) {
            return group1.group.ordinal() - group2.group.ordinal();
        }
        
    };
    
    private static GrouperGroup groupActive = new GrouperGroup("Active Goals", GoalGroup.ACTIVE);
    
    private static GrouperGroup groupInactive = new GrouperGroup("Inactive Goals", GoalGroup.INACTIVE);
    
    private static GrouperGroup groupDeclined = new GrouperGroup("Declined Goals", GoalGroup.DECLINED);
    
    private static QueryFilter<Goal> goalFilter = new QueryFilter<>();
    
    private static QueryFilter<Step> stepFilter = new QueryFilter<>();
    
    private static final Comparator<Review> reviewComparator = new Comparator<Review>() {
        
        @Override
        public int compare(Review review1, Review review2) {
            return review1.getNote().compareTo(review2.getNote());
        }
    };
    
    /**
     * Custom comparator is required for sorting goal types.
     */
    private static final Comparator<List<GoalType>> typeComparator = new Comparator<List<GoalType>>() {
        
        @Override
        public int compare(List<GoalType> types1, List<GoalType> types2) {
            return GoalRenderer.typeAsString(types1).compareToIgnoreCase(GoalRenderer.typeAsString(types2));
        }
        
    };
    
    // Start of auto-wired section
    
    private Toolbar toolbar;
    
    private Tabbox tabbox;
    
    // End of auto-wired section
    
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
        super(service, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, true, true, goalGrouper);
        setPaging(false);
    }
    
    public String getLabelClass(int i) {
        return "vistaPatientGoals-bold " + Constants.LABEL_SCLASS[i];
    }
    
    public String getGroupClass(int i) {
        return Constants.GROUP_SCLASS[i];
    }
    
    public Comparator<?> getTypeComparator() {
        return typeComparator;
    }
    
    public Comparator<?> getReviewComparator() {
        return reviewComparator;
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
    
    public void onReviewGroup(Event event) {
        Goal goal = (Goal) event.getData();
        AddEditController.execute(tabbox, goal, ActionType.REVIEW);
    }
    
    public void onAddStep(Event event) {
        Goal goal = (Goal) event.getData();
        newStep(ActionType.ADD, GoalGroup.ACTIVE, goal);
    }
    
    public void onReviewStep(Event event) {
        Step step = (Step) event.getData();
        AddEditController.execute(tabbox, step, ActionType.REVIEW);
    }
    
    private void newStep(ActionType actionType, GoalGroup goalGroup, Goal goal) {
        Step step = new Step(goal);
        step.setStatus(goalGroup == GoalGroup.ACTIVE ? "A;ACTIVE" : "I;INACTIVE");
        step.setName(ZKUtil.getLabel("vistaPatientGoals.new_.step.name"));
        step.setStartDate(FMDate.today());
        IUser user = UserContext.getActiveUser();
        ISecurityDomain domain = user.getSecurityDomain();
        step.setFacility(domain.getLogicalId() + ";" + domain.getName());
        step.setProvider(user.getLogicalId());
        AddEditController.execute(tabbox, step, actionType);
    }
    
    public void registerStepController(StepController controller) {
        controller.registerQueryFilter(stepFilter);
    }
    
    public void unregisterStepController(StepController controller) {
        controller.unregisterQueryFilter(stepFilter);
    }
    
    public void onClick$btnNewGoal() {
        newGoal(ActionType.ADD, GoalGroup.ACTIVE);
    }
    
    public void onClick$btnNewDeclined() {
        newGoal(ActionType.ADD, GoalGroup.DECLINED);
    }
    
    private void newGoal(ActionType actionType, GoalGroup goalGroup) {
        Goal goal = new Goal();
        goal.setPatient(getPatient());
        goal.setDeclined(goalGroup == GoalGroup.DECLINED);
        goal.setStatus(goalGroup == GoalGroup.ACTIVE ? "A;ACTIVE" : "I;INACTIVE");
        goal.setName(ZKUtil.getLabel("vistaPatientGoals.new_.goal.name"));
        goal.setStartDate(FMDate.today());
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
    
    /**
     * Receives event from add/edit controller when a commit of a new goal/step has succeeded.
     * 
     * @param event For adds, the event data is the goal or step committed. Otherwise, the event
     *            data is null.
     */
    public void onCommit(Event event) {
        Object data = event.getData();
        
        if (data instanceof Goal) {
            getModel().add((Goal) data);
        }
        
        applyFilters();
    }
    
    @Override
    public Date getDateByType(Goal goal, DateType dateType) {
        return dateType == DateType.UPDATED ? goal.getLastUpdated() : goal.getFollowupDate();
    }
    
    @Override
    public String onPatientChanging() {
        if (pendingChanges()
                && !PromptDialog.confirm(
                    "Patient goals has pending changes.  If you continue, all changes will be lost.  Continue?",
                    "Pending Changes")) {
            return "Patient goals has pending changes.";
        }
        
        return null;
    }
    
    /**
     * Returns true if any tab has pending changes.
     * 
     * @return True if pending changes exist.
     */
    private boolean pendingChanges() {
        for (Tab tab : tabbox.getTabs().<Tab> getChildren()) {
            if (tab.hasAttribute("changed")) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public void onPatientChanged(Patient patient) {
        super.onPatientChanged(patient);
        ZKUtil.disableChildren(toolbar, patient == null);
        tabbox.setSelectedIndex(0);
        
        while (tabbox.getTabs().getChildren().size() > 1) {
            tabbox.getTabs().getChildren().remove(1);
            tabbox.getTabpanels().getChildren().remove(1);
        }
    }
    
    @Override
    public void refresh() {
        super.refresh();
        Clients.resize(root);
    }
    
}
