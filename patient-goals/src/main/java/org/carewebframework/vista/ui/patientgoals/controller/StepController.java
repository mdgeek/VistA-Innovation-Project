/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.controller;

import java.util.List;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.vista.ui.patientgoals.model.Step;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.RowRenderer;

/**
 * Controller for goal steps.
 */
public class StepController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private Grid grid;
    
    private RowRenderer<Step> rowRenderer;
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        grid.setRowRenderer(rowRenderer);
        @SuppressWarnings("unchecked")
        List<Step> steps = (List<Step>) arg.get("steps");
        grid.setModel(new ListModelList<Step>(steps));
    }
    
    public void setRowRenderer(RowRenderer<Step> rowRenderer) {
        this.rowRenderer = rowRenderer;
    }
    
}
