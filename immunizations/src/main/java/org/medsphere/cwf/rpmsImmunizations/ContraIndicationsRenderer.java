package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.zkoss.zul.Listitem;

public class ContraIndicationsRenderer extends AbstractListitemRenderer<ImmunItem, Object>{

	@Override
	protected void renderItem(Listitem item, ImmunItem data) {
		if (data.isType('C')) {
			createCell(item, data.getVaccineName());
			createCell(item, data.getReason());
			createCell(item, data.getDate());
			item.setTooltiptext(data.getVaccineName() + " - " + data.getReason() + " - " + data.getDate());
		}
	}
}
