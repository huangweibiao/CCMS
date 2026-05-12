package com.ccms.vo;

import com.ccms.enums.ApprovalActionEnum;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 审批记录视图对象
 */
public class ApprovalRecordVO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 审批实例ID
     */
    private Long instanceId;
    
    /**
     * 审批节点ID
     */
    private Long nodeId;
    
    /**
     * 审批节点步骤编号
     */
    private Integer stepNumber;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 部门信息
     */
    private String department;
    
    /**
     * 职位信息
     */
    private String position;
    
    /**
     * 审批操作
     */
    private ApprovalActionEnum action;
    
    /**
     * 操作说明
     */
    private String actionDescription;
    
    /**
     * 审批意见
     */
    private String comments;
    
    /**
     * 处理时间
     */
    private LocalDateTime processTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 前处理时间（分钟）
     */
    private Long beforeProcessMinutes;
    
    /**
     * 是否超时处理
     */
    private Boolean timeoutFlag;
    
    /**
     * 附件数量
     */
    private Integer attachmentCount;
    
    /**
     * 自定义字段（JSON格式）
     */
    private String customData;

    public ApprovalRecordVO() {}

    public ApprovalRecordVO(Long id, Long instanceId, Long nodeId, Integer stepNumber, Long approverId, 
                           String approverName, String department, String position, ApprovalActionEnum action,
                           String actionDescription, String comments, LocalDateTime processTime, 
                           LocalDateTime createTime, String remarks, Long beforeProcessMinutes, 
                           Boolean timeoutFlag, Integer attachmentCount, String customData) {
        this.id = id;
        this.instanceId = instanceId;
        this.nodeId = nodeId;
        this.stepNumber = stepNumber;
        this.approverId = approverId;
        this.approverName = approverName;
        this.department = department;
        this.position = position;
        this.action = action;
        this.actionDescription = actionDescription;
        this.comments = comments;
        this.processTime = processTime;
        this.createTime = createTime;
        this.remarks = remarks;
        this.beforeProcessMinutes = beforeProcessMinutes;
        this.timeoutFlag = timeoutFlag;
        this.attachmentCount = attachmentCount;
        this.customData = customData;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public ApprovalActionEnum getAction() {
        return action;
    }

    public void setAction(ApprovalActionEnum action) {
        this.action = action;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public LocalDateTime getProcessTime() {
        return processTime;
    }

    public void setProcessTime(LocalDateTime processTime) {
        this.processTime = processTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Long getBeforeProcessMinutes() {
        return beforeProcessMinutes;
    }

    public void setBeforeProcessMinutes(Long beforeProcessMinutes) {
        this.beforeProcessMinutes = beforeProcessMinutes;
    }

    public Boolean getTimeoutFlag() {
        return timeoutFlag;
    }

    public void setTimeoutFlag(Boolean timeoutFlag) {
        this.timeoutFlag = timeoutFlag;
    }

    public Integer getAttachmentCount() {
        return attachmentCount;
    }

    public void setAttachmentCount(Integer attachmentCount) {
        this.attachmentCount = attachmentCount;
    }

    public String getCustomData() {
        return customData;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalRecordVO that = (ApprovalRecordVO) o;
        return Objects.equals(id, that.id) && Objects.equals(instanceId, that.instanceId) 
                && Objects.equals(nodeId, that.nodeId) && Objects.equals(stepNumber, that.stepNumber) 
                && Objects.equals(approverId, that.approverId) && Objects.equals(approverName, that.approverName) 
                && Objects.equals(department, that.department) && Objects.equals(position, that.position) 
                && Objects.equals(action, that.action) && Objects.equals(actionDescription, that.actionDescription) 
                && Objects.equals(comments, that.comments) && Objects.equals(processTime, that.processTime) 
                && Objects.equals(createTime, that.createTime) && Objects.equals(remarks, that.remarks) 
                && Objects.equals(beforeProcessMinutes, that.beforeProcessMinutes) 
                && Objects.equals(timeoutFlag, that.timeoutFlag) && Objects.equals(attachmentCount, that.attachmentCount) 
                && Objects.equals(customData, that.customData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, instanceId, nodeId, stepNumber, approverId, approverName, department, position,
                action, actionDescription, comments, processTime, createTime, remarks, beforeProcessMinutes,
                timeoutFlag, attachmentCount, customData);
    }

    @Override
    public String toString() {
        return "ApprovalRecordVO{" +
                "id=" + id +
                ", instanceId=" + instanceId +
                ", nodeId=" + nodeId +
                ", stepNumber=" + stepNumber +
                ", approverId=" + approverId +
                ", approverName='" + approverName + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", action=" + action +
                ", actionDescription='" + actionDescription + '\'' +
                ", comments='" + comments + '\'' +
                ", processTime=" + processTime +
                ", createTime=" + createTime +
                ", remarks='" + remarks + '\'' +
                ", beforeProcessMinutes=" + beforeProcessMinutes +
                ", timeoutFlag=" + timeoutFlag +
                ", attachmentCount=" + attachmentCount +
                ", customData='" + customData + '\'' +
                '}';
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long instanceId;
        private Long nodeId;
        private Integer stepNumber;
        private Long approverId;
        private String approverName;
        private String department;
        private String position;
        private ApprovalActionEnum action;
        private String actionDescription;
        private String comments;
        private LocalDateTime processTime;
        private LocalDateTime createTime;
        private String remarks;
        private Long beforeProcessMinutes;
        private Boolean timeoutFlag;
        private Integer attachmentCount;
        private String customData;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder instanceId(Long instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder nodeId(Long nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder stepNumber(Integer stepNumber) {
            this.stepNumber = stepNumber;
            return this;
        }

        public Builder approverId(Long approverId) {
            this.approverId = approverId;
            return this;
        }

        public Builder approverName(String approverName) {
            this.approverName = approverName;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder position(String position) {
            this.position = position;
            return this;
        }

        public Builder action(ApprovalActionEnum action) {
            this.action = action;
            return this;
        }

        public Builder actionDescription(String actionDescription) {
            this.actionDescription = actionDescription;
            return this;
        }

        public Builder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public Builder processTime(LocalDateTime processTime) {
            this.processTime = processTime;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public Builder beforeProcessMinutes(Long beforeProcessMinutes) {
            this.beforeProcessMinutes = beforeProcessMinutes;
            return this;
        }

        public Builder timeoutFlag(Boolean timeoutFlag) {
            this.timeoutFlag = timeoutFlag;
            return this;
        }

        public Builder attachmentCount(Integer attachmentCount) {
            this.attachmentCount = attachmentCount;
            return this;
        }

        public Builder customData(String customData) {
            this.customData = customData;
            return this;
        }

        public ApprovalRecordVO build() {
            return new ApprovalRecordVO(id, instanceId, nodeId, stepNumber, approverId, approverName,
                    department, position, action, actionDescription, comments, processTime, createTime,
                    remarks, beforeProcessMinutes, timeoutFlag, attachmentCount, customData);
        }
    }
}