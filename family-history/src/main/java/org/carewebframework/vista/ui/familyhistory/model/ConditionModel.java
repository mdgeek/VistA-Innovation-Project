package org.carewebframework.vista.ui.familyhistory.model;

import org.carewebframework.fhir.common.FhirUtil;

import ca.uhn.fhir.model.dstu2.composite.AgeDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory.Condition;
import ca.uhn.fhir.model.primitive.StringDt;

/**
 * Model object for condition.
 */
public class ConditionModel {
    
    private final Condition condition;
    
    public ConditionModel(Condition condition) {
        this.condition = condition;
    }
    
    public Condition getCondition() {
        return condition;
    }
    
    public String getNote() {
        return condition.getNote();
    }
    
    public Object getOnsetAge() {
        AgeDt age = FhirUtil.getTyped(condition.getOnset(), AgeDt.class);
        StringDt ageStr = FhirUtil.getTyped(condition.getOnset(), StringDt.class);
        return age != null ? age.getValue() : ageStr != null ? ageStr.getValue() : null;
    }
    
    public String getICD9() {
        CodingDt coding = FhirUtil.getCoding(condition.getType().getCoding(), "http://hl7.org/fhir/sid/icd-9");
        return coding == null ? null : coding.getCode();
    }
}
