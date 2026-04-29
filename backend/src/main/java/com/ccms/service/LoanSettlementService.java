package com.ccms.service;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.LoanRepayment;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 借款核销服务接口
 * 
 * @author 系统生成
 */
public interface LoanSettlementService {
    
    /**
     * 自动核销借款
     * 
     * @param reimburseId 报销单ID
     * @param userId 操作人ID
     * @return 核销结果
     */
    SettlementResult autoSettleLoan(Long reimburseId, Long userId);
    
    /**
     * 手动指定核销借款
     * 
     * @param reimburseId 报销单ID
     * @param loanIds 要核销的借款ID列表
     * @param settleAmounts 核销金额映射
     * @param userId 操作人ID
     * @return 核销结果
     */
    SettlementResult manualSettleLoan(Long reimburseId, List<Long> loanIds, 
                                     Map<Long, BigDecimal> settleAmounts, Long userId);
    
    /**
     * 计算可核销的借款金额
     * 
     * @param userId 用户ID
     * @param reimburseAmount 报销金额
     * @return 可核销的借款列表和金额
     */
    AvailableSettlement calculateAvailableSettlement(Long userId, BigDecimal reimburseAmount);
    
    /**
     * 获取借款核销历史
     * 
     * @param loanId 借款ID
     * @return 核销历史记录
     */
    List<LoanRepayment> getSettlementHistory(Long loanId);
    
    /**
     * 检查借款超期状态
     * 
     * @param loanId 借款ID
     * @return 超期状态信息
     */
    OverdueStatus checkLoanOverdue(Long loanId);
    
    /**
     * 发送借款提醒
     * 
     * @param loanIds 借款ID列表
     * @return 提醒发送结果
     */
    ReminderResult sendLoanReminders(List<Long> loanIds);
    
    /**
     * 核销结果
     */
    class SettlementResult {
        private final boolean success;              // 是否成功
        private final String message;               // 结果消息
        private final BigDecimal totalSettleAmount; // 总核销金额
        private final List<LoanRepayment> repayments; // 生成的还款记录
        private final List<Long> settledLoanIds;    // 已核销的借款ID列表
        
        public SettlementResult(boolean success, String message, BigDecimal totalSettleAmount,
                               List<LoanRepayment> repayments, List<Long> settledLoanIds) {
            this.success = success;
            this.message = message;
            this.totalSettleAmount = totalSettleAmount != null ? totalSettleAmount : BigDecimal.ZERO;
            this.repayments = repayments != null ? repayments : java.util.Collections.emptyList();
            this.settledLoanIds = settledLoanIds != null ? settledLoanIds : java.util.Collections.emptyList();
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public BigDecimal getTotalSettleAmount() { return totalSettleAmount; }
        public List<LoanRepayment> getRepayments() { return repayments; }
        public List<Long> getSettledLoanIds() { return settledLoanIds; }
    }
    
    /**
     * 可核销的借款信息
     */
    class AvailableSettlement {
        private final boolean canSettle;            // 是否可以核销
        private final BigDecimal maxSettleAmount;   // 最大可核销金额
        private final List<SettleableLoan> settleableLoans; // 可核销的借款列表
        private final String suggestInfo;           // 建议信息
        
        public AvailableSettlement(boolean canSettle, BigDecimal maxSettleAmount,
                                  List<SettleableLoan> settleableLoans, String suggestInfo) {
            this.canSettle = canSettle;
            this.maxSettleAmount = maxSettleAmount != null ? maxSettleAmount : BigDecimal.ZERO;
            this.settleableLoans = settleableLoans != null ? settleableLoans : java.util.Collections.emptyList();
            this.suggestInfo = suggestInfo;
        }
        
        // Getter方法
        public boolean isCanSettle() { return canSettle; }
        public BigDecimal getMaxSettleAmount() { return maxSettleAmount; }
        public List<SettleableLoan> getSettleableLoans() { return settleableLoans; }
        public String getSuggestInfo() { return suggestInfo; }
    }
    
    /**
     * 可核销的单笔借款
     */
    class SettleableLoan {
        private final Long loanId;                  // 借款ID
        private final String loanNo;                // 借款单号
        private final BigDecimal loanAmount;        // 借款总金额
        private final BigDecimal remainingAmount;   // 剩余未核销金额
        private final BigDecimal maxSettleAmount;   // 本次最大可核销金额
        private final boolean overdue;              // 是否超期
        private final String overdueInfo;           // 超期信息
        
        public SettleableLoan(Long loanId, String loanNo, BigDecimal loanAmount,
                             BigDecimal remainingAmount, BigDecimal maxSettleAmount,
                             boolean overdue, String overdueInfo) {
            this.loanId = loanId;
            this.loanNo = loanNo;
            this.loanAmount = loanAmount != null ? loanAmount : BigDecimal.ZERO;
            this.remainingAmount = remainingAmount != null ? remainingAmount : BigDecimal.ZERO;
            this.maxSettleAmount = maxSettleAmount != null ? maxSettleAmount : BigDecimal.ZERO;
            this.overdue = overdue;
            this.overdueInfo = overdueInfo;
        }
        
        // Getter方法
        public Long getLoanId() { return loanId; }
        public String getLoanNo() { return loanNo; }
        public BigDecimal getLoanAmount() { return loanAmount; }
        public BigDecimal getRemainingAmount() { return remainingAmount; }
        public BigDecimal getMaxSettleAmount() { return maxSettleAmount; }
        public boolean isOverdue() { return overdue; }
        public String getOverdueInfo() { return overdueInfo; }
    }
    
    /**
     * 超期状态信息
     */
    class OverdueStatus {
        private final boolean overdue;              // 是否超期
        private final int overdueDays;              // 超期天数
        private final BigDecimal overdueAmount;     // 超期金额
        private final String overdueLevel;          // 超期等级：NORMAL/WARNING/CRITICAL
        private final String suggestAction;         // 建议行动
        
        public OverdueStatus(boolean overdue, int overdueDays, BigDecimal overdueAmount,
                            String overdueLevel, String suggestAction) {
            this.overdue = overdue;
            this.overdueDays = overdueDays;
            this.overdueAmount = overdueAmount != null ? overdueAmount : BigDecimal.ZERO;
            this.overdueLevel = overdueLevel;
            this.suggestAction = suggestAction;
        }
        
        // Getter方法
        public boolean isOverdue() { return overdue; }
        public int getOverdueDays() { return overdueDays; }
        public BigDecimal getOverdueAmount() { return overdueAmount; }
        public String getOverdueLevel() { return overdueLevel; }
        public String getSuggestAction() { return suggestAction; }
    }
    
    /**
     * 提醒发送结果
     */
    class ReminderResult {
        private final boolean success;              // 是否成功
        private final String message;               // 结果消息
        private final int totalReminders;           // 总提醒数量
        private final int sentCount;                // 成功发送数量
        private final int failedCount;              // 发送失败数量
        private final List<Long> sentLoanIds;       // 成功发送的借款ID
        private final List<Long> failedLoanIds;     // 发送失败的借款ID
        
        public ReminderResult(boolean success, String message, int totalReminders,
                             int sentCount, int failedCount, List<Long> sentLoanIds, 
                             List<Long> failedLoanIds) {
            this.success = success;
            this.message = message;
            this.totalReminders = totalReminders;
            this.sentCount = sentCount;
            this.failedCount = failedCount;
            this.sentLoanIds = sentLoanIds != null ? sentLoanIds : java.util.Collections.emptyList();
            this.failedLoanIds = failedLoanIds != null ? failedLoanIds : java.util.Collections.emptyList();
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getTotalReminders() { return totalReminders; }
        public int getSentCount() { return sentCount; }
        public int getFailedCount() { return failedCount; }
        public List<Long> getSentLoanIds() { return sentLoanIds; }
        public List<Long> getFailedLoanIds() { return failedLoanIds; }
    }
}