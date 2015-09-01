package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractComboitemRenderer;
import org.zkoss.zul.Comboitem;

public class ImmunComboRenderer extends AbstractComboitemRenderer<String> {
	
	@Override
	protected void renderItem(Comboitem item, String s) {
		if (s.indexOf(StrUtil.U) > 0) {
			item.setLabel(StrUtil.piece(s, StrUtil.U,2));
			item.setValue(StrUtil.piece(s, StrUtil.U,1));
		} else {
			item.setLabel(StrUtil.piece(s, StrUtil.U));
			item.setValue(StrUtil.piece(s, StrUtil.U));
		}
	}

}
