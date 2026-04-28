package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "approval_node")
public class ApprovalNode extends BaseEntity {
    
    @Column(name = "process_id", nullable = false)
    private Long processId;
    
    @Column(name = "node_code", nullable = false, length = 50)
    private String nodeCode;
    
    @Column(name = "node_name", nullable = false, length = 200)
    private String nodeName;
    
    @Column(name = "node_type", nullable = false, length = 20)
    private String nodeType; // START, NORMAL, END, CONDITION
    
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;
    
    @Column(name = "approver_type", length = 20)
    private String approverType; // USER, ROLE, DEPT
    
    @Column(name = "approver_id")
    private Long approverId;
    
    @Column(name = "approver_name", length = 100)
    private String approverName;
    
    @Column(name = "condition_expression", length = 1000)
    private String conditionExpression;
    
    @Column(name = "is_required", columnDefinition = "tinyint(1) default 1")
    private Boolean required = true;
    
    @Column(name = "time_limit")
    private Integer timeLimit; // 小时数
    
    @Column(name = "next_node_id")
    private Long nextNodeId;
    
    @Column(name = "remark", length = 500)
    private String remark;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "process_time")
    private java.time.LocalDateTime processTime;
    
    @Column(name = "node_level")
    private Integer nodeLevel;
    
    // Getters and Setters
    public Long getProcessId() {
        return processId;
    }
    
    public void setProcessId(Long processId) {
        this.processId = processId;
    }
    
    public String getNodeCode() {
        return nodeCode;
    }
    
    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getNodeType() {
        return nodeType;
    }
    
    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }
    
    public Integer getStepNumber() {
        return stepNumber;
    }
    
    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }
    
    public String getApproverType() {
        return approverType;
    }
    
    public void setApproverType(String approverType) {
        this.approverType = approverType;
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
    
    public String getConditionExpression() {
        return conditionExpression;
    }
    
    public void setConditionExpression(String conditionExpression) {
        this.conditionExpression = conditionExpression;
    }
    
    public Boolean getRequired() {
        return required;
    }
    
    public void setRequired(Boolean required) {
        this.required = required;
    }
    
    public Integer getTimeLimit() {
        return timeLimit;
    }
    
    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
    
    public Long getNextNodeId() {
        return nextNodeId;
    }
    
    public void setNextNodeId(Long nextNodeId) {
        this.nextNodeId = nextNodeId;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public java.time.LocalDateTime getProcessTime() {
        return processTime;
    }
    
    public void setProcessTime(java.time.LocalDateTime processTime) {
        this.processTime = processTime;
    }
    
    public Integer getNodeLevel() {
        return nodeLevel;
    }
    
    public void setNodeLevel(Integer nodeLevel) {
        this.nodeLevel = nodeLevel;
    }
}