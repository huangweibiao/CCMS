package com.ccms.controller.finance;

import com.ccms.common.response.ApiResponse;
import com.ccms.entity.finance.FinancePayment;
import com.ccms.entity.finance.FinancePaymentMethod;
import com.ccms.service.finance.FinancePaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务支付管理Controller
 * 核心功能：支付单据管理、支付审批流程、支付执行
 */
@RestController
@RequestMapping("/api/finance/payment")
public class FinancePaymentController {

    @Autowired
    private FinancePaymentService paymentService;

    /**
     * 创建支付单据
     */
    @PostMapping("/create")
    public ApiResponse<Object> createPayment(
            @RequestParam String businessType,
            @RequestParam Long businessId,
            @RequestParam(required = false) String businessNo,
            @RequestParam BigDecimal amount,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String paymentReason,
            @RequestParam(required = false) String paymentReasonDetail) {
        
        try {
            FinancePaymentService.PaymentGenerationResult result = paymentService.createPayment(
                    businessType, businessId, businessNo, amount, paymentMethod, paymentReason, paymentReasonDetail);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "支付单创建成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("支付单创建失败: " + e.getMessage());
        }
    }

    /**
     * 审批支付
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<Object> approvePayment(
            @PathVariable Long id,
            @RequestParam Integer approve,
            @RequestParam(required = false) String comment) {
        
        try {
            FinancePaymentService.PaymentGenerationResult result = paymentService.approvePayment(id, approve, comment);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "支付审批成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("支付审批失败: " + e.getMessage());
        }
    }

    /**
     * 批量审批支付
     */
    @PostMapping("/batch-approve")
    public ApiResponse<Object> batchApprovePayments(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> paymentIds = (List<Long>) request.get("paymentIds");
            Integer approve = (Integer) request.getOrDefault("approve", 1);
            String comment = (String) request.getOrDefault("comment", "");

            Map<String, Object> result = paymentService.batchApprovePayments(paymentIds, approve, comment);

            return ApiResponse.success(result, "批量支付审批完成");
        } catch (Exception e) {
            return ApiResponse.error("批量支付审批失败: " + e.getMessage());
        }
    }

    /**
     * 执行支付
     */
    @PostMapping("/{id}/execute")
    public ApiResponse<Object> executePayment(@PathVariable Long id) {
        try {
            FinancePaymentService.PaymentGenerationResult result = paymentService.executePayment(id);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "支付执行成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("支付执行失败: " + e.getMessage());
        }
    }

    /**
     * 取消支付
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<Object> cancelPayment(
            @PathVariable Long id,
            @RequestParam String cancelReason) {
        
        try {
            FinancePaymentService.PaymentGenerationResult result = paymentService.cancelPayment(id, cancelReason);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "支付取消成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("支付取消失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询支付单据
     */
    @GetMapping("/{id}")
    public ApiResponse<Object> getPaymentById(@PathVariable Long id) {
        try {
            FinancePayment payment = paymentService.getPaymentById(id);
            return ApiResponse.success(payment, "查询支付单据成功");
        } catch (Exception e) {
            return ApiResponse.error("查询支付单据失败: " + e.getMessage());
        }
    }

    /**
     * 根据支付单据编号查询
     */
    @GetMapping("/no/{paymentNo}")
    public ApiResponse<Object> getPaymentByNo(@PathVariable String paymentNo) {
        try {
            FinancePayment payment = paymentService.getPaymentByNo(paymentNo);
            return ApiResponse.success(payment, "查询支付单据成功");
        } catch (Exception e) {
            return ApiResponse.error("查询支付单据失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付单据列表
     */
    @GetMapping("/list")
    public ApiResponse<Object> getPaymentList(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String businessNo,
            @RequestParam(required = false) Integer paymentStatus,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Map<String, Object> result = paymentService.getPaymentList(
                    businessType, businessNo, paymentStatus, startDate, endDate, page, size);

            return ApiResponse.success(result, "查询支付单据列表成功");
        } catch (Exception e) {
            return ApiResponse.error("查询支付单据列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询待审批的支付单据
     */
    @GetMapping("/pending")
    public ApiResponse<Object> getPendingPayments() {
        try {
            List<FinancePayment> payments = paymentService.findPendingApprovalPayments();
            return ApiResponse.success(payments);
        } catch (Exception e) {
            return ApiResponse.error("查询待审批支付单据失败: " + e.getMessage());
        }
    }

    /**
     * 查询支付统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getPaymentStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Integer paymentStatus) {
        
        try {
            Map<String, Object> result = paymentService.getPaymentStatistics(startDate, endDate, paymentStatus);

            return ApiResponse.success(result, "获取支付统计信息成功");
        } catch (Exception e) {
            return ApiResponse.error("获取支付统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付方式选项
     */
    @GetMapping("/payment-methods")
    public ApiResponse<Object> getPaymentMethods() {
        try {
            Map<String, String> methods = new HashMap<>();
            for (FinancePaymentMethod method : FinancePaymentMethod.values()) {
                methods.put(method.getCode().toString(), method.getName());
            }

            return ApiResponse.success(methods, "获取支付方式选项成功");
        } catch (Exception e) {
            return ApiResponse.error("获取支付方式选项失败: " + e.getMessage());
        }
    }

    /**
     * 获取支付状态选项
     */
    @GetMapping("/payment-status-options")
    public ApiResponse<Object> getPaymentStatusOptions() {
        try {
            Map<String, String> options = Map.of(
                    "0", "草稿",
                    "1", "待审批",
                    "2", "已审批",
                    "3", "已支付",
                    "4", "已取消",
                    "5", "已执行",
                    "6", "执行失败"
            );

            return ApiResponse.success(options, "获取支付状态选项成功");
        } catch (Exception e) {
            return ApiResponse.error("获取支付状态选项失败: " + e.getMessage());
        }
    }

    /**
     * 批量导出支付单据
     */
    @PostMapping("/export")
    public ApiResponse<Object> exportPayments(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> paymentIds = (List<Long>) request.get("paymentIds");
            String exportType = (String) request.getOrDefault("exportType", "EXCEL");

            // TODO: 实现支付单据导出功能
            Map<String, Object> result = new HashMap<>();
            result.put("exportType", exportType);
            result.put("totalCount", paymentIds.size());

            return ApiResponse.success(result, "支付单据导出成功");
        } catch (Exception e) {
            return ApiResponse.error("支付单据导出失败: " + e.getMessage());
        }
    }
}