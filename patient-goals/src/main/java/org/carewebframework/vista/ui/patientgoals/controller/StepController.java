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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.Step;

import org.zkoss.zul.Detail;
import org.zkoss.zul.ListModelList;

/**
 * Controller for goal steps.
 */
public class StepController extends AbstractGridController<Step, Step> {
    
    private static final long serialVersionUID = 1L;
    
    private GoalController goalController;
    
    /**
     * Creates the controller for the detail view (step list). It uses the following settings:
     * <ul>
     * <li>Does not specify a service.</li>
     * <li>Prefix for label references:
     * {@value org.carewebframework.vista.ui.patientgoals.controller.Constants#LABEL_PREFIX}</li>
     * <li>Prefix for property values:
     * {@value org.carewebframework.vista.ui.patientgoals.controller.Constants#PROPERTY_PREFIX}</li>
     * <li>No style sheet for printing (for now).</li>
     * <li>Does not respond to patient context changes.</li>
     * <li>Auto-wires comparators to grid columns.</li>
     * <li>Does not use a grouper.</li>
     * </ul>
     */
    public StepController() {
        super(null, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, false, true, null);
        setPaging(false);
    }
    
    /**
     * Initializes the controller:
     * <ul>
     * <li>Registers itself with the main controller.</li>
     * <li>Retrieves the step list and sets it into the model.</li>
     * </ul>
     */
    @Override
    protected void initializeController() {
        super.initializeController();
        goalController = GoalController.findController(root);
        goalController.registerStepController(this);
        Collection<Step> steps = ((Goal) arg.get("goal")).getSteps();
        setModel(new ListModelList<Step>(steps));
    }
    
    @Override
    protected void modelChanged(List<Step> filteredModel) {
        ZKUtil.findAncestor(root, Detail.class).invalidate();
        super.modelChanged(filteredModel);
    }
    
    /**
     * Extracts the date of the specified type from the step. This isn't currently used because
     * there is no filter for date ranges.
     */
    
    @Override
    public Date getDateByType(Step step, DateType dateType) {
        return dateType == DateType.UPDATED ? step.getLastUpdated() : step.getFollowupDate();
    }
    
    /**
     * Unregisters itself upon cleanup.
     */
    @Override
    public void cleanup() {
        super.cleanup();
        goalController.unregisterStepController(this);
    }
    
    @Override
    protected List<Step> toModel(List<Step> results) {
        return results;
    }
    
}
