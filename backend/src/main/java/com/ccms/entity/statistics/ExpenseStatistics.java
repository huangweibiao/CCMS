package com.ccms.entity.statistics;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 费用统计报表实体类
 */
@Entity
@Table(name = "expense_statistics")
public class ExpenseStatistics extends BaseEntity {

    @Column(nullable = false)
    private LocalDate statDate;

    @Column(length = 20)
    private String statPeriod; // DAY, WEEK, MONTH, YEAR

    private Long departmentId;

    private String departmentName;

    private String expenseTypeCode;

    private String expenseTypeName;

    @Column(precision = 15, scale = 2)
    private BigDecimal totalAmount;

    private Integer applyCount;

    private Integer reimburseCount;

    @Column(precision = 15, scale = 2)
    private BigDecimal budgetAmount;

    @Column(precision = 15, scale = 2)
    private BigDecimal budgetUsed;

    @Column(precision = 5, scale = 2)
    private BigDecimal budgetUsageRate;

    private Integer approvalPassCount;

    private Integer approvalRejectCount;

    @Column(precision = 5, scale = 2)
    private BigDecimal approvalPassRate;

    // 构造器
    public ExpenseStatistics() {}

    public ExpenseStatistics(LocalDate statDate, String statPeriod) {
        this.statDate = statDate;
        this.statPeriod = statPeriod;
    }

    // Getters and Setters
    public LocalDate getStatDate() { return statDate; }
    public void setStatDate(LocalDate statDate) { this.statDate = statDate; }

    public String getStatPeriod() { return statPeriod; }
    public void setStatPeriod(String statPeriod) { this.statPeriod = statPeriod; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getExpenseTypeCode() { return expenseTypeCode; }
    public void setExpenseTypeCode(String expenseTypeCode) { this.expenseTypeCode = expenseTypeCode; }

    public String getExpenseTypeName() { return expenseTypeName; }
    public void setExpenseTypeName(String expenseTypeName) { this.expenseTypeName = expenseTypeName; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getApplyCount() { return applyCount; }
    public void setApplyCount(Integer applyCount) { this.applyCount = applyCount; }

    public Integer getReimburseCount() { return reimburseCount; }
    public void setReimburseCount(Integer reimburseCount) { this.reimburseCount = reimburseCount; }

    public BigDecimal getBudgetAmount() { return budgetAmount; }
    public void setBudgetAmount(BigDecimal budgetAmount) { this.budgetAmount = budgetAmount; }

    public BigDecimal getBudgetUsed() { return budgetUsed; }
    public void setBudgetUsed(BigDecimal budgetUsed) { this.budgetUsed = budgetUsed; }

    public BigDecimal getBudgetUsageRate() { return budgetUsageRate; }
    public void setBudgetUsageRate(BigDecimal budgetUsageRate) { this.budgetUsageRate = budgetUsageRate; }

    public Integer getApprovalPassCount() { return approvalPassCount; }
    public void setApprovalPassCount(Integer approvalPassCount) { this.approvalPassCount = approvalPassCount; }

    public Integer getApprovalRejectCount() { return approvalRejectCount; }
    public void setApprovalRejectCount(Integer approvalRejectCount) { this.approvalRejectCount = approvalRejectCount; }

    public BigDecimal getApprovalPassRate() { return approvalPassRate; }
    public void setApprovalPassRate(BigDecimal approvalPassRate) { this.approvalPassRate = approvalPassRate; }

    /**
     * 计算预算使用率
     */
    public void calculateBudgetUsageRate() {
        if (budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.budgetUsageRate = budgetUsed
                    .divide(budgetAmount, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        } else {
            this.budgetUsageRate = BigDecimal.ZERO;
        }
    }

    /**
     * 计算审批通过率
     */
    public void calculateApprovalPassRate() {
        int totalApproval = approvalPassCount + approvalRejectCount;
        if (totalApproval > 0) {
            this.approvalPassRate = BigDecimal.valueOf(approvalPassCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalApproval), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            this.approvalPassRate = BigDecimal.ZERO;
        }
    }
}