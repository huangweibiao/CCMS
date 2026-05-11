package com.ccms.controller.workflow;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.audit.AuditLog;
import com.ccms.service.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审计日志控制器单元测试
 */
@WebMvcTest(AuditLogController.class)
class AuditLogControllerTest extends ControllerTestBase {

    private AuditLog createTestAuditLog(Long id, String module, String operation) {
        AuditLog log = new AuditLog();
        log.setId(id);
        log.setModule(module);
        log.setOperation(operation);
        log.setDescription("操作描述");
        log.setUserId(1L);
        log.setUsername("张三");
        log.setUserIp("192.168.1.1");
        log.setSuccess(true);
        log.setRequestMethod("POST");
        log.setRequestUrl("/api/test");

        return log;
    }

    @Test
    void shouldReturnAuditLogList() throws Exception {
        AuditLog log = createTestAuditLog(1L, "用户管理", "新增");
        Page<AuditLog> page = new PageImpl<>(
                Collections.singletonList(log),
                PageRequest.of(0, 10),
                1
        );
        when(auditLogService.findAuditLogs(any(), any(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        performGet("/api/audit/logs")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].module").value("用户管理"));
    }

    @Test
    void shouldReturnAuditLogByIdWhenExists() throws Exception {
        AuditLog log = createTestAuditLog(1L, "用户管理", "新增");
        when(auditLogService.findById(1L)).thenReturn(log);

        performGet("/api/audit/logs/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.module").value("用户管理"));
    }

    @Test
    void shouldReturnNotFoundWhenAuditLogNotExists() throws Exception {
        when(auditLogService.findById(999L)).thenReturn(null);

        performGet("/api/audit/logs/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUserAuditLogs() throws Exception {
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("userId", 1L);
        userStats.put("totalOperations", 100);
        when(auditLogService.getUserOperationStats(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(userStats);

        performGet("/api/audit/logs/user/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalOperations").value(100));
    }

    @Test
    void shouldReturnAuditLogsByTimeRange() throws Exception {
        AuditLog log = createTestAuditLog(1L, "用户管理", "新增");
        Page<AuditLog> page = new PageImpl<>(
                Collections.singletonList(log),
                PageRequest.of(0, 10),
                1
        );
        when(auditLogService.findAuditLogs(any(), any(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(page);

        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        performGet("/api/audit/logs/time-range?startTime=" + startTime + "&endTime=" + endTime)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldCreateAuditLogSuccess() throws Exception {
        AuditLog log = createTestAuditLog(1L, "用户管理", "新增");

        performPost("/api/audit/logs", log)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审计日志创建成功"));
    }

    @Test
    void shouldDeleteExpiredLogs() throws Exception {
        when(auditLogService.cleanupExpiredLogs(90)).thenReturn(100);

        performDelete("/api/audit/logs/expired?retentionDays=90")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.deletedCount").value(100))
                .andExpect(jsonPath("$.retentionDays").value(90));
    }

    @Test
    void shouldGetAuditStatistics() throws Exception {
        Map<String, Object> errorAnalysis = new HashMap<>();
        errorAnalysis.put("totalErrors", 10);

        Map<String, Long> activeUsers = new HashMap<>();
        activeUsers.put("张三", 50L);

        when(auditLogService.analyzeErrorPatterns(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(errorAnalysis);
        when(auditLogService.getActiveUsersRanking(any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(activeUsers);

        performGet("/api/audit/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errorAnalysis.totalErrors").value(10))
                .andExpect(jsonPath("$.activeUsers.张三").value(50));
    }

    @Test
    void shouldGetEntityAuditHistory() throws Exception {
        AuditLog log = createTestAuditLog(1L, "费用报销", "更新");
        Page<AuditLog> page = new PageImpl<>(
                Collections.singletonList(log),
                PageRequest.of(0, 10),
                1
        );
        when(auditLogService.getEntityAuditHistory("expense_reimburse", 1L, PageRequest.of(0, 10)))
                .thenReturn(page);

        performGet("/api/audit/logs/entity/expense_reimburse/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].module").value("费用报销"));
    }

    @Test
    void shouldExportAuditLogs() throws Exception {
        when(auditLogService.exportAuditLogs(any(), any(), any(), any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn("/exports/audit_logs.xlsx");

        performGet("/api/audit/logs/export")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.filePath").value("/exports/audit_logs.xlsx"));
    }
}
