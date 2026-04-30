package com.ccms.controller.report;

import com.ccms.common.response.ApiResponse;
import com.ccms.service.report.ExpenseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 综合报表分析控制器
 * 提供多维度的统计分析功能
 */
@RestController
@RequestMapping("/api/reports/comprehensive")
public class ComprehensiveReportController {

    @Autowired
    private ExpenseReportService expenseReportService;

    /**
     * 获取综合费用分析报告
     */
    @GetMapping("/analysis")
    public ApiResponse<Map<String, Object>> getComprehensiveAnalysis(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        Map<String, Object> analysis = new HashMap<>();
        
        // 部门费用统计
        List<ExpenseReportService.DepartmentExpenseStat> deptStats = expenseReportService.getDepartmentExpenseStats(startDate, endDate);
        analysis.put("departmentStats", deptStats);
        
        // 费用类型统计
        List<ExpenseReportService.ExpenseTypeStat> typeStats = expenseReportService.getExpenseTypeStats(startDate, endDate, null);
        analysis.put("expenseTypeStats", typeStats);
        
        // 月度趋势分析（默认当前年份）
        List<ExpenseReportService.MonthlyExpenseTrend> monthlyTrend = expenseReportService.getMonthlyExpenseTrend(startDate.getYear(), null);
        analysis.put("monthlyTrend", monthlyTrend);
        
        // 预算执行分析
        List<ExpenseReportService.BudgetExecution> budgetExecution = expenseReportService.getBudgetExecutionStats(startDate, endDate);
        analysis.put("budgetExecution", budgetExecution);
        
        // 费用排行榜（前10）
        List<ExpenseReportService.ExpenseRankItem> expenseRanking = expenseReportService.getExpenseRanking(startDate, endDate, 10);
        analysis.put("expenseRanking", expenseRanking);
        
        // 计算总结指标
        Map<String, Object> summary = calculateSummaryMetrics(deptStats, typeStats, budgetExecution);
        analysis.put("summary", summary);
        
        return ApiResponse.success(analysis);
    }
    
    /**
     * 获取费用对比分析
     */
    @GetMapping("/comparison")
    public ApiResponse<Map<String, Object>> getComparisonAnalysis(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate baseStartDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate baseEndDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate compareStartDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate compareEndDate) {
        
        Map<String, Object> comparison = new HashMap<>();
        
        // 基期统计数据
        List<ExpenseReportService.DepartmentExpenseStat> baseStats = expenseReportService.getDepartmentExpenseStats(baseStartDate, baseEndDate);
        List<ExpenseReportService.ExpenseTypeStat> baseTypeStats = expenseReportService.getExpenseTypeStats(baseStartDate, baseEndDate, null);
        List<ExpenseReportService.BudgetExecution> baseBudgetStats = expenseReportService.getBudgetExecutionStats(baseStartDate, baseEndDate);
        
        // 对比期统计数据
        List<ExpenseReportService.DepartmentExpenseStat> compareStats = expenseReportService.getDepartmentExpenseStats(compareStartDate, compareEndDate);
        List<ExpenseReportService.ExpenseTypeStat> compareTypeStats = expenseReportService.getExpenseTypeStats(compareStartDate, compareEndDate, null);
        List<ExpenseReportService.BudgetExecution> compareBudgetStats = expenseReportService.getBudgetExecutionStats(compareStartDate, compareEndDate);
        
        comparison.put("basePeriod", Map.of(
            "departmentStats", baseStats,
            "expenseTypeStats", baseTypeStats,
            "budgetExecution", baseBudgetStats
        ));
        
        comparison.put("comparePeriod", Map.of(
            "departmentStats", compareStats,
            "expenseTypeStats", compareTypeStats,
            "budgetExecution", compareBudgetStats
        ));
        
        // 计算增长率
        BigDecimal baseTotal = calculateTotalAmount(baseStats);
        BigDecimal compareTotal = calculateTotalAmount(compareStats);
        BigDecimal growthRate = baseTotal.compareTo(BigDecimal.ZERO) == 0 ? 
            compareTotal.multiply(BigDecimal.valueOf(100)) :
            compareTotal.subtract(baseTotal).divide(baseTotal, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
        
        comparison.put("growthRate", growthRate);
        comparison.put("baseTotal", baseTotal);
        comparison.put("compareTotal", compareTotal);
        
        return ApiResponse.success(comparison);
    }
    
    /**
     * 获取预算执行预警报告
     */
    @GetMapping("/budget-alerts")
    public ApiResponse<List<Map<String, Object>>> getBudgetAlerts(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "80") int warningThreshold,
            @RequestParam(defaultValue = "90") int alertThreshold) {
        
        List<ExpenseReportService.BudgetExecution> budgetExecution = expenseReportService.getBudgetExecutionStats(startDate, endDate);
        List<Map<String, Object>> alerts = budgetExecution.stream()
            .filter(budget -> budget.getExecutionRate() != null)
            .map(budget -> {
                Map<String, Object> alert = new HashMap<>();
                alert.put("budgetId", budget.getBudgetId());
                alert.put("budgetName", budget.getBudgetName());
                alert.put("executionRate", budget.getExecutionRate().longValue());
                alert.put("totalBudget", budget.getTotalBudget());
                alert.put("usedAmount", budget.getUsedAmount());
                
                // 判断预警级别
                if (budget.getExecutionRate().longValue() >= alertThreshold) {
                    alert.put("alertLevel", "CRITICAL");
                } else if (budget.getExecutionRate().longValue() >= warningThreshold) {
                    alert.put("alertLevel", "WARNING");
                } else {
                    alert.put("alertLevel", "NORMAL");
                }
                
                return alert;
            })
            .filter(alert -> !"NORMAL".equals(alert.get("alertLevel")))
            .toList();
        
        return ApiResponse.success(alerts);
    }
    
    private Map<String, Object> calculateSummaryMetrics(
            List<ExpenseReportService.DepartmentExpenseStat> deptStats,
            List<ExpenseReportService.ExpenseTypeStat> typeStats,
            List<ExpenseReportService.BudgetExecution> budgetExecution) {
        
        Map<String, Object> summary = new HashMap<>();
        
        // 总费用金额
        BigDecimal totalExpense = deptStats.stream()
            .map(ExpenseReportService.DepartmentExpenseStat::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 平均预算执行率
        Double avgExecutionRate = budgetExecution.stream()
            .filter(budget -> budget.getExecutionRate() != null)
            .mapToDouble(budget -> budget.getExecutionRate().doubleValue())
            .average()
            .orElse(0.0);
        
        // 费用类型数量
        int expenseTypeCount = typeStats.size();
        
        // 部门数量
        int departmentCount = deptStats.size();
        
        summary.put("totalExpense", totalExpense);
        summary.put("avgExecutionRate", String.format("%.2f%%", avgExecutionRate));
        summary.put("expenseTypeCount", expenseTypeCount);
        summary.put("departmentCount", departmentCount);
        
        return summary;
    }
    
    private BigDecimal calculateTotalAmount(List<ExpenseReportService.DepartmentExpenseStat> deptStats) {
        return deptStats.stream()
            .map(ExpenseReportService.DepartmentExpenseStat::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}