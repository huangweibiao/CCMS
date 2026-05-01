package com.ccms.entity.fee;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 费用类型表实体类
 * 对应表名：ccms_fee_type
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_fee_type")
public class FeeType extends BaseEntity {

    /**
     * 费用类型编码
     */
    @Column(name = "type_code", length = 64, nullable = false, unique = true)
    private String typeCode;
    
    /**
     * 费用类型名称
     */
    @Column(name = "type_name", length = 100, nullable = false)
    private String typeName;
    
    /**
     * 费用类型描述
     */
    @Column(name = "type_desc", length = 500)
    private String typeDesc;
    
    /**
     * 启用状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;
    
    /**
     * 费用类别：1-日常费用 2-差旅费用 3-业务招待 4-采购费用 5-其他
     */
    @Column(name = "category", nullable = false)
    private Integer category;
    
    /**
     * 预算控制标识：0-不控制 1-控制
     */
    @Column(name = "budget_control_flag", nullable = false)
    private Integer budgetControlFlag = 0;
    
    /**
     * 发票需求标识：0-不需要 1-需要
     */
    @Column(name = "invoice_require_flag", nullable = false)
    private Integer invoiceRequireFlag = 0;
    
    /**
     * 排序号
     */
    @Column(name = "sort_no")
    private Integer sortNo;
    
    /**
     * 父级类型ID（用于费用类型层级）
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 是否系统预设：0-用户自定义 1-系统预设
     */
    @Column(name = "is_system_preset", nullable = false)
    private Integer isSystemPreset = 0;
    
    /**
     * 创建人ID
     */
    @Column(name = "create_by", nullable = false)
    private Long createBy;
    
    /**
     * 更新人ID
     */
    @Column(name = "update_by", nullable = false)
    private Long updateBy;

    // Getters and Setters
    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeDesc() {
        return typeDesc;
    }

    public void setTypeDesc(String typeDesc) {
        this.typeDesc = typeDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getBudgetControlFlag() {
        return budgetControlFlag;
    }

    public void setBudgetControlFlag(Integer budgetControlFlag) {
        this.budgetControlFlag = budgetControlFlag;
    }

    public Integer getInvoiceRequireFlag() {
        return invoiceRequireFlag;
    }

    public void setInvoiceRequireFlag(Integer invoiceRequireFlag) {
        this.invoiceRequireFlag = invoiceRequireFlag;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getIsSystemPreset() {
        return isSystemPreset;
    }

    public void setIsSystemPreset(Integer isSystemPreset) {
        this.isSystemPreset = isSystemPreset;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    // 业务逻辑方法
    /**
     * 判断是否需要预算控制
     */
    @Transient
    public boolean isBudgetControlRequired() {
        return this.budgetControlFlag != null && this.budgetControlFlag == 1;
    }

    /**
     * 判断是否需要发票
     */
    @Transient
    public boolean isInvoiceRequired() {
        return this.invoiceRequireFlag != null && this.invoiceRequireFlag == 1;
    }

    /**
     * 判断是否启用
     */
    @Transient
    public boolean isEnabled() {
        return this.status != null && this.status == 1;
    }

    /**
     * 判断是否系统预设
     */
    @Transient
    public boolean isSystemPreset() {
        return this.isSystemPreset != null && this.isSystemPreset == 1;
    }

    /**
     * 使用费用类型编码生成显示文本
     */
    @Transient
    public String getDisplayText() {
        return this.typeCode + " - " + this.typeName;
    }

    @Override
    public String toString() {
        return "FeeType{" +
                "id=" + getId() +
                ", typeCode='" + typeCode + '\'' +
                ", typeName='" + typeName + '\'' +
                ", budgetControlFlag=" + budgetControlFlag +
                ", invoiceRequireFlag=" + invoiceRequireFlag +
                ", status=" + status +
                ", category=" + category +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}