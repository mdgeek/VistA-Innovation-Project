/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.model;

import org.carewebframework.common.StrUtil;

/**
 * Model object for a step within a patient goal.
 */
public class Step extends GoalBase {
    
    private String facility;
    
    private final Goal goal;
    
    public Step(Goal goal) {
        this.goal = goal;
    }
    
    @Override
    public void copyFrom(GoalBase source) {
        super.copyFrom(source);
        Step src = (Step) source;
        facility = src.facility;
    }
    
    public Goal getGoal() {
        return goal;
    }
    
    public String getFacilityName() {
        return facility == null ? "" : StrUtil.piece(facility, ";", 2);
    }
    
    public String getFacilityIEN() {
        return facility == null ? "" : StrUtil.piece(facility, ";");
    }
    
    public void setFacility(String facility) {
        this.facility = facility;
    }
    
}
