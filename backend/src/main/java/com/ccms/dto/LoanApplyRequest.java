package com.ccms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 借款申请请求DTO
 */
public class LoanApplyRequest {
    
    private Long id;
    private Long applyId;
    private Long loanUserId;
    private Long loanDeptId;
    private BigDecimal loanAmount;
    private LocalDate expectRepayDate;
    private String purpose;
    private String bankName;
    private String bankAccount;

    // 默认构造函数
    public LoanApplyRequest() {}

    // 带参构造函数
    public LoanApplyRequest(Long loanUserId, Long loanDeptId, BigDecimal loanAmount, 
                           LocalDate expectRepayDate, String purpose) {
        this.loanUserId = loanUserId;
        this.loanDeptId = loanDeptId;
        this.loanAmount = loanAmount;
        this.expectRepayDate = expectRepayDate;
        this.purpose = purpose;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Long getLoanUserId() {
        return loanUserId;
    }

    public void setLoanUserId(Long loanUserId) {
        this.loanUserId = loanUserId;
    }

    public Long getLoanDeptId() {
        return loanDeptId;
    }

    public void setLoanDeptId(Long loanDeptId) {
        this.loanDeptId = loanDeptId;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public LocalDate getExpectRepayDate() {
        return expectRepayDate;
    }

    public void setExpectRepayDate(LocalDate expectRepayDate) {
        this.expectRepayDate = expectRepayDate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    @Override
    public String toString() {
        return "LoanApplyRequest{" +
                "id=" + id +
                ", applyId=" + applyId +
                ", loanUserId=" + loanUserId +
                ", loanDeptId=" + loanDeptId +
                ", loanAmount=" + loanAmount +
                ", expectRepayDate=" + expectRepayDate +
                ", purpose='" + purpose + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                '}';
    }
}