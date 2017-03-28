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

import java.util.List;

import org.carewebframework.ui.FormController;
import org.carewebframework.ui.zk.LabeledElement;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.vista.api.util.FileEntry;
import org.carewebframework.vista.plugin.familyhistory.model.FamilyMember;
import org.carewebframework.vista.plugin.familyhistory.service.FamilyHistoryService;
import org.carewebframework.vista.ui.common.FileEntryRenderer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

/**
 * Controller for adding new members.
 */
public class AddEditMemberController extends FormController<FamilyMember> {

    private static final long serialVersionUID = 1L;

    private static final String DIALOG = Constants.RESOURCE_PATH + "addEditMember.zul";

    private static final FileEntryRenderer renderer = new FileEntryRenderer();

    // Start of auto-wire section

    private Combobox cboRelationship;

    private Combobox cboStatus;

    private Combobox cboAgeAtDeath;

    private Combobox cboMultipleBirth;

    private Combobox cboMultipleBirthType;

    private LabeledElement lblMultipleBirthType;

    private Textbox txtName;

    private Textbox txtCauseOfDeath;

    private Row rowDeath;

    // End of auto-wire section

    private final FamilyHistoryService service;

    public static boolean execute(FamilyMember fhx) {
        return execute(DIALOG, fhx);
    }

    public AddEditMemberController(FamilyHistoryService service) {
        this.service = service;
    }

    private void updateControls() {
        boolean deceased = isDeceased();
        rowDeath.setVisible(deceased);
        txtCauseOfDeath.setVisible(deceased);
        boolean multiple = isMultipleBirth();
        lblMultipleBirthType.setVisible(multiple);
    }

    private boolean isDeceased() {
        FileEntry status = getFileEntry(cboStatus);
        return status != null && "D".equals(status.getInternalValue());
    }

    private boolean isMultipleBirth() {
        FileEntry multipleBirth = getFileEntry(cboMultipleBirth);
        return multipleBirth != null && "Y".equals(multipleBirth.getInternalValue());
    }

    @Override
    protected void initControls() {
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

    @Override
    protected void populateControls(FamilyMember member) {
        ListUtil.selectComboboxData(cboRelationship, member.getRelationship());
        ListUtil.selectComboboxData(cboStatus, member.getStatus());
        ListUtil.selectComboboxData(cboAgeAtDeath, member.getAgeAtDeath());
        ListUtil.selectComboboxData(cboMultipleBirth, member.getMultipleBirth());
        ListUtil.selectComboboxData(cboMultipleBirthType, member.getMultipleBirthType());
        txtName.setText(member.getName());
        txtCauseOfDeath.setText(member.getCauseOfDeath());
        updateControls();
    }

    @Override
    protected void populateDomainObject(FamilyMember member) {
        member.setRelationship(getFileEntry(cboRelationship));
        member.setStatus(getFileEntry(cboStatus));
        boolean deceased = isDeceased();
        member.setAgeAtDeath(deceased ? getFileEntry(cboAgeAtDeath) : null);
        member.setCauseOfDeath(deceased ? txtCauseOfDeath.getText() : null);
        member.setMultipleBirth(getFileEntry(cboMultipleBirth));
        member.setMultipleBirthType(isMultipleBirth() ? getFileEntry(cboMultipleBirthType) : null);
        member.setName(txtName.getText().trim());
    }

    @Override
    protected boolean hasRequired() {
        if (cboRelationship.getSelectedItem() == null) {
            return isMissing(cboRelationship);
        }

        if (txtName.getText().trim().isEmpty()) {
            return isMissing(txtName);
        }

        if (cboStatus.getSelectedItem() == null) {
            return isMissing(cboStatus);
        }

        return true;
    }

    private FileEntry getFileEntry(Combobox cbo) {
        Comboitem item = cbo.getSelectedItem();
        return item == null ? null : (FileEntry) item.getValue();
    }

    @Override
    public void commit(FamilyMember member) {
        service.addEditMember(member);
    }

    public void onSelect$cboStatus() {
        updateControls();
    }

    public void onSelect$cboMultipleBirth() {
        updateControls();
    }

}
