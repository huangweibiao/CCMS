package com.ccms.controller.expense;

import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.repository.expense.ExpenseReimburseMainRepository;
import com.ccms.repository.expense.ExpenseReimburseDetailRepository;
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
 * 费用报销控制器
 * 对应设计文档：4.5 费用报销相关表
 */
@RestController
@RequestMapping("/api/expense/reimburse")
public class ExpenseReimburseController {

    private final ExpenseReimburseMainRepository reimburseMainRepository;
    private final ExpenseReimburseDetailRepository reimburseDetailRepository;

    @Autowired
    public ExpenseReimburseController(ExpenseReimburseMainRepository reimburseMainRepository,
                                      ExpenseReimburseDetailRepository reimburseDetailRepository) {
        this.reimburseMainRepository = reimburseMainRepository;
        this.reimburseDetailRepository = reimburseDetailRepository;
    }

    /**
     * 获取报销单列表（分页）
     * GET /api/expense/reimburse
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseReimburseMain>> getReimburseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        
        List<ExpenseReimburseMain> list;
        if (userId != null && status != null) {
            list = reimburseMainRepository.findBySubmitUserIdAndStatus(userId, status);
        } else if (userId != null) {
            list = reimburseMainRepository.findBySubmitUserId(userId);
        } else if (status != null) {
            list = reimburseMainRepository.findByStatus(status);
        } else {
            return ResponseEntity.ok(reimburseMainRepository.findAll(PageRequest.of(page, size)));
        }
        
        // 手动分页
        int start = Math.min(page * size, list.size());
        int end = Math.min(start + size, list.size());
        Page<ExpenseReimburseMain> reimbursePage = new PageImpl<>(
                list.subList(start, end),
                PageRequest.of(page, size),
                list.size()
        );
        return ResponseEntity.ok(reimbursePage);
    }

    /**
     * 根据ID获取报销单
     * GET /api/expense/reimburse/{reimburseId}
     */
    @GetMapping("/{reimburseId}")
    public ResponseEntity<ExpenseReimburseMain> getReimburseById(@PathVariable Long reimburseId) {
        return reimburseMainRepository.findById(reimburseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据报销单号获取
     * GET /api/expense/reimburse/no/{reimburseNo}
     */
    @GetMapping("/no/{reimburseNo}")
    public ResponseEntity<ExpenseReimburseMain> getReimburseByNo(@PathVariable String reimburseNo) {
        Optional<ExpenseReimburseMain> reimburse = reimburseMainRepository.findByReimburseNo(reimburseNo);
        return reimburse.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建报销单
     * POST /api/expense/reimburse
     */
    @PostMapping
    public ResponseEntity<ExpenseReimburseMain> createReimburse(@RequestBody ExpenseReimburseMain reimburse) {
        // 初始化状态为草稿
        if (reimburse.getStatus() == null) {
            reimburse.setStatus(0);
        }
        ExpenseReimburseMain saved = reimburseMainRepository.save(reimburse);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新报销单
     * PUT /api/expense/reimburse/{reimburseId}
     */
    @PutMapping("/{reimburseId}")
    public ResponseEntity<ExpenseReimburseMain> updateReimburse(@PathVariable Long reimburseId, @RequestBody ExpenseReimburseMain reimburse) {
        Optional<ExpenseReimburseMain> existingReimburse = reimburseMainRepository.findById(reimburseId);
        if (!existingReimburse.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        reimburse.setId(reimburseId);
        ExpenseReimburseMain updated = reimburseMainRepository.save(reimburse);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除报销单
     * DELETE /api/expense/reimburse/{reimburseId}
     */
    @DeleteMapping("/{reimburseId}")
    public ResponseEntity<Void> deleteReimburse(@PathVariable Long reimburseId) {
        Optional<ExpenseReimburseMain> existingReimburse = reimburseMainRepository.findById(reimburseId);
        if (!existingReimburse.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        // 先删除明细
        reimburseDetailRepository.deleteByReimburseId(reimburseId);
        // 再删除主表
        reimburseMainRepository.deleteById(reimburseId);
        return ResponseEntity.ok().build();
    }

    /**
     * 提交报销单
     * POST /api/expense/reimburse/{reimburseId}/submit
     */
    @PostMapping("/{reimburseId}/submit")
    public ResponseEntity<Map<String, Object>> submitReimburse(@PathVariable Long reimburseId) {
        Optional<ExpenseReimburseMain> reimburseOpt = reimburseMainRepository.findById(reimburseId);
        if (!reimburseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有草稿状态才能提交
        if (reimburse.getStatus() != 0) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "只有草稿状态的报销单才能提交");
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        // 更新状态为审批中
        reimburse.setStatus(1);
        reimburse.setSubmitTime(LocalDateTime.now());
        reimburseMainRepository.save(reimburse);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "报销单已提交");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取用户的报销单
     * GET /api/expense/reimburse/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ExpenseReimburseMain>> getUserReimburses(@PathVariable Long userId) {
        List<ExpenseReimburseMain> reimburses = reimburseMainRepository.findByReimburseUserId(userId);
        return ResponseEntity.ok(reimburses);
    }

    /**
     * 统计报销单
     * GET /api/expense/reimburse/statistics
     * 
     * 状态定义：0-草稿 1-审批中 2-通过 3-驳回 4-待支付 5-已支付 6-作废
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReimburseStatistics(
            @RequestParam(required = false) Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        if (userId != null) {
            // 按用户统计
            stats.put("total", reimburseMainRepository.findByReimburseUserId(userId).size());
            stats.put("draft", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 0).size());
            stats.put("pending", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 1).size());
            stats.put("approved", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 2).size());
            stats.put("rejected", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 3).size());
            stats.put("waitPay", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 4).size());
            stats.put("paid", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 5).size());
            stats.put("cancelled", reimburseMainRepository.findBySubmitUserIdAndStatus(userId, 6).size());
        } else {
            // 全局统计
            stats.put("total", reimburseMainRepository.count());
            stats.put("draft", reimburseMainRepository.findByStatus(0).size());
            stats.put("pending", reimburseMainRepository.findByStatus(1).size());
            stats.put("approved", reimburseMainRepository.findByStatus(2).size());
            stats.put("rejected", reimburseMainRepository.findByStatus(3).size());
            stats.put("waitPay", reimburseMainRepository.findByStatus(4).size());
            stats.put("paid", reimburseMainRepository.findByStatus(5).size());
            stats.put("cancelled", reimburseMainRepository.findByStatus(6).size());
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取报销单明细列表
     * GET /api/expense/reimburse/{reimburseId}/details
     */
    @GetMapping("/{reimburseId}/details")
    public ResponseEntity<List<ExpenseReimburseDetail>> getReimburseDetails(@PathVariable Long reimburseId) {
        // 先检查报销单是否存在
        Optional<ExpenseReimburseMain> reimburse = reimburseMainRepository.findById(reimburseId);
        if (!reimburse.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        List<ExpenseReimburseDetail> details = reimburseDetailRepository.findByReimburseId(reimburseId);
        return ResponseEntity.ok(details);
    }

    /**
     * 添加报销单明细
     * POST /api/expense/reimburse/{reimburseId}/details
     */
    @PostMapping("/{reimburseId}/details")
    public ResponseEntity<ExpenseReimburseDetail> addReimburseDetail(@PathVariable Long reimburseId,
                                                                     @RequestBody ExpenseReimburseDetail detail) {
        // 先检查报销单是否存在
        Optional<ExpenseReimburseMain> reimburse = reimburseMainRepository.findById(reimburseId);
        if (!reimburse.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        detail.setReimburseId(reimburseId);
        ExpenseReimburseDetail saved = reimburseDetailRepository.save(detail);
        return ResponseEntity.ok(saved);
    }

    /**
     * 撤回报销单
     * POST /api/expense/reimburse/{reimburseId}/withdraw
     */
    @PostMapping("/{reimburseId}/withdraw")
    public ResponseEntity<Map<String, Object>> withdrawReimburse(@PathVariable Long reimburseId) {
        Optional<ExpenseReimburseMain> reimburseOpt = reimburseMainRepository.findById(reimburseId);
        if (!reimburseOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有审批中状态才能撤回
        if (reimburse.getStatus() != 1) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("success", false);
            errorResult.put("message", "只有审批中的报销单才能撤回");
            return ResponseEntity.badRequest().body(errorResult);
        }
        
        // 更新状态为草稿
        reimburse.setStatus(0);
        reimburseMainRepository.save(reimburse);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "报销单已撤回");
        return ResponseEntity.ok(result);
    }
}
