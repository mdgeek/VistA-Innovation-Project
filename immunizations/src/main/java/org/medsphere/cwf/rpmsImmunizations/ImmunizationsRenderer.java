package org.medsphere.cwf.rpmsImmunizations;

import org.zkoss.zul.Listitem;
import org.carewebframework.cal.api.encounter.EncounterContext;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.zk.AbstractListitemRenderer;

public class ImmunizationsRenderer extends
		AbstractListitemRenderer<ImmunItem, Object> {

	@Override
	protected void renderItem(Listitem item, ImmunItem data) {
		boolean flag = false;
		StringBuilder sb = new StringBuilder();
		if (data.isType('I') || (data.isType('R'))) {
			createCell(item, data.getVaccineName());
			createCell(item, data.getDate());
			createCell(item, data.getAge());
			createCell(item, data.getLocationName());
			createCell(item, data.getReaction());
			createCell(item, data.getVolume());
			createCell(item, StrUtil.piece(data.getInjSite(), "~", 2));
			createCell(item, data.getLot());
			createCell(item, data.getManuf());
			createCell(item, data.getVISDate());
			createCell(item, data.getAdminByName());
			createCell(item, data.getVFCElig());
			createCell(item, data.getAdminNotes());

			if (EncounterContext.getActiveEncounter() != null) {
				if (data.getVisitIEN().isEmpty()) {
					flag = DateUtil.compare(
							DateUtil.stripTime(data.getDate()),
							DateUtil.stripTime(EncounterContext
									.getActiveEncounter().getPeriod()
									.getStart())) == 0;
				} else {
					flag = data.getVisitIEN().equals(
							EncounterContext.getActiveEncounter().getId()
									.getIdPart());
				}

				if (flag) {
					item.setSclass("immunizations-list-highlight");
				}
			}
		}
	}
	

}
