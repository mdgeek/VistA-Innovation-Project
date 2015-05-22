/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.service;

import org.carewebframework.cal.api.query.AbstractServiceContext;
import org.carewebframework.cal.api.query.IDataService;
import org.carewebframework.cal.api.query.IQueryResult;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.ui.patientgoals.model.Goal;

/**
 * Data service for patient goals.
 */
public class GoalService implements IDataService<Goal> {
    
    private final BrokerSession brokerSession;
    
    private int asyncHandle;
    
    public GoalService(BrokerSession brokerSession) {
        this.brokerSession = brokerSession;
    }
    
    @Override
    public IQueryResult<Goal> fetchData(AbstractServiceContext<Goal> serviceContext) throws Exception {
        abort();
        return null;
    }
    
    @Override
    public void abort() {
        if (asyncHandle > 0) {
            brokerSession.callRPCAbort(asyncHandle);
            asyncHandle = 0;
        }
    }
    
}
