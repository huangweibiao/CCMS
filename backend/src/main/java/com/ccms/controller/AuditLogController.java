package com.ccms.controller;

import com.ccms.entity.audit.AuditLog;
import com.ccms.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志管理控制器
 * 提供审计日志查询、统计和分析的RESTful接口
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * 查询审计日志列表
     */
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean success,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<AuditLog> auditLogs = auditLogService.findAuditLogs(
                module, operation, username, success, startTime, endTime, pageable);
        
        return ResponseEntity.ok(auditLogs);
    }

    /**
     * 根据ID查询审计日志详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogDetail(@PathVariable Long id) {
        AuditLog auditLog = auditLogService.findById(id);
        if (auditLog == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(auditLog);
    }

    /**
     * 查询实体操作历史
     */
    @GetMapping("/entity-history")
    public ResponseEntity<Page<AuditLog>> getEntityAuditHistory(
            @RequestParam String entityType,
            @RequestParam Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<AuditLog> auditHistory = auditLogService.getEntityAuditHistory(entityType, entityId, pageable);
        
        return ResponseEntity.ok(auditHistory);
    }

    /**
     * 获取用户操作统计
     */
    @GetMapping("/user-stats")
    public ResponseEntity<Map<String, Object>> getUserOperationStats(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> stats = auditLogService.getUserOperationStats(userId, startTime, endTime);
        return ResponseEntity.ok(stats);
    }

    /**
     * 分析错误操作模式
     */
    @GetMapping("/error-analysis")
    public ResponseEntity<Map<String, Object>> analyzeErrorPatterns(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        Map<String, Object> analysis = auditLogService.analyzeErrorPatterns(startTime, endTime);
        return ResponseEntity.ok(analysis);
    }

    /**
     * 获取活跃用户排名
     */
    @GetMapping("/active-users")
    public ResponseEntity<Map<String, Long>> getActiveUsersRanking(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Long> userRanking = auditLogService.getActiveUsersRanking(startTime, endTime, limit);
        return ResponseEntity.ok(userRanking);
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportAuditLogs(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Boolean success,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        String exportFilePath = auditLogService.exportAuditLogs(
                module, operation, username, success, startTime, endTime);
        
        return ResponseEntity.ok(exportFilePath);
    }

    /**
     * 清理过期审计日志
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupExpiredLogs(
            @RequestParam int retentionDays) {
        
        int deletedCount = auditLogService.cleanupExpiredLogs(retentionDays);
        
        Map<String, Object> result = Map.of(
                "message", "审计日志清理完成",
                "deletedCount", deletedCount,
                "retentionDays", retentionDays
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * 日志错误模式分析
     */
    @GetMapping("/pattern-analysis")
    public ResponseEntity<Map<String, Object>> analyzeOperationPatterns(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        // 获取操作频率分析
        Map<String, Object> patterns = auditLogService.analyzeErrorPatterns(startTime, endTime);
        
        // 添加高峰时段分析
        patterns.put("peakAnalysis", getPeakTimeAnalysis(startTime, endTime));
        
        return ResponseEntity.ok(patterns);
    }

    /**
     * 系统健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusHours(24);
        
        // 获取最近24小时的操作统计
        Pageable pageable = PageRequest.of(0, 1);
        Page<AuditLog> latestLogs = auditLogService.findAuditLogs(
                null, null, null, null, startTime, endTime, pageable);
        
        Map<String, Object> healthInfo = Map.of(
                "status", "healthy",
                "last24hRecordCount", latestLogs.getTotalElements(),
                "lastOperationTime", latestLogs.hasContent() ? 
                    latestLogs.getContent().get(0).getCreatedTime() : "无记录",
                "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(healthInfo);
    }

    /**
     * 高峰期分析（内部方法）
     */
    private Map<String, Object> getPeakTimeAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
        // 这里可以添加更复杂的高峰时段分析逻辑
        // 目前返回简单的时间段分布分析
        return Map.of(
                "morningPeak", "09:00-11:00",
                "afternoonPeak", "14:00-16:00",
                "eveningLow", "18:00-20:00"
        );
    }
}