package org.medsphere.cwf.rpmsImmunizations;

import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listitem;

public class VaccineRenderer extends AbstractListitemRenderer<Vaccine, Object> {
    
    private final Component doubleClickTarget;
    
    VaccineRenderer(Component doubleClickTarget) {
        this.doubleClickTarget = doubleClickTarget;
    }
    
    @Override
    protected void renderItem(Listitem item, Vaccine data) {
        createCell(item, data.getName());
        createCell(item, data.getDesc());
        createCell(item, data.isInactive() == true ? "Yes" : "No");
        //item.addForward(Events.ON_DOUBLE_CLICK, btnSelect, Events.ON_CLICK);
        item.addForward(Events.ON_DOUBLE_CLICK, doubleClickTarget, Events.ON_CLICK);
    }
}
