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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.vista.ui.patientgoals.model.Step;

import org.zkoss.zul.Listitem;

/**
 * Renderer for a goal step.
 */
public class StepRenderer extends AbstractListitemRenderer<Step, Object> {
    
    private static final Log log = LogFactory.getLog(StepRenderer.class);
    
    public StepRenderer() {
        super("background-color: white", null);
    }
    
    /**
     * Render the list item for the specified goal.
     *
     * @param item List item to render.
     * @param step The step associated with the list item.
     */
    @Override
    public void renderItem(Listitem item, Step step) {
        log.trace("item render");
        addCell(item, "");
    }
    
    /**
     * Add a cell to the list item containing the specified text value.
     *
     * @param item List item to receive new cell.
     * @param value Text to include in the new cell.
     */
    private void addCell(Listitem item, Object value) {
        createCell(item, value, null, null);
    }
    
}
