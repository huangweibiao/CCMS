package com.ccms.controller.expense;

import com.ccms.dto.LoanApplyRequest;
import com.ccms.dto.LoanResponse;
import com.ccms.entity.expense.LoanMain;
import com.ccms.service.impl.LoanServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 借款管理控制器
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanServiceImpl loanService;

    @Autowired
    public LoanController(LoanServiceImpl loanService) {
        this.loanService = loanService;
    }

    /**
     * 创建借款申请
     */
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@RequestBody LoanApplyRequest request) {
        try {
            LoanResponse response = loanService.createLoan(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取借款详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanResponse> getLoan(@PathVariable Long id) {
        try {
            LoanResponse response = loanService.getLoanResponseById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 更新借款信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<LoanResponse> updateLoan(@PathVariable Long id, 
                                                    @RequestBody LoanApplyRequest request) {
        try {
            request.setId(id);
            LoanResponse response = loanService.updateLoan(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除借款记录（只有草稿状态可以删除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        try {
            loanService.deleteLoan(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取用户借款列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<LoanResponse>> getUserLoans(@PathVariable Long userId) {
        List<LoanResponse> responses = loanService.getLoanResponsesByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 分页查询借款列表
     */
    @GetMapping
    public ResponseEntity<Page<LoanResponse>> getLoans(Pageable pageable) {
        Page<LoanResponse> page = loanService.findLoans(pageable);
        return ResponseEntity.ok(page);
    }

    /**
     * 根据状态查询借款列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanResponse>> getLoansByStatus(@PathVariable Integer status) {
        List<LoanResponse> responses = loanService.findLoansByStatus(status);
        return ResponseEntity.ok(responses);
    }

    /**
     * 根据部门查询借款列表
     */
    @GetMapping("/department/{deptId}")
    public ResponseEntity<List<LoanResponse>> getLoansByDepartment(@PathVariable Long deptId) {
        List<LoanResponse> responses = loanService.findLoansByDepartmentId(deptId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 提交借款审批
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<LoanResponse> submitForApproval(@PathVariable Long id) {
        try {
            LoanResponse response = loanService.submitForApproval(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 审批借款申请（批准）
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<LoanResponse> approveLoan(@PathVariable Long id, 
                                                    @RequestParam(required = false) String remark) {
        try {
            boolean success = loanService.approveLoan(id, remark != null ? remark : "审批通过");
            if (success) {
                LoanResponse response = loanService.getLoanResponseById(id);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 审批借款申请（驳回）
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<LoanResponse> rejectLoan(@PathVariable Long id, 
                                                   @RequestParam(required = false) String remark) {
        try {
            boolean success = loanService.rejectLoan(id, remark != null ? remark : "申请驳回");
            if (success) {
                LoanResponse response = loanService.getLoanResponseById(id);
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 借款放款
     */
    @PostMapping("/{id}/disburse")
    public ResponseEntity<LoanResponse> disburseLoan(@PathVariable Long id) {
        try {
            LoanResponse response = loanService.disburseLoanResponse(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 取消借款申请
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<LoanResponse> cancelLoan(@PathVariable Long id) {
        try {
            LoanResponse response = loanService.cancelLoan(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取待还款借款列表
     */
    @GetMapping("/pending-repayment")
    public ResponseEntity<List<LoanResponse>> getPendingRepaymentLoans() {
        List<LoanResponse> responses = loanService.findPendingRepaymentLoans();
        return ResponseEntity.ok(responses);
    }

    /**
     * 获取逾期借款列表
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<LoanResponse>> getOverdueLoans() {
        List<LoanResponse> responses = loanService.findOverdueLoans();
        return ResponseEntity.ok(responses);
    }

    /**
     * 检查用户是否可以借款
     */
    @GetMapping("/check-borrow-eligibility/{userId}")
    public ResponseEntity<Boolean> checkBorrowEligibility(@PathVariable Long userId,
                                                          @RequestParam BigDecimal amount) {
        boolean canBorrow = loanService.canUserBorrow(userId, amount);
        return ResponseEntity.ok(canBorrow);
    }

    /**
     * 计算用户借款总额
     */
    @GetMapping("/user/{userId}/total-loan")
    public ResponseEntity<BigDecimal> getUserTotalLoan(@PathVariable Long userId) {
        BigDecimal total = loanService.getUserTotalLoanAmount(userId);
        return ResponseEntity.ok(total);
    }

    /**
     * 计算用户未还余额
     */
    @GetMapping("/user/{userId}/unpaid-balance")
    public ResponseEntity<BigDecimal> getUserUnpaidBalance(@PathVariable Long userId) {
        BigDecimal balance = loanService.getUserUnpaidBalance(userId);
        return ResponseEntity.ok(balance);
    }

    /**
     * 统计时间段内的借款金额
     */
    @GetMapping("/amount-stats")
    public ResponseEntity<BigDecimal> getLoanAmountBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        // 这个方法需要特殊处理，先返回占位值
        return ResponseEntity.ok(BigDecimal.ZERO);
    }
}