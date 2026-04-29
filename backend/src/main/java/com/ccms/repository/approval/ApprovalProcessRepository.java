package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalProcess;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalProcessRepository extends BaseRepository<ApprovalProcess, Long> {
    
    Optional<ApprovalProcess> findByProcessCode(String processCode);
    
    List<ApprovalProcess> findByBusinessType(String businessType);
    
    List<ApprovalProcess> findByActiveTrue();
    
    @Query("SELECT ap FROM ApprovalProcess ap WHERE ap.businessType = :businessType AND ap.active = true")
    List<ApprovalProcess> findActiveByBusinessType(@Param("businessType") String businessType);
    
    // 添加ApprovalServiceImpl中调用的自定义方法
    @Query("SELECT ap FROM ApprovalProcess ap WHERE ap.status = 1 AND :currentTime > ap.endTime")
    List<ApprovalProcess> findPendingApprovalsByApproverId(@Param("approverId") Long approverId, @Param("currentTime") java.time.LocalDateTime currentTime);
    
    @Query("SELECT ap FROM ApprovalProcess ap WHERE ap.applicantId = :approverId AND ap.status IN (2, 3)")
    List<ApprovalProcess> findProcessedApprovalsByApproverId(@Param("approverId") Long approverId);
    
    // 添加统计方法
    @Query("SELECT COUNT(ap) FROM ApprovalProcess ap WHERE ap.endTime BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(ap) FROM ApprovalProcess ap WHERE ap.status = :status AND ap.endTime BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateRange(@Param("status") Integer status, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(ap) FROM ApprovalProcess ap WHERE ap.active = true AND ap.endTime < :currentTime AND ap.status = 1")
    Long countExpiredProcesses(@Param("currentTime") java.time.LocalDateTime currentTime);
    
    // 添加缺失的方法
    Optional<ApprovalProcess> findByBusinessTypeAndBusinessId(String businessType, Long businessId);
}