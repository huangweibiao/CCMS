package com.ccms.controller.expense;

import com.ccms.entity.expense.RepaymentRecord;
import com.ccms.repository.expense.RepaymentRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 还款管理控制器
 * 对应设计文档：4.6节 借款与还款相关表
 *
 * 还款类型定义：
 * 1-现金
 * 2-报销抵扣
 * 3-银行转账
 */
@RestController
@RequestMapping("/api/repayment")
public class RepaymentController {

    private final RepaymentRecordRepository repaymentRecordRepository;

    @Autowired
    public RepaymentController(RepaymentRecordRepository repaymentRecordRepository) {
        this.repaymentRecordRepository = repaymentRecordRepository;
    }

    /**
     * 获取还款记录列表（分页）
     * GET /api/repayment
     */
    @GetMapping
    public ResponseEntity<Page<RepaymentRecord>> getRepaymentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) Integer repayType) {
        Page<RepaymentRecord> repaymentPage;
        if (loanId != null && repayType != null) {
            List<RepaymentRecord> list = repaymentRecordRepository.findByLoanIdAndRepayType(loanId, repayType);
            int start = Math.min((int) PageRequest.of(page, size).getOffset(), list.size());
            int end = Math.min((start + size), list.size());
            repaymentPage = new PageImpl<>(
                    list.subList(start, end), PageRequest.of(page, size), list.size());
        } else if (loanId != null) {
            repaymentPage = repaymentRecordRepository.findByLoanId(loanId, PageRequest.of(page, size));
        } else if (repayType != null) {
            List<RepaymentRecord> list = repaymentRecordRepository.findByRepayType(repayType);
            int start = Math.min((int) PageRequest.of(page, size).getOffset(), list.size());
            int end = Math.min((start + size), list.size());
            repaymentPage = new PageImpl<>(
                    list.subList(start, end), PageRequest.of(page, size), list.size());
        } else {
            repaymentPage = repaymentRecordRepository.findAll(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(repaymentPage);
    }

    /**
     * 根据ID获取还款记录
     * GET /api/repayment/{recordId}
     */
    @GetMapping("/{recordId}")
    public ResponseEntity<RepaymentRecord> getRepaymentById(@PathVariable Long recordId) {
        return repaymentRecordRepository.findById(recordId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建还款记录
     * POST /api/repayment
     */
    @PostMapping
    public ResponseEntity<RepaymentRecord> createRepayment(@RequestBody RepaymentRecord repayment) {
        if (repayment.getRepayDate() == null) {
            repayment.setRepayDate(LocalDate.now());
        }
        RepaymentRecord saved = repaymentRecordRepository.save(repayment);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新还款记录
     * PUT /api/repayment/{recordId}
     */
    @PutMapping("/{recordId}")
    public ResponseEntity<RepaymentRecord> updateRepayment(@PathVariable Long recordId, @RequestBody RepaymentRecord repayment) {
        Optional<RepaymentRecord> existingOpt = repaymentRecordRepository.findById(recordId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repayment.setId(recordId);
        RepaymentRecord updated = repaymentRecordRepository.save(repayment);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除还款记录
     * DELETE /api/repayment/{recordId}
     */
    @DeleteMapping("/{recordId}")
    public ResponseEntity<Void> deleteRepayment(@PathVariable Long recordId) {
        if (!repaymentRecordRepository.existsById(recordId)) {
            return ResponseEntity.notFound().build();
        }
        repaymentRecordRepository.deleteById(recordId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取借款的还款记录
     * GET /api/repayment/loan/{loanId}
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<RepaymentRecord>> getRepaymentsByLoanId(@PathVariable Long loanId) {
        List<RepaymentRecord> repayments = repaymentRecordRepository.findByLoanId(loanId);
        return ResponseEntity.ok(repayments);
    }

    /**
     * 统计还款
     * GET /api/repayment/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRepaymentStatistics(
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();

        if (loanId != null) {
            BigDecimal totalRepaid = repaymentRecordRepository.sumRepayAmountByLoanId(loanId);
            Long repaymentCount = repaymentRecordRepository.countRepaymentByLoanId(loanId);
            BigDecimal reimburseDeduction = repaymentRecordRepository.sumReimburseDeductionByLoanId(loanId);

            stats.put("totalRepaidAmount", totalRepaid != null ? totalRepaid : BigDecimal.ZERO);
            stats.put("repaymentCount", repaymentCount != null ? repaymentCount : 0);
            stats.put("reimburseDeductionAmount", reimburseDeduction != null ? reimburseDeduction : BigDecimal.ZERO);
        } else if (userId != null) {
            BigDecimal totalRepaid = repaymentRecordRepository.sumReimburseDeductionByUserId(userId);
            List<RepaymentRecord> userRepayments = repaymentRecordRepository.findByUserId(userId);

            stats.put("totalRepaidAmount", totalRepaid != null ? totalRepaid : BigDecimal.ZERO);
            stats.put("repaymentCount", userRepayments.size());
        } else {
            stats.put("totalCount", repaymentRecordRepository.count());

            List<RepaymentRecord> cashRepayments = repaymentRecordRepository.findByRepayType(1);
            List<RepaymentRecord> reimburseRepayments = repaymentRecordRepository.findByRepayType(2);
            List<RepaymentRecord> transferRepayments = repaymentRecordRepository.findByRepayType(3);

            stats.put("cashRepaymentCount", cashRepayments.size());
            stats.put("reimburseRepaymentCount", reimburseRepayments.size());
            stats.put("transferRepaymentCount", transferRepayments.size());
        }

        return ResponseEntity.ok(stats);
    }
}
