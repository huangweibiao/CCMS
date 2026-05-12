package com.ccms.exception;

/**
 * 审批资源不存在异常
 */
public class ApprovalNotFoundException extends ApprovalException {
    
    private final String resourceType;
    private final Object resourceId;
    
    public ApprovalNotFoundException(String resourceType, Object resourceId) {
        super("APPROVAL_NOT_FOUND", 
              String.format("审批%s不存在: ID=%s", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public ApprovalNotFoundException(String resourceType, Object resourceId, Throwable cause) {
        super("APPROVAL_NOT_FOUND", 
              String.format("审批%s不存在: ID=%s", resourceType, String.valueOf(resourceId)));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public Object getResourceId() {
        return resourceId;
    }
}