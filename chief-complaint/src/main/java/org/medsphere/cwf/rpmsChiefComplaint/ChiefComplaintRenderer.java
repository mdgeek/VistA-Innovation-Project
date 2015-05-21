package org.medsphere.cwf.rpmsChiefComplaint;

import org.carewebframework.ui.zk.AbstractListitemRenderer;

import org.zkoss.zul.Listitem;

public class ChiefComplaintRenderer extends AbstractListitemRenderer<ChiefComplaint, Object> {
    
    @Override
    protected void renderItem(Listitem item, ChiefComplaint data) {
        createCell(item, data.getAuthorName());
        createCell(item, data.getNarrativeFormatted());
    }
    
}
