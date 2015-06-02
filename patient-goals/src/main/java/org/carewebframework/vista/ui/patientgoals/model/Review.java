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

import org.carewebframework.common.DateUtil;
import org.carewebframework.vista.mbroker.FMDate;

/**
 * Model object for a goal review.
 */
public class Review implements Comparable<Review> {
    
    private final FMDate reviewed;
    
    private final String note;
    
    public Review(FMDate reviewed, String note) {
        this.reviewed = reviewed;
        this.note = note;
    }
    
    public FMDate getReviewed() {
        return reviewed;
    }
    
    public String getNote() {
        return note;
    }
    
    @Override
    public int compareTo(Review review) {
        return DateUtil.compare(reviewed, review.reviewed);
    }
    
    @Override
    public String toString() {
        return (reviewed == null ? "" : DateUtil.formatDate(reviewed) + "  ") + (note == null ? "" : note);
    }
    
}
