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

import java.util.Date;

/**
 * Model object for a goal review.
 */
public class Review {
    
    private final Date reviewed;
    
    private final String note;
    
    public Review(Date reviewed, String note) {
        this.reviewed = reviewed;
        this.note = note;
    }
    
    public Date getReviewed() {
        return reviewed;
    }
    
    public String getNote() {
        return note;
    }
    
}
