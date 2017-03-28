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

import org.apache.commons.lang.BooleanUtils;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.vista.plugin.familyhistory.model.Condition;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal.
 */
public class ConditionRenderer extends AbstractRowRenderer<Condition, Object> {
    
    public ConditionRenderer() {
        super(null, null);
    }
    
    /**
     * Render the row for the specified family member condition.
     *
     * @param row Row to render.
     * @param condition A family member condition.
     */
    @Override
    public Component renderRow(Row row, Condition condition) {
        Component root = row.getFellowIfAny("root", true);
        Cell cell = createCell(row, null);
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, root, "onReviewCondition", condition);
        cell.appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-remove");
        anchor.addForward(Events.ON_CLICK, root, "onDeleteCondition", condition);
        cell.appendChild(anchor);
        createCell(row, condition.getSCTText());
        createCell(row, condition.getNote());
        boolean approxAge = BooleanUtils.toBoolean(condition.isAgeApproximate());
        String onsetAge = condition.getAgeAtOnset() == null ? "" : (approxAge ? "~" : "") + condition.getAgeAtOnset();
        createCell(row, onsetAge);
        createCell(row, condition.getDateModified());
        createCell(row, condition.getICD9());
        row.setSclass("alert-warning");
        return null;
    }
}
