package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "budget_category")
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
}