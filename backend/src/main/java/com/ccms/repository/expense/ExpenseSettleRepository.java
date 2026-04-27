package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseSettle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用核销记录表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface ExpenseSettleRepository extends JpaRepository<ExpenseSettle, Long> {

    /**
     * 根据申请单主表ID查询核销记录
     * 
     * @param applyMainId 申请单主表ID
     * @return 核销记录列表
     */
    List<ExpenseSettle> findByApplyMainId(Long applyMainId);

    /**
     * 根据费用明细ID查询核销记录
     * 
     * @param expenseDetailId 费用明细ID
     * @return 核销记录列表
     */
    List<ExpenseSettle> findByExpenseDetailId(Long expenseDetailId);

    /**
     * 根据核销状态查询核销记录
     * 
     * @param status 核销状态：0-草稿，1-已提交，2-已确认，3-已撤销
     * @return 核销记录列表
     */
    List<ExpenseSettle> findByStatus(Integer status);

    /**
     * 根据核销人ID查询核销记录
     * 
     * @param settleUserId 核销人ID
     * @return 核销记录列表
     */
    List<ExpenseSettle> findBySettleUserId(Long settleUserId);

    /**
     * 统计申请单的核销总额
     * 
     * @param applyMainId 申请单主表ID
     * @return 核销总额
     */
    @Query("SELECT COALESCE(SUM(es.settleAmount), 0) FROM ExpenseSettle es WHERE es.applyMainId = :applyMainId AND es.status >= 2")
    BigDecimal calculateTotalSettleAmount(@Param("applyMainId") Long applyMainId);

    /**
     * 统计费用明细的核销总额
     * 
     * @param expenseDetailId 费用明细ID
     * @return 核销总额
     */
    @Query("SELECT COALESCE(SUM(es.settleAmount), 0) FROM ExpenseSettle es WHERE es.expenseDetailId = :expenseDetailId AND es.status >= 2")
    BigDecimal calculateTotalSettleAmountByDetail(@Param("expenseDetailId") Long expenseDetailId);

    /**
     * 查询指定日期范围内的核销记录
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 核销记录列表
     */
    List<ExpenseSettle> findBySettleDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * 更新核销记录状态
     * 
     * @param id 核销记录ID
     * @param status 新状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ExpenseSettle es SET es.status = :status WHERE es.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 统计部门在指定年度的核销总额
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 核销总额
     */
    @Query("SELECT COALESCE(SUM(es.settleAmount), 0) FROM ExpenseSettle es " +
           "JOIN ExpenseApplyMain eam ON es.applyMainId = eam.id " +
           "WHERE eam.deptId = :deptId AND YEAR(es.settleDate) = :year AND es.status >= 2")
    BigDecimal calculateTotalSettleAmountByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);
}