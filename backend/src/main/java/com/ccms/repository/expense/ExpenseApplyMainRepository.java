package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseApplyMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @param applyNo 申请单号
     * @return 申请单信息
     */
    Optional<ExpenseApplyMain> findByApplyNo(String applyNo);

    /**
     * 根据申请人ID查询申请单列表
     * 
     * @param applyUserId 申请人ID
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByApplyUserId(Long applyUserId);

    /**
     * 根据申请部门ID查询申请单列表
     * 
     * @param applyDeptId 申请部门ID
     * @return 申请单列表
     */
    List<ExpenseApplyMain> findByApplyDeptId(Long applyDeptId);

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
    @Query("SELECT COALESCE(SUM(eam.totalAmount), 0) FROM ExpenseApplyMain eam WHERE eam.applyDeptId = :deptId AND YEAR(eam.createTime) = :year AND eam.status >= 2")
    BigDecimal calculateTotalApplyAmountByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 统计部门在指定年度已审批通过的申请总额
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 已审批通过总额
     */
    @Query("SELECT COALESCE(SUM(eam.totalAmount), 0) FROM ExpenseApplyMain eam WHERE eam.applyDeptId = :deptId AND YEAR(eam.createTime) = :year AND eam.status = 3")
    BigDecimal calculateApprovedAmountByDeptAndYear(@Param("deptId") Long deptId, @Param("year") Integer year);

    /**
     * 检查申请单号是否存在
     * 
     * @param applyNo 申请单号
     * @return 是否存在
     */
    boolean existsByApplyNo(String applyNo);





    /**
     * 查询费用类型申请单数量（按月份统计）
     * 
     * @param year 年份
     * @param month 月份
     * @return Object[] 包含 count 和 status
     */
    @Query("SELECT eam.status, COUNT(eam) FROM ExpenseApplyMain eam WHERE YEAR(eam.createTime) = :year AND MONTH(eam.createTime) = :month GROUP BY eam.status")
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

    // ==================== 为Controller提供的方法 ====================
    
    /**
     * 根据申请单号查询（原applyNumber兼容性方法）
     */
    default Optional<ExpenseApplyMain> findByApplyNumber(String applyNumber) {
        return findByApplyNo(applyNumber);
    }
    
    /**
     * 根据申请人ID和状态查询
     */
    default List<ExpenseApplyMain> findByApplyUserIdAndStatus(Long applyUserId, Integer status) {
        return findByApplyUserId(applyUserId).stream()
                .filter(e -> status.equals(e.getStatus()))
                .collect(Collectors.toList());
    }
    
    /**
     * 根据申请人ID查询并按创建时间倒序
     */
    default List<ExpenseApplyMain> findByApplyUserIdOrderByCreateTimeDesc(Long applyUserId) {
        List<ExpenseApplyMain> list = new ArrayList<>(findByApplyUserId(applyUserId));
        list.sort((e1, e2) -> {
            if (e1.getCreateTime() == null) return 1;
            if (e2.getCreateTime() == null) return -1;
            return e2.getCreateTime().compareTo(e1.getCreateTime());
        });
        return list;
    }
    
    /**
     * 根据部门ID查询并按创建时间倒序
     */
    default List<ExpenseApplyMain> findByApplyDeptIdOrderByCreateTimeDesc(Long deptId) {
        List<ExpenseApplyMain> list = new ArrayList<>(findByApplyDeptId(deptId));
        list.sort((e1, e2) -> {
            if (e1.getCreateTime() == null) return 1;
            if (e2.getCreateTime() == null) return -1;
            return e2.getCreateTime().compareTo(e1.getCreateTime());
        });
        return list;
    }
    
    /**
     * 统计申请人申请单数量
     */
    default long countByApplyUserId(Long applyUserId) {
        return findByApplyUserId(applyUserId).size();
    }
    
    /**
     * 统计申请人指定状态的申请单数量
     */
    default long countByApplyUserIdAndStatus(Long applyUserId, Integer status) {
        return findByApplyUserIdAndStatus(applyUserId, status).size();
    }
}
