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

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.vista.ui.familyhistory.model.Condition;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
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
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, row.getFellow("root", true), "onReviewCondition", condition);
        createCell(row, null).appendChild(anchor);
        createCell(row, condition.getNote());
        createCell(row, condition.getAgeAtOnset());
        createCell(row, condition.getDateModified());
        createCell(row, condition.getICD9());
        row.setSclass("alert-warning");
        return null;
    }
}
