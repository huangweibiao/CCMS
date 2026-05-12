package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;

@Entity
@Table(name = "ccms_budget_category")
public class BudgetCategory extends BaseEntity {
    
    @Column(name = "category_code", nullable = false, unique = true, length = 50)
    private String categoryCode;
    
    @Column(name = "category_name", nullable = false, length = 200)
    private String categoryName;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "category_level", nullable = false)
    private Integer categoryLevel = 1;
    
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    @Column(name = "is_enabled", columnDefinition = "tinyint(1) default 1")
    private Boolean enabled = true;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "budget_cycle", length = 20)
    private String budgetCycle = "MONTHLY"; // MONTHLY, QUARTERLY, YEARLY
    
    @Column(name = "is_system", columnDefinition = "tinyint(1) default 0")
    private Boolean system = false;

    // Manual getters and setters
    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getCategoryLevel() {
        return categoryLevel;
    }

    public void setCategoryLevel(Integer categoryLevel) {
        this.categoryLevel = categoryLevel;
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

    public String getBudgetCycle() {
        return budgetCycle;
    }

    public void setBudgetCycle(String budgetCycle) {
        this.budgetCycle = budgetCycle;
    }

    public Boolean getSystem() {
        return system;
    }

    public void setSystem(Boolean system) {
        this.system = system;
    }
}