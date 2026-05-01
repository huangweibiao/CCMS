package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 借款单表实体类
 * 对应表名：ccms_loan_main
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "loan_main")
public class LoanMain extends BaseEntity {

    /**
     * 借款单号
     */
    @Column(name = "loan_no", length = 64, nullable = false)
    private String loanNo;
    
    /**
     * 关联申请单ID
     */
    @Column(name = "apply_id")
    private Long applyId;
    
    /**
     * 借款人ID
     */
    @Column(name = "loan_user_id", nullable = false)
    private Long loanUserId;
    
    /**
     * 借款部门ID
     */
    @Column(name = "loan_dept_id", nullable = false)
    private Long loanDeptId;
    
    /**
     * 借款总额
     */
    @Column(name = "loan_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal loanAmount;
    
    /**
     * 已还金额
     */
    @Column(name = "repaid_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal repaidAmount = new BigDecimal("0.00");
    
    /**
     * 借款余额 - 计算字段，不直接存储
     */
    @Transient
    private BigDecimal balanceAmount;
    
    /**
     * 预期还款日期
     */
    @Column(name = "expect_repay_date", nullable = false)
    private LocalDate expectRepayDate;
    
    /**
     * 实际还款日期
     */
    @Column(name = "actual_repay_date")
    private LocalDate actualRepayDate;
    
    /**
     * 状态：1-借款中 2-部分核销 3-已核销 4-逾期 5-作废
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 用途
     */
    @Column(name = "purpose", length = 512)
    private String purpose;
    
    /**
     * 审批结果
     */
    @Column(name = "approval_result", length = 512)
    private String approvalResult;
    
    /**
     * 银行名称
     */
    @Column(name = "bank_name", length = 255)
    private String bankName;
    
    /**
     * 银行账号
     */
    @Column(name = "bank_account", length = 100)
    private String bankAccount;

    // Getters and Setters
    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
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

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    /**
     * 计算借款余额（借款金额 - 已还金额）
     */
    public BigDecimal getBalanceAmount() {
        if (loanAmount == null) {
            return BigDecimal.ZERO;
        }
        if (repaidAmount == null) {
            return loanAmount;
        }
        return loanAmount.subtract(repaidAmount);
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        // 这是一个计算字段，不需要setter，但为了兼容性保留
    }

    public LocalDate getExpectRepayDate() {
        return expectRepayDate;
    }

    public void setExpectRepayDate(LocalDate expectRepayDate) {
        this.expectRepayDate = expectRepayDate;
    }

    public LocalDate getActualRepayDate() {
        return actualRepayDate;
    }

    public void setActualRepayDate(LocalDate actualRepayDate) {
        this.actualRepayDate = actualRepayDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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
        return "LoanMain{" +
                "id=" + getId() +
                ", loanNo='" + loanNo + '\'' +
                ", applyId=" + applyId +
                ", loanUserId=" + loanUserId +
                ", loanDeptId=" + loanDeptId +
                ", loanAmount=" + loanAmount +
                ", repaidAmount=" + repaidAmount +
                ", balanceAmount=" + getBalanceAmount() +
                ", expectRepayDate=" + expectRepayDate +
                ", actualRepayDate=" + actualRepayDate +
                ", status=" + status +
                ", purpose='" + purpose + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}