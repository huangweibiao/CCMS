package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ccms_expense_detail")
public class ExpenseDetail extends BaseEntity {
    
    @Column(name = "expense_main_id", nullable = false)
    private Long expenseMainId;
    
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
    
    @Column(name = "currency", length = 10)
    private String currency = "CNY";
    
    @Column(name = "remark", length = 500)
    private String remark;
    
    @Column(name = "is_approved", columnDefinition = "tinyint(1) default 0")
    private Boolean approved = false;
    
    @Column(name = "budget_item_id")
    private Long budgetItemId;
    
    @Column(name = "budget_item_name", length = 200)
    private String budgetItemName;

    // Manual getters and setters
    public Long getExpenseMainId() {
        return expenseMainId;
    }

    public void setExpenseMainId(Long expenseMainId) {
        this.expenseMainId = expenseMainId;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
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
}