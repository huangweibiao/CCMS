package com.ccms.repository.export;

import com.ccms.entity.export.ExportTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 导出任务仓库接口
 */
public interface ExportTaskRepository extends JpaRepository<ExportTask, Long>, JpaSpecificationExecutor<ExportTask> {

    /**
     * 根据任务ID查找
     */
    Optional<ExportTask> findByTaskId(String taskId);

    /**
     * 根据创建人查询任务列表
     */
    List<ExportTask> findByCreateByOrderByCreateTimeDesc(String createBy);

    /**
     * 根据状态查询任务列表
     */
    List<ExportTask> findByStatusOrderByCreateTimeDesc(Integer status);

    /**
     * 根据创建人和状态查询
     */
    List<ExportTask> findByCreateByAndStatusOrderByCreateTimeDesc(String createBy, Integer status);

    /**
     * 查询指定时间之前的过期任务
     */
    @Query("SELECT et FROM ExportTask et WHERE et.createTime < :threshold AND et.status IN :statusList")
    List<ExportTask> findByCreateTimeBeforeAndStatusIn(
            @Param("threshold") LocalDateTime threshold,
            @Param("statusList") List<Integer> statusList);

    /**
     * 删除过期任务记录
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ExportTask et WHERE et.createTime < :threshold AND et.status IN :statusList")
    int deleteByCreateTimeBeforeAndStatusIn(
            @Param("threshold") LocalDateTime threshold,
            @Param("statusList") List<Integer> statusList);

    /**
     * 统计各类任务数量
     */
    @Query("SELECT et.status, COUNT(et) FROM ExportTask et WHERE et.createBy = :createBy GROUP BY et.status")
    List<Object[]> countTasksByStatus(@Param("createBy") String createBy);

    /**
     * 查找待处理的高优先级任务
     */
    @Query("SELECT et FROM ExportTask et WHERE et.status = 0 ORDER BY et.priority DESC, et.createTime ASC")
    List<ExportTask> findPendingTasksByPriority();

    /**
     * 更新任务状态
     */
    @Modifying
    @Transactional
    @Query("UPDATE ExportTask et SET et.status = :status WHERE et.taskId = :taskId")
    int updateTaskStatus(@Param("taskId") String taskId, @Param("status") Integer status);

    /**
     * 批量更新任务状态为超时
     */
    @Modifying
    @Transactional
    @Query("UPDATE ExportTask et SET et.status = 3, et.errorMessage = '处理超时' " +
           "WHERE et.status = 1 AND et.startTime < :timeoutThreshold")
    int markTimeoutTasks(@Param("timeoutThreshold") LocalDateTime timeoutThreshold);

    /**
     * 统计任务处理时长
     */
    @Query("SELECT AVG(et.actualSeconds), MAX(et.actualSeconds), MIN(et.actualSeconds) " +
           "FROM ExportTask et WHERE et.status = 2 AND et.actualSeconds IS NOT NULL")
    Object[] getTaskProcessTimeStats();

    /**
     * 根据模板代码统计导出次数
     */
    @Query("SELECT et.templateCode, COUNT(et) FROM ExportTask et WHERE et.status = 2 GROUP BY et.templateCode")
    List<Object[]> countExportsByTemplate();

    /**
     * 获取用户最近的任务
     */
    @Query("SELECT et FROM ExportTask et WHERE et.createBy = :createBy " +
           "AND et.createTime >= :recentTime ORDER BY et.createTime DESC")
    List<ExportTask> findRecentTasksByUser(@Param("createBy") String createBy,
                                          @Param("recentTime") LocalDateTime recentTime);

    /**
     * 根据创建时间范围查询
     */
    @Query("SELECT et FROM ExportTask et WHERE et.createTime BETWEEN :startTime AND :endTime")
    List<ExportTask> findByCreateTimeRange(@Param("startTime") LocalDateTime startTime,
                                          @Param("endTime") LocalDateTime endTime);

    /**
     * 检查任务是否存在
     */
    boolean existsByTaskId(String taskId);

    /**
     * 统计不同格式的导出次数
     */
    @Query("SELECT et.exportFormat, COUNT(et) FROM ExportTask et WHERE et.status = 2 GROUP BY et.exportFormat")
    List<Object[]> countExportsByFormat();
}