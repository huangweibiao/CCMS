package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.LoanRepayment;
import com.ccms.repository.ExpenseReimburseRepository;
import com.ccms.repository.LoanRepaymentRepository;
import com.ccms.service.LoanSettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 借款核销服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class LoanSettlementServiceImpl implements LoanSettlementService {
    
    @Autowired
    private LoanRepaymentRepository loanRepaymentRepository;
    
    @Autowired
    private ExpenseReimburseRepository expenseReimburseRepository;
    
    private static final int OVERDUE_WARNING_DAYS = 30;   // 警告级超期天数
    private static final int OVERDUE_CRITICAL_DAYS = 90;  // 严重级超期天数
    
    @Override
    public SettlementResult autoSettleLoan(Long reimburseId, Long userId) {
        try {
            // 1. 获取报销单信息
            ExpenseReimburse reimburse = expenseReimburseRepository.findById(reimburseId)
                    .orElseThrow(() -> new RuntimeException("报销单不存在: " + reimburseId));
            
            // 2. 校验报销单状态
            if (!reimburse.getStatus().equals("APPROVED")) {
                return new SettlementResult(false, "报销单尚未审批通过，无法进行核销", 
                    BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
            }
            
            // 3. 获取用户可核销的借款
            AvailableSettlement available = calculateAvailableSettlement(
                reimburse.getApplicantUserId(), reimburse.getReimburseAmount());
            
            if (!available.isCanSettle()) {
                return new SettlementResult(false, available.getSuggestInfo(), 
                    BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
            }
            
            // 4. 智能核销算法：优先核销超期借款
            List<SettleableLoan> settleableLoans = available.getSettleableLoans();
            BigDecimal remainingSettleAmount = reimburse.getReimburseAmount();
            List<LoanRepayment> repayments = new ArrayList<>();
            List<Long> settledLoanIds = new ArrayList<>();
            
            // 按超期程度排序：严重超期 -> 警告超期 -> 正常
            settleableLoans.sort((a, b) -> {
                if (a.isOverdue() && !b.isOverdue()) return -1;
                if (!a.isOverdue() && b.isOverdue()) return 1;
                if (a.isOverdue() && b.isOverdue()) {
                    // 比较超期程度（简化逻辑，实际应该根据具体超期天数）
                    return b.getOverdueInfo().compareTo(a.getOverdueInfo());
                }
                // 正常借款按时间排序（先进先出）
                return 1;
            });
            
            // 5. 执行核销
            for (SettleableLoan settleableLoan : settleableLoans) {
                if (remainingSettleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                
                // 计算本次核销金额
                BigDecimal settleAmount = settleableLoan.getMaxSettleAmount()
                        .min(remainingSettleAmount);
                
                // 创建还款记录
                LoanRepayment repayment = createRepaymentRecord(
                    settleableLoan.getLoanId(), reimburseId, settleAmount, 
                    "REIMBURSE_SETTLE", userId);
                
                LoanRepayment savedRepayment = loanRepaymentRepository.save(repayment);
                repayments.add(savedRepayment);
                settledLoanIds.add(settleableLoan.getLoanId());
                
                remainingSettleAmount = remainingSettleAmount.subtract(settleAmount);
            }
            
            // 6. 更新报销单的已核销金额
            BigDecimal totalSettleAmount = reimburse.getReimburseAmount()
                    .subtract(remainingSettleAmount);
            if (reimburse.getActualPaymentAmount() == null) {
                reimburse.setActualPaymentAmount(totalSettleAmount);
            } else {
                reimburse.setActualPaymentAmount(
                    reimburse.getActualPaymentAmount().add(totalSettleAmount));
            }
            
            // 7. 如果报销金额全部用于核销，则报销单状态变更为已支付
            if (remainingSettleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                reimburse.setStatus("PAID");
                reimburse.setPaymentTime(LocalDateTime.now());
            }
            
            expenseReimburseRepository.save(reimburse);
            
            // 8. 记录核销日志
            recordSettlementLog(reimburseId, settledLoanIds, totalSettleAmount, userId);
            
            String message = String.format("成功核销借款%.2f元，涉及%d笔借款", 
                totalSettleAmount.doubleValue(), settledLoanIds.size());
            
            return new SettlementResult(true, message, totalSettleAmount, repayments, settledLoanIds);
            
        } catch (Exception e) {
            return new SettlementResult(false, "核销借款失败: " + e.getMessage(), 
                BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
        }
    }
    
    @Override
    public SettlementResult manualSettleLoan(Long reimburseId, List<Long> loanIds, 
                                           Map<Long, BigDecimal> settleAmounts, Long userId) {
        try {
            // 1. 校验参数
            if (loanIds == null || loanIds.isEmpty() || settleAmounts == null || settleAmounts.isEmpty()) {
                return new SettlementResult(false, "核销参数不能为空", 
                    BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
            }
            
            // 2. 获取报销单信息
            ExpenseReimburse reimburse = expenseReimburseRepository.findById(reimburseId)
                    .orElseThrow(() -> new RuntimeException("报销单不存在: " + reimburseId));
            
            // 3. 校验报销单状态和金额
            if (!reimburse.getStatus().equals("APPROVED")) {
                return new SettlementResult(false, "报销单尚未审批通过，无法进行核销", 
                    BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
            }
            
            BigDecimal totalSettleAmount = settleAmounts.values().stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
            if (totalSettleAmount.compareTo(reimburse.getReimburseAmount()) > 0) {
                return new SettlementResult(false, "核销总金额超过报销金额", 
                    BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
            }
            
            // 4. 核销每笔借款
            List<LoanRepayment> repayments = new ArrayList<>();
            List<Long> settledLoanIds = new ArrayList<>();
            
            for (Long loanId : loanIds) {
                BigDecimal settleAmount = settleAmounts.get(loanId);
                if (settleAmount == null || settleAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                
                // 校验借款状态和剩余金额（简化实现）
                BigDecimal remainingAmount = calculateLoanRemainingAmount(loanId);
                if (remainingAmount.compareTo(settleAmount) < 0) {
                    return new SettlementResult(false, 
                        String.format("借款%d剩余金额不足，无法核销%.2f元", loanId, settleAmount.doubleValue()), 
                        BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
                }
                
                // 创建还款记录
                LoanRepayment repayment = createRepaymentRecord(
                    loanId, reimburseId, settleAmount, "REIMBURSE_SETTLE", userId);
                
                LoanRepayment savedRepayment = loanRepaymentRepository.save(repayment);
                repayments.add(savedRepayment);
                settledLoanIds.add(loanId);
            }
            
            // 5. 更新报销单
            if (reimburse.getActualPaymentAmount() == null) {
                reimburse.setActualPaymentAmount(totalSettleAmount);
            } else {
                reimburse.setActualPaymentAmount(
                    reimburse.getActualPaymentAmount().add(totalSettleAmount));
            }
            
            // 如果报销金额全部用于核销，则报销单状态变更为已支付
            if (totalSettleAmount.compareTo(reimburse.getReimburseAmount()) >= 0) {
                reimburse.setStatus("PAID");
                reimburse.setPaymentTime(LocalDateTime.now());
            }
            
            expenseReimburseRepository.save(reimburse);
            
            // 6. 记录核销日志
            recordSettlementLog(reimburseId, settledLoanIds, totalSettleAmount, userId);
            
            return new SettlementResult(true, "手动核销借款成功", totalSettleAmount, repayments, settledLoanIds);
            
        } catch (Exception e) {
            return new SettlementResult(false, "手动核销借款失败: " + e.getMessage(), 
                BigDecimal.ZERO, Collections.emptyList(), Collections.emptyList());
        }
    }
    
    @Override
    public AvailableSettlement calculateAvailableSettlement(Long userId, BigDecimal reimburseAmount) {
        try {
            // 1. 获取用户未结清的借款（简化实现，实际应从借款表查询）
            List<SettleableLoan> settleableLoans = getUnsettledLoansForUser(userId);
            
            // 2. 计算可用核销金额
            BigDecimal totalAvailableAmount = settleableLoans.stream()
                    .map(SettleableLoan::getMaxSettleAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            boolean canSettle = totalAvailableAmount.compareTo(reimburseAmount) >= 0;
            
            // 3. 生成建议信息
            String suggestInfo = generateSettlementSuggestion(settleableLoans, reimburseAmount);
            
            return new AvailableSettlement(canSettle, totalAvailableAmount.min(reimburseAmount), 
                settleableLoans, suggestInfo);
                
        } catch (Exception e) {
            return new AvailableSettlement(false, BigDecimal.ZERO, 
                Collections.emptyList(), "计算可核销金额失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<LoanRepayment> getSettlementHistory(Long loanId) {
        return loanRepaymentRepository.findByLoanIdOrderByRepaymentTimeDesc(loanId);
    }
    
    @Override
    public OverdueStatus checkLoanOverdue(Long loanId) {
        try {
            // 1. 获取借款信息（简化实现）
            LocalDateTime loanTime = LocalDateTime.now().minusDays(45); // 模拟借款时间
            LocalDateTime dueTime = loanTime.plusDays(30); // 还款期限30天
            BigDecimal loanAmount = new BigDecimal("5000.00");
            
            // 2. 计算超期情况
            if (LocalDateTime.now().isBefore(dueTime)) {
                return new OverdueStatus(false, 0, loanAmount, "NORMAL", "借款尚未到期");
            }
            
            long overdueDays = Duration.between(dueTime, LocalDateTime.now()).toDays();
            
            // 3. 判断超期等级
            String overdueLevel;
            String suggestAction;
            
            if (overdueDays >= OVERDUE_CRITICAL_DAYS) {
                overdueLevel = "CRITICAL";
                suggestAction = "严重超期，请立即处理并联系法务部门";
            } else if (overdueDays >= OVERDUE_WARNING_DAYS) {
                overdueLevel = "WARNING";
                suggestAction = "超期严重，建议立即催收处理";
            } else {
                overdueLevel = "WARNING";
                suggestAction = "轻微超期，建议尽快处理";
            }
            
            return new OverdueStatus(true, (int) overdueDays, loanAmount, overdueLevel, suggestAction);
            
        } catch (Exception e) {
            return new OverdueStatus(false, 0, BigDecimal.ZERO, "ERROR", 
                "检查超期状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public ReminderResult sendLoanReminders(List<Long> loanIds) {
        try {
            int total = loanIds.size();
            int successCount = 0;
            List<Long> successIds = new ArrayList<>();
            List<Long> failedIds = new ArrayList<>();
            
            // 发送提醒（模拟实现）
            for (Long loanId : loanIds) {
                try {
                    boolean sent = sendLoanReminder(loanId);
                    if (sent) {
                        successCount++;
                        successIds.add(loanId);
                    } else {
                        failedIds.add(loanId);
                    }
                } catch (Exception e) {
                    failedIds.add(loanId);
                }
            }
            
            String message = String.format("成功发送%d/%d个借款提醒", successCount, total);
            boolean success = successCount > 0;
            
            return new ReminderResult(success, message, total, successCount, 
                total - successCount, successIds, failedIds);
                
        } catch (Exception e) {
            return new ReminderResult(false, "发送借款提醒失败: " + e.getMessage(), 
                loanIds.size(), 0, loanIds.size(), Collections.emptyList(), loanIds);
        }
    }
    
    // ========== 私有方法 ==========
    
    /**
     * 创建还款记录
     */
    private LoanRepayment createRepaymentRecord(Long loanId, Long reimburseId, 
                                              BigDecimal amount, String type, Long userId) {
        LoanRepayment repayment = new LoanRepayment();
        repayment.setLoanId(loanId);
        repayment.setReimburseId(reimburseId);
        repayment.setRepaymentAmount(amount);
        repayment.setRepaymentType(type);
        repayment.setStatus("COMPLETED");
        repayment.setRepaymentTime(LocalDateTime.now());
        repayment.setCreatedBy(userId);
        repayment.setCreatedTime(LocalDateTime.now());
        repayment.setUpdatedBy(userId);
        repayment.setUpdatedTime(LocalDateTime.now());
        repayment.setRemarks("报销核销还款");
        
        // 设置乐观锁版本（如果实体有的话）
        if (repayment.getVersion() == null) {
            repayment.setVersion(0L);
        }
        
        return repayment;
    }
    
    /**
     * 获取用户未结清的借款列表（简化实现）
     */
    private List<SettleableLoan> getUnsettledLoansForUser(Long userId) {
        // 模拟数据，实际应该从借款表查询
        List<SettleableLoan> loans = new ArrayList<>();
        
        // 模拟三笔借款
        loans.add(new SettleableLoan(1L, "LN20260001", new BigDecimal("2000.00"), 
            new BigDecimal("800.00"), new BigDecimal("800.00"), true, "超期15天"));
        
        loans.add(new SettleableLoan(2L, "LN20260002", new BigDecimal("3000.00"), 
            new BigDecimal("1500.00"), new BigDecimal("1500.00"), false, "正常"));
            
        loans.add(new SettleableLoan(3L, "LN20260003", new BigDecimal("1000.00"), 
            new BigDecimal("500.00"), new BigDecimal("500.00"), true, "超期5天"));
            
        return loans;
    }
    
    /**
     * 计算借款剩余金额（简化实现）
     */
    private BigDecimal calculateLoanRemainingAmount(Long loanId) {
        // 模拟数据
        switch (loanId.intValue()) {
            case 1: return new BigDecimal("800.00");
            case 2: return new BigDecimal("1500.00");
            case 3: return new BigDecimal("500.00");
            default: return BigDecimal.ZERO;
        }
    }
    
    /**
     * 生成核销建议信息
     */
    private String generateSettlementSuggestion(List<SettleableLoan> loans, BigDecimal reimburseAmount) {
        if (loans.isEmpty()) {
            return "当前无可核销的借款";
        }
        
        int overdueCount = (int) loans.stream().filter(SettleableLoan::isOverdue).count();
        BigDecimal totalAvailable = loans.stream()
                .map(SettleableLoan::getMaxSettleAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (totalAvailable.compareTo(reimburseAmount) < 0) {
            return String.format("可用核销金额%.2f元不足，仍需支付%.2f元", 
                totalAvailable.doubleValue(), reimburseAmount.subtract(totalAvailable).doubleValue());
        }
        
        if (overdueCount > 0) {
            return String.format("可核销%.2f元，其中%d笔超期借款建议优先处理", 
                reimburseAmount.doubleValue(), overdueCount);
        }
        
        return String.format("可核销%.2f元，建议按先进先出原则处理", reimburseAmount.doubleValue());
    }
    
    /**
     * 记录核销日志
     */
    private void recordSettlementLog(Long reimburseId, List<Long> loanIds, 
                                   BigDecimal amount, Long userId) {
        // 实际应该写入系统操作日志表
        String log = String.format("用户%d通过报销单%d核销借款%.2f元，涉及借款ID：%s", 
            userId, reimburseId, amount.doubleValue(), loanIds.toString());
        
        // 这里可以调用日志服务记录
        System.out.println("[LoanSettlement] " + log);
    }
    
    /**
     * 发送借款提醒（模拟实现）
     */
    private boolean sendLoanReminder(Long loanId) {
        // 实际应该集成消息服务发送邮件、短信、钉钉等
        System.out.println("[Reminder] 发送借款" + loanId + "还款提醒");
        return true; // 模拟发送成功
    }
}