package com.ccms.controller.report;

import com.ccms.common.response.ApiResponse;
import com.ccms.service.report.ExpenseReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 费用统计报表控制器
 */
@RestController
@RequestMapping("/api/reports/expense")
public class ExpenseReportController {

    @Autowired
    private ExpenseReportService expenseReportService;

    /**
     * 获取部门费用统计
     */
    @GetMapping("/department-stats")
    public ApiResponse<List<ExpenseReportService.DepartmentExpenseStat>> getDepartmentExpenseStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        List<ExpenseReportService.DepartmentExpenseStat> stats = expenseReportService.getDepartmentExpenseStats(startDate, endDate);
        return ApiResponse.success(stats);
    }

    /**
     * 获取费用类型统计
     */
    @GetMapping("/type-stats")
    public ApiResponse<List<ExpenseReportService.ExpenseTypeStat>> getExpenseTypeStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) Long deptId) {
        
        List<ExpenseReportService.ExpenseTypeStat> stats = expenseReportService.getExpenseTypeStats(startDate, endDate, deptId);
        return ApiResponse.success(stats);
    }

    /**
     * 获取月度费用趋势分析
     */
    @GetMapping("/monthly-trend")
    public ApiResponse<List<ExpenseReportService.MonthlyExpenseTrend>> getMonthlyExpenseTrend(
            @RequestParam int year,
            @RequestParam(required = false) Long deptId) {
        
        List<ExpenseReportService.MonthlyExpenseTrend> trend = expenseReportService.getMonthlyExpenseTrend(year, deptId);
        return ApiResponse.success(trend);
    }

    /**
     * 获取费用支出排行榜
     */
    @GetMapping("/ranking")
    public ApiResponse<List<ExpenseReportService.ExpenseRankItem>> getExpenseRanking(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        
        List<ExpenseReportService.ExpenseRankItem> ranking = expenseReportService.getExpenseRanking(startDate, endDate, limit);
        return ApiResponse.success(ranking);
    }

    /**
     * 获取预算执行分析
     */
    @GetMapping("/budget-execution")
    public ApiResponse<List<ExpenseReportService.BudgetExecution>> getBudgetExecutionStats(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        List<ExpenseReportService.BudgetExecution> execution = expenseReportService.getBudgetExecutionStats(startDate, endDate);
        return ApiResponse.success(execution);
    }
}