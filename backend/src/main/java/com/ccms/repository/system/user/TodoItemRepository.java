package com.ccms.repository.system.user;

import com.ccms.entity.system.user.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待办项数据访问接口
 * 
 * @author 系统生成
 */
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long>, JpaSpecificationExecutor<TodoItem> {
    
    /**
     * 根据分配人ID和状态查询待办项
     * 
     * @param assigneeId 分配人ID
     * @param status 状态
     * @param priority 优先级
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByAssigneeIdAndStatusAndPriorityAndDeleted(Long assigneeId, String status, String priority, Integer deleted);
    
    /**
     * 根据分配人ID和删除标记查询待办项
     * 
     * @param assigneeId 分配人ID
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByAssigneeIdAndDeleted(Long assigneeId, Integer deleted);
    
    /**
     * 根据创建人ID查询待办项
     * 
     * @param creatorId 创建人ID
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByCreatorIdAndDeleted(Long creatorId, Integer deleted);
    
    /**
     * 根据业务类型和业务ID查询待办项
     * 
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByBizTypeAndBizIdAndDeleted(String bizType, Long bizId, Integer deleted);
    
    /**
     * 根据来源类型和来源ID查询待办项
     * 
     * @param sourceType 来源类型
     * @param sourceId 来源ID
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findBySourceTypeAndSourceIdAndDeleted(String sourceType, Long sourceId, Integer deleted);
    
    /**
     * 根据待办类型查询待办项
     * 
     * @param todoType 待办类型
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByTodoTypeAndDeleted(String todoType, Integer deleted);
    
    /**
     * 查询超期的待办项
     * 
     * @param currentTime 当前时间
     * @param status 待办状态
     * @return 超期待办项列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.deadline < :currentTime AND t.status = :status AND t.deleted = 0")
    List<TodoItem> findExpiredTodos(@Param("currentTime") LocalDateTime currentTime, 
                                   @Param("status") String status);
    
    /**
     * 查询即将到期的待办项
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param status 待办状态
     * @param deleted 删除标记
     * @return 即将到期待办项列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.deadline BETWEEN :startTime AND :endTime AND t.status = :status AND t.deleted = :deleted")
    List<TodoItem> findUpcomingTodos(@Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("status") String status,
                                    @Param("deleted") Integer deleted);
    
    /**
     * 统计用户的待办项数量
     * 
     * @param assigneeId 分配人ID
     * @param status 状态
     * @param deleted 删除标记
     * @return 待办项数量
     */
    long countByAssigneeIdAndStatusAndDeleted(Long assigneeId, String status, Integer deleted);
    
    /**
     * 统计用户各优先级的待办项数量
     * 
     * @param assigneeId 分配人ID
     * @param priority 优先级
     * @param deleted 删除标记
     * @return 待办项数量
     */
    long countByAssigneeIdAndPriorityAndDeleted(Long assigneeId, String priority, Integer deleted);
    
    /**
     * 统计用户完成的待办项数量
     * 
     * @param assigneeId 分配人ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deleted 删除标记
     * @return 完成待办项数量
     */
    @Query("SELECT COUNT(t) FROM TodoItem t WHERE t.assigneeId = :assigneeId AND t.status = 'COMPLETED' AND t.completedTime BETWEEN :startTime AND :endTime AND t.deleted = :deleted")
    long countCompletedTodosByAssigneeAndPeriod(@Param("assigneeId") Long assigneeId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime,
                                               @Param("deleted") Integer deleted);
    
    /**
     * 根据时间段查询创建的待办项
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByCreatedTimeBetweenAndDeleted(LocalDateTime startTime, LocalDateTime endTime, Integer deleted);
    
    /**
     * 根据时间段查询完成的待办项
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param deleted 删除标记
     * @return 已完成待办项列表
     */
    List<TodoItem> findByCompletedTimeBetweenAndDeleted(LocalDateTime startTime, LocalDateTime endTime, Integer deleted);
    
    /**
     * 查询用户待审批的待办项
     * 
     * @param assigneeId 分配人ID
     * @param statuses 状态列表
     * @param deleted 删除标记
     * @return 待审批待办项列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.assigneeId = :assigneeId AND t.status IN :statuses AND t.deleted = :deleted ORDER BY t.deadline ASC")
    List<TodoItem> findPendingApprovalTodos(@Param("assigneeId") Long assigneeId,
                                           @Param("statuses") List<String> statuses,
                                           @Param("deleted") Integer deleted);
    
    /**
     * 根据关键词搜索待办项
     * 
     * @param assigneeId 分配人ID
     * @param keyword 关键词
     * @param deleted 删除标记
     * @return 待办项列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.assigneeId = :assigneeId AND (t.title LIKE %:keyword% OR t.content LIKE %:keyword%) AND t.deleted = :deleted")
    List<TodoItem> searchByKeyword(@Param("assigneeId") Long assigneeId,
                                  @Param("keyword") String keyword,
                                  @Param("deleted") Integer deleted);
    
    /**
     * 统计用户待办项的平均完成时间
     * 
     * @param assigneeId 分配人ID
     * @param deleted 删除标记
     * @return 平均完成时间（天）
     */
    @Query(value = "SELECT AVG(DATEDIFF(t.completed_time, t.created_time)) FROM ccms_todo_item t WHERE t.assignee_id = :assigneeId AND t.status = 'COMPLETED' AND t.deleted = :deleted AND t.created_time IS NOT NULL AND t.completed_time IS NOT NULL", nativeQuery = true)
    Double findAverageCompletionDays(@Param("assigneeId") Long assigneeId, 
                                    @Param("deleted") Integer deleted);
    
    /**
     * 统计用户超过截止日期完成的待办项数量
     * 
     * @param assigneeId 分配人ID
     * @param deleted 删除标记
     * @return 超期完成数量
     */
    @Query("SELECT COUNT(t) FROM TodoItem t WHERE t.assigneeId = :assigneeId AND t.status = 'COMPLETED' AND t.deadline < t.completedTime AND t.deleted = :deleted")
    long countOverdueCompletedTodos(@Param("assigneeId") Long assigneeId, 
                                   @Param("deleted") Integer deleted);
    
    /**
     * 查询截止日期在之前且未删除的待办项
     * 
     * @param deadline 截止日期
     * @param status 状态
     * @param deleted 删除标记
     * @return 待办项列表
     */
    List<TodoItem> findByDeadlineBeforeAndStatusAndDeleted(LocalDateTime deadline, String status, Integer deleted);
    
    /**
     * 查找用户最近创建的N个待办项
     * 
     * @param assigneeId 分配人ID
     * @param limit 数量限制
     * @param deleted 删除标记
     * @return 最近待办项列表
     */
    @Query(value = "SELECT * FROM ccms_todo_item t WHERE t.assignee_id = :assigneeId AND t.deleted = :deleted ORDER BY t.created_time DESC LIMIT :limit", nativeQuery = true)
    List<TodoItem> findRecentTodosByAssignee(@Param("assigneeId") Long assigneeId,
                                            @Param("limit") int limit,
                                            @Param("deleted") Integer deleted);
    
    /**
     * 统计各业务类型的待办项分布
     * 
     * @param assigneeId 分配人ID
     * @param deleted 删除标记
     * @return 业务类型统计
     */
    @Query("SELECT t.bizType, COUNT(t) FROM TodoItem t WHERE t.assigneeId = :assigneeId AND t.deleted = :deleted GROUP BY t.bizType")
    List<Object[]> countTodosByBizType(@Param("assigneeId") Long assigneeId,
                                      @Param("deleted") Integer deleted);
    
    /**
     * 查询用户最高优先级的待办项
     * 
     * @param assigneeId 分配人ID
     * @param priorities 优先级列表
     * @param statuses 状态列表
     * @param deleted 删除标记
     * @return 高优先级待办项列表
     */
    @Query("SELECT t FROM TodoItem t WHERE t.assigneeId = :assigneeId AND t.priority IN :priorities AND t.status IN :statuses AND t.deleted = :deleted ORDER BY CASE t.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'NORMAL' THEN 3 WHEN 'LOW' THEN 4 END")
    List<TodoItem> findHighPriorityTodos(@Param("assigneeId") Long assigneeId,
                                        @Param("priorities") List<String> priorities,
                                        @Param("statuses") List<String> statuses,
                                        @Param("deleted") Integer deleted);
    
    /**
     * 逻辑删除待办项
     * 
     * @param todoId 待办项ID
     * @param updatedBy 更新人ID
     * @param updatedTime 更新时间
     * @return 影响行数
     */
    @Query("UPDATE TodoItem t SET t.deleted = 1, t.updatedBy = :updatedBy, t.updatedTime = :updatedTime WHERE t.todoId = :todoId")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int softDeleteTodo(@Param("todoId") Long todoId,
                      @Param("updatedBy") Long updatedBy,
                      @Param("updatedTime") LocalDateTime updatedTime);
    
    /**
     * 批量更新待办项状态
     * 
     * @param todoIds 待办项ID列表
     * @param newStatus 新状态
     * @param updatedBy 更新人ID
     * @param updatedTime 更新时间
     * @return 影响行数
     */
    @Query("UPDATE TodoItem t SET t.status = :newStatus, t.updatedBy = :updatedBy, t.updatedTime = :updatedTime WHERE t.todoId IN :todoIds")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int batchUpdateStatus(@Param("todoIds") List<Long> todoIds,
                         @Param("newStatus") String newStatus,
                         @Param("updatedBy") Long updatedBy,
                         @Param("updatedTime") LocalDateTime updatedTime);
}