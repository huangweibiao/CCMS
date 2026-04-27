package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseApplyMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 费用申请单主表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface ExpenseApplyMainRepository extends JpaRepository<ExpenseApplyMain, Long> {

    /**
     * 根据申请单号查询申请单
     * 
     * @param applyNumber 申请单号
     * @return 申请单信息
     */
    Optional<ExpenseApplyMain> findByApplyNumber(String applyNumber);

    /**
     * 根据申请人ID查询申请单列表
     * 
     * @param applyUserId 申请人ID
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByApplyUserId(Long applyUserId);

    /**
     * 根据部门ID查询申请单列表
     * 
     * @param deptId 部门ID
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByDeptId(Long deptId);

    /**
     * 根据申请状态查询申请单列表
     * 
     * @param status 申请状态：0-草稿，1-提交待审核，2-审批中，3-已审批，4-已驳回，5-已核销，6-已撤销
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByStatus(Integer status);

    /**
     * 根据审批状态查询申请单列表
     * 
     * @param approvalStatus 审批状态：0-待审批，1-审批中，2-已审批通过，3-已驳回
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByApprovalStatus(Integer approvalStatus);

    /**
     * 查询待审批的申请单
     * 
     * @return 待审批申请单列表
     */
    List<ExpenseApplyMain> findByStatusAndApprovalStatus(Integer status, Integer approvalStatus);

    /**
     * 统计部门在指定年度的申请总额
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 申请总额
     */
    @Query("SELECT COALESCE(SUM(eam.totalAmount), 0) FROM ExpenseApplyMain eam WHERE eam.deptId = :deptId AND YEAR(eam.applyDate) = :year AND eam.status >= 2")
    BigDecimal calculateTotalApplyAmountByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 统计部门在指定年度已审批通过的申请总额
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 已审批通过总额
     */
    @Query("SELECT COALESCE(SUM(eam.totalAmount), 0) FROM ExpenseApplyMain eam WHERE eam.deptId = :deptId AND YEAR(eam.applyDate) = :year AND eam.status = 3")
    BigDecimal calculateApprovedAmountByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 检查申请单号是否存在
     * 
     * @param applyNumber 申请单号
     * @return 是否存在
     */
    boolean existsByApplyNumber(String applyNumber);

    /**
     * 根据相关申请单号查询申请单
     * 
     * @param relatedApplyNumber 相关申请单号
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByRelatedApplyNumber(String relatedApplyNumber);

    /**
     * 查询指定日期范围内的申请单
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByApplyDateBetween(java.time.LocalDate startDate, java.time.LocalDate endDate);

    /**
     * 查询费用类型申请单数量（按月份统计）
     * 
     * @param year 年份
     * @param month 月份
     * @return Object[] 包含 count 和 status
     */
    @Query("SELECT eam.status, COUNT(eam) FROM ExpenseApplyMain eam WHERE YEAR(eam.applyDate) = :year AND MONTH(eam.applyDate) = :month GROUP BY eam.status")
    List<Object[]> findApplyCountByMonth(@Param("year") Integer year, @Param("month") Integer month);

    /**
     * 更新申请单审批状态和状态
     * 
     * @param id 申请单ID
     * @param status 新状态
     * @param approvalStatus 新审批状态
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ExpenseApplyMain eam SET eam.status = :status, eam.approvalStatus = :approvalStatus WHERE eam.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);
}