package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ccms_reimburse_item")
public class ReimburseItem extends BaseEntity {
    
    @Column(name = "reimburse_main_id", nullable = false)
    private Long reimburseMainId;
    
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
    
    @Column(name = "unit_price", precision = 15, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;
    
    @Column(name = "quantity")
    private Integer quantity = 1;
    
    @Column(name = "expense_reimburse_id", nullable = false)
    private Long expenseReimburseId = 0L;
    
    @Column(name = "expense_type", length = 100)
    private String expenseType;
    
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

    // Getter and Setter methods
    public Long getReimburseMainId() {
        return reimburseMainId;
    }

    public void setReimburseMainId(Long reimburseMainId) {
        this.reimburseMainId = reimburseMainId;
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

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getExpenseReimburseId() {
        return expenseReimburseId;
    }

    public void setExpenseReimburseId(Long expenseReimburseId) {
        this.expenseReimburseId = expenseReimburseId;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
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