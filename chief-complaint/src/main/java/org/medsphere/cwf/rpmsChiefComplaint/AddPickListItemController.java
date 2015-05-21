package org.medsphere.cwf.rpmsChiefComplaint;

import static org.carewebframework.common.StrUtil.U;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.rpms.api.common.BgoUtil;
import org.carewebframework.rpms.api.common.Params;
import org.carewebframework.rpms.ui.common.BgoBaseController;
import org.carewebframework.rpms.ui.common.PCC;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class AddPickListItemController extends BgoBaseController<Object> {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = "~./org/medsphere/cwf/rpmsChiefComplaint/addPickListSymptom.zul";
    
    private Button btnAdd;
    
    private Button btnCancel;
    
    private Window winSymWin;
    
    private Integer typeOfComplaint;
    
    private Textbox txtValue;
    
    private Checkbox ckBody;
    
    private final List<String> complaints = new ArrayList<String>();
    
    public static Boolean execute(Integer idx) {
        Params args = BgoUtil.packageParams(idx);
        Window dlg = PopupDialog.popup(DIALOG, args, false, false, false);
        dlg.setTitle(caption(idx));
        dlg.doModal();
        AddPickListItemController controller = (AddPickListItemController) FrameworkController.getController(dlg);
        return (Boolean) (controller.canceled() ? false : controller.result);
    }
    
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Params params = (Params) arg;
        typeOfComplaint = (Integer) arg.get(0);
        initForm();
    }
    
    /**
     * Loads form data from the current chief complaint
     */
    private void initForm() {
        ckBody.setVisible(typeOfComplaint == 0);
    }
    
    private static String caption(Integer idx) {
        String caption = "  ";
        switch (idx) {
            case 0:
                caption = "Symptom";
                break;
            case 1:
                caption = "Diagnosis";
                break;
            case 2:
                caption = "Request";
        }
        return "Add " + caption;
    }
    
    public void onClick$btnAdd() {
        String res = getBroker().callRPC(
            "BGOCC SETPL",
            StrUtil.xlate(txtValue.getValue(), U, "") + U + (typeOfComplaint + 1) + U
                    + (ckBody.isVisible() ? boolToInt(ckBody.isChecked()) : ""));
        result = !PCC.errorCheck(res);
        close(false);
    }
    
    public void onClick$btnCancel() {
        close(true);
    }
    
    public void onChange$txtValue() {
        btnAdd.setDisabled(txtValue.getValue().isEmpty());
    }
    
    private Integer boolToInt(Boolean val) {
        return val ? 1 : 0;
    }
    
}
