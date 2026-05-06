package com.ccms.controller.report;

import com.ccms.controller.BaseControllerTest;
import com.ccms.service.report.ExpenseReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用统计报表控制器单元测试
 */
@WebMvcTest(ExpenseReportController.class)
class ExpenseReportControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseReportService expenseReportService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldGetDepartmentExpenseStatsSuccessfully() throws Exception {
        // Given
        List<ExpenseReportService.DepartmentExpenseStat> stats = Arrays.asList(
                createDeptStat("技术部", new BigDecimal("50000")),
                createDeptStat("销售部", new BigDecimal("30000"))
        );
        when(expenseReportService.getDepartmentExpenseStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/reports/expense/department-stats")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldGetExpenseTypeStatsSuccessfully() throws Exception {
        // Given
        List<ExpenseReportService.ExpenseTypeStat> stats = Arrays.asList(
                createTypeStat("差旅费", new BigDecimal("20000")),
                createTypeStat("办公费", new BigDecimal("10000"))
        );
        when(expenseReportService.getExpenseTypeStats(any(LocalDate.class), any(LocalDate.class), any()))
                .thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/reports/expense/type-stats")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .param("deptId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGetMonthlyExpenseTrendSuccessfully() throws Exception {
        // Given
        List<ExpenseReportService.MonthlyExpenseTrend> trends = Arrays.asList(
                createMonthlyTrend(1, new BigDecimal("10000")),
                createMonthlyTrend(2, new BigDecimal("12000"))
        );
        when(expenseReportService.getMonthlyExpenseTrend(eq(2025), any()))
                .thenReturn(trends);

        // When & Then
        mockMvc.perform(get("/api/reports/expense/monthly-trend")
                        .param("year", "2025")
                        .param("deptId", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGetExpenseRankingSuccessfully() throws Exception {
        // Given
        List<ExpenseReportService.ExpenseRankItem> ranking = Arrays.asList(
                createRankItem("张三", new BigDecimal("15000")),
                createRankItem("李四", new BigDecimal("12000"))
        );
        when(expenseReportService.getExpenseRanking(any(LocalDate.class), any(LocalDate.class), eq(10)))
                .thenReturn(ranking);

        // When & Then
        mockMvc.perform(get("/api/reports/expense/ranking")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .param("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGetBudgetExecutionStatsSuccessfully() throws Exception {
        // Given
        List<ExpenseReportService.BudgetExecution> executions = Arrays.asList(
                createBudgetExecution(1L, "项目A预算", new BigDecimal("80.5")),
                createBudgetExecution(2L, "项目B预算", new BigDecimal("65.2"))
        );
        when(expenseReportService.getBudgetExecutionStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(executions);

        // When & Then
        mockMvc.perform(get("/api/reports/expense/budget-execution")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    private ExpenseReportService.DepartmentExpenseStat createDeptStat(String deptName, BigDecimal amount) {
        ExpenseReportService.DepartmentExpenseStat stat = new ExpenseReportService.DepartmentExpenseStat();
        stat.setDeptName(deptName);
        stat.setTotalAmount(amount);
        return stat;
    }

    private ExpenseReportService.ExpenseTypeStat createTypeStat(String typeName, BigDecimal amount) {
        ExpenseReportService.ExpenseTypeStat stat = new ExpenseReportService.ExpenseTypeStat();
        stat.setTypeName(typeName);
        stat.setTotalAmount(amount);
        return stat;
    }

    private ExpenseReportService.MonthlyExpenseTrend createMonthlyTrend(int month, BigDecimal amount) {
        ExpenseReportService.MonthlyExpenseTrend trend = new ExpenseReportService.MonthlyExpenseTrend();
        trend.setMonth(month);
        trend.setTotalAmount(amount);
        return trend;
    }

    private ExpenseReportService.ExpenseRankItem createRankItem(String userName, BigDecimal amount) {
        ExpenseReportService.ExpenseRankItem item = new ExpenseReportService.ExpenseRankItem();
        item.setUserName(userName);
        item.setTotalAmount(amount);
        return item;
    }

    private ExpenseReportService.BudgetExecution createBudgetExecution(Long budgetId, String budgetName, BigDecimal rate) {
        ExpenseReportService.BudgetExecution execution = new ExpenseReportService.BudgetExecution();
        execution.setBudgetId(budgetId);
        execution.setBudgetName(budgetName);
        execution.setExecutionRate(rate);
        return execution;
    }
}
