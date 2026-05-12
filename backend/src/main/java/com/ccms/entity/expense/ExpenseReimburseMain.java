package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

/**
 * 报销单主表实体类
 * 对应表名：ccms_expense_reimburse_main
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_expense_reimburse_main")
public class ExpenseReimburseMain extends BaseEntity {

    /**
     * 报销单号
     */
    @Column(name = "reimburse_no", length = 64, nullable = false)
    private String reimburseNo;
    
    /**
     * 关联申请单ID
     */
    @Column(name = "apply_id")
    private Long applyId;
    
    /**
     * 报销人ID
     */
    @Column(name = "reimburse_user_id", nullable = false)
    private Long reimburseUserId;
    
    /**
     * 报销部门ID
     */
    @Column(name = "reimburse_dept_id", nullable = false)
    private Long reimburseDeptId;
    
    /**
     * 报销总额
     */
    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    /**
     * 借款抵扣总金额
     */
    @Column(name = "loan_deduct_total", precision = 18, scale = 2, nullable = false)
    private BigDecimal loanDeductTotal;
    
    /**
     * 实际报销金额
     */
    @Column(name = "actual_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal actualAmount;
    
    /**
     * 报销事由
     */
    @Column(name = "reason", length = 512)
    private String reason;
    
    /**
     * 报销期间开始
     */
    @Column(name = "period_start")
    private Date periodStart;
    
    /**
     * 报销期间结束
     */
    @Column(name = "period_end")
    private Date periodEnd;
    
    /**
     * 状态：0-草稿 1-审批中 2-已通过 3-已驳回 4-待支付 5-已支付 6-已作废
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 审批状态
     */
    @Column(name = "approval_status")
    private Integer approvalStatus;
    
    /**
     * 当前审批节点
     */
    @Column(name = "current_node", length = 64)
    private String currentNode;
    
    /**
     * 审批流实例ID
     */
    @Column(name = "approval_instance_id")
    private Long approvalInstanceId;
    
    /**
     * 发票总额
     */
    @Column(name = "invoice_total", precision = 18, scale = 2, nullable = false)
    private BigDecimal invoiceTotal;
    
    /**
     * 增值税总额
     */
    @Column(name = "tax_total", precision = 18, scale = 2)
    private BigDecimal taxTotal;
    
    /**
     * 银行账户信息
     */
    @Column(name = "bank_info", length = 256)
    private String bankInfo;
    
    /**
     * 支付方式：1-公司账号 2-现金 3-网银转账
     */
    @Column(name = "payment_method")
    private Integer paymentMethod;
    
    /**
     * 支付时间
     */
    @Column(name = "payment_time")
    private Date paymentTime;
    
    /**
     * 提交人用户ID
     */
    @Column(name = "submit_user_id")
    private Long submitUserId;
    
    /**
     * 提交时间
     */
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    /**
     * 审批人用户ID
     */
    @Column(name = "approved_user_id")
    private Long approvedUserId;
    
    /**
     * 审批时间
     */
    @Column(name = "approved_time")
    private LocalDateTime approvedTime;
    
    /**
     * 审批备注
     */
    @Column(name = "approval_comment", length = 512)
    private String approvalComment;

    // Getters and Setters
    public String getReimburseNo() {
        return reimburseNo;
    }

    public void setReimburseNo(String reimburseNo) {
        this.reimburseNo = reimburseNo;
    }

    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Long getReimburseUserId() {
        return reimburseUserId;
    }

    public void setReimburseUserId(Long reimburseUserId) {
        this.reimburseUserId = reimburseUserId;
    }

    public Long getReimburseDeptId() {
        return reimburseDeptId;
    }

    public void setReimburseDeptId(Long reimburseDeptId) {
        this.reimburseDeptId = reimburseDeptId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getLoanDeductTotal() {
        return loanDeductTotal;
    }

    public void setLoanDeductTotal(BigDecimal loanDeductTotal) {
        this.loanDeductTotal = loanDeductTotal;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(Date periodStart) {
        this.periodStart = periodStart;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
    }

    public Long getApprovalInstanceId() {
        return approvalInstanceId;
    }

    public void setApprovalInstanceId(Long approvalInstanceId) {
        this.approvalInstanceId = approvalInstanceId;
    }

    public BigDecimal getInvoiceTotal() {
        return invoiceTotal;
    }

    public void setInvoiceTotal(BigDecimal invoiceTotal) {
        this.invoiceTotal = invoiceTotal;
    }

    public BigDecimal getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(BigDecimal taxTotal) {
        this.taxTotal = taxTotal;
    }

    public String getBankInfo() {
        return bankInfo;
    }

    public void setBankInfo(String bankInfo) {
        this.bankInfo = bankInfo;
    }

    public Integer getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Integer paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Date getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }

    public Long getSubmitUserId() {
        return submitUserId;
    }

    public void setSubmitUserId(Long submitUserId) {
        this.submitUserId = submitUserId;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public Long getApprovedUserId() {
        return approvedUserId;
    }

    public void setApprovedUserId(Long approvedUserId) {
        this.approvedUserId = approvedUserId;
    }

    public LocalDateTime getApprovedTime() {
        return approvedTime;
    }

    public void setApprovedTime(LocalDateTime approvedTime) {
        this.approvedTime = approvedTime;
    }

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    @Override
    public String toString() {
        return "ExpenseReimburseMain{" +
                "id=" + getId() +
                ", reimburseNo='" + reimburseNo + '\'' +
                ", applyId=" + applyId +
                ", reimburseUserId=" + reimburseUserId +
                ", reimburseDeptId=" + reimburseDeptId +
                ", totalAmount=" + totalAmount +
                ", loanDeductTotal=" + loanDeductTotal +
                ", actualAmount=" + actualAmount +
                ", reason='" + reason + '\'' +
                ", periodStart=" + periodStart +
                ", periodEnd=" + periodEnd +
                ", status=" + status +
                ", approvalStatus=" + approvalStatus +
                ", currentNode='" + currentNode + '\'' +
                ", approvalInstanceId=" + approvalInstanceId +
                ", submitUserId=" + submitUserId +
                ", submitTime=" + submitTime +
                ", approvedUserId=" + approvedUserId +
                ", approvedTime=" + approvedTime +
                ", approvalComment='" + approvalComment + '\'' +
                ", invoiceTotal=" + invoiceTotal +
                ", taxTotal=" + taxTotal +
                ", bankInfo='" + bankInfo + '\'' +
                ", paymentMethod=" + paymentMethod +
                ", paymentTime=" + paymentTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}