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
import java.util.Collections;
import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.apache.commons.lang.math.NumberUtils;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.common.NumUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.mbroker.AbstractBrokerQueryService;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalType;
import org.carewebframework.vista.ui.patientgoals.model.Review;
import org.carewebframework.vista.ui.patientgoals.model.Step;

/**
 * Data service for patient goals.
 */
public class GoalService extends AbstractBrokerQueryService<Goal> {
    
    private List<GoalType> goalTypes;
    
    public GoalService(BrokerSession brokerSession) {
        super(brokerSession, "BEHOPGAP GETGOAL");
    }
    
    @Override
    protected void createArgumentList(List<Object> args, IQueryContext context) {
        super.createArgumentList(args, context);
        Patient patient = (Patient) context.getParam("patient");
        args.add(patient.getId().getIdPart());
        args.add(true); // This argument forces retrieval of goal steps as well.
    }
    
    @Override
    public boolean hasRequired(IQueryContext context) {
        return context.getParam("patient") instanceof Patient;
    }
    
    /**
     * Convert raw data into patient goals list.
     * 
     * <pre>
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
     * For example:
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
     * </pre>
     */
    @Override
    protected List<Goal> processData(IQueryContext context, String data) {
        List<Goal> results = new ArrayList<>();
        List<String> list = StrUtil.toList(data, "\r");
        Goal goal = null;
        Step step = null;
        int state = 0;
        
        for (String line : list) {
            String[] pcs = StrUtil.split(line, StrUtil.U, 1);
            
            switch (state) {
                case 4: // Review
                    if ("REVIEW".equals(pcs[0])) {
                        goal.getReviews().add(new Review(FMDate.fromString(pcs[1]), pcs[2]));
                        break;
                    }
                    
                    Collections.sort(goal.getReviews());
                    
                    // Note that fall through is intended here.
                case 5: // Step
                    if ("STEP".equals(pcs[0])) {
                        state = 6;
                        step = new Step(goal);
                        goal.getSteps().add(step);
                        step.setFacility(pcs[1]);
                        step.setIEN(pcs[2]);
                        step.setNumber(NumberUtils.toFloat(pcs[3]));
                        step.setCreatedBy(pcs[4]);
                        step.setCreatedDate(FMDate.fromString(pcs[5]));
                        step.getTypes().add(getGoalType(pcs[6]));
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
                    goal.setPatient((Patient) context.getParam("patient"));
                    goal.setIEN(pcs[0]);
                    goal.setDeclined("GOAL NOT SET".equals(pcs[1]));
                    goal.setCreatedDate(FMDate.fromString(pcs[2]));
                    goal.setCreatedBy(pcs[3]);
                    goal.setLastUpdated(FMDate.fromString(pcs[4]));
                    goal.setFacility(pcs[5]);
                    goal.setProvider(pcs[6]);
                    goal.setStartDate(FMDate.fromString(pcs[7]));
                    goal.setFollowupDate(FMDate.fromString(pcs[8]));
                    goal.setStatus(pcs[9]);
                    goal.setNumber(NumberUtils.toFloat(pcs[10]));
                    state = 1;
                    break;
                
                case 1: // Types
                    for (int i = 0; i < pcs.length; i++) {
                        goal.getTypes().add(getGoalType(pcs[i]));
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
                    step.setName(pcs[0]);
                    state = 5;
                    break;
            }
        }
        
        if (goal != null) {
            results.add(goal);
        }
        
        return results;
    }
    
    public GoalType getGoalType(String name) {
        for (GoalType goalType : getGoalTypes()) {
            if (goalType.getName().equalsIgnoreCase(name)) {
                return goalType;
            }
        }
        
        return null;
    }
    
    public List<GoalType> getGoalTypes() {
        if (goalTypes == null) {
            goalTypes = new ArrayList<>();
            
            for (String entry : service.callRPCList("RGUTRPC FILENT", null, "9001002.4")) {
                goalTypes.add(new GoalType(entry));
            }
            
            Collections.sort(goalTypes);
        }
        
        return goalTypes;
    }
    
    /**
     * Add a new goal.
     * <p>
     * 
     * <pre>
     * (0) =   "GOAL" ^ GOAL SET STATUS ^ WHERE SET ^ GOAL NUMBER ^ PROVIDER ^ START DATE ^ FOLLOWUP DATE ^ USER
     * (1) =   "TYPE" ^ GOAL TYPE ^ GOAL TYPE ^ GOAL TYPE ...
     * (2) =   "NAME" ^ GOAL NAME
     * (3) = "REASON" ^ GOAL REASON
     * For example:
     * 0: GOAL^S^7819^5^1^3150602^3150630^1
     * 1: TYPE^PHYSICAL ACTIVITY
     * 2: NAME^My New Goal
     * 3: REASON^My Reason
     * </pre>
     * 
     * @param goal The goal to add.
     */
    public void addGoal(Goal goal) {
        float number = nextGoalNumber(goal.getPatient());
        List<String> data = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        addPiece(sb, "GOAL");
        addPiece(sb, goal.isDeclined() ? "N" : "S");
        addPiece(sb, goal.getFacilityIEN());
        addPiece(sb, NumUtil.toString(number));
        addPiece(sb, goal.getProviderIEN());
        addPiece(sb, goal.getStartDate());
        addPiece(sb, goal.getFollowupDate());
        addPiece(sb, UserContext.getActiveUser().getLogicalId());
        flush(sb, data);
        addPiece(sb, "TYPE");
        
        for (GoalType goalType : goal.getTypes()) {
            addPiece(sb, goalType.getName());
        }
        
        flush(sb, data);
        addPiece(sb, "NAME");
        addPiece(sb, goal.getName());
        flush(sb, data);
        addPiece(sb, "REASON");
        addPiece(sb, goal.getReason());
        flush(sb, data);
        String result = service.callRPC("BEHOPGAP ADDGOAL", goal.getPatient().getId().getIdPart(), data);
        goal.setIEN(checkResult(result));
        goal.setNumber(number);
        goal.setCreatedDate(FMDate.today());
        goal.setLastUpdated(FMDate.now());
    }
    
    private String getLocationIEN() {
        return UserContext.getActiveUser().getSecurityDomain().getLogicalId();
    }
    
    public void updateGoal(Goal goal, boolean includeReview) {
        Review review = includeReview ? goal.getLastReview() : null;
        FMDate reviewDate = review == null ? null : review.getReviewed();
        String reviewNote = review == null ? null : review.getNote();
        String result = service.callRPC("BEHOPGAP EDITGOAL", goal.getIEN(), goal.getFollowupDate(), goal.getStatusCode(),
            reviewDate, reviewNote);
        checkResult(result);
    }
    
    public void deleteGoal(Goal goal) {
        String result = service.callRPC("BEHOPGAP DELGOAL", goal.getIEN(), service.getUserId(), FMDate.now(),
            goal.getDeleteCode(), goal.getDeleteReason());
        checkResult(result);
    }
    
    public boolean canModify(Patient patient) {
        try {
            nextGoalNumber(patient);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private float nextGoalNumber(Patient patient) {
        String result = service.callRPC("BEHOPGAP NEXTGN", patient.getId().getIdPart(), getLocationIEN());
        return Float.parseFloat(result);
    }
    
    /**
     * Adds a new step.
     * <p>
     * 
     * <pre>
     *  (n) FACILITY ^ NUMBER ^ TYPE ^ START DATE ^ FOLLOWUP DATE ^ PROVIDER ^ TEXT
     * 
     * Example:
     * 1: 5217^1^NUTRITION^3101029^3101231^1239^EAT LESS THAN 1200 CAAPCDTESTES PER DAY
     * 2: 5217^2^PHYSICAL ACTIVITY^3101029^3101231^1239^WALK 60 MINUTES PER DAY
     * </pre>
     * 
     * @param step New step to add.
     */
    public void addStep(Step step) {
        float number = nextStepNumber(step);
        StringBuilder sb = new StringBuilder();
        addPiece(sb, step.getFacilityIEN());
        addPiece(sb, NumUtil.toString(number));
        addPiece(sb, step.getTypes().get(0).getName());
        addPiece(sb, step.getStartDate());
        addPiece(sb, step.getFollowupDate());
        addPiece(sb, step.getProviderIEN());
        addPiece(sb, step.getName());
        String result = service.callRPC("BEHOPGAP ADDSTEP", step.getGoal().getIEN(),
            Collections.singletonList(sb.toString()));
        step.setIEN(checkResult(result));
        step.setNumber(number);
        step.setCreatedDate(FMDate.today());
        step.setLastUpdated(FMDate.now());
    }
    
    public void updateStep(Step step) {
        String result = service.callRPC("BEHOPGAP EDITSTEP", step.getGoal().getIEN(), step.getFacilityIEN(), step.getIEN(),
            step.getFollowupDate(), step.getStatusCode());
        checkResult(result);
    }
    
    public void deleteStep(Step step) {
        String result = service.callRPC("BEHOPGAP DELSTEP", step.getGoal().getIEN(), step.getFacilityIEN(), step.getIEN(),
            service.getUserId(), FMDate.now(), step.getDeleteCode(), step.getDeleteReason());
        checkResult(result);
    }
    
    public float nextStepNumber(Step step) {
        String result = service.callRPC("BEHOPGAP NEXTSN", step.getGoal().getIEN(), step.getFacilityIEN());
        return Float.parseFloat(result);
    }
    
    private String checkResult(String result) {
        String[] pcs = StrUtil.split(result, StrUtil.U, 2);
        
        if ("0".equals(pcs[0])) {
            throw new RuntimeException(pcs[1]);
        }
        
        return pcs[1];
    }
    
    private void flush(StringBuilder sb, List<String> data) {
        data.add(sb.toString());
        sb.setLength(0);
    }
    
    private StringBuilder addPiece(StringBuilder sb, FMDate field) {
        return addPiece(sb, field == null ? null : field.getFMDate());
    }
    
    private StringBuilder addPiece(StringBuilder sb, Object field) {
        if (sb.length() > 0) {
            sb.append(StrUtil.U);
        }
        
        sb.append(field == null ? "" : field);
        return sb;
    }
}
