package com.ccms.controller.workflow;

import com.ccms.entity.audit.AuditLog;
import com.ccms.service.audit.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 审计日志控制器
 * 对应设计文档：审计日志功能
 */
@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * 1. 获取审计日志列表（分页）
     * GET /api/audit/logs
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime actualStartTime = startTime != null ? startTime : LocalDateTime.now().minusMonths(1);
        LocalDateTime actualEndTime = endTime != null ? endTime : LocalDateTime.now();
        Page<AuditLog> auditLogPage = auditLogService.findAuditLogs(module, operation, username, success,
                actualStartTime, actualEndTime, pageable);
        return ResponseEntity.ok(auditLogPage);
    }

    /**
     * 2. 根据ID获取审计日志
     * GET /api/audit/logs/{logId}
     */
    @GetMapping("/logs/{logId}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long logId) {
        AuditLog auditLog = auditLogService.findById(logId);
        if (auditLog != null) {
            return ResponseEntity.ok(auditLog);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 3. 获取用户的审计日志
     * GET /api/audit/logs/user/{userId}
     */
    @GetMapping("/logs/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserAuditLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Pageable pageable = PageRequest.of(page, size);
        LocalDateTime actualStartTime = startTime != null ? startTime : LocalDateTime.now().minusMonths(1);
        LocalDateTime actualEndTime = endTime != null ? endTime : LocalDateTime.now();
        Map<String, Object> userStats = auditLogService.getUserOperationStats(userId, actualStartTime, actualEndTime);
        return ResponseEntity.ok(userStats);
    }

    /**
     * 4. 根据时间范围查询审计日志
     * GET /api/audit/logs/time-range
     */
    @GetMapping("/logs/time-range")
    public ResponseEntity<Page<AuditLog>> getAuditLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean success) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogPage = auditLogService.findAuditLogs(module, operation, username, success,
                startTime, endTime, pageable);
        return ResponseEntity.ok(auditLogPage);
    }

    /**
     * 5. 创建审计日志
     * POST /api/audit/logs
     */
    @PostMapping("/logs")
    public ResponseEntity<Map<String, Object>> createAuditLog(@RequestBody AuditLog auditLog) {
        auditLogService.logOperation(
                auditLog.getModule(),
                auditLog.getOperation(),
                auditLog.getDescription(),
                auditLog.getUserId(),
                auditLog.getUsername(),
                auditLog.getUserIp(),
                auditLog.getSuccess(),
                auditLog.getRequestMethod(),
                auditLog.getRequestUrl(),
                auditLog.getRequestParams()
        );
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审计日志创建成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 6. 删除过期日志
     * DELETE /api/audit/logs/expired
     */
    @DeleteMapping("/logs/expired")
    public ResponseEntity<Map<String, Object>> deleteExpiredLogs(
            @RequestParam(defaultValue = "90") Integer retentionDays) {
        int deletedCount = auditLogService.cleanupExpiredLogs(retentionDays);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "过期日志清理成功");
        result.put("deletedCount", deletedCount);
        result.put("retentionDays", retentionDays);
        return ResponseEntity.ok(result);
    }

    /**
     * 7. 审计统计
     * GET /api/audit/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getAuditStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Long userId) {
        LocalDateTime actualStartTime = startTime != null ? startTime : LocalDateTime.now().minusMonths(1);
        LocalDateTime actualEndTime = endTime != null ? endTime : LocalDateTime.now();

        Map<String, Object> statistics = new HashMap<>();

        // 错误模式分析
        Map<String, Object> errorAnalysis = auditLogService.analyzeErrorPatterns(actualStartTime, actualEndTime);
        statistics.put("errorAnalysis", errorAnalysis);

        // 活跃用户排行（前10名）
        Map<String, Long> activeUsers = auditLogService.getActiveUsersRanking(actualStartTime, actualEndTime, 10);
        statistics.put("activeUsers", activeUsers);

        // 如果指定了用户ID，添加用户操作统计
        if (userId != null) {
            Map<String, Object> userStats = auditLogService.getUserOperationStats(userId, actualStartTime, actualEndTime);
            statistics.put("userStats", userStats);
        }

        statistics.put("startTime", actualStartTime);
        statistics.put("endTime", actualEndTime);

        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取实体审计历史
     * GET /api/audit/logs/entity/{entityType}/{entityId}
     */
    @GetMapping("/logs/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLog>> getEntityAuditHistory(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditLog> auditLogPage = auditLogService.getEntityAuditHistory(entityType, entityId, pageable);
        return ResponseEntity.ok(auditLogPage);
    }

    /**
     * 导出审计日志
     * GET /api/audit/logs/export
     */
    @GetMapping("/logs/export")
    public ResponseEntity<Map<String, Object>> exportAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        LocalDateTime actualStartTime = startTime != null ? startTime : LocalDateTime.now().minusMonths(1);
        LocalDateTime actualEndTime = endTime != null ? endTime : LocalDateTime.now();
        String filePath = auditLogService.exportAuditLogs(module, operation, username, success,
                actualStartTime, actualEndTime);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("filePath", filePath);
        result.put("message", "审计日志导出成功");
        return ResponseEntity.ok(result);
    }
}
