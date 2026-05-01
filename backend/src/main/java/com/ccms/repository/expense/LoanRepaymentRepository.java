package com.ccms.repository.expense;

import com.ccms.entity.expense.LoanRepayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 借款还款记录仓库接口
 */
@Repository
public interface LoanRepaymentRepository extends JpaRepository<LoanRepayment, Long> {
    
    /**
     * 根据借款ID查询还款记录
     */
    List<LoanRepayment> findByLoanId(Long loanId);
    
    /**
     * 根据借款ID分页查询还款记录
     */
    Page<LoanRepayment> findByLoanId(Long loanId, Pageable pageable);
    
    /**
     * 根据还款单号查询还款记录
     */
    Optional<LoanRepayment> findByRepaymentNo(String repaymentNo);
    
    /**
     * 查询到期未还款记录
     */
    @Query("SELECT lr FROM LoanRepayment lr WHERE lr.dueDate <= :currentDate AND lr.status = 0")
    List<LoanRepayment> findOverdueRepayments(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 查询用户所有还款记录
     */
    @Query("SELECT lr FROM LoanRepayment lr JOIN LoanMain lm ON lr.loanId = lm.id WHERE lm.loanUserId = :userId")
    List<LoanRepayment> findByUserId(@Param("userId") Long userId);
    
    /**
     * 查询某段时间内的还款记录
     */
    @Query("SELECT lr FROM LoanRepayment lr WHERE lr.repaymentDate BETWEEN :startDate AND :endDate")
    List<LoanRepayment> findByRepaymentDateRange(@Param("startDate") LocalDate startDate, 
                                                 @Param("endDate") LocalDate endDate);
    
    /**
     * 统计某借款的总还款金额
     */
    @Query("SELECT COALESCE(SUM(lr.repaymentAmount), 0) FROM LoanRepayment lr WHERE lr.loanId = :loanId AND lr.status = 1")
    BigDecimal sumRepaymentAmountByLoanId(@Param("loanId") Long loanId);
    
    /**
     * 统计用户总还款金额
     */
    @Query("SELECT COALESCE(SUM(lr.repaymentAmount), 0) FROM LoanRepayment lr JOIN LoanMain lm ON lr.loanId = lm.id WHERE lm.loanUserId = :userId AND lr.status = 1")
    BigDecimal sumRepaymentAmountByUserId(@Param("userId") Long userId);
    
    /**
     * 查询待还款记录
     */
    @Query("SELECT lr FROM LoanRepayment lr WHERE lr.status = 0 AND lr.dueDate > :currentDate")
    List<LoanRepayment> findPendingRepayments(@Param("currentDate") LocalDate currentDate);
    
    /**
     * 根据还款状态查询记录
     */
    List<LoanRepayment> findByStatus(Integer status);
}