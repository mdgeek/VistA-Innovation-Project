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

import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.Step;

import org.zkoss.zul.ListModelList;

/**
 * Controller for goal steps.
 */
public class StepController extends AbstractGridController<Step> {
    
    private static final long serialVersionUID = 1L;
    
    private GoalController goalController;
    
    public StepController() {
        super(null, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, false, true, null);
        setPaging(false);
    }
    
    @Override
    protected void initializeController() {
        super.initializeController();
        goalController = GoalController.findController(root);
        goalController.registerStepController(this);
        Collection<Step> steps = ((Goal) arg.get("goal")).getSteps();
        setModel(new ListModelList<Step>(steps));
    }
    
    @Override
    public Date getDateByType(Step step, DateType dateType) {
        return dateType == DateType.UPDATED ? step.getLastUpdated() : step.getFollowupDate();
    }
    
    @Override
    public void cleanup() {
        goalController.unregisterStepController(this);
    }
    
}
