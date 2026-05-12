package com.ccms.audit;

import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 审批操作审计日志实体类
 * 记录所有重要操作的用户行为
 */
@Entity
@Table(name = "ccms_approval_audit_log")
public class ApprovalAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 操作类型：CREATE, UPDATE, DELETE, APPROVE, REJECT, TRANSFER, SUBMIT等
     */
    @Column(name = "operation_type", nullable = false, length = 50)
    private String operationType;
    
    /**
     * 目标实体类型：FLOW_CONFIG, INSTANCE, RECORD, NODE等
     */
    @Column(name = "target_entity", nullable = false, length = 50)
    private String targetEntity;
    
    /**
     * 目标实体ID
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;
    
    /**
     * 操作用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    /**
     * 操作用户名
     */
    @Column(name = "user_name", length = 100)
    private String userName;
    
    /**
     * 用户IP地址
     */
    @Column(name = "user_ip", length = 45)
    private String userIp;
    
    /**
     * 操作描述
     */
    @Column(name = "operation_desc", length = 500)
    private String operationDesc;
    
    /**
     * 操作前的数据快照（JSON格式）
     */
    @Column(name = "before_data", columnDefinition = "TEXT")
    private String beforeData;
    
    /**
     * 操作后的数据快照（JSON格式）
     */
    @Column(name = "after_data", columnDefinition = "TEXT")
    private String afterData;
    
    /**
     * 操作详情（JSON格式）
     */
    @Column(name = "operation_details", columnDefinition = "TEXT")
    private String operationDetails;
    
    /**
     * 操作结果：SUCCESS, FAILED, PARTIAL_SUCCESS
     */
    @Column(name = "operation_result", length = 20)
    private String operationResult;
    
    /**
     * 错误信息（如果操作失败）
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    /**
     * 执行时间（毫秒）
     */
    @Column(name = "execution_time")
    private Long executionTime;
    
    /**
     * 操作时间
     */
    @CreationTimestamp
    @Column(name = "operation_time", nullable = false)
    private LocalDateTime operationTime;
    
    /**
     * 业务类型
     */
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    /**
     * 业务ID
     */
    @Column(name = "business_id")
    private Long businessId;
    
    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;
    
    /**
     * 客户端信息
     */
    @Column(name = "client_info", length = 200)
    private String clientInfo;
    
    /**
     * 扩展字段（JSON格式）
     */
    @Column(name = "extend_fields", columnDefinition = "TEXT")
    private String extendFields;
    
    // 构造函数
    public ApprovalAuditLog() {}
    
    public ApprovalAuditLog(String operationType, String targetEntity, Long targetId, 
                          Long userId, String userName, String operationDesc) {
        this.operationType = operationType;
        this.targetEntity = targetEntity;
        this.targetId = targetId;
        this.userId = userId;
        this.userName = userName;
        this.operationDesc = operationDesc;
        this.operationResult = "SUCCESS";
        this.operationTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getTargetEntity() { return targetEntity; }
    public void setTargetEntity(String targetEntity) { this.targetEntity = targetEntity; }
    
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getUserIp() { return userIp; }
    public void setUserIp(String userIp) { this.userIp = userIp; }
    
    public String getOperationDesc() { return operationDesc; }
    public void setOperationDesc(String operationDesc) { this.operationDesc = operationDesc; }
    
    public String getBeforeData() { return beforeData; }
    public void setBeforeData(String beforeData) { this.beforeData = beforeData; }
    
    public String getAfterData() { return afterData; }
    public void setAfterData(String afterData) { this.afterData = afterData; }
    
    public String getOperationDetails() { return operationDetails; }
    public void setOperationDetails(String operationDetails) { this.operationDetails = operationDetails; }
    
    public String getOperationResult() { return operationResult; }
    public void setOperationResult(String operationResult) { this.operationResult = operationResult; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }
    
    public LocalDateTime getOperationTime() { return operationTime; }
    public void setOperationTime(LocalDateTime operationTime) { this.operationTime = operationTime; }
    
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getClientInfo() { return clientInfo; }
    public void setClientInfo(String clientInfo) { this.clientInfo = clientInfo; }
    
    public String getExtendFields() { return extendFields; }
    public void setExtendFields(String extendFields) { this.extendFields = extendFields; }
}