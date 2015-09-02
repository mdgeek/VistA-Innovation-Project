package org.medsphere.cwf.rpmsImmunizations;

import static org.carewebframework.common.StrUtil.U;

import java.util.Comparator;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.RowComparator;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.api.util.VistAUtil;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class ImmunSelectorController extends BgoBaseController<String> {
    
    private static final long serialVersionUID = 1L;
    
    /*    *//**
             * Renders the list of Vaccines.
             */
    /*
     * private final AbstractListitemRenderer<String, Object> renderer = new
     * AbstractListitemRenderer<Vaccine, Object>() {
     * 
     * @Override protected void renderItem(Listitem item, Vaccine data) {
     * createCell(item, data.getvName); createCell(item, data.getvDesc());
     * createCell(item, data.getvInactive() == true ? "Yes" : "No");
     * item.addForward(Events.ON_DOUBLE_CLICK, btnSelect, Events.ON_CLICK); } };
     */
    
    private static final Comparator<String> PLComparator = new Comparator<String>() {
        
        @Override
        public int compare(String itm1, String itm2) {
            String str1 = StrUtil.piece(itm1, U, 2);
            String str2 = StrUtil.piece(itm2, U, 2);
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
    };
    
    private Button btnSelect;
    
    private Button btnSearch;
    
    private Radiogroup rgQual;
    
    private Listbox lbVaccines;
    
    private Textbox txtSearch;
    
    private String searchText;
    
    private String lookUpOpt;
    
    private Listheader colSort;
    
    private boolean forceShow;
    
    private boolean formLoading;
    
    private final ListModelList<Vaccine> model = new ListModelList<Vaccine>();
    
    private static final String DIALOG = "~./org/medsphere/cwf/rpmsImmunizations/immunSelector.zul";
    
    public static String execute(String searchVac, boolean forceShow) {
        Params args = BgoUtil.packageParams(searchVac, forceShow);
        Window dlg = PopupDialog.popup(DIALOG, args, false, false, true);
        ImmunSelectorController controller = (ImmunSelectorController) FrameworkController.getController(dlg);
        return controller.canceled() ? null : controller.result;
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        
        searchText = (String) arg.get(0);
        forceShow = (Boolean) arg.get(1);
        lbVaccines.setItemRenderer(new VaccineRenderer(btnSelect));
        lookUpOpt = getParam("Imm-LookupOpt");
        RowComparator.autowireColumnComparators(lbVaccines.getListhead().getChildren());
        loadForm();
        //auto return if query finds 1 match
        if (lbVaccines.getItemCount() == 1 && !forceShow) {
            lbVaccines.setSelectedIndex(0);
            Vaccine v = model.get(0);
            result = v.getItemData();
            close(false);
        }
    }
    
    private void loadForm() throws Exception {
        formLoading = true;
        txtSearch.setValue(searchText);
        rgQual.setSelectedIndex(lookUpOpt.equals("1") ? 1 : 2);
        
        if (colSort == null) {
            colSort = (Listheader) lbVaccines.getListhead().getChildren().get(0);
        }
        
        txtSearch.setText(searchText);
        lbVaccines.setModel(model);
        loadVaccines();
        formLoading = false;
    }
    
    private void loadVaccines() {
        getVaccines(model);
        sortVaccines();
        updateControls();
    }
    
    private void getVaccines(ListModelList<Vaccine> result) {
        String s = getRPCParam();
        List<String> lst = getBroker().callRPCList("BGOUTL DICLKUP", null, s);
        if (!PCC.errorCheck(lst)) {
            lbVaccines.getItems().clear();
            result.getInnerList().clear();
            for (String data : lst) {
                result.add(new Vaccine(data));
            }
            colSort.sort(true);
        }
        lbVaccines.invalidate();
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    /**
     * Update controls when the selection changes.
     */
    public void onSelect$lbVaccines() {
        updateControls();
    }
    
    private void updateControls() {
        boolean b = lbVaccines.getItemCount() > 0;
        if (b) {
            txtSearch.removeForward(Events.ON_OK, btnSearch, Events.ON_CLICK);
            txtSearch.addForward(Events.ON_OK, btnSelect, Events.ON_CLICK);
        } else {
            txtSearch.removeForward(Events.ON_OK, btnSelect, Events.ON_CLICK);
            txtSearch.addForward(Events.ON_OK, btnSelect, Events.ON_CLICK);
        }
        btnSelect.setDisabled(!(lbVaccines.getItemCount() > 0));
    }
    
    private String getRPCParam() {
        String result = "";
        Integer idx = rgQual.getSelectedIndex();
        String sScreen = "";
        
        if (lookUpOpt.equals("1")) {
            
            sScreen = "I $D(~AUTTIML(\"C\",Y)),($P($G(~AUTTIMM(Y,0)),U,7)'=1)";
        } else {
            switch (idx) {
                case 0:
                    sScreen = ".07'=1";
                    result = "";
                    break;
                case 1:
                    sScreen = "I $D(~AUTTIML(\"C\",Y)),($P($G(~AUTTIMM(Y,0)),U,7)'=1)";
                    result = "";
                    break;
                default:
                    sScreen = "";
                    result = "";
                    break;
            }
        }
        
        result = "9999999.14^" + StrUtil.xlate(txtSearch.getValue().toUpperCase(), U, "") + "^^^^^" + sScreen
                + "^1^.01;1.14;.07;1.01;1.03;1.05;1.07;1.09";
        return result;
    }
    
    private String getParam(String param, String def) {
        String s = getBroker().callRPC("BGOUTL GETPARM", param);
        return s == null ? def : StrUtil.xlate(s, "&", "");
    }
    
    private String getParam(String param) {
        return getParam(param, null);
    }
    
    /**
     * Process onClick event for vaccine selection
     * 
     * @capture the selected vaccine
     */
    public void onClick$btnSelect() {
        Listitem item = lbVaccines.getSelectedItem();
        if (item == null) {
            return;
        }
        
        lbVaccines.renderItem(item);
        Vaccine v = (Vaccine) item.getValue();
        result = v.getItemData();
        close(false);
    }
    
    public void onSort$lbVaccines(Event event) {
        event = ZKUtil.getEventOrigin(event);
        colSort = (Listheader) event.getTarget();
    }
    
    private void sortVaccines() {
        if (colSort != null) {
            boolean asc = "ascending".equals(colSort.getSortDirection());
            colSort.sort(asc, true);
        }
    }
    
    public void onChanging$txtSearch(InputEvent event) {
        String text = event.getValue().trim().toUpperCase();
        
        if (!text.endsWith(" ")) {
            selectListboxItem(lbVaccines, text, 3);
        }
    }
    
    private int selectListboxItem(Listbox lb, String label, Integer p) {
        int i = findListboxItem(lb, label, p);
        lb.setSelectedIndex(i);
        if (i > -1) {
            updateControls();
        }
        return i;
    }
    
    private int findListboxItem(Listbox lb, String searchVal, Integer p) {
        for (int i = 0; i < lb.getItemCount(); i++) {
            Listitem item = lb.getItemAtIndex(i);
            lb.renderItem(item);
            Vaccine value = item.getValue();
            String label = value.getName();
            
            if (label.toUpperCase().startsWith(searchVal) || checkBrands(value.getBrands(), searchVal)) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean checkBrands(String brands, String searchVal) {
        String[] pcs = StrUtil.split(brands, U);
        if (!brands.equals("^^^^")) {
            for (String s : pcs) {
                if (s.toUpperCase().startsWith(searchVal)) {
                    return true;
                }
            }
        }
        return false;
        
    }
    
    public void onClick$rgQual() {
        if (!formLoading) {
            setParam("Imm-LookupOpt", rgQual.getSelectedIndex());
        }
        loadVaccines();
    }
    
    public void onClick$btnSearch() {
        loadVaccines();
    }
    
    private void setParam(String param, Object value) {
        VistAUtil.getBrokerSession().callRPC("BGOUTL SETPARM", param + U + value);
    }
    
}
