/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.plugin.familyhistory.view;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.plugin.familyhistory.model.FamilyMember;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal.
 */
public class MemberRenderer extends AbstractRowRenderer<FamilyMember, Object> {
    
    private static final String CONDITION_VIEW = ZKUtil.getResourcePath(MemberRenderer.class, 1) + "condition.zul";
    
    public MemberRenderer() {
        super(null, null);
    }
    
    /**
     * Render the row for the specified family member history.
     *
     * @param row Row to render.
     * @param member A family member history.
     */
    @Override
    public Component renderRow(Row row, FamilyMember member) {
        Cell cell = createCell(row, null);
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, "root", "onReviewMember", member);
        cell.appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-remove");
        anchor.addForward(Events.ON_CLICK, "root", "onDeleteMember", member);
        cell.appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-plus");
        anchor.addForward(Events.ON_CLICK, "root", "onAddCondition", member);
        createCell(row, "").appendChild(anchor);
        createCell(row, member.getRelationship());
        createCell(row, member.getName());
        createCell(row, member.getStatus());
        createCell(row, member.getAgeAtDeath());
        createCell(row, member.getCauseOfDeath());
        createCell(row, member.getMultipleBirth());
        createCell(row, member.getMultipleBirthType());
        row.setSclass("alert-info");
        return row;
    }
    
    @Override
    protected void renderDetail(Detail detail, FamilyMember member) {
        if (!member.getConditions().isEmpty()) {
            Map<Object, Object> args = new HashMap<>();
            args.put("member", member);
            ZKUtil.loadZulPage(CONDITION_VIEW, detail, args);
            Clients.resize(detail);
        }
    }
}
