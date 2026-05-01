package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 报销单明细表实体类
 * 对应表名：expense_reimburse_detail
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "expense_reimburse_detail")
public class ExpenseReimburseDetail extends BaseEntity {

    /**
     * 报销单ID
     */
    @Column(name = "reimburse_id", nullable = false)
    private Long reimburseId;
    
    /**
     * 费用类型ID
     */
    @Column(name = "fee_type_id", nullable = false)
    private Long feeTypeId;
    
    /**
     * 费用发生日期
     */
    @Column(name = "expense_date", nullable = false)
    private Date expenseDate;
    
    /**
     * 金额
     */
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 数量
     */
    @Column(name = "quantity")
    private Integer quantity;
    
    /**
     * 单价
     */
    @Column(name = "unit_price", precision = 18, scale = 2)
    private BigDecimal unitPrice;
    
    /**
     * 费用说明
     */
    @Column(name = "description", length = 256)
    private String description;
    
    /**
     * 发票号码
     */
    @Column(name = "invoice_no", length = 64)
    private String invoiceNo;
    
    /**
     * 关联预算ID
     */
    @Column(name = "budget_id")
    private Long budgetId;
    
    /**
     * 借款抵扣金额
     */
    @Column(name = "loan_deduct_amount", precision = 18, scale = 2)
    private BigDecimal loanDeductAmount;

    // Getters and Setters
    public Long getReimburseId() {
        return reimburseId;
    }

    public void setReimburseId(Long reimburseId) {
        this.reimburseId = reimburseId;
    }

    public Long getFeeTypeId() {
        return feeTypeId;
    }

    public void setFeeTypeId(Long feeTypeId) {
        this.feeTypeId = feeTypeId;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public BigDecimal getLoanDeductAmount() {
        return loanDeductAmount;
    }

    public void setLoanDeductAmount(BigDecimal loanDeductAmount) {
        this.loanDeductAmount = loanDeductAmount;
    }

    @Override
    public String toString() {
        return "ExpenseReimburseDetail{" +
                "id=" + getId() +
                ", reimburseId=" + reimburseId +
                ", feeTypeId=" + feeTypeId +
                ", expenseDate=" + expenseDate +
                ", amount=" + amount +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", description='" + description + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", budgetId=" + budgetId +
                ", loanDeductAmount=" + loanDeductAmount +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}