package com.ccms.service.loan;

import com.ccms.entity.expense.LoanMain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 借款服务接口
 */
public interface LoanService {
    
    /**
     * 创建借款申请
     */
    LoanApply createLoanApply(LoanApply loanApply);
    
    /**
     * 更新借款申请
     */
    LoanApply updateLoanApply(LoanApply loanApply);
    
    /**
     * 根据ID获取借款申请
     */
    LoanApply getLoanApplyById(Long id);
    
    /**
     * 审批借款申请
     */
    LoanApply approveLoan(Long id, BigDecimal approvedAmount, String remark);
    
    /**
     * 驳回借款申请
     */
    LoanApply rejectLoan(Long id, String rejectReason);
    
    /**
     * 放款操作
     */
    LoanApply disburseLoan(Long id, BigDecimal actualAmount);
    
    /**
     * 还款操作
     */
    LoanApply repayLoan(Long id, BigDecimal repayAmount);
    
    /**
     * 报销时自动核销借款
     */
    WriteOffResult autoWriteOffForReimburse(Long reimburseId, BigDecimal reimburseAmount, Long applicantId);
    
    /**
     * 查询借款列表
     */
    Page<LoanApply> getLoanList(LoanQueryCondition condition, Pageable pageable);
    
    /**
     * 查询用户未还清的借款
     */
    List<LoanApply> getUnpaidLoansByApplicant(Long applicantId);
    
    /**
     * 计算用户总未还借款金额
     */
    BigDecimal calculateTotalUnpaidAmount(Long applicantId);
    
    /**
     * 检查是否有逾期借款
     */
    boolean hasOverdueLoans(Long applicantId);
    
    /**
     * 生成还款计划
     */
    void generateRepaymentPlan(Long loanId);
    
    class LoanQueryCondition {
        private String loanNo;
        private String applicantName;
        private Integer status;
        private Integer repaymentStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        
        // Getters and Setters
        public String getLoanNo() { return loanNo; }
        public void setLoanNo(String loanNo) { this.loanNo = loanNo; }
        
        public String getApplicantName() { return applicantName; }
        public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public Integer getRepaymentStatus() { return repaymentStatus; }
        public void setRepaymentStatus(Integer repaymentStatus) { this.repaymentStatus = repaymentStatus; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
    
    class WriteOffResult {
        private boolean success;
        private BigDecimal writeOffAmount;
        private BigDecimal remainingBalance;
        private String message;
        
        public WriteOffResult(boolean success, BigDecimal writeOffAmount, BigDecimal remainingBalance, String message) {
            this.success = success;
            this.writeOffAmount = writeOffAmount;
            this.remainingBalance = remainingBalance;
            this.message = message;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public BigDecimal getWriteOffAmount() { return writeOffAmount; }
        public BigDecimal getRemainingBalance() { return remainingBalance; }
        public String getMessage() { return message; }
    }
}