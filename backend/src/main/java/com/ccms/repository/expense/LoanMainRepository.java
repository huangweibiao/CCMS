package com.ccms.repository.expense;

import com.ccms.entity.expense.LoanMain;
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
 * 借款单数据访问接口
 */
@Repository
public interface LoanMainRepository extends JpaRepository<LoanMain, Long> {

    /**
     * 根据借款单号查询借款单
     */
    Optional<LoanMain> findByLoanNo(String loanNo);

    /**
     * 根据借款人ID查询借款单列表
     */
    List<LoanMain> findByLoanUserId(Long loanUserId);
    


    /**
     * 根据借款人ID查询借款单列表（分页）
     */
    Page<LoanMain> findByLoanUserId(Long loanUserId, Pageable pageable);

    /**
     * 根据状态查询借款单列表
     */
    List<LoanMain> findByStatus(Integer status);

    /**
     * 根据状态查询借款单列表（分页）
     */
    Page<LoanMain> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据借款日期范围查询借款单
     */
    @Query("SELECT lm FROM LoanMain lm WHERE lm.expectRepayDate BETWEEN :startDate AND :endDate")
    List<LoanMain> findByRepayDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * 查询借款余额大于指定值的借款单
     */
    @Query("SELECT lm FROM LoanMain lm WHERE (lm.loanAmount - lm.repaidAmount) > :amount")
    List<LoanMain> findByBalanceGreaterThan(@Param("amount") BigDecimal amount);

    /**
     * 查询指定借款人未还清的借款单
     */
    @Query("SELECT lm FROM LoanMain lm WHERE lm.loanUserId = :userId AND (lm.loanAmount - lm.repaidAmount) > 0")
    List<LoanMain> findUnpaidLoansByUserId(@Param("userId") Long userId);

    /**
     * 查询逾期未还的借款单
     */
    @Query("SELECT lm FROM LoanMain lm WHERE lm.status = 4 AND lm.expectRepayDate < CURRENT_DATE")
    List<LoanMain> findOverdueLoans();

    /**
     * 根据申请单ID查询借款单
     */
    Optional<LoanMain> findByApplyId(Long applyId);

    /**
     * 统计指定借款人的借款总额
     */
    @Query("SELECT SUM(lm.loanAmount) FROM LoanMain lm WHERE lm.loanUserId = :userId")
    BigDecimal sumLoanAmountByUserId(@Param("userId") Long userId);

    /**
     * 统计指定借款人的未还余额
     */
    @Query("SELECT SUM(lm.loanAmount - lm.repaidAmount) FROM LoanMain lm WHERE lm.loanUserId = :userId")
    BigDecimal sumBalanceAmountByUserId(@Param("userId") Long userId);

    /**
     * 根据部门和状态查询借款单
     */
    List<LoanMain> findByLoanDeptIdAndStatus(Long loanDeptId, Integer status);

    /**
     * 根据借款部门ID查询借款单列表
     */
    List<LoanMain> findByLoanDeptId(Long loanDeptId);

    /**
     * 查询待还款的借款单（状态为1-借款中且余额大于0）
     */
    @Query("SELECT lm FROM LoanMain lm WHERE lm.status = 1 AND (lm.loanAmount - lm.repaidAmount) > 0")
    List<LoanMain> findPendingRepaymentLoans();

    /**
     * 查询所有未还清的借款单（余额大于0）
     */
    @Query("SELECT lm FROM LoanMain lm WHERE (lm.loanAmount - lm.repaidAmount) > 0")
    List<LoanMain> findUnpaidLoans();

    /**
     * 查询指定部门未还清的借款单
     */
    @Query("SELECT lm FROM LoanMain lm WHERE lm.loanDeptId = :deptId AND (lm.loanAmount - lm.repaidAmount) > 0")
    List<LoanMain> findUnpaidLoansByDeptId(@Param("deptId") Long deptId);

    /**
     * 查询指定时间内创建的借款单
     */
    @Query("SELECT lm FROM LoanMain lm WHERE lm.createTime >= :startDate AND lm.createTime <= :endDate")
    List<LoanMain> findByCreateTimeBetween(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
}
