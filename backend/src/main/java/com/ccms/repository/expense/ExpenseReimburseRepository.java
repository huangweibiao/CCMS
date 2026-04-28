package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseReimburseRepository extends BaseRepository<ExpenseReimburse, Long> {
    
    Optional<ExpenseReimburse> findByReimburseNo(String reimburseNo);
    
    List<ExpenseReimburse> findByApplyUserId(Long applyUserId);
    
    List<ExpenseReimburse> findByDeptId(Long deptId);
    
    List<ExpenseReimburse> findByStatus(Integer status);
    
    List<ExpenseReimburse> findByApprovalStatus(Integer approvalStatus);
    
    List<ExpenseReimburse> findByApplyUserIdAndStatus(Long applyUserId, Integer status);
    
    List<ExpenseReimburse> findByReimburseDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT er FROM ExpenseReimburse er WHERE er.status = :status AND er.approvalStatus = :approvalStatus")
    List<ExpenseReimburse> findByStatusAndApprovalStatus(@Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);
    
    @Query("SELECT COUNT(er) FROM ExpenseReimburse er WHERE er.applyUserId = :userId AND er.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
}