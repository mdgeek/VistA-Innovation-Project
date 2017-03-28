/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.plugin.patientgoals.model;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.common.NumUtil;
import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.FMDate;

/**
 * Base class for goals and steps.
 */
public class GoalBase implements Comparable<GoalBase> {
    
    public enum GoalGroup {
        ACTIVE, INACTIVE, DECLINED
    }
    
    public enum GoalStatus {
        A, // ACTIVE
        ME, // MET
        S, // STOPPED
        MA, // MAINTAINING
        D; // DELETED/ENTERED IN ERROR
        
        public boolean isInactive() {
            return "SME".contains(name());
        }
        
        @Override
        public String toString() {
            return StrUtil.getLabel("vistaPatientGoals.status." + name() + ".label");
        }
    }
    
    public enum DeleteReason {
        W, // WRONG PATIENT/GOAL
        D, // DUPLICATE
        O; // OTHER
    }
    
    private String ien;
    
    private String name;
    
    private FMDate createdDate;
    
    private String createdBy;
    
    private FMDate lastUpdated;
    
    private String updatedBy;
    
    private String provider;
    
    private FMDate startDate;
    
    private FMDate followupDate;
    
    private GoalStatus status;
    
    private float number;
    
    private String reason;
    
    private String facility;
    
    private DeleteReason deleteReason;
    
    private String deleteText;
    
    private final List<GoalType> types = new ArrayList<>();
    
    @Override
    public int compareTo(GoalBase goalBase) {
        return Float.compare(this.number, goalBase.number);
    }
    
    public void copyFrom(GoalBase source) {
        ien = source.ien;
        name = source.name;
        createdDate = source.createdDate;
        createdBy = source.createdBy;
        lastUpdated = source.lastUpdated;
        updatedBy = source.updatedBy;
        provider = source.provider;
        startDate = source.startDate;
        followupDate = source.followupDate;
        status = source.status;
        number = source.number;
        reason = source.reason;
        facility = source.facility;
        types.clear();
        types.addAll(source.types);
    }
    
    public GoalGroup getGroup() {
        return status == null || status.isInactive() ? GoalGroup.INACTIVE : GoalGroup.ACTIVE;
    }
    
    public String getIEN() {
        return ien;
    }
    
    public void setIEN(String ien) {
        this.ien = ien;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public FMDate getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(FMDate createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public FMDate getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(FMDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public String getProviderIEN() {
        return getPiece(provider, 1);
    }
    
    public String getProviderName() {
        return getPiece(provider, 2);
    }
    
    public void setProvider(String provider) {
        this.provider = provider;
    }
    
    public FMDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(FMDate startDate) {
        this.startDate = startDate;
    }
    
    public FMDate getFollowupDate() {
        return followupDate;
    }
    
    public void setFollowupDate(FMDate followupDate) {
        this.followupDate = followupDate;
    }
    
    public GoalStatus getStatus() {
        return status;
    }
    
    public void setStatus(GoalStatus status) {
        this.status = status;
    }
    
    public float getNumber() {
        return number;
    }
    
    public String getNumberAsString() {
        return NumUtil.toString(number);
    }
    
    public void setNumber(float number) {
        this.number = number;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public List<GoalType> getTypes() {
        return types;
    }
    
    public String getFacilityIEN() {
        return getPiece(facility, 1);
    }
    
    public String getFacilityName() {
        return getPiece(facility, 2);
    }
    
    public void setFacility(String facility) {
        this.facility = facility;
    }
    
    public DeleteReason getDeleteReason() {
        return deleteReason;
    }
    
    public void setDeleteReason(DeleteReason deleteReason) {
        this.deleteReason = deleteReason;
    }
    
    public String getDeleteText() {
        return deleteText;
    }
    
    public void setDeleteText(String deleteText) {
        this.deleteText = deleteText;
    }
    
    public boolean isDeleted() {
        return status == GoalStatus.D;
    }
    
    private String getPiece(String value, int pc) {
        return value == null ? null : StrUtil.piece(value, ";", pc);
    }
}
