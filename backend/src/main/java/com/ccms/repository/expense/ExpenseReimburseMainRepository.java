package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface ExpenseReimburseMainRepository extends BaseRepository<ExpenseReimburseMain, Long> {
    
    Optional<ExpenseReimburseMain> findByReimburseNo(String reimburseNo);
    
    List<ExpenseReimburseMain> findBySubmitUserId(Long submitUserId);
    
    List<ExpenseReimburseMain> findByReimburseDeptId(Long reimburseDeptId);
    
    List<ExpenseReimburseMain> findByStatus(Integer status);
    
    List<ExpenseReimburseMain> findByApprovalStatus(Integer approvalStatus);
    
    List<ExpenseReimburseMain> findBySubmitUserIdAndStatus(Long submitUserId, Integer status);
    
    List<ExpenseReimburseMain> findBySubmitTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT erm FROM ExpenseReimburseMain erm WHERE erm.status = :status AND erm.approvalStatus = :approvalStatus")
    List<ExpenseReimburseMain> findByStatusAndApprovalStatus(@Param("status") Integer status, @Param("approvalStatus") Integer approvalStatus);
    
    @Query("SELECT COUNT(erm) FROM ExpenseReimburseMain erm WHERE erm.submitUserId = :userId AND erm.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);
    
    @Query("SELECT COUNT(erm) FROM ExpenseReimburseMain erm WHERE erm.status = :status AND erm.approvedUserId = :approverId")
    Long countByStatusAndApproverId(@Param("status") Integer status, @Param("approverId") Long approverId);
    
    @Query("SELECT erm FROM ExpenseReimburseMain erm WHERE erm.status = :status AND erm.submitTime BETWEEN :startDate AND :endDate")
    List<ExpenseReimburseMain> findByStatusAndSubmitTimeBetween(@Param("status") Integer status, 
                                                               @Param("startDate") LocalDateTime startDate, 
                                                               @Param("endDate") LocalDateTime endDate);
    
    // 添加缺失的方法
    List<ExpenseReimburseMain> findByReimburseUserId(Long reimburseUserId);
}
