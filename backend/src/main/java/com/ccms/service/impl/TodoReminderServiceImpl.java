package com.ccms.service.impl;

import com.ccms.entity.system.user.TodoItem;
import com.ccms.repository.TodoItemRepository;
import com.ccms.service.MessageNotifyService;
import com.ccms.service.TodoReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

// 辅助方法和缺失方法实现
class TodoOperationResult {
    private boolean success;
    private String message;
    
    public TodoOperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}

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
    public TodoOperationResult createTodo(TodoItem serviceTodo) {
        try {
            // 校验待办项（简化版本）
            if (!isValidTodo(serviceTodo)) {
                return new TodoOperationResult(false, "待办项信息不完整");
            }
            
            // 转换为实体并保存
            com.ccms.entity.system.user.TodoItem entity = new com.ccms.entity.system.user.TodoItem();
            copyTodoProperties(serviceTodo, entity);
            entity.setCreatedTime(LocalDateTime.now());
            
            com.ccms.entity.system.user.TodoItem savedTodo = todoItemRepository.save(entity);
            
            // 发送待办提醒
            if (savedTodo.getStatus().equals("PENDING")) {
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
    public TodoOperationResult updateTodo(TodoItem serviceTodo) {
        try {
            if (serviceTodo.getTodoId() == null) {
                return new TodoOperationResult(false, "待办项ID不能为空");
            }
            
            com.ccms.entity.system.user.TodoItem existingTodo = todoItemRepository.findById(serviceTodo.getTodoId())
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + serviceTodo.getTodoId()));
            
            // 转换并更新字段
            existingTodo.fromServiceItem(serviceTodo);
            existingTodo.setUpdatedTime(LocalDateTime.now());
            
            // 如果是状态变更，发送相应通知
            if (serviceTodo.getStatus() != null && !serviceTodo.getStatus().toString().equals(existingTodo.getStatus())) {
                existingTodo.setStatus(convertStatusToString(serviceTodo.getStatus()));
                if (serviceTodo.getStatus() == TodoStatus.COMPLETED) {
                    existingTodo.setCompletedTime(LocalDateTime.now());
                    sendTodoCompletionNotification(existingTodo.toServiceItem());
                }
            }
            
            com.ccms.entity.system.user.TodoItem updatedTodo = todoItemRepository.save(existingTodo);
            
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
            com.ccms.entity.system.user.TodoItem todo = todoItemRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
            
            // 检查权限：只能完成分配给自己的待办项
            if (!todo.getAssigneeId().equals(userId)) {
                return new TodoOperationResult(false, "无权完成此待办项");
            }
            
            todo.markAsCompleted();
            todo.setRemarks(remarks);
            todo.setUpdatedTime(LocalDateTime.now());
            todo.setUpdatedBy(userId);
            
            com.ccms.entity.system.user.TodoItem completedTodo = todoItemRepository.save(todo);
            
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
            com.ccms.entity.system.user.TodoItem todo = todoItemRepository.findById(todoId)
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
            
            com.ccms.entity.system.user.TodoItem cancelledTodo = todoItemRepository.save(todo);
            
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
            com.ccms.entity.system.user.TodoItem todo = todoItemRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
            
            // 检查权限：只能删除自己创建的待办项
            if (!todo.getCreatorId().equals(userId)) {
                return new TodoOperationResult(false, "无权删除此待办项");
            }
            
            // 只能删除已完成或已取消的待办项
            if (!"COMPLETED".equals(todo.getStatus()) && !"CANCELLED".equals(todo.getStatus())) {
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
    public List<TodoItem> getUserTodos(Long userId, TodoStatus status, Priority priority) {
        return todoItemRepository.findByAssigneeIdAndDeleted(userId, 0)
                .stream()
                .filter(todo -> status == null || todo.getStatus().equals(convertStatusToString(status)))
                .filter(todo -> priority == null || todo.getPriority().equals(convertPriorityToString(priority)))
                .map(com.ccms.entity.system.user.TodoItem::toServiceItem)
                .collect(Collectors.toList());
    }
    
    @Override
    public TodoItem getTodoDetail(Long todoId) {
        com.ccms.entity.system.user.TodoItem todo = todoItemRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("待办项不存在: " + todoId));
        return todo.toServiceItem();
    }
    
    @Override
    public void checkAndMarkExpiredTodos() {
        try {
            LocalDateTime now = LocalDateTime.now();
            List<com.ccms.entity.system.user.TodoItem> expiredTodos = todoItemRepository.findByDeadlineBeforeAndStatusAndDeleted(now, "PENDING", 0);
            
            for (com.ccms.entity.system.user.TodoItem todo : expiredTodos) {
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
    public MessageNotifyService.NotifyResult sendTodoReminder(TodoItem serviceTodo) {
        try {
            // 创建Receiver列表
            List<MessageNotifyService.Receiver> receivers = Collections.singletonList(
                new MessageNotifyService.Receiver(
                    serviceTodo.getAssigneeId(),
                    serviceTodo.getAssigneeName(),
                    null, // email
                    null, // phone
                    null  // chatUserId
                )
            );
            
            MessageNotifyService.TodoMessage todoMessage = new MessageNotifyService.TodoMessage(
                serviceTodo.getTitle(), 
                generateTodoContent(serviceTodo), 
                receivers
            );
            
            // 设置额外属性
            todoMessage.setBizType(serviceTodo.getBizType());
            todoMessage.setBizId(serviceTodo.getBizId());
            todoMessage.setSourceType(serviceTodo.getSourceType());
            todoMessage.setSourceId(serviceTodo.getSourceId());
            todoMessage.setCreatedBy(serviceTodo.getCreatorId());
            
            // 设置TodoMessage特有属性
            if (serviceTodo.getDeadline() != null) {
                todoMessage.setDeadline(serviceTodo.getDeadline());
            }
            if (serviceTodo.getActionUrl() != null) {
                todoMessage.setActionUrl(serviceTodo.getActionUrl());
            }
            todoMessage.setPriority(convertPriorityToString(serviceTodo.getPriority()));
            
            return messageNotifyService.sendTodoReminder(todoMessage);
            
        } catch (Exception e) {
            return new MessageNotifyService.NotifyResult(false, "发送待办提醒失败: " + e.getMessage(), null, "TODO");
        }
    }
    
    @Override
    public TodoStatistics getTodoStatistics(Long userId) {
        TodoStatistics stats = new TodoStatistics();
        
        List<com.ccms.entity.system.user.TodoItem> userTodos = todoItemRepository.findByAssigneeIdAndDeleted(userId, 0);
        
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
    public BatchTodoOperationResult batchCreateTodos(List<TodoItem> serviceTodos) {
        BatchTodoOperationResult result = new BatchTodoOperationResult();
        int successCount = 0;
        int failureCount = 0;
        List<String> errorMessages = new ArrayList<>();
        
        for (int i = 0; i < serviceTodos.size(); i++) {
            TodoItem todo = serviceTodos.get(i);
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
    
    private void sendTodoCompletionNotification(TodoItem serviceTodo) {
        try {
            if (serviceTodo.getCreatorId() != null && !serviceTodo.getCreatorId().equals(serviceTodo.getAssigneeId())) {
                List<MessageNotifyService.Receiver> receivers = Collections.singletonList(
                    new MessageNotifyService.Receiver(
                        serviceTodo.getCreatorId(),
                        serviceTodo.getCreatorName(),
                        null, // email
                        null, // phone
                        null  // chatUserId
                    )
                );
                
                MessageNotifyService.InnerMessage message = new MessageNotifyService.InnerMessage(
                    "待办项已完成通知",
                    String.format("待办项 [%s] 已被处理人 [%s] 完成\\n备注: %s", 
                        serviceTodo.getTitle(), 
                        serviceTodo.getAssigneeName() != null ? serviceTodo.getAssigneeName() : "",
                        serviceTodo.getRemarks() != null ? serviceTodo.getRemarks() : "无"),
                    receivers
                );
                
                message.setMessageType("TODO_COMPLETED");
                message.setMsgLevel("INFO");
                message.setBizType(serviceTodo.getBizType());
                message.setBizId(serviceTodo.getBizId());
                message.setSourceType("TODO");
                message.setSourceId(serviceTodo.getTodoId());
                message.setCreatedBy(serviceTodo.getCreatorId());
                
                messageNotifyService.sendInnerMessage(message);
            }
        } catch (Exception e) {
            System.err.println("发送待办完成通知失败: " + e.getMessage());
        }
    }
    
    private void sendTodoExpirationNotification(TodoItem serviceTodo) {
        try {
            List<MessageNotifyService.Receiver> receivers = Collections.singletonList(
                new MessageNotifyService.Receiver(
                    serviceTodo.getAssigneeId(),
                    serviceTodo.getAssigneeName(),
                    null, // email
                    null, // phone
                    null  // chatUserId
                )
            );
            
            MessageNotifyService.InnerMessage message = new MessageNotifyService.InnerMessage(
                "待办项已过期通知",
                String.format("待办项 [%s] 已超期，系统已自动标记为已过期", serviceTodo.getTitle()),
                receivers
            );
            
            message.setMessageType("TODO_EXPIRED");
            message.setMsgLevel("WARNING");
            message.setBizType(serviceTodo.getBizType());
            message.setBizId(serviceTodo.getBizId());
            message.setSourceType("TODO");
            message.setSourceId(serviceTodo.getTodoId());
            message.setCreatedBy(serviceTodo.getCreatorId());
            
            messageNotifyService.sendInnerMessage(message);
            
        } catch (Exception e) {
            System.err.println("发送待办过期通知失败: " + e.getMessage());
        }
    }
    
    // 辅助转换方法
    private String convertStatusToString(TodoStatus status) {
        if (status == null) return "PENDING";
        
        switch (status) {
            case PROCESSING: return "PROCESSING";
            case COMPLETED: return "COMPLETED";
            case CANCELLED: return "CANCELLED";
            case EXPIRED: return "EXPIRED";
            default: return "PENDING";
        }
    }
    
    private String convertPriorityToString(Priority priority) {
        if (priority == null) return "NORMAL";
        
        switch (priority) {
            case LOW: return "LOW";
            case HIGH: return "HIGH";
            case URGENT: return "URGENT";
            default: return "NORMAL";
        }
    }
    
    // 添加缺失的辅助方法
    private boolean isValidTodo(TodoItem todo) {
        return todo != null && 
               todo.getContent() != null && 
               !todo.getContent().trim().isEmpty() &&
               todo.getAssigneeId() != null;
    }
    
    private void copyTodoProperties(TodoItem source, com.ccms.entity.system.user.TodoItem target) {
        if (source.getContent() != null) target.setContent(source.getContent());
        if (source.getAssigneeId() != null) target.setAssigneeId(source.getAssigneeId());
        if (source.getPriority() != null) target.setPriority(source.getPriority().toString());
        if (source.getDeadline() != null) target.setDeadline(source.getDeadline());
        if (source.getBizType() != null) target.setBizType(source.getBizType());
        if (source.getBizId() != null) target.setBizId(source.getBizId());
        if (source.getActionUrl() != null) target.setActionUrl(source.getActionUrl());
        target.setStatus("PENDING"); // 默认状态
    }
    

}