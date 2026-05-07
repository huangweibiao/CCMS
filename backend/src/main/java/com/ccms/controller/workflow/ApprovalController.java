package com.ccms.controller.workflow;

import com.ccms.entity.approval.Approval;
import com.ccms.entity.approval.ApprovalProcess;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批管理控制器
 * 对应设计文档4.7节审批流程相关表
 */
@RestController
@RequestMapping("/api/approval")
public class ApprovalController {

    private final ApprovalService approvalService;

    @Autowired
    public ApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    /**
     * 1. 获取审批列表（分页）
     * GET /api/approval
     */
    @GetMapping
    public ResponseEntity<Page<Approval>> getApprovalList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long approverId,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String businessType) {
        Page<Approval> approvalPage = approvalService.getApprovalList(page, size, approverId, applicantId, status, businessType);
        return ResponseEntity.ok(approvalPage);
    }

    /**
     * 2. 根据ID获取审批
     * GET /api/approval/{approvalId}
     */
    @GetMapping("/{approvalId}")
    public ResponseEntity<Approval> getApprovalById(@PathVariable Long approvalId) {
        Approval approval = approvalService.getApprovalById(approvalId);
        if (approval != null) {
            return ResponseEntity.ok(approval);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 3. 创建审批
     * POST /api/approval
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createApproval(@RequestBody Approval approval) {
        approvalService.createApproval(approval);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批创建成功");
        result.put("data", approval);
        return ResponseEntity.ok(result);
    }

    /**
     * 4. 更新审批
     * PUT /api/approval/{approvalId}
     */
    @PutMapping("/{approvalId}")
    public ResponseEntity<Map<String, Object>> updateApproval(
            @PathVariable Long approvalId,
            @RequestBody Approval approval) {
        approval.setId(approvalId);
        approvalService.updateApproval(approval);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批更新成功");
        result.put("data", approval);
        return ResponseEntity.ok(result);
    }

    /**
     * 5. 删除审批
     * DELETE /api/approval/{approvalId}
     */
    @DeleteMapping("/{approvalId}")
    public ResponseEntity<Map<String, Object>> deleteApproval(@PathVariable Long approvalId) {
        approvalService.deleteApproval(approvalId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批删除成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 6. 审批通过
     * POST /api/approval/{approvalId}/approve
     */
    @PostMapping("/{approvalId}/approve")
    public ResponseEntity<Map<String, Object>> approve(
            @PathVariable Long approvalId,
            @RequestParam Long approverId,
            @RequestParam(required = false) String comment) {
        approvalService.approve(approvalId, approverId, 1, comment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批已通过");
        return ResponseEntity.ok(result);
    }

    /**
     * 7. 审批驳回
     * POST /api/approval/{approvalId}/reject
     */
    @PostMapping("/{approvalId}/reject")
    public ResponseEntity<Map<String, Object>> reject(
            @PathVariable Long approvalId,
            @RequestParam Long approverId,
            @RequestParam(required = false) String comment) {
        approvalService.approve(approvalId, approverId, 2, comment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批已驳回");
        return ResponseEntity.ok(result);
    }

    /**
     * 8. 获取待我审批的列表
     * GET /api/approval/pending/{approverId}
     */
    @GetMapping("/pending/{approverId}")
    public ResponseEntity<List<ApprovalProcess>> getPendingApprovals(@PathVariable Long approverId) {
        List<ApprovalProcess> pendingList = approvalService.getPendingApprovals(approverId);
        return ResponseEntity.ok(pendingList);
    }

    /**
     * 9. 获取我已处理的列表
     * GET /api/approval/processed/{approverId}
     */
    @GetMapping("/processed/{approverId}")
    public ResponseEntity<List<ApprovalProcess>> getProcessedApprovals(@PathVariable Long approverId) {
        List<ApprovalProcess> processedList = approvalService.getProcessedApprovals(approverId);
        return ResponseEntity.ok(processedList);
    }

    /**
     * 10. 审批统计
     * GET /api/approval/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getApprovalStatistics(
            @RequestParam(required = false) Long approverId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        Map<String, Object> statistics = approvalService.getApprovalStatistics(approverId, deptId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    // ==================== 扩展接口 ====================

    /**
     * 启动审批流程
     * POST /api/approval/process/start
     */
    @PostMapping("/process/start")
    public ResponseEntity<Map<String, Object>> startApprovalProcess(
            @RequestParam String businessType,
            @RequestParam Long businessId,
            @RequestParam Long applicantId,
            @RequestParam List<Long> approvers) {
        ApprovalProcess process = approvalService.startApprovalProcess(businessType, businessId, applicantId, approvers);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批流程已启动");
        result.put("data", process);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批流程详情
     * GET /api/approval/process/{processId}
     */
    @GetMapping("/process/{processId}")
    public ResponseEntity<ApprovalProcess> getApprovalProcess(@PathVariable Long processId) {
        ApprovalProcess process = approvalService.getApprovalProcess(processId);
        if (process != null) {
            return ResponseEntity.ok(process);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 处理审批操作（通用审批接口）
     * POST /api/approval/process/{processId}/handle
     */
    @PostMapping("/process/{processId}/handle")
    public ResponseEntity<Map<String, Object>> processApproval(
            @PathVariable Long processId,
            @RequestParam Long approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment) {
        approvalService.processApproval(processId, approverId, approved, comment);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", approved ? "审批已通过" : "审批已驳回");
        return ResponseEntity.ok(result);
    }

    /**
     * 撤回审批流程
     * POST /api/approval/process/{processId}/withdraw
     */
    @PostMapping("/process/{processId}/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawApprovalProcess(
            @PathVariable Long processId,
            @RequestParam Long applicantId) {
        approvalService.withdrawApprovalProcess(processId, applicantId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批流程已撤回");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批记录
     * GET /api/approval/process/{processId}/records
     */
    @GetMapping("/process/{processId}/records")
    public ResponseEntity<List<ApprovalRecord>> getApprovalRecords(@PathVariable Long processId) {
        List<ApprovalRecord> records = approvalService.getApprovalRecords(processId);
        return ResponseEntity.ok(records);
    }

    /**
     * 获取当前审批节点
     * GET /api/approval/process/{processId}/current-node
     */
    @GetMapping("/process/{processId}/current-node")
    public ResponseEntity<ApprovalNode> getCurrentApprovalNode(@PathVariable Long processId) {
        ApprovalNode node = approvalService.getCurrentApprovalNode(processId);
        if (node != null) {
            return ResponseEntity.ok(node);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 跳过当前审批节点
     * POST /api/approval/process/{processId}/skip-node
     */
    @PostMapping("/process/{processId}/skip-node")
    public ResponseEntity<Map<String, Object>> skipCurrentNode(
            @PathVariable Long processId,
            @RequestParam Long skipperId,
            @RequestParam String reason) {
        approvalService.skipCurrentNode(processId, skipperId, reason);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "当前节点已跳过");
        return ResponseEntity.ok(result);
    }

    /**
     * 重新分配审批节点
     * POST /api/approval/process/{processId}/reassign
     */
    @PostMapping("/process/{processId}/reassign")
    public ResponseEntity<Map<String, Object>> reassignApprover(
            @PathVariable Long processId,
            @RequestParam Long nodeId,
            @RequestParam Long newApproverId) {
        approvalService.reassignApprover(processId, nodeId, newApproverId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批人已重新分配");
        return ResponseEntity.ok(result);
    }

    /**
     * 催办审批
     * POST /api/approval/process/{processId}/remind
     */
    @PostMapping("/process/{processId}/remind")
    public ResponseEntity<Map<String, Object>> remindApproval(@PathVariable Long processId) {
        approvalService.remindApproval(processId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "催办通知已发送");
        return ResponseEntity.ok(result);
    }

    /**
     * 委派审批任务
     * POST /api/approval/process/{processId}/delegate
     */
    @PostMapping("/process/{processId}/delegate")
    public ResponseEntity<Map<String, Object>> delegateApprovalTask(
            @PathVariable Long processId,
            @RequestParam Long approverId,
            @RequestParam Long delegateToId,
            @RequestParam String reason) {
        approvalService.delegateApprovalTask(processId, approverId, delegateToId, reason);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批任务已委派");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批历史
     * GET /api/approval/{approvalId}/history
     */
    @GetMapping("/{approvalId}/history")
    public ResponseEntity<Object> getApprovalHistory(@PathVariable Long approvalId) {
        Object history = approvalService.getApprovalHistory(approvalId);
        return ResponseEntity.ok(history);
    }

    /**
     * 设置审批加急
     * POST /api/approval/{approvalId}/urgent
     */
    @PostMapping("/{approvalId}/urgent")
    public ResponseEntity<Map<String, Object>> setApprovalUrgent(
            @PathVariable Long approvalId,
            @RequestParam String urgentReason,
            @RequestParam Integer priority) {
        approvalService.setApprovalUrgent(approvalId, urgentReason, priority);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批已设置为加急");
        return ResponseEntity.ok(result);
    }

    /**
     * 委托审批
     * POST /api/approval/{approvalId}/delegate
     */
    @PostMapping("/{approvalId}/delegate")
    public ResponseEntity<Map<String, Object>> delegateApproval(
            @PathVariable Long approvalId,
            @RequestParam Long delegateToId,
            @RequestParam String reason) {
        approvalService.delegateApproval(approvalId, delegateToId, reason);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "审批已委托");
        return ResponseEntity.ok(result);
    }
}
