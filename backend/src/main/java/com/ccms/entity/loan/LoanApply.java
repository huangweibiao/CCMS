package com.ccms.entity.loan;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 借款申请单实体
 */
@Entity
@Table(name = "ccms_loan_apply")
public class LoanApply extends BaseEntity {
    
    @Column(name = "loan_no", nullable = false, unique = true, length = 50)
    private String loanNo;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;
    
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;
    
    @Column(name = "dept_id")
    private Long deptId;
    
    @Column(name = "dept_name", length = 100)
    private String deptName;
    
    @Column(name = "loan_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal loanAmount;
    
    @Column(name = "purpose", length = 500)
    private String purpose;
    
    @Column(name = "status", nullable = false)
    private Integer status; // 0-草稿, 1-审批中, 2-已通过, 3-已驳回, 4-已放款, 5-已还清, 6-部分还清
    
    @Column(name = "expected_repay_date")
    private LocalDate expectedRepayDate;
    
    @Column(name = "approved_amount", precision = 15, scale = 2)
    private BigDecimal approvedAmount;
    
    @Column(name = "actual_loan_amount", precision = 15, scale = 2)
    private BigDecimal actualLoanAmount;
    
    @Column(name = "loan_date")
    private LocalDate loanDate;
    
    @Column(name = "remaining_balance", precision = 15, scale = 2)
    private BigDecimal remainingBalance = BigDecimal.ZERO;
    
    @Column(name = "repayment_status", nullable = false)
    private Integer repaymentStatus = 0; // 0-未还, 1-已还清, 2-部分还款
    
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;
    
    @Column(name = "remark", length = 1000)
    private String remark;

    // Getters and Setters
    public String getLoanNo() {
        return loanNo;
    }

    public void setLoanNo(String loanNo) {
        this.loanNo = loanNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDate getExpectedRepayDate() {
        return expectedRepayDate;
    }

    public void setExpectedRepayDate(LocalDate expectedRepayDate) {
        this.expectedRepayDate = expectedRepayDate;
    }

    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public BigDecimal getActualLoanAmount() {
        return actualLoanAmount;
    }

    public void setActualLoanAmount(BigDecimal actualLoanAmount) {
        this.actualLoanAmount = actualLoanAmount;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }

    public Integer getRepaymentStatus() {
        return repaymentStatus;
    }

    public void setRepaymentStatus(Integer repaymentStatus) {
        this.repaymentStatus = repaymentStatus;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    /**
     * 检查是否处于可还款状态
     */
    public boolean isRepayable() {
        return status != null && status.equals(4) && 
               remainingBalance != null && remainingBalance.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 检查是否已过期
     */
    public boolean isOverdue() {
        return expectedRepayDate != null && 
               expectedRepayDate.isBefore(LocalDate.now()) &&
               repaymentStatus != null && repaymentStatus.equals(0);
    }
    
    /**
     * 计算可用核销额度
     */
    public BigDecimal calculateWriteOffAmount(BigDecimal reimburseAmount) {
        if (remainingBalance == null || remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        if (remainingBalance.compareTo(reimburseAmount) >= 0) {
            return reimburseAmount;
        } else {
            return remainingBalance;
        }
    }
    
    /**
     * LoanDeductionService需要的设置借款状态方法
     */
    public void setLoanStatus(int loanStatus) {
        this.status = loanStatus;
    }
}