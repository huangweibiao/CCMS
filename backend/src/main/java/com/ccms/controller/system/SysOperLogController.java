package com.ccms.controller.system;

import com.ccms.entity.system.log.SysOperLog;
import com.ccms.repository.system.log.SysOperLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志控制器
 * 对应设计文档：4.9.1 操作日志表 (sys_oper_log)
 */
@RestController
@RequestMapping("/api/system/oper-logs")
public class SysOperLogController {

    private final SysOperLogRepository operLogRepository;

    @Autowired
    public SysOperLogController(SysOperLogRepository operLogRepository) {
        this.operLogRepository = operLogRepository;
    }

    /**
     * 获取操作日志列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<SysOperLog>> getOperLogList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String operModule,
            @RequestParam(required = false) String operType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Page<SysOperLog> logPage;
        if (startTime != null && endTime != null) {
            List<SysOperLog> logs = operLogRepository.findByOperTimeBetween(startTime, endTime);
            logPage = new org.springframework.data.domain.PageImpl<>(logs, PageRequest.of(page, size), logs.size());
        } else {
            logPage = operLogRepository.findAll(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(logPage);
    }

    /**
     * 根据ID获取操作日志
     */
    @GetMapping("/{logId}")
    public ResponseEntity<SysOperLog> getOperLogById(@PathVariable Long logId) {
        return operLogRepository.findById(logId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据操作人ID获取操作日志
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SysOperLog>> getOperLogsByUserId(@PathVariable String userId) {
        List<SysOperLog> logs = operLogRepository.findByOperUserId(userId);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据操作模块获取操作日志
     */
    @GetMapping("/module/{operModule}")
    public ResponseEntity<List<SysOperLog>> getOperLogsByModule(@PathVariable String operModule) {
        List<SysOperLog> logs = operLogRepository.findByOperModule(operModule);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据操作类型获取操作日志
     */
    @GetMapping("/type/{operType}")
    public ResponseEntity<List<SysOperLog>> getOperLogsByType(@PathVariable String operType) {
        List<SysOperLog> logs = operLogRepository.findByOperType(operType);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据业务ID获取操作日志
     */
    @GetMapping("/business/{businessId}")
    public ResponseEntity<List<SysOperLog>> getOperLogsByBusinessId(@PathVariable String businessId) {
        List<SysOperLog> logs = operLogRepository.findByBusinessId(businessId);
        return ResponseEntity.ok(logs);
    }

    /**
     * 根据时间范围获取操作日志
     */
    @GetMapping("/time-range")
    public ResponseEntity<List<SysOperLog>> getOperLogsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<SysOperLog> logs = operLogRepository.findByOperTimeBetween(startTime, endTime);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取最近的操作日志
     */
    @GetMapping("/recent")
    public ResponseEntity<List<SysOperLog>> getRecentOperLogs(
            @RequestParam(defaultValue = "7") Integer days) {
        java.time.LocalDateTime startTime = java.time.LocalDateTime.now().minusDays(days);
        List<SysOperLog> logs = operLogRepository.findRecentOperLogs(startTime);
        return ResponseEntity.ok(logs);
    }

    /**
     * 获取模块操作频率统计
     */
    @GetMapping("/module-frequency")
    public ResponseEntity<List<Object[]>> getModuleOperFrequency(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<Object[]> frequency = operLogRepository.findModuleOperFrequency(startTime, endTime);
        return ResponseEntity.ok(frequency);
    }

    /**
     * 统计用户的操作次数
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Object>> countUserOperations(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long count = operLogRepository.countByOperUserIdAndOperTimeBetween(userId, startTime, endTime);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("count", count);
        result.put("startTime", startTime);
        result.put("endTime", endTime);
        return ResponseEntity.ok(result);
    }

    /**
     * 根据操作IP获取操作日志
     */
    @GetMapping("/ip/{operIp}")
    public ResponseEntity<List<SysOperLog>> getOperLogsByIp(@PathVariable String operIp) {
        List<SysOperLog> logs = operLogRepository.findByOperIp(operIp);
        return ResponseEntity.ok(logs);
    }

    /**
     * 删除过期日志
     */
    @DeleteMapping("/expired")
    public ResponseEntity<Void> deleteExpiredLogs(@RequestParam(defaultValue = "90") Integer expireDays) {
        java.time.LocalDateTime expireTime = java.time.LocalDateTime.now().minusDays(expireDays);
        operLogRepository.deleteExpiredLogs(expireTime);
        return ResponseEntity.ok().build();
    }
}
