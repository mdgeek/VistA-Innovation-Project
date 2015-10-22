package org.medsphere.cwf.rpmsImmunizations;

import static org.carewebframework.common.StrUtil.U;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.carewebframework.cal.api.patient.PatientContext;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.api.util.VistAUtil;
import org.carewebframework.vista.mbroker.BrokerSession;

import ca.uhn.fhir.model.dstu2.resource.Patient;

public class ImmunService {
    
    private static final Comparator<String> PLComparator = new Comparator<String>() {
        
        @Override
        public int compare(String itm1, String itm2) {
            String str1 = StrUtil.piece(itm1, U, 2);
            String str2 = StrUtil.piece(itm2, U, 2);
            return str1.toString().compareToIgnoreCase(str2.toString());
        }
    };
    
    private BrokerSession broker;
    
    private Boolean isRPMS;
    
    public boolean isRPMS() {
        if (isRPMS == null) {
            isRPMS = StrUtil.toBoolean(broker.callRPC("BGOVIMM RPMS"));
        }
        
        return isRPMS;
    }
    
    public List<String> dicLookup(List<String> result, String data) {
        return broker.callRPCList("BGOUTL DICLKUP", result, data);
    }
    
    public List<String> getVisitDetail(List<String> result, String visitIEN) {
        return broker.callRPCList("BGOUTL GETRPT", result, visitIEN);
    }
    
    public String getParam(String param, String def) {
        String s = broker.callRPC("BGOUTL GETPARM", param);
        s = StringUtils.isEmpty(s) ? def : s;
        return s.replace("&", "");
    }
    
    public void setRegistry(Patient patient) {
        broker.callRPC("BGOVIMM SETREG", patient.getId().getIdPart());
    }
    
    public void setParam(String param, Object value) {
        broker.callRPC("BGOUTL SETPARM", param + U + value);
    }
    
    public List<String> getContra(List<String> result) {
        result = broker.callRPCList("BGOVIMM GETCONT", result);
        Collections.sort(result, PLComparator);
        return result;
    }
    
    public String setContra(String data) {
        return broker.callRPC("BGOVIMM SETCONT", data);
    }
    
    public List<String> getImm(List<String> result, Patient patient) {
        return broker.callRPCList("BGOVIMM GET", result, patient.getId().getIdPart());
    }
    
    public String setImm(String data) {
        return broker.callRPC("BGOVIMM SET", data);
    }
    
    public String setRef(String data) {
        return broker.callRPC("BGOREF SET", data);
    }
    
    public String getVFCData(Patient patient) {
        return broker.callRPC("BGOVIMM GETVFC", VistAUtil.parseIEN(patient));
    }
    
    public List<String> getReasons(List<String> result) {
        result = broker.callRPCList("BGOREF GETREA", result, "IMMUNIZATION");
        Collections.sort(result, PLComparator);
        return result;
    }
    
    public List<String> getReactions(List<String> result) {
        result = broker.callRPCList("BGOUTL DICLKUP", result, "9002084.8^^^^^^.03=1^^.01");
        Collections.sort(result, PLComparator);
        result.add(0, "None");
        return result;
    }
    
    private List<String> init(List<String> result) {
        if (result == null) {
            result = new ArrayList<>();
        } else {
            result.clear();
        }
        return result;
    }
    
    public List<String> getDocument(List<String> result, Patient patient, String type) {
        return broker.callRPCList("BGOVIMM PRINT", result, patient.getId().getIdPart() + StrUtil.U + type);
    }
    
    public List<String> getOverrides(List<String> result) {
        result = init(result);
        result.add("0^No Override");
        result.add("1^Invalid - Bad Storage");
        result.add("2^Invalid - Defective");
        result.add("3^Invalid - Expired");
        result.add("4^Invalid - Admin Error");
        result.add("9^Forced Valid");
        return result;
    }
    
    public List<String> getLot(List<String> result, String vaccineId) {
        result = broker.callRPCList("BGOVIMM LOT", result, vaccineId);
        
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i);
            String[] pcs = StrUtil.split(s, U, 4);
            pcs[1] = pcs[1] + (pcs[2].isEmpty() ? "" : "  " + pcs[2]);
            pcs[1] = pcs[1] + (pcs[3].isEmpty() ? "" : "  (exp " + pcs[3] + ")");
            result.set(i, VistAUtil.concatParams(pcs[0], pcs[1], pcs[2], pcs[3]));
        }
        result.add(0, "(Lot Not Specified)");
        return result;
    }
    
    public List<String> getVacSites(List<String> result) {
        result = init(result);
        result.add("Left Thigh IM");
        result.add("Left Thigh SQ");
        result.add("Right Thigh IM");
        result.add("Right Thigh SQ");
        result.add("Both Thighs IM");
        result.add("Left Deltoid IM");
        result.add("Left Arm SQ");
        result.add("Right Deltoid IM");
        result.add("Right Arm SQ");
        result.add("Oral");
        result.add("Intranasal");
        result.add("Left Arm Intradermal");
        result.add("Right Arm Intradermal");
        return result;
    }
    
    public List<String> getVacDefaults(List<String> result, Patient patient, String vaccineId) {
        return broker.callRPCList("BGOVIMM LOADIMM", result, VistAUtil.parseIEN(patient) + U + vaccineId);
    }
    
    public List<String> getVacEligibilty(List<String> result) {
        result = broker.callRPCList("BGOVIMM2 GETELIG", result, VistAUtil.parseIEN(PatientContext.getActivePatient()));
        
        for (int i = 0; i < result.size(); i++) {
            String s = result.get(i);
            String[] pcs = StrUtil.split(s, U, 4);
            result.set(i, pcs[0] + U + pcs[2] + U + pcs[1] + U + pcs[3]);
        }
        Collections.sort(result, PLComparator);
        result.add(0, "(None Selected)");
        return result;
    }
    
    public void setBrokerSession(BrokerSession broker) {
        this.broker = broker;
    }
}
