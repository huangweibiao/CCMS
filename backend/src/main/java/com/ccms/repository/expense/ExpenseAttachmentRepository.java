package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseAttachment;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ExpenseAttachmentRepository extends BaseRepository<ExpenseAttachment, Long> {
    
    List<ExpenseAttachment> findByExpenseApplyId(Long expenseApplyId);
    
    List<ExpenseAttachment> findByExpenseApplyIdAndDeletedFalse(Long expenseApplyId);
    
    @Modifying
    @Transactional
    @Query("UPDATE ExpenseAttachment ea SET ea.deleted = true WHERE ea.expenseApplyId = :applyId")
    void softDeleteByExpenseApplyId(@Param("applyId") Long applyId);
    
    @Query("SELECT COUNT(ea) FROM ExpenseAttachment ea WHERE ea.expenseApplyId = :applyId AND ea.deleted = false")
    Long countActiveAttachmentsByExpenseApplyId(@Param("applyId") Long applyId);
}