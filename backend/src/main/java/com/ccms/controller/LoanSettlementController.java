package com.ccms.controller;

import com.ccms.entity.expense.LoanRepayment;
import com.ccms.service.LoanSettlementService;
import com.ccms.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 借款核销控制器
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/loan-settlement")
public class LoanSettlementController {
    
    @Autowired
    private LoanSettlementService loanSettlementService;
    
    /**
     * 自动核销借款
     * 
     * @param reimburseId 报销单ID
     * @param userId 操作人ID
     * @return 核销结果
     */
    @PostMapping("/auto-settle")
    public Result<LoanSettlementService.SettlementResult> autoSettleLoan(
            @RequestParam Long reimburseId,
            @RequestParam Long userId) {
        try {
            LoanSettlementService.SettlementResult result = 
                loanSettlementService.autoSettleLoan(reimburseId, userId);
            
            if (result.isSuccess()) {
                return Result.success(result, result.getMessage());
            } else {
                return Result.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return Result.error(500, "自动核销失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动指定核销借款
     * 
     * @param request 核销请求参数
     * @return 核销结果
     */
    @PostMapping("/manual-settle")
    public Result<LoanSettlementService.SettlementResult> manualSettleLoan(
            @RequestBody ManualSettleRequest request) {
        try {
            LoanSettlementService.SettlementResult result = 
                loanSettlementService.manualSettleLoan(
                    request.getReimburseId(), 
                    request.getLoanIds(),
                    request.getSettleAmounts(),
                    request.getUserId()
                );
            
            if (result.isSuccess()) {
                return Result.success(result, result.getMessage());
            } else {
                return Result.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return Result.error(500, "手动核销失败: " + e.getMessage());
        }
    }
    
    /**
     * 计算可核销的借款金额
     * 
     * @param userId 用户ID
     * @param reimburseAmount 报销金额
     * @return 可核销信息
     */
    @GetMapping("/available-settlement")
    public Result<LoanSettlementService.AvailableSettlement> getAvailableSettlement(
            @RequestParam Long userId,
            @RequestParam BigDecimal reimburseAmount) {
        try {
            LoanSettlementService.AvailableSettlement available = 
                loanSettlementService.calculateAvailableSettlement(userId, reimburseAmount);
            
            return Result.success(available, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "查询可核销金额失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取借款核销历史
     * 
     * @param loanId 借款ID
     * @return 核销历史记录
     */
    @GetMapping("/settlement-history/{loanId}")
    public Result<List<LoanRepayment>> getSettlementHistory(@PathVariable Long loanId) {
        try {
            List<LoanRepayment> history = loanSettlementService.getSettlementHistory(loanId);
            return Result.success(history, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "查询核销历史失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查借款超期状态
     * 
     * @param loanId 借款ID
     * @return 超期状态信息
     */
    @GetMapping("/overdue-status/{loanId}")
    public Result<LoanSettlementService.OverdueStatus> checkLoanOverdue(@PathVariable Long loanId) {
        try {
            LoanSettlementService.OverdueStatus status = loanSettlementService.checkLoanOverdue(loanId);
            return Result.success(status, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "检查超期状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 发送借款提醒
     * 
     * @param loanIds 借款ID列表
     * @return 提醒发送结果
     */
    @PostMapping("/send-reminders")
    public Result<LoanSettlementService.ReminderResult> sendLoanReminders(@RequestBody List<Long> loanIds) {
        try {
            LoanSettlementService.ReminderResult result = loanSettlementService.sendLoanReminders(loanIds);
            
            if (result.isSuccess()) {
                return Result.success(result, result.getMessage());
            } else {
                return Result.error(400, result.getMessage());
            }
        } catch (Exception e) {
            return Result.error(500, "发送提醒失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量检查借款超期状态
     * 
     * @param loanIds 借款ID列表
     * @return 超期状态映射
     */
    @PostMapping("/batch-overdue-status")
    public Result<Map<Long, LoanSettlementService.OverdueStatus>> batchCheckLoanOverdue(
            @RequestBody List<Long> loanIds) {
        try {
            Map<Long, LoanSettlementService.OverdueStatus> statusMap = loanIds.stream()
                    .collect(java.util.stream.Collectors.toMap(
                        loanId -> loanId,
                        loanSettlementService::checkLoanOverdue,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                    ));
            
            return Result.success(statusMap, "批量查询成功");
        } catch (Exception e) {
            return Result.error(500, "批量检查超期状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取还款统计信息
     * 
     * @param year 年份
     * @param month 月份
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<RepaymentStatistics> getRepaymentStatistics(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            // 模拟统计信息
            RepaymentStatistics stats = new RepaymentStatistics();
            stats.setTotalRepaymentCount(125);
            stats.setTotalRepaymentAmount(new BigDecimal("85000.00"));
            stats.setMonthlyAverage(new BigDecimal("4250.00"));
            stats.setOverdueLoanCount(8);
            stats.setSettleRatio(0.85);
            
            return Result.success(stats, "查询成功");
        } catch (Exception e) {
            return Result.error(500, "获取统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 手动核销请求参数
     */
    public static class ManualSettleRequest {
        private Long reimburseId;
        private List<Long> loanIds;
        private Map<Long, BigDecimal> settleAmounts;
        private Long userId;
        
        public Long getReimburseId() { return reimburseId; }
        public void setReimburseId(Long reimburseId) { this.reimburseId = reimburseId; }
        
        public List<Long> getLoanIds() { return loanIds; }
        public void setLoanIds(List<Long> loanIds) { this.loanIds = loanIds; }
        
        public Map<Long, BigDecimal> getSettleAmounts() { return settleAmounts; }
        public void setSettleAmounts(Map<Long, BigDecimal> settleAmounts) { this.settleAmounts = settleAmounts; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    
    /**
     * 还款统计信息
     */
    public static class RepaymentStatistics {
        private int totalRepaymentCount;
        private BigDecimal totalRepaymentAmount;
        private BigDecimal monthlyAverage;
        private int overdueLoanCount;
        private double settleRatio;
        
        public int getTotalRepaymentCount() { return totalRepaymentCount; }
        public void setTotalRepaymentCount(int totalRepaymentCount) { this.totalRepaymentCount = totalRepaymentCount; }
        
        public BigDecimal getTotalRepaymentAmount() { return totalRepaymentAmount; }
        public void setTotalRepaymentAmount(BigDecimal totalRepaymentAmount) { this.totalRepaymentAmount = totalRepaymentAmount; }
        
        public BigDecimal getMonthlyAverage() { return monthlyAverage; }
        public void setMonthlyAverage(BigDecimal monthlyAverage) { this.monthlyAverage = monthlyAverage; }
        
        public int getOverdueLoanCount() { return overdueLoanCount; }
        public void setOverdueLoanCount(int overdueLoanCount) { this.overdueLoanCount = overdueLoanCount; }
        
        public double getSettleRatio() { return settleRatio; }
        public void setSettleRatio(double settleRatio) { this.settleRatio = settleRatio; }
    }
}