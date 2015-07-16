/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.controller;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.common.StrUtil;
import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.ui.zk.ZKUtil;
import org.carewebframework.vista.ui.patientgoals.model.Goal;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.DeleteReason;
import org.carewebframework.vista.ui.patientgoals.model.GoalBase.GoalStatus;
import org.carewebframework.vista.ui.patientgoals.model.Step;
import org.carewebframework.vista.ui.patientgoals.service.GoalService;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Controller for deleting goals and steps
 */
public class DeleteController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PATH + "deletePrompt.zul";
    
    private final GoalService service;
    
    private GoalBase goalBase;
    
    // Start of auto-wired members
    
    private Radiogroup rgReason;
    
    private Textbox txtOther;
    
    private Button btnOK;
    
    // End of auto-wired members;
    
    /**
     * Generates the deletion prompt for a goal or step.
     * 
     * @param goalBase The step or goal.
     * @return Returns true if the operation was successful.
     */
    public static boolean execute(GoalBase goalBase) {
        Map<Object, Object> args = new HashMap<>();
        args.put("goalBase", goalBase);
        Component dlg = PopupDialog.popup(DIALOG, args, false, false, true);
        return dlg.hasAttribute("ok");
    }
    
    /**
     * Creates the controller with the goal service to use.
     * 
     * @param service The goal service.
     */
    public DeleteController(GoalService service) {
        this.service = service;
    }
    
    @Override
    public void doBeforeComposeChildren(Component comp) throws Exception {
        super.doBeforeComposeChildren(comp);
        arg = Executions.getCurrent().getArg();
        goalBase = (GoalBase) arg.get("goalBase");
    }
    
    /**
     * Populates the goal types and initializes the input elements with values from the step or
     * goal. Also, wires change events for all input elements.
     */
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        ((Window) comp).setTitle(getLabel("title", goalBase == null ? "" : goalBase.getName()));
        populateDeleteReason();
        ZKUtil.wireChangeEvents(comp, comp, Events.ON_CHANGE);
        txtOther.setFocus(true);
    }
    
    /**
     * Perform the delete operation and close the form.
     */
    public void onClick$btnOK() {
        doDelete();
        close(false);
    }
    
    /**
     * Close the form without further actions.
     */
    public void onClick$btnCancel() {
        close(true);
    }
    
    /**
     * Update form state when input element state changes.
     * 
     * @param event The forwarded change event.
     */
    public void onChange(Event event) {
        event = ZKUtil.getEventOrigin(event);
        String other;
        
        if (event instanceof InputEvent && event.getTarget() == txtOther) {
            other = ((InputEvent) event).getValue();
        } else {
            other = txtOther.getValue();
        }
        
        DeleteReason reason = getDeleteReason();
        boolean isOther = reason == DeleteReason.O;
        ZKUtil.updateStyle((HtmlBasedComponent) txtOther.getParent(), "visibility", isOther ? null : "hidden");
        txtOther.setFocus(isOther);
        boolean hasRequired = reason != null && (!isOther || !other.trim().isEmpty());
        btnOK.setDisabled(!hasRequired);
    }
    
    /**
     * Returns the label base on whether a step or a goal. Used by zul page to select appropriate
     * labels.
     * 
     * @param key The label key
     * @param args Optional arguments
     * @return The label text.
     */
    public String getLabel(String key, Object... args) {
        String type = goalBase instanceof Step ? "step" : "goal";
        return StrUtil.getLabel("vistaPatientGoals.delete." + key + ".label." + type, args);
    }
    
    /**
     * Populates radio group of delete reasons.
     */
    private void populateDeleteReason() {
        for (DeleteReason deleteReason : DeleteReason.values()) {
            Radio radio = new Radio(getLabel("reason." + deleteReason.name()));
            radio.setValue(deleteReason);
            rgReason.appendChild(radio);
            radio.setSelected(deleteReason == DeleteReason.O);
        }
    }
    
    private DeleteReason getDeleteReason() {
        Radio radio = rgReason.getSelectedItem();
        return radio == null ? null : (DeleteReason) radio.getValue();
    }
    
    private void doDelete() {
        DeleteReason reason = getDeleteReason();
        goalBase.setStatus(GoalStatus.D);
        goalBase.setDeleteReason(reason);
        goalBase.setDeleteText(reason == DeleteReason.O ? txtOther.getText() : "");
        
        if (goalBase instanceof Step) {
            service.deleteStep((Step) goalBase);
        } else {
            service.deleteGoal((Goal) goalBase);
        }
    }
    
    private void close(boolean cancel) {
        if (!cancel) {
            root.setAttribute("ok", true);
        }
        
        root.detach();
    }
}
