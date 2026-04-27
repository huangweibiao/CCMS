package com.ccms.controller;

import com.ccms.entity.budget.Budget;
import com.ccms.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 预算管理控制器
 * 处理预算的增删改查、分配、监控等操作
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @Autowired
    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    /**
     * 获取预算列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param year 年度（可选）
     * @param deptId 部门ID（可选）
     * @param status 状态（可选）
     * @param token 认证token
     * @return 预算分页列表
     */
    @GetMapping
    public ResponseEntity<Page<Budget>> getBudgetList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:list");
            
            Page<Budget> budgets = budgetService.getBudgetList(page, size, year, deptId, status);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取预算详情
     * 
     * @param budgetId 预算ID
     * @param token 认证token
     * @return 预算详情
     */
    @GetMapping("/{budgetId}")
    public ResponseEntity<Budget> getBudgetDetail(@PathVariable Long budgetId,
                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:view");
            
            Budget budget = budgetService.getBudgetById(budgetId);
            return ResponseEntity.ok(budget);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 创建预算
     * 
     * @param budget 预算信息
     * @param token 认证token
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createBudget(@RequestBody Budget budget,
                                                           @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:create");
            
            budgetService.createBudget(budget);
            return ResponseEntity.ok(Map.of("message", "预算创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算创建失败: " + e.getMessage()));
        }
    }

    /**
     * 更新预算信息
     * 
     * @param budgetId 预算ID
     * @param budget 更新后的预算信息
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping("/{budgetId}")
    public ResponseEntity<Map<String, String>> updateBudget(@PathVariable Long budgetId,
                                                           @RequestBody Budget budget,
                                                           @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:update");
            
            budget.setId(budgetId);
            budgetService.updateBudget(budget);
            return ResponseEntity.ok(Map.of("message", "预算更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除预算
     * 
     * @param budgetId 预算ID
     * @param token 认证token
     * @return 删除结果
     */
    @DeleteMapping("/{budgetId}")
    public ResponseEntity<Map<String, String>> deleteBudget(@PathVariable Long budgetId,
                                                           @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:delete");
            
            budgetService.deleteBudget(budgetId);
            return ResponseEntity.ok(Map.of("message", "预算删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算删除失败: " + e.getMessage()));
        }
    }

    /**
     * 提交预算审批
     * 
     * @param budgetId 预算ID
     * @param token 认证token
     * @return 提交结果
     */
    @PostMapping("/{budgetId}/submit")
    public ResponseEntity<Map<String, String>> submitBudget(@PathVariable Long budgetId,
                                                           @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:submit");
            
            budgetService.submitBudgetForApproval(budgetId);
            return ResponseEntity.ok(Map.of("message", "预算提交审批成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算提交审批失败: " + e.getMessage()));
        }
    }

    /**
     * 预算审批
     * 
     * @param budgetId 预算ID
     * @param approval 审批信息
     * @param token 认证token
     * @return 审批结果
     */
    @PostMapping("/{budgetId}/approve")
    public ResponseEntity<Map<String, String>> approveBudget(@PathVariable Long budgetId,
                                                            @RequestBody Map<String, Object> approval,
                                                            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:approve");
            
            Integer result = (Integer) approval.get("result");
            String comment = (String) approval.get("comment");
            Long approverId = (Long) approval.get("approverId");
            
            budgetService.approveBudget(budgetId, approverId, result, comment);
            return ResponseEntity.ok(Map.of("message", "预算审批完成"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算审批失败: " + e.getMessage()));
        }
    }

    /**
     * 分配预算
     * 
     * @param budgetId 预算ID
     * @param allocation 分配信息
     * @param token 认证token
     * @return 分配结果
     */
    @PostMapping("/{budgetId}/allocate")
    public ResponseEntity<Map<String, String>> allocateBudget(@PathVariable Long budgetId,
                                                             @RequestBody Map<String, Object> allocation,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:allocate");
            
            Long deptId = (Long) allocation.get("deptId");
            Double amount = (Double) allocation.get("amount");
            
            budgetService.allocateBudget(budgetId, deptId, amount);
            return ResponseEntity.ok(Map.of("message", "预算分配成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算分配失败: " + e.getMessage()));
        }
    }

    /**
     * 调整预算金额
     * 
     * @param budgetId 预算ID
     * @param adjustment 调整信息
     * @param token 认证token
     * @return 调整结果
     */
    @PutMapping("/{budgetId}/adjust")
    public ResponseEntity<Map<String, String>> adjustBudget(@PathVariable Long budgetId,
                                                           @RequestBody Map<String, Object> adjustment,
                                                           @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:adjust");
            
            Double newAmount = (Double) adjustment.get("newAmount");
            String reason = (String) adjustment.get("reason");
            
            budgetService.adjustBudgetAmount(budgetId, newAmount, reason);
            return ResponseEntity.ok(Map.of("message", "预算调整成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算调整失败: " + e.getMessage()));
        }
    }

    /**
     * 获取预算执行情况
     * 
     * @param budgetId 预算ID
     * @param token 认证token
     * @return 执行情况统计
     */
    @GetMapping("/{budgetId}/execution")
    public ResponseEntity<Map<String, Object>> getBudgetExecution(@PathVariable Long budgetId,
                                                                 @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:view");
            
            Map<String, Object> execution = budgetService.getBudgetExecution(budgetId);
            return ResponseEntity.ok(execution);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取年度预算统计
     * 
     * @param year 年度
     * @param token 认证token
     * @return 年度统计信息
     */
    @GetMapping("/statistics/annual")
    public ResponseEntity<Map<String, Object>> getAnnualBudgetStatistics(
            @RequestParam Integer year,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:statistics");
            
            Map<String, Object> statistics = budgetService.getAnnualBudgetStatistics(year);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取部门预算统计
     * 
     * @param deptId 部门ID
     * @param year 年度
     * @param token 认证token
     * @return 部门统计信息
     */
    @GetMapping("/statistics/department")
    public ResponseEntity<Map<String, Object>> getDepartmentBudgetStatistics(
            @RequestParam Long deptId,
            @RequestParam Integer year,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:statistics");
            
            Map<String, Object> statistics = budgetService.getDepartmentBudgetStatistics(deptId, year);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 预算预警设置
     * 
     * @param budgetId 预算ID
     * @param warning 预警设置
     * @param token 认证token
     * @return 预警设置结果
     */
    @PutMapping("/{budgetId}/warning")
    public ResponseEntity<Map<String, String>> setBudgetWarning(@PathVariable Long budgetId,
                                                               @RequestBody Map<String, Object> warning,
                                                               @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            budgetService.checkPermission(token, "budget:warning");
            
            Double threshold = (Double) warning.get("threshold");
            String notifyType = (String) warning.get("notifyType");
            
            budgetService.setBudgetWarning(budgetId, threshold, notifyType);
            return ResponseEntity.ok(Map.of("message", "预算预警设置成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "预算预警设置失败: " + e.getMessage()));
        }
    }
}