package com.ccms.exception;

/**
 * 审批业务异常类
 * 封装审批流程中的各种业务异常
 */
public class ApprovalException extends RuntimeException {
    
    /**
     * 异常代码
     */
    private final String errorCode;
    
    /**
     * 异常详情
     */
    private final String errorDetail;
    
    /**
     * 业务ID（如有）
     */
    private final String businessId;
    
    /**
     * 构造方法
     */
    public ApprovalException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = null;
        this.businessId = null;
    }
    
    public ApprovalException(String errorCode, String message, String errorDetail) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.businessId = null;
    }
    
    public ApprovalException(String errorCode, String message, String errorDetail, String businessId) {
        super(message);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.businessId = businessId;
    }
    
    public ApprovalException(String errorCode, String message, String errorDetail, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.businessId = null;
    }
    
    public ApprovalException(String errorCode, String message, String errorDetail, String businessId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorDetail = errorDetail;
        this.businessId = businessId;
    }
    
    /**
     * 获取错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 获取错误详情
     */
    public String getErrorDetail() {
        return errorDetail;
    }

    /**
     * 获取错误消息（兼容旧代码）
     */
    public String getErrorMessage() {
        return getMessage();
    }

    /**
     * 获取业务ID
     */
    public String getBusinessId() {
        return businessId;
    }
    
    /**
     * 判断是否是验证错误
     */
    public boolean isValidationError() {
        return errorCode != null && errorCode.startsWith("VALIDATION_");
    }
    
    /**
     * 判断是否是业务逻辑错误
     */
    public boolean isBusinessError() {
        return errorCode != null && errorCode.startsWith("BUSINESS_");
    }
    
    /**
     * 判断是否是权限错误
     */
    public boolean isSecurityError() {
        return errorCode != null && errorCode.startsWith("SECURITY_");
    }
    
    /**
     * 快速创建验证错误异常
     */
    public static ApprovalException validationError(String message, String detail) {
        return new ApprovalException("VALIDATION_ERROR", message, detail);
    }
    
    public static ApprovalException validationError(String message, String detail, String businessId) {
        return new ApprovalException("VALIDATION_ERROR", message, detail, businessId);
    }
    
    /**
     * 快速创建业务逻辑错误异常
     */
    public static ApprovalException businessError(String message, String detail) {
        return new ApprovalException("BUSINESS_ERROR", message, detail);
    }
    
    public static ApprovalException businessError(String message, String detail, String businessId) {
        return new ApprovalException("BUSINESS_ERROR", message, detail, businessId);
    }
    
    /**
     * 快速创建权限错误异常
     */
    public static ApprovalException securityError(String message, String detail) {
        return new ApprovalException("SECURITY_ERROR", message, detail);
    }
    
    public static ApprovalException securityError(String message, String detail, String businessId) {
        return new ApprovalException("SECURITY_ERROR", message, detail, businessId);
    }
    
    /**
     * 快速创建流程错误异常
     */
    public static ApprovalException workflowError(String message, String detail) {
        return new ApprovalException("WORKFLOW_ERROR", message, detail);
    }
    
    public static ApprovalException workflowError(String message, String detail, String businessId) {
        return new ApprovalException("WORKFLOW_ERROR", message, detail, businessId);
    }
    
    /**
     * 快速创建数据错误异常
     */
    public static ApprovalException dataError(String message, String detail) {
        return new ApprovalException("DATA_ERROR", message, detail);
    }
    
    public static ApprovalException dataError(String message, String detail, String businessId) {
        return new ApprovalException("DATA_ERROR", message, detail, businessId);
    }
    
    /**
     * 表示审批实例状态错误
     */
    public static ApprovalException instanceStatusError(String message, Long instanceId) {
        return new ApprovalException("INSTANCE_STATUS_ERROR", message, 
                "审批实例状态错误，实例ID: " + instanceId, instanceId != null ? instanceId.toString() : null);
    }
    
    /**
     * 表示业务数据不存在
     */
    public static ApprovalException businessDataNotFound(String businessType, String businessId) {
        return new ApprovalException("BUSINESS_DATA_NOT_FOUND", 
                "业务数据不存在", 
                "业务类型: " + businessType + ", 业务ID: " + businessId,
                businessId);
    }
    
    /**
     * 表示审批流程配置不存在
     */
    public static ApprovalException flowConfigNotFound(String businessType) {
        return new ApprovalException("FLOW_CONFIG_NOT_FOUND", 
                "审批流程配置不存在", 
                "业务类型: " + businessType,
                (String) null);
    }
    
    /**
     * 表示审批节点无效
     */
    public static ApprovalException invalidApprovalNode(Long nodeId) {
        return new ApprovalException("INVALID_APPROVAL_NODE", 
                "审批节点无效", 
                "节点ID: " + nodeId,
                nodeId != null ? nodeId.toString() : null);
    }
    
    /**
     * 表示审批操作不允许
     */
    public static ApprovalException approvalActionNotAllowed(Long instanceId, String action) {
        return new ApprovalException("APPROVAL_ACTION_NOT_ALLOWED", 
                "审批操作不允许", 
                "实例ID: " + instanceId + ", 操作: " + action,
                instanceId != null ? instanceId.toString() : null);
    }
    
    /**
     * 表示权限不足
     */
    public static ApprovalException insufficientPermission(Long userId, String action) {
        return new ApprovalException("INSUFFICIENT_PERMISSION", 
                "权限不足", 
                "用户ID: " + userId + ", 操作: " + action,
                userId != null ? userId.toString() : null);
    }
}