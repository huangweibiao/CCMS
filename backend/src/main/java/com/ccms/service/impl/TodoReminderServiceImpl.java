package com.ccms.service.impl;

import com.ccms.entity.system.TodoItem;
import com.ccms.repository.TodoItemRepository;
import com.ccms.service.MessageNotifyService;
import com.ccms.service.TodoReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 待办提醒服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class TodoReminderServiceImpl implements TodoReminderService {
    
    @Autowired
    private TodoItemRepository todoItemRepository;
    
    @Autowired
    private MessageNotifyService messageNotifyService;
    
    @Override
    public TodoOperationResult createTodo(com.ccms.service.TodoReminderService.TodoItem todoItem) {
        try {
            // 校验待办项
            if (!todoItem.isValid()) {
                return new TodoOperationResult(false, "待办项信息不完整");
            }
            
            // 设置默认值
            if (todoItem.getStatus() == null) {
                todoItem.setStatus(TodoStatus.PENDING);
            }
            if (todoItem.getCreatedTime() == null) {
                todoItem.setCreatedTime(LocalDateTime.now());
            }
            if (todoItem.getPriority() == null) {
                todoItem.setPriority(Priority.NORMAL);
            }
            
            // 转换为实体并保存
            TodoItem entity = new TodoItem();
            entity.fromServiceItem(todoItem);
            entity.setCreatedTime(LocalDateTime.now());
            
            TodoItem savedTodo = todoItemRepository.save(entity);
            
            // 发送待办提醒
            if (savedTodo.getStatus() == "PENDING") {
                sendTodoReminder(savedTodo.toServiceItem());
            }
            
            TodoOperationResult result = new TodoOperationResult(true, "创建待办项成功");
            result.setTodoId(savedTodo.getTodoId());
            result.setTodoItem(savedTodo.toServiceItem());
            return result;
            
        } catch (Exception e) {
            return new TodoOperationResult(false, "创建待办项失败: " + e.getMessage());
        }
    }
    
    @Override
    public TodoOperationResult updateTodo(com.ccms.service.TodoReminderService.TodoItem todoItem) {
        try {
            if (todoItem.getTodoId() == null) {
                return new TodoOperationResult(false, "待办项ID不能为空");
            }
            
            TodoItem existingTodo = todoItemRepository.findById(todoItem.getTodoId())
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoItem.getTodoId()));
            
            // 更新字段
            existingTodo.setTitle(todoItem.getTitle());
            existingTodo.setContent(todoItem.getContent());
            existingTodo.setTodoType(todoItem.getTodoType());
            existingTodo.setPriority(todoItem.getPriority() != null ? todoItem.getPriority().toString() : "NORMAL");
            existingTodo.setDeadline(todoItem.getDeadline());
            existingTodo.setActionUrl(todoItem.getActionUrl());
            existingTodo.setBizType(todoItem.getBizType());
            existingTodo.setBizId(todoItem.getBizId());
            existingTodo.setEstimatedMinutes(todoItem.getEstimatedMinutes());
            existingTodo.setRemarks(todoItem.getRemarks());
            existingTodo.setUpdatedTime(LocalDateTime.now());
            
            // 如果是状态变更，发送相应通知
            if (todoItem.getStatus() != null && !todoItem.getStatus().toString().equals(existingTodo.getStatus())) {
                existingTodo.setStatus(todoItem.getStatus().toString());
                if (todoItem.getStatus() == TodoStatus.COMPLETED) {
                    existingTodo.setCompletedTime(LocalDateTime.now());
                    sendTodoCompletionNotification(existingTodo.toServiceItem());
                }
            }
            
            TodoItem updatedTodo = todoItemRepository.save(existingTodo);
            
            TodoOperationResult result = new TodoOperationResult(true, "更新待办项成功");
            result.setTodoItem(updatedTodo.toServiceItem());
            return result;
            
        } catch (Exception e) {
            return new TodoOperationResult(false, "更新待办项失败: " + e.getMessage());
        }
    }
    
    @Override
    public TodoOperationResult completeTodo(Long todoId, Long userId, String remarks) {
        try {
            TodoItem todo = todoItemRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
            
            // 检查权限：只能完成分配给自己的待办项
            if (!todo.getAssigneeId().equals(userId)) {
                return new TodoOperationResult(false, "无权完成此待办项");
            }
            
            todo.markAsCompleted();
            todo.setRemarks(remarks);
            todo.setUpdatedTime(LocalDateTime.now());
            todo.setUpdatedBy(userId);
            
            TodoItem completedTodo = todoItemRepository.save(todo);
            
            // 发送完成通知
            sendTodoCompletionNotification(completedTodo.toServiceItem());
            
            TodoOperationResult result = new TodoOperationResult(true, "完成待办项成功");
            result.setTodoItem(completedTodo.toServiceItem());
            return result;
            
        } catch (Exception e) {
            return new TodoOperationResult(false, "完成待办项失败: " + e.getMessage());
        }
    }
    
    @Override
    public TodoOperationResult cancelTodo(Long todoId, Long userId, String reason) {
        try {
            TodoItem todo = todoItemRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
            
            // 检查权限：只能取消自己创建的或分配给的待办项
            boolean canCancel = todo.getCreatorId().equals(userId) || todo.getAssigneeId().equals(userId);
            if (!canCancel) {
                return new TodoOperationResult(false, "无权取消此待办项");
            }
            
            todo.markAsCancelled();
            todo.setRemarks(reason);
            todo.setUpdatedTime(LocalDateTime.now());
            todo.setUpdatedBy(userId);
            
            TodoItem cancelledTodo = todoItemRepository.save(todo);
            
            TodoOperationResult result = new TodoOperationResult(true, "取消待办项成功");
            result.setTodoItem(cancelledTodo.toServiceItem());
            return result;
            
        } catch (Exception e) {
            return new TodoOperationResult(false, "取消待办项失败: " + e.getMessage());
        }
    }
    
    @Override
    public TodoOperationResult deleteTodo(Long todoId, Long userId) {
        try {
            TodoItem todo = todoItemRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
            
            // 检查权限：只能删除自己创建的待办项
            if (!todo.getCreatorId().equals(userId)) {
                return new TodoOperationResult(false, "无权删除此待办项");
            }
            
            // 只能删除已完成或已取消的待办项
            if (todo.getStatus() != TodoStatus.COMPLETED && todo.getStatus() != TodoStatus.CANCELLED) {
                return new TodoOperationResult(false, "只能删除已完成或已取消的待办项");
            }
            
            todo.setDeleted(1);
            todo.setUpdatedTime(LocalDateTime.now());
            todo.setUpdatedBy(userId);
            todoItemRepository.save(todo);
            
            return new TodoOperationResult(true, "删除待办项成功");
            
        } catch (Exception e) {
            return new TodoOperationResult(false, "删除待办项失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<com.ccms.service.TodoReminderService.TodoItem> getUserTodos(Long userId, TodoStatus status, Priority priority) {
        return todoItemRepository.findByAssigneeIdAndStatusAndPriorityAndDeleted(userId, 
                status != null ? status.toString() : null, 
                priority != null ? priority.toString() : null, 
                0)
                .stream()
                .map(TodoItem::toServiceItem)
                .collect(Collectors.toList());
    }
    
    @Override
    public com.ccms.service.TodoReminderService.TodoItem getTodoDetail(Long todoId) {
        TodoItem todo = todoItemRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
        return todo.toServiceItem();
    }
    
    @Override
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    public void checkAndMarkExpiredTodos() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<TodoItem> expiredTodos = todoItemRepository.findExpiredTodos(now, "PENDING");
            
            for (TodoItem todo : expiredTodos) {
                todo.markAsExpired();
                todo.setUpdatedTime(now);
                todoItemRepository.save(todo);
                
                // 发送过期通知
                sendTodoExpirationNotification(todo.toServiceItem());
            }
            
            System.out.println("标记超期待办项完成，数量: " + expiredTodos.size());
            
        } catch (Exception e) {
            System.err.println("检查超期待办项失败: " + e.getMessage());
        }
    }
    
    @Override
    public MessageNotifyService.NotifyResult sendTodoReminder(com.ccms.service.TodoReminderService.TodoItem todoItem) {
        try {
            MessageNotifyService.TodoMessage todoMessage = new MessageNotifyService.TodoMessage(
                todoItem.getTitle(), 
                generateTodoContent(todoItem), 
                todoItem.getAssigneeId().toString()
            );
            
            todoMessage.setTodoType(todoItem.getTodoType());
            todoMessage.setDeadline(todoItem.getDeadline());
            todoMessage.setActionUrl(todoItem.getActionUrl());
            todoMessage.setPriority(getPriorityLevel(todoItem.getPriority()));
            todoMessage.setBizType(todoItem.getBizType());
            todoMessage.setBizId(todoItem.getBizId());
            todoMessage.setSourceType(todoItem.getSourceType());
            todoMessage.setSourceId(todoItem.getSourceId());
            todoMessage.setCreatedBy(todoItem.getCreatorId());
            
            return messageNotifyService.sendTodoReminder(todoMessage);
            
        } catch (Exception e) {
            MessageNotifyService.NotifyResult result = new MessageNotifyService.NotifyResult();
            result.setSuccess(false);
            result.setMessage("发送待办提醒失败: " + e.getMessage());
            return result;
        }
    }
    
    @Override
    public TodoStatistics getTodoStatistics(Long userId) {
        TodoStatistics stats = new TodoStatistics();
        
        List<TodoItem> userTodos = todoItemRepository.findByAssigneeIdAndDeleted(userId, 0);
        
        stats.setTotalCount(userTodos.size());
        stats.setPendingCount((int) userTodos.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .count());
        stats.setProcessingCount((int) userTodos.stream()
                .filter(t -> "PROCESSING".equals(t.getStatus()))
                .count());
        stats.setCompletedCount((int) userTodos.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count());
        stats.setExpiredCount((int) userTodos.stream()
                .filter(t -> "EXPIRED".equals(t.getStatus()))
                .count());
        
        // 计算超期待办数（未完成且已过截止日期）
        stats.setOverdueCount((int) userTodos.stream()
                .filter(t -> t.getDeadline() != null 
                        && LocalDateTime.now().isAfter(t.getDeadline())
                        && ("PENDING".equals(t.getStatus()) || "PROCESSING".equals(t.getStatus())))
                .count());
        
        // 计算紧急待办数
        stats.setUrgentCount((int) userTodos.stream()
                .filter(t -> "HIGH".equals(t.getPriority()) || "URGENT".equals(t.getPriority()))
                .filter(t -> "PENDING".equals(t.getStatus()) || "PROCESSING".equals(t.getStatus()))
                .count());
        
        // 计算完成率
        int totalNonCancelled = userTodos.stream()
                .filter(t -> !"CANCELLED".equals(t.getStatus()))
                .mapToInt(t -> 1)
                .sum();
        
        stats.setCompletionRate(totalNonCancelled > 0 ? 
                (double) stats.getCompletedCount() / totalNonCancelled : 0.0);
        
        return stats;
    }
    
    @Override
    public BatchTodoOperationResult batchCreateTodos(List<com.ccms.service.TodoReminderService.TodoItem> todoItems) {
        BatchTodoOperationResult result = new BatchTodoOperationResult();
        int successCount = 0;
        int failureCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        for (int i = 0; i < todoItems.size(); i++) {
            com.ccms.service.TodoReminderService.TodoItem todo = todoItems.get(i);
            try {
                TodoOperationResult singleResult = createTodo(todo);
                if (singleResult.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                    errorMessages.add("第" + (i + 1) + "个待办项: " + singleResult.getMessage());
                }
            } catch (Exception e) {
                failureCount++;
                errorMessages.add("第" + (i + 1) + "个待办项: " + e.getMessage());
            }
        }
        
        result.setSuccess(failureCount == 0);
        result.setMessage(String.format("批量创建待办项完成，成功%d个，失败%d个", successCount, failureCount));
        result.setSuccessCount(successCount);
        result.setFailureCount(failureCount);
        result.setErrorMessages(errorMessages);
        
        return result;
    }
    
    // ========== 私有方法 ==========
    
    private String getPriorityLevel(Priority priority) {
        if (priority == null) return "NORMAL";
        
        switch (priority) {
            case LOW: return "LOW";
            case NORMAL: return "NORMAL";
            case HIGH: return "HIGH";
            case URGENT: return "URGENT";
            default: return "NORMAL";
        }
    }
    
    private String generateTodoContent(com.ccms.service.TodoReminderService.TodoItem todo) {
        StringBuilder content = new StringBuilder();
        content.append(todo.getContent()).append("\n\n");
        
        if (todo.getDeadline() != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), todo.getDeadline());
            if (daysLeft < 0) {
                content.append("⚠️ 已超期: ").append(Math.abs(daysLeft)).append("天\n");
            } else if (daysLeft == 0) {
                content.append("⏰ 今日到期\n");
            } else {
                content.append("📅 剩余: ").append(daysLeft).append("天\n");
            }
        }
        
        if (todo.getEstimatedMinutes() != null && todo.getEstimatedMinutes() > 0) {
            content.append("⏱️ 预计耗时: ").append(todo.getEstimatedMinutes()).append("分钟\n");
        }
        
        if (todo.getActionUrl() != null && !todo.getActionUrl().isEmpty()) {
            content.append("🔗 操作链接: ").append(todo.getActionUrl()).append("\n");
        }
        
        return content.toString();
    }
    
    private void sendTodoCompletionNotification(com.ccms.service.TodoReminderService.TodoItem todo) {
        try {
            if (todo.getCreatorId() != null && !todo.getCreatorId().equals(todo.getAssigneeId())) {
                MessageNotifyService.InnerMessage message = new MessageNotifyService.InnerMessage(
                    "待办项已完成通知",
                    String.format("待办项 [%s] 已被处理人 [%s] 完成\n备注: %s", 
                        todo.getTitle(), 
                        todo.getAssigneeName() != null ? todo.getAssigneeName() : "",
                        todo.getRemarks() != null ? todo.getRemarks() : "无"),
                    todo.getCreatorId().toString()
                );
                
                message.setMsgType("TODO_COMPLETED");
                message.setMsgLevel("INFO");
                message.setBizType(todo.getBizType());
                message.setBizId(todo.getBizId());
                message.setSourceType("TODO");
                message.setSourceId(todo.getTodoId());
                message.setCreatedBy(todo.getAssigneeId());
                
                messageNotifyService.sendInnerMessage(message);
            }
        } catch (Exception e) {
            System.err.println("发送待办完成通知失败: " + e.getMessage());
        }
    }
    
    private void sendTodoExpirationNotification(com.ccms.service.TodoReminderService.TodoItem todo) {
        try {
            MessageNotifyService.InnerMessage message = new MessageNotifyService.InnerMessage(
                "待办项已过期通知",
                String.format("待办项 [%s] 已超期，系统已自动标记为已过期", todo.getTitle()),
                todo.getAssigneeId().toString()
            );
            
            message.setMsgType("TODO_EXPIRED");
            message.setMsgLevel("WARNING");
            message.setBizType(todo.getBizType());
            message.setBizId(todo.getBizId());
            message.setSourceType("TODO");
            message.setSourceId(todo.getTodoId());
            
            messageNotifyService.sendInnerMessage(message);
            
        } catch (Exception e) {
            System.err.println("发送待办过期通知失败: " + e.getMessage());
        }
    }
    
    private String getPriorityLevel(Priority priority) {
        if (priority == null) return "NORMAL";
        
        switch (priority) {
            case LOW: return "LOW";
            case NORMAL: return "NORMAL";
            case HIGH: return "HIGH";
            case URGENT: return "URGENT";
            default: return "NORMAL";
        }
    }
    
    private String generateTodoContent(TodoItem todo) {
        StringBuilder content = new StringBuilder();
        content.append(todo.getContent()).append("\n\n");
        
        if (todo.getDeadline() != null) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDateTime.now(), todo.getDeadline());
            if (daysLeft < 0) {
                content.append("⚠️ 已超期: ").append(Math.abs(daysLeft)).append("天\n");
            } else if (daysLeft == 0) {
                content.append("⏰ 今日到期\n");
            } else {
                content.append("📅 剩余: ").append(daysLeft).append("天\n");
            }
        }
        
        if (todo.getEstimatedMinutes() != null && todo.getEstimatedMinutes() > 0) {
            content.append("⏱️ 预计耗时: ").append(todo.getEstimatedMinutes()).append("分钟\n");
        }
        
        if (todo.getActionUrl() != null && !todo.getActionUrl().isEmpty()) {
            content.append("🔗 操作链接: ").append(todo.getActionUrl()).append("\n");
        }
        
        return content.toString();
    }
    
    private void sendTodoCompletionNotification(TodoItem todo) {
        try {
            if (todo.getCreatorId() != null && !todo.getCreatorId().equals(todo.getAssigneeId())) {
                MessageNotifyService.InnerMessage message = new MessageNotifyService.InnerMessage(
                    "待办项已完成通知",
                    String.format("待办项 [%s] 已被处理人 [%s] 完成\n备注: %s", 
                        todo.getTitle(), 
                        todo.getAssigneeName() != null ? todo.getAssigneeName() : "",
                        todo.getRemarks() != null ? todo.getRemarks() : "无"),
                    todo.getCreatorId().toString()
                );
                
                message.setMsgType("TODO_COMPLETED");
                message.setMsgLevel("INFO");
                message.setBizType(todo.getBizType());
                message.setBizId(todo.getBizId());
                message.setSourceType("TODO");
                message.setSourceId(todo.getTodoId());
                message.setCreatedBy(todo.getAssigneeId());
                
                messageNotifyService.sendInnerMessage(message);
            }
        } catch (Exception e) {
            System.err.println("发送待办完成通知失败: " + e.getMessage());
        }
    }
    
    private void sendTodoExpirationNotification(TodoItem todo) {
        try {
            MessageNotifyService.InnerMessage message = new MessageNotifyService.InnerMessage(
                "待办项已过期通知",
                String.format("待办项 [%s] 已超期，系统已自动标记为已过期", todo.getTitle()),
                todo.getAssigneeId().toString()
            );
            
            message.setMsgType("TODO_EXPIRED");
            message.setMsgLevel("WARNING");
            message.setBizType(todo.getBizType());
            message.setBizId(todo.getBizId());
            message.setSourceType("TODO");
            message.setSourceId(todo.getTodoId());
            
            messageNotifyService.sendInnerMessage(message);
            
        } catch (Exception e) {
            System.err.println("发送待办过期通知失败: " + e.getMessage());
        }
    }
}