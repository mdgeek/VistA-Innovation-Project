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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.Review;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Div;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal.
 */
public class GoalRenderer extends AbstractRowRenderer<Goal, Object> {
    
    private static final String STEP_VIEW = "~./org/carewebframework/vista/ui/patientgoals/steps.zul";
    
    public GoalRenderer() {
        super(null, null);
    }
    
    /**
     * Render the row for the specified goal.
     *
     * @param row Row to render.
     * @param goal The goal associated with the row.
     */
    @Override
    public Component renderRow(Row row, Goal goal) {
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        createCell(row, "").appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-plus");
        createCell(row, "").appendChild(anchor);
        createCell(row, goal.getLastUpdated());
        createCell(row, goal.getNumber());
        createCell(row, goal.getCreatedDate());
        createCell(row, goal.getStartDate());
        createCell(row, goal.getReason());
        createCell(row, goal.getType());
        createCell(row, goal.getFollowupDate());
        createCell(row, goal.getStatusText());
        createCell(row, goal.getProvider());
        createCell(row, goal.getReviews());
        return row;
    }
    
    @Override
    protected void renderDetail(final Detail detail, final Goal goal) {
        if (goal.getSteps().isEmpty()) {
            return;
        }
        
        detail.appendChild(new Div());
        
        detail.addEventListener(Events.ON_OPEN, new EventListener<Event>() {
            
            @Override
            public void onEvent(Event event) throws Exception {
                detail.removeEventListener(Events.ON_OPEN, this);
                ZKUtil.detachChildren(detail);
                Map<Object, Object> args = new HashMap<>();
                args.put("steps", goal.getSteps());
                ZKUtil.loadZulPage(STEP_VIEW, detail, args);
            }
            
        });
        
    }
    
    private Cell createCell(Row row, List<Review> reviews) {
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
        
        return createCell(row, sb.toString());
    }
    
}
