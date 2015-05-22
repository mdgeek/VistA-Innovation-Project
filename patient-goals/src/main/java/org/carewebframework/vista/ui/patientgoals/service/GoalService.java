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

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.cal.api.query.AbstractServiceContext;
import org.carewebframework.cal.api.query.IDataService;
import org.carewebframework.cal.api.query.IQueryResult;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.BrokerSession.IAsyncRPCEvent;
import org.carewebframework.vista.ui.patientgoals.model.Goal;

/**
 * Data service for patient goals.
 */
public class GoalService implements IDataService<Goal>, IAsyncRPCEvent {
    
    private final BrokerSession brokerSession;
    
    private final Object mutex = new Object();
    
    private int asyncHandle;
    
    private boolean done;
    
    private IQueryResult<Goal> results;
    
    public GoalService(BrokerSession brokerSession) {
        this.brokerSession = brokerSession;
    }
    
    @Override
    public IQueryResult<Goal> fetchData(AbstractServiceContext<Goal> serviceContext) throws Exception {
        abort();
        done = false;
        results = null;
        asyncHandle = brokerSession.callRPCAsync("BEHOPGAP GETGOAL", this, serviceContext.patient.getId().getIdPart());
        
        synchronized (mutex) {
            while (!done) {
                mutex.wait();
            }
        }
        return results;
    }
    
    @Override
    public void abort() {
        if (asyncHandle > 0) {
            brokerSession.callRPCAbort(asyncHandle);
            asyncHandle = 0;
            done();
        }
    }
    
    /**
     * Convert raw data into patient goals list. <code>
     * (goal no,0)=IEN(1)^GSET(2)^CREATED(3)^BY(4)^LASTMODIFIED(5)^FACILITY(6)
     * ^PROVIDER(7)^STARTDT(8)^FOLLOWUPDT(9)^STATUS(10)^GOAL NUMBER (11)
     * (goal no,10)=TYPE1^TYPE2^TYPE3...
     * (goal no,11)=GOALNAME
     * (goal no,12)=GOALREASON
     * (goal no,13,n)=REVIEW DATE(1)^NOTE(2)
     * </code> For example:<code>
     * 0: 1^GOAL SET^3150521^ADAM,ADAM^3150521.21114^DEMO IHS CLINIC^ADAM,ADAM^3150521^3150530^A;ACTIVE^1
     * 1: PHYSICAL ACTIVITY
     * 2: TEST
     * 3:
     * 4: REVIEW^3150522^PROGRESS NOTE REVIEWED
     * 5: 2^GOAL SET^3150521^ADAM,ADAM^3150521.212321^DEMO IHS CLINIC^ADAM,ADAM^3150521^3150530^S;GOAL STOPPED^2
     * 6: MEDICATIONS^OTHER^WELLNESS AND SAFETY
     * 7: TEST2
     * 8: TEST
     * </code>
     */
    @Override
    public void onRPCComplete(int handle, String data) {
        if (handle != asyncHandle) {
            return;
        }
        
        List<String> list = new ArrayList<>();
        final List<Goal> goals = new ArrayList<Goal>();
        list = StrUtil.toList(data, "\r");
        
        results = new IQueryResult<Goal>() {
            
            @Override
            public List<Goal> getResults() {
                return goals;
            }
            
            @Override
            public Object getMetadata(String key) {
                return null;
            }
            
        };
        
        done();
    }
    
    @Override
    public void onRPCError(int handle, int code, String text) {
        if (handle != asyncHandle) {
            return;
        }
        
        done();
    }
    
    private void done() {
        synchronized (mutex) {
            done = true;
            mutex.notifyAll();
        }
    }
}
