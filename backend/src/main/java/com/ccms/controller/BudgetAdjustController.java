package com.ccms.controller;

import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.service.BudgetAdjustService;
import com.ccms.vo.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 预算调整控制器
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/budget/adjust")
@CrossOrigin(origins = "*")
public class BudgetAdjustController {
    
    @Autowired
    private BudgetAdjustService budgetAdjustService;
    
    /**
     * 创建预算调整申请
     */
    @PostMapping("/apply")
    public ResponseEntity<Map<String, Object>> createAdjustApply(
            @RequestBody Map<String, Object> applyRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long budgetId = Long.valueOf(applyRequest.get("budgetId").toString());
            Long budgetDetailId = Long.valueOf(applyRequest.get("budgetDetailId").toString());
            Integer adjustType = Integer.valueOf(applyRequest.get("adjustType").toString());
            BigDecimal adjustAmount = new BigDecimal(applyRequest.get("adjustAmount").toString());
            String reason = applyRequest.get("reason").toString();
            Long applyUserId = Long.valueOf(applyRequest.get("applyUserId").toString());
            String applyUserName = applyRequest.get("applyUserName").toString();
            
            ResultVO<BudgetAdjust> result = budgetAdjustService.createAdjustApply(
                budgetId, budgetDetailId, adjustType, adjustAmount, 
                reason, applyUserId, applyUserName);
            
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("message", result.getMsg());
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "创建预算调整申请失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 提交调整申请到审批流程
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitToApproval(
            @RequestBody Map<String, Object> submitRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long adjustId = Long.valueOf(submitRequest.get("adjustId").toString());
            Long userId = Long.valueOf(submitRequest.get("userId").toString());
            
            ResultVO<BudgetAdjust> result = budgetAdjustService.submitToApproval(adjustId, userId);
            
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("message", result.getMsg());
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "提交预算调整申请失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 审批预算调整申请
     */
    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approveAdjust(
            @RequestBody Map<String, Object> approveRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long adjustId = Long.valueOf(approveRequest.get("adjustId").toString());
            Boolean approveResult = Boolean.valueOf(approveRequest.get("approveResult").toString());
            String approvalComment = approveRequest.get("approvalComment").toString();
            Long approverId = Long.valueOf(approveRequest.get("approverId").toString());
            String approverName = approveRequest.get("approverName").toString();
            
            ResultVO<BudgetAdjust> result = budgetAdjustService.approveAdjust(
                adjustId, approveResult, approvalComment, approverId, approverName);
            
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("message", result.getMsg());
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "审批预算调整申请失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 执行预算调整
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeAdjust(
            @RequestBody Map<String, Object> executeRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long adjustId = Long.valueOf(executeRequest.get("adjustId").toString());
            
            ResultVO<BudgetAdjust> result = budgetAdjustService.executeAdjust(adjustId);
            
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("message", result.getMsg());
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "执行预算调整失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 撤销预算调整申请
     */
    @PostMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelAdjust(
            @RequestBody Map<String, Object> cancelRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long adjustId = Long.valueOf(cancelRequest.get("adjustId").toString());
            Long userId = Long.valueOf(cancelRequest.get("userId").toString());
            String reason = cancelRequest.get("reason").toString();
            
            ResultVO<BudgetAdjust> result = budgetAdjustService.cancelAdjust(adjustId, userId, reason);
            
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("message", result.getMsg());
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "撤销预算调整申请失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取调整记录详情
     */
    @GetMapping("/detail/{adjustId}")
    public ResponseEntity<Map<String, Object>> getAdjustDetail(@PathVariable Long adjustId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            BudgetAdjust adjust = budgetAdjustService.getAdjustDetail(adjustId);
            
            if (adjust != null) {
                response.put("success", true);
                response.put("message", "获取调整记录详情成功");
                response.put("data", adjust);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "调整记录不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取调整记录详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 验证预算调整可行性
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateAdjustment(
            @RequestBody Map<String, Object> validateRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long budgetId = Long.valueOf(validateRequest.get("budgetId").toString());
            Long budgetDetailId = Long.valueOf(validateRequest.get("budgetDetailId").toString());
            Integer adjustType = Integer.valueOf(validateRequest.get("adjustType").toString());
            BigDecimal adjustAmount = new BigDecimal(validateRequest.get("adjustAmount").toString());
            
            ResultVO<Boolean> result = budgetAdjustService.validateAdjustment(
                budgetId, budgetDetailId, adjustType, adjustAmount);
            
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("message", result.getMsg());
                response.put("data", result.getData());
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMsg());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "预算调整验证失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}