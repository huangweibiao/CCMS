package com.ccms.repository;

import com.ccms.entity.expense.LoanRepayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 借款还款记录数据访问接口
 * 
 * @author 系统生成
 */
@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long>, 
                                                  JpaSpecificationExecutor<LoanRepayment> {
    
    /**
     * 根据借款ID查找还款记录
     * 
     * @param loanId 借款ID
     * @return 还款记录列表
     */
    List<LoanRepayment> findByLoanId(Long loanId);
    
    /**
     * 根据借款ID查找还款记录（按时间倒序）
     * 
     * @param loanId 借款ID
     * @return 还款记录列表
     */
    List<LoanRepayment> findByLoanIdOrderByRepaymentTimeDesc(Long loanId);
    
    /**
     * 根据报销单ID查找还款记录
     * 
     * @param reimburseId 报销单ID
     * @return 还款记录列表
     */
    List<LoanRepayment> findByReimburseId(Long reimburseId);
    
    /**
     * 根据还款状态查找记录
     * 
     * @param status 还款状态
     * @return 还款记录列表
     */
    List<LoanRepayment> findByStatus(String status);
    
    /**
     * 根据还款类型查找记录
     * 
     * @param repaymentType 还款类型
     * @return 还款记录列表
     */
    List<LoanRepayment> findByRepaymentType(String repaymentType);
    
    /**
     * 统计指定借款的总还款金额
     * 
     * @param loanId 借款ID
     * @return 总还款金额
     */
    @Query("SELECT SUM(r.repaymentAmount) FROM LoanRepayment r WHERE r.loanId = :loanId AND r.status = 'COMPLETED'")
    BigDecimal sumRepaymentAmountByLoanId(@Param("loanId") Long loanId);
    
    /**
     * 统计指定报销单的总核销金额
     * 
     * @param reimburseId 报销单ID
     * @return 总核销金额
     */
    @Query("SELECT SUM(r.repaymentAmount) FROM LoanRepayment r WHERE r.reimburseId = :reimburseId AND r.status = 'COMPLETED'")
    BigDecimal sumRepaymentAmountByReimburseId(@Param("reimburseId") Long reimburseId);
    
    /**
     * 查找指定时间段内的还款记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 还款记录列表
     */
    List<LoanRepayment> findByRepaymentTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 根据借款ID和还款状态查找记录
     * 
     * @param loanId 借款ID
     * @param status 还款状态
     * @return 还款记录列表
     */
    List<LoanRepayment> findByLoanIdAndStatus(Long loanId, String status);
    
    /**
     * 根据还款类型和状态查找记录
     * 
     * @param repaymentType 还款类型
     * @param status 还款状态
     * @return 还款记录列表
     */
    List<LoanRepayment> findByRepaymentTypeAndStatus(String repaymentType, String status);
    
    /**
     * 查找有超期风险的还款记录
     * 
     * @param startTime 计划还款开始时间
     * @param endTime 计划还款结束时间
     * @param status 还款状态
     * @return 有超期风险的记录
     */
    List<LoanRepayment> findByPlanRepaymentTimeBetweenAndStatus(LocalDateTime startTime, 
                                                               LocalDateTime endTime, 
                                                               String status);
    
    /**
     * 查找已完成还款但报销单未同步的记录
     * 
     * @param repaymentType 还款类型
     * @param status 还款状态
     * @return 需要同步的记录
     */
    @Query("SELECT r FROM LoanRepayment r WHERE r.repaymentType = :repaymentType AND r.status = :status AND r.reimburseId IS NOT NULL AND r.synced = false")
    List<LoanRepayment> findUnsyncedRepayments(@Param("repaymentType") String repaymentType, 
                                              @Param("status") String status);
    
    /**
     * 按还款类型统计总金额分组
     * 
     * @return 分组统计结果
     */
    @Query("SELECT r.repaymentType, SUM(r.repaymentAmount) FROM LoanRepayment r WHERE r.status = 'COMPLETED' GROUP BY r.repaymentType")
    List<Object[]> sumAmountGroupByRepaymentType();
    
    /**
     * 获取最近N笔还款记录
     * 
     * @param limit 记录条数
     * @return 最近还款记录
     */
    @Query("SELECT r FROM LoanRepayment r ORDER BY r.repaymentTime DESC LIMIT :limit")
    List<LoanRepayment> findRecentRepayments(@Param("limit") int limit);
    
    /**
     * 统计月度还款总额
     * 
     * @param year 年份
     * @param month 月份
     * @return 月度还款总额
     */
    @Query("SELECT SUM(r.repaymentAmount) FROM LoanRepayment r WHERE YEAR(r.repaymentTime) = :year AND MONTH(r.repaymentTime) = :month AND r.status = 'COMPLETED'")
    BigDecimal sumMonthlyRepayment(@Param("year") int year, @Param("month") int month);
    
    /**
     * 检查借款是否存在完成状态的还款记录
     * 
     * @param loanId 借款ID
     * @return 是否存在完成还款记录
     */
    @Query("SELECT COUNT(r) > 0 FROM LoanRepayment r WHERE r.loanId = :loanId AND r.status = 'COMPLETED'")
    boolean existsCompletedRepayment(@Param("loanId") Long loanId);
    
    /**
     * 查找需要审批的还款记录
     * 
     * @param status 状态（等待审批）
     * @return 需要审批的记录
     */
    List<LoanRepayment> findByStatusOrderByCreatedTimeAsc(String status);
    
    /**
     * 根据创建时间范围查找记录
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 记录列表
     */
    List<LoanRepayment> findByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}