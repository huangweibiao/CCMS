package com.ccms.service.todo;

import com.ccms.entity.todo.TodoItem;
import java.util.List;

/**
 * 待办事项服务接口
 */
public interface TodoService {
    
    /**
     * 获取用户待办列表
     */
    List<TodoItem> getUserTodos(Long userId, TodoItem.TodoStatus status, Integer priority);
    
    /**
     * 创建待办事项
     */
    TodoItem createTodo(TodoItem todo);
    
    /**
     * 更新待办状态
     */
    void updateTodoStatus(Long todoId, TodoItem.TodoStatus status);
    
    /**
     * 标记待办为完成
     */
    void markTodoCompleted(Long todoId);
    
    /**
     * 删除待办
     */
    void deleteTodo(Long todoId);
    
    /**
     * 批量创建审批待办
     */
    void batchCreateApprovalTodos(List<Long> userIds, String title, String description, 
                                 Long relatedId, String relatedType);
    
    /**
     * 获取待办统计
     */
    TodoStats getTodoStats(Long userId);
    
    /**
     * 处理过期待办
     */
    void processOverdueTodos();
    
    class TodoStats {
        private Integer pendingCount;
        private Integer processingCount;
        private Integer overdueCount;
        private Integer completedCount;
        
        public TodoStats() {}
        
        public TodoStats(Integer pendingCount, Integer processingCount) {
            this.pendingCount = pendingCount;
            this.processingCount = processingCount;
        }
        
        public Integer getPendingCount() { return pendingCount; }
        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
        
        public Integer getProcessingCount() { return processingCount; }
        public void setProcessingCount(Integer processingCount) { this.processingCount = processingCount; }
        
        public Integer getOverdueCount() { return overdueCount; }
        public void setOverdueCount(Integer overdueCount) { this.overdueCount = overdueCount; }
        
        public Integer getCompletedCount() { return completedCount; }
        public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }
    }
}