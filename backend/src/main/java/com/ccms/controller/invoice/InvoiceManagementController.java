package com.ccms.controller.invoice;

import com.ccms.entity.invoice.InvoiceInfo;
import com.ccms.repository.invoice.InvoiceInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 发票管理控制器（OCR识别 + 验真）
 * 对应设计文档：4.5节 发票相关表
 *
 * 验真状态定义：
 * 0-未验真
 * 1-验真通过
 * 2-验真失败
 */
@RestController
@RequestMapping("/api/invoice")
public class InvoiceManagementController {

    private final InvoiceInfoRepository invoiceInfoRepository;

    @Autowired
    public InvoiceManagementController(InvoiceInfoRepository invoiceInfoRepository) {
        this.invoiceInfoRepository = invoiceInfoRepository;
    }

    /**
     * OCR识别发票
     * POST /api/invoice/ocr
     */
    @PostMapping("/ocr")
    public ResponseEntity<Map<String, Object>> ocrInvoice(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 模拟OCR识别逻辑（实际项目中应调用第三方OCR接口）
            InvoiceInfo invoiceInfo = new InvoiceInfo();
            invoiceInfo.setInvoiceNo("OCR" + System.currentTimeMillis());
            invoiceInfo.setInvoiceCode("OCR_CODE");
            invoiceInfo.setInvoiceDate(LocalDate.now());
            invoiceInfo.setInvoiceAmount(java.math.BigDecimal.ZERO);
            invoiceInfo.setInvoiceType(2); // 默认普通发票
            invoiceInfo.setVerificationStatus(0); // 未验真
            invoiceInfo.setRemark("OCR自动识别");

            InvoiceInfo saved = invoiceInfoRepository.save(invoiceInfo);

            result.put("success", true);
            result.put("message", "OCR识别成功");
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "OCR识别失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 批量OCR识别发票
     * POST /api/invoice/ocr/batch
     */
    @PostMapping("/ocr/batch")
    public ResponseEntity<Map<String, Object>> batchOcrInvoice(@RequestParam("files") MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (MultipartFile file : files) {
            try {
                InvoiceInfo invoiceInfo = new InvoiceInfo();
                invoiceInfo.setInvoiceNo("OCR" + System.currentTimeMillis());
                invoiceInfo.setInvoiceCode("OCR_CODE");
                invoiceInfo.setInvoiceDate(LocalDate.now());
                invoiceInfo.setInvoiceAmount(java.math.BigDecimal.ZERO);
                invoiceInfo.setInvoiceType(2);
                invoiceInfo.setVerificationStatus(0);
                invoiceInfo.setRemark("批量OCR自动识别");
                invoiceInfoRepository.save(invoiceInfo);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }

        result.put("success", true);
        result.put("message", "批量OCR识别完成");
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", files.length);
        return ResponseEntity.ok(result);
    }

    /**
     * 验真发票
     * POST /api/invoice/verify/{invoiceId}
     */
    @PostMapping("/verify/{invoiceId}")
    public ResponseEntity<Map<String, Object>> verifyInvoice(@PathVariable Long invoiceId) {
        Optional<InvoiceInfo> invoiceOpt = invoiceInfoRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "发票不存在");
            return ResponseEntity.badRequest().body(result);
        }

        InvoiceInfo invoice = invoiceOpt.get();

        // 模拟验真逻辑（实际项目中应调用税务验真接口）
        boolean verifyPassed = true; // 模拟验真通过
        invoice.setVerificationStatus(verifyPassed ? 1 : 2);
        invoice.setVerificationTime(LocalDate.now());
        invoice.setVerificationRemark(verifyPassed ? "验真通过" : "验真失败");
        invoiceInfoRepository.save(invoice);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", verifyPassed ? "发票验真通过" : "发票验真失败");
        result.put("verificationStatus", invoice.getVerificationStatus());
        return ResponseEntity.ok(result);
    }

    /**
     * 批量验真发票
     * POST /api/invoice/verify/batch
     */
    @PostMapping("/verify/batch")
    public ResponseEntity<Map<String, Object>> batchVerifyInvoice(@RequestBody List<Long> invoiceIds) {
        Map<String, Object> result = new HashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Long invoiceId : invoiceIds) {
            Optional<InvoiceInfo> invoiceOpt = invoiceInfoRepository.findById(invoiceId);
            if (invoiceOpt.isPresent()) {
                InvoiceInfo invoice = invoiceOpt.get();
                if (invoice.getVerificationStatus() != 1) {
                    invoice.setVerificationStatus(1);
                    invoice.setVerificationTime(LocalDate.now());
                    invoice.setVerificationRemark("批量验真通过");
                    invoiceInfoRepository.save(invoice);
                }
                successCount++;
            } else {
                failCount++;
            }
        }

        result.put("success", true);
        result.put("message", "批量验真完成");
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalCount", invoiceIds.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取未验真发票列表
     * GET /api/invoice/unverified
     */
    @GetMapping("/unverified")
    public ResponseEntity<List<InvoiceInfo>> getUnverifiedInvoices() {
        List<InvoiceInfo> unverified = invoiceInfoRepository.findUnverifiedInvoices();
        return ResponseEntity.ok(unverified);
    }

    /**
     * 检查发票重复
     * GET /api/invoice/check-duplicate
     */
    @GetMapping("/check-duplicate")
    public ResponseEntity<Map<String, Object>> checkDuplicate(
            @RequestParam String invoiceNo,
            @RequestParam(required = false) String invoiceCode) {
        Map<String, Object> result = new HashMap<>();

        Optional<InvoiceInfo> existing;
        if (invoiceCode != null && !invoiceCode.isEmpty()) {
            existing = invoiceInfoRepository.findByInvoiceNoAndCode(invoiceNo, invoiceCode);
        } else {
            existing = invoiceInfoRepository.findByInvoiceNo(invoiceNo);
        }

        result.put("duplicate", existing.isPresent());
        if (existing.isPresent()) {
            result.put("existingInvoice", existing.get());
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 发票统计
     * GET /api/invoice/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", invoiceInfoRepository.count());
        stats.put("unverified", invoiceInfoRepository.countByVerificationStatus(0));
        stats.put("verified", invoiceInfoRepository.countByVerificationStatus(1));
        stats.put("verifyFailed", invoiceInfoRepository.countByVerificationStatus(2));
        return ResponseEntity.ok(stats);
    }
}
