/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.plugin.familyhistory.controller;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.plugin.familyhistory.model.Condition;
import org.carewebframework.vista.plugin.familyhistory.model.FamilyMember;
import org.carewebframework.vista.plugin.familyhistory.service.FamilyHistoryService;
import org.carewebframework.vista.plugin.familyhistory.view.MemberRenderer;
import org.hl7.fhir.dstu3.model.Patient;
import org.hspconsortium.cwf.ui.reporting.controller.AbstractGridController;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Toolbar;

/**
 * Controller for Family History main display.
 */
public class MemberController extends AbstractGridController<FamilyMember, FamilyMember> {

    private static final long serialVersionUID = 1L;

    private static final String FHX_EVENT = "PCC.%.FHH";

    private final IGenericEvent<String> fhxEventHandler = new IGenericEvent<String>() {

        @Override
        public void eventCallback(String eventName, String eventData) {
            String[] pcs = StrUtil.split(eventData, StrUtil.U);

            if (NumberUtils.toInt(pcs[2]) != service.getBroker().getId()) {
                Events.echoEvent("onRefresh", root, null);
            }
        }

    };

    private final FamilyHistoryService service;

    private String fhxEvent;

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
        super(service, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, true, true, null);
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
        FamilyMember member = new FamilyMember();

        if (AddEditMemberController.execute(member)) {
            getModel().add(member);
            rerender();
        }
    }

    public void onReviewMember(Event event) {
        FamilyMember member = (FamilyMember) event.getData();

        if (AddEditMemberController.execute(member)) {
            rerender();
        }
    }

    public void onDeleteMember(Event event) {
        FamilyMember member = (FamilyMember) event.getData();
        String message = StrUtil.getLabel("vistaFamilyHistory.delete.member.prompt", member.getName());

        if (PromptDialog.confirm(message, "@vistaFamilyHistory.delete.member.title")) {
            service.deleteMember(member);
            getModel().remove(member);
            rerender();
        }
    }

    public void onAddCondition(Event event) {
        FamilyMember member = (FamilyMember) event.getData();
        addEditCondition(new Condition(member));
    }

    public void onReviewCondition(Event event) {
        addEditCondition((Condition) event.getData());
    }

    public void onDeleteCondition(Event event) {
        Condition condition = (Condition) event.getData();
        String message = StrUtil.getLabel("vistaFamilyHistory.delete.condition.prompt", condition.getNote());

        if (PromptDialog.confirm(message, "@vistaFamilyHistory.delete.condition.title")) {
            service.deleteCondition(condition);
            rerender();
        }
    }

    private void addEditCondition(Condition condition) {
        if (AddEditConditionController.execute(condition)) {
            rerender();
        }
    }

    @Override
    public Date getDateByType(FamilyMember member, DateType dateType) {
        return null;
    }

    @Override
    public void onPatientChanged(Patient patient) {
        if (fhxEvent != null) {
            getEventManager().unsubscribe(fhxEvent, fhxEventHandler);
        }

        super.onPatientChanged(patient);
        ZKUtil.disableChildren(toolbar, patient == null);
        fhxEvent = patient == null ? null : FHX_EVENT.replace("%", patient.getIdElement().getIdPart());

        if (fhxEvent != null) {
            getEventManager().subscribe(fhxEvent, fhxEventHandler);
        }
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
