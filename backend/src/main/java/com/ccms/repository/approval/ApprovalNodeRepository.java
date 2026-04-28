package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalNode;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalNodeRepository extends BaseRepository<ApprovalNode, Long> {
    
    List<ApprovalNode> findByProcessId(Long processId);
    
    List<ApprovalNode> findByProcessIdOrderByStepNumber(Long processId);
    
    ApprovalNode findByProcessIdAndNodeCode(Long processId, String nodeCode);
    
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.nodeType = :nodeType")
    List<ApprovalNode> findByProcessIdAndNodeType(@Param("processId") Long processId, @Param("nodeType") String nodeType);
    
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.processId = :processId")
    Long countByProcessId(@Param("processId") Long processId);
    
    // 添加ApprovalServiceImpl中调用的自定义方法
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.stepNumber = (SELECT ap.currentNode FROM ApprovalProcess ap WHERE ap.id = :processId)")
    ApprovalNode findCurrentNodeByProcessId(@Param("processId") Long processId);
    
    // 添加统计方法
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.processTime BETWEEN :startDate AND :endDate")
    Long countByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = 0 AND an.processTime BETWEEN :startDate AND :endDate")
    Long countPendingByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = 1 AND an.processTime BETWEEN :startDate AND :endDate")
    Long countApprovedByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = 2 AND an.processTime BETWEEN :startDate AND :endDate")
    Long countRejectedByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT an FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status IN (1, 2) AND an.processTime BETWEEN :startDate AND :endDate")
    List<ApprovalNode> findProcessedByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);
    
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.approverId = :approverId")
    ApprovalNode findByProcessIdAndApproverId(@Param("processId") Long processId, @Param("approverId") Long approverId);
}