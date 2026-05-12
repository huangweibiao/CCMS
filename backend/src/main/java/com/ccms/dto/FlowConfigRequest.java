package com.ccms.dto;

import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.PriorityTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * 流程配置请求DTO
 */
public class FlowConfigRequest {
    
    /**
     * 流程代码（唯一标识）
     */
    @NotBlank(message = "流程代码不能为空")
    private String flowCode;
    
    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String flowName;
    
    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private BusinessTypeEnum businessType;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 优先级类型
     */
    private PriorityTypeEnum priorityType;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 金额上限（可选）
     */
    private Double amountLimit;
    
    /**
     * 金额下限（可选）
     */
    private Double amountFloor;
    
    // 无参构造函数
    public FlowConfigRequest() {}
    
    // 全参构造函数
    public FlowConfigRequest(String flowCode, String flowName, BusinessTypeEnum businessType, 
                           String description, PriorityTypeEnum priorityType, Boolean enabled, 
                           Double amountLimit, Double amountFloor) {
        this.flowCode = flowCode;
        this.flowName = flowName;
        this.businessType = businessType;
        this.description = description;
        this.priorityType = priorityType;
        this.enabled = enabled;
        this.amountLimit = amountLimit;
        this.amountFloor = amountFloor;
    }
    
    // Getter和Setter方法
    public String getFlowCode() {
        return flowCode;
    }
    
    public void setFlowCode(String flowCode) {
        this.flowCode = flowCode;
    }
    
    public String getFlowName() {
        return flowName;
    }
    
    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }
    
    public BusinessTypeEnum getBusinessType() {
        return businessType;
    }
    
    public void setBusinessType(BusinessTypeEnum businessType) {
        this.businessType = businessType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public PriorityTypeEnum getPriorityType() {
        return priorityType;
    }
    
    public void setPriorityType(PriorityTypeEnum priorityType) {
        this.priorityType = priorityType;
    }
    
    public Boolean getEnabled() {
        return enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    public Double getAmountLimit() {
        return amountLimit;
    }
    
    public void setAmountLimit(Double amountLimit) {
        this.amountLimit = amountLimit;
    }
    
    public Double getAmountFloor() {
        return amountFloor;
    }
    
    public void setAmountFloor(Double amountFloor) {
        this.amountFloor = amountFloor;
    }
    
    // Builder方法
    public static FlowConfigRequestBuilder builder() {
        return new FlowConfigRequestBuilder();
    }
    
    public static class FlowConfigRequestBuilder {
        private String flowCode;
        private String flowName;
        private BusinessTypeEnum businessType;
        private String description;
        private PriorityTypeEnum priorityType;
        private Boolean enabled;
        private Double amountLimit;
        private Double amountFloor;
        
        public FlowConfigRequestBuilder flowCode(String flowCode) {
            this.flowCode = flowCode;
            return this;
        }
        
        public FlowConfigRequestBuilder flowName(String flowName) {
            this.flowName = flowName;
            return this;
        }
        
        public FlowConfigRequestBuilder businessType(BusinessTypeEnum businessType) {
            this.businessType = businessType;
            return this;
        }
        
        public FlowConfigRequestBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public FlowConfigRequestBuilder priorityType(PriorityTypeEnum priorityType) {
            this.priorityType = priorityType;
            return this;
        }
        
        public FlowConfigRequestBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public FlowConfigRequestBuilder amountLimit(Double amountLimit) {
            this.amountLimit = amountLimit;
            return this;
        }
        
        public FlowConfigRequestBuilder amountFloor(Double amountFloor) {
            this.amountFloor = amountFloor;
            return this;
        }
        
        public FlowConfigRequest build() {
            return new FlowConfigRequest(flowCode, flowName, businessType, description, 
                                       priorityType, enabled, amountLimit, amountFloor);
        }
    }
    
    // equals方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlowConfigRequest that = (FlowConfigRequest) o;
        return Objects.equals(flowCode, that.flowCode) &&
                Objects.equals(flowName, that.flowName) &&
                businessType == that.businessType &&
                Objects.equals(description, that.description) &&
                priorityType == that.priorityType &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(amountLimit, that.amountLimit) &&
                Objects.equals(amountFloor, that.amountFloor);
    }
    
    // hashCode方法
    @Override
    public int hashCode() {
        return Objects.hash(flowCode, flowName, businessType, description, priorityType, 
                          enabled, amountLimit, amountFloor);
    }
    
    // toString方法
    @Override
    public String toString() {
        return "FlowConfigRequest{" +
                "flowCode='" + flowCode + '\'' +
                ", flowName='" + flowName + '\'' +
                ", businessType=" + businessType +
                ", description='" + description + '\'' +
                ", priorityType=" + priorityType +
                ", enabled=" + enabled +
                ", amountLimit=" + amountLimit +
                ", amountFloor=" + amountFloor +
                '}';
    }
}