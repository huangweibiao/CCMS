package com.ccms.repository.expense;

import com.ccms.entity.expense.ReimburseItem;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ReimburseItemRepository extends BaseRepository<ReimburseItem, Long> {
    
    List<ReimburseItem> findByReimburseMainId(Long reimburseMainId);
    
    @Query("SELECT ri FROM ReimburseItem ri WHERE ri.reimburseMainId = :mainId ORDER BY ri.itemNo ASC")
    List<ReimburseItem> findByReimburseMainIdOrderByItemNo(@Param("mainId") Long mainId);
    
    @Query("SELECT SUM(ri.amount) FROM ReimburseItem ri WHERE ri.reimburseMainId = :mainId")
    BigDecimal sumAmountByReimburseMainId(@Param("mainId") Long mainId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ReimburseItem ri WHERE ri.reimburseMainId = :mainId")
    void deleteByReimburseMainId(@Param("mainId") Long mainId);
    
    @Query("SELECT ri FROM ReimburseItem ri WHERE ri.budgetItemId = :budgetItemId")
    List<ReimburseItem> findByBudgetItemId(@Param("budgetItemId") Long budgetItemId);
    
    @Query("SELECT ri FROM ReimburseItem ri WHERE ri.expenseTypeId = :expenseTypeId")
    List<ReimburseItem> findByExpenseTypeId(@Param("expenseTypeId") Long expenseTypeId);
    
    List<ReimburseItem> findByExpenseReimburseId(Long expenseReimburseId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM ReimburseItem ri WHERE ri.expenseReimburseId = :expenseReimburseId")
    void deleteByExpenseReimburseId(@Param("expenseReimburseId") Long expenseReimburseId);
    
    @Query("SELECT ri FROM ReimburseItem ri WHERE ri.expenseReimburseId = :expenseReimburseId ORDER BY ri.itemNo ASC")
    List<ReimburseItem> findByExpenseReimburseIdOrderByItemNo(@Param("expenseReimburseId") Long expenseReimburseId);
}