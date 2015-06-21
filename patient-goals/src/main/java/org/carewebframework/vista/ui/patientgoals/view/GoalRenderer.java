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

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.HybridModel.GroupHeader;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.controller.Constants;
import org.carewebframework.vista.ui.patientgoals.controller.GoalController.GrouperGroup;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalGroup;
import org.carewebframework.vista.ui.patientgoals.model.GoalType;
import org.carewebframework.vista.ui.patientgoals.model.Review;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Group;
import org.zkoss.zul.Row;
import org.zkoss.zul.Span;

/**
 * Renderer for a goal.
 */
public class GoalRenderer extends AbstractRowRenderer<Goal, Object> {
    
    private static final String STEP_VIEW = ZKUtil.getResourcePath(GoalRenderer.class, 1) + "steps.zul";
    
    public GoalRenderer() {
        super(null, null);
    }
    
    /**
     * Converts a list of goal types to a comma-delimited string.
     * 
     * @param types List of goal types.
     * @return Comma-delimited string of goal types.
     */
    public static String typeAsString(List<GoalType> types) {
        return StrUtil.fromList(types, ", ");
    }
    
    /**
     * Render the row for the specified goal.
     *
     * @param row Row to render.
     * @param goal The goal associated with the row.
     */
    @Override
    public Component renderRow(Row row, Goal goal) {
        applyGroupStyle(row, goal);
        boolean declined = goal.getGroup() == GoalGroup.DECLINED;
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, "root", "onReviewGoal", goal);
        createCell(row, "").appendChild(anchor);
        Cell cell = createCell(row, "");
        
        if (goal.getGroup() == GoalGroup.ACTIVE) {
            anchor = new A();
            anchor.setIconSclass("glyphicon glyphicon-plus");
            anchor.addForward(Events.ON_CLICK, "root", "onAddStep", goal);
            cell.appendChild(anchor);
        }
        
        createCell(row, goal.getLastUpdated());
        createCell(row, goal.getNumberAsString());
        createCell(row, goal.getName());
        createCell(row, goal.getStartDate());
        createCell(row, goal.getReason());
        createCell(row, declined ? "" : typeAsString(goal.getTypes()));
        createCell(row, declined ? "" : goal.getFollowupDate());
        createCell(row, declined ? "" : goal.getStatus());
        createCell(row, goal.getProviderName().replace(",", ", "));
        Review review = goal.getLastReview();
        String label = review == null ? "" : review.getNote();
        cell = createCell(row, label);
        
        if (review != null) {
            Span info = new Span();
            info.setSclass("glyphicon glyphicon-info-sign");
            info.setTooltiptext(StrUtil.fromList(goal.getReviews()));
            cell.insertBefore(info, cell.getFirstChild());
        }
        
        return row;
    }
    
    @Override
    protected void renderDetail(Detail detail, Goal goal) {
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
        GrouperGroup gg = gh.getGroup();
        String sclass = Constants.LABEL_SCLASS[gg.getGroup().ordinal()];
        group.setWidgetListener("onBind", "jq(this).find('.z-label').addClass('" + sclass + "')");
        group.setOpen(gg.isOpen());
        group.addForward(Events.ON_OPEN, "root", "onGroupOpen", gg);
    }
    
    /**
     * Applies the correct style for the goal's group.
     * 
     * @param comp Component to receive the style.
     * @param goal The goal.
     */
    protected static void applyGroupStyle(HtmlBasedComponent comp, GoalBase goal) {
        ZKUtil.updateSclass(comp, Constants.GROUP_SCLASS[goal.getGroup().ordinal()], false);
    }
    
}
