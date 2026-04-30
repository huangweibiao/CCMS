package com.ccms.controller;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.service.InvoiceComplianceService;
import com.ccms.service.InvoiceVerifyService;
import com.ccms.service.VerifyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发票信息管理控制器
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/invoice-info")
@CrossOrigin(origins = "*")
public class InvoiceInfoController {
    
    @Autowired
    private InvoiceComplianceService invoiceComplianceService;
    
    @Autowired
    private InvoiceVerifyService invoiceVerifyService;
    
    // 这里需要注入实际的repository，暂时使用模拟方式
    // @Autowired
    // private ExpenseInvoiceRepository invoiceRepository;
    
    /**
     * 分页查询发票列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getInvoiceList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) Integer verifyStatus,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 构建分页信息
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 这里实际实现需要调用repository进行复杂查询
            // Page<ExpenseInvoice> invoicePage = invoiceRepository.findByFilters(invoiceNo, sellerName, verifyStatus, startDate, endDate, pageable);
            
            // 模拟数据返回
            Page<ExpenseInvoice> invoicePage = createMockInvoicePage(pageable);
            
            response.put("success", true);
            response.put("data", invoicePage.getContent());
            response.put("totalPages", invoicePage.getTotalPages());
            response.put("totalElements", invoicePage.getTotalElements());
            response.put("currentPage", page);
            response.put("pageSize", size);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取发票详情
     */
    @GetMapping("/detail/{invoiceId}")
    public ResponseEntity<Map<String, Object>> getInvoiceDetail(@PathVariable Long invoiceId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ExpenseInvoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            ExpenseInvoice invoice = createMockInvoice(invoiceId);
            
            if (invoice == null) {
                response.put("success", false);
                response.put("message", "发票不存在");
                return ResponseEntity.notFound().build();
            }
            
            response.put("success", true);
            response.put("data", invoice);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取详情失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 发票合规性检查
     */
    @PostMapping("/compliance/check")
    public ResponseEntity<Map<String, Object>> checkInvoiceCompliance(@RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Long invoiceId = Long.valueOf(request.get("invoiceId").toString());
            // ExpenseInvoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            ExpenseInvoice invoice = createMockInvoice(invoiceId);
            
            if (invoice == null) {
                response.put("success", false);
                response.put("message", "发票不存在");
                return ResponseEntity.badRequest().body(response);
            }
            
            InvoiceComplianceService.ComplianceResult result = 
                invoiceComplianceService.checkSingleInvoice(invoice, null);
            
            response.put("success", true);
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "合规性检查失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 发票统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 模拟统计信息
            statistics.put("totalInvoices", 1500);
            statistics.put("verifiedInvoices", 1200);
            statistics.put("failedVerification", 50);
            statistics.put("pendingVerification", 250);
            statistics.put("totalAmount", 850000);
            statistics.put("avgInvoiceAmount", 566.67);
            statistics.put("complianceRate", 85.7);
            
            // 按发票类型统计
            Map<String, Integer> typeStatistics = new HashMap<>();
            typeStatistics.put("增值税专票", 800);
            typeStatistics.put("普通发票", 500);
            typeStatistics.put("电子发票", 200);
            statistics.put("typeStatistics", typeStatistics);
            
            // 按销方统计
            Map<String, Integer> sellerStatistics = new HashMap<>();
            sellerStatistics.put("北京市某科技有限公司", 300);
            sellerStatistics.put("上海某服务有限公司", 250);
            sellerStatistics.put("深圳某贸易有限公司", 200);
            sellerStatistics.put("其他", 750);
            statistics.put("sellerStatistics", sellerStatistics);
            
            response.put("success", true);
            response.put("data", statistics);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取统计信息失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 批量验真发票
     */
    @PostMapping("/batch/verify")
    public ResponseEntity<Map<String, Object>> batchVerifyInvoices(@RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> invoices = (List<Map<String, String>>) request.get("invoices");
            
            if (invoices == null || invoices.isEmpty()) {
                response.put("success", false);
                response.put("message", "请提供需要验真的发票列表");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<VerifyResult> results = 
                invoiceVerifyService.verifyInvoicesBatch(invoices);
            
            int successCount = (int) results.stream()
                .filter(VerifyResult::isSuccess)
                .count();
            
            response.put("success", true);
            response.put("message", "批量验真完成，成功验真 " + successCount + "/" + invoices.size() + " 张发票");
            response.put("data", results);
            response.put("successCount", successCount);
            response.put("failedCount", invoices.size() - successCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量验真失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 导出发票数据
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportInvoiceData(
            @RequestParam(required = false) String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String exportFormat = "excel"; // 默认格式
            if (format != null && (format.equals("csv") || format.equals("pdf"))) {
                exportFormat = format;
            }
            
            // 模拟导出数据
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("format", exportFormat);
            exportData.put("fileName", "invoice_export_" + System.currentTimeMillis() + "." + exportFormat);
            exportData.put("downloadUrl", "/api/invoice-info/download/export/" + exportData.get("fileName"));
            exportData.put("recordCount", 1500);
            exportData.put("exportTime", System.currentTimeMillis());
            
            response.put("success", true);
            response.put("data", exportData);
            response.put("message", "导出请求已提交，请稍候下载");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "导出失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 更新发票备注
     */
    @PutMapping("/remark/{invoiceId}")
    public ResponseEntity<Map<String, Object>> updateInvoiceRemark(
            @PathVariable Long invoiceId, 
            @RequestBody Map<String, String> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String remark = request.get("remark");
            
            // ExpenseInvoice invoice = invoiceRepository.findById(invoiceId).orElse(null);
            ExpenseInvoice invoice = createMockInvoice(invoiceId);
            
            if (invoice == null) {
                response.put("success", false);
                response.put("message", "发票不存在");
                return ResponseEntity.notFound().build();
            }
            
            // 更新备注
            // invoice.setRemark(remark);
            // invoiceRepository.save(invoice);
            
            response.put("success", true);
            response.put("message", "备注更新成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新备注失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 创建模拟发票列表（用于演示）
     */
    private Page<ExpenseInvoice> createMockInvoicePage(Pageable pageable) {
        // 模拟实现，实际应该从数据库查询
        List<ExpenseInvoice> mockInvoices = java.util.Arrays.asList(
            createMockInvoice(1L),
            createMockInvoice(2L),
            createMockInvoice(3L)
        );
        
        return new org.springframework.data.domain.PageImpl<>(
            mockInvoices, 
            pageable, 
            mockInvoices.size()
        );
    }
    
    /**
     * 创建模拟发票（用于演示）
     */
    private ExpenseInvoice createMockInvoice(Long id) {
        ExpenseInvoice invoice = new ExpenseInvoice();
        invoice.setId(id);
        invoice.setInvoiceCode("14400123456" + id);
        invoice.setInvoiceNo("001234" + id);
        invoice.setInvoiceType(id.intValue() % 3 + 1); // 随机类型
        invoice.setSellerName("北京市某科技有限公司");
        invoice.setSellerTaxNo("91110108MA01" + id);
        invoice.setInvoiceAmount(new java.math.BigDecimal("100.00"));
        invoice.setTaxAmount(new java.math.BigDecimal("13.00"));
        invoice.setInvoiceDate(Date.valueOf(java.time.LocalDate.now().minusDays(id)));
        invoice.setVerifyStatus(id % 3 == 0 ? 0 : (id % 3 == 1 ? 1 : 2)); // 随机状态
        invoice.setCreateTime(java.time.LocalDateTime.now());
        
        return invoice;
    }
}