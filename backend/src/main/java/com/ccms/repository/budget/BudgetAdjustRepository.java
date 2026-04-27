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
     * 根据预算主表ID查询调整记录
     * 
     * @param budgetMainId 预算主表ID
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByBudgetMainId(Long budgetMainId);

    /**
     * 根据调整类型查询调整记录
     * 
     * @param adjustType 调整类型：1-追加，2-削减，3-调整
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByAdjustType(Integer adjustType);

    /**
     * 根据调整状态查询调整记录
     * 
     * @param status 调整状态：0-草稿，1-审批中，2-已审批，3-已生效，4-已撤销
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByStatus(Integer status);

    /**
     * 查询待审批的预算调整记录
     * 
     * @return 待审批记录列表
     */
    List<BudgetAdjust> findByStatusAndApprovalStatus(Integer status, Integer approvalStatus);

    /**
     * 统计预算主表的总调整金额
     * 
     * @param budgetMainId 预算主表ID
     * @return 总调整金额
     */
    @Query("SELECT COALESCE(SUM(ba.adjustAmount), 0) FROM BudgetAdjust ba WHERE ba.budgetMainId = :budgetMainId AND ba.status = 3")
    BigDecimal calculateTotalAdjustAmount(@Param("budgetMainId") Long budgetMainId);

    /**
     * 统计预算主表的追加金额
     * 
     * @param budgetMainId 预算主表ID
     * @return 追加金额
     */
    @Query("SELECT COALESCE(SUM(ba.adjustAmount), 0) FROM BudgetAdjust ba WHERE ba.budgetMainId = :budgetMainId AND ba.adjustType = 1 AND ba.status = 3")
    BigDecimal calculateTotalAppendAmount(@Param("budgetMainId") Long budgetMainId);

    /**
     * 统计预算主表的削减金额
     * 
     * @param budgetMainId 预算主表ID
     * @return 削减金额
     */
    @Query("SELECT COALESCE(SUM(ba.adjustAmount), 0) FROM BudgetAdjust ba WHERE ba.budgetMainId = :budgetMainId AND ba.adjustType = 2 AND ba.status = 3")
    BigDecimal calculateTotalCutAmount(@Param("budgetMainId") Long budgetMainId);

    /**
     * 根据创建人ID查询调整记录
     * 
     * @param createUserId 创建人ID
     * @return 调整记录列表
     */
    List<BudgetAdjust> findByCreateUserId(Long createUserId);

    /**
     * 更新调整记录状态
     * 
     * @param id 调整记录ID
     * @param status 新状态
     * @param approvalStatus 新审批状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE BudgetAdjust ba SET ba.status = :status, ba.approvalStatus = :approvalStatus WHERE ba.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);

    /**
     * 查询最近一年内的预算调整记录
     * 
     * @param budgetMainId 预算主表ID
     * @return 调整记录列表
     */
    @Query("SELECT ba FROM BudgetAdjust ba WHERE ba.budgetMainId = :budgetMainId AND ba.createTime >= CURRENT_DATE - 365")
    List<BudgetAdjust> findRecentAdjustments(@Param("budgetMainId") Long budgetMainId);
}