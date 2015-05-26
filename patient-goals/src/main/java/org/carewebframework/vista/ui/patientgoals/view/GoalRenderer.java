/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.view;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.Review;
import org.carewebframework.vista.ui.patientgoals.model.Step;

import org.zkoss.zul.A;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

/**
 * Renderer for a goal.
 */
public class GoalRenderer extends AbstractListitemRenderer<Goal, Object> {
    
    public GoalRenderer() {
        super("background-color: white", null);
    }
    
    /**
     * Render the list item for the specified goal.
     *
     * @param item List item to render.
     * @param goal The goal associated with the list item.
     */
    @Override
    public void renderItem(Listitem item, Goal goal) {
        addCell(item, "");
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        addCell(item, "").appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-plus");
        addCell(item, "").appendChild(anchor);
        addCell(item, goal.getLastUpdated());
        addCell(item, goal.getNumber());
        addCell(item, goal.getCreatedDate());
        addCell(item, goal.getStartDate());
        addCell(item, goal.getReason());
        addCell(item, goal.getType());
        addCell(item, goal.getFollowupDate());
        addCell(item, goal.getStatusText());
        addCell(item, goal.getProvider());
        addCell(item, goal.getReview());
        renderDetail(item, goal.getStep());
    }
    
    private void renderDetail(Listitem item, List<Step> steps) {
        if (steps.isEmpty()) {
            return;
        }
        
        Detail detail = new Detail();
        item.appendChild(detail);
    }
    
    /**
     * Add a cell to the list item containing the specified text value.
     *
     * @param item List item to receive new cell.
     * @param value Text to include in the new cell.
     * @return The newly created cell.
     */
    private Listcell addCell(Listitem item, Object value) {
        return createCell(item, value, null, null);
    }
    
    private Listcell addCell(Listitem item, List<Review> reviews) {
        StringBuilder sb = new StringBuilder();
        
        for (Review review : reviews) {
            String note = review.getNote();
            
            if (!StringUtils.isEmpty(note)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                
                sb.append(note);
            }
        }
        
        return addCell(item, sb.toString());
    }
    
}
