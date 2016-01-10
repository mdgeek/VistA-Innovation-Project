package org.medsphere.cwf.rpmsChiefComplaint;

import static org.carewebframework.common.StrUtil.CRLF;
import static org.carewebframework.common.StrUtil.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import org.carewebframework.cal.api.encounter.EncounterContext;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.vista.api.encounter.EncounterFlag;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.ui.encounter.EncounterUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;

public class AddComplaintController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = "~./org/medsphere/cwf/rpmsChiefComplaint/addChiefComplaint.zul";
    
    private Button btnSave;
    
    private Button btnClear;
    
    private Button btnAppend;
    
    private Textbox txtComplaint;
    
    private Groupbox gbSeverity;
    
    private Groupbox gbLocation;
    
    private Groupbox gbDuration;
    
    private Radiogroup rgSeverity;
    
    private Radiogroup rgLocation;
    
    private Radiogroup rgDuration;
    
    private Radiogroup rgComplaints;
    
    private Radiogroup rgWords;
    
    private Combobox cbOther;
    
    private ChiefComplaint complaint;
    
    private Div divRadio;
    
    private Spinner spnDuration;
    
    private String visitIEN = "";
    
    private enum CCCategory {
        SYMPTOMS, DISEASE, PATIENTREQ
    };
    
    private final static Set<EncounterFlag> EF1 = EncounterFlag.flags(EncounterFlag.NOT_LOCKED, EncounterFlag.VALIDATE_ONLY,
        EncounterFlag.FORCE);
        
    private final List<String> header = new ArrayList<String>();
    
    private final List<String> complaints = new ArrayList<String>();
    
    private static final Comparator<String> PLComparator = new Comparator<String>() {
        
        @Override
        public int compare(String itm1, String itm2) {
            String str1 = StrUtil.piece(itm1, U, 2);
            String str2 = StrUtil.piece(itm2, U, 2);
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
    };
    
    public static void execute(ChiefComplaint complaint) {
        Params params = new Params(complaint);
        PopupDialog.popup(DIALOG, params, true, false, true);
        return;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Iterator<Object> params = this.getParameters();
        this.complaint = (ChiefComplaint) params.next();
        getPrefixes();
        loadComplaints();
        //setGBVisibility(true);
        if (complaint != null) {
            txtComplaint.setText(complaint.getNarrativeFormatted());
        }
        txtComplaint.setFocus(true);
    }
    
    private void getPrefixes() {
        String pcs[] = StrUtil.split("Patient complains of^Patient reports^Patient requests", U);
        for (CCCategory c : CCCategory.values()) {
            header.add(VistAUtil.getSysParam("BGO CC PREFIX TEXT", pcs[c.ordinal()], Integer.toString((c.ordinal() + 1))));
        }
    }
    
    /**
     * Loads form data from the current chief complaint
     */
    private void loadComplaints() {
        getComplaints(complaints, rgComplaints.getSelectedIndex() + 1);
        clearComplaints();
        for (String s : complaints) {
            String pcs[] = StrUtil.split(s, U, 3);
            Radio radio = new Radio(pcs[1] + (pcs[2].equals("1") ? "*" : ""));
            radio.setId(pcs[0]);
            radio.setParent(divRadio);
            divRadio.appendChild(radio);
        }
        disableLocationControls(true);
        Clients.resize(divRadio);
    }
    
    private void getComplaints(List<String> result, Integer type) {
        getBroker().callRPCList("BGOCC GETPL", result, type);
        Collections.sort(result, PLComparator);
    }
    
    private void clearComplaints() {
        divRadio.getChildren().clear();
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onClick$btnSave() {
        
        if (validate()) {
            try {
                String result = "";
                String ccIEN = complaint != null ? complaint.getId().getIdPart() : "";
                result = getBroker().callRPC("BGOCC SET",
                    visitIEN + U + ccIEN + U + txtComplaint.getText().replace('\10', '\13'));
                PCC.errorCheck(result);
                close(false);
            } catch (Exception e) {
                PromptDialog.showError(e.getMessage());
                return;
            }
        } else {
            close(true);
        }
    }
    
    public void onClick$btnClear() {
        txtComplaint.setValue("");
        txtComplaint.invalidate();
        updateControls();
    }
    
    public void onClick$btnAppend() {
        if (rgComplaints.getSelectedIndex() > -1) {
            updateComplaintText();
        }
    }
    
    public void onCheck$rgWords() {
        String s = rgWords.getSelectedItem().getLabel();
        Boolean d = s.charAt(s.length() - 1) == '*';
        disableLocationControls(!d);
    }
    
    private void disableLocationControls(Boolean disable) {
        List<Radio> lstR = rgLocation.getChildren();
        for (Radio r : lstR) {
            r.setDisabled(disable);
        }
        cbOther.setDisabled(disable);
    }
    
    private void clearLocationControls() {
        rgLocation.setSelectedIndex(-1);
        cbOther.setText("");
    }
    
    public void onDoubleClick$rgWords() {
        updateComplaintText();
    }
    
    private Integer findEnd(String header) {
        Integer i, j, l;
        String s, c;
        j = -1;
        
        i = findStart(header);
        
        s = txtComplaint.getText();
        if (i == -1) {
            s = s + (s.isEmpty() || s.endsWith(CRLF) ? "" : CRLF);
            s = s + nextNum() + ".) " + header + ".";
            txtComplaint.setText(s);
            l = s.length();
            return l; //txtComplaint.toString().length();
        }
        
        j = s.indexOf(".)", i);
        j = j == -1 ? s.length() : j; //removed +1
        
        do {
            c = s.substring(j - 1, j);
            if (c.indexOf("0123456789. " + CRLF, 0) > -1) {
                j = j - 1;
            } else {
                break;
            }
        } while (j > 0);
        
        return j;
    }
    
    private String nextNum() {
        String s;
        Integer n, i, j;
        n = 0;
        s = txtComplaint.getText();
        i = s.indexOf(".)");
        while (i > 0) {
            do {
                if (NumberUtils.isNumber(StringUtils.mid(s, i - 1, 1))) {
                    i = i - 1;
                } else {
                    break;
                }
            } while (i > 0);
            j = StrUtil.extractInt(StringUtils.mid(s, i, s.length()));
            n = j > n ? j : n;
            i = s.indexOf(".)", i + 2);
        }
        
        return Integer.toString(n + 1);
    }
    
    private Integer findStart(String headerText) {
        return txtComplaint.getText().indexOf(headerText);
    }
    
    private String getCC() {
        String s = "";
        String sLoc = "";
        Radio r = rgWords.getSelectedItem();
        if (r == null) {
            return s;
        }
        s = r.getLabel();
        Boolean d = s.charAt(s.length() - 1) == '*';
        if (rgComplaints.getSelectedIndex() == 0 && (d)) {
            Radio rl = rgLocation.getSelectedItem();
            s = s.substring(0, s.length() - 1);
            if (rl != null) {
                sLoc = rl.getLabel().toLowerCase() + " ";
            }
            
            if (!cbOther.getText().isEmpty()) {
                sLoc = sLoc + cbOther.getText();
                addLocation(cbOther.getText());
            }
            
            if (!sLoc.isEmpty()) {
                s = s + " @ " + sLoc;
            }
        }
        
        if (rgSeverity.getSelectedIndex() > -1) {
            Radio r1 = rgSeverity.getSelectedItem();
            String l = r1.getLabel() + " ";
            s = l + s;
        }
        
        if (!spnDuration.getText().isEmpty()) {
            String dlabel = rgDuration.getSelectedItem().getLabel();
            Integer dur = spnDuration.getValue();
            s = s + " for " + dur.toString();
            s = s + " " + (dur > 1 ? dlabel : dlabel.substring(0, dlabel.length() - 1));
        }
        
        return s;
    }
    
    private void addLocation(String loc) {
        for (Comboitem s : cbOther.getItems()) {
            if (s.getLabel() == loc) {
                return;
            }
        }
        cbOther.appendItem(loc);
    }
    
    private void updateComplaintText() {
        String s, h, s1, s2;
        Integer i1;
        s = getCC();
        if (s.isEmpty()) {
            return;
        }
        
        h = header.get(rgComplaints.getSelectedIndex());
        i1 = findEnd(h);
        s1 = txtComplaint.getText();
        s2 = s1.substring(i1 - 1);
        s1 = s1.substring(0, i1 - 1);
        if (!s1.endsWith(h)) {
            s1 = s1 + ",";
        }
        txtComplaint.setText(s1 + " " + s + s2);
        //txtComplaint.setSelectionRange(0, 0);
        //Clients.scrollTo(0, 0);
        
        updateControls();
    }
    
    /**
     * Update list of complaints when type changes.
     */
    public void onClick$rgComplaints() {
        Integer idx = rgComplaints.getSelectedIndex();
        switch (idx) {
            case 0:
                setGBVisibility(true);
                break;
            default:
                setGBVisibility(false);
                break;
        }
        
        loadComplaints();
    }
    
    public void onChange$spnDuration() {
        Integer i = spnDuration.getValue();
        if (i == 0) {
            spnDuration.setText("");
        }
        //spnDuration.setText(spnDuration.getValue() == 0 ? "" : spnDuration.getValue().toString());
    }
    
    private void setGBVisibility(Boolean flag) {
        //TODO: change to divRGRight.setVisible(flag);
        gbSeverity.setVisible(flag);
        gbLocation.setVisible(flag);
        gbDuration.setVisible(flag);
    }
    
    private void updateControls() {
        btnClear.setDisabled(txtComplaint.getText().isEmpty());
        btnAppend.setDisabled(rgComplaints.getSelectedIndex() < 0);
        btnSave.setDisabled(txtComplaint.getText().isEmpty());
        rgSeverity.setSelectedIndex(-1);
        rgDuration.setSelectedIndex(1);
        
        spnDuration.setText("");
        clearLocationControls();
    }
    
    private Boolean validate() {
        txtComplaint.setText(StrUtil.xlate(txtComplaint.getText(), U, ""));
        if (txtComplaint.getText().isEmpty()) {
            return false;
        }
        
        if (visitIEN.isEmpty()) {
            if (!EncounterUtil.ensureEncounter(EF1)) {
                PromptDialog.showWarning("Unable to establish a visit context to store this exam.",
                    "Chief Complaint not Stored");
                return false;
            } else {
                visitIEN = EncounterContext.getActiveEncounter().getId().getIdPart();
            }
        }
        return true;
    }
    
}
