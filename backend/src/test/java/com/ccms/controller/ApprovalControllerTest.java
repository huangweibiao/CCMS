package com.ccms.controller;

import com.ccms.entity.approval.Approval;
import com.ccms.service.ApprovalService;
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

@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest extends BaseControllerTest {

    @MockBean
    private ApprovalService approvalService;

    @Autowired
    private ObjectMapper objectMapper;

    private Approval testApproval;
    private Page<Approval> testApprovalPage;

    @BeforeEach
    void setUp() {
        testApproval = TestDataFactory.createApproval();
        List<Approval> approvals = List.of(
                TestDataFactory.createApproval(),
                TestDataFactory.createApproval(),
                TestDataFactory.createApproval()
        );
        testApprovalPage = new PageImpl<>(approvals, PageRequest.of(0, 20), approvals.size());
    }

    @Test
    void shouldReturnApprovalList_whenGetApprovalListSuccess() throws Exception {
        // Given
        when(approvalService.checkPermission(anyString(), eq("approval:list"))).thenReturn(true);
        when(approvalService.getApprovalList(0, 20, null, null, null, null))
                .thenReturn(testApprovalPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.totalElements").value(3));

        verify(approvalService, times(1)).checkPermission(anyString(), eq("approval:list"));
        verify(approvalService, times(1)).getApprovalList(0, 20, null, null, null, null);
    }

    @Test
    void shouldReturnApprovalListWithFilters_whenGetApprovalListWithParameters() throws Exception {
        // Given
        when(approvalService.checkPermission(anyString(), eq("approval:list"))).thenReturn(true);
        when(approvalService.getApprovalList(0, 10, 123L, 456L, 1, "EXPENSE"))
                .thenReturn(testApprovalPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals")
                .param("page", "0")
                .param("size", "10")
                .param("approverId", "123")
                .param("applicantId", "456")
                .param("status", "1")
                .param("businessType", "EXPENSE")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(approvalService, times(1)).getApprovalList(0, 10, 123L, 456L, 1, "EXPENSE");
    }

    @Test
    void shouldReturnBadRequest_whenGetApprovalListAuthorizationFails() throws Exception {
        // Given
        when(approvalService.checkPermission(anyString(), eq("approval:list")))
                .thenThrow(new RuntimeException("权限验证失败"));

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnApprovalDetail_whenGetApprovalDetailSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        when(approvalService.checkPermission(anyString(), eq("approval:view"))).thenReturn(true);
        when(approvalService.getApprovalById(approvalId)).thenReturn(testApproval);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals/{approvalId}", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testApproval.getId()))
                .andExpect(jsonPath("$.businessType").value(testApproval.getBusinessType()));

        verify(approvalService, times(1)).getApprovalById(approvalId);
    }

    @Test
    void shouldReturnSuccess_whenCreateApprovalSuccess() throws Exception {
        // Given
        when(approvalService.checkPermission(anyString(), eq("approval:create"))).thenReturn(true);
        doNothing().when(approvalService).createApproval(any(Approval.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testApproval)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批流程创建成功"));

        verify(approvalService, times(1)).createApproval(any(Approval.class));
    }

    @Test
    void shouldReturnError_whenCreateApprovalFails() throws Exception {
        // Given
        when(approvalService.checkPermission(anyString(), eq("approval:create"))).thenReturn(true);
        doThrow(new RuntimeException("创建失败")).when(approvalService).createApproval(any(Approval.class));

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testApproval)));

        // Then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("审批流程创建失败: 创建失败"));
    }

    @Test
    void shouldReturnSuccess_whenUpdateApprovalSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        when(approvalService.checkPermission(anyString(), eq("approval:update"))).thenReturn(true);
        doNothing().when(approvalService).updateApproval(any(Approval.class));

        // When
        ResultActions result = mockMvc.perform(put("/api/approvals/{approvalId}", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testApproval)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批流程更新成功"));

        verify(approvalService, times(1)).updateApproval(any(Approval.class));
    }

    @Test
    void shouldReturnSuccess_whenDeleteApprovalSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        when(approvalService.checkPermission(anyString(), eq("approval:delete"))).thenReturn(true);
        doNothing().when(approvalService).deleteApproval(approvalId);

        // When
        ResultActions result = mockMvc.perform(delete("/api/approvals/{approvalId}", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批流程删除成功"));

        verify(approvalService, times(1)).deleteApproval(approvalId);
    }

    @Test
    void shouldReturnSuccess_whenApproveOperationSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        Map<String, Object> operation = Map.of(
                "result", 1,
                "comment", "同意审批",
                "approverId",- 123L
        );

        when(approvalService.checkPermission(anyString(), eq("approval:approve"))).thenReturn(true);
        doNothing().when(approvalService).approve(approvalId, 123L, 1, "同意审批");

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/{approvalId}/approve", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(operation)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批操作完成"));

        verify(approvalService, times(1)).approve(approvalId, 123L, 1, "同意审批");
    }

    @Test
    void shouldReturnPendingApprovals_whenGetPendingApprovalsSuccess() throws Exception {
        // Given
        when(approvalService.checkPermission(anyString(), eq("approval:pending"))).thenReturn(true);
        when(approvalService.getPendingApprovals(0, 20, 123L, "EXPENSE"))
                .thenReturn(testApprovalPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals/pending")
                .param("page", "0")
                .param("size", "20")
                .param("approverId", "123")
                .param("businessType", "EXPENSE")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(approvalService, times(1)).getPendingApprovals(0, 20, 123L, "EXPENSE");
    }

    @Test
    void shouldReturnSuccess_whenDelegateApprovalSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        Map<String, Object> delegation = Map.of(
                "delegateToId", 456L,
                "reason", "出差委托"
        );

        when(approvalService.checkPermission(anyString(), eq("approval:delegate"))).thenReturn(true);
        doNothing().when(approvalService).delegateApproval(approvalId, 456L, "出差委托");

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/{approvalId}/delegate", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delegation)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批委托成功"));

        verify(approvalService, times(1)).delegateApproval(approvalId, 456L, "出差委托");
    }

    @Test
    void shouldReturnSuccess_whenSetApprovalUrgentSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        Map<String, Object> urgentInfo = Map.of(
                "urgentReason", "紧急业务",
                "priority", 1
        );

        when(approvalService.checkPermission(anyString(), eq("approval:urgent"))).thenReturn(true);
        doNothing().when(approvalService).setApprovalUrgent(approvalId, "紧急业务", 1);

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/{approvalId}/urgent", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(urgentInfo)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批加急设置成功"));

        verify(approvalService, times(1)).setApprovalUrgent(approvalId, "紧急业务", 1);
    }

    @Test
    void shouldReturnApprovalHistory_whenGetApprovalHistorySuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        List<Object> history = List.of("history1", "history2");

        when(approvalService.checkPermission(anyString(), eq("approval:history"))).thenReturn(true);
        when(approvalService.getApprovalHistory(approvalId)).thenReturn(history);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals/{approvalId}/history", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(approvalService, times(1)).getApprovalHistory(approvalId);
    }

    @Test
    void shouldReturnStatistics_whenGetApprovalStatisticsSuccess() throws Exception {
        // Given
        Map<String, Object> statistics = Map.of(
                "totalCount", 100,
                "approvedCount", 80,
                "rejectedCount", 10,
                "pendingCount", 10
        );

        when(approvalService.checkPermission(anyString(), eq("approval:statistics"))).thenReturn(true);
        when(approvalService.getApprovalStatistics(123L, 456L, "2024-01-01", "2024-12-31"))
                .thenReturn(statistics);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals/statistics")
                .param("approverId", "123")
                .param("deptId", "456")
                .param("startDate", "2024-01-01")
                .param("endDate", "2024-12-31")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100));

        verify(approvalService, times(1)).getApprovalStatistics(123L, 456L, "2024-01-01", "2024-12-31");
    }

    @Test
    void shouldReturnSuccess_whenConfigureApprovalSuccess() throws Exception {
        // Given
        Map<String, Object> config = Map.of(
                "businessType", "EXPENSE",
                "approvalFlow", "一级审批->二级审批->三级审批"
        );

        when(approvalService.checkPermission(anyString(), eq("approval:configure"))).thenReturn(true);
        doNothing().when(approvalService).configureApproval("EXPENSE", "一级审批->二级审批->三级审批");

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/configuration")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(config)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批流程配置成功"));

        verify(approvalService, times(1)).configureApproval("EXPENSE", "一级审批->二级审批->三级审批");
    }

    @Test
    void shouldReturnSuccess_whenBatchOperationSuccess() throws Exception {
        // Given
        Map<String, Object> batchOperation = Map.of(
                "operation", "approve",
                "approvalIds", new Long[]{1L, 2L, 3L}
        );

        when(approvalService.checkPermission(anyString(), eq("approval:batch"))).thenReturn(true);
        doNothing().when(approvalService).batchOperation(any(Long[].class), eq("approve"));

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/batch")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(batchOperation)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("批量审批操作完成"));

        verify(approvalService, times(1)).batchOperation(any(Long[].class), eq("approve"));
    }

    @Test
    void shouldReturnSuccess_whenSetNotificationSuccess() throws Exception {
        // Given
        Map<String, Object> notification = Map.of(
                "userId", 123L,
                "notifyType", "EMAIL",
                "enabled", true
        );

        when(approvalService.checkPermission(anyString(), eq("approval:notification"))).thenReturn(true);
        doNothing().when(approvalService).setApprovalNotification(123L, "EMAIL", true);

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/notifications")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notification)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批通知设置成功"));

        verify(approvalService, times(1)).setApprovalNotification(123L, "EMAIL", true);
    }

    @Test
    void shouldReturnSuccess_whenMonitorApprovalProcessSuccess() throws Exception {
        // Given
        Map<String, Object> monitoringInfo = Map.of(
                "activeProcesses", 5,
                "avgProcessingTime", 2.5,
                "bottlenecks", List.of("部门审批")
        );

        when(approvalService.checkPermission(anyString(), eq("approval:monitor"))).thenReturn(true);
        when(approvalService.monitorApprovalProcess(anyMap())).thenReturn(monitoringInfo);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals/monitoring")
                .param("businessType", "EXPENSE")
                .param("timeRange", "7days")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.activeProcesses").value(5));

        verify(approvalService, times(1)).monitorApprovalProcess(anyMap());
    }

    @Test
    void shouldReturnSuccess_whenGetOptimizationSuggestionsSuccess() throws Exception {
        // Given
        List<String> suggestions = List.of("优化审批流程", "增加并行审批");

        when(approvalService.checkPermission(anyString(), eq("approval:suggestions"))).thenReturn(true);
        when(approvalService.getOptimizationSuggestions(anyMap())).thenReturn(suggestions);

        // When
        ResultActions result = mockMvc.perform(get("/api/approvals/optimization-suggestions")
                .param("businessType", "EXPENSE")
                .param("deptId", "456")
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("优化审批流程"));

        verify(approvalService, times(1)).getOptimizationSuggestions(anyMap());
    }

    @Test
    void shouldReturnSuccess_whenHandleExceptionSuccess() throws Exception {
        // Given
        Long approvalId = 1L;
        Map<String, Object> exception = Map.of(
                "exceptionType", "TIMEOUT",
                "action", "RESTART",
                "comment", "超时重启"
        );

        when(approvalService.checkPermission(anyString(), eq("approval:handle-exception"))).thenReturn(true);
        doNothing().when(approvalService).handleApprovalException(approvalId, "TIMEOUT", "RESTART", "超时重启");

        // When
        ResultActions result = mockMvc.perform(post("/api/approvals/{approvalId}/handle-exception", approvalId)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(exception)));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批异常处理完成"));

        verify(approvalService, times(1)).handleApprovalException(approvalId, "TIMEOUT", "RESTART", "超时重启");
    }
}