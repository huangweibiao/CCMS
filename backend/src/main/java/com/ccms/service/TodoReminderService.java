package com.ccms.service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 待办提醒服务接口
 * 
 * @author 系统生成
 */
public interface TodoReminderService {
    
    /**
     * 待办项状态枚举
     */
    enum TodoStatus {
        PENDING("待处理"),
        PROCESSING("处理中"),
        COMPLETED("已完成"),
        CANCELLED("已取消"),
        EXPIRED("已过期");
        
        private final String description;
        
        TodoStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 待办项优先级
     */
    enum Priority {
        LOW("低", "#52c41a"),
        NORMAL("中等", "#1890ff"),
        HIGH("高", "#faad14"),
        URGENT("紧急", "#f5222d");
        
        private final String label;
        private final String color;
        
        Priority(String label, String color) {
            this.label = label;
            this.color = color;
        }
        
        public String getLabel() {
            return label;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    /**
     * 待办项信息
     */
    class TodoItem {
        private Long todoId;
        private String title;
        private String content;
        private String todoType;
        private Priority priority;
        private Long assigneeId;
        private String assigneeName;
        private Long creatorId;
        private String creatorName;
        private LocalDateTime createdTime;
        private LocalDateTime deadline;
        private TodoStatus status;
        private String actionUrl;
        private String bizType;
        private Long bizId;
        private String sourceType;
        private Long sourceId;
        private Integer estimatedMinutes;
        private LocalDateTime completedTime;
        private String remarks;
        
        public TodoItem() {}
        
        public TodoItem(String title, String content, Long assigneeId) {
            this.title = title;
            this.content = content;
            this.assigneeId = assigneeId;
            this.priority = Priority.NORMAL;
            this.status = TodoStatus.PENDING;
            this.createdTime = LocalDateTime.now();
        }
        
        // Getter and Setter methods
        public Long getTodoId() { return todoId; }
        public void setTodoId(Long todoId) { this.todoId = todoId; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getTodoType() { return todoType; }
        public void setTodoType(String todoType) { this.todoType = todoType; }
        
        public Priority getPriority() { return priority; }
        public void setPriority(Priority priority) { this.priority = priority; }
        
        public Long getAssigneeId() { return assigneeId; }
        public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
        
        public String getAssigneeName() { return assigneeName; }
        public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }
        
        public Long getCreatorId() { return creatorId; }
        public void setCreatorId(Long creatorId) { this.creatorId = creatorId; }
        
        public String getCreatorName() { return creatorName; }
        public void setCreatorName(String creatorName) { this.creatorName = creatorName; }
        
        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        
        public TodoStatus getStatus() { return status; }
        public void setStatus(TodoStatus status) { this.status = status; }
        
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public Long getBizId() { return bizId; }
        public void setBizId(Long bizId) { this.bizId = bizId; }
        
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        
        public Long getSourceId() { return sourceId; }
        public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
        
        public Integer getEstimatedMinutes() { return estimatedMinutes; }
        public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
        
        public LocalDateTime getCompletedTime() { return completedTime; }
        public void setCompletedTime(LocalDateTime completedTime) { this.completedTime = completedTime; }
        
        public String getRemarks() { return remarks; }
        public void setRemarks(String remarks) { this.remarks = remarks; }
        
        // 校验待办项是否有效
        public boolean isValid() {
            return title != null && !title.trim().isEmpty() 
                    && assigneeId != null;
        }
        
        // 校验是否已过期
        public boolean isExpired() {
            return deadline != null && LocalDateTime.now().isAfter(deadline) 
                    && status == TodoStatus.PENDING;
        }
        
        // 标记为处理中
        public void markAsProcessing() {
            this.status = TodoStatus.PROCESSING;
        }
        
        // 标记为已完成
        public void markAsCompleted() {
            this.status = TodoStatus.COMPLETED;
            this.completedTime = LocalDateTime.now();
        }
        
        // 标记为已取消
        public void markAsCancelled() {
            this.status = TodoStatus.CANCELLED;
        }
        
        // 标记为已过期
        public void markAsExpired() {
            this.status = TodoStatus.EXPIRED;
        }
    }
    
    /**
     * 创建待办项
     * 
     * @param todoItem 待办项信息
     * @return 创建结果
     */
    TodoOperationResult createTodo(TodoItem todoItem);
    
    /**
     * 更新待办项
     * 
     * @param todoItem 待办项信息
     * @return 更新结果
     */
    TodoOperationResult updateTodo(TodoItem todoItem);
    
    /**
     * 完成待办项
     * 
     * @param todoId 待办项ID
     * @param userId 用户ID
     * @param remarks 完成备注
     * @return 完成结果
     */
    TodoOperationResult completeTodo(Long todoId, Long userId, String remarks);
    
    /**
     * 取消待办项
     * 
     * @param todoId 待办项ID
     * @param userId 用户ID
     * @param reason 取消原因
     * @return 取消结果
     */
    TodoOperationResult cancelTodo(Long todoId, Long userId, String reason);
    
    /**
     * 删除待办项
     * 
     * @param todoId 待办项ID
     * @param userId 用户ID
     * @return 删除结果
     */
    TodoOperationResult deleteTodo(Long todoId, Long userId);
    
    /**
     * 获取用户待办列表
     * 
     * @param userId 用户ID
     * @param status 状态筛选
     * @param priority 优先级筛选
     * @return 待办列表
     */
    List<TodoItem> getUserTodos(Long userId, TodoStatus status, Priority priority);
    
    /**
     * 获取待办项详情
     * 
     * @param todoId 待办项ID
     * @return 待办项详情
     */
    TodoItem getTodoDetail(Long todoId);
    
    /**
     * 检查过期待办项并自动标记
     */
    void checkAndMarkExpiredTodos();
    
    /**
     * 发送待办提醒（集成消息通知服务）
     * 
     * @param todoItem 待办项信息
     * @return 发送结果
     */
    MessageNotifyService.NotifyResult sendTodoReminder(TodoItem todoItem);
    
    /**
     * 获取待办统计信息
     * 
     * @param userId 用户ID
     * @return 待办统计
     */
    TodoStatistics getTodoStatistics(Long userId);
    
    /**
     * 批量创建待办项
     * 
     * @param todoItems 待办项列表
     * @return 批量创建结果
     */
    BatchTodoOperationResult batchCreateTodos(List<TodoItem> todoItems);
    
    /**
     * 待办操作结果
     */
    class TodoOperationResult {
        private boolean success;
        private String message;
        private Long todoId;
        private TodoItem todoItem;
        
        public TodoOperationResult() {}
        
        public TodoOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Long getTodoId() { return todoId; }
        public void setTodoId(Long todoId) { this.todoId = todoId; }
        
        public TodoItem getTodoItem() { return todoItem; }
        public void setTodoItem(TodoItem todoItem) { this.todoItem = todoItem; }
    }
    
    /**
     * 待办统计信息
     */
    class TodoStatistics {
        private int totalCount;
        private int pendingCount;
        private int processingCount;
        private int completedCount;
        private int expiredCount;
        private int overdueCount;
        private int urgentCount;
        private double completionRate;
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        
        public int getProcessingCount() { return processingCount; }
        public void setProcessingCount(int processingCount) { this.processingCount = processingCount; }
        
        public int getCompletedCount() { return completedCount; }
        public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
        
        public int getExpiredCount() { return expiredCount; }
        public void setExpiredCount(int expiredCount) { this.expiredCount = expiredCount; }
        
        public int getOverdueCount() { return overdueCount; }
        public void setOverdueCount(int overdueCount) { this.overdueCount = overdueCount; }
        
        public int getUrgentCount() { return urgentCount; }
        public void setUrgentCount(int urgentCount) { this.urgentCount = urgentCount; }
        
        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }
    
    /**
     * 批量待办操作结果
     */
    class BatchTodoOperationResult {
        private boolean success;
        private String message;
        private int successCount;
        private int failureCount;
        private List<String> errorMessages;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailureCount() { return failureCount; }
        public void setFailureCount(int failureCount) { this.failureCount = failureCount; }
        
        public List<String> getErrorMessages() { return errorMessages; }
        public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }
        
        public int getTotalCount() {
            return successCount + failureCount;
        }
        
        public double getSuccessRate() {
            int total = getTotalCount();
            return total > 0 ? (double) successCount / total : 0.0;
        }
    }
}