package com.ccms.repository.expense;

import com.ccms.entity.expense.RepaymentRecord;
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
 * 还款记录数据访问接口
 */
@Repository
public interface RepaymentRecordRepository extends JpaRepository<RepaymentRecord, Long> {

    /**
     * 根据借款单ID查询还款记录
     */
    List<RepaymentRecord> findByLoanId(Long loanId);

    /**
     * 根据借款单ID查询还款记录（分页）
     */
    Page<RepaymentRecord> findByLoanId(Long loanId, Pageable pageable);

    /**
     * 根据报销单ID查询还款记录
     */
    List<RepaymentRecord> findByReimburseId(Long reimburseId);

    /**
     * 根据还款人ID查询还款记录
     */
    List<RepaymentRecord> findByRepayBy(Long repayBy);

    /**
     * 根据还款人ID查询还款记录（分页）
     */
    Page<RepaymentRecord> findByRepayBy(Long repayBy, Pageable pageable);

    /**
     * 根据还款类型查询还款记录
     */
    List<RepaymentRecord> findByRepayType(Integer repayType);

    /**
     * 根据还款日期范围查询还款记录
     */
    @Query("SELECT rr FROM RepaymentRecord rr WHERE rr.repayDate BETWEEN :startDate AND :endDate")
    List<RepaymentRecord> findByRepayDateRange(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);

    /**
     * 统计指定借款单的还款总额
     */
    @Query("SELECT SUM(rr.repayAmount) FROM RepaymentRecord rr WHERE rr.loanId = :loanId")
    BigDecimal sumRepayAmountByLoanId(@Param("loanId") Long loanId);

    /**
     * 统计指定借款单的还款次数
     */
    @Query("SELECT COUNT(rr) FROM RepaymentRecord rr WHERE rr.loanId = :loanId")
    Long countRepaymentByLoanId(@Param("loanId") Long loanId);

    /**
     * 根据借款单ID和还款类型查询还款记录
     */
    List<RepaymentRecord> findByLoanIdAndRepayType(Long loanId, Integer repayType);

    /**
     * 查询最近的还款记录
     */
    @Query("SELECT rr FROM RepaymentRecord rr WHERE rr.loanId = :loanId ORDER BY rr.repayDate DESC")
    List<RepaymentRecord> findRecentRepaymentsByLoanId(@Param("loanId") Long loanId, Pageable pageable);

    /**
     * 根据报销单ID和还款类型查询还款记录
     */
    Optional<RepaymentRecord> findByReimburseIdAndRepayType(Long reimburseId, Integer repayType);

    /**
     * 统计指定借款人通过报销抵扣的还款金额
     */
    @Query("SELECT SUM(rr.repayAmount) FROM RepaymentRecord rr WHERE rr.repayBy = :userId AND rr.repayType = 2")
    BigDecimal sumReimburseDeductionByUserId(@Param("userId") Long userId);

    /**
     * 统计指定借款单通过报销抵扣的还款金额
     */
    @Query("SELECT SUM(rr.repayAmount) FROM RepaymentRecord rr WHERE rr.loanId = :loanId AND rr.repayType = 2")
    BigDecimal sumReimburseDeductionByLoanId(@Param("loanId") Long loanId);

    /**
     * 根据借款人ID查询还款记录
     */
    @Query("SELECT rr FROM RepaymentRecord rr WHERE rr.repayBy = :userId")
    List<RepaymentRecord> findByUserId(@Param("userId") Long userId);

    /**
     * 根据还款日期排序查询指定借款单的还款记录
     */
    @Query("SELECT rr FROM RepaymentRecord rr WHERE rr.loanId = :loanId ORDER BY rr.repayDate ASC")
    List<RepaymentRecord> findByLoanIdOrderByRepayDateAsc(@Param("loanId") Long loanId);

    /**
     * 查询指定借款单的单个还款记录（用于更新）
     */
    @Query("SELECT rr FROM RepaymentRecord rr WHERE rr.loanId = :loanId AND rr.id = :id")
    Optional<RepaymentRecord> findByIdAndLoanId(@Param("id") Long id, @Param("loanId") Long loanId);

    /**
     * 根据还款人ID和还款日期范围查询还款记录
     */
    @Query("SELECT rr FROM RepaymentRecord rr WHERE rr.repayBy = :userId AND rr.repayDate BETWEEN :startDate AND :endDate")
    List<RepaymentRecord> findByUserIdAndRepayDateRange(@Param("userId") Long userId, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);
}
