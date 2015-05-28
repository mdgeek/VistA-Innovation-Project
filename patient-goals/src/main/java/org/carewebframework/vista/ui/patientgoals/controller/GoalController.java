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
import java.util.HashSet;
import java.util.Set;

import org.carewebframework.api.query.AbstractQueryFilter;
import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.cal.ui.reporting.query.DateQueryFilter.DateType;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.ui.patientgoals.model.Goal;

import org.zkoss.zk.ui.util.Clients;

/**
 * Controller for patient goals list.
 */
public class GoalController extends AbstractGridController<Goal> {
    
    private static final long serialVersionUID = 1L;
    
    private static class StatusFilter extends AbstractQueryFilter<Goal> {
        
        private final Set<String> status;
        
        public StatusFilter(String status) {
            this.status = new HashSet<>(StrUtil.toList(status, ","));
        }
        
        @Override
        public boolean include(Goal result) {
            return status.contains(result.getStatusCode());
        }
        
        @Override
        public boolean updateContext(IQueryContext context) {
            return false;
        }
        
    }
    
    public GoalController() {
        super(null, "vistaPatientGoals", "BEHOPG", null, false, null);
        setPaging(false);
    }
    
    @Override
    protected void initializeController() {
        super.initializeController();
        registerQueryFilter(new StatusFilter((String) arg.get("status")));
    }
    
    @Override
    public Date getDateByType(Goal goal, DateType dateType) {
        return dateType == DateType.UPDATED ? goal.getLastUpdated() : goal.getFollowupDate();
    }
    
    @Override
    public void refresh() {
        super.refresh();
        Clients.resize(root);
    }
    
}
