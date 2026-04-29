package com.ccms.controller;

import com.ccms.entity.system.SysOperLog;
import com.ccms.service.SysOperLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统操作日志控制器
 * 提供操作日志查询、统计、导出等功能
 */
@RestController
@RequestMapping("/api/sys/operlog")
public class SysOperLogController {

    private final SysOperLogService operLogService;

    public SysOperLogController(SysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public ResponseEntity<Page<SysOperLog>> getOperLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) String operUserId,
            @RequestParam(required = false) String operUserName,
            @RequestParam(required = false) String operIp,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime operTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime operTimeEnd,
            @RequestParam(required = false) String businessModule) {
        
        SysOperLogService.OperLogQuery query = new SysOperLogService.OperLogQuery();
        query.setTitle(title);
        query.setBusinessType(businessType);
        query.setOperUserId(operUserId);
        query.setOperUserName(operUserName);
        query.setOperIp(operIp);
        query.setStatus(status);
        query.setOperTimeStart(operTimeStart);
        query.setOperTimeEnd(operTimeEnd);
        query.setBusinessModule(businessModule);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operTime"));
        Page<SysOperLog> operLogs = operLogService.getOperLogs(query, pageable);
        
        return ResponseEntity.ok(operLogs);
    }

    /**
     * 根据ID获取操作日志详情
     */
    @GetMapping("/{logId}")
    public ResponseEntity<SysOperLog> getOperLogById(@PathVariable Long logId) {
        SysOperLog operLog = operLogService.getOperLogById(logId);
        if (operLog == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(operLog);
    }

    /**
     * 根据业务信息查询操作日志
     */
    @GetMapping("/business/{businessModule}/{businessId}")
    public ResponseEntity<List<SysOperLog>> getOperLogsByBusiness(
            @PathVariable Long businessId,
            @PathVariable String businessModule) {
        List<SysOperLog> operLogs = operLogService.getOperLogsByBusiness(businessId, businessModule);
        return ResponseEntity.ok(operLogs);
    }

    /**
     * 根据用户ID查询操作日志
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<SysOperLog>> getOperLogsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "operTime"));
        Page<SysOperLog> operLogs = operLogService.getOperLogsByUser(userId, pageable);
        return ResponseEntity.ok(operLogs);
    }

    /**
     * 获取操作日志统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<SysOperLogService.OperLogStatistics> getOperLogStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String groupBy) {
        
        SysOperLogService.StatisticsQuery query = new SysOperLogService.StatisticsQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setGroupBy(groupBy);
        
        SysOperLogService.OperLogStatistics statistics = operLogService.getOperLogStatistics(query);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取操作趋势分析
     */
    @GetMapping("/trend")
    public ResponseEntity<List<SysOperLogService.OperTrend>> getOperTrendAnalysis(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "day") String period,
            @RequestParam(required = false) String businessType) {
        
        SysOperLogService.TrendQuery query = new SysOperLogService.TrendQuery();
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        query.setPeriod(period);
        query.setBusinessType(businessType);
        
        List<SysOperLogService.OperTrend> trends = operLogService.getOperTrendAnalysis(query);
        return ResponseEntity.ok(trends);
    }

    /**
     * 清理过期操作日志
     */
    @PostMapping("/cleanup")
    public ResponseEntity<SysOperLogService.CleanupResult> cleanupExpiredLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeDate) {
        
        SysOperLogService.CleanupResult result = operLogService.cleanupExpiredLogs(beforeDate);
        return ResponseEntity.ok(result);
    }

    /**
     * 导出操作日志
     */
    @PostMapping("/export")
    public ResponseEntity<Object> exportOperLogs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) String operUserId,
            @RequestParam(required = false) String operUserName,
            @RequestParam(required = false) String operIp,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime operTimeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime operTimeEnd,
            @RequestParam(required = false) String businessModule) {
        
        SysOperLogService.OperLogQuery query = new SysOperLogService.OperLogQuery();
        query.setTitle(title);
        query.setBusinessType(businessType);
        query.setOperUserId(operUserId);
        query.setOperUserName(operUserName);
        query.setOperIp(operIp);
        query.setStatus(status);
        query.setOperTimeStart(operTimeStart);
        query.setOperTimeEnd(operTimeEnd);
        query.setBusinessModule(businessModule);
        
        SysOperLogService.ExportResult result = operLogService.exportOperLogs(query);
        
        if (!result.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
        
        try {
            Path filePath = Paths.get(result.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", result.getFileName());
            headers.setContentLength(fileContent.length);
            
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
            
        } catch (IOException e) {
            SysOperLogService.ExportResult errorResult = new SysOperLogService.ExportResult();
            errorResult.setSuccess(false);
            errorResult.setMessage("导出文件读取失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResult);
        }
    }

    /**
     * 归档操作日志
     */
    @PostMapping("/archive")
    public ResponseEntity<SysOperLogService.ArchiveResult> archiveOperLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime beforeDate) {
        
        SysOperLogService.ArchiveResult result = operLogService.archiveOperLogs(beforeDate);
        return ResponseEntity.ok(result);
    }

    /**
     * 记录操作日志（供其他服务调用）
     */
    @PostMapping("/log")
    public ResponseEntity<Void> logOperation(@RequestBody SysOperLogService.OperLogInfo logInfo) {
        operLogService.logOperation(logInfo);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量记录操作日志
     */
    @PostMapping("/log/batch")
    public ResponseEntity<Void> batchLogOperations(@RequestBody List<SysOperLogService.OperLogInfo> logInfos) {
        operLogService.batchLogOperations(logInfos);
        return ResponseEntity.ok().build();
    }

    /**
     * 错误码状态映射
     */
    @GetMapping("/status/mapping")
    public ResponseEntity<String> getStatusMapping() {
        String mapping = "0: 成功, 1: 失败";
        return ResponseEntity.ok(mapping);
    }

    /**
     * 业务类型映射
     */
    @GetMapping("/business-type/mapping")
    public ResponseEntity<String> getBusinessTypeMapping() {
        String mapping = "0: 新增, 1: 修改, 2: 删除, 3: 查询, 4: 导入, 5: 导出, 9: 其他";
        return ResponseEntity.ok(mapping);
    }
}