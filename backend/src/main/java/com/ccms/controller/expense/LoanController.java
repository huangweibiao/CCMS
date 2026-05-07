package com.ccms.controller.expense;

import com.ccms.entity.expense.LoanMain;
import com.ccms.repository.expense.LoanMainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 借款管理控制器
 * 对应设计文档：4.6节 借款与还款相关表
 *
 * 状态定义：
 * 0-草稿
 * 1-审批中
 * 2-已放款
 * 3-部分还款
 * 4-已还清
 * 5-作废
 */
@RestController
@RequestMapping("/api/loan")
public class LoanController {

    private final LoanMainRepository loanMainRepository;

    @Autowired
    public LoanController(LoanMainRepository loanMainRepository) {
        this.loanMainRepository = loanMainRepository;
    }

    /**
     * 获取借款单列表（分页）
     * GET /api/loan
     */
    @GetMapping
    public ResponseEntity<Page<LoanMain>> getLoanList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        Page<LoanMain> loanPage;
        if (userId != null && status != null) {
            loanPage = loanMainRepository.findAll(PageRequest.of(page, size));
        } else if (userId != null) {
            loanPage = loanMainRepository.findByLoanUserId(userId, PageRequest.of(page, size));
        } else if (status != null) {
            loanPage = loanMainRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            loanPage = loanMainRepository.findAll(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(loanPage);
    }

    /**
     * 根据ID获取借款单
     * GET /api/loan/{loanId}
     */
    @GetMapping("/{loanId}")
    public ResponseEntity<LoanMain> getLoanById(@PathVariable Long loanId) {
        return loanMainRepository.findById(loanId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据借款单号获取借款单
     * GET /api/loan/no/{loanNo}
     */
    @GetMapping("/no/{loanNo}")
    public ResponseEntity<LoanMain> getLoanByNo(@PathVariable String loanNo) {
        return loanMainRepository.findByLoanNo(loanNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建借款单
     * POST /api/loan
     */
    @PostMapping
    public ResponseEntity<LoanMain> createLoan(@RequestBody LoanMain loan) {
        if (loan.getStatus() == null) {
            loan.setStatus(0);
        }
        if (loan.getRepaidAmount() == null) {
            loan.setRepaidAmount(BigDecimal.ZERO);
        }
        LoanMain saved = loanMainRepository.save(loan);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新借款单
     * PUT /api/loan/{loanId}
     */
    @PutMapping("/{loanId}")
    public ResponseEntity<LoanMain> updateLoan(@PathVariable Long loanId, @RequestBody LoanMain loan) {
        loan.setId(loanId);
        LoanMain updated = loanMainRepository.save(loan);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除借款单
     * DELETE /api/loan/{loanId}
     */
    @DeleteMapping("/{loanId}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long loanId) {
        loanMainRepository.deleteById(loanId);
        return ResponseEntity.ok().build();
    }

    /**
     * 提交借款单
     * POST /api/loan/{loanId}/submit
     */
    @PostMapping("/{loanId}/submit")
    public ResponseEntity<Map<String, Object>> submitLoan(@PathVariable Long loanId) {
        LoanMain loan = loanMainRepository.findById(loanId).orElse(null);
        if (loan == null) {
            return ResponseEntity.notFound().build();
        }
        loan.setStatus(1);
        loanMainRepository.save(loan);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "借款单已提交");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户的借款单
     * GET /api/loan/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanMain>> getUserLoans(@PathVariable Long userId) {
        List<LoanMain> loans = loanMainRepository.findByLoanUserId(userId);
        return ResponseEntity.ok(loans);
    }

    /**
     * 统计借款单
     * GET /api/loan/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getLoanStatistics(
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();

        if (userId != null) {
            List<LoanMain> userLoans = loanMainRepository.findByLoanUserId(userId);

            stats.put("total", userLoans.size());
            stats.put("draft", userLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 0).count());
            stats.put("pending", userLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 1).count());
            stats.put("loaned", userLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 2).count());
            stats.put("partialRepaid", userLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 3).count());
            stats.put("repaid", userLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 4).count());
            stats.put("cancelled", userLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 5).count());

            BigDecimal totalLoanAmount = userLoans.stream()
                    .map(LoanMain::getLoanAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRepaidAmount = userLoans.stream()
                    .map(LoanMain::getRepaidAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put("totalLoanAmount", totalLoanAmount);
            stats.put("totalRepaidAmount", totalRepaidAmount);
            stats.put("totalBalanceAmount", totalLoanAmount.subtract(totalRepaidAmount));
        } else {
            long total = loanMainRepository.count();
            stats.put("total", total);

            List<LoanMain> allLoans = loanMainRepository.findAll();
            stats.put("draft", allLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 0).count());
            stats.put("pending", allLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 1).count());
            stats.put("loaned", allLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 2).count());
            stats.put("partialRepaid", allLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 3).count());
            stats.put("repaid", allLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 4).count());
            stats.put("cancelled", allLoans.stream().filter(l -> l.getStatus() != null && l.getStatus() == 5).count());

            BigDecimal totalLoanAmount = allLoans.stream()
                    .map(LoanMain::getLoanAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalRepaidAmount = allLoans.stream()
                    .map(LoanMain::getRepaidAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            stats.put("totalLoanAmount", totalLoanAmount);
            stats.put("totalRepaidAmount", totalRepaidAmount);
            stats.put("totalBalanceAmount", totalLoanAmount.subtract(totalRepaidAmount));
        }

        return ResponseEntity.ok(stats);
    }
}
