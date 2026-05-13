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
    
    // 使用现有的applyUserId字段替代userId查询
    @Query("SELECT ea FROM ExpenseApply ea WHERE ea.applyUserId = :userId")
    List<ExpenseApply> findByUserId(@Param("userId") Long userId);
    
    // 添加缺失的统计方法
    @Query("SELECT COUNT(ea) FROM ExpenseApply ea WHERE ea.status = :status AND ea.currentApproverId = :approverId")
    Long countByStatusAndApproverId(@Param("status") Integer status, @Param("approverId") Long approverId);
    
    @Query("SELECT ea FROM ExpenseApply ea WHERE ea.status = :status AND ea.submitTime BETWEEN :startDate AND :endDate")
    List<ExpenseApply> findByStatusAndDateRange(@Param("status") Integer status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(ea) FROM ExpenseApply ea WHERE ea.deptId = :deptId AND ea.submitTime BETWEEN :startDate AND :endDate")
    Long countByDeptIdAndDateRange(@Param("deptId") Long deptId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT SUM(ea.totalAmount) FROM ExpenseApply ea WHERE ea.deptId = :deptId AND ea.submitTime BETWEEN :startDate AND :endDate")
    java.math.BigDecimal sumAmountByDeptIdAndDateRange(@Param("deptId") Long deptId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(ea) FROM ExpenseApply ea WHERE ea.deptId = :deptId AND ea.status = :status AND ea.submitTime BETWEEN :startDate AND :endDate")
    Long countByDeptIdAndStatusAndDateRange(@Param("deptId") Long deptId, @Param("status") Integer status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    // 添加支持applicantId的统计方法
    @Query("SELECT COUNT(ea) FROM ExpenseApply ea WHERE ea.applyUserId = :applyUserId AND ea.deptId = :deptId AND YEAR(ea.submitTime) = :year")
    Long countByApplicantIdAndDeptIdAndYear(@Param("applyUserId") Long applyUserId, @Param("deptId") Long deptId, @Param("year") Long year);
    
    @Query("SELECT SUM(ea.totalAmount) FROM ExpenseApply ea WHERE ea.applyUserId = :applyUserId AND ea.deptId = :deptId AND YEAR(ea.submitTime) = :year")
    java.math.BigDecimal sumAmountByApplicantIdAndDeptIdAndYear(@Param("applyUserId") Long applyUserId, @Param("deptId") Long deptId, @Param("year") Long year);
    
    @Query("SELECT COUNT(ea) FROM ExpenseApply ea WHERE ea.applyUserId = :applyUserId AND ea.deptId = :deptId AND ea.status = :status AND YEAR(ea.submitTime) = :year")
    Long countByApplicantIdAndDeptIdAndStatusAndYear(@Param("applyUserId") Long applyUserId, @Param("deptId") Long deptId, @Param("status") Integer status, @Param("year") Long year);
}