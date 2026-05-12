package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批审计日志仓储接口
 */
public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLog, Long>, JpaSpecificationExecutor<ApprovalAuditLog> {

    /**
     * 根据审批实例ID查询审计日志，按时间倒序排列
     */
    List<ApprovalAuditLog> findByInstanceIdOrderByLogTimeDesc(Long instanceId);

    /**
     * 根据业务类型和业务ID查询审计日志，按时间倒序排列
     */
    List<ApprovalAuditLog> findByBusinessTypeAndBusinessIdOrderByLogTimeDesc(String businessType, String businessId);

    /**
     * 根据操作人ID和时间范围查询审计日志
     */
    List<ApprovalAuditLog> findByOperatorIdAndLogTimeBetweenOrderByLogTimeDesc(
            Long operatorId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据业务类型和时间范围统计操作次数
     */
    @Query("SELECT COUNT(al) FROM ApprovalAuditLog al WHERE al.businessType = :businessType AND al.logTime BETWEEN :startTime AND :endTime")
    Long countByBusinessTypeAndTimeRange(@Param("businessType") String businessType,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    /**
     * 查询失败的操作日志
     */
    List<ApprovalAuditLog> findBySuccessFalseOrderByLogTimeDesc();

    /**
     * 根据动作类型查询最新的审计日志
     */
    List<ApprovalAuditLog> findByActionTypeOrderByLogTimeDesc(String actionType);

    /**
     * 统计指定时间范围内的日志数量
     */
    @Query("SELECT COUNT(al) FROM ApprovalAuditLog al WHERE al.logTime BETWEEN :startTime AND :endTime")
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime, 
                         @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的审计日志（用于数据归档）
     */
    void deleteByLogTimeBefore(LocalDateTime cutoffTime);
}