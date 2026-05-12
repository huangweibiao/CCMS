package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import com.ccms.enums.BusinessTypeEnum;

/**
 * 审批流配置表实体类
 * 对应表名：ccms_approval_flow_config
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_approval_flow_config")
public class ApprovalFlowConfig extends BaseEntity {

    /**
     * 流程编码
     */
    @Column(name = "flow_code", length = 32, nullable = false)
    private String flowCode;
    
    /**
     * 流程名称
     */
    @Column(name = "flow_name", length = 64, nullable = false)
    private String flowName;
    
    /**
     * 业务类型：APPROVAL/EXPENSE_REIMBURSE/LOAN/BUDGET_ADJUST
     */
    @Column(name = "business_type", length = 32, nullable = false)
    private String businessType;
    
    /**
     * 分类或类别
     */
    @Column(name = "category", length = 50)
    private String category;
    
    /**
     * creator_id
     */
    @Column(name = "creator_id")
    private Long creatorId;
    
    /**
     * 适用最小金额
     */
    @Column(name = "min_amount", precision = 18, scale = 2)
    private BigDecimal minAmount;
    
    /**
     * 适用最大金额
     */
    @Column(name = "max_amount", precision = 18, scale = 2)
    private BigDecimal maxAmount;
    
    /**
     * 适用部门ID（空表示全部门）
     */
    @Column(name = "dept_id")
    private Long deptId;
    
    /**
     * 适用费用类型ID（空表示全类型）
     */
    @Column(name = "fee_type_id")
    private Long feeTypeId;
    
    /**
     * 流程定义JSON（节点/审批人/条件）
     */
    @Column(name = "flow_json", columnDefinition = "text", nullable = false)
    private String flowJson;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 流程描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
    /**
     * 流程版本号
     */
    @Column(name = "version", nullable = false)
    private Integer version = 1;
    
    /**
     * 适用优先级（数字越小优先级越高）
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 100;

    // Getters and Setters
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

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
    
    /**
     * 设置业务类型枚举
     */
    public void setBusinessTypeEnum(BusinessTypeEnum businessTypeEnum) {
        this.businessType = businessTypeEnum != null ? businessTypeEnum.getCode() : null;
    }
    
    /**
     * 获取业务类型枚举
     */
    public BusinessTypeEnum getBusinessTypeEnum() {
        return BusinessTypeEnum.getByCode(this.businessType);
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getFeeTypeId() {
        return feeTypeId;
    }

    public void setFeeTypeId(Long feeTypeId) {
        this.feeTypeId = feeTypeId;
    }

    public String getFlowJson() {
        return flowJson;
    }

    public void setFlowJson(String flowJson) {
        this.flowJson = flowJson;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "ApprovalFlowConfig{" +
                "id=" + getId() +
                ", flowCode='" + flowCode + '\'' +
                ", flowName='" + flowName + '\'' +
                ", businessType='" + businessType + '\'' +
                ", minAmount=" + minAmount +
                ", maxAmount=" + maxAmount +
                ", deptId=" + deptId +
                ", feeTypeId=" + feeTypeId +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", version=" + version +
                ", priority=" + priority +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}