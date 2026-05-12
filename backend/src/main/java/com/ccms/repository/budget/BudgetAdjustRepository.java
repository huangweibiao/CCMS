package com.ccms.repository.budget;

import com.ccms.entity.budget.BudgetAdjust;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算调整记录表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface BudgetAdjustRepository extends JpaRepository<BudgetAdjust, Long> {

    /**
     * 根据预算ID查询调整记录
     * 
     * @param budgetId 预算ID
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByBudgetId(Long budgetId);

    /**
     * 根据调整类型查询调整记录
     * 
     * @param adjustType 调整类型：1-追加，2-削减，3-调整
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByAdjustType(Integer adjustType);

    /**
     * 根据审批状态查询调整记录
     * 
     * @param approvalStatus 审批状态：0-待提交 1-审批中 2-已通过 3-已驳回 4-已撤销
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByApprovalStatus(Integer approvalStatus);



    /**
     * 统计预算主表的总调整金额
     * 
     * @param budgetId 预算ID
     * @return 总调整金额
     */
    @Query("SELECT COALESCE(SUM(ba.adjustAmount), 0) FROM BudgetAdjust ba WHERE ba.budgetId = :budgetId AND ba.approvalStatus = 2")
    BigDecimal calculateTotalAdjustAmount(@Param("budgetId") Long budgetId);

    /**
     * 统计预算主表的追加金额
     * 
     * @param budgetId 预算ID
     * @return 追加金额
     */
    @Query("SELECT COALESCE(SUM(ba.adjustAmount), 0) FROM BudgetAdjust ba WHERE ba.budgetId = :budgetId AND ba.adjustType = 1 AND ba.approvalStatus = 2")
    BigDecimal calculateTotalAppendAmount(@Param("budgetId") Long budgetId);

    /**
     * 统计预算主表的削减金额
     * 
     * @param budgetId 预算ID
     * @return 削减金额
     */
    @Query("SELECT COALESCE(SUM(ba.adjustAmount), 0) FROM BudgetAdjust ba WHERE ba.budgetId = :budgetId AND ba.adjustType = 2 AND ba.approvalStatus = 2")
    BigDecimal calculateTotalCutAmount(@Param("budgetId") Long budgetId);

    /**
     * 根据创建人ID查询调整记录
     * 
     * @param createBy 创建人ID
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByCreateBy(Long createBy);

    /**
     * 更新调整记录状态
     * 
     * @param id 调整记录ID
     * @param approvalStatus 新审批状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE BudgetAdjust ba SET ba.approvalStatus = :approvalStatus, ba.approvalTime = CURRENT_TIMESTAMP WHERE ba.id = :id")
    void updateStatus(@Param("id") Long id, @Param("approvalStatus") Integer approvalStatus);

    /**
     * 查询最近一年内的预算调整记录
     * 
     * @param budgetId 预算ID
     * @return 调整记录列表
     */
    @Query("SELECT ba FROM BudgetAdjust ba WHERE ba.budgetId = :budgetId AND ba.createTime >= FUNCTION('DATE_SUB', CURRENT_DATE, 365, 'DAY')")
    List<BudgetAdjust> findRecentAdjustments(@Param("budgetId") Long budgetId);
}