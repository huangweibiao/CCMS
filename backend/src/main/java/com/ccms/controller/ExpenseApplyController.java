package com.ccms.controller;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.service.ExpenseApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 费用申请控制器
 * 处理费用申请的增删改查、提交审批、报销关联等操作
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/expense-applies")
public class ExpenseApplyController {

    private final ExpenseApplyService expenseApplyService;

    @Autowired
    public ExpenseApplyController(ExpenseApplyService expenseApplyService) {
        this.expenseApplyService = expenseApplyService;
    }

    /**
     * 获取费用申请列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param applicantId 申请人ID（可选）
     * @param deptId 部门ID（可选）
     * @param status 状态（可选）
     * @param year 年度（可选）
     * @param token 认证token
     * @return 费用申请分页列表
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseApply>> getExpenseApplyList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer year,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:list");
            
            Page<ExpenseApply> expenseApplies = expenseApplyService.getExpenseApplyList(
                    page, size, applicantId, deptId, status, year);
            return ResponseEntity.ok(expenseApplies);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取费用申请详情
     * 
     * @param applyId 申请ID
     * @param token 认证token
     * @return 费用申请详情
     */
    @GetMapping("/{applyId}")
    public ResponseEntity<ExpenseApply> getExpenseApplyDetail(@PathVariable Long applyId,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:view");
            
            ExpenseApply expenseApply = expenseApplyService.getExpenseApplyById(applyId);
            return ResponseEntity.ok(expenseApply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 创建费用申请
     * 
     * @param expenseApply 费用申请信息
     * @param token 认证token
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createExpenseApply(@RequestBody ExpenseApply expenseApply,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:create");
            
            expenseApplyService.createExpenseApply(expenseApply);
            return ResponseEntity.ok(Map.of("message", "费用申请创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用申请创建失败: " + e.getMessage()));
        }
    }

    /**
     * 更新费用申请
     * 
     * @param applyId 申请ID
     * @param expenseApply 更新后的费用申请信息
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping("/{applyId}")
    public ResponseEntity<Map<String, String>> updateExpenseApply(@PathVariable Long applyId,
                                                                 @RequestBody ExpenseApply expenseApply,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:update");
            
            expenseApply.setId(applyId);
            expenseApplyService.updateExpenseApply(expenseApply);
            return ResponseEntity.ok(Map.of("message", "费用申请更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用申请更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除费用申请
     * 
     * @param applyId 申请ID
     * @param token 认证token
     * @return 删除结果
     */
    @DeleteMapping("/{applyId}")
    public ResponseEntity<Map<String, String>> deleteExpenseApply(@PathVariable Long applyId,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:delete");
            
            expenseApplyService.deleteExpenseApply(applyId);
            return ResponseEntity.ok(Map.of("message", "费用申请删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用申请删除失败: " + e.getMessage()));
        }
    }

    /**
     * 提交费用申请审批
     * 
     * @param applyId 申请ID
     * @param token 认证token
     * @return 提交结果
     */
    @PostMapping("/{applyId}/submit")
    public ResponseEntity<Map<String, String>> submitExpenseApply(@PathVariable Long applyId,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:submit");
            
            expenseApplyService.submitExpenseApplyForApproval(applyId);
            return ResponseEntity.ok(Map.of("message", "费用申请提交审批成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用申请提交审批失败: " + e.getMessage()));
        }
    }

    /**
     * 费用申请审批
     * 
     * @param applyId 申请ID
     * @param approval 审批信息
     * @param token 认证token
     * @return 审批结果
     */
    @PostMapping("/{applyId}/approve")
    public ResponseEntity<Map<String, String>> approveExpenseApply(@PathVariable Long applyId,
                                                                  @RequestBody Map<String, Object> approval,
                                                                  @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:approve");
            
            Integer result = (Integer) approval.get("result");
            String comment = (String) approval.get("comment");
            Long approverId = (Long) approval.get("approverId");
            
            expenseApplyService.approveExpenseApply(applyId, approverId, result, comment);
            return ResponseEntity.ok(Map.of("message", "费用申请审批完成"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用申请审批失败: " + e.getMessage()));
        }
    }

    /**
     * 撤回费用申请
     * 
     * @param applyId 申请ID
     * @param reason 撤回原因
     * @param token 认证token
     * @return 撤回结果
     */
    @PostMapping("/{applyId}/withdraw")
    public ResponseEntity<Map<String, String>> withdrawExpenseApply(@PathVariable Long applyId,
                                                                   @RequestBody Map<String, String> reason,
                                                                   @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:withdraw");
            
            expenseApplyService.withdrawExpenseApply(applyId, reason.get("reason"));
            return ResponseEntity.ok(Map.of("message", "费用申请撤回成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用申请撤回失败: " + e.getMessage()));
        }
    }

    /**
     * 关联报销
     * 
     * @param applyId 申请ID
     * @param reimbursement 报销关联信息
     * @param token 认证token
     * @return 关联结果
     */
    @PostMapping("/{applyId}/link-reimbursement")
    public ResponseEntity<Map<String, String>> linkReimbursement(@PathVariable Long applyId,
                                                                @RequestBody Map<String, Long> reimbursement,
                                                                @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:link-reimbursement");
            
            Long reimbursementId = reimbursement.get("reimbursementId");
            expenseApplyService.linkToReimbursement(applyId, reimbursementId);
            return ResponseEntity.ok(Map.of("message", "报销关联成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "报销关联失败: " + e.getMessage()));
        }
    }

    /**
     * 获取申请统计
     * 
     * @param applicantId 申请人ID（可选）
     * @param deptId 部门ID（可选）
     * @param year 年度
     * @param token 认证token
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getExpenseApplyStatistics(
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long deptId,
            @RequestParam Integer year,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:statistics");
            
            Map<String, Object> statistics = expenseApplyService.getExpenseApplyStatistics(
                    applicantId, deptId, year);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 费用申请批量操作
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
            expenseApplyService.checkPermission(token, "expense-apply:batch");
            
            String operation = (String) batchOperation.get("operation");
            Long[] applyIds = (Long[]) batchOperation.get("applyIds");
            
            expenseApplyService.batchOperation(applyIds, operation);
            return ResponseEntity.ok(Map.of("message", "批量操作执行成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "批量操作执行失败: " + e.getMessage()));
        }
    }

    /**
     * 获取审批记录
     * 
     * @param applyId 申请ID
     * @param token 认证token
     * @return 审批记录列表
     */
    @GetMapping("/{applyId}/approval-history")
    public ResponseEntity<?> getApprovalHistory(@PathVariable Long applyId,
                                              @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:view");
            
            Object approvalHistory = expenseApplyService.getApprovalHistory(applyId);
            return ResponseEntity.ok(approvalHistory);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 费用申请预算检查
     * 
     * @param applyId 申请ID
     * @param token 认证token
     * @return 预算检查结果
     */
    @GetMapping("/{applyId}/budget-check")
    public ResponseEntity<Map<String, Object>> checkBudget(@PathVariable Long applyId,
                                                          @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:view");
            
            Map<String, Object> budgetCheck = expenseApplyService.checkBudgetAvailability(applyId);
            return ResponseEntity.ok(budgetCheck);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 费用申请金额调整
     * 
     * @param applyId 申请ID
     * @param adjustment 调整信息
     * @param token 认证token
     * @return 调整结果
     */
    @PutMapping("/{applyId}/adjust-amount")
    public ResponseEntity<Map<String, String>> adjustAmount(@PathVariable Long applyId,
                                                           @RequestBody Map<String, Object> adjustment,
                                                           @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:adjust");
            
            Double newAmount = (Double) adjustment.get("newAmount");
            String reason = (String) adjustment.get("reason");
            
            expenseApplyService.adjustExpenseAmount(applyId, newAmount, reason);
            return ResponseEntity.ok(Map.of("message", "费用金额调整成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用金额调整失败: " + e.getMessage()));
        }
    }

    /**
     * 导出费用申请数据
     * 
     * @param exportParams 导出参数
     * @param token 认证token
     * @return 导出文件
     */
    @PostMapping("/export")
    public ResponseEntity<?> exportExpenseApplies(@RequestBody Map<String, Object> exportParams,
                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseApplyService.checkPermission(token, "expense-apply:export");
            
            // 导出逻辑实现
            Object exportResult = expenseApplyService.exportExpenseApplies(exportParams);
            return ResponseEntity.ok(exportResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}