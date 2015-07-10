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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.carewebframework.api.query.IQueryContext;
import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.common.DateUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.mbroker.AbstractBrokerQueryService;
import org.carewebframework.vista.api.util.FileEntry;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;
import org.carewebframework.vista.mbroker.FMDate;
import org.carewebframework.vista.ui.familyhistory.model.Condition;
import org.carewebframework.vista.ui.familyhistory.model.FamilyMember;

import ca.uhn.fhir.model.dstu2.resource.Patient;

/**
 * Data service for patient goals.
 */
public class FamilyHistoryService extends AbstractBrokerQueryService<FamilyMember> {
    
    private static volatile boolean initChoices = true;
    
    private static final Object mutex = new Object();
    
    private static List<FileEntry> relationshipChoices;
    
    private static List<FileEntry> statusChoices;
    
    private static List<FileEntry> ageAtDeathChoices;
    
    private static List<FileEntry> multipleBirthChoices;
    
    private static List<FileEntry> multipleBirthTypeChoices;
    
    public FamilyHistoryService(BrokerSession brokerSession) {
        super(brokerSession, "BGOFHX GET");
    }
    
    @Override
    public boolean hasRequired(IQueryContext context) {
        return context.getParam("patient") instanceof Patient;
    }
    
    /**
     * Process raw data.
     * 
     * <pre>
     * Relationship IEN [1] Relationship [2] ^ Status [3] ^ Age at Death [4] ^ Cause of Death [5] ^
     * Multiple Birth [6] ^ Multiple Birth Type [7] ^ ICD9 [8] ^ Narrative [9] ^ [10] ^
     * Date Modified [11] ^ Description [12] ^ Family hx IEN [13] ^ Age at DX [14] ^
     * Age at DX Approximate [15] ^ Snomed CT [16] ^ Snomed Desc ID [17] ^
     * Additional ICD codes - ";" delimited [18]
     * </pre>
     */
    @Override
    protected List<FamilyMember> processData(IQueryContext context, String data) {
        initChoices();
        Map<String, FamilyMember> map = new HashMap<>();
        List<FamilyMember> results = new ArrayList<>();
        List<String> list = StrUtil.toList(data, "\r");
        
        if (list.size() == 1 && !list.get(0).contains(StrUtil.U)) {
            return results;
        }
        
        for (String line : list) {
            String[] pcs = StrUtil.split(line, StrUtil.U, 18);
            FamilyMember member = map.get(pcs[0]);
            
            if (member == null) {
                member = new FamilyMember();
                map.put(pcs[0], member);
                results.add(member);
                member.setIEN(Long.parseLong(pcs[0]));
                member.setRelationship(FileEntry.find(relationshipChoices, pcs[1]));
                member.setStatus(FileEntry.find(statusChoices, pcs[2]));
                member.setAgeAtDeath(FileEntry.find(ageAtDeathChoices, pcs[3]));
                member.setCauseOfDeath(pcs[4]);
                member.setMultipleBirth(FileEntry.find(multipleBirthChoices, pcs[5]));
                member.setMultipleBirthType(FileEntry.find(multipleBirthTypeChoices, pcs[6]));
                member.setName(pcs[11]);
            }
            
            if (!pcs[12].isEmpty()) {
                Condition condition = new Condition(member);
                member.getConditions().add(condition);
                condition.setICD9(pcs[7]);
                String[] note = pcs[8].split("\\|", 2);
                condition.setSCTText(note[0].trim());
                condition.setNote(note[1].trim());
                Date date = DateUtil.parseDate(pcs[10]);
                condition.setDateModified(date == null ? null : new FMDate(date));
                condition.setIEN(Long.parseLong(pcs[12]));
                condition.setAgeAtOnset(pcs[13].isEmpty() ? null : Integer.parseInt(pcs[13]));
                condition.setAgeApproximate(pcs[14].isEmpty() ? null : StrUtil.toBoolean(pcs[14]));
                condition.setSCTCode(pcs[15]);
                condition.setSCTDx(pcs[16]);
                condition.setICD9Other(pcs[17]);
            }
        }
        return results;
    }
    
    @Override
    protected void createArgumentList(List<Object> args, IQueryContext context) {
        args.add(((Patient) context.getParam("patient")).getId().getIdPart());
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
                    relationshipChoices = FileEntry.fromList(
                        service.callRPCList("RGUTRPC FILGET", null, "9999999.36", null, null, "I $P($G(^(21)),U)"), true);
                    statusChoices = FileEntry.fromList(service.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".04"),
                        true);
                    ageAtDeathChoices = FileEntry.fromList(service.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".05"),
                        false);
                    multipleBirthChoices = FileEntry
                            .fromList(service.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".07"), true);
                    multipleBirthTypeChoices = FileEntry
                            .fromList(service.callRPCList("RGUTRPC SETVALS", null, "9000014.1", ".08"), true);
                    initChoices = false;
                }
            }
        }
    }
    
    public void deleteMember(FamilyMember member) {
        service.callRPC("BGOFHX DEL", member.getIEN());
    }
    
    public void addEditMember(FamilyMember member) {
        doAddEdit(member, null);
    }
    
    public void deleteCondition(Condition condition) {
        String result = service.callRPC("BGOFHX DEL", StrUtil.U + condition.getIEN());
        checkResult(result);
        condition.getMember().getConditions().remove(condition);
    }
    
    public void addEditCondition(Condition condition) {
        doAddEdit(null, condition);
    }
    
    private void doAddEdit(FamilyMember member, Condition condition) {
        List<String> params = new ArrayList<>();
        
        if (condition == null) {
            params.add(VistAUtil.concatParams("REL", member.getIEN(), member.getRelationship(), member.getName(),
                member.getStatus(), member.getAgeAtDeath(), member.getCauseOfDeath(), member.getMultipleBirth(),
                member.getMultipleBirthType()));
        } else {
            String ageApproximate = condition.isAgeApproximate() == null ? "@" : condition.isAgeApproximate() ? "1" : "0";
            params.add(VistAUtil.concatParams("REL", condition.getMember().getIEN()));
            params.add(VistAUtil.concatParams("FHX", condition.getIEN(), condition.getICD9(), condition.getNote(),
                condition.getAgeAtOnset(), ageApproximate, condition.getSCTCode(), condition.getSCTDx()));
        }
        
        String result = service.callRPC("BGOFHX SET", PatientContext.getActivePatient().getId().getIdPart(), params);
        result = checkResult(result);
        
        if (member != null) {
            if (member.getIEN() == null) {
                member.setIEN(Long.parseLong(StrUtil.piece(result, ";R")));
            }
        }
        
        if (condition != null) {
            if (condition.getIEN() == null) {
                condition.setIEN(Long.parseLong(StrUtil.piece(result, ";F")));
                condition.getMember().getConditions().add(condition);
            }
            
            condition.setDateModified(FMDate.today());
        }
        
    }
    
    private String checkResult(String result) {
        String[] pcs = StrUtil.split(result, StrUtil.U, 2);
        
        if (pcs[0].startsWith("-")) {
            throw new RuntimeException(pcs[1]);
        }
        
        return pcs[0].isEmpty() ? pcs[1] : pcs[0];
    }
    
}
