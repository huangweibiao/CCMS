package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Entity
@Table(name = "expense_type")
public class ExpenseType extends BaseEntity {
    
    @Column(name = "type_code", nullable = false, unique = true, length = 50)
    private String typeCode;
    
    @Column(name = "type_name", nullable = false, length = 200)
    private String typeName;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "type_level", nullable = false)
    private Integer typeLevel = 1;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Column(name = "is_enabled", columnDefinition = "tinyint(1) default 1")
    private Boolean enabled = true;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "need_approval", columnDefinition = "tinyint(1) default 0")
    private Boolean needApproval = false;
    
    @Column(name = "approval_threshold", precision = 15, scale = 2)
    private Double approvalThreshold;
    
    @Column(name = "budget_category_id")
    private Long budgetCategoryId;
    
    @Column(name = "budget_category_name", length = 200)
    private String budgetCategoryName;
    
    @Column(name = "is_system", columnDefinition = "tinyint(1) default 0")
    private Boolean system = false;
    
    @Column(name = "create_user")
    private Long createUser;
    
    @Column(name = "update_user")
    private Long updateUser;

    // Getter and Setter methods
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

    public Integer getTypeLevel() {
        return typeLevel;
    }

    public void setTypeLevel(Integer typeLevel) {
        this.typeLevel = typeLevel;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getNeedApproval() {
        return needApproval;
    }

    public void setNeedApproval(Boolean needApproval) {
        this.needApproval = needApproval;
    }

    public Double getApprovalThreshold() {
        return approvalThreshold;
    }

    public void setApprovalThreshold(Double approvalThreshold) {
        this.approvalThreshold = approvalThreshold;
    }

    public Long getBudgetCategoryId() {
        return budgetCategoryId;
    }

    public void setBudgetCategoryId(Long budgetCategoryId) {
        this.budgetCategoryId = budgetCategoryId;
    }

    public String getBudgetCategoryName() {
        return budgetCategoryName;
    }

    public void setBudgetCategoryName(String budgetCategoryName) {
        this.budgetCategoryName = budgetCategoryName;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(Long updateUser) {
        this.updateUser = updateUser;
    }
}