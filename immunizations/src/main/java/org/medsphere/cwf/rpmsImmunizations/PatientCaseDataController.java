package org.medsphere.cwf.rpmsImmunizations;

import static org.carewebframework.common.StrUtil.U;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.fhir.common.IReferenceable;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupController;
import org.carewebframework.rpms.ui.terminology.general.controller.LookupParams.Table;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.mbroker.FMDate;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.*;

public class PatientCaseDataController extends BgoBaseController<Object>{
	
	private static final long serialVersionUID = 1L;
	
	 private static final String DIALOG = "~./org/medsphere/cwf/rpmsImmunizations/managePatientCaseData.zul";
	 
	 private Button btnSave;
	 
	 private Button btnCancel;
	 
	 private Button btnManager;
	 
	 private Textbox txtManager;
	 
	 private Textbox txtParent;
	 
	 private Textbox txtOther;
	 
	 private Label lblInactiveDate;
	 
	 private Datebox datInactiveDate;
	 
	 private Button btnInactiveDate;
	 
	 private Label lblInactiveReason;
	
	 private Combobox cboInactiveReason;
	 
	 private Combobox cboForecast;
	 
	 private Combobox cboMother;
	 
	 private Label lblInactiveMoved;
	 
	 private Textbox txtInactiveMoved;
	 
	 private Radiogroup rgStatus;
	 
	 private Groupbox gbInactive;
	 
	 private boolean formLoading = false;
	 
	 private List<String> forecast = new ArrayList<String>();
	
	 private List<String> mother = new ArrayList<String>();
	
	 private List<String> inactReasons = new ArrayList<String>();
	 
	 private List<String> caseData = new ArrayList<String>();
	 
	 private final ComboitemRenderer<String> comboRenderer = new ImmunComboRenderer();


		public static void execute() {
			Window dlg = PopupDialog.popup(DIALOG, true, true, true);
			PatientCaseDataController controller = (PatientCaseDataController) FrameworkController.getController(dlg);
			return;
		}
		
		@Override
		public void doAfterCompose(Component comp) throws Exception {
			super.doAfterCompose(comp);
			loadForm();
		}

		private void loadForm() {
			// TODO Auto-generated method stub
			// BGOVIMM GETCASE", PATIENT HANDLE
			formLoading = true;
			getInactReasons(inactReasons);
			getForecast(forecast);
			getMother(mother);
			updateComboValues();
			getCaseData();
			updateControls();
			formLoading = false;
		}

		private void getCaseData() {
			getBroker().callRPCList("BGOVIMM GETCASE", caseData,
					PatientContext.getActivePatient().getId().getIdPart());
			if (caseData.size() > 0 && !PCC.errorCheck(caseData)) {
				String val = packList(caseData, U);
				String [] pcs = StrUtil.split(val, U, 16);
				txtManager.setText(pcs[4]);
				txtManager.setAttribute("ID", pcs[5]);
				txtParent.setText(pcs[2]);
				txtOther.setText(pcs[10]);
				selectItem(cboForecast, pcs[12]);
				selectItem(cboMother, pcs[6]);
				rgStatus.setSelectedIndex(pcs[1].isEmpty() ? 0 : 1);
				if (!pcs[1].isEmpty()) {
					txtInactiveMoved.setText(pcs[8]);
					datInactiveDate.setValue(new FMDate(pcs[1]));
					selectItem(cboInactiveReason, pcs[14]);
				} else {
					rgStatus.setSelectedIndex(0);
				}
			}
		}
		
		private String packList(Iterable<?> list, String delimiter) {
			StringBuilder sb = new StringBuilder();
			boolean flg = false;
			for (Object ln : list) {
				ln = ln == null ? "" : ln;
				
				if (ln != null) {
					if (flg) {
						sb.append(delimiter);
					} else {
						flg = true;
					}
					sb.append(ln);
				}
			}
			return sb.toString();
		}

		private void updateComboValues() {
			loadComboValues(cboInactiveReason, inactReasons, comboRenderer);
			loadComboValues(cboForecast, forecast, comboRenderer);
			loadComboValues(cboMother, mother, comboRenderer);	
		}
		
		private void loadComboValues(Combobox cbo, List<String> lst, ComboitemRenderer renderer) {
			cbo.setItemRenderer(renderer);
			cbo.setModel(new ListModelList<String>(lst));;
			Events.sendEvent(cbo, new Event("onInitRender", cbo, "0"));
		}

		private void getForecast(List<String> result) {
			result.clear();
			result.add("0^Normal");
			result.add("1^Influenza");
			result.add("2^Pneumococcal");
			result.add("3^Both, Influ & Pneumo");
			result.add("4^Disregard Risk Factors");
		}

		private void getMother(List<String> result) {
			result.clear();
			result.add("P^Positive");
			result.add("N^Negative");
			result.add("U^Unknown");
		}

		private void getInactReasons(List<String> result) {
			result.clear();
			result.add("m^Moved Elsewhere");
			result.add("t^Treatment Elsewhere");
			result.add("d^Deceased");
			result.add("p^Previously Inactivated");
			result.add("n^Never Activated");
			result.add("i^Ineligible, non-Ben");	
		}

		private void updateControls() {
			// TODO Auto-generated method stub
			updateInactiveControls();
		}
		
	    private void selectItem(Combobox cbo, String label) {
	        if (label != null) {
	            ListUtil.selectComboboxItem(cbo, label);
	        }
	    }
	 
		public void onClick$btnCancel() {
			close(true);
		}
		
		public void onClick$btnSave() {
			if (!validate()) {
				return;
			}
			
			try {
				String result = "";
				result = getBroker().callRPC("BGOVIMM SETREG", toDAO());
				PCC.errorCheck(result);
				close(false);
			} catch (Exception e) {
				PromptDialog.showError(e.getMessage());
				return;
			}
		}
		
		public void onClick$btnManager() {
			String val = LookupController.execute(Table.rtProvider,
					txtManager.getValue(), false, "I $D(~BIMGR(Y)),$$ACTPRV(Y)");
			if (val != null) {
				txtManager.setAttribute("ID", StrUtil.piece(val, StrUtil.U));
				txtManager.setText(StrUtil.piece(val, StrUtil.U, 3));
			}
		}
		
		public void onClick$rgStatus() {
			updateInactiveControls();
		}
	
		private void updateInactiveControls() {
			boolean b = rgStatus.getSelectedIndex() == 0;
			if (b) {
				txtInactiveMoved.setText("");
				cboInactiveReason.setSelectedIndex(-1);
				cboInactiveReason.setText("");
				datInactiveDate.setText("");;
			} else {
				if (!formLoading) {
					datInactiveDate.setValue(DateUtil.today());
				}
			}
			ZKUtil.disableChildren(gbInactive, b);
		}

		private Object toDAO() {
			StringBuilder sb = new StringBuilder();
			appendData(sb, PatientContext.getActivePatient().getId().getIdPart());
			appendData(sb, txtManager.getAttribute("ID"));
			appendData(sb, txtParent.getText());
			appendData(sb, txtOther.getText());
			appendData(sb, rgStatus.getSelectedIndex() > 0 ? "1" : "0");
			appendData(sb, new FMDate(datInactiveDate.getValue()).getFMDate());
			appendData(sb, cboInactiveReason.getSelectedIndex() > -1 ? StringUtils.left(cboInactiveReason.getText().toLowerCase(),1) : "");
			appendData(sb, txtInactiveMoved.getText());
			appendData(sb, cboForecast.getSelectedIndex() > -1 ? Integer.toString(cboForecast.getSelectedIndex()): "");
			appendData(sb, cboMother.getSelectedIndex() > -1 ? StringUtils.left(cboMother.getText().toUpperCase(), 1) : "");
			appendData(sb, StrUtil.piece(cboMother.getValue(),U));
			return sb.toString();
		}
		
	    private void appendData(StringBuilder sb, Object data) {
	        sb.append(data == null ? "" : data instanceof IReferenceable ? ((IReferenceable) data).getId().getIdPart() : data)
	                .append('^');
	    }

		private boolean validate() {
			// TODO Auto-generated method stub
			return true;
		}
	 
}
