package com.ccms.service.audit;

import com.ccms.entity.audit.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 审计日志服务接口
 * 提供系统操作日志的记录、查询和分析功能
 */
public interface AuditLogService {

    /**
     * 记录操作日志
     * 
     * @param module 模块名称
     * @param operation 操作类型
     * @param description 操作描述
     * @param userId 用户ID
     * @param username 用户名
     * @param userIp 用户IP
     * @param success 操作是否成功
     */
    void logOperation(String module, String operation, String description, 
                     Long userId, String username, String userIp, Boolean success);

    /**
     * 记录操作日志（带请求信息）
     * 
     * @param module 模块名称
     * @param operation 操作类型
     * @param description 操作描述
     * @param userId 用户ID
     * @param username 用户名
     * @param userIp 用户IP
     * @param success 操作是否成功
     * @param requestMethod 请求方法
     * @param requestUrl 请求URL
     * @param requestParams 请求参数
     */
    void logOperation(String module, String operation, String description,
                     Long userId, String username, String userIp, Boolean success,
                     String requestMethod, String requestUrl, String requestParams);

    /**
     * 记录实体操作日志
     * 
     * @param module 模块名称
     * @param operation 操作类型
     * @param description 操作描述
     * @param userId 用户ID
     * @param username 用户名
     * @param userIp 用户IP
     * @param success 操作是否成功
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param entityData 实体数据快照
     */
    void logEntityOperation(String module, String operation, String description,
                           Long userId, String username, String userIp, Boolean success,
                           String entityType, Long entityId, String entityData);

    /**
     * 查询审计日志
     * 
     * @param module 模块名称（可选）
     * @param operation 操作类型（可选）
     * @param username 用户名（可选）
     * @param success 操作状态（可选）
     * @param startTime 开始时间（必填）
     * @param endTime 结束时间（必填）
     * @param pageable 分页参数
     * @return 分页查询结果
     */
    Page<AuditLog> findAuditLogs(String module, String operation, String username, 
                               Boolean success, LocalDateTime startTime, LocalDateTime endTime,
                               Pageable pageable);

    /**
     * 根据ID查询审计日志
     * 
     * @param id 日志ID
     * @return 审计日志对象
     */
    AuditLog findById(Long id);

    /**
     * 获取实体操作历史
     * 
     * @param entityType 实体类型
     * @param entityId 实体ID
     * @param pageable 分页参数
     * @return 实体操作历史记录
     */
    Page<AuditLog> getEntityAuditHistory(String entityType, Long entityId, Pageable pageable);

    /**
     * 获取用户操作统计
     * 
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 用户操作统计信息
     */
    Map<String, Object> getUserOperationStats(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取错误操作分析
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 错误操作分析结果
     */
    Map<String, Object> analyzeErrorPatterns(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取活跃用户排行
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param limit 返回数量限制
     * @return 活跃用户排行
     */
    Map<String, Long> getActiveUsersRanking(LocalDateTime startTime, LocalDateTime endTime, int limit);

    /**
     * 清理过期审计日志
     * 
     * @param retentionDays 保留天数
     * @return 清理的日志数量
     */
    int cleanupExpiredLogs(int retentionDays);

    /**
     * 导出审计日志
     * 
     * @param module 模块名称（可选）
     * @param operation 操作类型（可选）
     * @param username 用户名（可选）
     * @param success 操作状态（可选）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 导出文件路径
     */
    String exportAuditLogs(String module, String operation, String username, 
                          Boolean success, LocalDateTime startTime, LocalDateTime endTime);
}