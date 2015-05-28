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

import org.carewebframework.api.query.IQueryService;
import org.carewebframework.api.query.QueryUtil;
import org.carewebframework.cal.ui.reporting.controller.AbstractServiceController;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.model.Goal;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Include;

/**
 * Controller for patient goals plugin.
 */
public class MainController extends AbstractServiceController<Goal> {
    
    private static final long serialVersionUID = 1L;
    
    private enum GoalFilter {
        ACTIVE, INACTIVE, DECLINED
    }
    
    private enum StepFilter {
        ACTIVE, INACTIVE
    }
    
    private Groupbox gbFirst;
    
    private final Checkbox[] chkActiveFilter = { null, null };
    
    private GoalFilter activeGoalFilter;
    
    private StepFilter activeStepFilter;
    
    public MainController(IQueryService<Goal> service) {
        super(service, true, "vistaPatientGoals");
    }
    
    @Override
    protected void initializeController() {
        super.initializeController();
    }
    
    public void onToggleOpen(Event event) {
        Component target = event.getTarget();
        boolean noToggle = target instanceof Groupbox;
        Groupbox gb = noToggle ? (Groupbox) target : ZKUtil.findAncestor(target, Groupbox.class);
        
        if (!noToggle) {
            gb.setOpen(!gb.isOpen());
        }
        
        updateGroupbox(gb);
    }
    
    private void updateGroupbox(Groupbox gb) {
        Component toggle = gb.getCaption().getFirstChild();
        ((HtmlBasedComponent) toggle).setSclass(gb.isOpen() ? "glyphicon-minus" : "glyphicon-plus");
    }
    
    public void onClick$btnExpandAll() {
        openAll(true);
    }
    
    public void onClick$btnCollapseAll() {
        openAll(false);
    }
    
    private void openAll(boolean open) {
        Groupbox gb = gbFirst;
        
        while (gb != null) {
            gb.setOpen(open);
            updateGroupbox(gb);
            gb = (Groupbox) gb.getNextSibling();
        }
    }
    
    /**
     * Determine filter change from onCheck event.
     * 
     * @param event The onCheck event.
     */
    public void onFilterChange(Event event) {
        event = ZKUtil.getEventOrigin(event);
        Checkbox chk1 = (Checkbox) event.getTarget();
        long value = (Long) chk1.getValue();
        int which = value < 10 ? 0 : 1;
        int ord = (int) value % 10;
        Checkbox chk2 = chkActiveFilter[which];
        
        if (chk2 != null && chk2.isChecked()) {
            chk2.setChecked(false);
        }
        
        chkActiveFilter[which] = chk1.isChecked() ? chk1 : null;
        
        if (which == 0) {
            activeGoalFilter = chk1.isChecked() ? GoalFilter.values()[ord] : null;
            goalFilterChanged();
        } else {
            activeStepFilter = chk1.isChecked() ? StepFilter.values()[ord] : null;
            stepFilterChanged();
        }
    }
    
    /**
     * Passes the filtered model to each of the grids.
     */
    @Override
    protected void modelChanged(List<Goal> filteredModel) {
        Component gb = gbFirst;
        
        while (gb != null) {
            Component target = ZKUtil.findChild(gb, Include.class).getFirstChild();
            QueryFinishedEvent event = new QueryFinishedEvent(null, QueryUtil.packageResult(filteredModel), target);
            Events.postEvent(event);
            gb = gb.getNextSibling();
        }
    }
    
    private void goalFilterChanged() {
    }
    
    private void stepFilterChanged() {
    }
    
}
