package com.ccms.controller;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.service.ExpenseReimburseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 费用报销控制器
 * 处理费用报销的增删改查、提交审批、支付关联等操作
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/expense-reimburses")
public class ExpenseReimburseController {

    private final ExpenseReimburseService expenseReimburseService;

    @Autowired
    public ExpenseReimburseController(ExpenseReimburseService expenseReimburseService) {
        this.expenseReimburseService = expenseReimburseService;
    }

    /**
     * 获取费用报销列表（分页）
     * 
     * @param page 页码
     * @param size 每页大小
     * @param applicantId 申请人ID（可选）
     * @param deptId 部门ID（可选）
     * @param status 状态（可选）
     * @param year 年度（可选）
     * @param token 认证token
     * @return 费用报销分页列表
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseReimburse>> getExpenseReimburseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer year,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:list");
            
            Page<ExpenseReimburse> expenseReimburses = expenseReimburseService.getExpenseReimburseList(
                    page, size, applicantId, deptId, status, year);
            return ResponseEntity.ok(expenseReimburses);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 获取费用报销详情
     * 
     * @param reimburseId 报销ID
     * @param token 认证token
     * @return 费用报销详情
     */
    @GetMapping("/{reimburseId}")
    public ResponseEntity<ExpenseReimburse> getExpenseReimburseDetail(@PathVariable Long reimburseId,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:view");
            
            ExpenseReimburse expenseReimburse = expenseReimburseService.getExpenseReimburseById(reimburseId);
            return ResponseEntity.ok(expenseReimburse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 创建费用报销
     * 
     * @param expenseReimburse 费用报销信息
     * @param token 认证token
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<Map<String, String>> createExpenseReimburse(@RequestBody ExpenseReimburse expenseReimburse,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:create");
            
            expenseReimburseService.createExpenseReimburse(expenseReimburse);
            return ResponseEntity.ok(Map.of("message", "费用报销创建成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用报销创建失败: " + e.getMessage()));
        }
    }

    /**
     * 更新费用报销
     * 
     * @param reimburseId 报销ID
     * @param expenseReimburse 更新后的费用报销信息
     * @param token 认证token
     * @return 更新结果
     */
    @PutMapping("/{reimburseId}")
    public ResponseEntity<Map<String, String>> updateExpenseReimburse(@PathVariable Long reimburseId,
                                                                     @RequestBody ExpenseReimburse expenseReimburse,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:update");
            
            expenseReimburse.setId(reimburseId);
            expenseReimburseService.updateExpenseReimburse(expenseReimburse);
            return ResponseEntity.ok(Map.of("message", "费用报销更新成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用报销更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除费用报销
     * 
     * @param reimburseId 报销ID
     * @param token 认证token
     * @return 删除结果
     */
    @DeleteMapping("/{reimburseId}")
    public ResponseEntity<Map<String, String>> deleteExpenseReimburse(@PathVariable Long reimburseId,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:delete");
            
            expenseReimburseService.deleteExpenseReimburse(reimburseId);
            return ResponseEntity.ok(Map.of("message", "费用报销删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用报销删除失败: " + e.getMessage()));
        }
    }

    /**
     * 提交费用报销审批
     * 
     * @param reimburseId 报销ID
     * @param token 认证token
     * @return 提交结果
     */
    @PostMapping("/{reimburseId}/submit")
    public ResponseEntity<Map<String, String>> submitExpenseReimburse(@PathVariable Long reimburseId,
                                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:submit");
            
            expenseReimburseService.submitExpenseReimburseForApproval(reimburseId);
            return ResponseEntity.ok(Map.of("message", "费用报销提交审批成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用报销提交审批失败: " + e.getMessage()));
        }
    }

    /**
     * 费用报销审批
     * 
     * @param reimburseId 报销ID
     * @param approval 审批信息
     * @param token 认证token
     * @return 审批结果
     */
    @PostMapping("/{reimburseId}/approve")
    public ResponseEntity<Map<String, String>> approveExpenseReimburse(@PathVariable Long reimburseId,
                                                                      @RequestBody Map<String, Object> approval,
                                                                      @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:approve");
            
            Integer result = (Integer) approval.get("result");
            String comment = (String) approval.get("comment");
            Long approverId = (Long) approval.get("approverId");
            
            expenseReimburseService.approveExpenseReimburse(reimburseId, approverId, result, comment);
            return ResponseEntity.ok(Map.of("message", "费用报销审批完成"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "费用报销审批失败: " + e.getMessage()));
        }
    }

    /**
     * 关联申请单
     * 
     * @param reimburseId 报销ID
     * @param apply 申请单关联信息
     * @param token 认证token
     * @return 关联结果
     */
    @PostMapping("/{reimburseId}/link-apply")
    public ResponseEntity<Map<String, String>> linkApply(@PathVariable Long reimburseId,
                                                        @RequestBody Map<String, Long> apply,
                                                        @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:link-apply");
            
            Long applyId = apply.get("applyId");
            expenseReimburseService.linkToExpenseApply(reimburseId, applyId);
            return ResponseEntity.ok(Map.of("message", "申请单关联成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "申请单关联失败: " + e.getMessage()));
        }
    }

    /**
     * 费用报销支付处理
     * 
     * @param reimburseId 报销ID
     * @param payment 支付信息
     * @param token 认证token
     * @return 支付处理结果
     */
    @PostMapping("/{reimburseId}/process-payment")
    public ResponseEntity<Map<String, String>> processPayment(@PathVariable Long reimburseId,
                                                             @RequestBody Map<String, Object> payment,
                                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:process-payment");
            
            String paymentMethod = (String) payment.get("paymentMethod");
            String paymentInfo = (String) payment.get("paymentInfo");
            Long processorId = (Long) payment.get("processorId");
            
            expenseReimburseService.processPayment(reimburseId, processorId, paymentMethod, paymentInfo);
            return ResponseEntity.ok(Map.of("message", "报销支付处理成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "报销支付处理失败: " + e.getMessage()));
        }
    }

    /**
     * 获取报销统计
     * 
     * @param applicantId 申请人ID（可选）
     * @param deptId 部门ID（可选）
     * @param year 年度
     * @param token 认证token
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getExpenseReimburseStatistics(
            @RequestParam(required = false) Long applicantId,
            @RequestParam(required = false) Long deptId,
            @RequestParam Integer year,
            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:statistics");
            
            Map<String, Object> statistics = expenseReimburseService.getExpenseReimburseStatistics(
                    applicantId, deptId, year);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 报销凭证上传
     * 
     * @param reimburseId 报销ID
     * @param voucher 凭证信息
     * @param token 认证token
     * @return 上传结果
     */
    @PostMapping("/{reimburseId}/upload-voucher")
    public ResponseEntity<Map<String, String>> uploadVoucher(@PathVariable Long reimburseId,
                                                            @RequestBody Map<String, Object> voucher,
                                                            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:upload-voucher");
            
            String voucherType = (String) voucher.get("voucherType");
            String voucherUrl = (String) voucher.get("voucherUrl");
            String fileName = (String) voucher.get("fileName");
            
            expenseReimburseService.uploadVoucher(reimburseId, voucherType, voucherUrl, fileName);
            return ResponseEntity.ok(Map.of("message", "报销凭证上传成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "报销凭证上传失败: " + e.getMessage()));
        }
    }

    /**
     * 报销凭证下载
     * 
     * @param reimburseId 报销ID
     * @param token 认证token
     * @return 凭证下载链接
     */
    @GetMapping("/{reimburseId}/download-voucher")
    public ResponseEntity<Map<String, String>> downloadVoucher(@PathVariable Long reimburseId,
                                                              @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:download-voucher");
            
            String downloadUrl = expenseReimburseService.getVoucherDownloadUrl(reimburseId);
            return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 报销退款处理
     * 
     * @param reimburseId 报销ID
     * @param refund 退款信息
     * @param token 认证token
     * @return 退款处理结果
     */
    @PostMapping("/{reimburseId}/process-refund")
    public ResponseEntity<Map<String, String>> processRefund(@PathVariable Long reimburseId,
                                                            @RequestBody Map<String, Object> refund,
                                                            @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:process-refund");
            
            Double refundAmount = (Double) refund.get("refundAmount");
            String refundReason = (String) refund.get("refundReason");
            Long processorId = (Long) refund.get("processorId");
            
            expenseReimburseService.processRefund(reimburseId, processorId, refundAmount, refundReason);
            return ResponseEntity.ok(Map.of("message", "报销退款处理成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "报销退款处理失败: " + e.getMessage()));
        }
    }

    /**
     * 报销状态跟踪
     * 
     * @param reimburseId 报销ID
     * @param token 认证token
     * @return 状态跟踪信息
     */
    @GetMapping("/{reimburseId}/status-tracking")
    public ResponseEntity<?> getStatusTracking(@PathVariable Long reimburseId,
                                             @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:view");
            
            Object statusTracking = expenseReimburseService.getStatusTracking(reimburseId);
            return ResponseEntity.ok(statusTracking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 报销批量导出
     * 
     * @param exportParams 导出参数
     * @param token 认证token
     * @return 导出文件
     */
    @PostMapping("/export")
    public ResponseEntity<?> exportExpenseReimburses(@RequestBody Map<String, Object> exportParams,
                                                    @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:export");
            
            // 导出逻辑实现
            Object exportResult = expenseReimburseService.exportExpenseReimburses(exportParams);
            return ResponseEntity.ok(exportResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 报销数据分析
     * 
     * @param analysisParams 分析参数
     * @param token 认证token
     * @return 分析结果
     */
    @PostMapping("/analysis")
    public ResponseEntity<?> analyzeExpenseReimburses(@RequestBody Map<String, Object> analysisParams,
                                                     @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:analysis");
            
            // 数据分析逻辑
            Object analysisResult = expenseReimburseService.analyzeExpenseReimburses(analysisParams);
            return ResponseEntity.ok(analysisResult);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 紧急报销处理
     * 
     * @param reimburseId 报销ID
     * @param urgentInfo 紧急处理信息
     * @param token 认证token
     * @return 紧急处理结果
     */
    @PostMapping("/{reimburseId}/urgent-processing")
    public ResponseEntity<Map<String, String>> urgentProcessing(@PathVariable Long reimburseId,
                                                               @RequestBody Map<String, Object> urgentInfo,
                                                               @RequestHeader("Authorization") String token) {
        try {
            // 验证权限
            expenseReimburseService.checkPermission(token, "expense-reimburse:urgent-processing");
            
            String urgentReason = (String) urgentInfo.get("urgentReason");
            Long processorId = (Long) urgentInfo.get("processorId");
            
            expenseReimburseService.urgentProcessing(reimburseId, processorId, urgentReason);
            return ResponseEntity.ok(Map.of("message", "紧急报销处理成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "紧急报销处理失败: " + e.getMessage()));
        }
    }
}