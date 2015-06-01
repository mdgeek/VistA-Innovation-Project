/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.model;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Model object for a goal type.
 */
public class GoalType implements Comparable<GoalType> {
    
    private final String name;
    
    private final String formattedName;
    
    private final int ien;
    
    public GoalType(String data) {
        String[] pcs = data.split("\\^", 2);
        ien = Integer.parseInt(pcs[0]);
        name = pcs[1];
        formattedName = WordUtils.capitalizeFully(name);
    }
    
    public String getName() {
        return name;
    }
    
    public int getIEN() {
        return ien;
    }
    
    @Override
    public String toString() {
        return formattedName;
    }
    
    @Override
    public int compareTo(GoalType goalType) {
        return name.compareToIgnoreCase(goalType.name);
    }
    
}
