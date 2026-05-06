package com.ccms.controller;

import com.ccms.entity.audit.AuditLog;
import com.ccms.service.audit.AuditLogService;
import com.ccms.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditLogController.class)
class AuditLogControllerTest extends BaseControllerTest {

    @MockBean
    private AuditLogService auditLogService;

    @Autowired
    private ObjectMapper objectMapper;

    private AuditLog testAuditLog;
    private Page<AuditLog> testAuditLogPage;
    private LocalDateTime testStartTime;
    private LocalDateTime testEndTime;

    @BeforeEach
    void setUp() {
        testStartTime = LocalDateTime.now().minusDays(1);
        testEndTime = LocalDateTime.now();
        
        testAuditLog = TestDataFactory.createAuditLog();
        List<AuditLog> auditLogs = List.of(
                TestDataFactory.createAuditLog(),
                TestDataFactory.createAuditLog(),
                TestDataFactory.createAuditLog()
        );
        testAuditLogPage = new PageImpl<>(auditLogs, PageRequest.of(0, 20), auditLogs.size());
        
        // 配置ObjectMapper支持LocalDateTime序列化
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldReturnAuditLogList_whenGetAuditLogsSuccess() throws Exception {
        // Given
        when(auditLogService.findAuditLogs(
                null, null, null, null, testStartTime, testEndTime, any()))
                .thenReturn(testAuditLogPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3));

        verify(auditLogService, times(1)).findAuditLogs(
                null, null, null, null, testStartTime, testEndTime, any());
    }

    @Test
    void shouldReturnAuditLogListWithFilters_whenGetAuditLogsWithParameters() throws Exception {
        // Given
        when(auditLogService.findAuditLogs(
                "user", "login", "admin", true, testStartTime, testEndTime, any()))
                .thenReturn(testAuditLogPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs")
                .param("module", "user")
                .param("operation", "login")
                .param("username", "admin")
                .param("success", "true")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(auditLogService, times(1)).findAuditLogs(
                "user", "login", "admin", true, testStartTime, testEndTime, any());
    }

    @Test
    void shouldReturnAuditLogDetail_whenGetAuditLogDetailSuccess() throws Exception {
        // Given
        Long logId = 1L;
        when(auditLogService.findById(logId)).thenReturn(testAuditLog);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/{id}", logId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testAuditLog.getId()))
                .andExpect(jsonPath("$.operation").value(testAuditLog.getOperation()));

        verify(auditLogService, times(1)).findById(logId);
    }

    @Test
    void shouldReturnNotFound_whenGetAuditLogDetailNotFound() throws Exception {
        // Given
        Long logId = 999L;
        when(auditLogService.findById(logId)).thenReturn(null);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/{id}", logId)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isNotFound());

        verify(auditLogService, times(1)).findById(logId);
    }

    @Test
    void shouldReturnEntityHistory_whenGetEntityAuditHistorySuccess() throws Exception {
        // Given
        when(auditLogService.getEntityAuditHistory("ExpenseApply", 1L, any()))
                .thenReturn(testAuditLogPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/entity-history")
                .param("entityType", "ExpenseApply")
                .param("entityId", "1")
                .param("page", "0")
                .param("size", "20")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(auditLogService, times(1)).getEntityAuditHistory("ExpenseApply", 1L, any());
    }

    @Test
    void shouldReturnUserStats_whenGetUserOperationStatsSuccess() throws Exception {
        // Given
        Map<String, Object> stats = Map.of(
                "totalOperations", 50,
                "successfulOperations", 45,
                "failedOperations", 5,
                "averageDuration", "150ms"
        );

        when(auditLogService.getUserOperationStats(123L, testStartTime, testEndTime))
                .thenReturn(stats);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/user-stats")
                .param("userId", "123")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalOperations").value(50))
                .andExpect(jsonPath("$.successfulOperations").value(45));

        verify(auditLogService, times(1)).getUserOperationStats(123L, testStartTime, testEndTime);
    }

    @Test
    void shouldReturnErrorAnalysis_whenAnalyzeErrorPatternsSuccess() throws Exception {
        // Given
        Map<String, Object> analysis = Map.of(
                "totalErrors", 15,
                "errorRate", "5.2%",
                "commonErrorTypes", List.of("PermissionDenied", "ValidationFailed"),
                "suggestions", List.of("加强权限验证", "优化输入校验")
        );

        when(auditLogService.analyzeErrorPatterns(testStartTime, testEndTime))
                .thenReturn(analysis);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/error-analysis")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.totalErrors").value(15))
                .andExpect(jsonPath("$.errorRate").value("5.2%"));

        verify(auditLogService, times(1)).analyzeErrorPatterns(testStartTime, testEndTime);
    }

    @Test
    void shouldReturnActiveUsersRanking_whenGetActiveUsersRankingSuccess() throws Exception {
        // Given
        Map<String, Long> ranking = Map.of(
                "admin", 150L,
                "manager", 85L,
                "user1", 42L
        );

        when(auditLogService.getActiveUsersRanking(testStartTime, testEndTime, 10))
                .thenReturn(ranking);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/active-users")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.admin").value(150L))
                .andExpect(jsonPath("$.manager").value(85L));

        verify(auditLogService, times(1)).getActiveUsersRanking(testStartTime, testEndTime, 10);
    }

    @Test
    void shouldReturnExportPath_whenExportAuditLogsSuccess() throws Exception {
        // Given
        String exportPath = "/exports/audit_logs_2024.csv";
        when(auditLogService.exportAuditLogs(
                "user", "login", "admin", true, testStartTime, testEndTime))
                .thenReturn(exportPath);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/export")
                .param("module", "user")
                .param("operation", "login")
                .param("username", "admin")
                .param("success", "true")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(content().string(exportPath));

        verify(auditLogService, times(1)).exportAuditLogs(
                "user", "login", "admin", true, testStartTime, testEndTime);
    }

    @Test
    void shouldReturnCleanupResult_whenCleanupExpiredLogsSuccess() throws Exception {
        // Given
        int retentionDays = 90;
        int deletedCount = 1500;

        when(auditLogService.cleanupExpiredLogs(retentionDays))
                .thenReturn(deletedCount);

        // When
        ResultActions result = mockMvc.perform(delete("/api/audit-logs/cleanup")
                .param("retentionDays", String.valueOf(retentionDays))
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审计日志清理完成"))
                .andExpect(jsonPath("$.deletedCount").value(1500))
                .andExpect(jsonPath("$.retentionDays").value(90));

        verify(auditLogService, times(1)).cleanupExpiredLogs(retentionDays);
    }

    @Test
    void shouldReturnPatternAnalysis_whenAnalyzeOperationPatternsSuccess() throws Exception {
        // Given
        Map<String, Object> patterns = Map.of(
                "operationFrequencies", Map.of("login", 150L, "query", 200L),
                "errorPatterns", Map.of("validation_errors", 15, "timeout_errors", 8)
        );

        when(auditLogService.analyzeErrorPatterns(testStartTime, testEndTime))
                .thenReturn(patterns);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/pattern-analysis")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.operationFrequencies.login").value(150L))
                .andExpect(jsonPath("$.peakAnalysis.morningPeak").value("09:00-11:00"));

        verify(auditLogService, times(1)).analyzeErrorPatterns(testStartTime, testEndTime);
    }

    @Test
    void shouldReturnHealthInfo_whenHealthCheckSuccess() throws Exception {
        // Given
        LocalDateTime recentTime = testEndTime.minusMinutes(30);
        AuditLog recentLog = TestDataFactory.createAuditLog();
        recentLog.setCreateTime(recentTime);
        
        List<AuditLog> recentLogs = List.of(recentLog);
        Page<AuditLog> recentPage = new PageImpl<>(recentLogs, PageRequest.of(0, 1), 1);

        when(auditLogService.findAuditLogs(
                null, null, null, null, any(), any(), any()))
                .thenReturn(recentPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/health")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.last24hRecordCount").value(1));

        verify(auditLogService, times(1)).findAuditLogs(
                null, null, null, null, any(), any(), any());
    }

    @Test
    void shouldReturnHealthInfo_whenNoRecentLogs() throws Exception {
        // Given
        Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 1), 0);

        when(auditLogService.findAuditLogs(
                null, null, null, null, any(), any(), any()))
                .thenReturn(emptyPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs/health")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("healthy"))
                .andExpect(jsonPath("$.last24hRecordCount").value(0))
                .andExpect(jsonPath("$.lastOperationTime").value("无记录"));

        verify(auditLogService, times(1)).findAuditLogs(
                null, null, null, null, any(), any(), any());
    }

    @Test
    void shouldHandleEmptyAuditLogList_whenNoMatchingLogs() throws Exception {
        // Given
        Page<AuditLog> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(auditLogService.findAuditLogs(
                "nonexistent", "invalid", "nonexistent", false, testStartTime, testEndTime, any()))
                .thenReturn(emptyPage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs")
                .param("module", "nonexistent")
                .param("operation", "invalid")
                .param("username", "nonexistent")
                .param("success", "false")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(auditLogService, times(1)).findAuditLogs(
                "nonexistent", "invalid", "nonexistent", false, testStartTime, testEndTime, any());
    }

    @Test
    void shouldHandleLargePageSize_whenGetAuditLogsWithLargeSize() throws Exception {
        // Given
        List<AuditLog> largeList = TestDataFactory.createAuditLogs(50);
        Page<AuditLog> largePage = new PageImpl<>(largeList, PageRequest.of(0, 100), 50);

        when(auditLogService.findAuditLogs(
                null, null, null, null, testStartTime, testEndTime, any()))
                .thenReturn(largePage);

        // When
        ResultActions result = mockMvc.perform(get("/api/audit-logs")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .param("page", "0")
                .param("size", "100")
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(50));

        verify(auditLogService, times(1)).findAuditLogs(
                null, null, null, null, testStartTime, testEndTime, any());
    }

    @Test
    void shouldHandleDateTimeFormatVariation_whenGetAuditLogs() throws Exception {
        // Given
        when(auditLogService.findAuditLogs(
                null, null, null, null, testStartTime, testEndTime, any()))
                .thenReturn(testAuditLogPage);

        // When - 使用不同的时间格式
        ResultActions result = mockMvc.perform(get("/api/audit-logs")
                .param("startTime", testStartTime.toString().replace("T", " "))
                .param("endTime", testEndTime.toString().replace("T", " "))
                .contentType(MediaType.APPLICATION_JSON));

        // Then - Spring应该能处理标准的时间格式转换
        result.andExpect(status().isOk());

        verify(auditLogService, times(1)).findAuditLogs(
                null, null, null, null, testStartTime, testEndTime, any());
    }

    @Test
    void shouldHandleNullSuccessParameter_whenGetAuditLogs() throws Exception {
        // Given
        when(auditLogService.findAuditLogs(
                "user", "login", "admin", null, testStartTime, testEndTime, any()))
                .thenReturn(testAuditLogPage);

        // When - 不传递success参数
        ResultActions result = mockMvc.perform(get("/api/audit-logs")
                .param("module", "user")
                .param("operation", "login")
                .param("username", "admin")
                .param("startTime", testStartTime.toString())
                .param("endTime", testEndTime.toString())
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        result.andExpect(status().isOk());

        verify(auditLogService, times(1)).findAuditLogs(
                "user", "login", "admin", null, testStartTime, testEndTime, any());
    }
}