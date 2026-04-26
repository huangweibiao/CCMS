package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

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
    private Date repayDate;
    
    /**
     * 还款人ID
     */
    @Column(name = "repay_by", nullable = false)
    private Long repayBy;
    
    /**
     * 还款备注
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

    public Date getRepayDate() {
        return repayDate;
    }

    public void setRepayDate(Date repayDate) {
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