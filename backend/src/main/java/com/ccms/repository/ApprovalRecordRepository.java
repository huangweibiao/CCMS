package com.ccms.repository;

import com.ccms.entity.approval.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批记录数据访问接口
 */
@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {
    
    /**
     * 根据业务单据ID和类型查找审批记录，按审批步骤排序
     */
    List<ApprovalRecord> findByBusinessIdAndBusinessTypeOrderByApprovalStep(Long businessId, String businessType);
    
    /**
     * 根据业务单据ID查找审批记录
     */
    List<ApprovalRecord> findByBusinessId(Long businessId);
    
    /**
     * 根据审批配置ID查找审批记录
     */
    List<ApprovalRecord> findByApprovalConfigId(Long approvalConfigId);
    
    /**
     * 查找超时审批记录
     */
    @Query("SELECT r FROM ApprovalRecord r WHERE r.approvalStatus = 0 AND r.createTime < ?1")
    List<ApprovalRecord> findTimeoutRecords(LocalDateTime deadline);
}