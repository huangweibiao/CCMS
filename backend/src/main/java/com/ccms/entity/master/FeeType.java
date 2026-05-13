package com.ccms.entity.master;

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
     * 类型编码
     */
    @Column(name = "type_code", length = 32, nullable = false)
    private String typeCode;
    
    /**
     * 类型名称（差旅/餐饮/交通/办公等）
     */
    @Column(name = "type_name", length = 64, nullable = false)
    private String typeName;
    
    /**
     * 上级类型ID
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 是否预算控制：0-否 1-是
     */
    @Column(name = "budget_control", nullable = false)
    private Integer budgetControl;
    
    /**
     * 是否需发票：0-否 1-是
     */
    @Column(name = "need_invoice", nullable = false)
    private Integer needInvoice;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    /**
     * 类别
     */
    @Column(name = "category")
    private Integer category;

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

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getBudgetControl() {
        return budgetControl;
    }

    public void setBudgetControl(Integer budgetControl) {
        this.budgetControl = budgetControl;
    }

    public Integer getNeedInvoice() {
        return needInvoice;
    }

    public void setNeedInvoice(Integer needInvoice) {
        this.needInvoice = needInvoice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "FeeType{" +
                "id=" + getId() +
                ", typeCode='" + typeCode + '\'' +
                ", typeName='" + typeName + '\'' +
                ", parentId=" + parentId +
                ", budgetControl=" + budgetControl +
                ", needInvoice=" + needInvoice +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                ", category=" + category +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}
