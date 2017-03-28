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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.terminology.TermMatch;
import org.carewebframework.rpms.ui.terminology.sct.controller.LookupSCTController;
import org.carewebframework.rpms.ui.terminology.sct.controller.LookupSCTController.SelectedTerm;
import org.carewebframework.ui.FormController;
import org.carewebframework.vista.plugin.familyhistory.model.Condition;
import org.carewebframework.vista.plugin.familyhistory.service.FamilyHistoryService;

import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Textbox;

/**
 * Controller for adding new conditions.
 */
public class AddEditConditionController extends FormController<Condition> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PATH + "addEditCondition.zul";
    
    public static boolean execute(Condition condition) {
        return execute(DIALOG, condition);
    }
    
    // Start of auto-wire section
    
    private Bandbox bbCondition;
    
    private Textbox txtNote;
    
    private Intbox intAgeAtDiagnosis;
    
    private Checkbox chkAgeApproximate;
    
    // End of auto-wire section
    
    private final FamilyHistoryService service;
    
    private SelectedTerm term;
    
    public AddEditConditionController(FamilyHistoryService service) {
        this.service = service;
    }
    
    public void onOpen$bbCondition() {
        SelectedTerm result = LookupSCTController.execute("SRCH Family History");
        
        if (result != null) {
            term = result;
            bbCondition.setText(term.term.getTermText());
            changed(bbCondition);
        }
    }
    
    @Override
    protected void populateControls(Condition condition) {
        bbCondition.setText(condition.getSCTText());
        txtNote.setText(condition.getNote());
        intAgeAtDiagnosis.setValue(condition.getAgeAtOnset());
        
        if (condition.isAgeApproximate() != null) {
            chkAgeApproximate.setChecked(condition.isAgeApproximate());
        }
    }
    
    @Override
    protected void populateDomainObject(Condition condition) {
        condition.setSCTText(bbCondition.getText());
        condition.setNote(txtNote.getText().trim());
        condition.setAgeAtOnset(intAgeAtDiagnosis.getValue());
        condition.setAgeApproximate(condition.getAgeAtOnset() == null ? null : chkAgeApproximate.isChecked());
        
        if (term != null) {
            TermMatch match = term.termMatch;
            String icd9s = StrUtil.fromList(Arrays.asList(match.getMappedICDs()), ";");
            condition.setICD9(StrUtil.piece(icd9s, ";"));
            condition.setICD9Other(StrUtil.piece(icd9s, ";", 2, 9999));
            condition.setSCTCode(match.getCode());
            condition.setSCTDx(term.term.getDescriptionId());
        }
    }
    
    @Override
    protected boolean hasRequired() {
        if (StringUtils.isEmpty(bbCondition.getText())) {
            return isMissing(bbCondition);
        }
        
        return true;
    }
    
    @Override
    protected void commit(Condition condition) {
        service.addEditCondition(condition);
    }
    
}
