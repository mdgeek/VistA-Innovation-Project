/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at 
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.medsphere.cwf.rpmsImmunizations;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.cal.api.encounter.EncounterContext;
import org.carewebframework.cal.api.encounter.EncounterContext.IEncounterContextEvent;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.cal.api.patient.PatientContext.IPatientContextEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.common.BgoUtil.BgoSecurity;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.rpms.api.domain.Contraindication;
import org.carewebframework.rpms.api.domain.Forecast;
import org.carewebframework.rpms.api.domain.Immunization;
import org.carewebframework.rpms.api.domain.Refusal;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ReportBox;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.mbroker.AsyncRPCCompleteEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCErrorEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCEventDispatcher;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Patient;

/**
 * This is a sample controller that extends the PluginController class which provides some
 * convenience methods for accessing framework services and automatically registers the
 * controller with the framework so that it may receive context change events (if the controller
 * implements a supported context-related interface).  This controller illustrates the use of
 * the IPluginEvent interface to receive plugin lifecycle notifications.. 
 *
 */
public class MainController extends BgoBaseController<Object> implements IPluginEvent {

    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(MainController.class);

    private static final ImmunizationsRenderer immunRenderer = new ImmunizationsRenderer();
    
    private static final ContraIndicationsRenderer contraRenderer = new ContraIndicationsRenderer();
    
    private static final ForecastRenderer forecastRenderer = new ForecastRenderer();
    
	private static enum Command {
		ADD, EDIT, DELETE, REFRESH, ADDCONTRA, DELCONTRA, CASEDATA
	}
	
	public static enum EventType {
		CURRENT, HISTORICAL, REFUSAL
	}
	
	private static final String FORECASTERROR = "#212";

	private Image imgMain;
	
	private Listbox lbImmunizations;
	
	private Listbox lbForecast;
	
	private Listbox lbContra;
	
	private Button btnAdd;
	
	private Button btnEdit;
	
	private Button btnDelete;
	
	private Button btnProfile;
	
	private Button btnCaseData;
	
	private Menuitem mnuAdd;
	
	private Menuitem mnuEdit;
	
	private Menuitem mnuDelete;
	
	private Menuitem mnuPrintRecord;
	
	private Menuitem mnuDueLetter;
	
	private Menuitem mnuProfile;
	
	private Menuitem mnuCaseData;
	
	private Menuitem mnuVisitDetail;
	
	private Menuitem mnuAddContra;
	
	private Menuitem mnuDeleteContra;
	
	private String pccEvent;
	
	private String refusalEvent;
	
	private String contraIndEvent;
	
	private boolean allowAsync;
	
	private boolean hideButtons;
	
	private boolean hideIcon;
	
	private Listheader colSort;
	
	private boolean noRefresh;
	
	private Button btnPrintRecord;
	
	private Button btnDueLetter;
	
	private Button btnAddContra;
	
	private Button btnDeleteContra;
	
	private final List<ImmunItem> immunList = new ArrayList<ImmunItem>();
	
	private final List<ImmunItem> forecastList = new ArrayList<ImmunItem>();
	
	private final List<ImmunItem> contraList = new ArrayList<ImmunItem>();
	
	private ListModelList<ImmunItem> iModel = null;
	
	private ListModelList<ImmunItem> fModel = null;
	
	private ListModelList<ImmunItem> cModel = null;
	
	private Object selectedItem;
	
	private Encounter encounter;
	
	private BgoSecurity bgoSecurity;
	
	private int forecastHandle = 0;
	
	private int immunHandle = 0;
	
	private boolean isManager = false;
	
	@Override
	public void onAsyncRPCComplete(AsyncRPCCompleteEvent event) {
		if (event.getRPCName().equals("BGOVIMM GET")) {
			if (event.getHandle() == forecastHandle) {
				forecastHandle = 0;
				loadForecast(StrUtil.toList(event.getData(), "\r"));
			} else {
				immunHandle = 0;
				loadImmunizations(StrUtil.toList(event.getData(), "\r"));
			}
		} 
	}

	@Override
	public void onAsyncRPCError(AsyncRPCErrorEvent event) {
		if (event.getHandle() == forecastHandle) {
			forecastHandle = 0;
		} else {
			immunHandle = 0;
		}
	}
	
	private final IPatientContextEvent patientContextEventHandler = new IPatientContextEvent() {
		
		@Override
		public String pending(boolean silent) {
			return null;
		}
		
		@Override
		public void committed() {
			IEventManager eventManager = EventManager.getInstance();
			
			if (pccEvent != null) {
				eventManager.unsubscribe(pccEvent,  genericEventHandler);
				eventManager.unsubscribe(refusalEvent, genericEventHandler);
				eventManager.unsubscribe(contraIndEvent, genericEventHandler);
			}
			
			Patient patient = PatientContext.getActivePatient();
			pccEvent = patient == null ? null : "PCC."
					+ patient.getId().getIdPart() + ".IMM";
			refusalEvent = patient == null ? null : "REFUSAL."
					+ patient.getId().getIdPart() + ".IMMUNIZATION";
			contraIndEvent = patient == null ? null : "CONTRAINDICATION."
					+ patient.getId().getIdPart() + ".IMMUNIZATION";
			
			if (pccEvent != null) {
				eventManager.subscribe(pccEvent, genericEventHandler);
				eventManager.subscribe(refusalEvent, genericEventHandler);
				eventManager.subscribe(contraIndEvent, genericEventHandler);
				String s = getBroker().callRPC("BGOVIMM SETREG", patient.getId().getIdPart());	
			}
			
			loadImmunizations(false);
			updateControls();
		}
		
		@Override
		public void canceled() {
		}
	};
	
	private final IEncounterContextEvent encounterContextEventHandler = new IEncounterContextEvent() {
		
		@Override
		public String pending(boolean silent) {
			return null;
		}
		
		@Override
		public void committed() {
			encounter = EncounterContext.getActiveEncounter();
			updateControls();
			lbImmunizations.setModel(iModel);
		}
		
		@Override
		public void canceled() {
		}
	};
	
	private final IGenericEvent<Object> genericEventHandler = new IGenericEvent<Object>() {
		
		@Override
		public void eventCallback(String eventName, Object eventData) {
			if (eventName.equals(pccEvent) || (eventName.equals(refusalEvent)) || (eventName.equals(contraIndEvent)) || (eventName.startsWith("REFRESH"))) {
				refresh();
			}
		}
	};
	
	/**
	 * Return whether data should be retrieved asynchronously.
	 * 
	 * @return The asynchronous setting.
	 */
	public boolean getAllowAsync() {
		return allowAsync;
	}

	/**
	 * Sets the asynchronous property.
	 * 
	 * @param value
	 *            The asynchronous setting.
	 */
	public void setAllowAsync(boolean allowAsync) {
		this.allowAsync = allowAsync;
	}

	/**
	 * Return whether plugin icon should be hidden.
	 * 
	 * @return The hide icon setting.
	 */
	public boolean getHideIcon() {
		return hideIcon;
	}

	/**
	 * Sets the hide plugin icon property.
	 * 
	 * @param value
	 *            The hide icon setting.
	 */
	public void setHideIcon(boolean hideIcon) {
		this.hideIcon = hideIcon;
		imgMain.setVisible(!hideIcon);
	}

	/**
	 * Return whether buttons should be hidden.
	 * 
	 * @return The hide buttons setting.
	 */
	public boolean getHideButtons() {
		return hideButtons;
	}

	/**
	 * Sets the hide buttons property.
	 * 
	 * @param value
	 *            The hide buttons setting.
	 */
	public void setHideButtons(boolean hideButtons) {
		this.hideButtons = hideButtons;
		btnAdd.setVisible(!hideButtons);
		btnEdit.setVisible(!hideButtons);
		btnDelete.setVisible(!hideButtons);
		btnProfile.setVisible(!hideButtons);
		btnCaseData.setVisible(!hideButtons && isManager);
		btnPrintRecord.setVisible(!hideButtons);
		btnDueLetter.setVisible(!hideButtons);
		btnAddContra.setVisible(!hideButtons);
		btnDeleteContra.setVisible(!hideButtons);
	}
	
    /**
     * @see org.carewebframework.ui.FrameworkController#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        bgoSecurity = BgoUtil.initSecurity("BGO DISABLE IMM EDITING",  "BIZ EDIT PATIENTS");
        isManager = getBroker().callRPCBool("RGCWFUSR HASKEYS", "BIZ MANAGER");
        lbImmunizations.setItemRenderer(immunRenderer);
        lbForecast.setItemRenderer(forecastRenderer);
        lbContra.setItemRenderer(contraRenderer);
        RowComparator.autowireColumnComparators(lbImmunizations.getListhead().getChildren());
        getAppFramework().registerObject(patientContextEventHandler);
        getAppFramework().registerObject(encounterContextEventHandler);
        patientContextEventHandler.committed();
        CommandUtil.associateCommand("REFRESH",  lbImmunizations);
        getEventManager().subscribe("REFRESH",  genericEventHandler);
    }

	/**
	 * @see org.carewebframework.shell.plugins.IPluginEvent#onLoad(org.carewebframework.shell.plugins.PluginContainer)
	 */
	@Override
	public void onLoad(final PluginContainer container) {
		container.registerProperties(this, "allowAsync", "hideButtons",
				"hideIcon");
	}

	/**
	 * @see org.carewebframework.shell.plugins.IPluginEvent#onUnload()
	 */
	@Override
	public void onUnload() {
		getEventManager().unsubscribe("REFRESH", genericEventHandler);
		log.trace("Plugin Unloaded");
	}

	/**
	 * @see org.carewebframework.shell.plugins.IPluginEvent#onActivate()
	 */
	@Override
	public void onActivate() {
		log.trace("Plugin Activated");
	}

	/**
	 * @see org.carewebframework.shell.plugins.IPluginEvent#onInactivate()
	 */
	@Override
	public void onInactivate() {
		log.trace("Plugin Deactivated");
	}
	
	@Override
	public void refresh() {
		if (!noRefresh) {
			saveGridState();
			loadImmunizations(true);
			restoreGridState();
		}
	}
	
	private void loadImmunizations(Boolean noAsync) {
		lbImmunizations.getItems().clear();
		lbForecast.getItems().clear();
		lbContra.getItems().clear();
		
		getAsyncDispatcher().abort();
		Patient patient = PatientContext.getActivePatient();
		
		if (patient == null) {
			return;
		}
		
		EventUtil.status("Loading Immunization Data");
		
		if (allowAsync && !noAsync) {
			immunHandle = getAsyncDispatcher().callRPCAsync("BGOVIMM GET", 
					patient.getId().getIdPart());
		} else {
			loadImmunizations(getBroker().callRPCList("BGOVIMM GET", null,
					patient.getId().getIdPart()));
		}
		
		EventUtil.status();
		
	}

	private void loadImmunizations(List<String> data){
		immunList.clear();
		forecastList.clear();
		contraList.clear();
		
		EventUtil.status("Loading Immunization Data");
		
		try {
			if (data == null || data.isEmpty()) {
				return;
			}
			
			PCC.errorCheck(data);
			
			for (String s : data) {
				
				char type = s.charAt(0);

				switch (type) {
				case 'I':
					immunList.add(new ImmunItem(s));
					break;
				case 'R':
					immunList.add(new ImmunItem(s));
					break;
				case 'C':
					contraList.add(new ImmunItem(s));
					break;
				case 'F':
					forecastList.add(new ImmunItem(s));
					if (s.contains(FORECASTERROR)) {
						updateForecast();
					}
					break;
				}
			}
		} finally {
			refreshLists();
		}
		EventUtil.status();
	}
	
	private void loadForecast(List<String> data) {
		forecastList.clear();
		EventUtil.status("Loading Forecast Data");

		try {
			if (data == null || data.isEmpty()) {
				return;
			}

			PCC.errorCheck(data);

			for (String s : data) {
				forecastList.add(new ImmunItem(s));
				if (s.contains(FORECASTERROR)) {
					updateForecast();
				}
			}
		} finally {
			fModel = new ListModelList<ImmunItem>(forecastList);
			lbForecast.setModel(fModel);
		}
		EventUtil.status();
	}
	
	private void updateControls() {
		boolean b = (PatientContext.getActivePatient() == null)
				|| !bgoSecurity.isEnabled;
				//|| !BgoUtil.checkSecurity(true);
		ImmunItem immun = getSelectedImmun();
		boolean locked = immun == null ? true : immun.isLocked();
		btnAdd.setDisabled(b);
		btnEdit.setDisabled(b || locked);
		btnDelete.setDisabled(locked);
		btnPrintRecord.setDisabled(b);
		btnDueLetter.setDisabled(b);
		btnProfile.setDisabled(b);
		btnCaseData.setVisible(isManager);
		btnCaseData.setDisabled(b);
		btnAddContra.setDisabled(b);
		btnDeleteContra.setDisabled(b || lbContra.getSelectedIndex() < 0);
		mnuAddContra.setDisabled(btnAddContra.isDisabled());
		mnuDeleteContra.setDisabled(btnDeleteContra.isDisabled());
		mnuAdd.setDisabled(btnAdd.isDisabled());
		mnuEdit.setDisabled(btnEdit.isDisabled());
		mnuDelete.setDisabled(btnDelete.isDisabled());
		mnuPrintRecord.setDisabled(btnPrintRecord.isDisabled());
		mnuDueLetter.setDisabled(btnDueLetter.isDisabled());
		mnuProfile.setDisabled(btnProfile.isDisabled());
		mnuCaseData.setVisible(btnCaseData.isVisible());
		mnuCaseData.setDisabled(b);
		mnuVisitDetail.setDisabled(immun == null || immun.getEncounter() == null);
	}

	private void refreshLists() {
		iModel = new ListModelList<ImmunItem>(immunList);
		fModel = new ListModelList<ImmunItem>(forecastList);
		cModel = new ListModelList<ImmunItem>(contraList);
		
		if (colSort == null) {
			colSort = (Listheader) lbImmunizations.getListhead().getChildren().get(0);
		}
		
		lbImmunizations.setModel(iModel);
		lbForecast.setModel(fModel);
		lbContra.setModel(cModel);
		//sortImmuns();
		updateControls();
		Events.echoEvent("onResize", lbImmunizations, null);
		Clients.resize(lbImmunizations);
	}
	
	private void saveGridState() {
		selectedItem = getSelectedImmun();
	}
	
	private void restoreGridState() {
		if (selectedItem != null) {
			lbImmunizations.setSelectedIndex(ListUtil.findListboxData(lbImmunizations, 
					selectedItem));
			selectedItem = null;
		}
	}
	
	private ImmunItem getSelectedImmun() {
		Listitem item = lbImmunizations.getSelectedItem();
		
		if (item != null) {
			lbImmunizations.renderItem(item);
			return (ImmunItem) item.getValue();
		} else {
			return null;
		}
	}
	
	private void sortImmuns() {
		if (colSort != null) {
			boolean asc = "ascending".equals(colSort.getSortDirection());
			colSort.sort(asc, true);
		}
	}
	
	public void doCommand(Command cmd) {
		if (!bgoSecurity.verifyWriteAccess(true)) {
			return;
		}
		
		switch (cmd) {
		case ADD:
				addImmunization();
			break;
		
		case EDIT:
				editImmunization();
			break;
			
		case DELETE:
				deleteImmunization();
			break;
			
		case REFRESH:
			refresh();
			break;
			
		case ADDCONTRA:
			addContraIndication();
			break;
			
		case DELCONTRA:
			deleteContraIndication();
			break;
			
		case CASEDATA:
			managePatientCaseData();
		}			
	}
	
	private void deleteContraIndication() {
		Listitem item = lbContra.getSelectedItem();
		
		if (item != null) {
			ImmunItem contra = item.getValue();
			if (contra.deleteContra()) {
				item.setSelected(false);
				item.setVisible(false);
			}
		}	
	}

	private void deleteImmunization() {
		Listitem item = lbImmunizations.getSelectedItem();

		if (item != null) {
			ImmunItem immun = item.getValue();
			if (immun.delete()) {
				item.setSelected(false);
				item.setVisible(false);
			}
		}
		
	}

	private void addImmunization() {
		AddImmunController.execute(null);
	}
	
	private void editImmunization() {
		AddImmunController.execute(getSelectedImmun());
	}

	private void addContraIndication() {
		AddImmunContraController.execute();
	}
	
	private void managePatientCaseData() {
		PatientCaseDataController.execute();
	}
	
	public void onClick$btnAddContra() {
		doCommand(Command.ADDCONTRA);
	}
	
	public void onClick$btnDeleteContra() {
		doCommand(Command.DELCONTRA);
	}
	
	public void onClick$btnCaseData() {
		doCommand(Command.CASEDATA);
	}
	
	public void onClick$btnPrintRecord() {
		 List<String> lst = getBroker().callRPCList("BGOVIMM PRINT", null, 
				 PatientContext.getActivePatient().getId().getIdPart() + StrUtil.U + "2");
		 String s = StrUtil.fromList(lst, "\r");
         ReportBox.modal(s, "Print Record", true);	
	}
	
	public void onClick$btnDueLetter() {
		 List<String> lst = getBroker().callRPCList("BGOVIMM PRINT", null, 
				 PatientContext.getActivePatient().getId().getIdPart() + StrUtil.U + "1");
		 String s = StrUtil.fromList(lst, "\r");
         ReportBox.modal(s, "Due Letter", true);
	}
	
	public void onClick$btnProfile() {
		 List<String> lst = getBroker().callRPCList("BGOVIMM PROFILE", null, 
				 PatientContext.getActivePatient().getId().getIdPart());
		 String s = StrUtil.fromList(lst, "\r");
		 if (!PCC.errorCheck(lst)) {
			 ReportBox.modal(s, "Profile", true);
		 }
	}
	
	public void onClick$imgInfo() {
		EventUtil.status("InfoButtonService notified.");
		
		Listitem item = lbImmunizations.getSelectedItem();

		if (item != null) {
			ImmunItem immun = item.getValue();
			String immName = immun.getVaccineName();
			getEventManager().fireLocalEvent("INFOBUTTON", "NAME=" + immName + "^System=INFO_BUTTON");
		} else {
			getEventManager().fireLocalEvent("INFOBUTTON",  "");
		}
		
	}
	
	public void onClick$mnuVisitDetail() {
        ImmunItem item = getSelectedImmun();
        
        if (item != null) {
        	String visitIEN = item.getVisitIEN();
            List<String> lst = getBroker().callRPCList("BGOUTL GETRPT", null, visitIEN);
            String s = StrUtil.fromList(lst, "\r");
            ReportBox.modal(s, "Visit Detail", true);
        }
	}
	
	public void onClick$btnAdd() {
		doCommand(Command.ADD);
	}
	
	public void onClick$btnEdit() {
		doCommand(Command.EDIT);
	}
	
	public void onClick$btnDelete() {
		doCommand(Command.DELETE);
	}

	public void onSelect$lbContra() {
		updateControls();
	}
	
	public void onSelect$lbImmunizations() {
		updateControls();
	}
	
	public void onClick$mnuRefresh() {
		doCommand(Command.REFRESH);
	}
	
	public void onDoubleClick$lbImmunizations() {
		if (!btnEdit.isDisabled()) {
			doCommand(Command.EDIT);
		}
	}
	
	private void updateForecast() {
		forecastHandle = getAsyncDispatcher().callRPCAsync("BGOVIMM GET", 
				PatientContext.getActivePatient().getId().getIdPart() + "^F");
	}
	
}
