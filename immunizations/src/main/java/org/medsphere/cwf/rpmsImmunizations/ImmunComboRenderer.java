package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractComboitemRenderer;

import org.zkoss.zul.Comboitem;

/**
 * Renders a combo item where data is either a single value or an entry in the form of internal
 * <code>value^external value</code>.
 */
public class ImmunComboRenderer extends AbstractComboitemRenderer<String> {
    
    @Override
    protected void renderItem(Comboitem item, String data) {
        String value = StrUtil.piece(data, StrUtil.U);
        String label = StrUtil.piece(data, StrUtil.U, 2);
        item.setValue(value);
        item.setLabel(label.isEmpty() ? value : label);
    }
    
}
