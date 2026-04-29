package com.ccms.controller;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import com.ccms.service.RepaymentManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 还款管理控制器
 */
@RestController
@RequestMapping("/api/repayments")
@CrossOrigin(origins = "*")
public class RepaymentController {

    @Autowired
    private RepaymentManagementService repaymentService;

    /**
     * 创建还款记录
     */
    @PostMapping
    public ResponseEntity<RepaymentResponse> createRepayment(@RequestBody RepaymentRequest request) {
        RepaymentResponse response = repaymentService.createRepayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取还款详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<RepaymentResponse> getRepayment(@PathVariable Long id) {
        RepaymentResponse response = repaymentService.getRepaymentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新还款记录
     */
    @PutMapping("/{id}")
    public ResponseEntity<RepaymentResponse> updateRepayment(@PathVariable Long id, 
                                                            @RequestBody RepaymentRequest request) {
        request.setId(id);
        RepaymentResponse response = repaymentService.updateRepayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除还款记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRepayment(@PathVariable Long id) {
        repaymentService.deleteRepayment(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取借款的还款记录
     */
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<RepaymentResponse>> getRepaymentsByLoan(@PathVariable Long loanId) {
        List<RepaymentResponse> response = repaymentService.getRepaymentsByLoanId(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询还款记录
     */
    @GetMapping
    public ResponseEntity<Page<RepaymentResponse>> getRepayments(Pageable pageable) {
        Page<RepaymentResponse> response = repaymentService.findRepayments(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据状态查询还款记录
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<RepaymentResponse>> getRepaymentsByStatus(@PathVariable Integer status) {
        List<RepaymentResponse> response = repaymentService.findRepaymentsByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * 还款确认
     */
    @PostMapping("/{id}/confirm")
    public ResponseEntity<RepaymentResponse> confirmRepayment(@PathVariable Long id) {
        RepaymentResponse response = repaymentService.confirmRepayment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 还款取消
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RepaymentResponse> cancelRepayment(@PathVariable Long id) {
        RepaymentResponse response = repaymentService.cancelRepayment(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 批量确认还款
     */
    @PostMapping("/batch-confirm")
    public ResponseEntity<Void> batchConfirmRepayments(@RequestBody List<Long> repaymentIds) {
        repaymentService.batchConfirmRepayments(repaymentIds);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户还款记录
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RepaymentResponse>> getUserRepayments(@PathVariable Long userId) {
        List<RepaymentResponse> response = repaymentService.findRepaymentsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 自动还款核销
     */
    @PostMapping("/{loanId}/auto-write-off")
    public ResponseEntity<RepaymentResponse> autoWriteOff(@PathVariable Long loanId) {
        RepaymentResponse response = repaymentService.autoWriteOff(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * 借款未还金额统计
     */
    @GetMapping("/stats/loan/{loanId}")
    public ResponseEntity<Object> getLoanRepaymentStats(@PathVariable Long loanId) {
        Object stats = repaymentService.getLoanRepaymentStats(loanId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 用户未还金额统计
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Object> getUserRepaymentStats(@PathVariable Long userId) {
        Object stats = repaymentService.getUserRepaymentStats(userId);
        return ResponseEntity.ok(stats);
    }
}