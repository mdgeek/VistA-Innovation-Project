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
import org.carewebframework.vista.ui.familyhistory.model.ConditionModel;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal.
 */
public class ConditionRenderer extends AbstractRowRenderer<ConditionModel, Object> {
    
    public ConditionRenderer() {
        super(null, null);
    }
    
    /**
     * Render the row for the specified family member condition.
     *
     * @param row Row to render.
     * @param model A family member condition.
     */
    @Override
    public Component renderRow(Row row, ConditionModel model) {
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, row.getFellow("root", true), "onReviewCondition", model.getCondition());
        createCell(row, null).appendChild(anchor);
        createCell(row, model.getNote()); // provider narrative
        createCell(row, model.getOnsetAge()); // age at diagnosis
        createCell(row, ""); // date modified
        createCell(row, model.getICD9()); // icd
        row.setSclass("alert-warning");
        return null;
    }
}
