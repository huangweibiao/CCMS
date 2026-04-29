package com.ccms.entity.loan;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 借款还款记录表实体类
 * 对应表名：ccms_loan_repayment
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_loan_repayment")
public class LoanRepayment extends BaseEntity {

    /**
     * 借款单ID
     */
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    
    /**
     * 报销单ID（核销时关联）
     */
    @Column(name = "reimburse_id")
    private Long reimburseId;
    
    /**
     * 还款类型：1-全额还款 2-部分还款 3-逾期还款
     */
    @Column(name = "repayment_type", nullable = false)
    private Integer repaymentType;
    
    /**
     * 还款金额
     */
    @Column(name = "repayment_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal repaymentAmount;
    
    /**
     * 还款状态：0-待确认 1-已确认 2-已撤销
     */
    @Column(name = "repayment_status", nullable = false)
    private Integer repaymentStatus = 0;
    
    /**
     * 还款时间
     */
    @Column(name = "repayment_time")
    private LocalDateTime repaymentTime;
    
    /**
     * 还款确认人ID
     */
    @Column(name = "confirmed_by")
    private Long confirmedBy;
    
    /**
     * 确认时间
     */
    @Column(name = "confirmed_time")
    private LocalDateTime confirmedTime;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 512)
    private String remark;

    // Getters and Setters
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

    public Integer getRepaymentType() {
        return repaymentType;
    }

    public void setRepaymentType(Integer repaymentType) {
        this.repaymentType = repaymentType;
    }

    public BigDecimal getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(BigDecimal repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    public Integer getRepaymentStatus() {
        return repaymentStatus;
    }

    public void setRepaymentStatus(Integer repaymentStatus) {
        this.repaymentStatus = repaymentStatus;
    }

    public LocalDateTime getRepaymentTime() {
        return repaymentTime;
    }

    public void setRepaymentTime(LocalDateTime repaymentTime) {
        this.repaymentTime = repaymentTime;
    }

    public Long getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(Long confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public LocalDateTime getConfirmedTime() {
        return confirmedTime;
    }

    public void setConfirmedTime(LocalDateTime confirmedTime) {
        this.confirmedTime = confirmedTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "LoanRepayment{" +
                "id=" + getId() +
                ", loanId=" + loanId +
                ", repaymentType=" + repaymentType +
                ", repaymentAmount=" + repaymentAmount +
                ", repaymentStatus=" + repaymentStatus +
                ", repaymentTime=" + repaymentTime +
                ", confirmedBy=" + confirmedBy +
                ", confirmedTime=" + confirmedTime +
                ", remark='" + remark + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}