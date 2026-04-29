package com.ccms.repository;

import com.ccms.entity.system.SysOperLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统操作日志仓储接口
 */
@Repository
public interface SysOperLogRepository extends JpaRepository<SysOperLog, Long>, JpaSpecificationExecutor<SysOperLog> {

    /**
     * 根据业务ID和模块查询操作日志
     */
    List<SysOperLog> findByBusinessIdAndBusinessModuleOrderByOperTimeDesc(String businessId, String businessModule);

    /**
     * 根据用户ID分页查询操作日志
     */
    Page<SysOperLog> findByOperUserIdOrderByOperTimeDesc(String operUserId, Pageable pageable);

    /**
     * 根据时间范围统计操作数量
     */
    long countByOperTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据时间范围和状态统计操作数量
     */
    long countByOperTimeBetweenAndStatus(LocalDateTime startTime, LocalDateTime endTime, Integer status);

    /**
     * 根据时间范围查询操作日志
     */
    List<SysOperLog> findByOperTimeBefore(LocalDateTime beforeTime);

    /**
     * 删除指定时间之前的操作日志
     */
    @Modifying
    @Query("DELETE FROM SysOperLog s WHERE s.operTime < :beforeTime")
    int deleteByOperTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 根据时间范围查询平均执行耗时
     */
    @Query("SELECT AVG(s.costTime) FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime AND s.costTime IS NOT NULL")
    Long findAvgCostTimeByOperTimeBetween(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据时间范围按业务类型分组统计
     */
    @Query("SELECT s.businessType, COUNT(s) FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime GROUP BY s.businessType")
    List<Object[]> countByOperTimeBetweenGroupByBusinessType(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据时间范围按用户分组统计
     */
    @Query("SELECT s.operName, COUNT(s) FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime GROUP BY s.operName")
    List<Object[]> countByOperTimeBetweenGroupByUser(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据时间范围按模块分组统计
     */
    @Query("SELECT s.operModule, COUNT(s) FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime GROUP BY s.operModule")
    List<Object[]> countByOperTimeBetweenGroupByModule(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按天统计操作趋势
     */
    @Query("SELECT DATE(s.operTime) as date, COUNT(s), " +
           "SUM(CASE WHEN s.status = 0 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), " +
           "AVG(CASE WHEN s.costTime IS NOT NULL THEN s.costTime ELSE NULL END) " +
           "FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE(s.operTime) ORDER BY DATE(s.operTime)")
    List<Object[]> findTrendByDay(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按小时统计操作趋势
     */
    @Query("SELECT DATE_FORMAT(s.operTime, '%Y-%m-%d %H:00') as hour, COUNT(s), " +
           "SUM(CASE WHEN s.status = 0 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), " +
           "AVG(CASE WHEN s.costTime IS NOT NULL THEN s.costTime ELSE NULL END) " +
           "FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE_FORMAT(s.operTime, '%Y-%m-%d %H:00') ORDER BY hour")
    List<Object[]> findTrendByHour(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 按月统计操作趋势
     */
    @Query("SELECT DATE_FORMAT(s.operTime, '%Y-%m') as month, COUNT(s), " +
           "SUM(CASE WHEN s.status = 0 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN s.status = 1 THEN 1 ELSE 0 END), " +
           "AVG(CASE WHEN s.costTime IS NOT NULL THEN s.costTime ELSE NULL END) " +
           "FROM SysOperLog s WHERE s.operTime BETWEEN :startTime AND :endTime " +
           "GROUP BY DATE_FORMAT(s.operTime, '%Y-%m') ORDER BY month")
    List<Object[]> findTrendByMonth(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询最近的操作日志
     */
    List<SysOperLog> findTop10ByOrderByOperTimeDesc();

    /**
     * 查询错误操作日志
     */
    Page<SysOperLog> findByStatusOrderByOperTimeDesc(Integer status, Pageable pageable);

    /**
     * 根据操作内容模糊查询
     */
    Page<SysOperLog> findByOperContentContainingOrderByOperTimeDesc(String operContent, Pageable pageable);
}