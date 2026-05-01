package com.ccms.repository.statistics;

import com.ccms.entity.statistics.ExpenseStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 费用统计数据访问接口
 */
@Repository
public interface ExpenseStatisticsRepository extends JpaRepository<ExpenseStatistics, Long> {

    /**
     * 根据统计日期和统计周期删除数据
     */
    void deleteByStatDateAndStatPeriod(LocalDate statDate, String statPeriod);

    /**
     * 根据统计周期和日期范围删除数据
     */
    void deleteByStatPeriodAndStatDateBetween(String statPeriod, LocalDate startDate, LocalDate endDate);

    /**
     * 根据部门和日期范围查询统计数据
     */
    List<ExpenseStatistics> findByDepartmentIdAndStatDateBetween(Long departmentId, LocalDate startDate, LocalDate endDate);

    /**
     * 根据日期范围查询统计数据
     */
    List<ExpenseStatistics> findByStatDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * 根据费用类型和日期范围查询统计数据
     */
    List<ExpenseStatistics> findByExpenseTypeCodeAndStatDateBetween(String expenseTypeCode, LocalDate startDate, LocalDate endDate);

    /**
     * 获取部门费用总量统计
     */
    @Query("SELECT es.departmentId, es.departmentName, SUM(es.totalAmount) " +
           "FROM ExpenseStatistics es " +
           "WHERE es.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY es.departmentId, es.departmentName")
    List<Object[]> getDepartmentExpenseSummary(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);

    /**
     * 获取费用类型月度趋势
     */
    @Query("SELECT es.expenseTypeCode, es.expenseTypeName, es.statDate, SUM(es.totalAmount) " +
           "FROM ExpenseStatistics es " +
           "WHERE es.statDate BETWEEN :startDate AND :endDate " +
           "AND es.statPeriod = 'MONTH' " +
           "GROUP BY es.expenseTypeCode, es.expenseTypeName, es.statDate")
    List<Object[]> getExpenseTypeMonthlyTrend(@Param("startDate") LocalDate startDate, 
                                             @Param("endDate") LocalDate endDate);

    /**
     * 获取预算使用率排名
     */
    @Query("SELECT es.departmentId, es.departmentName, MAX(es.budgetUsageRate) " +
           "FROM ExpenseStatistics es " +
           "WHERE es.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY es.departmentId, es.departmentName " +
           "ORDER BY MAX(es.budgetUsageRate) DESC")
    List<Object[]> getBudgetUsageRanking(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    /**
     * 获取审批通过率统计
     */
    @Query("SELECT es.departmentId, es.departmentName, AVG(es.approvalPassRate) " +
           "FROM ExpenseStatistics es " +
           "WHERE es.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY es.departmentId, es.departmentName")
    List<Object[]> getApprovalPassRateStats(@Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);

    /**
     * 获取最新统计日期
     */
    @Query("SELECT MAX(es.statDate) FROM ExpenseStatistics es WHERE es.statPeriod = :statPeriod")
    LocalDate getLatestStatDate(@Param("statPeriod") String statPeriod);

    /**
     * 检查统计日期是否存在
     */
    boolean existsByStatDateAndStatPeriod(LocalDate statDate, String statPeriod);

    /**
     * 获取部门支出最多的费用类型
     */
    @Query("SELECT e.expenseTypeCode, e.expenseTypeName, SUM(e.totalAmount) " +
           "FROM ExpenseStatistics e " +
           "WHERE e.departmentId = :departmentId " +
           "AND e.statDate BETWEEN :startDate AND :endDate " +
           "GROUP BY e.expenseTypeCode, e.expenseTypeName " +
           "ORDER BY SUM(e.totalAmount) DESC")
    List<Object[]> getTopExpenseTypesByDepartment(@Param("departmentId") Long departmentId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);
}