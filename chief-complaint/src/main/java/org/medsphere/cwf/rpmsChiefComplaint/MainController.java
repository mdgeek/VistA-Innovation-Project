/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * 
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.medsphere.cwf.rpmsChiefComplaint;

import static org.carewebframework.common.StrUtil.U;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Patient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.carewebframework.api.context.UserContext;
import org.carewebframework.api.event.EventManager;
import org.carewebframework.api.event.EventUtil;
import org.carewebframework.api.event.IEventManager;
import org.carewebframework.api.event.IGenericEvent;
import org.carewebframework.cal.api.encounter.EncounterContext;
import org.carewebframework.cal.api.encounter.EncounterContext.IEncounterContextEvent;
import org.carewebframework.cal.api.encounter.EncounterUtil;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.cal.api.patient.PatientContext.IPatientContextEvent;
import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.BgoConstants;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.shell.plugins.IPluginEvent;
import org.carewebframework.shell.plugins.PluginContainer;
import org.carewebframework.ui.command.CommandUtil;
import org.carewebframework.ui.zk.ListUtil;
import org.carewebframework.ui.zk.PromptDialog;
import org.carewebframework.ui.zk.ReportBox;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.vista.ui.mbroker.AsyncRPCCompleteEvent;
import org.carewebframework.vista.ui.mbroker.AsyncRPCErrorEvent;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menuitem;

public class MainController extends BgoBaseController<Object> implements IPluginEvent {
    
    private static final long serialVersionUID = 1L;
    
    private static final Log log = LogFactory.getLog(MainController.class);
    
    private static final ChiefComplaintRenderer chiefRenderer = new ChiefComplaintRenderer();
    
    private static enum Command {
        ADD, EDIT, DELETE, MANAGE, REFRESH
    }
    
    private Image imgMain;
    
    private Listbox lbCC;
    
    private Button btnAdd;
    
    private Button btnEdit;
    
    private Button btnDelete;
    
    private Menuitem mnuAdd;
    
    private Menuitem mnuEdit;
    
    private Menuitem mnuDelete;
    
    private Menuitem mnuVisitDetail;
    
    private Menuitem mnuManagePickList;
    
    private Menuitem mnuRefresh;
    
    private String pccEvent;
    
    private String visitEvent;
    
    private boolean allowAsync;
    
    private boolean hideButtons;
    
    private boolean hideIcon;
    
    private Listheader colSort;
    
    private boolean noRefresh;
    
    private final List<ChiefComplaint> chiefList = new ArrayList<ChiefComplaint>();
    
    private Object selectedItem;
    
    private Encounter encounter;
    
    private boolean g_bCAC;
    
    @Override
    public void onAsyncRPCComplete(AsyncRPCCompleteEvent event) {
        loadChiefComplaints(StrUtil.toList(event.getData(), "\r"));
    }
    
    @Override
    public void onAsyncRPCError(AsyncRPCErrorEvent event) {
        //TODO: 
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
                eventManager.unsubscribe(pccEvent, genericEventHandler);
            }
            
            Patient patient = PatientContext.getActivePatient();
            pccEvent = patient == null ? null : "PCC." + patient.getId().getIdPart() + ".VST";
            
            if (pccEvent != null) {
                eventManager.subscribe(pccEvent, genericEventHandler);
            }
            
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
            IEventManager eventManager = EventManager.getInstance();
            
            if (visitEvent != null) {
                eventManager.unsubscribe(visitEvent, genericEventHandler);
            }
            
            encounter = EncounterContext.getActiveEncounter();
            visitEvent = encounter == null ? null : "VISIT." + encounter.getId().getIdPart() + ".NT";
            
            if (visitEvent != null) {
                eventManager.subscribe(visitEvent, genericEventHandler);
            }
            updateControls();
            loadChiefComplaints(false);
        }
        
        @Override
        public void canceled() {
        }
    };
    
    private final IGenericEvent<Object> genericEventHandler = new IGenericEvent<Object>() {
        
        @Override
        public void eventCallback(String eventName, Object eventData) {
            if (eventName.equals(pccEvent) || eventName.equals(visitEvent) || (eventName.startsWith("REFRESH"))) {
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
     * @param value The asynchronous setting.
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
     * @param value The hide icon setting.
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
     * @param value The hide buttons setting.
     */
    public void setHideButtons(boolean hideButtons) {
        this.hideButtons = hideButtons;
        btnAdd.setVisible(!hideButtons);
        btnEdit.setVisible(!hideButtons);
        btnDelete.setVisible(!hideButtons);
    }
    
    /**
     * @see org.carewebframework.ui.FrameworkController#doAfterCompose(org.zkoss.zk.ui.Component)
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        BgoUtil.initSecurity("BGO DISABLE CC EDITING", null);
        //g_bCAC = SecurityUtil.isGranted("BGOZ CAC");
        String s = getBroker().callRPC("BGOUTL CHKSEC", "BGOZ CAC");
        g_bCAC = StrUtil.toBoolean(StrUtil.extractIntPrefix(s));
        
        lbCC.setItemRenderer(chiefRenderer);
        
        RowComparator.autowireColumnComparators(lbCC.getListhead().getChildren());
        getAppFramework().registerObject(patientContextEventHandler);
        getAppFramework().registerObject(encounterContextEventHandler);
        patientContextEventHandler.committed();
        CommandUtil.associateCommand("REFRESH", lbCC);
        getEventManager().subscribe("REFRESH", genericEventHandler);
    }
    
    protected void updateControls() {
        ChiefComplaint complaint = getSelectedCComplaint();
        encounter = EncounterContext.getActiveEncounter();
        boolean b = (!EncounterUtil.isPrepared(encounter) || !BgoUtil.checkSecurity(true));
        boolean e = encounter == null ? true : EncounterUtil.isLocked(encounter);
        
        btnAdd.setDisabled(b);
        btnEdit.setDisabled(e || (!checkAuthor()));
        btnDelete.setDisabled(btnEdit.isDisabled());
        
        mnuAdd.setDisabled(btnAdd.isDisabled());
        ;
        mnuEdit.setDisabled(btnEdit.isDisabled());
        mnuDelete.setDisabled(btnDelete.isDisabled());
        mnuVisitDetail.setDisabled(complaint == null);
        
        mnuManagePickList.setVisible(g_bCAC);
        
    }
    
    private boolean checkAuthor() {
        //compare author of selected item to current user
        Boolean result = false;
        String user = UserContext.getActiveUser().getLogicalId();
        Listitem item = lbCC.getSelectedItem();
        
        if (item != null) {
            ChiefComplaint complaint = item.getValue();
            if (complaint.getAuthor().getId().getIdPart().equals(user)) {
                result = true;
            }
        }
        return result;
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onLoad(org.carewebframework.shell.plugins.PluginContainer)
     */
    @Override
    public void onLoad(PluginContainer container) {
        container.registerProperties(this, "allowAsync", "hideButtons", "hideIcon");
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onUnload()
     */
    @Override
    public void onUnload() {
        getEventManager().unsubscribe("REFRESH", genericEventHandler);
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onActivate()
     */
    @Override
    public void onActivate() {
    }
    
    /**
     * @see org.carewebframework.shell.plugins.IPluginEvent#onInactivate()
     */
    @Override
    public void onInactivate() {
    }
    
    @Override
    public void refresh() {
        if (!noRefresh && !(EncounterContext.getActiveEncounter() == null)) {
            saveGridState();
            loadChiefComplaints(true);
            restoreGridState();
        }
    }
    
    private void refreshList() {
        lbCC.setModel((ListModelList<?>) null);
        ListModelList<ChiefComplaint> model = new ListModelList<ChiefComplaint>(chiefList);
        
        if (colSort == null) {
            colSort = (Listheader) lbCC.getListhead().getChildren().get(0);
        }
        
        lbCC.setModel(model);
        updateControls();
        Events.echoEvent("onResize", lbCC, null);
    }
    
    private void restoreGridState() {
        if (selectedItem != null) {
            lbCC.setSelectedIndex(ListUtil.findListboxData(lbCC, selectedItem));
            selectedItem = null;
        }
    }
    
    private void saveGridState() {
        selectedItem = getSelectedCComplaint();
    }
    
    private ChiefComplaint getSelectedCComplaint() {
        Listitem item = lbCC.getSelectedItem();
        
        if (item != null) {
            lbCC.renderItem(item);
            return (ChiefComplaint) item.getValue();
        } else {
            return null;
        }
    }
    
    private void loadChiefComplaints(Boolean noAsync) {
        lbCC.getItems().clear();
        getAsyncDispatcher().abort();
        String visitIEN = EncounterContext.getActiveEncounter().getId().getIdPart();
        
        if (visitIEN == null) {
            return;
        }
        
        if (allowAsync && !noAsync) {
            getAsyncDispatcher().callRPCAsync("BGOCC GET", visitIEN);
        } else {
            loadChiefComplaints(getBroker().callRPCList("BGOCC GET", null, visitIEN));
        }
    }
    
    private void loadChiefComplaints(List<String> data) {
        String t = "";
        chiefList.clear();
        EventUtil.status("Loading Chief Complaint Data");
        
        try {
            if (data == null || data.isEmpty()) {
                return;
            }
            
            PCC.errorCheck(data);
            
            ListIterator<String> itr = data.listIterator();
            while (itr.hasNext()) {
                String s = "";
                String val = itr.next();
                String[] pcs = StrUtil.split(val, U, 3);
                
                String cc = val;
                int narrcnt = StrUtil.toInt(pcs[2]);
                for (int n = 0; n < narrcnt; n++) {
                    t = itr.next();
                    s = s + (n == 0 ? "" : StrUtil.CRLF) + t;
                }
                chiefList.add(new ChiefComplaint(cc, s));
            }
            
        } finally {
            refreshList();
        }
        EventUtil.status();
        
    }
    
    public void doCommand(Command cmd) {
        if (!BgoUtil.checkSecurity(false)) {
            return;
        }
        
        switch (cmd) {
            case ADD:
                addComplaint();
                break;
            
            case EDIT:
                editComplaint();
                break;
            
            case DELETE:
                deleteComplaint();
                break;
            
            case MANAGE:
                managePickList();
                break;
            
            case REFRESH:
                refresh();
                break;
        }
        
    }
    
    private void managePickList() {
        ManagePickListController.execute();
        
    }
    
    private void addComplaint() {
        AddComplaintController.execute(null);
    }
    
    private void editComplaint() {
        if (!(getSelectedCComplaint() == null)) {
            AddComplaintController.execute(getSelectedCComplaint());
        }
    }
    
    private void deleteComplaint() {
        Listitem item = lbCC.getSelectedItem();
        
        if (item != null) {
            ChiefComplaint complaint = item.getValue();
            
            if (PromptDialog.confirm(BgoConstants.TX_CNFM_CC_DEL, BgoConstants.TC_CNFM_CC_DEL)) {
                PCC.errorCheck(getBroker().callRPC("BGOCC DEL", complaint.getId().getIdPart()));
                
                item.setSelected(false);
                item.setVisible(false);
            }
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
    
    public void onClick$mnuManagePickList() {
        doCommand(Command.MANAGE);
    }
    
    public void onDoubleClick$lbCC() {
        if (!btnEdit.isDisabled()) {
            doCommand(Command.EDIT);
        }
    }
    
    public void onSelect$lbCC() {
        updateControls();
    }
    
    public void onClick$mnuVisitDetail() {
        Listitem item = lbCC.getSelectedItem();
        
        if (item != null) {
            lbCC.clearSelection();
            String visitIEN = EncounterContext.getActiveEncounter().getId().getIdPart();
            List<String> lst = getBroker().callRPCList("BGOUTL GETRPT", null, visitIEN);
            ReportBox.modal(lst, "Visit Detail", true);
        }
    }
    
    public void onClick$mnuRefresh() {
        doCommand(Command.REFRESH);
    }
    
}
