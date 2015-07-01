package org.carewebframework.vista.ui.familyhistory.model;

import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.resource.FamilyMemberHistory;

/**
 * Model object for family member.
 */
public class MemberModel {
    
    private final FamilyMemberHistory member;
    
    private String relationships;
    
    public MemberModel(FamilyMemberHistory member) {
        this.member = member;
    }
    
    public FamilyMemberHistory getMember() {
        return member;
    }
    
    public String getName() {
        return member.getName();
    }
    
    public String getRelationships() {
        if (relationships == null) {
            StringBuilder sb = new StringBuilder();
            
            for (CodingDt coding : member.getRelationship().getCoding()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                
                sb.append(coding.getDisplay());
            }
            
            relationships = sb.toString();
        }
        
        return relationships;
    }
    
    /* Also need:
        status (deceased/living)
        age at death (range)
        cause of death
        multiple birth
        multiple birth type
     */
}
