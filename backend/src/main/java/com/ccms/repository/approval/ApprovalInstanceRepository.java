package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 审批实例数据访问接口
 */
public interface ApprovalInstanceRepository extends BaseRepository<ApprovalInstance, Long> {
    
    /**
     * 根据业务ID和业务类型查找审批实例
     */
    Optional<ApprovalInstance> findByBusinessIdAndBusinessType(Long businessId, String businessType);
    
    /**
     * 根据业务类型查找审批实例列表
     */
    List<ApprovalInstance> findByBusinessType(String businessType);
    
    /**
     * 根据状态查找审批实例
     */
    List<ApprovalInstance> findByStatus(Integer status);
    
    /**
     * 根据申请人ID查找审批实例
     */
    List<ApprovalInstance> findByApplicantId(Long applicantId);
    
    /**
     * 根据当前审批人ID查找审批实例
     */
    List<ApprovalInstance> findByCurrentApproverId(Long currentApproverId);
    
    /**
     * 根据创建时间范围查找审批实例
     */
    List<ApprovalInstance> findByCreateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);
    
    /**
     * 统计指定时间范围内的审批实例数量
     */
    @Query("SELECT COUNT(ai) FROM ApprovalInstance ai WHERE ai.createTime BETWEEN :startTime AND :endTime")
    long countByCreateTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据业务类型统计审批实例数量
     */
    @Query("SELECT ai.businessType, COUNT(ai) FROM ApprovalInstance ai WHERE ai.createTime BETWEEN :startTime AND :endTime GROUP BY ai.businessType")
    List<Object[]> countByBusinessTypeAndTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据状态统计审批实例详细信息
     */
    @Query("SELECT ai.status, COUNT(ai), AVG(TIMESTAMPDIFF(DAY, ai.createTime, ai.approveTime)) FROM ApprovalInstance ai WHERE ai.approveTime IS NOT NULL AND ai.createTime BETWEEN :startTime AND :endTime GROUP BY ai.status")
    List<Object[]> findApprovalStatsByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据业务类型和业务ID查找最新的审批实例
     */
    Optional<ApprovalInstance> findTopByBusinessTypeAndBusinessIdOrderByCreateTimeDesc(String businessType, Long businessId);
}