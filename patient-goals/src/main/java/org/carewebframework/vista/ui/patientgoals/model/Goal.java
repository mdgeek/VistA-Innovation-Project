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
import java.util.Collection;
import java.util.List;

import ca.uhn.fhir.model.dstu2.resource.Patient;

/**
 * Model object for a patient goal.
 */
public class Goal extends GoalBase {
    
    private boolean declined;
    
    private Patient patient;
    
    private final List<Review> reviews = new ArrayList<>();
    
    private final List<Step> steps = new ArrayList<>();
    
    @Override
    public void copyFrom(GoalBase source) {
        super.copyFrom(source);
        Goal src = (Goal) source;
        declined = src.declined;
        patient = src.patient;
        reviews.clear();
        reviews.addAll(src.reviews);
        steps.clear();
        steps.addAll(src.steps);
    }
    
    public Patient getPatient() {
        return patient;
    }
    
    public void setPatient(Patient patient) {
        this.patient = patient;
    }
    
    public boolean isDeclined() {
        return declined;
    }
    
    public void setDeclined(boolean declined) {
        this.declined = declined;
    }
    
    public List<Review> getReviews() {
        return reviews;
    }
    
    public Review getLastReview() {
        return reviews.isEmpty() ? null : reviews.get(reviews.size() - 1);
    }
    
    public Collection<Step> getSteps() {
        return steps;
    }
    
    @Override
    public GoalGroup getGroup() {
        return declined ? GoalGroup.DECLINED : super.getGroup();
    }
    
}
