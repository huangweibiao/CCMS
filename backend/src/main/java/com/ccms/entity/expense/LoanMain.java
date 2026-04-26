package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 借款单表实体类
 * 对应表名：ccms_loan_main
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_loan_main")
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
     * 已核销金额
     */
    @Column(name = "verified_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal verifiedAmount;
    
    /**
     * 借款余额
     */
    @Column(name = "balance_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal balanceAmount;
    
    /**
     * 预期还款日期
     */
    @Column(name = "expect_repay_date", nullable = false)
    private Date expectRepayDate;
    
    /**
     * 实际还款日期
     */
    @Column(name = "actual_repay_date")
    private Date actualRepayDate;
    
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

    public BigDecimal getVerifiedAmount() {
        return verifiedAmount;
    }

    public void setVerifiedAmount(BigDecimal verifiedAmount) {
        this.verifiedAmount = verifiedAmount;
    }

    public BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public Date getExpectRepayDate() {
        return expectRepayDate;
    }

    public void setExpectRepayDate(Date expectRepayDate) {
        this.expectRepayDate = expectRepayDate;
    }

    public Date getActualRepayDate() {
        return actualRepayDate;
    }

    public void setActualRepayDate(Date actualRepayDate) {
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

    @Override
    public String toString() {
        return "LoanMain{" +
                "id=" + getId() +
                ", loanNo='" + loanNo + '\'' +
                ", applyId=" + applyId +
                ", loanUserId=" + loanUserId +
                ", loanDeptId=" + loanDeptId +
                ", loanAmount=" + loanAmount +
                ", verifiedAmount=" + verifiedAmount +
                ", balanceAmount=" + balanceAmount +
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