package com.ccms.service;

import com.ccms.entity.expense.RepaymentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 还款服务接口
 */
public interface RepaymentService {

    /**
     * 创建还款记录
     */
    RepaymentRecord createRepayment(RepaymentRecord repayment);

    /**
     * 根据ID获取还款记录
     */
    RepaymentRecord getRepaymentById(Long id);

    /**
     * 根据借款单ID获取还款记录列表
     */
    List<RepaymentRecord> getRepaymentsByLoanId(Long loanId);

    /**
     * 根据借款单ID获取还款记录列表（分页）
     */
    Page<RepaymentRecord> getRepaymentsByLoanId(Long loanId, Pageable pageable);

    /**
     * 根据报销单ID获取还款记录
     */
    List<RepaymentRecord> getRepaymentsByReimburseId(Long reimburseId);

    /**
     * 统计借款单的总还款金额
     */
    BigDecimal getTotalRepaymentAmount(Long loanId);

    /**
     * 统计借款单的还款次数
     */
    Long getRepaymentCountByLoanId(Long loanId);

    /**
     * 现金还款
     */
    boolean repayByCash(Long loanId, BigDecimal amount, String remark);

    /**
     * 银行转账还款
     */
    boolean repayByTransfer(Long loanId, BigDecimal amount, String remark);

    /**
     * 报销抵扣还款
     */
    boolean repayByReimburse(Long loanId, Long reimburseId, BigDecimal amount);

    /**
     * 自动核销借款（报销时调用）
     */
    boolean autoDeductLoan(Long reimburseId, Long userId, BigDecimal reimburseAmount);

    /**
     * 取消还款记录
     */
    boolean cancelRepayment(Long repaymentId);

    /**
     * 获取指定日期范围内的还款记录
     */
    List<RepaymentRecord> getRepaymentsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 统计用户通过报销抵扣的总金额
     */
    BigDecimal getUserReimburseDeductionAmount(Long userId);

    /**
     * 获取借款单最近的还款记录
     */
    List<RepaymentRecord> getRecentRepaymentsByLoanId(Long loanId, int limit);

    /**
     * 批量还款处理
     */
    boolean batchRepayment(List<Long> loanIds, BigDecimal amount, Integer repayType, String remark);

    /**
     * 还款前校验
     */
    boolean validateRepayment(Long loanId, BigDecimal amount);

    /**
     * 生成还款记录编号
     */
    String generateRepaymentNo();

    /**
     * 更新还款记录信息
     */
    RepaymentRecord updateRepayment(RepaymentRecord repayment);
    
    /**
     * 删除还款记录
     */
    void deleteRepayment(Long id);
    
    /**
     * 分页查询还款记录
     */
    Page<RepaymentRecord> findRepayments(Pageable pageable);
    
    /**
     * 根据状态查询还款记录
     */
    List<RepaymentRecord> findRepaymentsByStatus(Integer status);
    
    /**
     * 还款确认
     */
    RepaymentRecord confirmRepayment(Long id);
    

    
    /**
     * 批量确认还款
     */
    void batchConfirmRepayments(List<Long> repaymentIds);
    
    /**
     * 获取用户还款记录
     */
    List<RepaymentRecord> findRepaymentsByUserId(Long userId);
    
    /**
     * 自动还款核销
     */
    RepaymentRecord autoWriteOff(Long loanId);
    
    /**
     * 借款未还金额统计
     */
    Object getLoanRepaymentStats(Long loanId);
    
    /**
     * 用户未还金额统计
     */
    Object getUserRepaymentStats(Long userId);
}