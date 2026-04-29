package com.ccms.entity.todo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 待办事项实体
 */
@Entity
@Table(name = "todo_item")
public class TodoItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TodoType type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TodoStatus status = TodoStatus.PENDING;
    
    @Column(nullable = false)
    private Long relatedId;
    
    @Column(length = 100)
    private String relatedType;
    
    @Column
    private LocalDateTime deadline;
    
    @Column(nullable = false)
    private Integer priority = 3;
    
    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column
    private LocalDateTime completeTime;
    
    @Version
    private Long version;
    
    public enum TodoType {
        APPROVAL, // 审批待办
        PAYMENT,  // 付款待办
        AUDIT,    // 审核待办
        REMINDER  // 提醒待办
    }
    
    public enum TodoStatus {
        PENDING,    // 待处理
        PROCESSING, // 处理中
        COMPLETED,  // 已完成
        OVERDUE     // 已过期
    }
    
    // Constructors
    public TodoItem() {}
    
    public TodoItem(Long userId, String title, TodoType type, Long relatedId, String relatedType) {
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.relatedId = relatedId;
        this.relatedType = relatedType;
    }
    
    // Business methods
    public boolean isOverdue() {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }
    
    public boolean canComplete() {
        return status == TodoStatus.PENDING || status == TodoStatus.PROCESSING;
    }
    
    public void markCompleted() {
        if (canComplete()) {
            this.status = TodoStatus.COMPLETED;
            this.completeTime = LocalDateTime.now();
        }
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public TodoType getType() { return type; }
    public void setType(TodoType type) { this.type = type; }
    
    public TodoStatus getStatus() { return status; }
    public void setStatus(TodoStatus status) { this.status = status; }
    
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    
    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    
    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}