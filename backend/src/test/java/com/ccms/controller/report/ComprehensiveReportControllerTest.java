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
 * 综合报表分析控制器单元测试
 */
@WebMvcTest(ComprehensiveReportController.class)
class ComprehensiveReportControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseReportService expenseReportService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldGetComprehensiveAnalysisSuccessfully() throws Exception {
        // Given
        when(expenseReportService.getDepartmentExpenseStats(any(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseReportService.getExpenseTypeStats(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseReportService.getMonthlyExpenseTrend(anyInt(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseReportService.getBudgetExecutionStats(any(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseReportService.getExpenseRanking(any(), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/reports/comprehensive/analysis")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.departmentStats").exists())
                .andExpect(jsonPath("$.data.expenseTypeStats").exists())
                .andExpect(jsonPath("$.data.monthlyTrend").exists())
                .andExpect(jsonPath("$.data.budgetExecution").exists())
                .andExpect(jsonPath("$.data.expenseRanking").exists())
                .andExpect(jsonPath("$.data.summary").exists());
    }

    @Test
    void shouldGetComparisonAnalysisSuccessfully() throws Exception {
        // Given
        when(expenseReportService.getDepartmentExpenseStats(any(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseReportService.getExpenseTypeStats(any(), any(), any()))
                .thenReturn(Collections.emptyList());
        when(expenseReportService.getBudgetExecutionStats(any(), any()))
                .thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/reports/comprehensive/comparison")
                        .param("baseStartDate", "2025-01-01")
                        .param("baseEndDate", "2025-06-30")
                        .param("compareStartDate", "2024-01-01")
                        .param("compareEndDate", "2024-06-30"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.basePeriod").exists())
                .andExpect(jsonPath("$.data.comparePeriod").exists())
                .andExpect(jsonPath("$.data.growthRate").exists());
    }

    @Test
    void shouldGetBudgetAlertsSuccessfully() throws Exception {
        // Given
        List<ExpenseReportService.BudgetExecution> executions = Arrays.asList(
                createBudgetExecution(1L, "预算A", new BigDecimal("85.0")),
                createBudgetExecution(2L, "预算B", new BigDecimal("95.0"))
        );
        when(expenseReportService.getBudgetExecutionStats(any(), any()))
                .thenReturn(executions);

        // When & Then
        mockMvc.perform(get("/api/reports/comprehensive/budget-alerts")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31")
                        .param("warningThreshold", "80")
                        .param("alertThreshold", "90"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    private ExpenseReportService.BudgetExecution createBudgetExecution(Long budgetId, String budgetName, BigDecimal rate) {
        ExpenseReportService.BudgetExecution execution = new ExpenseReportService.BudgetExecution();
        execution.setBudgetId(budgetId);
        execution.setBudgetName(budgetName);
        execution.setExecutionRate(rate);
        execution.setTotalBudget(new BigDecimal("100000"));
        execution.setUsedAmount(new BigDecimal("80000"));
        return execution;
    }
}
