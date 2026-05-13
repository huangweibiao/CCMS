package com.ccms.entity.finance;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务支付单据实体类
 * 对应表名：finance_payment
 */
@Entity
@Table(name = "finance_payment")
public class FinancePayment extends BaseEntity {

    /**
     * 支付单据ID - 使用BaseEntity的id字段，映射到payment_id列
     */
    @Override
    @Column(name = "payment_id")
    public Long getId() {
        return super.getId();
    }
    
    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    /**
     * 支付单据编号
     */
    @Column(name = "payment_no", length = 32, unique = true, nullable = false)
    private String paymentNo;

    /**
     * 业务类型 (EXPENSE/REIMBURSE/PAYMENT/OTHER)
     */
    @Column(name = "business_type", length = 20, nullable = false)
    private String businessType;

    /**
     * 业务单据ID
     */
    @Column(name = "business_id")
    private Long businessId;

    /**
     * 业务单据编号
     */
    @Column(name = "business_no", length = 32)
    private String businessNo;

    /**
     * 申请部门ID
     */
    @Column(name = "apply_department_id")
    private Long applyDepartmentId;

    /**
     * 申请部门名称
     */
    @Column(name = "apply_department_name", length = 64)
    private String applyDepartmentName;

    /**
     * 申请人工号
     */
    @Column(name = "apply_employee_id")
    private Long applyEmployeeId;

    /**
     * 申请人工姓名
     */
    @Column(name = "apply_employee_name", length = 32)
    private String applyEmployeeName;

    /**
     * 支付金额
     */
    @Column(name = "amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    /**
     * 支付方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20, nullable = false)
    private FinancePaymentMethod paymentMethod;

    /**
     * 收款账户
     */
    @Column(name = "payee_account", length = 64)
    private String payeeAccount;

    /**
     * 收款账户银行
     */
    @Column(name = "payee_bank", length = 64)
    private String payeeBank;

    /**
     * 收款人姓名
     */
    @Column(name = "payee_name", length = 32)
    private String payeeName;

    /**
     * 支付事由
     */
    @Column(name = "payment_reason", length = 512)
    private String paymentReason;

    /**
     * 支付事由详细说明
     */
    @Column(name = "payment_reason_detail", length = 1024, columnDefinition = "TEXT")
    private String paymentReasonDetail;

    /**
     * 支付日期
     */
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    /**
     * 计划支付日期
     */
    @Column(name = "scheduled_payment_date")
    private LocalDate scheduledPaymentDate;

    /**
     * 实际支付日期
     */
    @Column(name = "actual_payment_date")
    private LocalDate actualPaymentDate;

    /**
     * 支付状态
     * 0-草稿 1-待审批 2-已审批 3-已支付 4-已取消
     */
    @Column(name = "payment_status", nullable = false)
    private Integer paymentStatus;

    /**
     * 支付凭证ID
     */
    @Column(name = "voucher_id")
    private Long voucherId;

    /**
     * 审批人ID
     */
    @Column(name = "approval_user_id")
    private Long approvalUserId;

    /**
     * 审批人姓名
     */
    @Column(name = "approval_user_name", length = 32)
    private String approvalUserName;

    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /**
     * 审批意见
     */
    @Column(name = "approval_comment", length = 512)
    private String approvalComment;

    /**
     * 会计核算状态
     * 0-未核算 1-已核算 2-核算异常
     */
    @Column(name = "accounting_status", nullable = false)
    private Integer accountingStatus;

    /**
     * 凭证生成状态
     * 0-未生成 1-已生成 2-生成失败
     */
    @Column(name = "voucher_generation_status")
    private Integer voucherGenerationStatus;

    /**
     * 附件数量
     */
    @Column(name = "attachment_count", nullable = false)
    private Integer attachmentCount;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1024, columnDefinition = "TEXT")
    private String remark;

    // Lombok注解不完整，手动添加必要的getter/setter方法

    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String paymentNo) { this.paymentNo = paymentNo; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long businessId) { this.businessId = businessId; }

    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String businessNo) { this.businessNo = businessNo; }

    public Long getApplyDepartmentId() { return applyDepartmentId; }
    public void setApplyDepartmentId(Long applyDepartmentId) { this.applyDepartmentId = applyDepartmentId; }

    public String getApplyDepartmentName() { return applyDepartmentName; }
    public void setApplyDepartmentName(String applyDepartmentName) { this.applyDepartmentName = applyDepartmentName; }

    public Long getApplyEmployeeId() { return applyEmployeeId; }
    public void setApplyEmployeeId(Long applyEmployeeId) { this.applyEmployeeId = applyEmployeeId; }

    public String getApplyEmployeeName() { return applyEmployeeName; }
    public void setApplyEmployeeName(String applyEmployeeName) { this.applyEmployeeName = applyEmployeeName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public FinancePaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(FinancePaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPayeeAccount() { return payeeAccount; }
    public void setPayeeAccount(String payeeAccount) { this.payeeAccount = payeeAccount; }

    public String getPayeeBank() { return payeeBank; }
    public void setPayeeBank(String payeeBank) { this.payeeBank = payeeBank; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }

    public String getPaymentReason() { return paymentReason; }
    public void setPaymentReason(String paymentReason) { this.paymentReason = paymentReason; }

    public String getPaymentReasonDetail() { return paymentReasonDetail; }
    public void setPaymentReasonDetail(String paymentReasonDetail) { this.paymentReasonDetail = paymentReasonDetail; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public LocalDate getScheduledPaymentDate() { return scheduledPaymentDate; }
    public void setScheduledPaymentDate(LocalDate scheduledPaymentDate) { this.scheduledPaymentDate = scheduledPaymentDate; }

    public LocalDate getActualPaymentDate() { return actualPaymentDate; }
    public void setActualPaymentDate(LocalDate actualPaymentDate) { this.actualPaymentDate = actualPaymentDate; }

    public Integer getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(Integer paymentStatus) { this.paymentStatus = paymentStatus; }

    public Long getVoucherId() { return voucherId; }
    public void setVoucherId(Long voucherId) { this.voucherId = voucherId; }

    public Long getApprovalUserId() { return approvalUserId; }
    public void setApprovalUserId(Long approvalUserId) { this.approvalUserId = approvalUserId; }

    public String getApprovalUserName() { return approvalUserName; }
    public void setApprovalUserName(String approvalUserName) { this.approvalUserName = approvalUserName; }

    public LocalDateTime getApprovalTime() { return approvalTime; }
    public void setApprovalTime(LocalDateTime approvalTime) { this.approvalTime = approvalTime; }

    public String getApprovalComment() { return approvalComment; }
    public void setApprovalComment(String approvalComment) { this.approvalComment = approvalComment; }

    public Integer getAccountingStatus() { return accountingStatus; }
    public void setAccountingStatus(Integer accountingStatus) { this.accountingStatus = accountingStatus; }

    public Integer getVoucherGenerationStatus() { return voucherGenerationStatus; }
    public void setVoucherGenerationStatus(Integer voucherGenerationStatus) { this.voucherGenerationStatus = voucherGenerationStatus; }

    public Integer getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(Integer attachmentCount) { this.attachmentCount = attachmentCount; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}