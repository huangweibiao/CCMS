package com.ccms.dto;

import com.ccms.enums.BusinessTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * 审批请求DTO
 */
public class ApprovalRequest {
    
    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private BusinessTypeEnum businessType;
    
    /**
     * 业务ID
     */
    @NotBlank(message = "业务ID不能为空")
    private String businessId;
    
    /**
     * 申请人ID
     */
    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;
    
    /**
     * 审批标题
     */
    @NotBlank(message = "审批标题不能为空")
    private String title;
    
    /**
     * 审批内容
     */
    private String content;
    
    /**
     * 金额（可选）
     */
    private BigDecimal amount;
    
    /**
     * 部门ID（可选，用于流程配置匹配）
     */
    private Long departId;
    
    /**
     * 紧急程度
     */
    private String urgencyLevel;
    
    /**
     * 自定义数据（JSON格式）
     */
    private String customData;

    // 无参构造函数
    public ApprovalRequest() {}
    
    // 全参构造函数
    public ApprovalRequest(BusinessTypeEnum businessType, String businessId, Long applicantId, 
                          String title, String content, BigDecimal amount, Long departId, 
                          String urgencyLevel, String customData) {
        this.businessType = businessType;
        this.businessId = businessId;
        this.applicantId = applicantId;
        this.title = title;
        this.content = content;
        this.amount = amount;
        this.departId = departId;
        this.urgencyLevel = urgencyLevel;
        this.customData = customData;
    }

    // Getter方法
    public BusinessTypeEnum getBusinessType() {
        return businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Long getDepartId() {
        return departId;
    }

    public String getUrgencyLevel() {
        return urgencyLevel;
    }

    public String getCustomData() {
        return customData;
    }
    
    // Setter方法
    public void setBusinessType(BusinessTypeEnum businessType) {
        this.businessType = businessType;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDepartId(Long departId) {
        this.departId = departId;
    }

    public void setUrgencyLevel(String urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    public void setCustomData(String customData) {
        this.customData = customData;
    }
    
    // Builder方法
    public static ApprovalRequestBuilder builder() {
        return new ApprovalRequestBuilder();
    }
    
    public static class ApprovalRequestBuilder {
        private BusinessTypeEnum businessType;
        private String businessId;
        private Long applicantId;
        private String title;
        private String content;
        private BigDecimal amount;
        private Long departId;
        private String urgencyLevel;
        private String customData;
        
        public ApprovalRequestBuilder businessType(BusinessTypeEnum businessType) {
            this.businessType = businessType;
            return this;
        }
        
        public ApprovalRequestBuilder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }
        
        public ApprovalRequestBuilder applicantId(Long applicantId) {
            this.applicantId = applicantId;
            return this;
        }
        
        public ApprovalRequestBuilder title(String title) {
            this.title = title;
            return this;
        }
        
        public ApprovalRequestBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public ApprovalRequestBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public ApprovalRequestBuilder departId(Long departId) {
            this.departId = departId;
            return this;
        }
        
        public ApprovalRequestBuilder urgencyLevel(String urgencyLevel) {
            this.urgencyLevel = urgencyLevel;
            return this;
        }
        
        public ApprovalRequestBuilder customData(String customData) {
            this.customData = customData;
            return this;
        }
        
        public ApprovalRequest build() {
            return new ApprovalRequest(businessType, businessId, applicantId, title, content, 
                                     amount, departId, urgencyLevel, customData);
        }
    }
    
    // equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalRequest that = (ApprovalRequest) o;
        return businessType == that.businessType &&
                Objects.equals(businessId, that.businessId) &&
                Objects.equals(applicantId, that.applicantId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(departId, that.departId) &&
                Objects.equals(urgencyLevel, that.urgencyLevel) &&
                Objects.equals(customData, that.customData);
    }
    
    // hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(businessType, businessId, applicantId, title, content, 
                          amount, departId, urgencyLevel, customData);
    }
    
    // toString方法
    @Override
    public String toString() {
        return "ApprovalRequest{" +
                "businessType=" + businessType +
                ", businessId='" + businessId + '\'' +
                ", applicantId=" + applicantId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", amount=" + amount +
                ", departId=" + departId +
                ", urgencyLevel='" + urgencyLevel + '\'' +
                ", customData='" + customData + '\'' +
                '}';
    }
}