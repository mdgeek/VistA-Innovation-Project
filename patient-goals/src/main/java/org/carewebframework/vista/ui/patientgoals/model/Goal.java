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

import java.util.ArrayList;
import java.util.List;

/**
 * Model object for a patient goal.
 */
public class Goal extends GoalBase {
    
    private boolean declined;
    
    private String locationIEN;
    
    private String name;
    
    private final List<Review> review = new ArrayList<>();
    
    private final List<Step> step = new ArrayList<>();
    
    public boolean isDeclined() {
        return declined;
    }
    
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
    
    public String getLocationIEN() {
        return locationIEN;
    }
    
    public void setLocationIEN(String locationIEN) {
        this.locationIEN = locationIEN;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<Review> getReviews() {
        return review;
    }
    
    public List<Step> getSteps() {
        return step;
    }
    
    @Override
    public GoalGroup getGroup() {
        return declined ? GoalGroup.DECLINED : "SME".contains(getStatusCode()) ? GoalGroup.INACTIVE : GoalGroup.ACTIVE;
    }
    
}
