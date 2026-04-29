package com.ccms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 借款详情响应DTO
 */
public class LoanResponse {
    
    private Long id;
    private Long applyId;
    private String loanNo;
    private Long loanUserId;
    private String loanUserName;
    private Long loanDeptId;
    private String loanDeptName;
    private BigDecimal loanAmount;
    private LocalDate expectRepayDate;
    private String purpose;
    private Integer status;
    private String statusText;
    private BigDecimal remainAmount;
    private BigDecimal repaidAmount;
    private BigDecimal balanceAmount;
    private LocalDate actualRepayDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String approvalStatus;
    private String approvalResult;
    private String bankName;
    private String bankAccount;

    // 默认构造函数
    public LoanResponse() {}

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

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public Long getLoanUserId() {
        return loanUserId;
    }

    public void setLoanUserId(Long loanUserId) {
        this.loanUserId = loanUserId;
    }

    public String getLoanUserName() {
        return loanUserName;
    }

    public void setLoanUserName(String loanUserName) {
        this.loanUserName = loanUserName;
    }

    public Long getLoanDeptId() {
        return loanDeptId;
    }

    public void setLoanDeptId(Long loanDeptId) {
        this.loanDeptId = loanDeptId;
    }

    public String getLoanDeptName() {
        return loanDeptName;
    }

    public void setLoanDeptName(String loanDeptName) {
        this.loanDeptName = loanDeptName;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public BigDecimal getRemainAmount() {
        return remainAmount;
    }

    public void setRemainAmount(BigDecimal remainAmount) {
        this.remainAmount = remainAmount;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public LocalDate getActualRepayDate() {
        return actualRepayDate;
    }

    public void setActualRepayDate(LocalDate actualRepayDate) {
        this.actualRepayDate = actualRepayDate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalResult() {
        return approvalResult;
    }

    public void setApprovalResult(String approvalResult) {
        this.approvalResult = approvalResult;
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
        return "LoanResponse{" +
                "id=" + id +
                ", applyId=" + applyId +
                ", loanNo='" + loanNo + '\'' +
                ", loanUserId=" + loanUserId +
                ", loanUserName='" + loanUserName + '\'' +
                ", loanDeptId=" + loanDeptId +
                ", loanDeptName='" + loanDeptName + '\'' +
                ", loanAmount=" + loanAmount +
                ", expectRepayDate=" + expectRepayDate +
                ", purpose='" + purpose + '\'' +
                ", status=" + status +
                ", statusText='" + statusText + '\'' +
                ", remainAmount=" + remainAmount +
                ", repaidAmount=" + repaidAmount +
                ", actualRepayDate=" + actualRepayDate +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", approvalStatus='" + approvalStatus + '\'' +
                ", approvalResult='" + approvalResult + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                '}';
    }
}