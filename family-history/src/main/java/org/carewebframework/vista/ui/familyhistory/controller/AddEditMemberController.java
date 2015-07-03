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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.FileEntry;
import org.carewebframework.vista.ui.familyhistory.service.FamilyHistoryService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;

/**
 * Controller for adding new members.
 */
public class AddEditMemberController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PATH + "addEditMember.zul";
    
    // Start of auto-wire section
    
    private Combobox cboRelationship;
    
    private Combobox cboStatus;
    
    private Combobox cboAgeAtDeath;
    
    private Combobox cboMultipleBirth;
    
    private Combobox cboMultipleBirthType;
    
    private Textbox txtName;
    
    private Button btnSave;
    
    private Grid grid;
    
    // End of auto-wire section
    
    private FamilyMemberHistory member;
    
    private boolean cancelled;
    
    private boolean changed;
    
    private FamilyHistoryService service;
    
    public static FamilyMemberHistory execute(FamilyMemberHistory fhx, FamilyHistoryService service) {
        Map<Object, Object> args = new HashMap<>();
        args.put("member", fhx);
        args.put("service", service);
        Window dlg = PopupDialog.popup(DIALOG, args, true, true, true);
        AddEditMemberController controller = (AddEditMemberController) FrameworkController.getController(dlg);
        return controller == null || controller.cancelled ? null : controller.member;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        member = (FamilyMemberHistory) arg.get("member");
        service = (FamilyHistoryService) arg.get("service");
        populateControls();
        ZKUtil.wireChangeEvents(grid.getRows(), comp, Events.ON_CHANGE);
    }
    
    private void populateControls() {
        populateCombobox(cboRelationship, service.getRelationshipChoices(),
            member.getRelationship().getCodingFirstRep().getDisplay());
        populateCombobox(cboStatus, service.getStatusChoices(), null);
        populateCombobox(cboAgeAtDeath, service.getAgeAtDeathChoices(), null);
        populateCombobox(cboMultipleBirth, service.getMultipleBirthChoices(), null);
        populateCombobox(cboMultipleBirthType, service.getMultipleBirthTypeChoices(), null);
        txtName.setText(member.getName());
    }
    
    private void populateCombobox(Combobox cbo, List<FileEntry> choices, String current) {
        cbo.setModel(new ListModelList<>(choices, false));
        cbo.setReadonly(true);
        
        if (current != null) {
            for (Comboitem item : cbo.getItems()) {
                if (item.<FileEntry> getValue().getInternalValue().equals(current)) {
                    cbo.setSelectedItem(item);
                    break;
                }
            }
        }
    }
    
    private void populateFamilyMember() {
    
    }
    
    private boolean confirmCancel() {
        return !changed || PromptDialog.confirm(
            "If you continue, your unsaved changes will be lost.  Are you sure you want to do this?", "Unsaved Changes");
    }
    
    private boolean commit() {
        return true;
    }
    
    public void onClose() {
        close(true);
    }
    
    public void onChange() {
        changed = true;
        btnSave.setDisabled(false);
    }
    
    public void onClick$btnSave() {
        close(false);
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    private void close(boolean cancelled) {
        if (cancelled && !confirmCancel()) {
            return;
        }
        
        if (!cancelled && !commit()) {
            return;
        }
        
        this.cancelled = cancelled;
        root.detach();
    }
}
