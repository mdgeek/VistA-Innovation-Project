/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 *
 * This Source Code Form is also subject to the terms of the Health-Related Additional
 * Disclaimer of Warranty and Limitation of Liability available at
 * http://www.carewebframework.org/licensing/disclaimer.
 */
package org.carewebframework.vista.ui.patientgoals.model;

import java.util.ArrayList;
import java.util.List;

import org.carewebframework.common.StrUtil;
import org.carewebframework.vista.mbroker.FMDate;

/**
 * Base class for goals and steps.
 */
public abstract class GoalBase implements Comparable<GoalBase> {
    
    public enum GoalGroup {
        ACTIVE, INACTIVE, DECLINED
    };
    
    private String ien;
    
    private FMDate createdDate;
    
    private String createdBy;
    
    private FMDate lastUpdated;
    
    private String updatedBy;
    
    private String provider;
    
    private FMDate startDate;
    
    private FMDate followupDate;
    
    private String status;
    
    private float number;
    
    private String reason;
    
    private final List<GoalType> type = new ArrayList<>();
    
    public abstract GoalGroup getGroup();
    
    @Override
    public int compareTo(GoalBase goalBase) {
        return Float.compare(this.number, goalBase.number);
    }
    
    public String getIEN() {
        return ien;
    }
    
    public void setIEN(String ien) {
        this.ien = ien;
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
    
    public String getProvider() {
        return provider;
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
    
    public String getStatusCode() {
        return status == null ? "" : StrUtil.piece(status, ";");
    }
    
    public String getStatusText() {
        return status == null ? "" : StrUtil.piece(status, ";", 2);
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public float getNumber() {
        return number;
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
        return type;
    }
    
}
