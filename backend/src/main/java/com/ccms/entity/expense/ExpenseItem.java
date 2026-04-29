package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expense_item")
public class ExpenseItem extends BaseEntity {
    
    @Column(name = "expense_apply_id", nullable = false)
    private Long expenseApplyId;
    
    @Column(name = "item_no", nullable = false)
    private Integer itemNo;
    
    @Column(name = "expense_type_id")
    private Long expenseTypeId;
    
    @Column(name = "expense_type_name", length = 100)
    private String expenseTypeName;
    
    @Column(name = "description", nullable = false, length = 500)
    private String description;
    
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "occur_date", nullable = false)
    private LocalDate occurDate;
    
    @Column(name = "project_code", length = 50)
    private String projectCode;
    
    @Column(name = "project_name", length = 200)
    private String projectName;
    
    @Column(name = "cost_center", length = 50)
    private String costCenter;
    
    @Column(name = "cost_center_name", length = 200)
    private String costCenterName;
    
    @Column(name = "budget_item_id")
    private Long budgetItemId;
    
    @Column(name = "budget_item_name", length = 200)
    private String budgetItemName;
    
    @Column(name = "budget_available_amount", precision = 15, scale = 2)
    private BigDecimal budgetAvailableAmount;
    
    @Column(name = "currency", length = 10)
    private String currency = "CNY";
    
    @Column(name = "is_budget_checked", columnDefinition = "tinyint(1) default 0")
    private Boolean budgetChecked = false;
    
    @Column(name = "remark", length = 500)
    private String remark;
    
    @Transient
    private String expenseType;
    
    @Transient
    private Integer quantity;
    
    @Transient
    private Double unitPrice;
    
    @Transient
    private BigDecimal unitPriceAmount; // 用于BigDecimal计算
    
    // Getters and Setters
    public Long getExpenseApplyId() {
        return expenseApplyId;
    }
    
    public void setExpenseApplyId(Long expenseApplyId) {
        this.expenseApplyId = expenseApplyId;
    }
    
    public Integer getItemNo() {
        return itemNo;
    }
    
    public void setItemNo(Integer itemNo) {
        this.itemNo = itemNo;
    }
    
    public Long getExpenseTypeId() {
        return expenseTypeId;
    }
    
    public void setExpenseTypeId(Long expenseTypeId) {
        this.expenseTypeId = expenseTypeId;
    }
    
    public String getExpenseTypeName() {
        return expenseTypeName;
    }
    
    public void setExpenseTypeName(String expenseTypeName) {
        this.expenseTypeName = expenseTypeName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public LocalDate getOccurDate() {
        return occurDate;
    }
    
    public void setOccurDate(LocalDate occurDate) {
        this.occurDate = occurDate;
    }
    
    public String getProjectCode() {
        return projectCode;
    }
    
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getCostCenter() {
        return costCenter;
    }
    
    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }
    
    public String getCostCenterName() {
        return costCenterName;
    }
    
    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
    }
    
    public Long getBudgetItemId() {
        return budgetItemId;
    }
    
    public void setBudgetItemId(Long budgetItemId) {
        this.budgetItemId = budgetItemId;
    }
    
    public String getBudgetItemName() {
        return budgetItemName;
    }
    
    public void setBudgetItemName(String budgetItemName) {
        this.budgetItemName = budgetItemName;
    }
    
    public BigDecimal getBudgetAvailableAmount() {
        return budgetAvailableAmount;
    }
    
    public void setBudgetAvailableAmount(BigDecimal budgetAvailableAmount) {
        this.budgetAvailableAmount = budgetAvailableAmount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Boolean getBudgetChecked() {
        return budgetChecked;
    }
    
    public void setBudgetChecked(Boolean budgetChecked) {
        this.budgetChecked = budgetChecked;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    public String getExpenseType() {
        return expenseType;
    }
    
    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
        if (unitPrice != null) {
            this.unitPriceAmount = BigDecimal.valueOf(unitPrice);
        }
    }

    public BigDecimal getUnitPriceAmount() {
        return unitPriceAmount;
    }

    public void setUnitPriceAmount(BigDecimal unitPriceAmount) {
        this.unitPriceAmount = unitPriceAmount;
        if (unitPriceAmount != null) {
            this.unitPrice = unitPriceAmount.doubleValue();
        }
    }
}