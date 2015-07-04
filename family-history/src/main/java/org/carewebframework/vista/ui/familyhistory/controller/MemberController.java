/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.familyhistory.controller;

import java.util.Date;
import java.util.List;

import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.familyhistory.model.Condition;
import org.carewebframework.vista.ui.familyhistory.model.FamilyMember;
import org.carewebframework.vista.ui.familyhistory.service.FamilyHistoryService;
import org.carewebframework.vista.ui.familyhistory.view.MemberRenderer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Toolbar;

import ca.uhn.fhir.model.dstu2.resource.Patient;

/**
 * Controller for Family History main display.
 */
public class MemberController extends AbstractGridController<FamilyMember, FamilyMember> {
    
    private static final long serialVersionUID = 1L;
    
    private final FamilyHistoryService service;
    
    // Start of auto-wired section
    
    private Toolbar toolbar;
    
    // End of auto-wired section
    
    /**
     * Allows the condition controller to find the enclosing member controller.
     * 
     * @param root Root component of the condition view.
     * @return The member controller.
     */
    public static MemberController findController(Component root) {
        return (MemberController) FrameworkController.getController(root.getParent(), true);
    }
    
    public MemberController(FamilyHistoryService service) {
        super(service, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null);
        this.service = service;
        setPaging(false);
    }
    
    @Override
    public void initializeController() {
        super.initializeController();
        MemberRenderer.setExpandDetail(getGrid(), true);
    }
    
    /**
     * Expands all details.
     */
    public void onClick$btnExpandAll() {
        MemberRenderer.expandAll(getGrid(), true, null);
    }
    
    /**
     * Collapses all details.
     */
    public void onClick$btnCollapseAll() {
        MemberRenderer.expandAll(getGrid(), false, null);
    }
    
    public void onClick$btnNewMember() {
        AddEditMemberController.execute(null, service);
    }
    
    public void onReviewMember(Event event) {
        FamilyMember fhx = (FamilyMember) event.getData();
        AddEditMemberController.execute(fhx, service);
    }
    
    public void onAddCondition(Event event) {
        FamilyMember fhx = (FamilyMember) event.getData();
        AddEditConditionController.execute(null, service);
    }
    
    public void onReviewCondition(Event event) {
        Condition condition = (Condition) event.getData();
        AddEditConditionController.execute(condition, service);
    }
    
    @Override
    public Date getDateByType(FamilyMember member, DateType dateType) {
        return null;
    }
    
    @Override
    public void onPatientChanged(Patient patient) {
        super.onPatientChanged(patient);
        ZKUtil.disableChildren(toolbar, patient == null);
    }
    
    @Override
    public void refresh() {
        super.refresh();
        Clients.resize(root);
    }
    
    @Override
    protected List<FamilyMember> toModel(List<FamilyMember> results) {
        return results;
    }
}
