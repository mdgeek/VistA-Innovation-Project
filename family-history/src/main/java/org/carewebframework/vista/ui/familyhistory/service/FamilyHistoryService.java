/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.familyhistory.service;

import java.util.List;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.GenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;

import org.carewebframework.api.query.AbstractQueryService;
import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.api.query.IQueryResult;
import org.carewebframework.api.query.QueryUtil;
import org.carewebframework.fhir.common.FhirUtil;
import org.carewebframework.vista.mbroker.BrokerSession;

/**
 * Data service for patient goals.
 */
public class FamilyHistoryService extends AbstractQueryService<FamilyMemberHistory> {
    
    private final GenericClient fhirClient;
    
    private final BrokerSession brokerSession;
    
    public FamilyHistoryService(GenericClient fhirClient, BrokerSession brokerSession) {
        this.fhirClient = fhirClient;
        this.brokerSession = brokerSession;
    }
    
    @Override
    public boolean hasRequired(IQueryContext context) {
        return context.getParam("patient") instanceof Patient;
    }
    
    @Override
    public IQueryResult<FamilyMemberHistory> fetch(IQueryContext context) {
        Patient patient = (Patient) context.getParam("patient");
        IQuery query = fhirClient.search().forResource(FamilyMemberHistory.class)
                .where(FamilyMemberHistory.PATIENT.hasId(patient.getId().getIdPart()));
        Bundle bundle = query.execute();
        List<FamilyMemberHistory> list = FhirUtil.getEntries(bundle, FamilyMemberHistory.class);
        return QueryUtil.packageResult(list);
    }
    
}
