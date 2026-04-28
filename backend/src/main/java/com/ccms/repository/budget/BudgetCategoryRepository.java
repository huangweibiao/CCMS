package com.ccms.repository.budget;

import com.ccms.entity.budget.BudgetCategory;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetCategoryRepository extends BaseRepository<BudgetCategory, Long> {
    
    BudgetCategory findByCategoryCode(String categoryCode);
    
    List<BudgetCategory> findByParentId(Long parentId);
    
    List<BudgetCategory> findByEnabledTrue();
    
    List<BudgetCategory> findByCategoryLevel(Integer level);
    
    @Query("SELECT bc FROM BudgetCategory bc WHERE bc.parentId IS NULL AND bc.enabled = true ORDER BY bc.sortOrder ASC")
    List<BudgetCategory> findRootCategories();
    
    @Query("SELECT COUNT(bc) FROM BudgetCategory bc WHERE bc.parentId = :parentId")
    Long countByParentId(@Param("parentId") Long parentId);
}