package com.ccms.service.report.impl;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.budget.Budget;
import com.ccms.repository.expense.ExpenseReimburseRepository;
import com.ccms.repository.BudgetRepository;
import com.ccms.service.report.ExpenseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 费用统计报表服务实现类
 */
@Service
public class ExpenseReportServiceImpl implements ExpenseReportService {

    @Autowired
    private ExpenseReimburseRepository expenseReimburseRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Override
    public List<DepartmentExpenseStat> getDepartmentExpenseStats(LocalDate startDate, LocalDate endDate) {
        // 实现部门费用统计逻辑
        List<Object[]> rawStats = expenseReimburseRepository.findDepartmentExpenseStats(startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        List<DepartmentExpenseStat> stats = new ArrayList<>();
        for (Object[] raw : rawStats) {
            DepartmentExpenseStat stat = new DepartmentExpenseStat(
                ((Number) raw[0]).longValue(),
                (String) raw[1],
                (BigDecimal) raw[2]
            );
            stat.setApplyCount(((Number) raw[3]).intValue());
            stats.add(stat);
        }
        
        // 计算预算使用情况
        calculateBudgetUsage(stats);
        
        return stats;
    }

    @Override
    public List<ExpenseTypeStat> getExpenseTypeStats(LocalDate startDate, LocalDate endDate, Long deptId) {
        // 实现费用类型统计逻辑
        List<Object[]> rawStats = expenseReimburseRepository.findExpenseTypeStats(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59), 
                deptId);
        
        // 计算总金额
        BigDecimal totalAmount = rawStats.stream()
                .map(raw -> (BigDecimal) raw[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<ExpenseTypeStat> stats = new ArrayList<>();
        for (Object[] raw : rawStats) {
            ExpenseTypeStat stat = new ExpenseTypeStat(
                (String) raw[0],
                (BigDecimal) raw[1]
            );
            stat.setCount(((Number) raw[2]).intValue());
            
            // 计算百分比
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal percentage = stat.getAmount()
                        .divide(totalAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                stat.setPercentage(percentage);
            }
            
            stats.add(stat);
        }
        
        return stats;
    }

    @Override
    public List<MonthlyExpenseTrend> getMonthlyExpenseTrend(int year, Long deptId) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        
        List<Object[]> rawTrends = expenseReimburseRepository.findMonthlyExpenseTrend(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59), 
                deptId);
        
        // 填充完整的12个月数据
        Map<String, MonthlyExpenseTrend> trendMap = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            String month = String.format("%02d", i);
            trendMap.put(month, new MonthlyExpenseTrend(month + "月", BigDecimal.ZERO));
        }
        
        // 更新实际数据
        for (Object[] raw : rawTrends) {
            String month = ((String) raw[0]).replace("月", "");
            MonthlyExpenseTrend trend = new MonthlyExpenseTrend(
                month + "月",
                (BigDecimal) raw[1]
            );
            trend.setCount(((Number) raw[2]).intValue());
            trendMap.put(month, trend);
        }
        
        return new ArrayList<>(trendMap.values());
    }

    @Override
    public List<ExpenseRankItem> getExpenseRanking(LocalDate startDate, LocalDate endDate, int limit) {
        List<Object[]> rawRanking = expenseReimburseRepository.findExpenseRanking(
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59), 
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "amount")));
        
        return rawRanking.stream()
                .map(raw -> {
                    ExpenseRankItem item = new ExpenseRankItem(
                        (String) raw[0],
                        (BigDecimal) raw[1]
                    );
                    item.setCount(((Number) raw[2]).intValue());
                    return item;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BudgetExecution> getBudgetExecutionStats(LocalDate startDate, LocalDate endDate) {
        List<Budget> budgets = budgetRepository.findActiveBudgets();
        
        List<BudgetExecution> executions = new ArrayList<>();
        for (Budget budget : budgets) {
            // 查询预算期内费用金额
            BigDecimal usedAmount = expenseReimburseRepository.findSumAmountByBudgetId(
                    budget.getId(), 
                    startDate.atStartOfDay(), 
                    endDate.atTime(23, 59, 59));
            
            if (usedAmount == null) {
                usedAmount = BigDecimal.ZERO;
            }
            
            BudgetExecution execution = new BudgetExecution(
                budget.getBudgetName(),
                budget.getBudgetAmount(),
                usedAmount
            );
            execution.setBudgetId(budget.getId());
            
            // 计算执行率
            if (budget.getBudgetAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal rate = usedAmount
                        .divide(budget.getBudgetAmount(), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                execution.setExecutionRate(rate);
            }
            
            executions.add(execution);
        }
        
        return executions.stream()
                .sorted((a, b) -> {
                    BigDecimal rateA = a.getExecutionRate() != null ? a.getExecutionRate() : BigDecimal.ZERO;
                    BigDecimal rateB = b.getExecutionRate() != null ? b.getExecutionRate() : BigDecimal.ZERO;
                    return rateB.compareTo(rateA);
                })
                .collect(Collectors.toList());
    }

    /**
     * 计算预算使用情况
     */
    private void calculateBudgetUsage(List<DepartmentExpenseStat> stats) {
        // 这里可以添加预算使用率的计算逻辑
        // 从预算表中获取各部门的预算信息
        for (DepartmentExpenseStat stat : stats) {
            BigDecimal budgetAmount = budgetRepository.findBudgetByDeptId(stat.getDeptId());
            if (budgetAmount != null && budgetAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal usageRate = stat.getTotalAmount()
                        .divide(budgetAmount, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                stat.setBudgetUsed(usageRate);
            } else {
                stat.setBudgetUsed(BigDecimal.ZERO);
            }
        }
    }

    /**
     * 获取自定义报表数据
     */
    public Map<String, Object> getCustomReport(ReportQuery query) {
        Map<String, Object> result = new HashMap<>();
        
        // 根据查询条件生成自定义报表数据
        // 这里可以实现各种复杂的数据聚合和统计逻辑
        
        return result;
    }
    
    /**
     * 报表查询条件类
     */
    public static class ReportQuery {
        private LocalDate startDate;
        private LocalDate endDate;
        private List<Long> deptIds;
        private List<String> expenseTypes;
        private String reportType;
        private Map<String, Object> customParams;

        // Getters and Setters
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public List<Long> getDeptIds() { return deptIds; }
        public void setDeptIds(List<Long> deptIds) { this.deptIds = deptIds; }
        
        public List<String> getExpenseTypes() { return expenseTypes; }
        public void setExpenseTypes(List<String> expenseTypes) { this.expenseTypes = expenseTypes; }
        
        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
        
        public Map<String, Object> getCustomParams() { return customParams; }
        public void setCustomParams(Map<String, Object> customParams) { this.customParams = customParams; }
    }
}