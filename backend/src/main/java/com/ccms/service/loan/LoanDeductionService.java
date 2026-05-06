package com.ccms.service.loan;

import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.LoanRepayment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借款核销服务
 * 实现借款余额自动检测和智能核销功能
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class LoanDeductionService {

    /**
     * 自动检测并核销借款
     * 根据设计文档要求：
     * - 自动检测借款人未还清借款
     * - 智能计算可抵扣金额
     * - 自动生成还款记录
     */
    public LoanDeductionResult autoDeductLoan(ExpenseReimburse reimburse) {
        LoanDeductionResult result = new LoanDeductionResult();
        Long userId = reimburse.getApplyUserId();
        
        // 1. 查找用户未还清的借款
        List<LoanMain> outstandingLoans = findOutstandingLoans(userId);
        if (outstandingLoans.isEmpty()) {
            result.setDeductedAmount(BigDecimal.ZERO);
            result.setMessage("无待核销借款");
            return result;
        }
        
        // 2. 计算报销金额
        BigDecimal reimburseAmount = calculateReimburseAmount(reimburse);
        if (reimburseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            result.setDeductedAmount(BigDecimal.ZERO);
            result.setMessage("报销金额为0，无法核销借款");
            return result;
        }
        
        // 3. 智能计算可抵扣金额
        DeductionPlan deductionPlan = calculateDeductionPlan(outstandingLoans, reimburseAmount);
        
        // 4. 执行核销操作
        executeDeduction(deductionPlan, reimburse);
        
        // 5. 生成核销记录
        generateDeductionRecords(deductionPlan, reimburse);
        
        result.setDeductedAmount(deductionPlan.getTotalDeducted());
        result.setMessage("成功核销借款￥" + deductionPlan.getTotalDeducted());
        result.setDeductionPlan(deductionPlan);
        
        return result;
    }

    /**
     * 查找未还清借款
     */
    private List<LoanMain> findOutstandingLoans(Long userId) {
        // TODO: 实现查询逻辑，查找状态为"借款中"或"部分还款"的借款单
        // 按借款时间排序，优先处理较早的借款
        return List.of();
    }

    /**
     * 计算报销金额（减去已核销部分）
     */
    private BigDecimal calculateReimburseAmount(ExpenseReimburse reimburse) {
        BigDecimal totalAmount = reimburse.getTotalAmount();
        BigDecimal alreadyDeducted = getAlreadyDeductedAmount(reimburse.getId());
        return totalAmount.subtract(alreadyDeducted);
    }

    /**
     * 获取已核销金额
     */
    private BigDecimal getAlreadyDeductedAmount(Long reimburseId) {
        // TODO: 实现查询逻辑，获取当前报销单已核销的借款总额
        return BigDecimal.ZERO;
    }

    /**
     * 计算核销计划
     */
    private DeductionPlan calculateDeductionPlan(List<LoanMain> outstandingLoans, 
                                                 BigDecimal availableAmount) {
        DeductionPlan plan = new DeductionPlan();
        BigDecimal remainingAmount = availableAmount;
        
        // 按借款时间顺序核销（先借先还原则）
        for (LoanMain loan : outstandingLoans) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            // 计算该笔借款的剩余应还金额
            BigDecimal outstandingAmount = calculateOutstandingAmount(loan);
            
            if (outstandingAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 计算本次可核销金额
                BigDecimal deductAmount = outstandingAmount.min(remainingAmount);
                
                // 添加核销明细
                plan.addDeductionDetail(loan, deductAmount);
                
                // 更新剩余金额
                remainingAmount = remainingAmount.subtract(deductAmount);
            }
        }
        
        return plan;
    }

    /**
     * 计算借款剩余应还金额
     */
    private BigDecimal calculateOutstandingAmount(LoanMain loan) {
        BigDecimal loanAmount = loan.getLoanAmount();
        BigDecimal repaidAmount = getRepaidAmount(loan.getId());
        return loanAmount.subtract(repaidAmount);
    }

    /**
     * 获取已还款金额
     */
    private BigDecimal getRepaidAmount(Long loanId) {
        // TODO: 实现查询逻辑，获取该借款的累计还款金额
        return BigDecimal.ZERO;
    }

    /**
     * 执行核销操作
     */
    private void executeDeduction(DeductionPlan deductionPlan, ExpenseReimburse reimburse) {
        for (DeductionDetail detail : deductionPlan.getDetails()) {
            // 更新借款状态
            updateLoanStatus(detail.getLoan(), detail.getDeductAmount());
            
            // 更新报销单信息
            updateReimburseDeduction(reimburse, detail.getDeductAmount());
        }
    }

    /**
     * 更新借款状态
     */
    private void updateLoanStatus(LoanMain loan, BigDecimal deductedAmount) {
        // 计算还款后的剩余金额
        BigDecimal outstandingAmount = calculateOutstandingAmount(loan);
        BigDecimal remainingAfterDeduction = outstandingAmount.subtract(deductedAmount);
        
        // 更新借款状态
        if (remainingAfterDeduction.compareTo(BigDecimal.ZERO) <= 0) {
            // 借款已还清
            loan.setLoanStatus(3); // 已还清
        } else {
            // 部分还款
            loan.setLoanStatus(2); // 还款中
        }
        
        // TODO: 保存借款更新
    }

    /**
     * 更新报销单核销信息
     */
    private void updateReimburseDeduction(ExpenseReimburse reimburse, BigDecimal deductedAmount) {
        BigDecimal currentDeduction = reimburse.getLoanDeductionAmount();
        if (currentDeduction == null) {
            currentDeduction = BigDecimal.ZERO;
        }
        
        reimburse.setLoanDeductionAmount(currentDeduction.add(deductedAmount));
        
        // 重新计算实报金额
        BigDecimal realAmount = reimburse.getTotalAmount().subtract(
            reimburse.getLoanDeductionAmount() != null ? reimburse.getLoanDeductionAmount() : BigDecimal.ZERO
        );
        reimburse.setRealAmount(realAmount);
        
        // TODO: 保存报销单更新
    }

    /**
     * 生成核销记录
     */
    private void generateDeductionRecords(DeductionPlan deductionPlan, ExpenseReimburse reimburse) {
        for (DeductionDetail detail : deductionPlan.getDetails()) {
            LoanRepayment repayment = new LoanRepayment();
            
            repayment.setLoanId(detail.getLoan().getId());
            repayment.setReimburseId(reimburse.getId());
            repayment.setRepaymentAmount(detail.getDeductAmount());
            repayment.setRepaymentTime(LocalDateTime.now());
            repayment.setRepaymentType(2); // 报销抵扣
            repayment.setRemark("报销单自动核销：" + reimburse.getReimburseNo());
            
            // TODO: 保存还款记录
        }
    }

    /**
     * 借款核销结果
     */
    public static class LoanDeductionResult {
        private BigDecimal deductedAmount;
        private String message;
        private DeductionPlan deductionPlan;

        // Getters and Setters
        public BigDecimal getDeductedAmount() {
            return deductedAmount;
        }

        public void setDeductedAmount(BigDecimal deductedAmount) {
            this.deductedAmount = deductedAmount;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public DeductionPlan getDeductionPlan() {
            return deductionPlan;
        }

        public void setDeductionPlan(DeductionPlan deductionPlan) {
            this.deductionPlan = deductionPlan;
        }
    }

    /**
     * 核销计划
     */
    public static class DeductionPlan {
        private List<DeductionDetail> details;
        
        public DeductionPlan() {
            this.details = new java.util.ArrayList<>();
        }
        
        public void addDeductionDetail(LoanMain loan, BigDecimal amount) {
            details.add(new DeductionDetail(loan, amount));
        }
        
        public BigDecimal getTotalDeducted() {
            return details.stream()
                    .map(DeductionDetail::getDeductAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        
        public List<DeductionDetail> getDetails() {
            return details;
        }
    }

    /**
     * 核销明细
     */
    public static class DeductionDetail {
        private LoanMain loan;
        private BigDecimal deductAmount;
        
        public DeductionDetail(LoanMain loan, BigDecimal deductAmount) {
            this.loan = loan;
            this.deductAmount = deductAmount;
        }
        
        // Getters
        public LoanMain getLoan() {
            return loan;
        }
        
        public BigDecimal getDeductAmount() {
            return deductAmount;
        }
    }
}