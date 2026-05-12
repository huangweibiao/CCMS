package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.AuditActionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 审批审计日志实体类
 * 记录所有审批操作的详细历史，用于追踪和审计
 */
@Entity
@Table(name = "ccms_approval_audit_log")
public class ApprovalAuditLog extends BaseEntity {

    /**
     * 审批实例ID
     */
    @Column(name = "instance_id")
    private Long instanceId;

    /**
     * 审批记录ID
     */
    @Column(name = "record_id")
    private Long recordId;

    /**
     * 流程配置ID
     */
    @Column(name = "config_id")
    private Long configId;

    /**
     * 业务类型
     */
    @Column(name = "business_type", length = 32)
    private String businessType;

    /**
     * 业务ID
     */
    @Column(name = "business_id", length = 64)
    private String businessId;

    /**
     * 审计动作类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", length = 32, nullable = false)
    private AuditActionType actionType;

    /**
     * 操作人ID
     */
    @Column(name = "operator_id")
    private Long operatorId;

    /**
     * 操作人姓名
     */
    @Column(name = "operator_name", length = 100)
    private String operatorName;

    /**
     * 操作描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 原状态（用于状态变更记录）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 32)
    private ApprovalStatus oldStatus;

    /**
     * 新状态（用于状态变更记录）
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 32)
    private ApprovalStatus newStatus;

    /**
     * 日志时间
     */
    @Column(name = "log_time", nullable = false)
    private LocalDateTime logTime;

    /**
     * IP地址
     */
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    /**
     * 用户代理
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * 备注信息
     */
    @Column(name = "remarks", length = 1000)
    private String remarks;

    /**
     * 请求参数（JSON格式）
     */
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams;

    /**
     * 响应结果（JSON格式）
     */
    @Column(name = "response_result", columnDefinition = "TEXT")
    private String responseResult;

    /**
     * 是否成功
     */
    @Column(name = "is_success")
    private Boolean success = true;

    /**
     * 错误信息
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * 耗时（毫秒）
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * 组织机构ID
     */
    @Column(name = "org_id")
    private Long orgId;

    /**
     * 部门ID
     */
    @Column(name = "dept_id")
    private Long deptId;

    /**
     * 租户ID（多租户支持）
     */
    @Column(name = "tenant_id")
    private Long tenantId;

    // Getters and Setters
    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }

    public Long getRecordId() {
        return recordId;
    }

    public void setRecordId(Long recordId) {
        this.recordId = recordId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public AuditActionType getActionType() {
        return actionType;
    }

    public void setActionType(AuditActionType actionType) {
        this.actionType = actionType;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApprovalStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(ApprovalStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public ApprovalStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ApprovalStatus newStatus) {
        this.newStatus = newStatus;
    }

    public LocalDateTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(String requestParams) {
        this.requestParams = requestParams;
    }

    public String getResponseResult() {
        return responseResult;
    }

    public void setResponseResult(String responseResult) {
        this.responseResult = responseResult;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "ApprovalAuditLog{" +
                "id=" + getId() +
                ", instanceId=" + instanceId +
                ", recordId=" + recordId +
                ", businessType='" + businessType + '\'' +
                ", businessId='" + businessId + '\'' +
                ", actionType=" + actionType +
                ", operatorName='" + operatorName + '\'' +
                ", description='" + description + '\'' +
                ", oldStatus=" + oldStatus +
                ", newStatus=" + newStatus +
                ", logTime=" + logTime +
                ", success=" + success +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}