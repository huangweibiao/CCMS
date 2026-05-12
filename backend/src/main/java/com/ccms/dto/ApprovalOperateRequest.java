package com.ccms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 审批操作请求DTO
 */
public class ApprovalOperateRequest {
    
    /**
     * 操作人ID
     */
    @NotNull(message = "操作人ID不能为空")
    private Long operatorId;
    
    /**
     * 操作类型
     */
    @NotNull(message = "操作类型不能为空")
    private Integer action;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 附件ID（可选）
     */
    private String attachmentId;
    
    /**
     * 自定义操作数据（JSON格式）
     */
    private String customData;

    // 无参构造函数
    public ApprovalOperateRequest() {}
    
    // 全参构造函数
    public ApprovalOperateRequest(Long operatorId, Integer action, String remarks, 
                                  String attachmentId, String customData) {
        this.operatorId = operatorId;
        this.action = action;
        this.remarks = remarks;
        this.attachmentId = attachmentId;
        this.customData = customData;
    }

    // Getter方法
    public Long getOperatorId() {
        return operatorId;
    }

    public Integer getAction() {
        return action;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public String getCustomData() {
        return customData;
    }
    
    // Setter方法
    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }
    
    // Builder方法
    public static ApprovalOperateRequestBuilder builder() {
        return new ApprovalOperateRequestBuilder();
    }
    
    public static class ApprovalOperateRequestBuilder {
        private Long operatorId;
        private Integer action;
        private String remarks;
        private String attachmentId;
        private String customData;
        
        public ApprovalOperateRequestBuilder operatorId(Long operatorId) {
            this.operatorId = operatorId;
            return this;
        }
        
        public ApprovalOperateRequestBuilder action(Integer action) {
            this.action = action;
            return this;
        }
        
        public ApprovalOperateRequestBuilder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }
        
        public ApprovalOperateRequestBuilder attachmentId(String attachmentId) {
            this.attachmentId = attachmentId;
            return this;
        }
        
        public ApprovalOperateRequestBuilder customData(String customData) {
            this.customData = customData;
            return this;
        }
        
        public ApprovalOperateRequest build() {
            return new ApprovalOperateRequest(operatorId, action, remarks, attachmentId, customData);
        }
    }
    
    // equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalOperateRequest that = (ApprovalOperateRequest) o;
        return Objects.equals(operatorId, that.operatorId) &&
                Objects.equals(action, that.action) &&
                Objects.equals(remarks, that.remarks) &&
                Objects.equals(attachmentId, that.attachmentId) &&
                Objects.equals(customData, that.customData);
    }
    
    // hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(operatorId, action, remarks, attachmentId, customData);
    }
    
    // toString方法
    @Override
    public String toString() {
        return "ApprovalOperateRequest{" +
                "operatorId=" + operatorId +
                ", action=" + action +
                ", remarks='" + remarks + '\'' +
                ", attachmentId='" + attachmentId + '\'' +
                ", customData='" + customData + '\'' +
                '}';
    }
}