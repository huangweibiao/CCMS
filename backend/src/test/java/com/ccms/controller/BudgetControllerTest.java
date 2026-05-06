package com.ccms.controller;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.service.BudgetService;
import com.ccms.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
class BudgetControllerTest extends BaseControllerTest {

    @MockBean
    private BudgetService budgetService;

    @Autowired
    private ObjectMapper objectMapper;

    private BudgetMain testBudget;
    private Page<BudgetMain> testBudgetPage;

    @BeforeEach
    void setUp() {
        testBudget = TestDataFactory.createBudget();
        List<BudgetMain> budgets = List.of(
                TestDataFactory.createBudget(),
                TestDataFactory.createBudget(),
                TestDataFactory.createBudget()
        );
        testBudgetPage = new PageImpl<>(budgets, PageRequest.of(0, 20), budgets.size());
    }

    @Test
    void shouldReturnBudgetList_whenGetBudgetListSuccess() throws Exception {
        // Given
        when(budgetService.checkPermission(anyString(), eq("budget:list"))).thenReturn(true);
        when(budgetService.getBudgetList(0, 20, null, null, null))
                .thenReturn(testBudgetPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(budgetService, times(1)).checkPermission(anyString(), eq("budget:list"));
        verify(budgetService, times(1)).getBudgetList(0, 20, null, null, null);
    }

    @Test
    void shouldReturnBudgetListWithFilters_whenGetBudgetListWithParameters() throws Exception {
        // Given
        when(budgetService.checkPermission(anyString(), eq("budget:list"))).thenReturn(true);
        when(budgetService.getBudgetList(0, 10, 2024, 123L, 1))
                .thenReturn(testBudgetPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets")
                .param("page", "0")
                .param("size", "10")
                .param("year", "2024")
                .param("deptId", "123")
                .param("status", "1")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(budgetService, times(1)).getBudgetList(0, 10, 2024, 123L, 1);
    }

    @Test
    void shouldReturnBadRequest_whenGetBudgetListAuthorizationFails() throws Exception {
        // Given
        when(budgetService.checkPermission(anyString(), eq("budget:list")))
                .thenThrow(new RuntimeException("权限验证失败"));

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBudgetDetail_whenGetBudgetDetailSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        when(budgetService.checkPermission(anyString(), eq("budget:view"))).thenReturn(true);
        when(budgetService.getBudgetById(budgetId)).thenReturn(testBudget);

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets/{budgetId}", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testBudget.getId()))
                .andExpect(jsonPath("$.budgetYear").value(testBudget.getBudgetYear()));

        verify(budgetService, times(1)).getBudgetById(budgetId);
    }

    @Test
    void shouldReturnSuccess_whenCreateBudgetSuccess() throws Exception {
        // Given
        when(budgetService.checkPermission(anyString(), eq("budget:create"))).thenReturn(true);
        doNothing().when(budgetService).createBudget(any(BudgetMain.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBudget)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算创建成功"));

        verify(budgetService, times(1)).createBudget(any(BudgetMain.class));
    }

    @Test
    void shouldReturnError_whenCreateBudgetFails() throws Exception {
        // Given
        when(budgetService.checkPermission(anyString(), eq("budget:create"))).thenReturn(true);
        doThrow(new RuntimeException("创建失败")).when(budgetService).createBudget(any(BudgetMain.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBudget)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("预算创建失败: 创建失败"));
    }

    @Test
    void shouldReturnSuccess_whenUpdateBudgetSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        when(budgetService.checkPermission(anyString(), eq("budget:update"))).thenReturn(true);
        doNothing().when(budgetService).updateBudget(any(BudgetMain.class));

        // When
        ResultActions result = mockMvc.perform(put("/api/budgets/{budgetId}", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBudget)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算更新成功"));

        verify(budgetService, times(1)).updateBudget(any(BudgetMain.class));
    }

    @Test
    void shouldReturnSuccess_whenDeleteBudgetSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        when(budgetService.checkPermission(anyString(), eq("budget:delete"))).thenReturn(true);
        doNothing().when(budgetService).deleteBudget(budgetId);

        // When
        ResultActions result = mockMvc.perform(delete("/api/budgets/{budgetId}", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算删除成功"));

        verify(budgetService, times(1)).deleteBudget(budgetId);
    }

    @Test
    void shouldReturnSuccess_whenSubmitBudgetSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        when(budgetService.checkPermission(anyString(), eq("budget:submit"))).thenReturn(true);
        doNothing().when(budgetService).submitBudgetForApproval(budgetId);

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets/{budgetId}/submit", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算提交审批成功"));

        verify(budgetService, times(1)).submitBudgetForApproval(budgetId);
    }

    @Test
    void shouldReturnSuccess_whenApproveBudgetSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> approval = Map.of(
                "result", 1,
                "comment", "同意预算",
                "approverId", 123L
        );

        when(budgetService.checkPermission(anyString(), eq("budget:approve"))).thenReturn(true);
        doNothing().when(budgetService).approveBudget(budgetId, 123L, 1, "同意预算");

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets/{budgetId}/approve", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approval)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算审批完成"));

        verify(budgetService, times(1)).approveBudget(budgetId, 123L, 1, "同意预算");
    }

    @Test
    void shouldReturnSuccess_whenAllocateBudgetSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> allocation = Map.of(
                "deptId", 456L,
                "amount", 50000.0
        );

        when(budgetService.checkPermission(anyString(), eq("budget:allocate"))).thenReturn(true);
        doNothing().when(budgetService).allocateBudget(budgetId, 456L, 50000.0);

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets/{budgetId}/allocate", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(allocation)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算分配成功"));

        verify(budgetService, times(1)).allocateBudget(budgetId, 456L, 50000.0);
    }

    @Test
    void shouldReturnSuccess_whenAdjustBudgetSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> adjustment = Map.of(
                "newAmount", 100000.0,
                "reason", "业务需求调整"
        );

        when(budgetService.checkPermission(anyString(), eq("budget:adjust"))).thenReturn(true);
        doNothing().when(budgetService).adjustBudgetAmount(budgetId, 100000.0, "业务需求调整");

        // When
        ResultActions result = mockMvc.perform(put("/api/budgets/{budgetId}/adjust", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustment)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算调整成功"));

        verify(budgetService, times(1)).adjustBudgetAmount(budgetId, 100000.0, "业务需求调整");
    }

    @Test
    void shouldReturnBudgetExecution_whenGetBudgetExecutionSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> execution = Map.of(
                "allocatedAmount", 50000.0,
                "usedAmount", 30000.0,
                "remainingAmount", 20000.0,
                "utilizationRate", 60.0
        );

        when(budgetService.checkPermission(anyString(), eq("budget:view"))).thenReturn(true);
        when(budgetService.getBudgetExecution(budgetId)).thenReturn(execution);

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets/{budgetId}/execution", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.allocatedAmount").value(50000.0))
                .andExpect(jsonPath("$.utilizationRate").value(60.0));

        verify(budgetService, times(1)).getBudgetExecution(budgetId);
    }

    @Test
    void shouldReturnAnnualStatistics_whenGetAnnualBudgetStatisticsSuccess() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
                "year", 2024,
                "totalBudget", 1000000.0,
                "totalUsed", 600000.0,
                "utilizationRate", 60.0,
                "deptCount", 10,
                "activeProjects", 25
        );

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getAnnualBudgetStatistics(2024)).thenReturn(statistics);

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets/statistics/annual")
                .param("year", "2024")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.totalBudget").value(1000000.0));

        verify(budgetService, times(1)).getAnnualBudgetStatistics(2024);
    }

    @Test
    void shouldReturnDepartmentStatistics_whenGetDepartmentBudgetStatisticsSuccess() throws Exception {
        // Given
        Long deptId = 123L;
        Map<String, Object> statistics = Map.of(
                "deptId", deptId,
                "deptName", "技术部",
                "annualBudget", 200000.0,
                "usedAmount", 120000.0,
                "remainingAmount", 80000.0,
                "projectsCount", 5
        );

        when(budgetService.checkPermission(anyString(), eq("budget:statistics"))).thenReturn(true);
        when(budgetService.getDepartmentBudgetStatistics(deptId, 2024)).thenReturn(statistics);

        // When
        ResultActions result = mockMvc.perform(get("/api/budgets/statistics/department")
                .param("deptId", deptId.toString())
                .param("year", "2024")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.deptId").value(deptId))
                .andExpect(jsonPath("$.annualBudget").value(200000.0));

        verify(budgetService, times(1)).getDepartmentBudgetStatistics(deptId, 2024);
    }

    @Test
    void shouldReturnSuccess_whenSetBudgetWarningSuccess() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> warning = Map.of(
                "threshold", 0.8,
                "notifyType", "EMAIL"
        );

        when(budgetService.checkPermission(anyString(), eq("budget:warning"))).thenReturn(true);
        doNothing().when(budgetService).setBudgetWarning(budgetId, 0.8, "EMAIL");

        // When
        ResultActions result = mockMvc.perform(put("/api/budgets/{budgetId}/warning", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warning)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("预算预警设置成功"));

        verify(budgetService, times(1)).setBudgetWarning(budgetId, 0.8, "EMAIL");
    }

    @Test
    void shouldReturnBadRequest_whenApproveBudgetFails() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> approval = Map.of(
                "result", 1,
                "comment", "同意预算",
                "approverId", 123L
        );

        when(budgetService.checkPermission(anyString(), eq("budget:approve"))).thenReturn(true);
        doThrow(new RuntimeException("审批失败")).when(budgetService).approveBudget(budgetId, 123L, 1, "同意预算");

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets/{budgetId}/approve", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approval)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("预算审批失败: 审批失败"));
    }

    @Test
    void shouldReturnBadRequest_whenAllocateBudgetFails() throws Exception {
        // Given
        Long budgetId = 1L;
        Map<String, Object> allocation = Map.of(
                "deptId", 456L,
                "amount", 50000.0
        );

        when(budgetService.checkPermission(anyString(), eq("budget:allocate"))).thenReturn(true);
        doThrow(new RuntimeException("分配失败")).when(budgetService).allocateBudget(budgetId, 456L, 50000.0);

        // When
        ResultActions result = mockMvc.perform(post("/api/budgets/{budgetId}/allocate", budgetId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(allocation)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("预算分配失败: 分配失败"));
    }
}