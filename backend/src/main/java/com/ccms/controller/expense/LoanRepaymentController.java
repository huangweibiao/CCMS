package com.ccms.controller.expense;

import com.ccms.entity.expense.LoanRepayment;
import com.ccms.repository.expense.LoanRepaymentRepository;
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

/**
 * 借款还款控制器
 * 对应设计文档：4.6 借款与还款相关表
 */
@RestController
@RequestMapping("/api/loan/repayment")
public class LoanRepaymentController {

    private final LoanRepaymentRepository loanRepaymentRepository;

    @Autowired
    public LoanRepaymentController(LoanRepaymentRepository loanRepaymentRepository) {
        this.loanRepaymentRepository = loanRepaymentRepository;
    }

    /**
     * 获取还款记录列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<LoanRepayment>> getRepaymentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) Integer status) {
        Page<LoanRepayment> repaymentPage;
        if (loanId != null) {
            repaymentPage = loanRepaymentRepository.findByLoanId(loanId, PageRequest.of(page, size));
        } else if (status != null) {
            List<LoanRepayment> list = loanRepaymentRepository.findByStatus(status);
            int start = Math.min((int) PageRequest.of(page, size).getOffset(), list.size());
            int end = Math.min((start + size), list.size());
            repaymentPage = new PageImpl<>(list.subList(start, end), PageRequest.of(page, size), list.size());
        } else {
            repaymentPage = loanRepaymentRepository.findAll(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(repaymentPage);
    }

    /**
     * 根据ID获取还款记录
     */
    @GetMapping("/{repaymentId}")
    public ResponseEntity<LoanRepayment> getRepaymentById(@PathVariable Long repaymentId) {
        return loanRepaymentRepository.findById(repaymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据还款单号获取还款记录
     */
    @GetMapping("/no/{repaymentNo}")
    public ResponseEntity<LoanRepayment> getRepaymentByNo(@PathVariable String repaymentNo) {
        return loanRepaymentRepository.findByRepaymentNo(repaymentNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建还款记录
     */
    @PostMapping
    public ResponseEntity<LoanRepayment> createRepayment(@RequestBody LoanRepayment repayment) {
        repayment.setStatus(LoanRepayment.STATUS_PENDING);
        repayment.setApprovalStatus(LoanRepayment.APPROVAL_PENDING);
        LoanRepayment saved = loanRepaymentRepository.save(repayment);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新还款记录
     */
    @PutMapping("/{repaymentId}")
    public ResponseEntity<LoanRepayment> updateRepayment(@PathVariable Long repaymentId, @RequestBody LoanRepayment repayment) {
        repayment.setId(repaymentId);
        LoanRepayment updated = loanRepaymentRepository.save(repayment);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除还款记录
     */
    @DeleteMapping("/{repaymentId}")
    public ResponseEntity<Void> deleteRepayment(@PathVariable Long repaymentId) {
        loanRepaymentRepository.deleteById(repaymentId);
        return ResponseEntity.ok().build();
    }

    /**
     * 确认还款
     */
    @PostMapping("/{repaymentId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmRepayment(@PathVariable Long repaymentId) {
        LoanRepayment repayment = loanRepaymentRepository.findById(repaymentId).orElse(null);
        if (repayment == null) {
            return ResponseEntity.notFound().build();
        }
        repayment.setStatus(LoanRepayment.STATUS_CONFIRMED);
        loanRepaymentRepository.save(repayment);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "还款已确认");
        return ResponseEntity.ok(result);
    }

    /**
     * 审批还款
     */
    @PostMapping("/{repaymentId}/approve")
    public ResponseEntity<Map<String, Object>> approveRepayment(
            @PathVariable Long repaymentId,
            @RequestParam Long approverId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String comment) {
        LoanRepayment repayment = loanRepaymentRepository.findById(repaymentId).orElse(null);
        if (repayment == null) {
            return ResponseEntity.notFound().build();
        }
        repayment.setApprovalStatus(approved ? LoanRepayment.APPROVAL_APPROVED : LoanRepayment.APPROVAL_REJECTED);
        repayment.setApprovedBy(approverId);
        repayment.setApprovalComment(comment);
        loanRepaymentRepository.save(repayment);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", approved ? "还款审批通过" : "还款审批驳回");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取借款的还款记录
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanRepayment>> getRepaymentsByLoan(@PathVariable Long loanId) {
        List<LoanRepayment> repayments = loanRepaymentRepository.findByLoanId(loanId);
        return ResponseEntity.ok(repayments);
    }

    /**
     * 获取用户的还款记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanRepayment>> getRepaymentsByUser(@PathVariable Long userId) {
        List<LoanRepayment> repayments = loanRepaymentRepository.findByUserId(userId);
        return ResponseEntity.ok(repayments);
    }

    /**
     * 获取到期未还款记录
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanRepayment>> getOverdueRepayments() {
        List<LoanRepayment> overdue = loanRepaymentRepository.findOverdueRepayments(LocalDate.now());
        return ResponseEntity.ok(overdue);
    }

    /**
     * 统计还款金额
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getRepaymentStatistics(
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();
        if (loanId != null) {
            BigDecimal total = loanRepaymentRepository.sumRepaymentAmountByLoanId(loanId);
            stats.put("loanId", loanId);
            stats.put("totalRepaymentAmount", total);
        } else if (userId != null) {
            BigDecimal total = loanRepaymentRepository.sumRepaymentAmountByUserId(userId);
            stats.put("userId", userId);
            stats.put("totalRepaymentAmount", total);
        }
        return ResponseEntity.ok(stats);
    }
}
