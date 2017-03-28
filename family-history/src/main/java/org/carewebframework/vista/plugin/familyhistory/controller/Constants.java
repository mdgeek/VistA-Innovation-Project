/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.plugin.familyhistory.controller;

import org.carewebframework.ui.zk.ZKUtil;

public class Constants {
    
    public static final String RESOURCE_PATH = ZKUtil.getResourcePath(Constants.class, 1);
    
    public static final String PROPERTY_PREFIX = "BGOFH";
    
    public static final String LABEL_PREFIX = "vistaFamilyHistory";
    
    /**
     * Enforce static class.
     */
    private Constants() {
    };
}
