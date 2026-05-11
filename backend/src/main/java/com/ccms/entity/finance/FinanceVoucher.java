package com.ccms.entity.finance;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务凭证主表实体类
 * 对应表名：finance_voucher
 */
@Entity
@Table(name = "finance_voucher")
public class FinanceVoucher extends BaseEntity {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * 凭证编号
     */
    @Column(name = "voucher_no", length = 64, nullable = false, unique = true)
    private String voucherNo;

    /**
     * 凭证模板ID
     */
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    /**
     * 业务类型：EXPENSE/REIMBURSE/PAYMENT/OTHER
     */
    @Column(name = "business_type", length = 32, nullable = false)
    private String businessType;

    /**
     * 业务单据ID
     */
    @Column(name = "business_id", nullable = false)
    private Long businessId;

    /**
     * 业务单号
     */
    @Column(name = "business_no", length = 64)
    private String businessNo;

    /**
     * 凭证日期
     */
    @Column(name = "voucher_date", nullable = false)
    private LocalDate voucherDate;

    /**
     * 借方科目编码
     */
    @Column(name = "debit_account", length = 32)
    private String debitAccount;

    /**
     * 借方科目名称
     */
    @Column(name = "debit_account_name", length = 64)
    private String debitAccountName;

    /**
     * 贷方科目编码
     */
    @Column(name = "credit_account", length = 32)
    private String creditAccount;

    /**
     * 贷方科目名称
     */
    @Column(name = "credit_account_name", length = 64)
    private String creditAccountName;

    /**
     * 金额
     */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * 摘要
     */
    @Column(name = "summary", length = 512)
    private String summary;

    /**
     * 状态：0-草稿 1-已生成 2-已审核 3-已记账
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    /**
     * 凭证状态：0-无效 1-正常 2-已红冲
     */
    @Column(name = "voucher_status", nullable = false)
    private Integer voucherStatus;

    /**
     * 记账日期
     */
    @Column(name = "accounting_date")
    private LocalDate accountingDate;

    /**
     * 记账人ID
     */
    @Column(name = "posting_user_id")
    private Long postingUserId;

    /**
     * 记账时间
     */
    @Column(name = "posting_time")
    private LocalDateTime postingTime;

    /**
     * 创建人ID
     */
    @Column(name = "create_by", nullable = false, updatable = false)
    private Long createBy;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime;

    /**
     * 更新人ID
     */
    @Column(name = "update_by")
    private Long updateBy;

    /**
     * 更新时间
     */
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    // getter方法
    public Long getId() {
        return id;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public LocalDate getVoucherDate() {
        return voucherDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getVoucherStatus() {
        return voucherStatus;
    }

    public LocalDate getAccountingDate() {
        return accountingDate;
    }

    public String getDebitAccount() {
        return debitAccount;
    }

    public String getDebitAccountName() {
        return debitAccountName;
    }

    public String getCreditAccount() {
        return creditAccount;
    }

    public String getCreditAccountName() {
        return creditAccountName;
    }

    public String getSummary() {
        return summary;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public LocalDateTime getPostingTime() {
        return postingTime;
    }

    public Long getPostingUserId() {
        return postingUserId;
    }

    public Integer getVersion() {
        return version;
    }

    // setter方法
    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public void setVoucherDate(LocalDate voucherDate) {
        this.voucherDate = voucherDate;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setVoucherStatus(Integer voucherStatus) {
        this.voucherStatus = voucherStatus;
    }

    public void setAccountingDate(LocalDate accountingDate) {
        this.accountingDate = accountingDate;
    }

    public void setDebitAccount(String debitAccount) {
        this.debitAccount = debitAccount;
    }

    public void setDebitAccountName(String debitAccountName) {
        this.debitAccountName = debitAccountName;
    }

    public void setCreditAccount(String creditAccount) {
        this.creditAccount = creditAccount;
    }

    public void setCreditAccountName(String creditAccountName) {
        this.creditAccountName = creditAccountName;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public void setPostingUserId(Long postingUserId) {
        this.postingUserId = postingUserId;
    }

    public void setPostingTime(LocalDateTime postingTime) {
        this.postingTime = postingTime;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}