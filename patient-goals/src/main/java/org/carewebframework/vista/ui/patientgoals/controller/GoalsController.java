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

import org.carewebframework.cal.api.query.AbstractServiceContext.DateMode;
import org.carewebframework.cal.ui.reporting.controller.AbstractListController;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;

/**
 * Controller for patient goals list.
 */
public class GoalsController extends AbstractListController<Goal> {
    
    private static final long serialVersionUID = 1L;
    
    public GoalsController(GoalService service) {
        super(service, "vistaPatientGoals", "BGOPG", null);
        setPaging(false);
    }
    
    @Override
    protected Date getDate(Goal result, DateMode dateMode) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
