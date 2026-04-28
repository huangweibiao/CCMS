package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "budget")
public class Budget extends BaseEntity {
    
    @Column(name = "budget_year", nullable = false)
    private String budgetYear;
    
    @Column(name = "budget_name", nullable = false)
    private String budgetName;
    
    @Column(name = "dept_id")
    private Long deptId;
    
    @Column(name = "budget_category_id")
    private Long budgetCategoryId;
    
    @Column(name = "budget_amount", precision = 15, scale = 2)
    private BigDecimal budgetAmount;
    
    @Column(name = "actual_amount", precision = 15, scale = 2)
    private BigDecimal actualAmount;
    
    @Column(name = "remaining_amount", precision = 15, scale = 2)
    private BigDecimal remainingAmount;
    
    @Column(name = "start_date")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "budget_status", length = 20)
    private String budgetStatus;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "approver_id")
    private Long approverId;
    
    @Column(name = "approval_status", length = 20)
    private String approvalStatus;
    
    @Column(name = "approval_time")
    private LocalDate approvalTime;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "is_leaf")
    private Boolean isLeaf;
}