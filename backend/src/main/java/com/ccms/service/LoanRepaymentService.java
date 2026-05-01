package com.ccms.service;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import com.ccms.entity.expense.LoanRepayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 借款还款服务接口
 */
public interface LoanRepaymentService {

    /**
     * 创建还款记录
     */
    LoanRepayment createRepayment(LoanRepayment repayment);

    /**
     * 执行还款操作
     */
    LoanRepayment executeRepayment(Long repaymentId);

    /**
     * 取消还款
     */
    boolean cancelRepayment(Long repaymentId);

    /**
     * 根据ID获取还款记录
     */
    LoanRepayment getRepaymentById(Long id);

    /**
     * 根据还款单号获取还款记录
     */
    LoanRepayment getRepaymentByRepaymentNo(String repaymentNo);

    /**
     * 根据借款ID获取还款记录
     */
    List<LoanRepayment> getRepaymentsByLoanId(Long loanId);

    /**
     * 根据借款ID获取还款记录（分页）
     */
    Page<LoanRepayment> getRepaymentsByLoanId(Long loanId, Pageable pageable);

    /**
     * 获取到期未还款记录
     */
    List<LoanRepayment> getOverdueRepayments();

    /**
     * 获取待还款记录
     */
    List<LoanRepayment> getPendingRepayments();

    /**
     * 统计某借款的总还款金额
     */
    BigDecimal getTotalRepaymentAmountByLoanId(Long loanId);

    /**
     * 统计用户总还款金额
     */
    BigDecimal getTotalRepaymentAmountByUserId(Long userId);

    /**
     * 生成还款单号
     */
    String generateRepaymentNo();

    /**
     * 通过报销单自动还款（核销借款）
     */
    LoanRepayment autoRepaymentByReimbursement(Long loanId, BigDecimal amount, String reimbursementNo);

    /**
     * 强制还款（系统操作）
     */
    LoanRepayment forceRepayment(Long loanId, BigDecimal amount, String remark);

    /**
     * 创建还款申请（DTO接口）
     */
    RepaymentResponse createRepayment(RepaymentRequest request);

    /**
     * 获取用户还款记录
     */
    List<RepaymentResponse> getRepaymentsByUserId(Long userId);

    /**
     * 分页查询还款记录
     */
    Page<RepaymentResponse> findRepayments(Pageable pageable);

    /**
     * 执行报销自动核销
     */
    List<RepaymentResponse> autoSettleByReimbursement(Long reimbursementId);
}