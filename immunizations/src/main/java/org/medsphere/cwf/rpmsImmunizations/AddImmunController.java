package org.medsphere.cwf.rpmsImmunizations;

import static org.carewebframework.common.StrUtil.U;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.domain.IUser;
import org.carewebframework.cal.api.encounter.EncounterContext;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.common.FhirUtil;
import org.carewebframework.fhir.common.IReferenceable;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.BgoConstants;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupController;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupParams.Table;
import org.carewebframework.ui.zk.DateTimebox;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.encounter.EncounterFlag;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.encounter.EncounterUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.ComboitemRenderer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Doublespinner;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import org.medsphere.cwf.rpmsImmunizations.MainController.EventType;

import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.resource.Practitioner;

public class AddImmunController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = "~./org/medsphere/cwf/rpmsImmunizations/addImmunization.zul";
    
    private Button btnSave;
    
    private Textbox txtVaccine;
    
    private Textbox txtProvider;
    
    private Button btnLocation;
    
    private Textbox txtLocation;
    
    private Radio radFacility;
    
    private Radio radOther;
    
    private Radio radCurrent;
    
    private Radio radHistorical;
    
    private Radio radRefusal;
    
    private Textbox txtComment;
    
    private Textbox txtAdminNote;
    
    private Datebox datEventDate;
    
    private EventType eventType;
    
    private String visitIEN = "";
    
    private Component fraCurrent;
    
    private Component fraHistorical;
    
    private Component fraRefusal;
    
    private Component fraDate;
    
    private Component fraReaction;
    
    private Component fraAdminNote;
    
    private static final String NONESEL = "(None Selected)";
    
    private ImmunItem immunItem;
    
    private Combobox cboReason;
    
    private Combobox cboSite;
    
    private Combobox cboVacElig;
    
    private Label lblVacElig;
    
    private Label lblWarn;
    
    private Combobox cboLot;
    
    private Combobox cboReaction;
    
    private Combobox cboOverride;
    
    private DateTimebox datGiven;
    
    private Window winMain;
    
    private Doublespinner spnVolume;
    
    private final String TC_NO_REF_REASON = "Refusal Reason not Selected";
    
    private final List<String> refusalReasons = new ArrayList<String>();
    
    private final List<String> lotNumbers = new ArrayList<String>();
    
    private final List<String> vacSites = new ArrayList<String>();
    
    private final List<String> vacElig = new ArrayList<String>();
    
    private final List<String> vacReactions = new ArrayList<String>();
    
    private final List<String> vacOverrides = new ArrayList<String>();
    
    private final List<String> vacDefaults = new ArrayList<String>();
    
    private String strVFCData;
    
    private Datebox datVIS;
    
    private Checkbox cbCounsel;
    
    private Patient patient;
    
    private Label lblProv;
    
    private String selPrv;
    
    private String selLoc;
    
    private static final Comparator<String> PLComparator = new Comparator<String>() {
        
        @Override
        public int compare(String itm1, String itm2) {
            String str1 = StrUtil.piece(itm1, U, 2);
            String str2 = StrUtil.piece(itm2, U, 2);
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
    };
    
    private final ComboitemRenderer<String> comboRenderer = new ImmunComboRenderer();
    
    //private final static Set<EncounterFlag> EF = EncounterFlag.flags(EncounterFlag.NOT_LOCKED, EncounterFlag.VALIDATE_ONLY);
    
    private final static Set<EncounterFlag> EF1 = EncounterFlag.flags(EncounterFlag.NOT_LOCKED, EncounterFlag.VALIDATE_ONLY,
        EncounterFlag.FORCE);
        
    public static enum VacDefault {
        LOT, VOLUME, VISDATE
    }
    
    public static void execute(ImmunItem item) {
        Params params = new Params(item);
        PopupDialog.popup(DIALOG, params, true, true, true);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Params params = (Params) arg;
        this.immunItem = (ImmunItem) params.get(0);
        Window win = (Window) root;
        win.setTitle(StrUtil.formatMessage(win.getTitle(), immunItem == null ? "Add" : "Edit"));
        strVFCData = getVFCData();
        patient = PatientContext.getActivePatient();
        loadForm();
    }
    
    /**
     * Loads form data from the current Immunization Item
     */
    private void loadForm() {
        getReasons(refusalReasons);
        getVacEligibilty(vacElig);
        getVacSites(vacSites);
        getReactions(vacReactions);
        getOverrides(vacOverrides);
        updateComboValues();
        
        selPrv = "";
        selLoc = "";
        
        //datGiven.setDateConstraint(new SimpleDateConstraint(SimpleDateConstraint.NO_NEGATIVE, DateUtil.addDays(patient.getBirthDate(), -1, true), null, BgoConstants.TX_BAD_DATE_DOB));
        datGiven.setDateConstraint(getConstraintDOBDate());
        datEventDate.setConstraint(getConstraintDOBDate());
        radFacility.setLabel(getParam("Caption-Facility", "&Facility"));
        if (immunItem != null) {
            
            txtVaccine.setValue(immunItem.getVaccineName());
            txtVaccine.setAttribute("ID", immunItem.getVaccineID());
            txtVaccine.setAttribute("DATA", immunItem.getVaccineID() + U + immunItem.getVaccineName());
            setEventType(immunItem.getEventType());
            
            radRefusal.setDisabled(!radRefusal.isChecked());
            radHistorical.setDisabled(!radHistorical.isChecked());
            radCurrent.setDisabled(!radCurrent.isChecked());
            
            visitIEN = immunItem.getVisitIEN();
            if (immunItem.getProvider() != null) {
                txtProvider.setText(FhirUtil.formatName(immunItem.getProvider().getName()));
                txtProvider.setAttribute("ID", immunItem.getProvider().getId().getIdPart());
                selPrv = immunItem.getProvider().getId().getIdPart() + U + U + immunItem.getProvider().getName();
            }
            switch (immunItem.getEventType()) {
                case REFUSAL:
                    ListUtil.selectComboboxItem(cboReason, immunItem.getReason());
                    txtComment.setText(immunItem.getComment());
                    datEventDate.setValue(immunItem.getDate());
                    break;
                case HISTORICAL:
                    datEventDate.setValue(immunItem.getDate());
                    txtLocation.setValue(immunItem.isImmunization() ? immunItem.getLocationName() : "");
                    txtLocation.setAttribute("ID", immunItem.isImmunization() ? immunItem.getLocationID() : "");
                    selLoc = immunItem.getLocationID() + U + U + immunItem.getLocationName();
                    radOther.setSelected(txtLocation.getAttribute("ID") != null
                            ? txtLocation.getAttribute("ID").toString().isEmpty() : false);
                    txtAdminNote.setText(immunItem.getAdminNotes());
                    ZKUtil.disableChildren(fraDate, true);
                    ZKUtil.disableChildren(fraHistorical, true);
                default:
                    getLot(lotNumbers);
                    loadComboValues(cboLot, lotNumbers, comboRenderer);
                    loadVaccination();
                    txtLocation.setValue(immunItem.isImmunization() ? immunItem.getLocationName() : "");
                    txtLocation.setAttribute("ID", immunItem.isImmunization() ? immunItem.getLocationID() : "");
                    selLoc = immunItem.getLocationID() + U + U + immunItem.getLocationName();
                    radOther.setSelected(txtLocation.getAttribute("ID") != null
                            ? txtLocation.getAttribute("ID").toString().isEmpty() : false);
                    ListUtil.selectComboboxItem(cboLot, immunItem.getLot());
                    ListUtil.selectComboboxItem(cboSite, StrUtil.piece(immunItem.getInjSite(), "~", 2));
                    spnVolume.setText(immunItem.getVolume());
                    datGiven.setDate(immunItem.getDate());
                    datVIS.setValue(immunItem.isImmunization() ? immunItem.getVISDate() : null);
                    ListUtil.selectComboboxItem(cboReaction, immunItem.getReaction());
                    ListUtil.selectComboboxData(cboOverride, immunItem.getVacOverride());
                    txtAdminNote.setText(immunItem.getAdminNotes());
                    cbCounsel.setChecked(immunItem.wasCounseled());
            }
        } else {
            IUser user = UserContext.getActiveUser();
            Practitioner provider = new Practitioner();
            provider.setId(user.getLogicalId());
            ;
            provider.setName(FhirUtil.parseName(user.getFullName()));
            txtProvider.setValue(FhirUtil.formatName(provider.getName()));
            txtProvider.setAttribute("ID", VistAUtil.parseIEN(provider)); //provider.getId().getIdPart());
            selPrv = txtProvider.getAttribute("ID") + U + U + txtProvider.getValue();
            Location location = new Location();
            location.setName("");
            location.setId("");
            datGiven.setDate(getBroker().getHostTime());
            
            onClick$btnVaccine(null);
            
            if (txtVaccine.getValue().isEmpty()) {
                close(true);
                return;
            }
            
            Encounter encounter = EncounterContext.getActiveEncounter();
            if (!EncounterUtil.isPrepared(encounter)) {
                setEventType(EventType.HISTORICAL);
                radCurrent.setDisabled(true);
            } else {
                if (isCategory(encounter, "E")) {
                    setEventType(EventType.HISTORICAL);
                    Date date = encounter == null ? null : encounter.getPeriod().getStart();
                    datEventDate.setValue(DateUtil.stripTime(date == null ? getBroker().getHostTime() : date));
                    radCurrent.setDisabled(true);
                    txtLocation.setText(user.getSecurityDomain().getName());
                    PromptDialog.showInfo(user.getSecurityDomain().getLogicalId());
                    txtLocation.setAttribute("ID", user.getSecurityDomain().getLogicalId());
                    
                } else {
                    if (isVaccineInactive()) {
                        setEventType(EventType.HISTORICAL);
                        radCurrent.setDisabled(true);
                    } else {
                        setEventType(EventType.CURRENT);
                        radCurrent.setDisabled(false);
                    }
                }
            }
            selectItem(cboReason, NONESEL);
        }
        btnSave.setLabel(immunItem == null ? "Add" : "Save");
        btnSave.setTooltiptext(immunItem == null ? "Add record" : "Save record");
        //cboReason.setFocus(true);
        txtVaccine.setFocus(true);
    }
    
    private String getConstraintDOBDate() {
        if (patient == null) {
            return "";
        } else {
            return "after " + new SimpleDateFormat("yyyyMMdd").format(patient.getBirthDate());
        }
    }
    
    private void updateVFC() {
        boolean b = "Y".equals(StrUtil.piece(strVFCData, U));
        lblVacElig.setVisible(b);
        cboVacElig.setVisible(b);
        selectItem(cboVacElig, StrUtil.piece(strVFCData, U, 3));
    }
    
    private void selectItem(Combobox cbo, String label) {
        if (label != null) {
            ListUtil.selectComboboxItem(cbo, label);
        }
    }
    
    private boolean isCategory(Encounter encounter, String category) {
        return category.equals(EncounterUtil.getServiceCategory(encounter));
    }
    
    private void updateControls() {
        boolean enabled = false;
        
        if (eventType != null && !txtVaccine.getValue().isEmpty()) {
            switch (eventType) {
                case HISTORICAL:
                    enabled = datEventDate.getValue() != null && (!txtLocation.getValue().isEmpty());
                    break;
                case CURRENT:
                    enabled = true;
                    break;
                case REFUSAL:
                    enabled = datEventDate.getValue() != null && (cboReason.getSelectedIndex() > 0);
                    break;
            }
        }
        btnSave.setDisabled(!enabled);
        btnSave.setFocus(enabled);
        btnSave.invalidate();
    }
    
    private String getVFCData() {
        return getBroker().callRPC("BGOVIMM GETVFC", VistAUtil.parseIEN(PatientContext.getActivePatient())); //PatientContext.getActivePatient().getId().getIdPart() );
    }
    
    private void setEventType(EventType evType) {
        eventType = evType;
        
        switch (evType) {
            case HISTORICAL:
                fraCurrent.setVisible(false);
                fraDate.setVisible(true);
                fraHistorical.setVisible(true);
                fraRefusal.setVisible(false);
                fraAdminNote.setVisible(true);
                radHistorical.setChecked(true);
                // radFacility.setSelected(true);
                lblProv.setValue("Documented By");
                break;
                
            case CURRENT:
                fraCurrent.setVisible(true);
                fraDate.setVisible(false);
                fraHistorical.setVisible(false);
                fraRefusal.setVisible(false);
                fraAdminNote.setVisible(true);
                radCurrent.setChecked(true);
                updateVFC();
                lblProv.setValue("Administered By");
                break;
                
            case REFUSAL:
                radRefusal.setChecked(true);
                fraCurrent.setVisible(false);
                fraHistorical.setVisible(false);
                fraDate.setVisible(true);
                fraAdminNote.setVisible(false);
                fraRefusal.setVisible(true);
                loadComboReasonValues(refusalReasons);
                selectItem(cboReason, "");
                lblProv.setValue("Documented By");
                break;
        }
        
        fraReaction.setVisible(eventType == EventType.CURRENT && immunItem != null ? true : false);
        //txtComment.setTooltip(eventType == evType.REFUSAL ? "Comment (max 245 chars.)" : "");
        updateDialogTitle();
        winMain.invalidate();
        updateControls();
    }
    
    private void getReasons(List<String> result) {
        getBroker().callRPCList("BGOREF GETREA", result, "IMMUNIZATION");
        Collections.sort(result, PLComparator);
        result.add(0, NONESEL);
    }
    
    private void getReactions(List<String> result) {
        getBroker().callRPCList("BGOUTL DICLKUP", result, "9002084.8^^^^^^.03=1^^.01");
        Collections.sort(result, PLComparator);
        result.add(0, "None");
    }
    
    private void getOverrides(List<String> result) {
        result.clear();
        result.add("0^No Override");
        result.add("1^Invalid - Bad Storage");
        result.add("2^Invalid - Defective");
        result.add("3^Invalid - Expired");
        result.add("4^Invalid - Admin Error");
        result.add("9^Forced Valid");
    }
    
    private void getLot(List<String> result) {
        getBroker().callRPCList("BGOVIMM LOT", result, getVaccineID());
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i);
            String[] pcs = StrUtil.split(s, U, 4);
            pcs[1] = pcs[1] + (pcs[2].isEmpty() ? "" : "  " + pcs[2]);
            pcs[1] = pcs[1] + (pcs[3].isEmpty() ? "" : "  (exp " + pcs[3] + ")");
            result.set(i, VistAUtil.concatParams(pcs[0], pcs[1], pcs[2], pcs[3]));
        }
        result.add(0, "(Lot Not Specified)");
    }
    
    private void getVacSites(List<String> result) {
        result.clear();
        result.add("Left Thigh IM");
        result.add("Left Thigh SQ");
        result.add("Right Thigh IM");
        result.add("Right Thigh SQ");
        result.add("Both Thighs IM");
        result.add("Left Deltoid IM");
        result.add("Left Arm SQ");
        result.add("Right Deltoid IM");
        result.add("Right Arm SQ");
        result.add("Oral");
        result.add("Intranasal");
        result.add("Left Arm Intradermal");
        result.add("Right Arm Intradermal");
    }
    
    private void getVacEligibilty(List<String> result) {
        result.clear();
        getBroker().callRPCList("BGOVIMM2 GETELIG", result, VistAUtil.parseIEN(PatientContext.getActivePatient()));
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i);
            String[] pcs = StrUtil.split(s, U, 4);
            result.set(i, pcs[0] + U + pcs[2] + U + pcs[1] + U + pcs[3]);
        }
        Collections.sort(result, PLComparator);
        result.add(0, "(None Selected)");
    }
    
    private String getVaccineID() {
        return getVaccineInfo(1);
    }
    
    private boolean isVaccineInactive() {
        return StrUtil.toBoolean(getVaccineInfo(5));
    }
    
    private String getVaccineInfo(int position) {
        String result = "";
        Object vac = txtVaccine.getAttribute("DATA");
        if (vac != null) {
            result = StrUtil.piece(vac.toString(), StrUtil.U, position);
        }
        return result;
    }
    
    private void updateComboValues() {
        loadComboReasonValues(refusalReasons);
        loadComboValues(cboSite, vacSites, comboRenderer);
        loadComboValues(cboVacElig, vacElig, comboRenderer);
        loadComboValues(cboReaction, vacReactions, comboRenderer);
        loadComboValues(cboOverride, vacOverrides, comboRenderer);
    }
    
    private void loadComboReasonValues(List<String> lst) {
        cboReason.setItemRenderer(comboRenderer);
        cboReason.setModel(new ListModelList<String>(lst));
        Events.sendEvent(cboReason, new Event("onInitRender", cboReason, "0"));
    }
    
    private void loadComboValues(Combobox cbo, List<String> lst, ComboitemRenderer<?> renderer) {
        cbo.setItemRenderer(renderer);
        cbo.setModel(new ListModelList<String>(lst));
        ;
        Events.sendEvent(cbo, new Event("onInitRender", cbo, "0"));
    }
    
    /**
     * Retrieves information specific to patient and selected vaccination. First record: Default Lot
     * [1] ^ Default Volume [2] ^ Default VIS Date [3] Subsequent records: Contraindication IEN [1]
     * ^ Contraindication Text [2] ^ Date Noted [3]
     */
    
    private void loadVaccination() {
        String s = "";
        vacDefaults.clear();
        getBroker().callRPCList("BGOVIMM LOADIMM", vacDefaults,
            VistAUtil.parseIEN(PatientContext.getActivePatient()) + U + getVaccineID());
            
        if (PCC.errorCheck(vacDefaults)) {
            onClick$btnCancel();
        }
        
        if (immunItem == null && !getVacContraindications().isEmpty()) {
            if (!PromptDialog
                    .confirm(
                        "The patient has the following contraindications" + " for " + txtVaccine.getText() + " of: "
                                + StrUtil.CRLF + getVacContraindications() + "Do you wish to continue?",
                        "ALERT: Contraindications Exist")) {
                onClick$btnCancel();
            }
        }
        
        spnVolume.setText(getVacDefaults(VacDefault.VOLUME));
        datVIS.setValue(VistAUtil.parseDate(getVacDefaults(VacDefault.VISDATE)));
        ListUtil.selectComboboxData(cboLot, getVacDefaults(VacDefault.LOT));
        displayWarning();
        
        s = txtVaccine.getText().toUpperCase();
        if (StringUtils.indexOf(s, " ORAL") > -1 || (StringUtils.startsWith(s, "OPV"))) {
            selectItem(cboSite, "ORAL");
        } else {
            if (StringUtils.indexOf(s, " INTRANASAL") > -1) {
                selectItem(cboSite, "INTRANASAL");
            }
        }
        
        cboLot.setFocus(true);
        
    }
    
    private String getVacContraindications() {
        String result = "";
        for (int i = 1; i < vacDefaults.size(); i++) {
            result = result + "--> " + StrUtil.piece(vacDefaults.get(i), U, 2) + StrUtil.CRLF;
        }
        return result;
    }
    
    private String getVacDefaults(VacDefault position) {
        String result = "";
        if (vacDefaults.size() > 0) {
            result = StrUtil.piece(vacDefaults.get(0), U, position.ordinal() + 1);
        }
        return result;
    }
    
    public void onClick$btnProvider() {
        String val = LookupController.execute(Table.rtProvider, txtProvider.getValue());
        val = val != null ? val : selPrv;
        updateTextBox(txtProvider, val);
        selPrv = val;
        updateControls();
    }
    
    public void updateTextBox(Textbox tb, String val) {
        tb.setAttribute("ID", StrUtil.piece(val, U));
        tb.setText(StrUtil.piece(val, U, 3));
    }
    
    public void onClick$btnLocation() {
        String val = LookupController.execute(Table.rtLocation, txtLocation.getValue());
        val = val != null ? val : selLoc;
        updateTextBox(txtLocation, val);
        selLoc = val;
        updateControls();
    }
    
    public void onClick$btnVaccine(Event event) {
        boolean b = ZKUtil.getEventOrigin(event) instanceof MouseEvent;
        //String val = ImmunSelectorController.execute(txtVaccine.getValue() == null ? "" : txtVaccine.getValue(), b);
        String val = ImmunSelectorController.execute("", b);
        if (val != null) {
            txtVaccine.setAttribute("DATA", val);
            txtVaccine.setValue(StrUtil.piece(val, StrUtil.U, 2));
            txtVaccine.setAttribute("ID", StrUtil.piece(val, StrUtil.U));
            resetControls();
            getLot(lotNumbers);
            loadComboValues(cboLot, lotNumbers, comboRenderer);
            loadVaccination();
        }
        updateControls();
    }
    
    private void resetControls() {
        cboLot.setText("");
        
    }
    
    public void onClick$radHistorical() {
        setEventType(EventType.HISTORICAL);
    }
    
    public void onClick$radCurrent() {
        setEventType(EventType.CURRENT);
    }
    
    public void onClick$radRefusal() {
        setEventType(EventType.REFUSAL);
    }
    
    public void onClick$radFacility() {
        btnLocation.setVisible(true);
        txtLocation.setValue("");
        selLoc = "";
        updateControls();
    }
    
    public void onClick$radOther() {
        btnLocation.setVisible(false);
        txtLocation.setAttribute("ID", null);
        txtLocation.setValue("");
        selLoc = "";
        updateControls();
    }
    
    public void onChanging$spnVolume(InputEvent event) {
        String s = event.getValue();
        
        if ("0.00".equals(s)) {
            spnVolume.setValue(null);
            spnVolume.invalidate();
        }
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onClick$btnSave() {
        if (!validate()) {
            return;
        }
        
        boolean saveAsContraindication = false;
        
        if (fraReaction.isVisible()) {
            int i = cboReaction.getSelectedIndex();
            
            if (i > 0) {
                String IEN = cboReaction.getSelectedItem().getValue();
                
                switch (IEN) {
                    case "12":
                        i = 1;
                        break;
                    case "6":
                        i = 3;
                        break;
                    case "7":
                        i = 5;
                        break;
                    case "9":
                        i = 4;
                        break;
                    default:
                        i = 10;
                }
                
                if (i == 10) {
                    if (PromptDialog.confirm("Do you wish to save the reaction of '" + cboReaction.getText()
                            + "' as a contraindication for the patient?",
                        "Save to Contraindications?")) {
                        saveAsContraindication = true;
                    }
                } else {
                    saveAsContraindication = true;
                }
                
                if (saveAsContraindication) {
                    try {
                        String result = "";
                        result = getBroker().callRPC("BGOVIMM SETCONT", toContraDAO(i));
                        if (PCC.errorCheck(result)) {
                            return;
                        }
                    } catch (Exception e) {
                        PromptDialog.showError(e.getMessage());
                        return;
                    }
                }
            }
        }
        
        try {
            String result = "";
            
            switch (eventType) {
                case HISTORICAL:
                case CURRENT:
                    result = getBroker().callRPC("BGOVIMM SET", toDAOI(immunItem));
                    PCC.errorCheck(result);
                    break;
                    
                case REFUSAL:
                    result = getBroker().callRPC("BGOREF SET", toDAOR(immunItem));
                    PCC.errorCheck(result);
            }
            close(false);
        } catch (Exception e) {
            PromptDialog.showError(e.getMessage());
            return;
        }
    }
    
    public void onSelect$cboLot() {
        displayWarning();
    }
    
    public void onSelect$cboReason() {
        updateControls();
    }
    
    public void onSelect$cboVacElig() {
        updateControls();
    }
    
    public void onChange$txtLocation() {
        updateControls();
    }
    
    private void displayWarning() {
        String warning = "";
        int i = cboLot.getSelectedIndex();
        if (i > -1) {
            String s = cboLot.getSelectedItem().getLabel();
            String s1 = StrUtil.piece(StrUtil.piece(s, "(exp ", 2), ")");
            if (!s1.isEmpty()) {
                Date d = VistAUtil.parseDate(s1);
                if (d.compareTo(VistAUtil.getBrokerSession().getHostTime()) <= 0) {
                    warning = "The selected lot expired on " + s1 + ".";
                }
            }
        }
        lblWarn.setValue(warning);
    }
    
    /**
     * @param record Immunization information
     * @return V Immunization file IEN Add/Edit immunization INP = Visit IEN [1] ^ Historical [2] ^
     *         Patient IEN [3] ^ Imm IEN [4]^ V File IEN [5] ^ Provider IEN [6] ^ Location [7] ^
     *         Other Location [8] ^ Imm Date [9] ^ Lot # [10] ^ Reaction [11] ^ VIS Date [12] ^ Dose
     *         Override [13] ^ Inj Site [14] ^ Volume [15] ^ Counseled [16] ^ VFC Eligibility [17] ^
     *         admin comments [18]
     */
    private String toDAOI(ImmunItem record) {
        StringBuilder sb = new StringBuilder();
        appendData(sb, visitIEN);
        appendData(sb, eventType == EventType.HISTORICAL ? "1" : "0");
        appendData(sb, PatientContext.getActivePatient().getId().getIdPart());
        appendData(sb, getVaccineInfo(1));
        appendData(sb, record != null && record.getFileIEN() != null ? record.getFileIEN() : "");
        appendData(sb, txtProvider.getAttribute("ID").toString());
        if (radOther.isSelected()) {
            appendData(sb, "");
            appendData(sb, txtLocation.getText());
        } else {
            appendData(sb, txtLocation.getAttribute("ID") != null ? txtLocation.getAttribute("ID").toString() : "");
            appendData(sb, txtLocation.getValue() != null ? txtLocation.getValue().toString() : "");
        }
        appendData(sb, eventType == EventType.CURRENT ? new FMDate(datGiven.getDate()).getFMDate()
                : new FMDate(datEventDate.getValue()).getFMDate());
        appendData(sb,
            eventType == EventType.CURRENT && cboLot.getSelectedIndex() > -1 ? cboLot.getSelectedItem().getValue() : "");
        appendData(sb, fraReaction.isVisible() && (cboReaction.getSelectedIndex() > -1)
                ? cboReaction.getSelectedItem().getValue() : "");
        appendData(sb, eventType == EventType.CURRENT ? new FMDate(datVIS.getValue()).getFMDate() : "");
        
        appendData(sb, fraReaction.isVisible() && (cboOverride.getSelectedIndex() > -1)
                ? cboOverride.getSelectedItem().getValue() : "");
        appendData(sb,
            eventType == EventType.CURRENT && (cboSite.getSelectedIndex() > -1) ? cboSite.getSelectedItem().getValue() : "");
        appendData(sb, eventType == EventType.CURRENT ? spnVolume.getValue() : "");
        appendData(sb, eventType == EventType.CURRENT ? boolToInt(cbCounsel.isChecked()) : "");
        appendData(sb, eventType == EventType.CURRENT ? cboVacElig.getValue() : "");
        appendData(sb,
            txtAdminNote.getValue().isEmpty() ? "@" : StrUtil.xlate(txtAdminNote.getValue().toString(), StrUtil.CRLF, ""));
        return sb.toString();
    }
    
    /**
     * @param record Patient Refusal information
     * @return Refusal file IEN Add/edit a refusal INP = Refusal IEN [1] ^ Refusal Type [2] ^ Item
     *         IEN [3] ^ Patient IEN [4] ^ Refusal Date [5] ^ Comment [6] ^ Provider IEN [7] ^
     *         Reason [8] Patch 5, changed mammogram code for bilateral mammogram Patch 13,changes
     *         for SNOMED
     */
    
    private String toDAOR(ImmunItem record) {
        StringBuilder sb = new StringBuilder();
        String val = cboReason.getSelectedItem().getValue();
        String prv = txtProvider.getAttribute("ID").toString();
        String refIEN = record != null && record.getFileIEN() != null ? record.getFileIEN() : "";
        appendData(sb, refIEN);
        appendData(sb, "IMMUNIZATION");
        appendData(sb, getVaccineInfo(1));
        appendData(sb, PatientContext.getActivePatient().getId().getIdPart());
        appendData(sb, refIEN.isEmpty() ? "T" : record.getDate().getFMDate());
        appendData(sb,
            txtComment.getValue().isEmpty() ? "@" : StrUtil.xlate(txtComment.getValue().toString(), StrUtil.CRLF, ""));
        appendData(sb, prv);
        appendData(sb, val);
        return sb.toString();
    }
    
    private void appendData(StringBuilder sb, Object data) {
        sb.append(data == null ? "" : data instanceof IReferenceable ? ((IReferenceable) data).getId().getIdPart() : data)
                .append('^');
    }
    
    /**
     * @param IEN Contraindication IEN
     * @return BI PATIENT CONTRAINDICATIONS File IEN Set contraindication record
     */
    private String toContraDAO(int IEN) {
        String val = txtVaccine.getAttribute("DATA").toString();
        StringBuilder sb = new StringBuilder();
        appendData(sb, PatientContext.getActivePatient().getId().getIdPart());
        appendData(sb, StrUtil.piece(val, U, 1));
        appendData(sb, IEN);
        return sb.toString();
    }
    
    private boolean validate() {
        String val;
        val = getVaccineInfo(1);
        if (val.isEmpty()) {
            PromptDialog.showWarning("You must select a Vaccine.", "Vaccine not selected");
            txtVaccine.setFocus(true);
            return false;
        }
        
        if (eventType == EventType.HISTORICAL) {
            if ((datEventDate.getValue() == null) || txtLocation.getText().isEmpty()) {
                PromptDialog.showWarning("You must select a location and the date of the historical vaccination event.",
                    BgoConstants.TC_NO_LOC_DATE);
                return false;
            }
        }
        
        if (visitIEN.isEmpty() && eventType == EventType.CURRENT) {
            if (!EncounterUtil.ensureEncounter(EF1)) {
                PromptDialog.showWarning("Unable to establish a visit context to store this vaccination.",
                    "Vaccination not Stored");
                return false;
            } else {
                visitIEN = EncounterContext.getActiveEncounter().getId().getIdPart();
            }
        }
        
        if (eventType == EventType.REFUSAL && NONESEL.equals(cboReason.getValue())) {
            PromptDialog.showWarning("You must select a reason for the exam refusal.", TC_NO_REF_REASON);
            return false;
        }
        
        if (eventType == EventType.REFUSAL && txtComment.toString().length() == 1) {
            PromptDialog.showWarning(BgoConstants.TX_CMNT_TOO_SHORT, BgoConstants.TC_CMNT_TOO_SHORT);
            return false;
        }
        
        return true;
    }
    
    private String getParam(String param, String def) {
        String s = getBroker().callRPC("BGOUTL GETPARM", param);
        s = StringUtils.isEmpty(s) ? def : s;
        return s.replace("&", "");
    }
    
    private void updateDialogTitle() {
        String l = immunItem == null ? "Add" : "Edit";
        String m = radHistorical.isChecked() ? "Historical" : "";
        String r = radRefusal.isChecked() ? "Not Provided / Refused" : "";
        winMain.setTitle(StrUtil.formatMessage("%s %s Immunization %s", l, m, r));
    }
    
    private Integer boolToInt(Boolean val) {
        return val ? 1 : 0;
    }
    
    public void onChange$datEventDate(Event event) {
        
        if (datEventDate.getValue() != null) {
            int diff = datEventDate.getValue().compareTo(patient.getBirthDate());
            
            if (diff < 0) {
                PromptDialog.showError(BgoConstants.TX_BAD_DATE_DOB, BgoConstants.TC_BAD_DATE_DOB);
                datEventDate.setValue(DateUtil.stripTime(getBroker().getHostTime()));
            }
        }
        updateControls();
    }
    
    /*    public void onChange$datGiven(Event event) {
    	//Patient patient = PatientContext.getActivePatient();
    	Date d = datGiven.getDate();
    	if (d == null) {
    		return;
    	}
    	Date bd = patient.getBirthDate();
    	int diff = d.compareTo(patient.getBirthDate());
    	
    	if (diff < 0) {
    		PromptDialog.showError(BgoConstants.TX_BAD_DATE_DOB, BgoConstants.TC_BAD_DATE_DOB);
    		datGiven.setDate(getBroker().getHostTime());
    	}
    }*/
}
