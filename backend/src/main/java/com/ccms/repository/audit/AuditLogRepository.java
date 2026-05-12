package com.ccms.repository.audit;

import com.ccms.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据访问接口
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * 根据操作类型查找审计日志
     */
    List<AuditLog> findByOperationOrderByCreateTimeDesc(String operation);

    /**
     * 根据模块查找审计日志
     */
    Page<AuditLog> findByModuleOrderByCreateTimeDesc(String module, Pageable pageable);

    /**
     * 根据用户名查找审计日志
     */
    Page<AuditLog> findByUsernameOrderByCreateTimeDesc(String username, Pageable pageable);

    /**
     * 根据用户ID查找审计日志
     */
    List<AuditLog> findByUserIdOrderByCreateTimeDesc(Long userId);

    /**
     * 根据操作结果查找审计日志
     */
    Page<AuditLog> findBySuccessOrderByCreateTimeDesc(Boolean success, Pageable pageable);

    /**
     * 根据时间范围查找审计日志
     */
    Page<AuditLog> findByCreateTimeBetweenOrderByCreateTimeDesc(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    /**
     * 根据实体类型和实体ID查找审计日志
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreateTimeDesc(String entityType, Long entityId);

    /**
     * 根据实体类型和实体ID分页查找审计日志
     */
    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreateTimeDesc(String entityType, Long entityId, Pageable pageable);

    /**
     * 复杂的查询：根据多个条件查找审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:module IS NULL OR a.module = :module) AND " +
           "(:operation IS NULL OR a.operation = :operation) AND " +
           "(:username IS NULL OR a.username = :username) AND " +
           "(:success IS NULL OR a.success = :success) AND " +
           "(a.createTime BETWEEN :startTime AND :endTime)")
    Page<AuditLog> findByComplexConditions(@Param("module") String module,
                                           @Param("operation") String operation,
                                           @Param("username") String username,
                                           @Param("success") Boolean success,
                                           @Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime,
                                           Pageable pageable);

    /**
     * 统计指定时间段内的操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.operation = :operation AND a.createTime BETWEEN :startTime AND :endTime")
    long countByOperationAndTimeRange(@Param("operation") String operation,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 获取错误日志统计
     */
    @Query("SELECT a.errorMessage, COUNT(a) FROM AuditLog a WHERE a.success = false AND a.createTime BETWEEN :startTime AND :endTime GROUP BY a.errorMessage")
    List<Object[]> getErrorStats(@Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户活动最多的前N个用户
     */
    @Query("SELECT a.username, COUNT(a) as activityCount FROM AuditLog a WHERE a.createTime BETWEEN :startTime AND :endTime GROUP BY a.username ORDER BY activityCount DESC")
    List<Object[]> getTopActiveUsers(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    Pageable pageable);

    /**
     * 获取用户操作统计
     */
    @Query("SELECT a.operation, COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.createTime BETWEEN :startTime AND :endTime GROUP BY a.operation")
    List<Object[]> getOperationStatsByUser(@Param("userId") Long userId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户成功率统计
     */
    @Query("SELECT COUNT(a), SUM(CASE WHEN a.success = true THEN 1 ELSE 0 END) FROM AuditLog a WHERE a.userId = :userId AND a.createTime BETWEEN :startTime AND :endTime")
    List<Object[]> getUserSuccessRate(@Param("userId") Long userId,
                                     @Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间段的平均响应时间
     */
    @Query("SELECT AVG(a.executionTime) FROM AuditLog a WHERE a.createTime BETWEEN :startTime AND :endTime AND a.executionTime IS NOT NULL")
    Double getAverageResponseTime(@Param("startTime") LocalDateTime startTime,
                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户操作成功率统计
     */
    @Query("SELECT COUNT(a),\n                   SUM(CASE WHEN a.success = true THEN 1 ELSE 0 END),\n                   CAST(SUM(CASE WHEN a.success = true THEN 1 ELSE 0 END) AS DOUBLE) / COUNT(a) * 100 \n            FROM AuditLog a \n            WHERE a.userId = :userId AND a.createTime BETWEEN :startTime AND :endTime")
    List<Object[]> getUserDetailedSuccessRate(@Param("userId") Long userId,
                                             @Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户模块操作统计
     */
    @Query("SELECT a.module, COUNT(a) FROM AuditLog a WHERE a.userId = :userId AND a.createTime BETWEEN :startTime AND :endTime GROUP BY a.module")
    List<Object[]> getModuleStatsByUser(@Param("userId") Long userId,
                                       @Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 按模块统计错误操作
     */
    @Query("SELECT a.module, COUNT(a) FROM AuditLog a WHERE a.success = false AND a.createTime BETWEEN :startTime AND :endTime GROUP BY a.module")
    List<Object[]> getErrorStatsByModule(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    /**
     * 按小时统计错误操作
     */
    @Query("SELECT HOUR(a.createTime), COUNT(a) FROM AuditLog a WHERE a.success = false AND a.createTime BETWEEN :startTime AND :endTime GROUP BY HOUR(a.createTime)")
    List<Object[]> getErrorStatsByHour(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取常见错误操作
     */
    @Query("SELECT a.module, a.operation, COUNT(a) FROM AuditLog a WHERE a.success = false AND a.createTime BETWEEN :startTime AND :endTime GROUP BY a.module, a.operation ORDER BY COUNT(a) DESC")
    List<Object[]> getCommonErrorOperations(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期审计日志
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM AuditLog a WHERE a.createTime < :cutoffTime")
    int deleteByCreateTimeBefore(@Param("cutoffTime") LocalDateTime cutoffTime);
}
