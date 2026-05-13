package com.ccms.entity.finance;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 财务凭证明细表实体类
 * 对应表名：finance_voucher_detail
 */
@Entity
@Table(name = "finance_voucher_detail")
public class FinanceVoucherDetail extends BaseEntity {



    /**
     * 凭证ID
     */
    @Column(name = "voucher_id", nullable = false)
    private Long voucherId;

    /**
     * 行号
     */
    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    /**
     * 科目编码
     */
    @Column(name = "account_code", length = 32, nullable = false)
    private String accountCode;

    /**
     * 科目名称
     */
    @Column(name = "account_name", length = 64, nullable = false)
    private String accountName;

    /**
     * 借方金额
     */
    @Column(name = "debit_amount", precision = 18, scale = 2)
    private BigDecimal debitAmount;

    /**
     * 贷方金额
     */
    @Column(name = "credit_amount", precision = 18, scale = 2)
    private BigDecimal creditAmount;

    /**
     * 用途说明
     */
    @Column(name = "description", length = 256)
    private String description;

    /**
     * 成本中心
     */
    @Column(name = "cost_center", length = 64)
    private String costCenter;

    /**
     * 部门
     */
    @Column(name = "department", length = 64)
    private String department;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    // getter方法
    public Long getId() {
        return id;
    }

    public Long getVoucherId() {
        return voucherId;
    }

    public Integer getLineNo() {
        return lineNo;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public BigDecimal getDebitAmount() {
        return debitAmount;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount;
    }

    public String getDescription() {
        return description;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public String getDepartment() {
        return department;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    // setter方法
    public void setVoucherId(Long voucherId) {
        this.voucherId = voucherId;
    }

    public void setLineNo(Integer lineNo) {
        this.lineNo = lineNo;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setDebitAmount(BigDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public void setCreditAmount(BigDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}