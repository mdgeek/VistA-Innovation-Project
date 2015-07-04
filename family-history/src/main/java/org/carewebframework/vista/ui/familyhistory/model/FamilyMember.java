package org.carewebframework.vista.ui.familyhistory.model;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.vista.api.util.FileEntry;

/**
 * Model object for family member.
 */
public class FamilyMember {
    
    private String ien;
    
    private String name;
    
    private FileEntry relationship;
    
    private FileEntry status;
    
    private String causeOfDeath;
    
    private FileEntry multipleBirth;
    
    private FileEntry multipleBirthType;
    
    private FileEntry ageAtDeath;
    
    private final List<Condition> conditions = new ArrayList<>();
    
    public String getIEN() {
        return ien;
    }
    
    public void setIEN(String ien) {
        this.ien = ien;
    }
    
    public FamilyMember() {
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FileEntry getRelationship() {
        return relationship;
    }
    
    public void setRelationship(FileEntry relationship) {
        this.relationship = relationship;
    }
    
    public FileEntry getStatus() {
        return status;
    }
    
    public void setStatus(FileEntry status) {
        this.status = status;
    }
    
    public String getCauseOfDeath() {
        return causeOfDeath;
    }
    
    public void setCauseOfDeath(String causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }
    
    public FileEntry getAgeAtDeath() {
        return ageAtDeath;
    }
    
    public void setAgeAtDeath(FileEntry ageAtDeath) {
        this.ageAtDeath = ageAtDeath;
    }
    
    public FileEntry getMultipleBirth() {
        return multipleBirth;
    }
    
    public void setMultipleBirth(FileEntry multipleBirth) {
        this.multipleBirth = multipleBirth;
    }
    
    public FileEntry getMultipleBirthType() {
        return multipleBirthType;
    }
    
    public void setMultipleBirthType(FileEntry multipleBirthType) {
        this.multipleBirthType = multipleBirthType;
    }
    
    public List<Condition> getConditions() {
        return conditions;
    }
    
}
