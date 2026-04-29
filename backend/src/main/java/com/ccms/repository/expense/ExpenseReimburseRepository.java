package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.repository.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseReimburseRepository extends BaseRepository<ExpenseReimburse, Long> {
    
    Optional<ExpenseReimburse> findByReimburseNo(String reimburseNo);
    
    List<ExpenseReimburse> findByApplyUserId(Long applyUserId);
    
    List<ExpenseReimburse> findByDeptId(Long deptId);
    
    List<ExpenseReimburse> findByStatus(Integer status);
    
    List<ExpenseReimburse> findByApprovalStatus(Integer approvalStatus);
    
    List<ExpenseReimburse> findByApplyUserIdAndStatus(Long applyUserId, Integer status);
    
    List<ExpenseReimburse> findByReimburseDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT er FROM ExpenseReimburse er WHERE er.status = :status AND er.approvalStatus = :approvalStatus")
    List<ExpenseReimburse> findByStatusAndApprovalStatus(@Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);
    
    @Query("SELECT COUNT(er) FROM ExpenseReimburse er WHERE er.applyUserId = :userId AND er.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    @Query("SELECT COUNT(er) FROM ExpenseReimburse er WHERE er.status = :status AND er.currentApproverId = :approverId")
    Long countByStatusAndApproverId(@Param("status") Integer status, @Param("approverId") Long approverId);
    
    @Query("SELECT er FROM ExpenseReimburse er WHERE er.status = :status AND er.submitTime BETWEEN :startDate AND :endDate")
    List<ExpenseReimburse> findByStatusAndDateRange(@Param("status") Integer status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(er) FROM ExpenseReimburse er WHERE er.deptId = :deptId AND er.submitTime BETWEEN :startDate AND :endDate")
    Long countByDeptIdAndDateRange(@Param("deptId") Long deptId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT SUM(er.totalAmount) FROM ExpenseReimburse er WHERE er.deptId = :deptId AND er.submitTime BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByDeptIdAndDateRange(@Param("deptId") Long deptId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(er) FROM ExpenseReimburse er WHERE er.deptId = :deptId AND er.status = :status AND er.submitTime BETWEEN :startDate AND :endDate")
    Long countByDeptIdAndStatusAndDateRange(@Param("deptId") Long deptId, @Param("status") Integer status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(er) FROM ExpenseReimburse er WHERE er.deptId = :deptId AND er.paymentStatus = :paymentStatus AND er.submitTime BETWEEN :startDate AND :endDate")
    Long countByDeptIdAndPaymentStatusAndDateRange(@Param("deptId") Long deptId, @Param("paymentStatus") Integer paymentStatus, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT SUM(er.totalAmount) FROM ExpenseReimburse er WHERE er.deptId = :deptId AND er.paymentStatus = 1 AND er.submitTime BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumPaidAmountByDeptIdAndDateRange(@Param("deptId") Long deptId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // 添加缺失的findByUserId方法
    List<ExpenseReimburse> findByUserId(Long userId);

    // ========== 报表统计相关查询方法 ==========

    /**
     * 部门费用统计
     */
    @Query("SELECT er.deptId, er.deptName, SUM(er.totalAmount), COUNT(er) " +
           "FROM ExpenseReimburse er " +
           "WHERE er.submitTime BETWEEN :startTime AND :endTime AND er.status >= 1 " +
           "GROUP BY er.deptId, er.deptName " +
           "ORDER BY SUM(er.totalAmount) DESC")
    List<Object[]> findDepartmentExpenseStats(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 费用类型统计（基于reimburseNo的前缀作为费用类型）
     */
    @Query("SELECT SUBSTRING(er.reimburseNo, 1, 2) as expenseType, SUM(er.totalAmount), COUNT(er) " +
           "FROM ExpenseReimburse er " +
           "WHERE er.submitTime BETWEEN :startTime AND :endTime AND er.status >= 1 " +
           "AND (:deptId IS NULL OR er.deptId = :deptId) " +
           "GROUP BY SUBSTRING(er.reimburseNo, 1, 2) " +
           "ORDER BY SUM(er.totalAmount) DESC")
    List<Object[]> findExpenseTypeStats(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime,
                                       @Param("deptId") Long deptId);

    /**
     * 月度费用趋势分析
     */
    @Query("SELECT DATE_FORMAT(er.submitTime, '%m') as month, SUM(er.totalAmount), COUNT(er) " +
           "FROM ExpenseReimburse er " +
           "WHERE er.submitTime BETWEEN :startTime AND :endTime AND er.status >= 1 " +
           "AND (:deptId IS NULL OR er.deptId = :deptId) " +
           "GROUP BY DATE_FORMAT(er.submitTime, '%m') " +
           "ORDER BY month")
    List<Object[]> findMonthlyExpenseTrend(@Param("startTime") LocalDateTime startTime, 
                                          @Param("endTime") LocalDateTime endTime,
                                          @Param("deptId") Long deptId);

    /**
     * 费用支出排行榜
     */
    @Query("SELECT er.applyUserName, SUM(er.totalAmount), COUNT(er) " +
           "FROM ExpenseReimburse er " +
           "WHERE er.submitTime BETWEEN :startTime AND :endTime AND er.status >= 1 " +
           "GROUP BY er.applyUserId, er.applyUserName " +
           "ORDER BY SUM(er.totalAmount) DESC")
    List<Object[]> findExpenseRanking(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime,
                                     Pageable pageable);

    /**
     * 根据预算ID查询费用金额合计
     */
    @Query("SELECT SUM(er.totalAmount) " +
           "FROM ExpenseReimburse er " +
           "WHERE er.deptId = (SELECT b.deptId FROM Budget b WHERE b.id = :budgetId) " +
           "AND er.submitTime BETWEEN :startTime AND :endTime AND er.status >= 1")
    BigDecimal findSumAmountByBudgetId(@Param("budgetId") Long budgetId,
                                      @Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取费用统计核心指标
     */
    @Query("SELECT COUNT(er), SUM(er.totalAmount), AVG(er.totalAmount), " +
           "COUNT(CASE WHEN er.status = 3 THEN 1 ELSE null END) as approvedCount, " +
           "SUM(CASE WHEN er.status = 3 THEN er.totalAmount ELSE 0 END) as approvedAmount " +
           "FROM ExpenseReimburse er " +
           "WHERE er.submitTime BETWEEN :startTime AND :endTime")
    Object[] findCoreMetrics(@Param("startTime") LocalDateTime startTime,
                            @Param("endTime") LocalDateTime endTime);

    /**
     * 预算执行情况统计
     */
    @Query("SELECT b.deptId, b.budgetName, b.budgetAmount, " +
           "SUM(CASE WHEN er.status = 3 THEN er.totalAmount ELSE 0 END) as actualAmount " +
           "FROM Budget b LEFT JOIN ExpenseReimburse er ON b.deptId = er.deptId " +
           "AND er.submitTime BETWEEN :startTime AND :endTime " +
           "WHERE b.budgetStatus = 'APPROVED' AND b.startDate <= CURRENT_DATE AND b.endDate >= CURRENT_DATE " +
           "GROUP BY b.deptId, b.budgetName, b.budgetAmount")
    List<Object[]> findBudgetExecutionStats(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);
}