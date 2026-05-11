package com.ccms.controller.budget;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 预算管理控制器单元测试
 */
@WebMvcTest(BudgetController.class)
class BudgetControllerTest extends ControllerTestBase {

    @MockBean
    private BudgetService budgetService;

    private BudgetMain createTestBudget(Long id, String budgetNo, Integer status) {
        BudgetMain budget = new BudgetMain();
        budget.setId(id);
        budget.setBudgetNo(budgetNo);
        budget.setBudgetYear(2025);
        budget.setBudgetPeriod("YEAR");
        budget.setDeptId(1L);
        budget.setCostCenterId(1L);
        budget.setTotalAmount(new BigDecimal("100000.00"));
        budget.setUsedAmount(new BigDecimal("30000.00"));
        budget.setFrozenAmount(new BigDecimal("10000.00"));
        budget.setStatus(status);
        budget.setBudgetName("2025年度预算");
        return budget;
    }

    @Test
    void shouldReturnBudgetListWhenQuerySuccess() throws Exception {
        // given
        BudgetMain budget = createTestBudget(1L, "B2025001", 1);
        Page<BudgetMain> page = new PageImpl<>(
                Collections.singletonList(budget),
                PageRequest.of(0, 20),
                1
        );
        when(budgetService.checkPermission(anyString(), eq("budget:list"))).thenReturn(true);
        when(budgetService.getBudgetList(anyInt(), anyInt(), any(), any(), any())).thenReturn(page);

        // when & then
        performGet("/api/budgets")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].budgetNo").value("B2025001"));
    }

    @Test
    void shouldReturnBudgetListWithFilters() throws Exception {
        // given
        BudgetMain budget = createTestBudget(1L, "B2025001", 1);
        Page<BudgetMain> page = new PageImpl<>(
                Collections.singletonList(budget),
                PageRequest.of(0, 20),
                1
        );
        when(budgetService.checkPermission(anyString(), eq("budget:list"))).thenReturn(true);
        when(budgetService.getBudgetList(eq(0), eq(20), eq(2025), eq(1L), eq(1))).thenReturn(page);

        // when & then
        performGet("/api/budgets?year=2025&deptId=1&status=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].budgetYear").value(2025));
    }

    @Test
    void shouldReturnBudgetDetailWhenGetByIdSuccess() throws Exception {
        // given
        BudgetMain budget = createTestBudget(1L, "B2025001", 1);
        when(budgetService.checkPermission(anyString(), eq("budget:view"))).thenReturn(true);
        when(budgetService.getBudgetById(1L)).thenReturn(budget);

        // when & then
        performGet("/api/budgets/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.budgetNo").value("B2025001"));
    }

    @Test
    void shouldCreateBudgetSuccess() throws Exception {
        // given
        BudgetMain budget = createTestBudget(1L, "B2025001", 0);
        when(budgetService.checkPermission(anyString(), eq("budget:create"))).thenReturn(true);
        doReturn(budget).when(budgetService).createBudget(any(BudgetMain.class));

        // when & then
        performPost("/api/budgets", budget)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算创建成功"));
    }

    @Test
    void shouldUpdateBudgetSuccess() throws Exception {
        // given
        BudgetMain budget = createTestBudget(1L, "B2025001", 1);
        when(budgetService.checkPermission(anyString(), eq("budget:update"))).thenReturn(true);
        doReturn(budget).when(budgetService).updateBudget(any(BudgetMain.class));

        // when & then
        performPut("/api/budgets/1", budget)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算更新成功"));
    }

    @Test
    void shouldDeleteBudgetSuccess() throws Exception {
        // given
        when(budgetService.checkPermission(anyString(), eq("budget:delete"))).thenReturn(true);
        doNothing().when(budgetService).deleteBudget(1L);

        // when & then
        performDelete("/api/budgets/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算删除成功"));
    }

    @Test
    void shouldSubmitBudgetSuccess() throws Exception {
        // given
        when(budgetService.checkPermission(anyString(), eq("budget:submit"))).thenReturn(true);
        doNothing().when(budgetService).submitBudgetForApproval(1L);

        // when & then
        performPost("/api/budgets/1/submit")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算提交审批成功"));
    }

    @Test
    void shouldApproveBudgetSuccess() throws Exception {
        // given
        Map<String, Object> approval = new HashMap<>();
        approval.put("result", 1);
        approval.put("comment", "同意");
        approval.put("approverId", 2L);

        when(budgetService.checkPermission(anyString(), eq("budget:approve"))).thenReturn(true);
        doNothing().when(budgetService).approveBudget(anyLong(), anyLong(), anyInt(), anyString());

        // when & then
        performPost("/api/budgets/1/approve", approval)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算审批完成"));
    }

    @Test
    void shouldAllocateBudgetSuccess() throws Exception {
        // given
        Map<String, Object> allocation = new HashMap<>();
        allocation.put("deptId", 2L);
        allocation.put("amount", 50000.0);

        when(budgetService.checkPermission(anyString(), eq("budget:allocate"))).thenReturn(true);
        doNothing().when(budgetService).allocateBudget(anyLong(), anyLong(), anyDouble());

        // when & then
        performPost("/api/budgets/1/allocate", allocation)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算分配成功"));
    }

    @Test
    void shouldAdjustBudgetSuccess() throws Exception {
        // given
        Map<String, Object> adjustment = new HashMap<>();
        adjustment.put("newAmount", 150000.0);
        adjustment.put("reason", "业务扩展需要");

        when(budgetService.checkPermission(anyString(), eq("budget:adjust"))).thenReturn(true);
        doNothing().when(budgetService).adjustBudgetAmount(anyLong(), anyDouble(), anyString());

        // when & then
        performPut("/api/budgets/1/adjust", adjustment)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算调整成功"));
    }

    @Test
    void shouldReturnBudgetExecution() throws Exception {
        // given
        BudgetService.BudgetExecution execution = new BudgetService.BudgetExecution(
                new BigDecimal("100000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("30.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:view"))).thenReturn(true);
        when(budgetService.getBudgetExecution(1L)).thenReturn(execution);

        // when & then
        performGet("/api/budgets/1/execution")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAnnualStatistics() throws Exception {
        // given
        BudgetService.BudgetStatistics statistics = new BudgetService.BudgetStatistics(
                new BigDecimal("500000.00"),
                new BigDecimal("150000.00"),
                new BigDecimal("350000.00"),
                new BigDecimal("30.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getAnnualBudgetStatistics(2025)).thenReturn(statistics);

        // when & then
        performGet("/api/budgets/statistics/annual?year=2025")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnDepartmentStatistics() throws Exception {
        // given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("deptId", 1L);
        statistics.put("year", 2025);
        statistics.put("totalBudget", 100000.00);

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getDepartmentBudgetStatistics(1L, 2025)).thenReturn(statistics);

        // when & then
        performGet("/api/budgets/statistics/department?deptId=1&year=2025")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deptId").value(1))
                .andExpect(jsonPath("$.year").value(2025));
    }

    @Test
    void shouldSetBudgetWarningSuccess() throws Exception {
        // given
        Map<String, Object> warning = new HashMap<>();
        warning.put("threshold", 80.0);
        warning.put("notifyType", "EMAIL");

        when(budgetService.checkPermission(anyString(), eq("budget:warning"))).thenReturn(true);
        doNothing().when(budgetService).setBudgetWarning(anyLong(), anyDouble(), anyString());

        // when & then
        performPut("/api/budgets/1/warning", warning)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算预警设置成功"));
    }

    /**
     * Task 13.1: 添加预算执行率计算测试
     */

    @Test
    void shouldCalculateBudgetExecutionRate() throws Exception {
        // given
        BudgetService.BudgetExecution execution = new BudgetService.BudgetExecution(
                new BigDecimal("100000.00"),
                new BigDecimal("30000.00"),
                new BigDecimal("30.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:view"))).thenReturn(true);
        when(budgetService.getBudgetExecution(1L)).thenReturn(execution);

        // when & then
        performGet("/api/budgets/1/execution")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetAmount").value(100000.00))
                .andExpect(jsonPath("$.usedAmount").value(30000.00))
                .andExpect(jsonPath("$.executionRate").value(30.00));
    }

    @Test
    void shouldHandleZeroBudgetInExecutionRate() throws Exception {
        // given
        BudgetService.BudgetExecution execution = new BudgetService.BudgetExecution(
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                new BigDecimal("0.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:view"))).thenReturn(true);
        when(budgetService.getBudgetExecution(1L)).thenReturn(execution);

        // when & then
        performGet("/api/budgets/1/execution")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionRate").value(0.00));
    }

    /**
     * Task 13.2: 添加预算偏差分析测试
     */

    @Test
    void shouldAnalyzeBudgetVariance() throws Exception {
        // given
        BudgetService.BudgetExecution execution = new BudgetService.BudgetExecution(
                new BigDecimal("100000.00"),
                new BigDecimal("120000.00"),
                new BigDecimal("120.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getBudgetExecution(1L)).thenReturn(execution);

        // when & then
        performGet("/api/budgets/1/execution")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetAmount").value(100000.00))
                .andExpect(jsonPath("$.usedAmount").value(120000.00))
                .andExpect(jsonPath("$.executionRate").value(120.00));
    }

    @Test
    void shouldAnalyzeFavorableBudgetVariance() throws Exception {
        // given
        BudgetService.BudgetExecution execution = new BudgetService.BudgetExecution(
                new BigDecimal("100000.00"),
                new BigDecimal("80000.00"),
                new BigDecimal("80.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getBudgetExecution(1L)).thenReturn(execution);

        // when & then
        performGet("/api/budgets/1/execution")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionRate").value(80.00));
    }

    /**
     * Task 13.3: 添加预算趋势分析测试
     */

    @Test
    void shouldAnalyzeBudgetTrendByYear() throws Exception {
        // given
        BudgetService.BudgetStatistics statistics = new BudgetService.BudgetStatistics(
                new BigDecimal("500000.00"),
                new BigDecimal("150000.00"),
                new BigDecimal("350000.00"),
                new BigDecimal("30.00")
        );

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getAnnualBudgetStatistics(2025)).thenReturn(statistics);

        // when & then
        performGet("/api/budgets/statistics/annual?year=2025")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudget").value(500000.00));
    }

    /**
     * Task 13.4: 添加预算对比报表数据准确性测试
     */

    @Test
    void shouldCompareBudgetVsActualByDepartment() throws Exception {
        // given
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("deptId", 1L);
        statistics.put("year", 2025);
        statistics.put("totalBudget", 100000.00);
        statistics.put("totalUsed", 95000.00);
        statistics.put("variance", -5000.00);

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getDepartmentBudgetStatistics(1L, 2025)).thenReturn(statistics);

        // when & then
        performGet("/api/budgets/statistics/department?deptId=1&year=2025")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudget").value(100000.00))
                .andExpect(jsonPath("$.totalUsed").value(95000.00));
    }
}

