package com.ccms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 还款请求DTO
 */
public class RepaymentRequest {
    
    private Long id;
    private Long loanId;
    private String repayNo;
    private BigDecimal repayAmount;
    private LocalDate repayDate;
    private Integer repayType;
    private Long repayBy;
    private String bankName;
    private String bankAccount;
    private String remark;

    // 默认构造函数
    public RepaymentRequest() {}

    // 带参构造函数
    public RepaymentRequest(Long loanId, String repayNo, BigDecimal repayAmount, LocalDate repayDate, Integer repayType) {
        this.loanId = loanId;
        this.repayNo = repayNo;
        this.repayAmount = repayAmount;
        this.repayDate = repayDate;
        this.repayType = repayType;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public String getRepayNo() {
        return repayNo;
    }

    public void setRepayNo(String repayNo) {
        this.repayNo = repayNo;
    }

    public BigDecimal getRepayAmount() {
        return repayAmount;
    }

    public void setRepayAmount(BigDecimal repayAmount) {
        this.repayAmount = repayAmount;
    }

    public LocalDate getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(LocalDate repayDate) {
        this.repayDate = repayDate;
    }

    public Integer getRepayType() {
        return repayType;
    }

    public void setRepayType(Integer repayType) {
        this.repayType = repayType;
    }

    public Long getRepayBy() {
        return repayBy;
    }

    public void setRepayBy(Long repayBy) {
        this.repayBy = repayBy;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "RepaymentRequest{" +
                "id=" + id +
                ", loanId=" + loanId +
                ", repayNo='" + repayNo + '\'' +
                ", repayAmount=" + repayAmount +
                ", repayDate=" + repayDate +
                ", repayType=" + repayType +
                ", repayBy=" + repayBy +
                ", bankName='" + bankName + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}