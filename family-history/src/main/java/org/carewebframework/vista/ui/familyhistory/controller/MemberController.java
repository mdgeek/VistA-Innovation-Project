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

import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;
import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.cal.ui.reporting.controller.AbstractGridController;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.familyhistory.service.FamilyHistoryService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Toolbar;

/**
 * Controller for Family History main display.
 */
public class MemberController extends AbstractGridController<FamilyMemberHistory> {
    
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
    
    public void onClick$mnuRefresh() {
        refresh();
    }
    
    @Override
    public Date getDateByType(FamilyMemberHistory fhx, DateType dateType) {
        return fhx.getDate();
    }
    
    @Override
    public void onPatientChanged(Patient patient) {
        super.onPatientChanged(patient);
        ZKUtil.disableChildren(toolbar, patient == null);
    }
}
