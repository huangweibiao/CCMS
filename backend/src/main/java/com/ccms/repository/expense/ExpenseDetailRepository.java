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
     * 根据费用申请单主表ID查询费用明细
     * 
     * @param expenseMainId 费用申请单主表ID
     * @return 费用明细列表
     */
    List<ExpenseDetail> findByExpenseMainId(Long expenseMainId);

    /**
     * 根据费用类型ID查询费用明细
     * 
     * @param expenseTypeId 费用类型ID
     * @return 费用明细列表
     */
    List<ExpenseDetail> findByExpenseTypeId(Long expenseTypeId);

    /**
     * 根据审核状态查询费用明细
     * 
     * @param approved 审核状态
     * @return 费用明细列表
     */
    List<ExpenseDetail> findByApproved(Boolean approved);

    /**
     * 统计申请单的总额
     * 
     * @param expenseMainId 费用申请单主表ID
     * @return 总额
     */
    @Query("SELECT COALESCE(SUM(ed.amount), 0) FROM ExpenseDetail ed WHERE ed.expenseMainId = :expenseMainId")
    BigDecimal calculateTotalAmount(@Param("expenseMainId") Long expenseMainId);



    /**
     * 根据费用申请单主表ID和费用类型ID查询费用明细
     * 
     * @param expenseMainId 费用申请单主表ID
     * @param expenseTypeId 费用类型ID
     * @return 费用明细
     */
    List<ExpenseDetail> findByExpenseMainIdAndExpenseTypeId(Long expenseMainId, Long expenseTypeId);


}