/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.plugin.patientgoals.view;

import org.carewebframework.ui.zk.AbstractRowRenderer;
import org.carewebframework.vista.plugin.patientgoals.model.Step;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Row;

/**
 * Renderer for a goal step.
 */
public class StepRenderer extends AbstractRowRenderer<Step, Object> {
    
    public StepRenderer() {
        super(null, null);
    }
    
    /**
     * Render the row for the specified step.
     *
     * @param row Row to render.
     * @param step The step associated with the row.
     */
    @Override
    protected Component renderRow(Row row, Step step) {
        Component root = row.getFellowIfAny("root", true);
        GoalRenderer.applyGroupStyle(row, step);
        Cell cell = createCell(row, null);
        A anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-pencil");
        anchor.addForward(Events.ON_CLICK, root, "onReviewStep", step);
        cell.appendChild(anchor);
        anchor = new A();
        anchor.setIconSclass("glyphicon glyphicon-remove");
        anchor.addForward(Events.ON_CLICK, root, "onDeleteStep", step);
        cell.appendChild(anchor);
        createCell(row, step.getNumberAsString());
        createCell(row, step.getName());
        createCell(row, step.getStartDate());
        createCell(row, step.getFollowupDate());
        createCell(row, step.getStatus());
        return null;
    }
    
}
