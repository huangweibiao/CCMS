package com.ccms.controller.audit;

import com.ccms.dto.PageResponse;
import com.ccms.entity.approval.ApprovalAuditLog;
import com.ccms.service.ApprovalAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批审计控制器
 * 提供审计日志的查询、导出和统计分析接口
 */
@RestController
@RequestMapping("/api/approval/audit")
public class ApprovalAuditController {

    @Autowired
    private ApprovalAuditService approvalAuditService;

    /**
     * 查询审计日志
     */
    @GetMapping("/logs")
    public ResponseEntity<PageResponse<ApprovalAuditLog>> queryAuditLogs(
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String targetEntity,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String operationResult,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "operationTime") String sortField,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.Direction.fromString(sortDirection.toUpperCase()), sortField);
        
        Page<ApprovalAuditLog> auditLogs = approvalAuditService.queryAuditLogs(
                operationType, targetEntity, userId, businessType, operationResult,
                startTime, endTime, pageable);
        
        PageResponse<ApprovalAuditLog> response = PageResponse.fromPage(auditLogs);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取实体操作历史
     */
    @GetMapping("/entity-history/{entity}/{id}")
    public ResponseEntity<List<ApprovalAuditLog>> getEntityAuditHistory(
            @PathVariable String entity,
            @PathVariable Long id) {
        List<ApprovalAuditLog> history = approvalAuditService.getEntityAuditHistory(entity, id);
        return ResponseEntity.ok(history);
    }

    /**
     * 生成审计报告
     */
    @GetMapping("/report")
    public ResponseEntity<Map<String, Object>> generateAuditReport(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        // 设置默认时间范围
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7); // 默认最近7天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        Map<String, Object> report = approvalAuditService.generateAuditReport(startTime, endTime);
        return ResponseEntity.ok(report);
    }

    /**
     * 导出审计日志
     */
    @GetMapping("/export")
    public ResponseEntity<String> exportAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "csv") String format) {
        
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30); // 默认最近30天
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        String exportData = approvalAuditService.exportAuditLogs(startTime, endTime, format);
        
        // 设置响应头
        String filename = "audit_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String contentType;
        String fileExtension;
        
        switch (format.toLowerCase()) {
            case "csv":
                contentType = "text/csv";
                fileExtension = ".csv";
                break;
            case "json":
                contentType = "application/json";
                fileExtension = ".json";
                break;
            default:
                contentType = "text/plain";
                fileExtension = ".txt";
        }
        
        return ResponseEntity.ok()
                .header("Content-Type", contentType)
                .header("Content-Disposition", "attachment; filename=" + filename + fileExtension)
                .body(exportData);
    }

    /**
     * 分析性能问题
     */
    @GetMapping("/performance-analysis")
    public ResponseEntity<List<ApprovalAuditLog>> analyzePerformanceIssues(
            @RequestParam(defaultValue = "5000") Long threshold) {
        List<ApprovalAuditLog> slowOperations = approvalAuditService.analyzePerformanceIssues(threshold);
        return ResponseEntity.ok(slowOperations);
    }

    /**
     * 获取操作类型统计
     */
    @GetMapping("/operation-stats")
    public ResponseEntity<Map<String, Object>> getOperationStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        
        Map<String, Object> report = approvalAuditService.generateAuditReport(startTime, endTime);
        
        // 提取操作类型统计
        Map<String, Object> stats = new HashMap<>();
        stats.put("operationTypeDistribution", report.get("operationTypeDistribution"));
        stats.put("userOperationFrequency", report.get("userOperationFrequency"));
        stats.put("operationSuccessRates", report.get("operationSuccessRates"));
        stats.put("dailyOperationStats", report.get("dailyOperationStats"));
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 清理过期审计数据
     */
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldAuditData(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeTime) {
        
        if (beforeTime == null) {
            beforeTime = LocalDateTime.now().minusMonths(6); // 默认清理6个月前的数据
        }
        
        int deletedCount = approvalAuditService.cleanupOldAuditData(beforeTime);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "清理完成");
        response.put("deletedCount", deletedCount);
        response.put("cleanedBefore", beforeTime);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取系统审计统计概览
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getAuditOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 今日操作统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        
        // 获取今日数据（简化实现，实际应该调用统计方法）
        Map<String, Object> todayReport = approvalAuditService.generateAuditReport(todayStart, now);
        
        overview.put("todayStats", todayReport);
        overview.put("lastUpdated", LocalDateTime.now());
        
        return ResponseEntity.ok(overview);
    }
}