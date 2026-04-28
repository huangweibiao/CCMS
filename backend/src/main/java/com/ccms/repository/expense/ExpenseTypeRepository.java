package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseType;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseTypeRepository extends BaseRepository<ExpenseType, Long> {
    
    ExpenseType findByTypeCode(String typeCode);
    
    List<ExpenseType> findByParentId(Long parentId);
    
    List<ExpenseType> findByEnabledTrue();
    
    List<ExpenseType> findByNeedApprovalTrue();
    
    List<ExpenseType> findByTypeLevel(Integer level);
    
    @Query("SELECT et FROM ExpenseType et WHERE et.parentId IS NULL AND et.enabled = true ORDER BY et.sortOrder ASC")
    List<ExpenseType> findRootTypes();
    
    @Query("SELECT COUNT(et) FROM ExpenseType et WHERE et.parentId = :parentId")
    Long countByParentId(@Param("parentId") Long parentId);
    
    @Query("SELECT et FROM ExpenseType et WHERE et.budgetCategoryId = :categoryId AND et.enabled = true")
    List<ExpenseType> findByBudgetCategoryId(@Param("categoryId") Long categoryId);
}