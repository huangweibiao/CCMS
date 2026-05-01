package com.ccms.service.statistics;

import com.ccms.entity.statistics.ExpenseStatistics;
import com.ccms.vo.statistics.ExpenseTrendAnalysis;
import com.ccms.vo.statistics.BudgetExecutionAnalysis;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 费用统计分析服务接口
 */
public interface ExpenseStatisticsService {

    /**
     * 生成日统计报表
     */
    void generateDailyStatistics(LocalDate date);

    /**
     * 生成月度统计报表
     */
    void generateMonthlyStatistics(int year, int month);

    /**
     * 生成年度统计报表
     */
    void generateYearlyStatistics(int year);

    /**
     * 获取部门费用统计
     */
    List<ExpenseStatistics> getDepartmentExpenseStats(LocalDate startDate, LocalDate endDate, Long departmentId);

    /**
     * 获取费用类型统计
     */
    List<ExpenseStatistics> getExpenseTypeStats(LocalDate startDate, LocalDate endDate, String expenseTypeCode);

    /**
     * 获取预算执行分析
     */
    BudgetExecutionAnalysis getBudgetExecutionAnalysis(int year, int month, Long departmentId);

    /**
     * 获取费用趋势分析
     */
    ExpenseTrendAnalysis getExpenseTrendAnalysis(LocalDate startDate, LocalDate endDate, String dimension);

    /**
     * 获取部门排名统计
     */
    List<Map<String, Object>> getDepartmentRanking(LocalDate startDate, LocalDate endDate);

    /**
     * 获取费用类型占比分析
     */
    List<Map<String, Object>> getExpenseTypeRatioAnalysis(LocalDate startDate, LocalDate endDate, Long departmentId);

    /**
     * 获取审批效率统计
     */
    Map<String, Object> getApprovalEfficiencyStats(LocalDate startDate, LocalDate endDate);

    /**
     * 获取异常费用检测
     */
    List<Map<String, Object>> detectAbnormalExpenses(LocalDate startDate, LocalDate endDate);

    /**
     * 获取费用预警信息
     */
    List<Map<String, Object>> getExpenseWarnings(Long departmentId);

    /**
     * 计算平均报销周期
     */
    BigDecimal getAverageReimburseCycle(LocalDate startDate, LocalDate endDate);

    /**
     * 获取费用结构分析
     */
    Map<String, BigDecimal> getExpenseStructureAnalysis(LocalDate startDate, LocalDate endDate);

    /**
     * 获取年度预算执行概况
     */
    Map<String, Object> getYearlyBudgetExecutionOverview(int year);

    /**
     * 获取费用月度对比分析
     */
    Map<String, Object> getMonthlyExpenseComparison(int year, int baseMonth);

    /**
     * 获取预算使用预测
     */
    Map<String, Object> getBudgetUsageForecast(int remainingDays, BigDecimal currentUsage);
}