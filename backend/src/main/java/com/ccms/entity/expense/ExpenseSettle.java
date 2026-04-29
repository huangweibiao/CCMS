package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_settle")
public class ExpenseSettle extends BaseEntity {
    
    @Column(name = "settle_no", nullable = false, unique = true, length = 50)
    private String settleNo;
    
    @Column(name = "expense_apply_id", nullable = false)
    private Long expenseApplyId;
    
    @Column(name = "apply_no", length = 50)
    private String applyNo;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "apply_user_id", nullable = false)
    private Long applyUserId;
    
    @Column(name = "apply_user_name", nullable = false, length = 100)
    private String applyUserName;
    
    @Column(name = "dept_id")
    private Long deptId;
    
    @Column(name = "dept_name", length = 100)
    private String deptName;
    
    @Column(name = "settle_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal settleAmount;
    
    @Column(name = "currency", length = 10)
    private String currency = "CNY";
    
    @Column(name = "settle_date", nullable = false)
    private LocalDate settleDate;
    
    @Column(name = "status", nullable = false)
    private Integer status; // 0-草稿 1-已提交 2-结算中 3-已完成 4-已取消
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "bank_account", length = 100)
    private String bankAccount;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column(name = "complete_time")
    private LocalDateTime completeTime;
    
    @Column(name = "cancel_time")
    private LocalDateTime cancelTime;
    
    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
    
    @Column(name = "handler_id")
    private Long handlerId;
    
    @Column(name = "handler_name", length = 100)
    private String handlerName;
    
    @Column(name = "remark", length = 1000)
    private String remark;

    // Manual getters and setters
    public String getSettleNo() {
        return settleNo;
    }

    public void setSettleNo(String settleNo) {
        this.settleNo = settleNo;
    }

    public Long getExpenseApplyId() {
        return expenseApplyId;
    }

    public void setExpenseApplyId(Long expenseApplyId) {
        this.expenseApplyId = expenseApplyId;
    }

    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public BigDecimal getSettleAmount() {
        return settleAmount;
    }

    public void setSettleAmount(BigDecimal settleAmount) {
        this.settleAmount = settleAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDate getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(LocalDate settleDate) {
        this.settleDate = settleDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public LocalDateTime getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(LocalDateTime completeTime) {
        this.completeTime = completeTime;
    }

    public LocalDateTime getCancelTime() {
        return cancelTime;
    }

    public void setCancelTime(LocalDateTime cancelTime) {
        this.cancelTime = cancelTime;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(Long handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}