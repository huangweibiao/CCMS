package com.ccms.service;

import com.ccms.entity.expense.LoanMain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 借款服务接口
 */
public interface LoanService {

    /**
     * 申请借款
     */
    LoanMain applyLoan(LoanMain loan);

    /**
     * 更新借款单信息
     */
    LoanMain updateLoan(LoanMain loan);

    /**
     * 根据ID获取借款单
     */
    LoanMain getLoanById(Long id);

    /**
     * 根据借款单号获取借款单
     */
    LoanMain getLoanByLoanNo(String loanNo);

    /**
     * 根据用户ID获取借款列表
     */
    List<LoanMain> getLoansByUserId(Long userId);

    /**
     * 根据用户ID获取借款列表（分页）
     */
    Page<LoanMain> getLoansByUserId(Long userId, Pageable pageable);

    /**
     * 根据状态获取借款列表
     */
    List<LoanMain> getLoansByStatus(Integer status);

    /**
     * 获取逾期借款列表
     */
    List<LoanMain> getOverdueLoans();

    /**
     * 获取用户未还清的借款
     */
    List<LoanMain> getUnpaidLoansByUserId(Long userId);

    /**
     * 计算用户借款总额
     */
    BigDecimal getUserTotalLoanAmount(Long userId);

    /**
     * 计算用户未还余额
     */
    BigDecimal getUserUnpaidBalance(Long userId);

    /**
     * 审批借款申请
     */
    boolean approveLoan(Long loanId, String remark);

    /**
     * 驳回借款申请
     */
    boolean rejectLoan(Long loanId, String remark);

    /**
     * 放款操作
     */
    boolean disburseLoan(Long loanId);

    /**
     * 更新借款状态
     */
    boolean updateLoanStatus(Long loanId, Integer status);

    /**
     * 检查用户是否可以借款
     */
    boolean canUserBorrow(Long userId, BigDecimal amount);

    /**
     * 生成借款单号
     */
    String generateLoanNo();

    /**
     * 统计指定日期范围内的借款金额
     */
    BigDecimal getLoanAmountBetweenDates(LocalDate startDate, LocalDate endDate);
}