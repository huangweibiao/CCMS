package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

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
    
    // Getters and Setters
    public String getBudgetYear() {
        return budgetYear;
    }
    
    public void setBudgetYear(String budgetYear) {
        this.budgetYear = budgetYear;
    }
    
    public String getBudgetName() {
        return budgetName;
    }
    
    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }
    
    public Long getDeptId() {
        return deptId;
    }
    
    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }
    
    public Long getBudgetCategoryId() {
        return budgetCategoryId;
    }
    
    public void setBudgetCategoryId(Long budgetCategoryId) {
        this.budgetCategoryId = budgetCategoryId;
    }
    
    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }
    
    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }
    
    public BigDecimal getActualAmount() {
        return actualAmount;
    }
    
    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }
    
    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }
    
    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public String getBudgetStatus() {
        return budgetStatus;
    }
    
    public void setBudgetStatus(String budgetStatus) {
        this.budgetStatus = budgetStatus;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getApproverId() {
        return approverId;
    }
    
    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }
    
    public String getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public LocalDate getApprovalTime() {
        return approvalTime;
    }
    
    public void setApprovalTime(LocalDate approvalTime) {
        this.approvalTime = approvalTime;
    }
    
    public Long getParentId() {
        return parentId;
    }
    
    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
    
    public Boolean getIsLeaf() {
        return isLeaf;
    }
    
    public void setIsLeaf(Boolean isLeaf) {
        this.isLeaf = isLeaf;
    }
}