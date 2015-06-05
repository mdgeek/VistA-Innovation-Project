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

import org.carewebframework.ui.zk.ZKUtil;

public class Constants {
    
    public static final String RESOURCE_PATH = ZKUtil.getResourcePath(Constants.class, 1);
    
    public static final String PROPERTY_PREFIX = "BEHOPG";
    
    public static final String LABEL_PREFIX = "vistaPatientGoals";
    
    public static final String[] GROUP_SCLASS = { "alert-success", "alert-danger", "alert-warning" };
    
    public static final String[] LABEL_SCLASS = { "text-success", "text-danger", "text-warning" };
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
}
