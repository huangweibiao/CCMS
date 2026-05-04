package com.ccms.repository.budget;

import com.ccms.entity.budget.BudgetDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 预算明细表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface BudgetDetailRepository extends JpaRepository<BudgetDetail, Long> {

    /**
     * 根据预算主表ID查询明细列表
     * 
     * @param budgetMainId 预算主表ID
     * @return 明细列表
     */
    List<BudgetDetail> findByBudgetMainId(Long budgetMainId);

    /**
     * 根据费用类型查询预算明细
     * 
     * @param expenseType 费用类型：1-差旅费，2-办公费，3-业务招待费，4-交通费，5-通讯费，6-培训费，7-其他
     * @return 预算明细列表
     */
    List<BudgetDetail> findByExpenseType(Integer expenseType);

    /**
     * 根据预算主表ID和费用类型查询预算明细
     * 
     * @param budgetMainId 预算主表ID
     * @param expenseType 费用类型
     * @return 预算明细
     */
    Optional<BudgetDetail> findByBudgetMainIdAndExpenseType(Long budgetMainId, Integer expenseType);

    /**
     * 统计预算主表的预算总额
     * 
     * @param budgetMainId 预算主表ID
     * @return 预算总额
     */
    @Query("SELECT COALESCE(SUM(bd.budgetAmount), 0) FROM BudgetDetail bd WHERE bd.budgetMainId = :budgetMainId")
    BigDecimal calculateTotalBudgetAmount(@Param("budgetMainId") Long budgetMainId);

    /**
     * 统计预算主表的已使用金额
     * 
     * @param budgetMainId 预算主表ID
     * @return 已使用金额
     */
    @Query("SELECT COALESCE(SUM(bd.usedAmount), 0) FROM BudgetDetail bd WHERE bd.budgetMainId = :budgetMainId")
    BigDecimal calculateTotalUsedAmount(@Param("budgetMainId") Long budgetMainId);

    /**
     * 统计预算主表的剩余金额
     * 
     * @param budgetMainId 预算主表ID
     * @return 剩余金额
     */
    @Query("SELECT COALESCE(SUM(bd.budgetAmount - bd.usedAmount), 0) FROM BudgetDetail bd WHERE bd.budgetMainId = :budgetMainId")
    BigDecimal calculateTotalRemainingAmount(@Param("budgetMainId") Long budgetMainId);

    /**
     * 更新明细使用金额
     * 
     * @param id 明细ID
     * @param usedAmount 使用金额
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE BudgetDetail bd SET bd.usedAmount = bd.usedAmount + :usedAmount, bd.remainingAmount = bd.remainingAmount - :usedAmount WHERE bd.id = :id")
    void updateUsedAmount(@Param("id") Long id, @Param("usedAmount") BigDecimal usedAmount);

    /**
     * 根据部门ID和年份查询预算明细
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @param expenseType 费用类型
     * @return 预算明细列表
     */
    @Query("SELECT bd FROM BudgetDetail bd " +
           "JOIN BudgetMain bm ON bd.budgetMainId = bm.id " +
           "WHERE bm.deptId = :deptId AND bm.budgetYear = :year AND bd.expenseType = :expenseType AND bm.status >= 2")
    List<BudgetDetail> findByDeptIdAndYearAndExpenseType(@Param("deptId") Long deptId, @Param("year") Integer year, @Param("expenseType") Integer expenseType);

    /**
     * 查询部门在指定年度的总预算（按费用类型分组）
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return Object[] 包含 expenseType 和 totalAmount
     */
    @Query("SELECT bd.expenseType, SUM(bd.budgetAmount) FROM BudgetDetail bd " +
           "JOIN BudgetMain bm ON bd.budgetMainId = bm.id " +
           "WHERE bm.deptId = :deptId AND bm.budgetYear = :year AND bm.status >= 2 " +
           "GROUP BY bd.expenseType")
    List<Object[]> findBudgetSummaryByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 查询部门在指定年度的总已使用金额（按费用类型分组）
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return Object[] 包含 expenseType 和 totalUsedAmount
     */
    @Query("SELECT bd.expenseType, SUM(bd.usedAmount) FROM BudgetDetail bd " +
           "JOIN BudgetMain bm ON bd.budgetMainId = bm.id " +
           "WHERE bm.deptId = :deptId AND bm.budgetYear = :year AND bm.status >= 2 " +
           "GROUP BY bd.expenseType")
    List<Object[]> findUsedAmountSummaryByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);
    
    /**
     * 查找可用的预算，预算金额大于已使用金额且状态有效的预算明细
     * 
     * @param deptId 部门ID
     * @return 可用的预算明细列表
     */
    @Query("SELECT bd FROM BudgetDetail bd " +
           "JOIN BudgetMain bm ON bd.budgetMainId = bm.id " +
           "WHERE bm.deptId = :deptId AND bm.status >= 2 AND bd.budgetAmount > bd.usedAmount")
    List<BudgetDetail> findAvailableBudget(@Param("deptId") Long deptId);
}