package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批记录表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {

    /**
     * 根据业务ID和业务类型查询审批记录
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 审批记录列表
     */
    List<ApprovalRecord> findByBusinessIdAndBusinessType(Long businessId, String businessType);

    /**
     * 根据审批人ID查询审批记录
     * 
     * @param approverId 审批人ID
     * @return 审批记录列表
     */
    List<ApprovalRecord> findByApproverId(Long approverId);

    /**
     * 根据审批状态查询审批记录
     * 
     * @param approvalStatus 审批状态：0-待审批，1-审批中，2-已通过，3-已驳回，4-已撤回
     * @return 审批记录列表
     */
    List<ApprovalRecord> findByApprovalStatus(Integer approvalStatus);

    /**
     * 查询待审批的记录
     * 
     * @param approverId 审批人ID
     * @return 待审批记录列表
     */
    List<ApprovalRecord> findByApproverIdAndApprovalStatus(Long approverId, Integer approvalStatus);

    /**
     * 查询指定业务的最新审批节点
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 当前审批节点
     */
    @Query("SELECT ar FROM ApprovalRecord ar WHERE ar.businessId = :businessId AND ar.businessType = :businessType ORDER BY ar.approvalTime DESC LIMIT 1")
    ApprovalRecord findLatestRecordByBusiness(@Param("businessId") Long businessId, @Param("businessType") String businessType);

    /**
     * 统计用户的待审批数量
     * 
     * @param approverId 审批人ID
     * @return 待审批数量
     */
    @Query("SELECT COUNT(ar) FROM ApprovalRecord ar WHERE ar.approverId = :approverId AND ar.approvalStatus = 0")
    Long countPendingApprovalsByUserId(@Param("approverId") Long approverId);

    /**
     * 更新审批记录状态
     * 
     * @param id 记录ID
     * @param approvalStatus 新审批状态
     * @param approvalComment 审批意见
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE ApprovalRecord ar SET ar.approvalStatus = :approvalStatus, ar.approvalComment = :approvalComment, ar.approvalTime = CURRENT_TIMESTAMP WHERE ar.id = :id")
    void updateApprovalStatus(@Param("id") Long id, @Param("approvalStatus") Integer approvalStatus, @Param("approvalComment") String approvalComment);

    /**
     * 查询指定业务的所有审批节点（按审批顺序排序）
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 审批记录列表
     */
    @Query("SELECT ar FROM ApprovalRecord ar WHERE ar.businessId = :businessId AND ar.businessType = :businessType ORDER BY ar.approvalTime ASC")
    List<ApprovalRecord> findRecordsByBusinessOrdered(@Param("businessId") Long businessId, @Param("businessType") String businessType);

    /**
     * 查询指定业务已审批通过的记录
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 已通过审批记录列表
     */
    List<ApprovalRecord> findByBusinessIdAndBusinessTypeAndApprovalStatus(Long businessId, String businessType, Integer approvalStatus);

    /**
     * 统计指定业务的审批记录数量
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 记录数量
     */
    Long countByBusinessIdAndBusinessType(Long businessId, String businessType);
    
    /**
     * 创建委派记录
     * 
     * @param processId 流程ID
     * @param sourceApproverId 原审批人ID
     * @param targetApproverId 目标审批人ID
     * @param comment 委派说明
     * @return 创建的记录
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query(value = "INSERT INTO approval_record (process_id, approver_id, original_approver_id, delegation_comment, create_time) VALUES (:processId, :targetApproverId, :sourceApproverId, :comment, CURRENT_TIMESTAMP)", nativeQuery = true)
    void createDelegationRecord(@Param("processId") Long processId, @Param("sourceApproverId") Long sourceApproverId, @Param("targetApproverId") Long targetApproverId, @Param("comment") String comment);
}