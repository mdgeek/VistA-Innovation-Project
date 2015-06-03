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
import java.util.Map;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.HybridModel.GroupHeader;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.controller.Constants;
import org.carewebframework.vista.ui.patientgoals.controller.GoalController.GrouperGroup;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.Review;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
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
        anchor.addForward(Events.ON_CLICK, "root", "onReviewGroup", goal);
        createCell(row, "").appendChild(anchor);
        Cell cell = createCell(row, "");
        
        if (group == GoalGroup.ACTIVE) {
            anchor = new A();
            anchor.setIconSclass("glyphicon glyphicon-plus");
            anchor.addForward(Events.ON_CLICK, "root", "onAddStep", goal);
            cell.appendChild(anchor);
        }
        
        createCell(row, goal.getLastUpdated());
        createCell(row, goal.getNumberString());
        createCell(row, goal.getName());
        createCell(row, goal.getStartDate());
        createCell(row, goal.getReason());
        createCell(row, goal.getTypes());
        createCell(row, goal.getFollowupDate());
        createCell(row, goal.getStatusText());
        createCell(row, goal.getProvider());
        
        Review review = goal.getLastReview();
        String label = review == null ? "" : review.getNote();
        String hint = review == null ? "" : review.toString();
        createCell(row, label).setTooltiptext(hint);
        
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
    
}
