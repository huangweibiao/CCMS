package com.ccms.controller.invoice;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.repository.expense.ExpenseInvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 发票信息控制器
 * 对应设计文档：4.5节 发票表
 */
@RestController
@RequestMapping("/api/invoice/info")
public class InvoiceInfoController {

    private final ExpenseInvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceInfoController(ExpenseInvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * 获取发票列表（分页）
     * GET /api/invoice/info
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseInvoice>> getInvoiceList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer invoiceType,
            @RequestParam(required = false) Integer verifyStatus) {
        Page<ExpenseInvoice> invoicePage;
        if (invoiceType != null && verifyStatus != null) {
            // 根据发票类型和验真状态查询
            List<ExpenseInvoice> list = invoiceRepository.findByInvoiceType(invoiceType).stream()
                    .filter(i -> i.getVerifyStatus().equals(verifyStatus))
                    .toList();
            invoicePage = new PageImpl<>(
                    list.subList(page * size, Math.min((page + 1) * size, list.size())),
                    PageRequest.of(page, size),
                    list.size()
            );
        } else if (invoiceType != null) {
            List<ExpenseInvoice> list = invoiceRepository.findByInvoiceType(invoiceType);
            invoicePage = new PageImpl<>(
                    list.subList(page * size, Math.min((page + 1) * size, list.size())),
                    PageRequest.of(page, size),
                    list.size()
            );
        } else if (verifyStatus != null) {
            List<ExpenseInvoice> list = invoiceRepository.findByVerifyStatus(verifyStatus);
            invoicePage = new PageImpl<>(
                    list.subList(page * size, Math.min((page + 1) * size, list.size())),
                    PageRequest.of(page, size),
                    list.size()
            );
        } else {
            invoicePage = invoiceRepository.findAll(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(invoicePage);
    }

    /**
     * 根据ID获取发票
     * GET /api/invoice/info/{invoiceId}
     */
    @GetMapping("/{invoiceId}")
    public ResponseEntity<ExpenseInvoice> getInvoiceById(@PathVariable Long invoiceId) {
        return invoiceRepository.findById(invoiceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据发票号码获取
     * GET /api/invoice/info/no/{invoiceNo}
     */
    @GetMapping("/no/{invoiceNo}")
    public ResponseEntity<List<ExpenseInvoice>> getInvoiceByNo(@PathVariable String invoiceNo) {
        List<ExpenseInvoice> invoices = invoiceRepository.findByInvoiceNo(invoiceNo);
        if (!invoices.isEmpty()) {
            return ResponseEntity.ok(invoices);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建发票
     * POST /api/invoice/info
     */
    @PostMapping
    public ResponseEntity<ExpenseInvoice> createInvoice(@RequestBody ExpenseInvoice invoice) {
        // 初始化验真状态为未验真
        if (invoice.getVerifyStatus() == null) {
            invoice.setVerifyStatus(0);
        }
        ExpenseInvoice saved = invoiceRepository.save(invoice);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新发票
     * PUT /api/invoice/info/{invoiceId}
     */
    @PutMapping("/{invoiceId}")
    public ResponseEntity<ExpenseInvoice> updateInvoice(@PathVariable Long invoiceId, @RequestBody ExpenseInvoice invoice) {
        Optional<ExpenseInvoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (!existingInvoice.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        invoice.setId(invoiceId);
        ExpenseInvoice updated = invoiceRepository.save(invoice);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除发票
     * DELETE /api/invoice/info/{invoiceId}
     */
    @DeleteMapping("/{invoiceId}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long invoiceId) {
        Optional<ExpenseInvoice> existingInvoice = invoiceRepository.findById(invoiceId);
        if (!existingInvoice.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        invoiceRepository.deleteById(invoiceId);
        return ResponseEntity.ok().build();
    }

    /**
     * 验真发票
     * POST /api/invoice/info/{invoiceId}/verify
     */
    @PostMapping("/{invoiceId}/verify")
    public ResponseEntity<Map<String, Object>> verifyInvoice(@PathVariable Long invoiceId) {
        Optional<ExpenseInvoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (!invoiceOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        ExpenseInvoice invoice = invoiceOpt.get();

        // 模拟验真逻辑（实际项目中应调用第三方发票验真接口）
        // 这里模拟验真通过
        invoice.setVerifyStatus(1); // 已验真通过
        invoice.setVerifyResult("验真通过");
        invoice.setVerifyTime(LocalDateTime.now());
        invoice.setVerifyComment("系统自动验真");
        invoiceRepository.save(invoice);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "发票验真成功");
        result.put("verifyStatus", 1);
        result.put("verifyResult", "验真通过");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取报销单的发票
     * GET /api/invoice/info/reimburse/{reimburseId}
     */
    @GetMapping("/reimburse/{reimburseId}")
    public ResponseEntity<List<ExpenseInvoice>> getInvoicesByReimburseId(@PathVariable Long reimburseId) {
        List<ExpenseInvoice> invoices = invoiceRepository.findByReimburseId(reimburseId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * 统计发票
     * GET /api/invoice/info/statistics
     *
     * 发票类型：1-增值税专票 2-普票 3-电子票
     * 验真状态：0-未验真 1-已验真通过 2-验真失败
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getInvoiceStatistics(
            @RequestParam(required = false) Long reimburseDetailId) {
        Map<String, Object> stats = new HashMap<>();

        if (reimburseDetailId != null) {
            // 按报销明细统计
            List<ExpenseInvoice> invoices = invoiceRepository.findByReimburseDetailId(reimburseDetailId);
            stats.put("total", invoices.size());
            stats.put("totalAmount", invoices.stream()
                    .mapToDouble(i -> i.getInvoiceAmount() != null ? i.getInvoiceAmount().doubleValue() : 0)
                    .sum());
            stats.put("totalTax", invoices.stream()
                    .mapToDouble(i -> i.getTaxAmount() != null ? i.getTaxAmount().doubleValue() : 0)
                    .sum());
        } else {
            // 全局统计
            stats.put("total", invoiceRepository.count());
            stats.put("vatSpecial", invoiceRepository.countByInvoiceType(1)); // 增值税专票
            stats.put("vatNormal", invoiceRepository.countByInvoiceType(2));  // 普票
            stats.put("electronic", invoiceRepository.countByInvoiceType(3));  // 电子票

            // 按验真状态统计
            stats.put("unverified", invoiceRepository.findByVerifyStatus(0).size()); // 未验真
            stats.put("verified", invoiceRepository.findByVerifyStatus(1).size());   // 已验真通过
            stats.put("verifyFailed", invoiceRepository.findByVerifyStatus(2).size()); // 验真失败
        }

        return ResponseEntity.ok(stats);
    }
}
