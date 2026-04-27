package com.ccms.controller;

import com.ccms.entity.approval.Approval;
import com.ccms.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 审批流程控制器
 * 处理审批流程的统一管理和监控
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalService approvalService;

    @Autowired
    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * 获取审批列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param approverId 审批人ID（可选）
     * @param applicantId 申请人ID（可选）
     * @param status 状态（可选）
     * @param businessType 业务类型（可选）
     * @param token 认证token
     * @return 审批分页列表
     */
    @GetMapping
    public ResponseEntity<Page<Approval>> getApprovalList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long approverId,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String businessType,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:list");
            
            Page<Approval> approvals = approvalService.getApprovalList(
                    page, size, approverId, applicantId, status, businessType);
            return ResponseEntity.ok(approvals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取审批详情
     * 
     * @param approvalId 审批ID
     * @param token 认证token
     * @return 审批详情
     */
    @GetMapping("/{approvalId}")
    public ResponseEntity<Approval> getApprovalDetail(@PathVariable Long approvalId,
                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:view");
            
            Approval approval = approvalService.getApprovalById(approvalId);
            return ResponseEntity.ok(approval);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 创建审批流程
     * 
     * @param approval 审批信息
     * @param token 认证token
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createApproval(@RequestBody Approval approval,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:create");
            
            approvalService.createApproval(approval);
            return ResponseEntity.ok(Map.of("message", "审批流程创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批流程创建失败: " + e.getMessage()));
        }
    }

    /**
     * 更新审批流程
     * 
     * @param approvalId 审批ID
     * @param approval 更新后的审批信息
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping("/{approvalId}")
    public ResponseEntity<Map<String, String>> updateApproval(@PathVariable Long approvalId,
                                                             @RequestBody Approval approval,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:update");
            
            approval.setId(approvalId);
            approvalService.updateApproval(approval);
            return ResponseEntity.ok(Map.of("message", "审批流程更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批流程更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除审批流程
     * 
     * @param approvalId 审批ID
     * @param token 认证token
     * @return 删除结果
     */
    @DeleteMapping("/{approvalId}")
    public ResponseEntity<Map<String, String>> deleteApproval(@PathVariable Long approvalId,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:delete");
            
            approvalService.deleteApproval(approvalId);
            return ResponseEntity.ok(Map.of("message", "审批流程删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批流程删除失败: " + e.getMessage()));
        }
    }

    /**
     * 审批操作
     * 
     * @param approvalId 审批ID
     * @param operation 审批操作
     * @param token 认证token
     * @return 审批结果
     */
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<Map<String, String>> approve(@PathVariable Long approvalId,
                                                      @RequestBody Map<String, Object> operation,
                                                      @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:approve");
            
            Integer result = (Integer) operation.get("result");
            String comment = (String) operation.get("comment");
            Long approverId = (Long) operation.get("approverId");
            
            approvalService.approve(approvalId, approverId, result, comment);
            return ResponseEntity.ok(Map.of("message", "审批操作完成"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批操作失败: " + e.getMessage()));
        }
    }

    /**
     * 获取待审批列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @param approverId 审批人ID
     * @param businessType 业务类型（可选）
     * @param token 认证token
     * @return 待审批列表
     */
    @GetMapping("/pending")
    public ResponseEntity<Page<Approval>> getPendingApprovals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam Long approverId,
            @RequestParam(required = false) String businessType,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:pending");
            
            Page<Approval> pendingApprovals = approvalService.getPendingApprovals(
                    page, size, approverId, businessType);
            return ResponseEntity.ok(pendingApprovals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 审批流程委托
     * 
     * @param approvalId 审批ID
     * @param delegation 委托信息
     * @param token 认证token
     * @return 委托结果
     */
    @PostMapping("/{approvalId}/delegate")
    public ResponseEntity<Map<String, String>> delegateApproval(@PathVariable Long approvalId,
                                                               @RequestBody Map<String, Object> delegation,
                                                               @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:delegate");
            
            Long delegateToId = (Long) delegation.get("delegateToId");
            String reason = (String) delegation.get("reason");
            
            approvalService.delegateApproval(approvalId, delegateToId, reason);
            return ResponseEntity.ok(Map.of("message", "审批委托成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批委托失败: " + e.getMessage()));
        }
    }

    /**
     * 审批流程加急
     * 
     * @param approvalId 审批ID
     * @param urgentInfo 加急信息
     * @param token 认证token
     * @return 加急结果
     */
    @PostMapping("/{approvalId}/urgent")
    public ResponseEntity<Map<String, String>> setApprovalUrgent(@PathVariable Long approvalId,
                                                                @RequestBody Map<String, Object> urgentInfo,
                                                                @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:urgent");
            
            String urgentReason = (String) urgentInfo.get("urgentReason");
            Integer priority = (Integer) urgentInfo.get("priority");
            
            approvalService.setApprovalUrgent(approvalId, urgentReason, priority);
            return ResponseEntity.ok(Map.of("message", "审批加急设置成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批加急设置失败: " + e.getMessage()));
        }
    }

    /**
     * 获取审批历史
     * 
     * @param approvalId 审批ID
     * @param token 认证token
     * @return 审批历史记录
     */
    @GetMapping("/{approvalId}/history")
    public ResponseEntity<?> getApprovalHistory(@PathVariable Long approvalId,
                                               @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:history");
            
            Object history = approvalService.getApprovalHistory(approvalId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 审批统计
     * 
     * @param approverId 审批人ID（可选）
     * @param deptId 部门ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param token 认证token
     * @return 审批统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getApprovalStatistics(
            @RequestParam(required = false) Long approverId,
            @RequestParam(required = false) Long deptId,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:statistics");
            
            Map<String, Object> statistics = approvalService.getApprovalStatistics(
                    approverId, deptId, startDate, endDate);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 审批流程配置
     * 
     * @param config 审批配置信息
     * @param token 认证token
     * @return 配置结果
     */
    @PostMapping("/configuration")
    public ResponseEntity<Map<String, String>> configureApproval(@RequestBody Map<String, Object> config,
                                                                @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:configure");
            
            String businessType = (String) config.get("businessType");
            Object approvalFlow = config.get("approvalFlow");
            
            approvalService.configureApproval(businessType, approvalFlow);
            return ResponseEntity.ok(Map.of("message", "审批流程配置成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批流程配置失败: " + e.getMessage()));
        }
    }

    /**
     * 批量审批操作
     * 
     * @param batchOperation 批量操作信息
     * @param token 认证token
     * @return 批量操作结果
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, String>> batchOperation(
            @RequestBody Map<String, Object> batchOperation,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:batch");
            
            String operation = (String) batchOperation.get("operation");
            Long[] approvalIds = (Long[]) batchOperation.get("approvalIds");
            
            approvalService.batchOperation(approvalIds, operation);
            return ResponseEntity.ok(Map.of("message", "批量审批操作完成"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "批量审批操作失败: " + e.getMessage()));
        }
    }

    /**
     * 审批通知设置
     * 
     * @param notification 通知设置
     * @param token 认证token
     * @return 设置结果
     */
    @PostMapping("/notifications")
    public ResponseEntity<Map<String, String>> setNotification(@RequestBody Map<String, Object> notification,
                                                              @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:notification");
            
            Long userId = (Long) notification.get("userId");
            String notifyType = (String) notification.get("notifyType");
            Boolean enabled = (Boolean) notification.get("enabled");
            
            approvalService.setApprovalNotification(userId, notifyType, enabled);
            return ResponseEntity.ok(Map.of("message", "审批通知设置成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批通知设置失败: " + e.getMessage()));
        }
    }

    /**
     * 导出审批数据
     * 
     * @param exportParams 导出参数
     * @param token 认证token
     * @return 导出文件
     */
    @PostMapping("/export")
    public ResponseEntity<?> exportApprovals(@RequestBody Map<String, Object> exportParams,
                                            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:export");
            
            // 导出逻辑实现
            Object exportResult = approvalService.exportApprovals(exportParams);
            return ResponseEntity.ok(exportResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 审批效率分析
     * 
     * @param analysisParams 分析参数
     * @param token 认证token
     * @return 分析结果
     */
    @PostMapping("/efficiency-analysis")
    public ResponseEntity<?> analyzeApprovalEfficiency(@RequestBody Map<String, Object> analysisParams,
                                                      @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:analysis");
            
            // 效率分析逻辑
            Object analysisResult = approvalService.analyzeApprovalEfficiency(analysisParams);
            return ResponseEntity.ok(analysisResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 审批异常处理
     * 
     * @param approvalId 审批ID
     * @param exception 异常信息
     * @param token 认证token
     * @return 处理结果
     */
    @PostMapping("/{approvalId}/handle-exception")
    public ResponseEntity<Map<String, String>> handleException(@PathVariable Long approvalId,
                                                              @RequestBody Map<String, Object> exception,
                                                              @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:handle-exception");
            
            String exceptionType = (String) exception.get("exceptionType");
            String action = (String) exception.get("action");
            String comment = (String) exception.get("comment");
            
            approvalService.handleApprovalException(approvalId, exceptionType, action, comment);
            return ResponseEntity.ok(Map.of("message", "审批异常处理完成"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "审批异常处理失败: " + e.getMessage()));
        }
    }

    /**
     * 审批流程监控
     * 
     * @param monitorParams 监控参数
     * @param token 认证token
     * @return 监控信息
     */
    @GetMapping("/monitoring")
    public ResponseEntity<?> monitorApprovalProcess(@RequestParam(required = false) String businessType,
                                                   @RequestParam(required = false) String timeRange,
                                                   @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:monitor");
            
            Map<String, Object> monitorParams = Map.of(
                    "businessType", businessType,
                    "timeRange", timeRange
            );
            
            Object monitoringInfo = approvalService.monitorApprovalProcess(monitorParams);
            return ResponseEntity.ok(monitoringInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 审批流程优化建议
     * 
     * @param suggestionParams 建议参数
     * @param token 认证token
     * @return 优化建议
     */
    @GetMapping("/optimization-suggestions")
    public ResponseEntity<?> getOptimizationSuggestions(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Long deptId,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            approvalService.checkPermission(token, "approval:suggestions");
            
            Map<String, Object> suggestionParams = Map.of(
                    "businessType", businessType,
                    "deptId", deptId
            );
            
            Object suggestions = approvalService.getOptimizationSuggestions(suggestionParams);
            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}