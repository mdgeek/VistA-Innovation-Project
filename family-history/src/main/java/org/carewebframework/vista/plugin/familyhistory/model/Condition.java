package org.carewebframework.vista.plugin.familyhistory.model;

import org.carewebframework.vista.mbroker.FMDate;

/**
 * Model object for condition.
 */
public class Condition {
    
    private Long ien;
    
    private String note;
    
    private Integer ageAtOnset;
    
    private Boolean ageApproximate;
    
    private String icd9;
    
    private String icd9Other;
    
    private String sctText;
    
    private String sctCode;
    
    private String sctDx;
    
    private FMDate dateModified;
    
    private final FamilyMember member;
    
    public Long getIEN() {
        return ien;
    }
    
    public void setIEN(Long ien) {
        this.ien = ien;
    }
    
    public Condition(FamilyMember member) {
        this.member = member;
    }
    
    public FamilyMember getMember() {
        return member;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public Integer getAgeAtOnset() {
        return ageAtOnset;
    }
    
    public void setAgeAtOnset(Integer ageAtOnset) {
        this.ageAtOnset = ageAtOnset;
    }
    
    public Boolean isAgeApproximate() {
        return ageApproximate;
    }
    
    public void setAgeApproximate(Boolean ageApproximate) {
        this.ageApproximate = ageApproximate;
    }
    
    public String getICD9() {
        return icd9;
    }
    
    public void setICD9(String icd9) {
        this.icd9 = icd9;
    }
    
    public String getICD9Other() {
        return icd9Other;
    }
    
    public void setICD9Other(String icd9Other) {
        this.icd9Other = icd9Other;
    }
    
    public String getSCTCode() {
        return sctCode;
    }
    
    public void setSCTCode(String sctCode) {
        this.sctCode = sctCode;
    }
    
    public String getSCTText() {
        return sctText;
    }
    
    public void setSCTText(String sctText) {
        this.sctText = sctText;
    }
    
    public String getSCTDx() {
        return sctDx;
    }
    
    public void setSCTDx(String sctDx) {
        this.sctDx = sctDx;
    }
    
    public FMDate getDateModified() {
        return dateModified;
    }
    
    public void setDateModified(FMDate dateModified) {
        this.dateModified = dateModified;
    }
    
}
