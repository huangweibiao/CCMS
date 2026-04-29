package com.ccms.entity.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作日志审计实体
 */
@Entity
@Table(name = "operation_log")
public class OperationLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(length = 100)
    private String userName;
    
    @Column(nullable = false, length = 100)
    private String operationType;
    
    @Column(nullable = false, length = 500)
    private String description;
    
    @Column(length = 100)
    private String targetType;
    
    @Column
    private Long targetId;
    
    @Column(length = 50)
    private String ipAddress;
    
    @Column(length = 2000)
    private String requestUrl;
    
    @Column(length = 50)
    private String requestMethod;
    
    @Column(columnDefinition = "TEXT")
    private String requestParams;
    
    @Column(columnDefinition = "TEXT")
    private String responseResult;
    
    @Column(nullable = false)
    private Boolean success = true;
    
    @Column
    private String errorMessage;
    
    @Column(nullable = false)
    private Integer executionTime;
    
    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    // Constructors
    public OperationLog() {}
    
    public OperationLog(Long userId, String operationType, String description) {
        this.userId = userId;
        this.operationType = operationType;
        this.description = description;
    }
    
    // Business methods
    public void markAsFailed(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    public void setExecutionInfo(Integer executionTime) {
        this.executionTime = executionTime;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    
    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getRequestUrl() { return requestUrl; }
    public void setRequestUrl(String requestUrl) { this.requestUrl = requestUrl; }
    
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    
    public String getRequestParams() { return requestParams; }
    public void setRequestParams(String requestParams) { this.requestParams = requestParams; }
    
    public String getResponseResult() { return responseResult; }
    public void setResponseResult(String responseResult) { this.responseResult = responseResult; }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public Integer getExecutionTime() { return executionTime; }
    public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}