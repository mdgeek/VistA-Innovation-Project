/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.familyhistory.controller;

import java.util.HashMap;
import java.util.Map;

import org.carewebframework.ui.FrameworkController;
import org.carewebframework.ui.zk.PopupDialog;
import org.carewebframework.vista.ui.familyhistory.model.Condition;
import org.carewebframework.vista.ui.familyhistory.service.FamilyHistoryService;

import org.zkoss.zul.Window;

/**
 * Controller for adding new conditions.
 */
public class AddEditConditionController extends FrameworkController {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DIALOG = Constants.RESOURCE_PATH + "addEditCondition.zul";
    
    public static Condition execute(Condition condition, FamilyHistoryService service) {
        Map<Object, Object> args = new HashMap<>();
        args.put("condition", condition);
        args.put("service", service);
        Window dlg = PopupDialog.popup(DIALOG, args, true, true, true);
        AddEditConditionController controller = (AddEditConditionController) FrameworkController.getController(dlg);
        return condition; //controller == null || controller.cancelled ? null : controller.member;
    }
    
}
