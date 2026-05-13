package com.ccms.repository.system.log;

import com.ccms.entity.system.log.SysOperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 操作日志表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long> {

    /**
     * 根据操作人ID查询日志
     * 
     * @param operUserId 操作人ID
     * @return 日志列表
     */
    List<SysOperLog> findByOperUserId(String operUserId);

    /**
     * 根据操作模块查询日志
     * 
     * @param operModule 操作模块
     * @return 日志列表
     */
    List<SysOperLog> findByOperModule(String operModule);

    /**
     * 根据操作类型查询日志
     * 
     * @param operType 操作类型
     * @return 日志列表
     */
    List<SysOperLog> findByOperType(String operType);

    /**
     * 根据业务ID查询日志
     * 
     * @param businessId 业务ID
     * @return 日志列表
     */
    List<SysOperLog> findByBusinessId(String businessId);

    /**
     * 查询指定时间范围内的日志
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 日志列表
     */
    List<SysOperLog> findByOperTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询最近的操作日志
     * 
     * @param startTime 开始时间
     * @return 日志列表
     */
    @Query("SELECT sol FROM SysOperLog sol WHERE sol.operTime >= :startTime ORDER BY sol.operTime DESC")
    List<SysOperLog> findRecentOperLogs(@Param("startTime") LocalDateTime startTime);

    /**
     * 统计用户的操作次数
     * 
     * @param operUserId 操作人ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作次数
     */
    Long countByOperUserIdAndOperTimeBetween(String operUserId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询模块操作频率统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return Object[] 包含 operModule 和 count
     */
    @Query("SELECT sol.operModule, COUNT(sol) FROM SysOperLog sol WHERE sol.operTime BETWEEN :startTime AND :endTime GROUP BY sol.operModule ORDER BY COUNT(sol) DESC")
    List<Object[]> findModuleOperFrequency(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 删除过期日志
     * 
     * @param expireDays 过期天数
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM SysOperLog sol WHERE sol.operTime < :expireTime")
    void deleteExpiredLogs(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 根据操作IP查询日志
     * 
     * @param operIp 操作IP
     * @return 日志列表
     */
    List<SysOperLog> findByOperIp(String operIp);

    /**
     * 查询指定用户的最近操作
     * 
     * @param operUserId 操作人ID
     * @param limit 限制数量
     * @return 日志列表
     */
    @Query("SELECT sol FROM SysOperLog sol WHERE sol.operUserId = :operUserId ORDER BY sol.operTime DESC LIMIT :limit")
    List<SysOperLog> findRecentOpsByUser(@Param("operUserId") String operUserId, @Param("limit") Integer limit);
}