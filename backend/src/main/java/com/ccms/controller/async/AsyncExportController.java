package com.ccms.controller.async;

import com.ccms.service.async.AsyncExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 异步导出任务控制器
 */
@RestController
@RequestMapping("/api/async/export")
public class AsyncExportController {

    @Autowired
    private AsyncExportService asyncExportService;

    /**
     * 创建异步导出任务
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createExportTask(
            @RequestParam String templateCode,
            @RequestParam String format,
            @RequestBody Map<String, Object> params,
            @RequestHeader("X-User-Id") String createBy) {
        
        try {
            String taskId = asyncExportService.createExportTask(templateCode, params, format, createBy);
            
            // 异步执行任务
            asyncExportService.executeExportTask(taskId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "导出任务创建成功");
            result.put("data", Map.of(
                "taskId", taskId,
                "status", "待处理"
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "创建导出任务失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取任务状态
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        try {
            Map<String, Object> status = asyncExportService.getTaskStatus(taskId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取任务状态成功");
            result.put("data", status);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取任务状态失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 取消导出任务
     */
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<Map<String, Object>> cancelTask(@PathVariable String taskId) {
        try {
            boolean success = asyncExportService.cancelTask(taskId);
            
            Map<String, Object> result = new HashMap<>();
            if (success) {
                result.put("code", 200);
                result.put("message", "任务取消成功");
            } else {
                result.put("code", 400);
                result.put("message", "任务无法取消，可能已完成或正在处理");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "取消任务失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取用户的任务列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getUserTasks(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            // 模拟任务列表数据
            Map<String, Object> taskList = new HashMap<>();
            taskList.put("total", 5);
            taskList.put("page", page);
            taskList.put("size", size);
            taskList.put("data", new Object[]{
                Map.of(
                    "taskId", "task-001",
                    "templateCode", "EXPENSE_REPORT",
                    "status", "已完成",
                    "createTime", "2025-03-15 10:00:00"
                ),
                Map.of(
                    "taskId", "task-002", 
                    "templateCode", "BUDGET_ANALYSIS",
                    "status", "处理中",
                    "createTime", "2025-03-15 09:30:00"
                )
            });
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取任务列表成功");
            result.put("data", taskList);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取任务列表失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTaskStats(@RequestHeader("X-User-Id") String userId) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalTasks", 15);
            stats.put("completedTasks", 10);
            stats.put("failedTasks", 2);
            stats.put("processingTasks", 1);
            stats.put("pendingTasks", 2);
            stats.put("avgProcessTime", "2分30秒");
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取统计信息成功");
            result.put("data", stats);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取统计信息失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 清理用户的任务记录
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupTasks(@RequestHeader("X-User-Id") String userId) {
        try {
            // 异步执行清理任务
            asyncExportService.cleanExpiredTasks();
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "清理任务已提交，将在后台执行");
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "清理任务失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 重试失败的任务
     */
    @PostMapping("/retry/{taskId}")
    public ResponseEntity<Map<String, Object>> retryTask(@PathVariable String taskId) {
        try {
            // 异步重试任务
            asyncExportService.executeExportTask(taskId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "任务重试已提交");
            result.put("data", Map.of("taskId", taskId));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "任务重试失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 批量提交导出任务
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> batchSubmitTasks(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") String userId) {
        
        try {
            // 解析批量任务请求
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, Object>> tasks = 
                (java.util.List<Map<String, Object>>) request.get("tasks");
            
            java.util.List<String> taskIds = new java.util.ArrayList<>();
            
            for (Map<String, Object> task : tasks) {
                String templateCode = (String) task.get("templateCode");
                String format = (String) task.get("format");
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) task.get("params");
                
                String taskId = asyncExportService.createExportTask(templateCode, params, format, userId);
                asyncExportService.executeExportTask(taskId);
                taskIds.add(taskId);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "批量任务提交成功");
            result.put("data", Map.of("taskIds", taskIds));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "批量任务提交失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}