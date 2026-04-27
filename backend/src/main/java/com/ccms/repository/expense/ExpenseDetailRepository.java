package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 费用明细表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface ExpenseDetailRepository extends JpaRepository<ExpenseDetail, Long> {

    /**
     * 根据申请单主表ID查询费用明细
     * 
     * @param applyMainId 申请单主表ID
     * @return 费用明细列表
     */
    List<ExpenseDetail> findByApplyMainId(Long applyMainId);

    /**
     * 根据费用类型查询费用明细
     * 
     * @param expenseType 费用类型：1-差旅费，2-办公费，3-业务招待费，4-交通费，5-通讯费，6-培训费，7-其他
     * @return 费用明细列表
     */
    List<ExpenseDetail> findByExpenseType(Integer expenseType);

    /**
     * 根据核销状态查询费用明细
     * 
     * @param settleStatus 核销状态：0-未核销，1-已核销，2-部分核销
     * @return 费用明细列表
     */
    List<ExpenseDetail> findBySettleStatus(Integer settleStatus);

    /**
     * 统计申请单的总额
     * 
     * @param applyMainId 申请单主表ID
     * @return 总额
     */
    @Query("SELECT COALESCE(SUM(ed.amount), 0) FROM ExpenseDetail ed WHERE ed.applyMainId = :applyMainId")
    BigDecimal calculateTotalAmount(@Param("applyMainId") Long applyMainId);

    /**
     * 统计申请单的已核销总额
     * 
     * @param applyMainId 申请单主表ID
     * @return 已核销总额
     */
    @Query("SELECT COALESCE(SUM(ed.settledAmount), 0) FROM ExpenseDetail ed WHERE ed.applyMainId = :applyMainId")
    BigDecimal calculateTotalSettledAmount(@Param("applyMainId") Long applyMainId);

    /**
     * 统计申请单的待核销总额
     * 
     * @param applyMainId 申请单主表ID
     * @return 待核销总额
     */
    @Query("SELECT COALESCE(SUM(ed.amount - ed.settledAmount), 0) FROM ExpenseDetail ed WHERE ed.applyMainId = :applyMainId")
    BigDecimal calculateTotalPendingAmount(@Param("applyMainId") Long applyMainId);

    /**
     * 更新费用明细的核销金额
     * 
     * @param id 明细ID
     * @param settledAmount 核销金额
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ExpenseDetail ed SET ed.settledAmount = ed.settledAmount + :settledAmount, ed.settleStatus = CASE WHEN (ed.amount - ed.settledAmount - :settledAmount) <= 0 THEN 1 WHEN ed.settledAmount + :settledAmount > 0 THEN 2 ELSE 0 END WHERE ed.id = :id")
    void updateSettledAmount(@Param("id") Long id, @Param("settledAmount") BigDecimal settledAmount);

    /**
     * 根据申请单主表ID和费用类型查询费用明细
     * 
     * @param applyMainId 申请单主表ID
     * @param expenseType 费用类型
     * @return 费用明细
     */
    List<ExpenseDetail> findByApplyMainIdAndExpenseType(Long applyMainId, Integer expenseType);

    /**
     * 查询未核销的费用明细
     * 
     * @return 未核销明细列表
     */
    List<ExpenseDetail> findBySettleStatusIn(List<Integer> settleStatus);

    /**
     * 统计部门在指定年度的费用明细（按费用类型分组）
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return Object[] 包含 expenseType, totalAmount, totalSettledAmount
     */
    @Query("SELECT ed.expenseType, SUM(ed.amount), SUM(ed.settledAmount) FROM ExpenseDetail ed " +
           "JOIN ExpenseApplyMain eam ON ed.applyMainId = eam.id " +
           "WHERE eam.deptId = :deptId AND YEAR(eam.applyDate) = :year " +
           "GROUP BY ed.expenseType")
    List<Object[]> findExpenseSummaryByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);
}