package com.ccms.repository.loan;

import com.ccms.entity.expense.LoanMain;
import com.ccms.service.loan.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 借款申请单数据访问接口
 */
@Repository
public interface LoanApplyRepository extends JpaRepository<LoanApply, Long> {
    
    /**
     * 根据借款单号查找借款单
     */
    Optional<LoanApply> findByLoanNo(String loanNo);
    
    /**
     * 根据申请人查找借款单
     */
    List<LoanApply> findByApplicantId(Long applicantId);
    
    /**
     * 根据申请人ID和创建时间范围查找借款单
     */
    @Query("SELECT l FROM LoanApply l WHERE l.applicantId = :applicantId AND l.createTime BETWEEN :startTime AND :endTime")
    List<LoanApply> findByApplicantIdAndCreateTimeBetween(@Param("applicantId") Long applicantId, 
                                                          @Param("startTime") LocalDateTime startTime, 
                                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据申请人ID和还款状态查找借款单
     */
    List<LoanApply> findByApplicantIdAndRepaymentStatusIn(Long applicantId, List<Integer> repaymentStatuses);
    
    /**
     * 根据状态查找借款单
     */
    List<LoanApply> findByStatus(Integer status);
    
    /**
     * 根据还款状态查找借款单
     */
    List<LoanApply> findByRepaymentStatus(Integer repaymentStatus);
    
    /**
     * 查找逾期借款单
     */
    @Query("SELECT l FROM LoanApply l WHERE l.expectedRepayDate < CURRENT_DATE AND l.repaymentStatus = 0")
    List<LoanApply> findOverdueLoans();
    
    /**
     * 根据条件查询借款单（分页）
     */
    @Query("SELECT l FROM LoanApply l WHERE " +
           "(:loanNo IS NULL OR l.loanNo LIKE %:loanNo%) AND " +
           "(:applicantName IS NULL OR l.applicantName LIKE %:applicantName%) AND " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:repaymentStatus IS NULL OR l.repaymentStatus = :repaymentStatus) AND " +
           "(:startDate IS NULL OR l.createTime >= :startDate) AND " +
           "(:endDate IS NULL OR l.createTime <= :endDate)")
    Page<LoanApply> findByConditions(@Param("loanNo") String loanNo,
                                      @Param("applicantName") String applicantName,
                                      @Param("status") Integer status,
                                      @Param("repaymentStatus") Integer repaymentStatus,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate,
                                      Pageable pageable);
    
    /**
     * 统计申请人剩余借款总额
     */
    @Query("SELECT SUM(l.remainingBalance) FROM LoanApply l WHERE l.applicantId = :applicantId AND l.repaymentStatus IN (0, 2)")
    BigDecimal sumRemainingBalanceByApplicantId(@Param("applicantId") Long applicantId);
    
    /**
     * 简化版的条件查询方法
     */
    default Page<LoanApply> findByConditions(LoanService.LoanQueryCondition condition, Pageable pageable) {
        LocalDateTime startDate = condition.getStartDate() != null ? condition.getStartDate().atStartOfDay() : null;
        LocalDateTime endDate = condition.getEndDate() != null ? condition.getEndDate().atTime(23, 59, 59) : null;
        
        return findByConditions(
            condition.getLoanNo(),
            condition.getApplicantName(),
            condition.getStatus(),
            condition.getRepaymentStatus(),
            startDate,
            endDate,
            pageable
        );
    }
}