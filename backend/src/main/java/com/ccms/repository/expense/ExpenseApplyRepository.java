package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseApplyRepository extends BaseRepository<ExpenseApply, Long> {
    
    Optional<ExpenseApply> findByApplyNo(String applyNo);
    
    List<ExpenseApply> findByApplyUserId(Long applyUserId);
    
    List<ExpenseApply> findByDeptId(Long deptId);
    
    List<ExpenseApply> findByStatus(Integer status);
    
    List<ExpenseApply> findByApprovalStatus(Integer approvalStatus);
    
    List<ExpenseApply> findByApplyUserIdAndStatus(Long applyUserId, Integer status);
    
    List<ExpenseApply> findByApplyDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ea FROM ExpenseApply ea WHERE ea.status = :status AND ea.approvalStatus = :approvalStatus")
    List<ExpenseApply> findByStatusAndApprovalStatus(@Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);
    
    @Query("SELECT COUNT(ea) FROM ExpenseApply ea WHERE ea.applyUserId = :userId AND ea.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
}