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
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.mbroker.AbstractBrokerService;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.Review;
import org.carewebframework.vista.ui.patientgoals.model.Step;

/**
 * Data service for patient goals.
 */
public class GoalService extends AbstractBrokerService<Goal> {
    
    public GoalService(BrokerSession brokerSession) {
        super(brokerSession, "BEHOPGAP GETGOAL", false);
    }
    
    @Override
    protected void createArgumentList(List<Object> args, AbstractServiceContext<Goal> serviceContext) {
        super.createArgumentList(args, serviceContext);
        args.add(true); // This argument forces retrieval of goal steps as well.
    }
    
    /**
     * Convert raw data into patient goals list. <code>
     * (0) IEN [0] ^ GSET [1] ^ CREATED BY [2] ^ CREATED DATE [3] ^ LAST MODIFIED [4] ^ FACILITY [5] ^ PROVIDER [6] ^
     *     START DATE [7] ^ FOLLOWUP DATE [8] ^ STATUS [9] ^ GOAL NUMBER [10]
     * (1) TYPE1 ^ TYPE2 ^ TYPE3...
     * (2) GOAL NAME [0]
     * (3) GOAL REASON [0]
     * (4) "REVIEW" [0] ^ REVIEW DATE [1] ^ NOTE [2]    (will be 0 or more rows of these)
     * (5) "STEP" [0] ^ FACILITY [1] ^ STEP IEN [2] ^ STEP NUMBER [3] ^ CREATED BY [4] ^ CREATED DATE [5] ^
     *     TYPE [6] ^ START DATE [7] ^ FOLLOWUP DATE [8] ^ MODIFIED BY [9] ^ LAST MODIFIED [10] ^ STATUS [11] ^
     *     PROVIDER [13]    (will be 0 or more rows of these)
     * (6) STEP TEXT [0]  (only if #5 is present)
     * </code> For example:<code>
     * 0: 1^GOAL SET^3150521^ADAM,ADAM^3150521.21114^DEMO IHS CLINIC^ADAM,ADAM^3150521^3150530^A;ACTIVE^1
     * 1: PHYSICAL ACTIVITY
     * 2: TEST
     * 3:
     * 4: REVIEW^3150522^PROGRESS NOTE REVIEWED
     * 5: STEP^7819;DEMO IHS CLINIC^1^1^ADAM,ADAM^3150521^TOBACCO^3150521^3150627^ADAM,ADAM^3150521.211237^A;ACTIVE^ADAM,ADAM
     * 6: TEST STEP
     * 7: 2^GOAL SET^3150521^ADAM,ADAM^3150521.212321^DEMO IHS CLINIC^ADAM,ADAM^3150521^3150530^S;GOAL STOPPED^2
     * 8: MEDICATIONS^OTHER^WELLNESS AND SAFETY
     * 9: TEST2
     * 10:TEST
     * </code>
     */
    @Override
    protected List<Goal> processData(String data) {
        List<String> list = new ArrayList<>();
        List<Goal> results = new ArrayList<>();
        Goal goal = null;
        Step step = null;
        int state = 0;
        list = StrUtil.toList(data, "\r");
        
        for (String line : list) {
            String[] pcs = StrUtil.split(line, StrUtil.U, 1);
            
            switch (state) {
                case 4: // Review
                    if ("REVIEW".equals(pcs[0])) {
                        goal.getReview().add(new Review(FMDate.fromString(pcs[1]), pcs[2]));
                        break;
                    }
                    // Note that fall through is intended here.
                case 5: // Step
                    if ("STEP".equals(pcs[0])) {
                        state = 6;
                        step = new Step();
                        goal.getStep().add(step);
                        step.setFacility(pcs[1]);
                        step.setIEN(pcs[2]);
                        step.setNumber(pcs[3]);
                        step.setCreatedBy(pcs[4]);
                        step.setCreatedDate(FMDate.fromString(pcs[5]));
                        step.getType().add(pcs[6]);
                        step.setStartDate(FMDate.fromString(pcs[7]));
                        step.setFollowupDate(FMDate.fromString(pcs[8]));
                        step.setUpdatedBy(pcs[9]);
                        step.setLastUpdated(FMDate.fromString(pcs[10]));
                        step.setStatus(pcs[11]);
                        step.setProvider(pcs[12]);
                        break;
                    }
                    // Note that fall through is intended here.
                case 0: // New goal
                    if (goal != null) {
                        results.add(goal);
                    }
                    
                    step = null;
                    goal = new Goal();
                    goal.setIEN(pcs[0]);
                    goal.setDeclined("GOAL NOT SET".equals(pcs[1]));
                    goal.setCreatedDate(FMDate.fromString(pcs[2]));
                    goal.setCreatedBy(pcs[3]);
                    goal.setLastUpdated(FMDate.fromString(pcs[4]));
                    goal.setLocationIEN(pcs[5]);
                    goal.setProvider(pcs[6]);
                    goal.setStartDate(FMDate.fromString(pcs[7]));
                    goal.setFollowupDate(FMDate.fromString(pcs[8]));
                    goal.setStatus(pcs[9]);
                    goal.setNumber(pcs[10]);
                    state = 1;
                    break;
                
                case 1: // Types
                    for (int i = 0; i < pcs.length; i++) {
                        goal.getType().add(pcs[i]);
                    }
                    
                    state = 2;
                    break;
                
                case 2: // Goal name
                    goal.setName(pcs[0]);
                    state = 3;
                    break;
                
                case 3: // Goal reason
                    goal.setReason(pcs[0]);
                    state = 4;
                    break;
                
                case 6: // Step text
                    step.setText(pcs[0]);
                    state = 5;
                    break;
            }
        }
        
        if (goal != null) {
            results.add(goal);
        }
        
        return results;
    }
}
