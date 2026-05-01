package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 借款还款表实体类
 * 对应表名：ccms_loan_repayment
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "loan_repayment")
public class LoanRepayment extends BaseEntity {

    /**
     * 借款记录ID
     */
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    
    /**
     * 报销单ID（通过报销单核销时的关联）
     */
    @Column(name = "reimburse_id")
    private Long reimburseId;
    
    /**
     * 还款金额
     */
    @Column(name = "repayment_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal repaymentAmount;
    
    /**
     * 还款方式：1-现金还款 2-银行转账 3-报销核销
     */
    @Column(name = "repayment_type", nullable = false)
    private Integer repaymentType;
    
    /**
     * 还款时间
     */
    @Column(name = "repayment_time", nullable = false)
    private java.time.LocalDateTime repaymentTime;
    
    /**
     * 还款人ID
     */
    @Column(name = "repayment_user_id", nullable = false)
    private Long repaymentUserId;
    
    /**
     * 收款人ID
     */
    @Column(name = "receiver_user_id")
    private Long receiverUserId;
    
    /**
     * 银行账户信息
     */
    @Column(name = "bank_account_info", length = 500)
    private String bankAccountInfo;
    
    /**
     * 还款凭证图片（存放文件路径）
     */
    @Column(name = "voucher_attachment", length = 500)
    private String voucherAttachment;
    
    /**
     * 还款状态：0-待确认 1-已确认 2-已撤销
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
    
    /**
     * 备注信息
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
    /**
     * 审批状态：0-待审批 1-审批通过 2-审批驳回
     */
    @Column(name = "approval_status")
    private Integer approvalStatus = 0;
    
    /**
     * 审批人ID
     */
    @Column(name = "approved_by")
    private Long approvedBy;
    
    /**
     * 审批时间
     */
    @Column(name = "approval_time")
    private java.time.LocalDateTime approvalTime;
    
    /**
     * 审批意见
     */
    @Column(name = "approval_comment", length = 500)
    private String approvalComment;
    
    // 还款方式常量
    public static final int TYPE_CASH = 1;        // 现金还款
    public static final int TYPE_BANK_TRANSFER = 2; // 银行转账
    public static final int TYPE_REIMBURSE = 3;   // 报销核销
    
    // 状态常量
    public static final int STATUS_PENDING = 0;   // 待确认
    public static final int STATUS_CONFIRMED = 1; // 已确认
    public static final int STATUS_CANCELLED = 2; // 已撤销
    
    // 审批状态常量
    public static final int APPROVAL_PENDING = 0; // 待审批
    public static final int APPROVAL_APPROVED = 1; // 通过
    public static final int APPROVAL_REJECTED = 2; // 驳回
    
    // Getter and Setter 方法
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

    public BigDecimal getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(BigDecimal repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    public Integer getRepaymentType() {
        return repaymentType;
    }

    public void setRepaymentType(Integer repaymentType) {
        this.repaymentType = repaymentType;
    }

    public java.time.LocalDateTime getRepaymentTime() {
        return repaymentTime;
    }

    public void setRepaymentTime(java.time.LocalDateTime repaymentTime) {
        this.repaymentTime = repaymentTime;
    }

    public Long getRepaymentUserId() {
        return repaymentUserId;
    }

    public void setRepaymentUserId(Long repaymentUserId) {
        this.repaymentUserId = repaymentUserId;
    }

    public Long getReceiverUserId() {
        return receiverUserId;
    }

    public void setReceiverUserId(Long receiverUserId) {
        this.receiverUserId = receiverUserId;
    }

    public String getBankAccountInfo() {
        return bankAccountInfo;
    }

    public void setBankAccountInfo(String bankAccountInfo) {
        this.bankAccountInfo = bankAccountInfo;
    }

    public String getVoucherAttachment() {
        return voucherAttachment;
    }

    public void setVoucherAttachment(String voucherAttachment) {
        this.voucherAttachment = voucherAttachment;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Long getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(Long approvedBy) {
        this.approvedBy = approvedBy;
    }

    public java.time.LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(java.time.LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }
}