package com.ccms.controller;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.service.LoanManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 借款管理控制器
 */
@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    @Autowired
    private LoanManagementService loanService;

    /**
     * 创建借款申请
     */
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanApplyRequest request) {
        LoanResponse response = loanService.createLoan(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取借款详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable Long id) {
        LoanResponse response = loanService.getLoanById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 更新借款信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> updateLoan(@PathVariable Long id, 
                                                   @RequestBody LoanApplyRequest request) {
        request.setId(id);
        LoanResponse response = loanService.updateLoan(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除借款记录
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取用户借款列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getUserLoans(@PathVariable Long userId) {
        List<LoanResponse> response = loanService.getLoansByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 分页查询借款列表
     */
    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getLoans(Pageable pageable) {
        Page<LoanResponse> response = loanService.findLoans(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 根据状态查询借款
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanResponse>> getLoansByStatus(@PathVariable Integer status) {
        List<LoanResponse> response = loanService.findLoansByStatus(status);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取部门借款列表
     */
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<LoanResponse>> getDepartmentLoans(@PathVariable Long deptId) {
        List<LoanResponse> response = loanService.findLoansByDepartmentId(deptId);
        return ResponseEntity.ok(response);
    }

    /**
     * 提交借款审批
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanResponse> submitForApproval(@PathVariable Long id) {
        LoanResponse response = loanService.submitForApproval(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 取消借款申请
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<LoanResponse> cancelLoan(@PathVariable Long id) {
        LoanResponse response = loanService.cancelLoan(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 借款放款
     */
    @PostMapping("/{id}/disburse")
    public ResponseEntity<LoanResponse> disburseLoan(@PathVariable Long id) {
        LoanResponse response = loanService.disburseLoan(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取待还款借款
     */
    @GetMapping("/pending-repayment")
    public ResponseEntity<List<LoanResponse>> getPendingRepaymentLoans() {
        List<LoanResponse> response = loanService.findPendingRepaymentLoans();
        return ResponseEntity.ok(response);
    }

    /**
     * 获取逾期借款
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        List<LoanResponse> response = loanService.findOverdueLoans();
        return ResponseEntity.ok(response);
    }
}