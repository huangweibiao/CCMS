package com.ccms.controller.finance;

import com.ccms.common.response.ApiResponse;
import com.ccms.entity.finance.FinanceVoucher;
import com.ccms.service.finance.FinanceVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/voucher")
public class FinanceVoucherController {

    @Autowired
    private FinanceVoucherService voucherService;

    /**
     * 生成凭证
     */
    @PostMapping("/generate")
    public ApiResponse<Object> generateVoucher(
            @RequestParam String businessType,
            @RequestParam Long businessId,
            @RequestParam(required = false) String businessNo,
            @RequestParam BigDecimal businessAmount) {
        
        try {
            FinanceVoucherService.VoucherGenerationResult result = voucherService.generateVoucher(
                    businessType, businessId, businessNo, businessAmount);

            if (result.isSuccess()) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("凭证生成失败: " + e.getMessage());
        }
    }

    /**
     * 审核凭证
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<Object> approveVoucher(
            @PathVariable Long id,
            @RequestParam Integer approve,
            @RequestParam(required = false) String remark) {
        
        try {
            FinanceVoucherService.VoucherGenerationResult result = voucherService.approveVoucher(id, approve, remark);

            if (result.isSuccess()) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("凭证审核失败: " + e.getMessage());
        }
    }

    /**
     * 凭证记账
     */
    @PostMapping("/{id}/posting")
    public ApiResponse<Object> postingVoucher(@PathVariable Long id) {
        try {
            FinanceVoucherService.VoucherGenerationResult result = voucherService.postingVoucher(id);

            if (result.isSuccess()) {
                return ApiResponse.success();
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("凭证记账失败: " + e.getMessage());
        }
    }

    /**
     * 查询凭证详情
     */
    @GetMapping("/{id}/detail")
    public ApiResponse<Object> getVoucherDetail(@PathVariable Long id) {
        try {
            Map<String, Object> detail = voucherService.getVoucherDetail(id);
            return ApiResponse.success(detail);
        } catch (Exception e) {
            return ApiResponse.error("查询凭证详情失败: " + e.getMessage());
        }
    }

    /**
     * 查询凭证列表
     */
    @GetMapping("/list")
    public ApiResponse<Object> getVoucherList(
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) String businessNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            LocalDate start = startDate != null ? LocalDate.parse(startDate) : null;
            LocalDate end = endDate != null ? LocalDate.parse(endDate) : null;

            Map<String, Object> result = voucherService.getVoucherList(
                    businessType, businessNo, status, start, end, page, size);

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("查询凭证列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取凭证统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getVoucherStatistics(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            Map<String, Object> result = voucherService.getVoucherStatistics(start, end);

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取凭证统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取凭证状态选项
     */
    @GetMapping("/status-options")
    public ApiResponse<Object> getStatusOptions() {
        try {
            Map<String, Object> options = Map.of(
                "0", "草稿",
                "1", "已生成",
                "2", "已审核",
                "3", "已记账",
                "4", "已驳回"
            );

            return ApiResponse.success(options);
        } catch (Exception e) {
            return ApiResponse.error("获取凭证状态选项失败: " + e.getMessage());
        }
    }
}