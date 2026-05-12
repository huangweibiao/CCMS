package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApproverTypeEnum;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalNodeRepository extends BaseRepository<ApprovalNode, Long> {
    
    List<ApprovalNode> findByFlowConfig_Id(Long flowConfigId);
    
    List<ApprovalNode> findByFlowConfig_IdOrderByStepNumber(Long flowConfigId);
    
    Optional<ApprovalNode> findByFlowConfig_IdAndNodeCode(Long flowConfigId, String nodeCode);
    
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfig.id = :flowConfigId AND an.approverType = :approverType")
    List<ApprovalNode> findByFlowConfigIdAndApproverType(@Param("flowConfigId") Long flowConfigId, @Param("approverType") ApproverTypeEnum approverType);
    
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.flowConfig.id = :flowConfigId")
    Long countByFlowConfigId(@Param("flowConfigId") Long flowConfigId);
    
    // 根据当前步骤查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfig.id = :flowConfigId AND an.stepNumber = :stepNumber")
    Optional<ApprovalNode> findByFlowConfigIdAndStepNumber(@Param("flowConfigId") Long flowConfigId, @Param("stepNumber") Integer stepNumber);
    
    // 查找下一个节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfig.id = :flowConfigId AND an.stepNumber = (SELECT an2.stepNumber + 1 FROM ApprovalNode an2 WHERE an2.flowConfig.id = :flowConfigId ORDER BY an2.stepNumber DESC LIMIT 1)")
    Optional<ApprovalNode> findNextNodeByFlowConfigId(@Param("flowConfigId") Long flowConfigId);

    // 根据步骤号排序获取所有节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfig.id = :flowConfigId ORDER BY an.stepNumber ASC")
    List<ApprovalNode> findByFlowConfigIdOrderByStepNumberAsc(@Param("flowConfigId") Long flowConfigId);
    
    // 查找第一个节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfig.id = :flowConfigId ORDER BY an.stepNumber ASC LIMIT 1")
    Optional<ApprovalNode> findFirstNodeByFlowConfigId(@Param("flowConfigId") Long flowConfigId);
    
    // 根据审批人ID和状态查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = :status")
    List<ApprovalNode> findByApproverIdAndStatus(@Param("approverId") Long approverId, @Param("status") Integer status);

    // 根据流程ID和节点级别查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.nodeLevel = :nodeLevel")
    List<ApprovalNode> findByProcessIdAndNodeLevel(@Param("processId") Long processId, @Param("nodeLevel") Integer nodeLevel);

    // 根据流程ID查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId")
    List<ApprovalNode> findByProcessId(@Param("processId") Long processId);

    // 根据流程ID查找当前节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.status = 0 ORDER BY an.stepNumber LIMIT 1")
    ApprovalNode findCurrentNodeByProcessId(@Param("processId") Long processId);

    // 根据流程ID和节点级别查找节点(重载方法)
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.nodeLevel = :nodeLevel")
    List<ApprovalNode> findByProcessIdAndNodeLevel(@Param("processId") Long processId, @Param("nodeLevel") int nodeLevel);

    // 根据审批人ID和时间范围统计节点
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.processTime BETWEEN :startDate AND :endDate")
    Long countByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    // 根据审批人ID和时间范围统计待处理节点
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = 0 AND an.processTime BETWEEN :startDate AND :endDate")
    Long countPendingByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    // 根据审批人ID和时间范围统计已批准节点
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = 1 AND an.processTime BETWEEN :startDate AND :endDate")
    Long countApprovedByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    // 根据审批人ID和时间范围统计已拒绝节点
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = 2 AND an.processTime BETWEEN :startDate AND :endDate")
    Long countRejectedByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    // 根据审批人ID和时间范围查找已处理的节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status IN (1, 2) AND an.processTime BETWEEN :startDate AND :endDate")
    List<ApprovalNode> findProcessedByApproverIdAndDateRange(@Param("approverId") Long approverId, @Param("startDate") java.time.LocalDateTime startDate, @Param("endDate") java.time.LocalDateTime endDate);

    // 根据流程ID和审批人ID查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.processId = :processId AND an.approverId = :approverId")
    List<ApprovalNode> findByProcessIdAndApproverId(@Param("processId") Long processId, @Param("approverId") Long approverId);

    // 这里删除了instanceId相关的方法，因为实体中没有instanceId字段
    // 暂时注释掉，后续根据实际需求添加
}