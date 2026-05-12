package com.ccms.dto;

import com.ccms.enums.ApprovalStatusEnum;
import java.util.Objects;

/**
 * 审批结果DTO
 */
public class ApprovalResult {
    
    /**
     * 操作是否成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 审批实例ID
     */
    private Long instanceId;
    
    /**
     * 流程配置ID
     */
    private Long flowConfigId;
    
    /**
     * 当前节点
     */
    private Integer currentNode;
    
    /**
     * 审批状态
     */
    private ApprovalStatusEnum status;
    
    /**
     * 是否完成
     */
    private Boolean completed;
    
    /**
     * 完成时间
     */
    private Long finishTime;
    
    /**
     * 错误码（失败时使用）
     */
    private String errorCode;
    
    /**
     * 错误详情（失败时使用）
     */
    private String errorDetails;
    
    // 无参构造函数
    public ApprovalResult() {}
    
    // 全参构造函数
    public ApprovalResult(boolean success, String message, Long instanceId, Long flowConfigId, 
                         Integer currentNode, ApprovalStatusEnum status, Boolean completed, 
                         Long finishTime, String errorCode, String errorDetails) {
        this.success = success;
        this.message = message;
        this.instanceId = instanceId;
        this.flowConfigId = flowConfigId;
        this.currentNode = currentNode;
        this.status = status;
        this.completed = completed;
        this.finishTime = finishTime;
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
    }
    
    // Getter方法
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Long getInstanceId() {
        return instanceId;
    }
    
    public Long getFlowConfigId() {
        return flowConfigId;
    }
    
    public Integer getCurrentNode() {
        return currentNode;
    }
    
    public ApprovalStatusEnum getStatus() {
        return status;
    }
    
    public Boolean getCompleted() {
        return completed;
    }
    
    public Long getFinishTime() {
        return finishTime;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    // Setter方法
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
    }
    
    public void setFlowConfigId(Long flowConfigId) {
        this.flowConfigId = flowConfigId;
    }
    
    public void setCurrentNode(Integer currentNode) {
        this.currentNode = currentNode;
    }
    
    public void setStatus(ApprovalStatusEnum status) {
        this.status = status;
    }
    
    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
    public void setFinishTime(Long finishTime) {
        this.finishTime = finishTime;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    // equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalResult that = (ApprovalResult) o;
        return success == that.success &&
                Objects.equals(message, that.message) &&
                Objects.equals(instanceId, that.instanceId) &&
                Objects.equals(flowConfigId, that.flowConfigId) &&
                Objects.equals(currentNode, that.currentNode) &&
                status == that.status &&
                Objects.equals(completed, that.completed) &&
                Objects.equals(finishTime, that.finishTime) &&
                Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(errorDetails, that.errorDetails);
    }
    
    // hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(success, message, instanceId, flowConfigId, currentNode, 
                          status, completed, finishTime, errorCode, errorDetails);
    }
    
    // toString方法
    @Override
    public String toString() {
        return "ApprovalResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", instanceId=" + instanceId +
                ", flowConfigId=" + flowConfigId +
                ", currentNode=" + currentNode +
                ", status=" + status +
                ", completed=" + completed +
                ", finishTime=" + finishTime +
                ", errorCode='" + errorCode + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                '}';
    }
    
    // 手动Builder实现
    public static ApprovalResultBuilder builder() {
        return new ApprovalResultBuilder();
    }
    
    public static class ApprovalResultBuilder {
        private boolean success;
        private String message;
        private Long instanceId;
        private Long flowConfigId;
        private Integer currentNode;
        private ApprovalStatusEnum status;
        private Boolean completed;
        private Long finishTime;
        private String errorCode;
        private String errorDetails;
        
        public ApprovalResultBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public ApprovalResultBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public ApprovalResultBuilder instanceId(Long instanceId) {
            this.instanceId = instanceId;
            return this;
        }
        
        public ApprovalResultBuilder flowConfigId(Long flowConfigId) {
            this.flowConfigId = flowConfigId;
            return this;
        }
        
        public ApprovalResultBuilder currentNode(Integer currentNode) {
            this.currentNode = currentNode;
            return this;
        }
        
        public ApprovalResultBuilder status(ApprovalStatusEnum status) {
            this.status = status;
            return this;
        }
        
        public ApprovalResultBuilder completed(Boolean completed) {
            this.completed = completed;
            return this;
        }
        
        public ApprovalResultBuilder finishTime(Long finishTime) {
            this.finishTime = finishTime;
            return this;
        }
        
        public ApprovalResultBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }
        
        public ApprovalResultBuilder errorDetails(String errorDetails) {
            this.errorDetails = errorDetails;
            return this;
        }
        
        public ApprovalResult build() {
            ApprovalResult result = new ApprovalResult();
            result.success = this.success;
            result.message = this.message;
            result.instanceId = this.instanceId;
            result.flowConfigId = this.flowConfigId;
            result.currentNode = this.currentNode;
            result.status = this.status;
            result.completed = this.completed;
            result.finishTime = this.finishTime;
            result.errorCode = this.errorCode;
            result.errorDetails = this.errorDetails;
            return result;
        }
    }
}