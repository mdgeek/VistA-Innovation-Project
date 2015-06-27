package org.medsphere.cwf.rpmsChiefComplaint;

import static org.carewebframework.common.StrUtil.U;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.PromptDialog;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radiogroup;

public class ManagePickListController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = "~./org/medsphere/cwf/rpmsChiefComplaint/managePickLists.zul";
    
    /**
     * Renders the list of complaints.
     */
    private final AbstractListitemRenderer<String, Object> renderer = new AbstractListitemRenderer<String, Object>() {
        
        @Override
        protected void renderItem(Listitem item, String s) {
            item.setId(StrUtil.piece(s, U));
            item.setLabel(StrUtil.piece(s, U, 2));
        }
    };
    
    private Button btnDelete;
    
    private Radiogroup rgComplaints;
    
    private Listbox lstItems;
    
    //private List<String> complaints = new ArrayList<String>();
    
    private static final Comparator<String> PLComparator = new Comparator<String>() {
        
        @Override
        public int compare(String itm1, String itm2) {
            String str1 = StrUtil.piece(itm1, U, 2);
            String str2 = StrUtil.piece(itm2, U, 2);
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
        
    };
    
    private final ListModelList<String> complaints = new ListModelList<String>();
    
    public static void execute() {
        PopupDialog.popup(DIALOG, true, false, true);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        lstItems.setItemRenderer(renderer);
        loadForm();
    }
    
    /**
     * Loads form data from the current chief complaint
     */
    private void loadForm() {
        lstItems.setModel((ListModelList<?>) null);
        getComplaints(complaints);
        lstItems.setModel(complaints);
        updateControls();
    }
    
    private void updateControls() {
        btnDelete.setDisabled(lstItems.getSelectedCount() == 0); //complaints.isSelectionEmpty());
    }
    
    private void getComplaints(List<String> result) {
        getBroker().callRPCList("BGOCC GETPL", result, rgComplaints.getSelectedIndex() + 1);
        Collections.sort(result, PLComparator);
    }
    
    /**
     * Update controls when the selection changes.
     */
    public void onSelect$lstItems() {
        updateControls();
    }
    
    /**
     * Update list of complaints when type changes.
     */
    
    public void onClick$rgComplaints() {
        getComplaints(complaints);
        lstItems.invalidate();
    }
    
    public void onClick$btnDelete() {
        if (PromptDialog.confirm("Are you sure you want to delete the selected entry", "Delete Entry")) {
            delete();
        }
    }
    
    public void onClick$btnAdd() {
        if (AddPickListItemController.execute(rgComplaints.getSelectedIndex())) {
            Refresh();
        }
        ;
    }
    
    private void delete() {
        //Set<String> set = complaints.getSelection();
        Listitem itm = lstItems.getSelectedItem();
        if (itm != null) {
            String valId = itm.getId();
            if (deleteItem(valId)) {
                complaints.remove(itm);
                complaints.removeFromSelection(itm);
                itm.setSelected(false);
                itm.setVisible(false);
            }
        }
        //complaints.getSelection();
        //List<String> lst = complaints.getInnerList();
        //Iterator iter = lst.iterator();
        //while (iter.hasNext()) {
        //lst.
        //Object val = iter.next();
        //	String val = itm.getValue();
        //	if (deleteItem(val)) {
        //		complaints.remove(itm);
        //	};
        //}
        //lstItems.invalidate();
        updateControls();
    }
    
    private boolean deleteItem(String item) {
        String result = getBroker().callRPC("BGOCC DELPL", StrUtil.piece(item, U));
        PCC.errorCheck(result);
        Boolean res = result.isEmpty() ? true : false;
        return res;
    }
    
    private void Refresh() {
        loadForm();
    }
    
}
