package org.medsphere.cwf.rpmsImmunizations;

import static org.carewebframework.common.StrUtil.U;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.carewebframework.cal.api.encounter.EncounterContext;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.common.IReferenceable;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.encounter.EncounterFlag;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.encounter.EncounterUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class AddImmunContraController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Renders the list of visits.
     */
    private final AbstractListitemRenderer<String, Object> renderer = new AbstractListitemRenderer<String, Object>() {
        
        @Override
        protected void renderItem(Listitem item, String s) {
            createCell(item, StrUtil.piece(s, StrUtil.U, 2));
            item.addForward(Events.ON_DOUBLE_CLICK, btnSave, Events.ON_CLICK);
        }
    };
    
    private static final String DIALOG = "~./org/medsphere/cwf/rpmsImmunizations/addContraIndication.zul";
    
    private Button btnSave;
    
    private Listbox lstContra;
    
    private Textbox txtVaccine;
    
    private final static Set<EncounterFlag> EF2 = EncounterFlag.flags(EncounterFlag.VALIDATE_ONLY);
    
    private final ListModelList<String> model = new ListModelList<String>();
    
    private static final Comparator<String> PLComparator = new Comparator<String>() {
        
        @Override
        public int compare(String itm1, String itm2) {
            String str1 = StrUtil.piece(itm1, U, 2);
            String str2 = StrUtil.piece(itm2, U, 2);
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
    };
    
    public static void execute() {
        PopupDialog.popup(DIALOG, true, true, true);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        lstContra.setItemRenderer(renderer);
        loadForm();
    }
    
    private void loadForm() {
        lstContra.setModel(model);
        getContra(model);
        updateControls();
    }
    
    private void getContra(List<String> result) {
        getBroker().callRPCList("BGOVIMM GETCONT", result);
        Collections.sort(result, PLComparator);
    }
    
    private void updateControls() {
        Object o = txtVaccine.getAttribute("DATA");
        btnSave.setDisabled(model.isSelectionEmpty() || o == null || ((String) o).isEmpty());
        
    }
    
    /**
     * Returns the currently selected contraindication.
     * 
     * @return The currently selected contraindication.
     */
    private String getSelected() {
        String s = lstContra.getSelectedItem().getValue();
        return s;
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onSelect$lstContra() {
        updateControls();
    }
    
    public void onClick$btnVaccine(Event event) {
        //onOK is a key event
        boolean b = ZKUtil.getEventOrigin(event) instanceof MouseEvent;
        String val = ImmunSelectorController.execute(txtVaccine.getValue() == null ? "" : txtVaccine.getValue(), b);
        
        if (val != null) {
            txtVaccine.setAttribute("DATA", val);
            txtVaccine.setValue(StrUtil.piece(val, StrUtil.U, 2));
        } else {
            txtVaccine.setValue("");
            txtVaccine.setAttribute("DATA", null);
        }
        updateControls();
    }
    
    public void onClick$btnSave() {
        if (btnSave.isDisabled() || !validate()) {
            return;
        }
        
        try {
            String result = "";
            result = getBroker().callRPC("BGOVIMM SETCONT", toDAO());
            PCC.errorCheck(result);
            close(false);
        } catch (Exception e) {
            PromptDialog.showError(e.getMessage());
            return;
        }
    }
    
    private boolean validate() {
        Object o;
        o = txtVaccine.getAttribute("DATA");
        if (o == null) {
            PromptDialog.showWarning("You must first select the vaccine related to the contraindication.",
                "Specify Vaccine");
            return false;
        }
        
        return true;
    }
    
    /**
     * Contraindication information
     * 
     * @return BI PATIENT CONTRAINDICATIONS File IEN Set contraindication record
     */
    private String toDAO() {
        String val = txtVaccine.getAttribute("DATA").toString();
        StringBuilder sb = new StringBuilder();
        appendData(sb, PatientContext.getActivePatient().getId().getIdPart());
        appendData(sb, StrUtil.piece(val, U, 1));
        appendData(sb, StrUtil.piece(getSelected(), U, 1));
        if (EncounterUtil.ensureEncounter(EF2)) {
            appendData(sb,
                new FMDate(DateUtil.stripTime(EncounterContext.getActiveEncounter().getPeriod().getStart())).getFMDate());
        }
        return sb.toString();
    }
    
    private void appendData(StringBuilder sb, Object data) {
        sb.append(data == null ? "" : data instanceof IReferenceable ? ((IReferenceable) data).getId().getIdPart() : data)
                .append('^');
    }
}
