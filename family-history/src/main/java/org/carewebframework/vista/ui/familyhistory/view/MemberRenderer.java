/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.familyhistory.view;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.familyhistory.model.MemberModel;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal.
 */
public class MemberRenderer extends AbstractRowRenderer<MemberModel, Object> {
    
    private static final String CONDITION_VIEW = ZKUtil.getResourcePath(MemberRenderer.class, 1) + "condition.zul";
    
    public MemberRenderer() {
        super(null, null);
    }
    
    /**
     * Render the row for the specified family member history.
     *
     * @param row Row to render.
     * @param model A family member history.
     */
    @Override
    public Component renderRow(Row row, MemberModel model) {
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, "root", "onReviewMember", model.getMember());
        createCell(row, "").appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-plus");
        anchor.addForward(Events.ON_CLICK, "root", "onAddCondition", model.getMember());
        createCell(row, "").appendChild(anchor);
        createCell(row, model.getRelationships());
        createCell(row, model.getName());
        createCell(row, ""); // status (deceased/living)
        createCell(row, ""); // age at death (range)
        createCell(row, ""); // cause of death
        createCell(row, ""); // multiple birth
        createCell(row, ""); // multiple birth type
        row.setSclass("alert-info");
        return row;
    }
    
    @Override
    protected void renderDetail(Detail detail, MemberModel model) {
        if (!model.getMember().getCondition().isEmpty()) {
            Map<Object, Object> args = new HashMap<>();
            args.put("member", model.getMember());
            ZKUtil.loadZulPage(CONDITION_VIEW, detail, args);
        }
    }
}
