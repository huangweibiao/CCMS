package com.ccms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 还款详情响应DTO
 */
public class RepaymentResponse {
    
    private Long id;
    private Long loanId;
    private String loanNo;
    private String repayNo;
    private BigDecimal repayAmount;
    private LocalDate repayDate;
    private Integer repayType;
    private String repayTypeText;
    private Integer status;
    private String statusText;
    private String bankName;
    private String bankAccount;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long userId;
    private String userName;

    // 默认构造函数
    public RepaymentResponse() {}

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

    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
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

    public String getRepayTypeText() {
        return repayTypeText;
    }

    public void setRepayTypeText(String repayTypeText) {
        this.repayTypeText = repayTypeText;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "RepaymentResponse{" +
                "id=" + id +
                ", loanId=" + loanId +
                ", loanNo='" + loanNo + '\'' +
                ", repayNo='" + repayNo + '\'' +
                ", repayAmount=" + repayAmount +
                ", repayDate=" + repayDate +
                ", repayType='" + repayType + '\'' +
                ", repayTypeText='" + repayTypeText + '\'' +
                ", status=" + status +
                ", statusText='" + statusText + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankAccount='" + bankAccount + '\'' +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }
}