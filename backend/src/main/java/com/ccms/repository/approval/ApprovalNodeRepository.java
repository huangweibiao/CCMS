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
}