package com.ccms.controller;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.service.ExpenseApplyService;
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

@WebMvcTest(ExpenseApplyController.class)
class ExpenseApplyControllerTest extends BaseControllerTest {

    @MockBean
    private ExpenseApplyService expenseApplyService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseApply testExpenseApply;
    private Page<ExpenseApply> testExpenseApplyPage;

    @BeforeEach
    void setUp() {
        testExpenseApply = TestDataFactory.createExpenseApply();
        List<ExpenseApply> expenseApplies = List.of(
                TestDataFactory.createExpenseApply(),
                TestDataFactory.createExpenseApply(),
                TestDataFactory.createExpenseApply()
        );
        testExpenseApplyPage = new PageImpl<>(expenseApplies, PageRequest.of(0, 20), expenseApplies.size());
    }

    @Test
    void shouldReturnExpenseApplyList_whenGetExpenseApplyListSuccess() throws Exception {
        // Given
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:list"))).thenReturn(true);
        when(expenseApplyService.getExpenseApplyList(0, 20, null, null, null, null))
                .thenReturn(testExpenseApplyPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(expenseApplyService, times(1)).checkPermission(anyString(), eq("expense-apply:list"));
        verify(expenseApplyService, times(1)).getExpenseApplyList(0, 20, null, null, null, null);
    }

    @Test
    void shouldReturnExpenseApplyListWithFilters_whenGetExpenseApplyListWithParameters() throws Exception {
        // Given
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:list"))).thenReturn(true);
        when(expenseApplyService.getExpenseApplyList(0, 10, 123L, 456L, 1, 2024))
                .thenReturn(testExpenseApplyPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies")
                .param("page", "0")
                .param("size", "10")
                .param("applicantId", "123")
                .param("deptId", "456")
                .param("status", "1")
                .param("year", "2024")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(expenseApplyService, times(1)).getExpenseApplyList(0, 10, 123L, 456L, 1, 2024);
    }

    @Test
    void shouldReturnBadRequest_whenGetExpenseApplyListAuthorizationFails() throws Exception {
        // Given
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:list")))
                .thenThrow(new RuntimeException("权限验证失败"));

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnExpenseApplyDetail_whenGetExpenseApplyDetailSuccess() throws Exception {
        // Given
        Long applyId = 1L;
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:view"))).thenReturn(true);
        when(expenseApplyService.getExpenseApplyById(applyId)).thenReturn(testExpenseApply);

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies/{applyId}", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testExpenseApply.getId()))
                .andExpect(jsonPath("$.applyNumber").value(testExpenseApply.getApplyNumber()));

        verify(expenseApplyService, times(1)).getExpenseApplyById(applyId);
    }

    @Test
    void shouldReturnSuccess_whenCreateExpenseApplySuccess() throws Exception {
        // Given
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:create"))).thenReturn(true);
        doNothing().when(expenseApplyService).createExpenseApply(any(ExpenseApply.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testExpenseApply)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请创建成功"));

        verify(expenseApplyService, times(1)).createExpenseApply(any(ExpenseApply.class));
    }

    @Test
    void shouldReturnError_whenCreateExpenseApplyFails() throws Exception {
        // Given
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:create"))).thenReturn(true);
        doThrow(new RuntimeException("创建失败")).when(expenseApplyService).createExpenseApply(any(ExpenseApply.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testExpenseApply)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("费用申请创建失败: 创建失败"));
    }

    @Test
    void shouldReturnSuccess_whenUpdateExpenseApplySuccess() throws Exception {
        // Given
        Long applyId = 1L;
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:update"))).thenReturn(true);
        doNothing().when(expenseApplyService).updateExpenseApply(any(ExpenseApply.class));

        // When
        ResultActions result = mockMvc.perform(put("/api/expense-applies/{applyId}", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testExpenseApply)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请更新成功"));

        verify(expenseApplyService, times(1)).updateExpenseApply(any(ExpenseApply.class));
    }

    @Test
    void shouldReturnSuccess_whenDeleteExpenseApplySuccess() throws Exception {
        // Given
        Long applyId = 1L;
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:delete"))).thenReturn(true);
        doNothing().when(expenseApplyService).deleteExpenseApply(applyId);

        // When
        ResultActions result = mockMvc.perform(delete("/api/expense-applies/{applyId}", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请删除成功"));

        verify(expenseApplyService, times(1)).deleteExpenseApply(applyId);
    }

    @Test
    void shouldReturnSuccess_whenSubmitExpenseApplySuccess() throws Exception {
        // Given
        Long applyId = 1L;
        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:submit"))).thenReturn(true);
        doNothing().when(expenseApplyService).submitExpenseApplyForApproval(applyId);

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/submit", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请提交审批成功"));

        verify(expenseApplyService, times(1)).submitExpenseApplyForApproval(applyId);
    }

    @Test
    void shouldReturnSuccess_whenApproveExpenseApplySuccess() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, Object> approval = Map.of(
                "result", 1,
                "comment", "同意申请",
                "approverId", 123L
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:approve"))).thenReturn(true);
        doNothing().when(expenseApplyService).approveExpenseApply(applyId, 123L, 1, "同意申请");

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/approve", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approval)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请审批完成"));

        verify(expenseApplyService, times(1)).approveExpenseApply(applyId, 123L, 1, "同意申请");
    }

    @Test
    void shouldReturnSuccess_whenWithdrawExpenseApplySuccess() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, String> reason = Map.of(
                "reason", "申请信息有误"
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:withdraw"))).thenReturn(true);
        doNothing().when(expenseApplyService).withdrawExpenseApply(applyId, "申请信息有误");

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/withdraw", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reason)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用申请撤回成功"));

        verify(expenseApplyService, times(1)).withdrawExpenseApply(applyId, "申请信息有误");
    }

    @Test
    void shouldReturnSuccess_whenLinkReimbursementSuccess() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, Long> reimbursement = Map.of(
                "reimbursementId", 456L
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:link-reimbursement"))).thenReturn(true);
        doNothing().when(expenseApplyService).linkToReimbursement(applyId, 456L);

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/link-reimbursement", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reimbursement)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("报销关联成功"));

        verify(expenseApplyService, times(1)).linkToReimbursement(applyId, 456L);
    }

    @Test
    void shouldReturnStatistics_whenGetExpenseApplyStatisticsSuccess() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
                "totalCount", 50,
                "approvedCount", 40,
                "pendingCount", 5,
                "rejectedCount", 5,
                "totalAmount", 100000.0
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:statistics"))).thenReturn(true);
        when(expenseApplyService.getExpenseApplyStatistics(123L, 456L, 2024)).thenReturn(statistics);

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies/statistics")
                .param("applicantId", "123")
                .param("deptId", "456")
                .param("year", "2024")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(50))
                .andExpect(jsonPath("$.totalAmount").value(100000.0));

        verify(expenseApplyService, times(1)).getExpenseApplyStatistics(123L, 456L, 2024);
    }

    @Test
    void shouldReturnSuccess_whenBatchOperationSuccess() throws Exception {
        // Given
        Map<String, Object> batchOperation = Map.of(
                "operation", "approve",
                "applyIds", new Long[]{1L, 2L, 3L}
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:batch"))).thenReturn(true);
        doNothing().when(expenseApplyService).batchOperation(any(Long[].class), eq("approve"));

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/batch")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchOperation)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("批量操作执行成功"));

        verify(expenseApplyService, times(1)).batchOperation(any(Long[].class), eq("approve"));
    }

    @Test
    void shouldReturnApprovalHistory_whenGetApprovalHistorySuccess() throws Exception {
        // Given
        Long applyId = 1L;
        List<Object> approvalHistory = List.of("history1", "history2");

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:view"))).thenReturn(true);
        when(expenseApplyService.getApprovalHistory(applyId)).thenReturn(approvalHistory);

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies/{applyId}/approval-history", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(expenseApplyService, times(1)).getApprovalHistory(applyId);
    }

    @Test
    void shouldReturnBudgetCheck_whenCheckBudgetSuccess() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, Object> budgetCheck = Map.of(
                "isAvailable", true,
                "availableAmount", 10000.0,
                "requiredAmount", 5000.0,
                "remainingBudget", 5000.0
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:view"))).thenReturn(true);
        when(expenseApplyService.checkBudgetAvailability(applyId)).thenReturn(budgetCheck);

        // When
        ResultActions result = mockMvc.perform(get("/api/expense-applies/{applyId}/budget-check", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.isAvailable").value(true))
                .andExpect(jsonPath("$.availableAmount").value(10000.0));

        verify(expenseApplyService, times(1)).checkBudgetAvailability(applyId);
    }

    @Test
    void shouldReturnSuccess_whenAdjustAmountSuccess() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, Object> adjustment = Map.of(
                "newAmount", 8000.0,
                "reason", "项目需求变更"
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:adjust"))).thenReturn(true);
        doNothing().when(expenseApplyService).adjustExpenseAmount(applyId, 8000.0, "项目需求变更");

        // When
        ResultActions result = mockMvc.perform(put("/api/expense-applies/{applyId}/adjust-amount", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(adjustment)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用金额调整成功"));

        verify(expenseApplyService, times(1)).adjustExpenseAmount(applyId, 8000.0, "项目需求变更");
    }

    @Test
    void shouldReturnBadRequest_whenApproveExpenseApplyFails() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, Object> approval = Map.of(
                "result", 1,
                "comment", "同意申请",
                "approverId", 123L
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:approve"))).thenReturn(true);
        doThrow(new RuntimeException("审批失败")).when(expenseApplyService).approveExpenseApply(applyId, 123L, 1, "同意申请");

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/approve", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approval)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("费用申请审批失败: 审批失败"));
    }

    @Test
    void shouldReturnBadRequest_whenWithdrawExpenseApplyFails() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, String> reason = Map.of(
                "reason", "申请信息有误"
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:withdraw"))).thenReturn(true);
        doThrow(new RuntimeException("撤回失败")).when(expenseApplyService).withdrawExpenseApply(applyId, "申请信息有误");

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/withdraw", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reason)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("费用申请撤回失败: 撤回失败"));
    }

    @Test
    void shouldReturnBadRequest_whenLinkReimbursementFails() throws Exception {
        // Given
        Long applyId = 1L;
        Map<String, Long> reimbursement = Map.of(
                "reimbursementId", 456L
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:link-reimbursement"))).thenReturn(true);
        doThrow(new RuntimeException("关联失败")).when(expenseApplyService).linkToReimbursement(applyId, 456L);

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/{applyId}/link-reimbursement", applyId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reimbursement)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("报销关联失败: 关联失败"));
    }

    @Test
    void shouldReturnExportData_whenExportExpenseAppliesSuccess() throws Exception {
        // Given
        Map<String, Object> exportParams = Map.of(
                "year", 2024,
                "format", "excel"
        );

        Map<String, Object> exportData = Map.of(
                "fileName", "expense_applies_2024.xlsx",
                "dataSize", 50
        );

        when(expenseApplyService.checkPermission(anyString(), eq("expense-apply:export"))).thenReturn(true);
        when(expenseApplyService.exportExpenseApplies(exportParams)).thenReturn(exportData);

        // When
        ResultActions result = mockMvc.perform(post("/api/expense-applies/export")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exportParams)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("expense_applies_2024.xlsx"));

        verify(expenseApplyService, times(1)).exportExpenseApplies(exportParams);
    }
}