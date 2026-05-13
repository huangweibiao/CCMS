package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.BusinessTypeEnum;
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
     * 根据业务ID和业务类型枚举查找审批实例
     */
    Optional<ApprovalInstance> findByBusinessIdAndBusinessType(Long businessId, BusinessTypeEnum businessType);
    
    /**
     * 根据业务类型枚举查找审批实例列表
     */
    List<ApprovalInstance> findByBusinessType(BusinessTypeEnum businessType);
    
    /**
     * 根据状态代码查找审批实例
     */
    List<ApprovalInstance> findByStatus(Integer statusCode);
    
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
    @Query("SELECT ai.status, COUNT(ai), AVG(TIMESTAMPDIFF(DAY, ai.createTime, ai.finishTime)) FROM ApprovalInstance ai WHERE ai.finishTime IS NOT NULL AND ai.createTime BETWEEN :startTime AND :endTime GROUP BY ai.status")
    List<Object[]> findApprovalStatsByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据业务类型和业务ID查找最新的审批实例
     */
    Optional<ApprovalInstance> findTopByBusinessTypeAndBusinessIdOrderByCreateTimeDesc(BusinessTypeEnum businessType, Long businessId);
    
    /**
     * 根据流程配置ID查找审批实例
     */
    List<ApprovalInstance> findByFlowConfigId(Long flowConfigId);
    
    /**
     * 查找未完成的审批实例
     */
    @Query("SELECT ai FROM ApprovalInstance ai WHERE ai.status IN (0, 1)")
    List<ApprovalInstance> findUnfinishedInstances();
    
    /**
     * 查找指定时间段内处理的审批实例
     */
    @Query("SELECT ai FROM ApprovalInstance ai WHERE ai.finishTime IS NOT NULL AND ai.finishTime BETWEEN :startTime AND :endTime")
    List<ApprovalInstance> findCompletedInstancesInTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 计算平均审批时长
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, ai.createTime, ai.finishTime)) FROM ApprovalInstance ai WHERE ai.finishTime IS NOT NULL AND ai.createTime BETWEEN :startTime AND :endTime")
    Double findAverageApprovalDuration(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    /**
     * 根据状态代码和完成时间之后查找审批实例
     */
    List<ApprovalInstance> findByStatusAndFinishTimeAfter(Integer statusCode, LocalDateTime finishTime);

    // 以下方法是编译错误中缺失的方法声明
    Optional<ApprovalInstance> findTop1ByBusinessTypeAndBusinessId(String businessType, Long businessId);

    List<ApprovalInstance> findByStatusNotAndCreateByNot(Long status, String createBy, org.springframework.data.domain.Pageable pageable);

    List<ApprovalInstance> findByCreateBy(String createBy, org.springframework.data.domain.Pageable pageable);

    Long countByCreateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    Long countByStatusAndCreateTimeBetween(Long status, LocalDateTime startDate, LocalDateTime endDate);

    // 新增缺失的查询方法
    /**
     * 根据状态和更新时间之前删除审批实例
     */
    int deleteByStatusAndUpdateTimeBefore(ApprovalStatusEnum status, LocalDateTime updateTime);

    /**
     * 根据状态统计审批实例数量
     */
    Long countByStatus(ApprovalStatusEnum status);

    /**
     * 根据创建时间之后统计审批实例数量
     */
    Long countByCreateTimeAfter(LocalDateTime createTime);

    /**
     * 根据状态列表统计审批实例数量
     */
    Long countByStatusIn(List<ApprovalStatusEnum> statusList);

    /**
     * 根据业务ID查找审批实例
     */
    Optional<ApprovalInstance> findByBusinessId(Long businessId);
}