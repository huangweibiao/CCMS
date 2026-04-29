package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 还款记录表实体类
 * 对应表名：ccms_repayment_record
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_repayment_record")
public class RepaymentRecord extends BaseEntity {

    /**
     * 借款单ID
     */
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    
    /**
     * 关联报销单ID
     */
    @Column(name = "reimburse_id")
    private Long reimburseId;
    
    /**
     * 还款金额
     */
    @Column(name = "repay_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal repayAmount;
    
    /**
     * 还款类型：1-现金 2-报销抵扣 3-银行转账
     */
    @Column(name = "repay_type", nullable = false)
    private Integer repayType;
    
    /**
     * 还款日期
     */
    @Column(name = "repay_date", nullable = false)
    private LocalDate repayDate;
    
    /**
     * 还款人ID
     */
    @Column(name = "repay_by", nullable = false)
    private Long repayBy;
    
    /**
     * 还款单号
     */
    @Column(name = "repay_no", length = 50, nullable = false)
    private String repayNo;

    /**
     * 银行名称
     */
    @Column(name = "bank_name", length = 100)
    private String bankName;

    /**
     * 银行账号
     */
    @Column(name = "bank_account", length = 50)
    private String bankAccount;

    /**
     * 还款备注
     */
    @Column(name = "remark", length = 512)
    private String remark;

    // Getters and Setters
    public String getRepayNo() {
        return repayNo;
    }

    public void setRepayNo(String repayNo) {
        this.repayNo = repayNo;
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

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public Long getReimburseId() {
        return reimburseId;
    }

    public void setReimburseId(Long reimburseId) {
        this.reimburseId = reimburseId;
    }

    public BigDecimal getRepayAmount() {
        return repayAmount;
    }

    public void setRepayAmount(BigDecimal repayAmount) {
        this.repayAmount = repayAmount;
    }

    public Integer getRepayType() {
        return repayType;
    }

    public void setRepayType(Integer repayType) {
        this.repayType = repayType;
    }

    public LocalDate getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(LocalDate repayDate) {
        this.repayDate = repayDate;
    }

    public Long getRepayBy() {
        return repayBy;
    }

    public void setRepayBy(Long repayBy) {
        this.repayBy = repayBy;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "RepaymentRecord{" +
                "id=" + getId() +
                ", loanId=" + loanId +
                ", reimburseId=" + reimburseId +
                ", repayAmount=" + repayAmount +
                ", repayType=" + repayType +
                ", repayDate=" + repayDate +
                ", repayBy=" + repayBy +
                ", remark='" + remark + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}