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
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.HybridModel.IGrouper;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.patientgoals.controller.AddEditController.ActionType;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalStatus;
import org.carewebframework.vista.ui.patientgoals.model.GoalType;
import org.carewebframework.vista.ui.patientgoals.model.Review;
import org.carewebframework.vista.ui.patientgoals.model.Step;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;
import org.carewebframework.vista.ui.patientgoals.view.GoalRenderer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.OpenEvent;
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
    
    /**
     * This is the base class for the goal and the step filter.
     *
     * @param <T> This will be either the Goal or the Step class.
     */
    private static class QueryFilter<T extends GoalBase> extends AbstractQueryFilter<T> {
        
        protected GoalGroup group;
        
        protected Checkbox chkActive;
        
        /**
         * Accepts an onCheck event from one of the query filter check boxes and updates the filter
         * state. It also contains logic to uncheck the previously checked option, if any.
         * 
         * @param event The onCheck event from a query filter check box.
         */
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
        
        /**
         * A goal or step is included if its group belongs to the currently selected filter group.
         */
        @Override
        public boolean include(T goalBase) {
            return !goalBase.isDeleted() && (group == null || goalBase.getGroup() == group);
        }
        
    }
    
    /**
     * This class defines the attributes of a single goal group. It is used by the grouper for
     * displaying and ordering the groups.
     */
    public static class GrouperGroup implements Comparable<GrouperGroup> {
        
        private final String label;
        
        private final GoalGroup group;
        
        private boolean open;
        
        public GrouperGroup(GoalGroup group) {
            this.label = StrUtil.getLabel(Constants.LABEL_PREFIX + ".goal.group." + group.name().toLowerCase() + ".label");
            this.group = group;
            this.open = true;
        }
        
        public GoalGroup getGroup() {
            return group;
        }
        
        public boolean isOpen() {
            return open;
        }
        
        public void setOpen(boolean open) {
            this.open = open;
        }
        
        @Override
        public int compareTo(GrouperGroup group2) {
            return group.ordinal() - group2.group.ordinal();
        }
        
    }
    
    /**
     * This is the grouper that informs the group model how to group goals. It causes goals to be
     * grouped into one of three categories: active, inactive, declined.
     */
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
            return goal1.compareTo(goal2);
        }
        
        @Override
        public int compareGroup(GrouperGroup group1, GrouperGroup group2) {
            return group1.compareTo(group2);
        }
        
    };
    
    private static GrouperGroup groupActive = new GrouperGroup(GoalGroup.ACTIVE);
    
    private static GrouperGroup groupInactive = new GrouperGroup(GoalGroup.INACTIVE);
    
    private static GrouperGroup groupDeclined = new GrouperGroup(GoalGroup.DECLINED);
    
    private static QueryFilter<Goal> goalFilter = new QueryFilter<>();
    
    private static QueryFilter<Step> stepFilter = new QueryFilter<>();
    
    /**
     * Custom comparator used to sort the column displaying the review note.
     */
    private static final Comparator<Review> reviewComparator = new Comparator<Review>() {
        
        @Override
        public int compare(Review review1, Review review2) {
            return review1.getNote().compareTo(review2.getNote());
        }
    };
    
    /**
     * Custom comparator used to sort the column display the goal types.
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
    
    private final GoalService service;
    
    /**
     * Allows the step controller to find the enclosing goal controller.
     * 
     * @param stepRoot Root component of the step view.
     * @return The goal controller.
     */
    public static GoalController findController(Component stepRoot) {
        return (GoalController) FrameworkController.getController(stepRoot.getParent(), true);
    }
    
    /**
     * Creates the controller for the main page (goal list). It uses the following settings:
     * <ul>
     * <li>Prefix for label references:
     * {@value org.carewebframework.vista.ui.patientgoals.controller.Constants#LABEL_PREFIX}</li>
     * <li>Prefix for property values:
     * {@value org.carewebframework.vista.ui.patientgoals.controller.Constants#PROPERTY_PREFIX}</li>
     * <li>No style sheet for printing (for now).</li>
     * <li>Responds to patient context changes.</li>
     * <li>Auto-wires comparators to grid columns.</li>
     * <li>Uses a grouper for organizing goals into three groups (active, inactive, declined).</li>
     * </ul>
     * 
     * @param service The goal service instance that has API's for manipulating goals and steps.
     */
    public GoalController(GoalService service) {
        super(service, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, true, true, goalGrouper);
        this.service = service;
        setPaging(false);
    }
    
    // Start of EL methods.
    
    /**
     * Returns the style classes to apply to UI text elements representing a given goal group. This
     * is used by EL expressions in the main zul page.
     * 
     * @param i Ordinal value of the {@link GoalGroup}.
     * @return The CSS style classes.
     */
    public String getLabelClass(int i) {
        return "vistaPatientGoals-bold " + Constants.LABEL_SCLASS[i];
    }
    
    /**
     * Returns the style classes to apply to UI list elements representing a given goal group. This
     * is used by EL expressions in the main zul page.
     * 
     * @param i Ordinal value of the {@link GoalGroup}.
     * @return The CSS style classes.
     */
    public String getGroupClass(int i) {
        return Constants.GROUP_SCLASS[i];
    }
    
    /**
     * Returns the custom comparator used for sorting the type column in the grid. It allows the
     * comparator to be referenced using EL expressions in the main zul page.
     * 
     * @return A custom comparator.
     */
    public Comparator<?> getTypeComparator() {
        return typeComparator;
    }
    
    /**
     * Returns the custom comparator used for sorting the note column in the grid. It allows the
     * comparator to be referenced using EL expressions in the main zul page.
     * 
     * @return A custom comparator.
     */
    public Comparator<?> getReviewComparator() {
        return reviewComparator;
    }
    
    // End of EL methods
    
    /**
     * Register the goal query filter.
     */
    @Override
    protected void initializeController() {
        super.initializeController();
        registerQueryFilter(goalFilter);
    }
    
    /**
     * Called whenever a group is expanded or collapsed.
     * 
     * @param event The open event.
     */
    public void onGroupOpen(Event event) {
        GrouperGroup gg = (GrouperGroup) event.getData();
        OpenEvent openEvent = (OpenEvent) ZKUtil.getEventOrigin(event);
        gg.setOpen(openEvent.isOpen());
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
    
    // Start of goal action handlers.
    
    /**
     * Handle request to add a new goal.
     */
    public void onAddGoal() {
        newGoal(ActionType.ADD, GoalGroup.ACTIVE);
    }
    
    /**
     * Handle a request to add a declined goal.
     */
    public void onAddDeclined() {
        newGoal(ActionType.ADD, GoalGroup.DECLINED);
    }
    
    /**
     * Handle request to review a goal.
     * 
     * @param event The trigger event.
     */
    public void onReviewGoal(Event event) {
        Goal goal = (Goal) event.getData();
        AddEditController.execute(tabbox, goal, ActionType.REVIEW);
    }
    
    /**
     * Initialize a new goal base instance with default values.
     * 
     * @param goalBase Goal base to initialize.
     */
    private void initGoalBase(GoalBase goalBase, GoalGroup goalGroup) {
        IUser user = UserContext.getActiveUser();
        ISecurityDomain domain = user.getSecurityDomain();
        goalBase.setFacility(domain.getLogicalId() + ";" + domain.getName());
        goalBase.setProvider(user.getLogicalId() + ";" + user.getFullName());
        goalBase.setStartDate(FMDate.today());
        
        if (goalGroup == GoalGroup.DECLINED) {
            goalBase.setStatus(GoalStatus.D);
            goalBase.getTypes().add(service.getGoalType("OTHER"));
            goalBase.setFollowupDate(new FMDate(DateUtil.addDays(new Date(), 14, true)));
            goalBase.setReason(StrUtil.getLabel("vistaPatientGoals.new_declined.reason"));
        } else {
            goalBase.setStatus(goalGroup == GoalGroup.ACTIVE ? GoalStatus.A : GoalStatus.S);
        }
    }
    
    /**
     * Creates a new goal with the specified default values.
     * 
     * @param actionType The action type (should always be ADD).
     * @param goalGroup The group (ACTIVE or DECLINED)
     */
    private void newGoal(ActionType actionType, GoalGroup goalGroup) {
        Goal goal = new Goal();
        initGoalBase(goal, goalGroup);
        goal.setPatient(getPatient());
        goal.setDeclined(goalGroup == GoalGroup.DECLINED);
        goal.setName(StrUtil.getLabel("vistaPatientGoals.new_" + (goal.isDeclined() ? "declined" : "goal") + ".name"));
        AddEditController.execute(tabbox, goal, actionType);
    }
    
    // End of goal action handlers
    
    // Start of step action handlers
    
    /**
     * Handle request to add a step.
     * 
     * @param event The trigger event.
     */
    public void onAddStep(Event event) {
        Goal goal = (Goal) event.getData();
        newStep(ActionType.ADD, GoalGroup.ACTIVE, goal);
    }
    
    /**
     * Handle request to review a step.
     * 
     * @param event The trigger event.
     */
    public void onReviewStep(Event event) {
        Step step = (Step) event.getData();
        AddEditController.execute(tabbox, step, ActionType.REVIEW);
    }
    
    /**
     * Creates a new step with the specified default values.
     * 
     * @param actionType The action type (should always be ADD).
     * @param goalGroup The group (ACTIVE or INACTIVE)
     * @param goal The parent goal.
     */
    private void newStep(ActionType actionType, GoalGroup goalGroup, Goal goal) {
        Step step = new Step(goal);
        initGoalBase(step, goalGroup);
        step.setName(StrUtil.getLabel("vistaPatientGoals.new_step.name"));
        AddEditController.execute(tabbox, step, actionType);
    }
    
    // End of step action handlers
    
    /**
     * The step controller for each detail view calls this after initialization. The action is to
     * register the step filter (which is shared across step controllers) with the step controller.
     * 
     * @param controller The step controller.
     */
    public void registerStepController(StepController controller) {
        controller.registerQueryFilter(stepFilter);
    }
    
    /**
     * The step controller for each detail view calls this during during cleanup. The action is to
     * unregister the step filter (which is shared across step controllers) from the step
     * controller.
     * 
     * @param controller The step controller.
     */
    public void unregisterStepController(StepController controller) {
        controller.unregisterQueryFilter(stepFilter);
    }
    
    // Start of view expansion control.
    
    /**
     * Expands all groups and details.
     */
    public void onClick$btnExpandAll() {
        openAll(true);
    }
    
    /**
     * Collapses all groups and details.
     */
    public void onClick$btnCollapseAll() {
        openAll(false);
    }
    
    /**
     * Expands or collapses all groups and details in the current view.
     * 
     * @param open True = expand operation. False = collapse operation.
     */
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
        
        groupActive.setOpen(open);
        groupDeclined.setOpen(open);
        groupInactive.setOpen(open);
    }
    
    // End of view expansion control.
    
    /**
     * Receives event from the add/edit controller when a commit of changes has succeeded.
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
    
    /**
     * Extracts the date of the specified type from the goal. This isn't currently used because
     * there is no filter for date ranges.
     */
    @Override
    public Date getDateByType(Goal goal, DateType dateType) {
        return dateType == DateType.UPDATED ? goal.getLastUpdated() : goal.getFollowupDate();
    }
    
    // Start of context change handling.
    
    /**
     * Called when the patient context change is about to occur. Checks to make sure that there are
     * no pending changes.
     */
    @Override
    public String onPatientChanging(boolean silent) {
        if (!silent
                && pendingChanges()
                && !PromptDialog.confirm("@vistaPatientGoals.changes_pending.message",
                    "@vistaPatientGoals.changes_pending.title")) {
            return StrUtil.getLabel("vistaPatientGoals.changes_pending.response");
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
    
    /**
     * Updates the controller state to reflect the context change. Removes any add/edit tabs.
     */
    @Override
    public void onPatientChanged(Patient patient) {
        super.onPatientChanged(patient);
        ZKUtil.disableChildren(toolbar, patient == null || !service.canModify(patient));
        tabbox.setSelectedIndex(0);
        
        while (tabbox.getTabs().getChildren().size() > 1) {
            tabbox.getTabs().getChildren().remove(1);
            tabbox.getTabpanels().getChildren().remove(1);
        }
    }
    
    // End of context change handling
    
    @Override
    public void refresh() {
        super.refresh();
        Clients.resize(root);
    }
    
}
