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

import org.carewebframework.api.query.AbstractQueryService;
import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.api.query.IQueryResult;
import org.carewebframework.api.query.QueryUtil;
import org.carewebframework.fhir.common.FhirUtil;
import org.carewebframework.vista.api.util.FileEntry;
import org.carewebframework.vista.mbroker.BrokerSession;

import ca.uhn.fhir.model.api.Bundle;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.rest.client.GenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;

/**
 * Data service for patient goals.
 */
public class FamilyHistoryService extends AbstractQueryService<FamilyMemberHistory> {
    
    private static volatile boolean initChoices = true;
    
    private static final Object mutex = new Object();
    
    private static List<FileEntry> relationshipChoices;
    
    private static List<FileEntry> statusChoices;
    
    private static List<FileEntry> ageAtDeathChoices;
    
    private static List<FileEntry> multipleBirthChoices;
    
    private static List<FileEntry> multipleBirthTypeChoices;
    
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
    
    public List<FileEntry> getStatusChoices() {
        initChoices();
        return statusChoices;
    }
    
    public List<FileEntry> getRelationshipChoices() {
        initChoices();
        return relationshipChoices;
    }
    
    public List<FileEntry> getAgeAtDeathChoices() {
        initChoices();
        return ageAtDeathChoices;
    }
    
    public List<FileEntry> getMultipleBirthChoices() {
        initChoices();
        return multipleBirthChoices;
    }
    
    public List<FileEntry> getMultipleBirthTypeChoices() {
        initChoices();
        return multipleBirthTypeChoices;
    }
    
    private void initChoices() {
        if (initChoices) {
            synchronized (mutex) {
                if (initChoices) {
                    relationshipChoices = FileEntry.fromList(brokerSession.callRPCList("RGUTRPC FILENT", null, "9999999.36"),
                        true);
                    statusChoices = FileEntry.fromList(brokerSession.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".04"),
                        true);
                    ageAtDeathChoices = FileEntry.fromList(brokerSession.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".05"),
                        true);
                    multipleBirthChoices = FileEntry
                            .fromList(brokerSession.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".07"), true);
                    multipleBirthTypeChoices = FileEntry
                            .fromList(brokerSession.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".08"), true);
                    initChoices = false;
                }
            }
        }
    }
}
