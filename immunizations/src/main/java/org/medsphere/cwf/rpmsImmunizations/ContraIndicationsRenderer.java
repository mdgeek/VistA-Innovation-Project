package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.A;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

public class ContraIndicationsRenderer extends AbstractListitemRenderer<ImmunItem, Object> {
    
    @Override
    protected void renderItem(Listitem item, ImmunItem data) {
        if (data.isType('C')) {
            Listcell cell = createCell(item, null);
            A anchor = new A();
            
            if (data.isLocked()) {
                anchor.setIconSclass("glyphicon glyphicon-lock");
                anchor.setDisabled(true);
            } else {
                anchor.setIconSclass("glyphicon glyphicon-remove");
                anchor.addForward(Events.ON_CLICK, "root", "onDeleteContraindication", item);
            }
            
            cell.appendChild(anchor);
            createCell(item, data.getVaccineName());
            createCell(item, data.getReason());
            createCell(item, data.getDate());
            item.setTooltiptext(data.getVaccineName() + " - " + data.getReason() + " - " + data.getDate());
        }
    }
}
