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
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.FileEntry;
import org.carewebframework.vista.ui.common.FileEntryRenderer;
import org.carewebframework.vista.ui.familyhistory.model.FamilyMember;
import org.carewebframework.vista.ui.familyhistory.service.FamilyHistoryService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for adding new members.
 */
public class AddEditMemberController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PATH + "addEditMember.zul";
    
    private static final FileEntryRenderer renderer = new FileEntryRenderer();
    
    // Start of auto-wire section
    
    private Combobox cboRelationship;
    
    private Combobox cboStatus;
    
    private Combobox cboAgeAtDeath;
    
    private Combobox cboMultipleBirth;
    
    private Combobox cboMultipleBirthType;
    
    private Label lblMultipleBirthType;
    
    private Textbox txtName;
    
    private Textbox txtCauseOfDeath;
    
    private Row rowDeath1;
    
    private Row rowDeath2;
    
    private Button btnSave;
    
    private Grid grid;
    
    // End of auto-wire section
    
    private FamilyMember member;
    
    private boolean cancelled;
    
    private boolean changed;
    
    private FamilyHistoryService service;
    
    public static FamilyMember execute(FamilyMember fhx, FamilyHistoryService service) {
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
        member = (FamilyMember) arg.get("member");
        
        if (member == null) {
            member = new FamilyMember();
        }
        
        service = (FamilyHistoryService) arg.get("service");
        populateComboboxes();
        Events.postEvent("onDeferredInit", comp, null);
    }
    
    /**
     * Deferred to allow combo boxes to fully render.
     */
    public void onDeferredInit() {
        populateControls();
    }
    
    private void updateControls() {
        FileEntry status = getFileEntry(cboStatus);
        boolean deceased = status != null && "D".equals(status.getInternalValue());
        rowDeath1.setVisible(deceased);
        rowDeath2.setVisible(deceased);
        txtCauseOfDeath.setVisible(deceased);
        FileEntry multipleBirth = getFileEntry(cboMultipleBirth);
        boolean multiple = multipleBirth != null && "Y".equals(multipleBirth.getInternalValue());
        lblMultipleBirthType.setVisible(multiple);
        cboMultipleBirthType.setVisible(multiple);
    }
    
    private void populateComboboxes() {
        populateCombobox(cboRelationship, service.getRelationshipChoices());
        populateCombobox(cboStatus, service.getStatusChoices());
        populateCombobox(cboAgeAtDeath, service.getAgeAtDeathChoices());
        populateCombobox(cboMultipleBirth, service.getMultipleBirthChoices());
        populateCombobox(cboMultipleBirthType, service.getMultipleBirthTypeChoices());
    }
    
    private void populateCombobox(Combobox cbo, List<FileEntry> choices) {
        cbo.setItemRenderer(renderer);
        cbo.setModel(new ListModelList<>(choices, false));
        cbo.setReadonly(true);
    }
    
    private void populateControls() {
        ListUtil.selectComboboxData(cboRelationship, member.getRelationship());
        ListUtil.selectComboboxData(cboStatus, member.getStatus());
        ListUtil.selectComboboxData(cboAgeAtDeath, member.getAgeAtDeath());
        ListUtil.selectComboboxData(cboMultipleBirth, member.getMultipleBirth());
        ListUtil.selectComboboxData(cboMultipleBirthType, member.getMultipleBirthType());
        txtName.setText(member.getName());
        txtCauseOfDeath.setText(member.getCauseOfDeath());
        updateControls();
        ZKUtil.wireChangeEvents(grid.getRows(), root, Events.ON_CHANGE);
    }
    
    /**
     * Allows combobox to fully render before setting current value.
     * 
     * @param event
     */
    public void onComboInit(Event event) {
        Combobox cbo = (Combobox) event.getData();
        ListUtil.selectComboboxData(cbo, cbo.getAttribute("current"));
    }
    
    private void populateFamilyMember() {
        member.setRelationship(getFileEntry(cboRelationship));
        member.setStatus(getFileEntry(cboStatus));
        member.setAgeAtDeath(getFileEntry(cboAgeAtDeath));
        member.setMultipleBirth(getFileEntry(cboMultipleBirth));
        member.setMultipleBirthType(getFileEntry(cboMultipleBirthType));
        member.setName(txtName.getText().trim());
    }
    
    private FileEntry getFileEntry(Combobox cbo) {
        Comboitem item = cbo.getSelectedItem();
        return item == null ? null : (FileEntry) item.getValue();
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
    
    public void onSelect$cboStatus() {
        updateControls();
    }
    
    public void onSelect$cboMultipleBirth() {
        updateControls();
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
