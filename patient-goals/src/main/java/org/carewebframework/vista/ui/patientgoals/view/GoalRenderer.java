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

import org.carewebframework.common.NumUtil;
import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.HybridModel.GroupHeader;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.controller.Constants;
import org.carewebframework.vista.ui.patientgoals.controller.GoalController.GrouperGroup;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.Review;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Group;
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
        GoalGroup group = goal.getGroup();
        ZKUtil.updateSclass(row, Constants.GROUP_SCLASS[group.ordinal()], false);
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        createCell(row, "").appendChild(anchor);
        Cell cell = createCell(row, "");
        
        if (group == GoalGroup.ACTIVE) {
            anchor = new A();
            anchor.setIconSclass("glyphicon glyphicon-plus");
            cell.appendChild(anchor);
        }
        
        createCell(row, goal.getLastUpdated());
        createCell(row, NumUtil.toString(goal.getNumber()));
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
        
        Map<Object, Object> args = new HashMap<>();
        args.put("goal", goal);
        ZKUtil.loadZulPage(STEP_VIEW, detail, args);
    }
    
    @Override
    protected void renderGroup(Group group, Object object) {
        super.renderGroup(group, object);
        @SuppressWarnings("unchecked")
        GroupHeader<Group, GrouperGroup> gh = (GroupHeader<Group, GrouperGroup>) object;
        String sclass = Constants.LABEL_SCLASS[gh.getGroup().getGroup().ordinal()];
        group.setWidgetListener("onBind", "jq(this).find('.z-label').addClass('" + sclass + "')");
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
