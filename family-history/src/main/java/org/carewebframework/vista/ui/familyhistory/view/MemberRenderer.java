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
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.ui.zk.ZKUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal.
 */
public class MemberRenderer extends AbstractRowRenderer<FamilyMemberHistory, Object> {
    
    private static final String CONDITION_VIEW = ZKUtil.getResourcePath(MemberRenderer.class, 1) + "condition.zul";
    
    public static String concatCoding(List<CodingDt> codings) {
        StringBuilder sb = new StringBuilder();
        
        for (CodingDt coding : codings) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            
            sb.append(coding.getDisplay());
        }
        
        return sb.toString();
    }
    
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
    public Component renderRow(Row row, FamilyMemberHistory member) {
        createCell(row, concatCoding(member.getRelationship().getCoding()));
        createCell(row, member.getName());
        createCell(row, ""); // status (deceased/living)
        createCell(row, ""); // age at death (range)
        createCell(row, ""); // cause of death
        createCell(row, ""); // multiple birth
        createCell(row, ""); // multiple birth type
        row.setSclass("alert-info");
        return row;
    }
    
    @Override
    protected void renderDetail(Detail detail, FamilyMemberHistory member) {
        if (!member.getCondition().isEmpty()) {
            Map<Object, Object> args = new HashMap<>();
            args.put("member", member);
            ZKUtil.loadZulPage(CONDITION_VIEW, detail, args);
            detail.setOpen(true);
        }
    }
}
