package com.ccms.entity.system.user;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 待办项实体类
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_todo_item")
public class TodoItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long todoId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "todo_type", length = 50)
    private String todoType;
    
    @Column(length = 20)
    private String priority; // LOW, NORMAL, HIGH, URGENT
    
    @Column(name = "assignee_id", nullable = false)
    private Long assigneeId;
    
    @Column(name = "assignee_name", length = 100)
    private String assigneeName;
    
    @Column(name = "creator_id", nullable = false)
    private Long creatorId;
    
    @Column(name = "creator_name", length = 100)
    private String creatorName;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "deadline")
    private LocalDateTime deadline;
    
    @Column(length = 20)
    private String status; // PENDING, PROCESSING, COMPLETED, CANCELLED, EXPIRED
    
    @Column(name = "action_url", length = 500)
    private String actionUrl;
    
    @Column(name = "biz_type", length = 50)
    private String bizType;
    
    @Column(name = "biz_id")
    private Long bizId;
    
    @Column(name = "source_type", length = 50)
    private String sourceType;
    
    @Column(name = "source_id")
    private Long sourceId;
    
    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;
    
    @Column(name = "completed_time")
    private LocalDateTime completedTime;
    
    @Column(name = "remarks", length = 500)
    private String remarks;
    
    @Column(name = "deleted", nullable = false)
    private Integer deleted = 0;
    
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // 构造方法
    public TodoItem() {
        this.createdTime = LocalDateTime.now();
        this.deleted = 0;
    }
    
    // Getter和Setter方法
    public Long getTodoId() { return todoId; }
    public void setTodoId(Long todoId) { this.todoId = todoId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getTodoType() { return todoType; }
    public void setTodoType(String todoType) { this.todoType = todoType; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
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
    
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    
    // 业务方法
    public void markAsProcessing() {
        this.status = "PROCESSING";
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.completedTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsCancelled() {
        this.status = "CANCELLED";
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsExpired() {
        this.status = "EXPIRED";
        this.updatedTime = LocalDateTime.now();
    }
    
    // 校验方法
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() 
                && assigneeId != null && creatorId != null;
    }
    
    public boolean isExpired() {
        return deadline != null && LocalDateTime.now().isAfter(deadline) 
                && "PENDING".equals(status);
    }
    
    // 转换方法
    public com.ccms.service.TodoReminderService.TodoItem toServiceItem() {
        com.ccms.service.TodoReminderService.TodoItem item = 
            new com.ccms.service.TodoReminderService.TodoItem(title, content, assigneeId);
        
        item.setTodoId(todoId);
        item.setTodoType(todoType);
        item.setPriority(convertPriority(priority));
        item.setAssigneeName(assigneeName);
        item.setCreatorId(creatorId);
        item.setCreatorName(creatorName);
        item.setCreatedTime(createdTime);
        item.setDeadline(deadline);
        item.setStatus(convertStatus(status));
        item.setActionUrl(actionUrl);
        item.setBizType(bizType);
        item.setBizId(bizId);
        item.setSourceType(sourceType);
        item.setSourceId(sourceId);
        item.setEstimatedMinutes(estimatedMinutes);
        item.setCompletedTime(completedTime);
        item.setRemarks(remarks);
        
        return item;
    }
    
    // 从服务对象转换
    public void fromServiceItem(com.ccms.service.TodoReminderService.TodoItem serviceItem) {
        this.title = serviceItem.getTitle();
        this.content = serviceItem.getContent();
        this.todoType = serviceItem.getTodoType();
        this.priority = convertPriorityToString(serviceItem.getPriority());
        this.assigneeId = serviceItem.getAssigneeId();
        this.assigneeName = serviceItem.getAssigneeName();
        this.creatorId = serviceItem.getCreatorId();
        this.creatorName = serviceItem.getCreatorName();
        this.deadline = serviceItem.getDeadline();
        this.status = convertStatusToString(serviceItem.getStatus());
        this.actionUrl = serviceItem.getActionUrl();
        this.bizType = serviceItem.getBizType();
        this.bizId = serviceItem.getBizId();
        this.sourceType = serviceItem.getSourceType();
        this.sourceId = serviceItem.getSourceId();
        this.estimatedMinutes = serviceItem.getEstimatedMinutes();
        this.remarks = serviceItem.getRemarks();
        
        if (serviceItem.getCreatedTime() != null) {
            this.createdTime = serviceItem.getCreatedTime();
        }
    }
    
    // 辅助方法
    private com.ccms.service.TodoReminderService.TodoStatus convertStatus(String status) {
        if (status == null) return com.ccms.service.TodoReminderService.TodoStatus.PENDING;
        
        switch (status) {
            case "PROCESSING": return com.ccms.service.TodoReminderService.TodoStatus.PROCESSING;
            case "COMPLETED": return com.ccms.service.TodoReminderService.TodoStatus.COMPLETED;
            case "CANCELLED": return com.ccms.service.TodoReminderService.TodoStatus.CANCELLED;
            case "EXPIRED": return com.ccms.service.TodoReminderService.TodoStatus.EXPIRED;
            default: return com.ccms.service.TodoReminderService.TodoStatus.PENDING;
        }
    }
    
    private String convertStatusToString(com.ccms.service.TodoReminderService.TodoStatus status) {
        if (status == null) return "PENDING";
        
        switch (status) {
            case PROCESSING: return "PROCESSING";
            case COMPLETED: return "COMPLETED";
            case CANCELLED: return "CANCELLED";
            case EXPIRED: return "EXPIRED";
            default: return "PENDING";
        }
    }
    
    private com.ccms.service.TodoReminderService.Priority convertPriority(String priority) {
        if (priority == null) return com.ccms.service.TodoReminderService.Priority.NORMAL;
        
        switch (priority) {
            case "LOW": return com.ccms.service.TodoReminderService.Priority.LOW;
            case "HIGH": return com.ccms.service.TodoReminderService.Priority.HIGH;
            case "URGENT": return com.ccms.service.TodoReminderService.Priority.URGENT;
            default: return com.ccms.service.TodoReminderService.Priority.NORMAL;
        }
    }
    
    private String convertPriorityToString(com.ccms.service.TodoReminderService.Priority priority) {
        if (priority == null) return "NORMAL";
        
        switch (priority) {
            case LOW: return "LOW";
            case HIGH: return "HIGH";
            case URGENT: return "URGENT";
            default: return "NORMAL";
        }
    }
}
