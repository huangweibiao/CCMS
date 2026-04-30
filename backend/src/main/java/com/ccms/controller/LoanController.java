package com.ccms.controller;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.service.LoanManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 借款管理控制器
 */
@RestController
@RequestMapping("/api/loans")
@CrossOrigin(origins = "*")
public class LoanController {

    private static final Logger logger = LoggerFactory.getLogger(LoanController.class);
    
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

    /**
     * 借款还款接口
     */
    @PostMapping("/{id}/repay")
    public ResponseEntity<LoanResponse> repayLoan(
            @PathVariable Long id,
            @RequestParam BigDecimal repayAmount,
            @RequestParam(required = false) String repaymentMethod) {
        try {
            // 这里需要调用具体的还款服务
            // 简化处理：假设调用LoanServiceImpl的repayLoan方法
            // 实际实现应与现有的LoanService接口保持一致
            logger.info("收到还款请求，借款单ID：{}，还款金额：{}，还款方式：{}", 
                id, repayAmount, repaymentMethod);
            
            // 获取借款详情
            LoanResponse loan = loanService.getLoanById(id);
            
            // 这里应该调用借款还款的核心业务逻辑
            // 简化处理：返回当前借款信息
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            logger.error("借款还款失败", e);
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 报销核销借款接口
     */
    @PostMapping("/{id}/write-off")
    public ResponseEntity<LoanResponse> writeOffLoan(
            @PathVariable Long id,
            @RequestParam Long reimburseId,
            @RequestParam BigDecimal amount) {
        try {
            logger.info("收到核销请求，借款单ID：{}，报销单ID：{}，核销金额：{}", 
                id, reimburseId, amount);
            
            // 获取借款详情
            LoanResponse loan = loanService.getLoanById(id);
            
            // 这里应该调用借款核销的核心业务逻辑
            // 简化处理：返回当前借款信息
            return ResponseEntity.ok(loan);
        } catch (Exception e) {
            logger.error("借款核销失败", e);
            return ResponseEntity.badRequest().body(null);
        }
    }
}