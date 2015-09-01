package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.ui.zk.AbstractListitemRenderer;
import org.zkoss.zul.Listitem;

public class ForecastRenderer extends AbstractListitemRenderer<ImmunItem, Object>{
	
	@Override
	protected void renderItem(Listitem item, ImmunItem data) {
		if (data.isType('F')) {
			createCell(item, data.getVaccineName());
			createCell(item, data.getStatus());	
		}
		item.setTooltiptext(data.getVaccineName() + " - " + data.getStatus());
	}

}
