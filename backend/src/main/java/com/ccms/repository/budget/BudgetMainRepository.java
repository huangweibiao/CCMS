package com.ccms.repository.budget;

import com.ccms.entity.budget.BudgetMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 预算主表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface BudgetMainRepository extends JpaRepository<BudgetMain, Long> {

    /**
     * 根据预算编码查询预算
     * 
     * @param budgetNo 预算编码
     * @return 预算信息
     */
    Optional<BudgetMain> findByBudgetNo(String budgetNo);

    /**
     * 根据部门ID查询预算列表
     * 
     * @param deptId 部门ID
     * @return 预算列表
     */
    List<BudgetMain> findByDeptId(Long deptId);

    /**
     * 根据预算周期查询预算列表
     * 
     * @param budgetPeriod 预算周期：MONTH-月度，QUARTER-季度，YEAR-年度
     * @return 预算列表
     */
    List<BudgetMain> findByBudgetPeriod(String budgetPeriod);

    /**
     * 根据预算状态查询预算列表
     * 
     * @param status 预算状态：0-草稿，1-审批中，2-已审批，3-已发布，4-已终止
     * @return 预算列表
     */
    List<BudgetMain> findByStatus(Integer status);

    /**
     * 根据年份查询预算列表
     * 
     * @param year 年份
     * @return 预算列表
     */
    List<BudgetMain> findByBudgetYear(Integer budgetYear);

    /**
     * 根据年份和部门查询预算
     * 
     * @param year 年份
     * @param deptId 部门ID
     * @return 预算信息
     */
    Optional<BudgetMain> findByBudgetYearAndDeptId(Integer budgetYear, Long deptId);

    /**
     * 查询部门在指定年份的所有预算
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 预算列表
     */
    @Query("SELECT b FROM BudgetMain b WHERE b.deptId = :deptId AND b.budgetYear = :year ORDER BY b.budgetCycle ASC")
    List<BudgetMain> findByDeptIdAndBudgetYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 统计部门在指定年份的总预算金额
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 总预算金额
     */
    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM BudgetMain b WHERE b.deptId = :deptId AND b.budgetYear = :year AND b.status >= 2")
    BigDecimal calculateTotalBudgetAmountByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 根据预算编码查询是否已存在相同编码的预算
     * 
     * @param budgetNo 预算编码
     * @return 是否存在
     */
    boolean existsByBudgetNo(String budgetNo);

    /**
     * 查询所有审批中的预算
     * 
     * @return 审批中预算列表
     */
    List<BudgetMain> findByStatusAndApprovalStatus(Integer status, Integer approvalStatus);

    /**
     * 根据创建用户ID查询预算列表
     * 
     * @param createBy 创建用户ID
     * @return 预算列表
     */
    List<BudgetMain> findByCreateBy(Long createBy);

    /**
     * 更新预算状态
     * 
     * @param id 预算ID
     * @param status 新状态
     * @param approvalStatus 新审批状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE BudgetMain b SET b.status = :status, b.approvalStatus = :approvalStatus WHERE b.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);
}