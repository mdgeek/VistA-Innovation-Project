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
import java.util.Date;
import java.util.List;

import org.carewebframework.common.StrUtil;

/**
 * Base class for goals and steps.
 */
public abstract class GoalBase {
    
    public enum GoalGroup {
        ACTIVE, INACTIVE, DECLINED
    };
    
    private String ien;
    
    private Date createdDate;
    
    private String createdBy;
    
    private Date lastUpdated;
    
    private String updatedBy;
    
    private String provider;
    
    private Date startDate;
    
    private Date followupDate;
    
    private String status;
    
    private String number;
    
    private String reason;
    
    private final List<String> type = new ArrayList<>();
    
    public abstract GoalGroup getGroup();
    
    public String getIEN() {
        return ien;
    }
    
    public void setIEN(String ien) {
        this.ien = ien;
    }
    
    public Date getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public Date getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(Date lastUpdated) {
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
    
    public Date getStartDate() {
        return startDate;
    }
    
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    public Date getFollowupDate() {
        return followupDate;
    }
    
    public void setFollowupDate(Date followupDate) {
        this.followupDate = followupDate;
    }
    
    public String getStatusCode() {
        return StrUtil.piece(status, ";");
    }
    
    public String getStatusText() {
        return StrUtil.piece(status, ";", 2);
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getNumber() {
        return number;
    }
    
    public void setNumber(String number) {
        this.number = number;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
    
    public List<String> getType() {
        return type;
    }
    
}
