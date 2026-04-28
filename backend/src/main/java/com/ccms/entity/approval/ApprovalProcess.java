package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "approval_process")
public class ApprovalProcess extends BaseEntity {
    
    @Column(name = "process_code", nullable = false, unique = true, length = 50)
    private String processCode;
    
    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;
    
    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_active", columnDefinition = "tinyint(1) default 1")
    private Boolean active = true;
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    @Column(name = "total_nodes")
    private Integer totalNodes = 0;
    
    @Column(name = "current_node")
    private Integer currentNode = 0;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "end_time")
    private java.time.LocalDateTime endTime;
    
    @Column(name = "applicant_id")
    private Long applicantId;
    
    @Column(name = "process_id")
    private Long processId;
    
    // Getters and Setters
    public String getProcessCode() {
        return processCode;
    }
    
    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }
    
    public String getProcessName() {
        return processName;
    }
    
    public void setProcessName(String processName) {
        this.processName = processName;
    }
    
    public String getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Integer getVersion() {
        return version;
    }
    
    public void setVersion(Integer version) {
        this.version = version;
    }
    
    public Integer getTotalNodes() {
        return totalNodes;
    }
    
    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }
    
    public Integer getCurrentNode() {
        return currentNode;
    }
    
    public void setCurrentNode(Integer currentNode) {
        this.currentNode = currentNode;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public java.time.LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(java.time.LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Long getApplicantId() {
        return applicantId;
    }
    
    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }
    
    public Long getProcessId() {
        return processId;
    }
    
    public void setProcessId(Long processId) {
        this.processId = processId;
    }
}