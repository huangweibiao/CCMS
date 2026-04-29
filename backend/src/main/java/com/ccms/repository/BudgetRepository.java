package com.ccms.repository;

import com.ccms.entity.budget.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算仓储接口
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * 查询活跃的预算（预算状态为已批准的）
     */
    @Query("SELECT b FROM Budget b WHERE b.budgetStatus = 'APPROVED' AND b.startDate <= CURRENT_DATE AND b.endDate >= CURRENT_DATE")
    List<Budget> findActiveBudgets();

    /**
     * 根据部门ID查询预算
     */
    @Query("SELECT b.budgetAmount FROM Budget b WHERE b.deptId = :deptId AND b.budgetStatus = 'APPROVED' AND b.startDate <= CURRENT_DATE AND b.endDate >= CURRENT_DATE")
    BigDecimal findBudgetByDeptId(@Param("deptId") Long deptId);

    /**
     * 查询指定年份的预算
     */
    List<Budget> findByBudgetYear(String budgetYear);

    /**
     * 查询部门和年份的预算
     */
    Budget findByDeptIdAndBudgetYear(Long deptId, String budgetYear);

    /**
     * 获取部门预算执行情况
     */
    @Query("SELECT b.deptId, SUM(b.budgetAmount), SUM(b.actualAmount) FROM Budget b WHERE b.budgetYear = :year AND b.budgetStatus = 'APPROVED' GROUP BY b.deptId")
    List<Object[]> getDeptBudgetExecution(@Param("year") String year);

    /**
     * 查询预算执行率超标的部门
     */
    @Query("SELECT b.deptId FROM Budget b WHERE b.actualAmount > b.budgetAmount * 0.8 AND b.budgetStatus = 'APPROVED'")
    List<Long> findOverBudgetDepts();
}