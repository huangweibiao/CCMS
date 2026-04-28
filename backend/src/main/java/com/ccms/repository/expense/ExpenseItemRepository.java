package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseItem;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ExpenseItemRepository extends BaseRepository<ExpenseItem, Long> {
    
    List<ExpenseItem> findByExpenseApplyId(Long expenseApplyId);
    
    @Query("SELECT ei FROM ExpenseItem ei WHERE ei.expenseApplyId = :applyId ORDER BY ei.itemNo ASC")
    List<ExpenseItem> findByExpenseApplyIdOrderByItemNo(@Param("applyId") Long applyId);
    
    @Query("SELECT SUM(ei.amount) FROM ExpenseItem ei WHERE ei.expenseApplyId = :applyId")
    BigDecimal sumAmountByExpenseApplyId(@Param("applyId") Long applyId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ExpenseItem ei WHERE ei.expenseApplyId = :applyId")
    void deleteByExpenseApplyId(@Param("applyId") Long applyId);
    
    @Query("SELECT ei FROM ExpenseItem ei WHERE ei.budgetItemId = :budgetItemId")
    List<ExpenseItem> findByBudgetItemId(@Param("budgetItemId") Long budgetItemId);
    
    @Query("SELECT ei FROM ExpenseItem ei WHERE ei.expenseTypeId = :expenseTypeId")
    List<ExpenseItem> findByExpenseTypeId(@Param("expenseTypeId") Long expenseTypeId);
}