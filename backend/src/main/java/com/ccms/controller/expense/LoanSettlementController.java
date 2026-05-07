package com.ccms.controller.expense;

import com.ccms.entity.expense.ExpenseSettle;
import com.ccms.repository.expense.ExpenseSettleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 借款结算控制器
 * 对应设计文档：4.6节 借款与还款相关表
 *
 * 结算状态定义：
 * 0-草稿
 * 1-已提交
 * 2-结算中
 * 3-已完成
 * 4-已取消
 */
@RestController
@RequestMapping("/api/loan/settlement")
public class LoanSettlementController {

    private final ExpenseSettleRepository expenseSettleRepository;

    @Autowired
    public LoanSettlementController(ExpenseSettleRepository expenseSettleRepository) {
        this.expenseSettleRepository = expenseSettleRepository;
    }

    /**
     * 获取结算列表（分页）
     * GET /api/loan/settlement
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseSettle>> getSettlementList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long applyUserId,
            @RequestParam(required = false) Integer status) {
        Page<ExpenseSettle> settlePage;
        List<ExpenseSettle> list;
        
        if (applyUserId != null && status != null) {
            list = expenseSettleRepository.findByApplyUserId(applyUserId);
            list = list.stream().filter(s -> status.equals(s.getStatus())).collect(Collectors.toList());
        } else if (applyUserId != null) {
            list = expenseSettleRepository.findByApplyUserId(applyUserId);
        } else if (status != null) {
            list = expenseSettleRepository.findByStatus(status);
        } else {
            return ResponseEntity.ok(expenseSettleRepository.findAll(PageRequest.of(page, size)));
        }
        
        int start = Math.min((int) PageRequest.of(page, size).getOffset(), list.size());
        int end = Math.min((start + size), list.size());
        settlePage = new PageImpl<>(list.subList(start, end), PageRequest.of(page, size), list.size());
        return ResponseEntity.ok(settlePage);
    }

    /**
     * 根据ID获取结算
     * GET /api/loan/settlement/{settleId}
     */
    @GetMapping("/{settleId}")
    public ResponseEntity<ExpenseSettle> getSettlementById(@PathVariable Long settleId) {
        return expenseSettleRepository.findById(settleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建结算
     * POST /api/loan/settlement
     */
    @PostMapping
    public ResponseEntity<ExpenseSettle> createSettlement(@RequestBody ExpenseSettle settle) {
        // 设置默认值
        if (settle.getStatus() == null) {
            settle.setStatus(0); // 草稿
        }
        if (settle.getSettleDate() == null) {
            settle.setSettleDate(java.time.LocalDate.now());
        }
        ExpenseSettle saved = expenseSettleRepository.save(settle);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新结算
     * PUT /api/loan/settlement/{settleId}
     */
    @PutMapping("/{settleId}")
    public ResponseEntity<ExpenseSettle> updateSettlement(@PathVariable Long settleId, @RequestBody ExpenseSettle settle) {
        Optional<ExpenseSettle> existingOpt = expenseSettleRepository.findById(settleId);
        if (existingOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        settle.setId(settleId);
        ExpenseSettle updated = expenseSettleRepository.save(settle);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除结算
     * DELETE /api/loan/settlement/{settleId}
     */
    @DeleteMapping("/{settleId}")
    public ResponseEntity<Void> deleteSettlement(@PathVariable Long settleId) {
        if (!expenseSettleRepository.existsById(settleId)) {
            return ResponseEntity.notFound().build();
        }
        expenseSettleRepository.deleteById(settleId);
        return ResponseEntity.ok().build();
    }

    /**
     * 确认结算
     * POST /api/loan/settlement/{settleId}/confirm
     */
    @PostMapping("/{settleId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmSettlement(@PathVariable Long settleId) {
        Optional<ExpenseSettle> settleOpt = expenseSettleRepository.findById(settleId);
        if (settleOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ExpenseSettle settle = settleOpt.get();
        settle.setStatus(3); // 已完成
        settle.setCompleteTime(LocalDateTime.now());
        expenseSettleRepository.save(settle);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "结算已确认");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取借款的结算记录
     * GET /api/loan/settlement/loan/{loanId}
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<ExpenseSettle>> getSettlementsByLoanId(@PathVariable Long loanId) {
        List<ExpenseSettle> settles = expenseSettleRepository.findByExpenseApplyId(loanId);
        return ResponseEntity.ok(settles);
    }

    /**
     * 统计结算
     * GET /api/loan/settlement/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSettlementStatistics(
            @RequestParam(required = false) Long loanId,
            @RequestParam(required = false) Integer year) {
        Map<String, Object> stats = new HashMap<>();
        
        if (loanId != null) {
            BigDecimal totalSettled = expenseSettleRepository.calculateTotalSettleAmount(loanId);
            stats.put("totalSettledAmount", totalSettled);
            stats.put("settlementCount", expenseSettleRepository.findByExpenseApplyId(loanId).size());
        } else if (year != null) {
            // 按年份统计
            List<ExpenseSettle> allSettles = expenseSettleRepository.findAll();
            long yearCount = allSettles.stream()
                    .filter(s -> s.getSettleDate() != null && s.getSettleDate().getYear() == year)
                    .count();
            BigDecimal yearAmount = allSettles.stream()
                    .filter(s -> s.getSettleDate() != null && s.getSettleDate().getYear() == year)
                    .map(ExpenseSettle::getSettleAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("yearSettlementCount", yearCount);
            stats.put("yearSettlementAmount", yearAmount);
        } else {
            stats.put("totalCount", expenseSettleRepository.count());
            stats.put("draftCount", expenseSettleRepository.findByStatus(0).size());
            stats.put("submittedCount", expenseSettleRepository.findByStatus(1).size());
            stats.put("processingCount", expenseSettleRepository.findByStatus(2).size());
            stats.put("completedCount", expenseSettleRepository.findByStatus(3).size());
            stats.put("cancelledCount", expenseSettleRepository.findByStatus(4).size());
            
            // 统计总金额
            List<ExpenseSettle> allSettles = expenseSettleRepository.findAll();
            BigDecimal totalAmount = allSettles.stream()
                    .map(ExpenseSettle::getSettleAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalSettlementAmount", totalAmount);
        }
        
        return ResponseEntity.ok(stats);
    }
}
