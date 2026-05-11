package com.ccms.controller.finance;

import com.ccms.common.response.ApiResponse;
import com.ccms.entity.finance.FinanceReport;
import com.ccms.entity.finance.FinanceReportType;
import com.ccms.entity.finance.FinanceReportPeriod;
import com.ccms.service.finance.FinanceReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 财务报表管理Controller
 * 核心功能：费用统计、支付统计、部门分析、科目分析、报表导出
 */
@RestController
@RequestMapping("/api/finance/report")
public class FinanceReportController {

    @Autowired
    private FinanceReportService reportService;

    /**
     * 生成费用统计报表
     */
    @PostMapping("/expense")
    public ApiResponse<Object> generateExpenseReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.generateExpenseReport(
                    startDate, endDate, departmentId);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "费用统计报表生成成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("费用统计报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成支付统计报表
     */
    @PostMapping("/payment")
    public ApiResponse<Object> generatePaymentReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.generatePaymentReport(
                    startDate, endDate, departmentId);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "支付统计报表生成成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("支付统计报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成部门费用分析报表
     */
    @PostMapping("/department")
    public ApiResponse<Object> generateDepartmentReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam Long departmentId) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.generateDepartmentExpenseReport(
                    startDate, endDate, departmentId);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "部门费用分析报表生成成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("部门费用分析报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成科目余额分析报表
     */
    @PostMapping("/account")
    public ApiResponse<Object> generateAccountReport(
            @RequestParam LocalDate accountDate) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.generateAccountBalanceReport(accountDate);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "科目余额分析报表生成成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("科目余额分析报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 生成现金流报表
     */
    @PostMapping("/cashflow")
    public ApiResponse<Object> generateCashFlowReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long departmentId) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.generateCashFlowReport(
                    startDate, endDate, departmentId);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "现金流报表生成成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("现金流报表生成失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询报表
     */
    @GetMapping("/{id}")
    public ApiResponse<Object> getReportById(@PathVariable Long id) {
        try {
            FinanceReport report = reportService.getReportById(id);
            return ApiResponse.success(report, "查询报表成功");
        } catch (Exception e) {
            return ApiResponse.error("查询报表失败: " + e.getMessage());
        }
    }

    /**
     * 查询报表列表
     */
    @GetMapping("/list")
    public ApiResponse<Object> getReportList(
            @RequestParam(required = false) FinanceReportType reportType,
            @RequestParam(required = false) FinanceReportPeriod reportPeriod,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer approvalStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Map<String, Object> result = reportService.getReportList(
                    reportType, reportPeriod, startDate, endDate, departmentId, approvalStatus, page, size);

            return ApiResponse.success(result, "查询报表列表成功");
        } catch (Exception e) {
            return ApiResponse.error("查询报表列表失败: " + e.getMessage());
        }
    }

    /**
     * 审核报表
     */
    @PostMapping("/{id}/approve")
    public ApiResponse<Object> approveReport(
            @PathVariable Long id,
            @RequestParam Integer approve,
            @RequestParam(required = false) String comment) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.approveReport(id, approve, comment);

            if (result.isSuccess()) {
                return ApiResponse.success(null, "报表审核成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("报表审核失败: " + e.getMessage());
        }
    }

    /**
     * 导出报表
     */
    @PostMapping("/{id}/export")
    public ApiResponse<Object> exportReport(
            @PathVariable Long id,
            @RequestParam String exportType) {
        
        try {
            FinanceReportService.ReportGenerationResult result = reportService.exportReport(id, exportType);

            if (result.isSuccess()) {
                return ApiResponse.success(result.getReportFilePath(), "报表导出成功");
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            return ApiResponse.error("报表导出失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Object> getReportStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) FinanceReportType reportType) {
        
        try {
            Map<String, Object> result = reportService.getReportStatistics(startDate, endDate, reportType);

            return ApiResponse.success(result, "获取报表统计信息成功");
        } catch (Exception e) {
            return ApiResponse.error("获取报表统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表类型选项
     */
    @GetMapping("/report-types")
    public ApiResponse<Object> getReportTypes() {
        try {
            Map<String, String> types = new HashMap<>();
            for (FinanceReportType type : FinanceReportType.values()) {
                types.put(type.getCode().toString(), type.getName());
            }

            return ApiResponse.success(types, "获取报表类型选项成功");
        } catch (Exception e) {
            return ApiResponse.error("获取报表类型选项失败: " + e.getMessage());
        }
    }

    /**
     * 获取报表周期选项
     */
    @GetMapping("/period-options")
    public ApiResponse<Object> getPeriodOptions() {
        try {
            Map<String, String> periods = new HashMap<>();
            for (FinanceReportPeriod period : FinanceReportPeriod.values()) {
                periods.put(period.getCode().toString(), period.getName());
            }

            return ApiResponse.success(periods, "获取报表周期选项成功");
        } catch (Exception e) {
            return ApiResponse.error("获取报表周期选项失败: " + e.getMessage());
        }
    }

    /**
     * 获取审批状态选项
     */
    @GetMapping("/status-options")
    public ApiResponse<Object> getStatusOptions() {
        try {
            Map<String, String> options = Map.of(
                "0", "待生成",
                "1", "已生成",
                "2", "已审核",
                "3", "已拒绝",
                "4", "已删除"
            );

            return ApiResponse.success(options, "获取报表状态选项成功");
        } catch (Exception e) {
            return ApiResponse.error("获取报表状态选项失败: " + e.getMessage());
        }
    }

    /**
     * 批量导出报表
     */
    @PostMapping("/batch-export")
    public ApiResponse<Object> batchExportReports(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> reportIds = (List<Long>) request.get("reportIds");
            String exportType = (String) request.getOrDefault("exportType", "EXCEL");

            // TODO: 实现批量报表导出功能
            Map<String, Object> result = new HashMap<>();
            result.put("exportType", exportType);
            result.put("totalCount", reportIds.size());

            return ApiResponse.success(result, "批量报表导出成功");
        } catch (Exception e) {
            return ApiResponse.error("批量报表导出失败: " + e.getMessage());
        }
    }
}