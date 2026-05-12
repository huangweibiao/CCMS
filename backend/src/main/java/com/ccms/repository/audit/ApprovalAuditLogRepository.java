package com.ccms.repository.audit;

import com.ccms.audit.ApprovalAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批审计日志Repository接口
 */
@Repository
public interface ApprovalAuditLogRepository extends JpaRepository<ApprovalAuditLog, Long> {

    /**
     * 根据操作类型查询审计日志
     */
    List<ApprovalAuditLog> findByOperationType(String operationType);

    /**
     * 根据目标实体类型和ID查询审计日志
     */
    List<ApprovalAuditLog> findByTargetEntityAndTargetId(String targetEntity, Long targetId);

    /**
     * 根据用户ID查询审计日志
     */
    List<ApprovalAuditLog> findByUserId(Long userId);

    /**
     * 根据操作时间范围查询审计日志
     */
    List<ApprovalAuditLog> findByOperationTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据操作结果查询审计日志
     */
    List<ApprovalAuditLog> findByOperationResult(String operationResult);

    /**
     * 根据业务类型查询审计日志
     */
    List<ApprovalAuditLog> findByBusinessType(String businessType);

    /**
     * 根据业务ID查询审计日志
     */
    List<ApprovalAuditLog> findByBusinessId(Long businessId);

    /**
     * 综合条件查询审计日志（分页）
     */
    @Query("SELECT a FROM ApprovalAuditLog a WHERE " +
           "(:operationType IS NULL OR a.operationType = :operationType) AND " +
           "(:targetEntity IS NULL OR a.targetEntity = :targetEntity) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:businessType IS NULL OR a.businessType = :businessType) AND " +
           "(:operationResult IS NULL OR a.operationResult = :operationResult) AND " +
           "a.operationTime BETWEEN :startTime AND :endTime")
    Page<ApprovalAuditLog> findAuditLogsByConditions(@Param("operationType") String operationType,
                                                   @Param("targetEntity") String targetEntity,
                                                   @Param("userId") Long userId,
                                                   @Param("businessType") String businessType,
                                                   @Param("operationResult") String operationResult,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   Pageable pageable);

    /**
     * 统计操作类型分布
     */
    @Query("SELECT a.operationType, COUNT(a) FROM ApprovalAuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime GROUP BY a.operationType")
    List<Object[]> countOperationsByType(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户操作频率
     */
    @Query("SELECT a.userId, a.userName, COUNT(a) FROM ApprovalAuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime GROUP BY a.userId, a.userName ORDER BY COUNT(a) DESC")
    List<Object[]> countOperationsByUser(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作成功率
     */
    @Query("SELECT a.operationType, COUNT(a), SUM(CASE WHEN a.operationResult = 'SUCCESS' THEN 1 ELSE 0 END) FROM ApprovalAuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime GROUP BY a.operationType")
    List<Object[]> calculateOperationSuccessRate(@Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 查询所有失败的操作日志
     */
    List<ApprovalAuditLog> findByOperationResultNotOrderByOperationTimeDesc(String operationResult);

    /**
     * 根据部门ID查询审计日志
     */
    List<ApprovalAuditLog> findByDepartmentId(Long departmentId);

    /**
     * 查找特定实体的完整操作历史
     */
    List<ApprovalAuditLog> findByTargetEntityAndTargetIdOrderByOperationTimeDesc(String targetEntity, Long targetId);

    /**
     * 删除指定时间前的审计日志（用于数据清理）
     */
    @Query("DELETE FROM ApprovalAuditLog a WHERE a.operationTime < :beforeTime")
    int deleteByOperationTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 统计最近N天的操作量
     */
    @Query("SELECT DATE(a.operationTime), COUNT(a) FROM ApprovalAuditLog a WHERE a.operationTime >= :startDate GROUP BY DATE(a.operationTime) ORDER BY DATE(a.operationTime) DESC")
    List<Object[]> countOperationsByDaily(@Param("startDate") LocalDateTime startDate);

    /**
     * 获取执行时间过长的操作日志（性能分析）
     */
    @Query("SELECT a FROM ApprovalAuditLog a WHERE a.executionTime > :threshold ORDER BY a.executionTime DESC")
    List<ApprovalAuditLog> findSlowOperations(@Param("threshold") Long threshold);
}