package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 费用借款实体类
 * 对应表名：expense_loan
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_expense_loan")
public class ExpenseLoan extends BaseEntity {

    /**
     * 借款单号
     */
    @Column(name = "loan_no", length = 64, nullable = false, unique = true)
    private String loanNo;
    
    /**
     * 借款事由
     */
    @Column(name = "reason", length = 512, nullable = false)
    private String reason;
    
    /**
     * 借款用途
     */
    @Column(name = "purpose", length = 256)
    private String purpose;
    
    /**
     * 借款金额
     */
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 借款类型：1-备用金 2-差旅借款 3-采购借款 4-其他
     */
    @Column(name = "loan_type", nullable = false)
    private Integer loanType;
    
    /**
     * 借款部门ID
     */
    @Column(name = "loan_dept_id", nullable = false)
    private Long loanDeptId;
    
    /**
     * 借款人ID
     */
    @Column(name = "loan_user_id", nullable = false)
    private Long loanUserId;
    
    /**
     * 预算ID
     */
    @Column(name = "budget_id")
    private Long budgetId;
    
    /**
     * 预算明细ID
     */
    @Column(name = "budget_detail_id")
    private Long budgetDetailId;
    
    /**
     * 借款日期
     */
    @Column(name = "loan_date", nullable = false)
    private Date loanDate;
    
    /**
     * 预计还款日期
     */
    @Column(name = "expected_repay_date")
    private Date expectedRepayDate;
    
    /**
     * 实际还款日期
     */
    @Column(name = "actual_repay_date")
    private Date actualRepayDate;
    
    /**
     * 借款状态：0-草稿 1-审批中 2-审批通过 3-已借出 4-还款中 5-已还清 6-逾期 7-作废
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 还款方式：1-全额还款 2-分期还款 3-报销抵扣 4-工资抵扣
     */
    @Column(name = "repay_method")
    private Integer repayMethod;
    
    /**
     * 已还金额
     */
    @Column(name = "repaid_amount", precision = 18, scale = 2)
    private BigDecimal repaidAmount;
    
    /**
     * 待还金额
     */
    @Column(name = "pending_amount", precision = 18, scale = 2)
    private BigDecimal pendingAmount;
    
    /**
     * 关联报销单ID
     */
    @Column(name = "reimburse_id")
    private Long reimburseId;
    
    /**
     * 审批状态：0-待提交 1-审批中 2-审批通过 3-审批拒绝
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
     * 审批意见
     */
    @Column(name = "approval_comment", length = 512)
    private String approvalComment;
    
    /**
     * 借出时间
     */
    @Column(name = "lend_time")
    private Date lendTime;
    
    /**
     * 借出确认人ID
     */
    @Column(name = "lend_confirmer_id")
    private Long lendConfirmerId;
    
    /**
     * 还款确认人ID
     */
    @Column(name = "repay_confirmer_id")
    private Long repayConfirmerId;
    
    /**
     * 逾期天数
     */
    @Column(name = "overdue_days")
    private Integer overdueDays;
    
    /**
     * 是否逾期提醒
     */
    @Column(name = "overdue_reminded")
    private Boolean overdueReminded;
    
    /**
     * 最后提醒时间
     */
    @Column(name = "last_remind_time")
    private Date lastRemindTime;
    
    // Getters and Setters
    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getLoanType() {
        return loanType;
    }

    public void setLoanType(Integer loanType) {
        this.loanType = loanType;
    }

    public Long getLoanDeptId() {
        return loanDeptId;
    }

    public void setLoanDeptId(Long loanDeptId) {
        this.loanDeptId = loanDeptId;
    }

    public Long getLoanUserId() {
        return loanUserId;
    }

    public void setLoanUserId(Long loanUserId) {
        this.loanUserId = loanUserId;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public Long getBudgetDetailId() {
        return budgetDetailId;
    }

    public void setBudgetDetailId(Long budgetDetailId) {
        this.budgetDetailId = budgetDetailId;
    }

    public Date getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(Date loanDate) {
        this.loanDate = loanDate;
    }

    public Date getExpectedRepayDate() {
        return expectedRepayDate;
    }

    public void setExpectedRepayDate(Date expectedRepayDate) {
        this.expectedRepayDate = expectedRepayDate;
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

    public Integer getRepayMethod() {
        return repayMethod;
    }

    public void setRepayMethod(Integer repayMethod) {
        this.repayMethod = repayMethod;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public BigDecimal getPendingAmount() {
        return pendingAmount;
    }

    public void setPendingAmount(BigDecimal pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public Long getReimburseId() {
        return reimburseId;
    }

    public void setReimburseId(Long reimburseId) {
        this.reimburseId = reimburseId;
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

    public String getApprovalComment() {
        return approvalComment;
    }

    public void setApprovalComment(String approvalComment) {
        this.approvalComment = approvalComment;
    }

    public Date getLendTime() {
        return lendTime;
    }

    public void setLendTime(Date lendTime) {
        this.lendTime = lendTime;
    }

    public Long getLendConfirmerId() {
        return lendConfirmerId;
    }

    public void setLendConfirmerId(Long lendConfirmerId) {
        this.lendConfirmerId = lendConfirmerId;
    }

    public Long getRepayConfirmerId() {
        return repayConfirmerId;
    }

    public void setRepayConfirmerId(Long repayConfirmerId) {
        this.repayConfirmerId = repayConfirmerId;
    }

    public Integer getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Integer overdueDays) {
        this.overdueDays = overdueDays;
    }

    public Boolean getOverdueReminded() {
        return overdueReminded;
    }

    public void setOverdueReminded(Boolean overdueReminded) {
        this.overdueReminded = overdueReminded;
    }

    public Date getLastRemindTime() {
        return lastRemindTime;
    }

    public void setLastRemindTime(Date lastRemindTime) {
        this.lastRemindTime = lastRemindTime;
    }

    // 业务方法
    
    /**
     * 检查是否可以提交审批
     */
    public boolean canSubmit() {
        return this.status == 0; // 草稿状态
    }
    
    /**
     * 检查是否可以借出
     */
    public boolean canLend() {
        return this.status == 2 && this.amount != null && this.amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 检查是否可以还款
     */
    public boolean canRepay() {
        return (this.status == 3 || this.status == 4 || this.status == 6) &&
               this.pendingAmount != null && this.pendingAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 处理借款借出
     */
    public void processLending(Long confirmerId) {
        if (canLend()) {
            this.status = 3; // 已借出
            this.lendTime = new Date(System.currentTimeMillis());
            this.lendConfirmerId = confirmerId;
        }
    }
    
    /**
     * 处理部分还款
     */
    public boolean processPartialRepayment(BigDecimal repayAmount, Long confirmerId) {
        if (repayAmount == null || repayAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.repaidAmount == null) {
            this.repaidAmount = BigDecimal.ZERO;
        }
        if (this.pendingAmount == null) {
            this.pendingAmount = this.amount;
        }
        
        if (repayAmount.compareTo(this.pendingAmount) > 0) {
            return false; // 不能超过待还金额
        }
        
        this.repaidAmount = this.repaidAmount.add(repayAmount);
        this.pendingAmount = this.pendingAmount.subtract(repayAmount);
        this.repayConfirmerId = confirmerId;
        
        // 更新状态
        if (this.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = 5; // 已还清
            this.actualRepayDate = new Date(System.currentTimeMillis());
        } else {
            this.status = 4; // 还款中
        }
        
        return true;
    }
    
    /**
     * 检查是否逾期
     */
    public boolean checkOverdue() {
        if (this.expectedRepayDate == null || this.status == 5 || this.status == 7) {
            return false; // 无预计还款日期或已结清/作废
        }
        
        long currentTime = System.currentTimeMillis();
        long expectedTime = this.expectedRepayDate.getTime();
        
        if (currentTime > expectedTime) {
            this.status = 6; // 逾期
            // 计算逾期天数
            long diffTime = currentTime - expectedTime;
            this.overdueDays = (int) (diffTime / (1000 * 60 * 60 * 24));
            return true;
        }
        
        return false;
    }
    
    /**
     * 关联报销抵扣
     */
    public boolean linkReimburseDeduction(Long reimburseId, BigDecimal deductionAmount) {
        if (this.status != 3 && this.status != 4 && this.status != 6) {
            return false; // 非已借出/还款中/逾期状态不能抵扣
        }
        
        if (deductionAmount == null || deductionAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        if (this.pendingAmount == null) {
            this.pendingAmount = this.amount.subtract(this.repaidAmount != null ? this.repaidAmount : BigDecimal.ZERO);
        }
        
        if (deductionAmount.compareTo(this.pendingAmount) > 0) {
            return false; // 抵扣金额不能超过待还金额
        }
        
        if (this.repaidAmount == null) {
            this.repaidAmount = BigDecimal.ZERO;
        }
        
        this.repaidAmount = this.repaidAmount.add(deductionAmount);
        this.pendingAmount = this.pendingAmount.subtract(deductionAmount);
        this.reimburseId = reimburseId;
        
        // 更新状态
        if (this.pendingAmount.compareTo(BigDecimal.ZERO) == 0) {
            this.status = 5; // 已还清
            this.actualRepayDate = new Date(System.currentTimeMillis());
        } else {
            this.status = 4; // 还款中
        }
        
        return true;
    }
    
    /**
     * 获取借款摘要信息
     */
    public String getLoanSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("借款单号：").append(this.loanNo)
               .append("，金额：").append(this.amount)
               .append("，状态：").append(getStatusDescription(this.status));
        
        if (this.pendingAmount != null && this.pendingAmount.compareTo(BigDecimal.ZERO) > 0) {
            summary.append("，待还：").append(this.pendingAmount);
        }
        
        return summary.toString();
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDescription(Integer status) {
        if (status == null) return "未知";
        
        switch (status) {
            case 0: return "草稿";
            case 1: return "审批中";
            case 2: return "审批通过";
            case 3: return "已借出";
            case 4: return "还款中";
            case 5: return "已还清";
            case 6: return "逾期";
            case 7: return "作废";
            default: return "未知";
        }
    }
    
    /**
     * 获取借款类型描述
     */
    public String getLoanTypeDescription() {
        if (this.loanType == null) return "未知";
        
        switch (this.loanType) {
            case 1: return "备用金";
            case 2: return "差旅借款";
            case 3: return "采购借款";
            case 4: return "其他";
            default: return "未知";
        }
    }

    @Override
    public String toString() {
        return "ExpenseLoan{" +
                "id=" + getId() +
                ", loanNo='" + loanNo + '\'' +
                ", reason='" + reason + '\'' +
                ", purpose='" + purpose + '\'' +
                ", amount=" + amount +
                ", loanType=" + loanType +
                ", loanDeptId=" + loanDeptId +
                ", loanUserId=" + loanUserId +
                ", budgetId=" + budgetId +
                ", budgetDetailId=" + budgetDetailId +
                ", loanDate=" + loanDate +
                ", expectedRepayDate=" + expectedRepayDate +
                ", actualRepayDate=" + actualRepayDate +
                ", status=" + status +
                ", repayMethod=" + repayMethod +
                ", repaidAmount=" + repaidAmount +
                ", pendingAmount=" + pendingAmount +
                ", reimburseId=" + reimburseId +
                ", approvalStatus=" + approvalStatus +
                ", currentNode='" + currentNode + '\'' +
                ", approvalInstanceId=" + approvalInstanceId +
                ", approvalComment='" + approvalComment + '\'' +
                ", lendTime=" + lendTime +
                ", lendConfirmerId=" + lendConfirmerId +
                ", repayConfirmerId=" + repayConfirmerId +
                ", overdueDays=" + overdueDays +
                ", overdueReminded=" + overdueReminded +
                ", lastRemindTime=" + lastRemindTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}