package com.ccms.repository;

import com.ccms.entity.expense.ExpenseApplyMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 费用申请单主表数据访问接口
 */
@Repository
public interface ExpenseApplyMainRepository extends JpaRepository<ExpenseApplyMain, Long> {
    
    /**
     * 根据申请单号查询申请单
     */
    ExpenseApplyMain findByApplyNo(String applyNo);
    
    /**
     * 根据申请人ID查询申请单列表
     */
    List<ExpenseApplyMain> findByApplyUserId(Long applyUserId);
    
    /**
     * 根据审批状态查询申请单列表
     */
    List<ExpenseApplyMain> findByStatus(Integer status);
    
    /**
     * 根据部门和状态查询申请单
     */
    @Query("SELECT e FROM ExpenseApplyMain e WHERE e.applyDeptId = :deptId AND e.status = :status")
    List<ExpenseApplyMain> findByDeptIdAndStatus(@Param("deptId") Long deptId, @Param("status") Integer status);
}