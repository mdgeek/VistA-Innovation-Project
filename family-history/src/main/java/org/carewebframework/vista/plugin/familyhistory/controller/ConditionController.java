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

import java.util.Date;
import java.util.List;

import org.carewebframework.api.query.DateQueryFilter.DateType;
import org.carewebframework.vista.plugin.familyhistory.model.Condition;
import org.carewebframework.vista.plugin.familyhistory.model.FamilyMember;
import org.hspconsortium.cwf.ui.reporting.controller.AbstractGridController;
import org.zkoss.zul.ListModelList;

/**
 * Controller for family member conditions..
 */
public class ConditionController extends AbstractGridController<Condition, Condition> {

    private static final long serialVersionUID = 1L;

    /**
     * Creates the controller for the detail view (step list). It uses the following settings:
     * <ul>
     * <li>Does not specify a service.</li>
     * <li>Prefix for label references:
     * {@value org.carewebframework.vista.plugin.familyhistory.controller.Constants#LABEL_PREFIX}</li>
     * <li>Prefix for property values:
     * {@value org.carewebframework.vista.plugin.familyhistory.controller.Constants#PROPERTY_PREFIX}
     * </li>
     * <li>No style sheet for printing (for now).</li>
     * <li>Does not respond to patient context changes.</li>
     * <li>Auto-wires comparators to grid columns.</li>
     * <li>Does not use a grouper.</li>
     * </ul>
     */
    public ConditionController() {
        super(null, Constants.LABEL_PREFIX, Constants.PROPERTY_PREFIX, null, false, true, null);
        setPaging(false);
    }

    /**
     * Initializes the controller:
     * <ul>
     * <li>Registers itself with the main controller.</li>
     * <li>Retrieves the step list and sets it into the model.</li>
     * </ul>
     */
    @Override
    protected void initializeController() {
        super.initializeController();
        List<Condition> conditions = ((FamilyMember) arg.get("member")).getConditions();
        setModel(toModel(conditions));
    }

    /**
     * Extracts the date of the specified type from the condition. This isn't currently used because
     * there is no filter for date ranges.
     */
    @Override
    public Date getDateByType(Condition condition, DateType dateType) {
        return condition.getDateModified();
    }

    /**
     * Converts query results to model.
     */
    @Override
    protected ListModelList<Condition> toModel(List<Condition> results) {
        return new ListModelList<>(results);
    }

}
