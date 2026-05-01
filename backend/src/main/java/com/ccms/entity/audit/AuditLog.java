package com.ccms.entity.audit;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 审计日志实体类
 */
@Entity
@Table(name = "audit_log")
public class AuditLog extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    private String module;

    @NotBlank
    @Size(max = 100)
    private String operation;

    @NotBlank
    @Size(max = 500)
    private String description;

    private Long userId;

    @Size(max = 100)
    private String username;

    @Size(max = 50)
    private String userIp;

    @Size(max = 1000)
    private String requestUrl;

    @Size(max = 10)
    private String requestMethod;

    @Lob
    private String requestParams;

    @Lob
    private String responseResult;

    private Boolean success = true;

    private Integer statusCode;

    @Size(max = 1000)
    private String errorMessage;

    private Long executionTime; // 执行时间（毫秒）

    @Size(max = 100)
    private String entityType;

    private Long entityId;

    @Size(max = 500)
    private String entityName;

    // 构造器
    public AuditLog() {}

    public AuditLog(String module, String operation, String description) {
        this.module = module;
        this.operation = operation;
        this.description = description;
    }

    // Getters and Setters
    public String getModule() { return module; }
    public void setModule(String module) { this.module = module; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserIp() { return userIp; }
    public void setUserIp(String userIp) { this.userIp = userIp; }

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

    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getExecutionTime() { return executionTime; }
    public void setExecutionTime(Long executionTime) { this.executionTime = executionTime; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }

    /**
     * 设置操作成功状态
     */
    public void markSuccess() {
        this.success = true;
        this.statusCode = 200;
    }

    /**
     * 设置操作失败状态
     */
    public void markFailure(String errorMessage, Integer statusCode) {
        this.success = false;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

    /**
     * 设置用户信息
     */
    public void setUserInfo(Long userId, String username, String userIp) {
        this.userId = userId;
        this.username = username;
        this.userIp = userIp;
    }

    /**
     * 设置请求信息
     */
    public void setRequestInfo(String requestUrl, String requestMethod, String requestParams) {
        this.requestUrl = requestUrl;
        this.requestMethod = requestMethod;
        this.requestParams = requestParams;
    }

    /**
     * 设置实体信息
     */
    public void setEntityInfo(String entityType, Long entityId, String entityName) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.entityName = entityName;
    }
}