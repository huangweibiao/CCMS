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
    
    List<ApprovalNode> findByFlowConfigId(Long flowConfigId);
    
    List<ApprovalNode> findByFlowConfigIdOrderByStepNumber(Long flowConfigId);
    
    Optional<ApprovalNode> findByFlowConfigIdAndNodeCode(Long flowConfigId, String nodeCode);
    
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfigId = :flowConfigId AND an.approverType = :approverType")
    List<ApprovalNode> findByFlowConfigIdAndApproverType(@Param("flowConfigId") Long flowConfigId, @Param("approverType") ApproverTypeEnum approverType);
    
    @Query("SELECT COUNT(an) FROM ApprovalNode an WHERE an.flowConfigId = :flowConfigId")
    Long countByFlowConfigId(@Param("flowConfigId") Long flowConfigId);
    
    // 根据当前步骤查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfigId = :flowConfigId AND an.stepNumber = :stepNumber")
    Optional<ApprovalNode> findByFlowConfigIdAndStepNumber(@Param("flowConfigId") Long flowConfigId, @Param("stepNumber") Integer stepNumber);
    
    // 查找下一个节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfigId = :flowConfigId AND an.stepNumber = (SELECT an2.stepNumber + 1 FROM ApprovalNode an2 WHERE an2.flowConfigId = :flowConfigId ORDER BY an2.stepNumber DESC LIMIT 1)")
    Optional<ApprovalNode> findNextNodeByFlowConfigId(@Param("flowConfigId") Long flowConfigId);

    // 根据步骤号排序获取所有节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfigId = :flowConfigId ORDER BY an.stepNumber ASC")
    List<ApprovalNode> findByFlowConfigIdOrderByStepNumberAsc(@Param("flowConfigId") Long flowConfigId);
    
    // 查找第一个节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.flowConfigId = :flowConfigId ORDER BY an.stepNumber ASC LIMIT 1")
    Optional<ApprovalNode> findFirstNodeByFlowConfigId(@Param("flowConfigId") Long flowConfigId);
    
    // 根据审批人ID和状态查找节点
    @Query("SELECT an FROM ApprovalNode an WHERE an.approverId = :approverId AND an.status = :status")
    List<ApprovalNode> findByApproverIdAndStatus(@Param("approverId") Long approverId, @Param("status") Integer status);

    // 以下方法是编译错误中缺失的方法声明
    List<ApprovalNode> findByProcessIdAndNodeLevel(Long processId, Integer nodeLevel);

    List<ApprovalNode> findByProcessId(Long processId);

    ApprovalNode findCurrentNodeByProcessId(Long processId);

    List<ApprovalNode> findByProcessIdAndNodeLevel(Long processId, int nodeLevel);

    Long countByApproverIdAndDateRange(Long approverId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    Long countPendingByApproverIdAndDateRange(Long approverId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    Long countApprovedByApproverIdAndDateRange(Long approverId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    Long countRejectedByApproverIdAndDateRange(Long approverId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<ApprovalNode> findProcessedByApproverIdAndDateRange(Long approverId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);

    List<ApprovalNode> findByProcessIdAndApproverId(Long processId, Long approverId);

    List<ApprovalNode> findByInstanceIdOrderByStepNumber(Long instanceId);
}