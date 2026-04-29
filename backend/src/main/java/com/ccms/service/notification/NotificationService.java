package com.ccms.service.notification;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface NotificationService {
    
    /**
     * 发送邮件通知
     */
    void sendEmail(String to, String subject, String content);
    
    /**
     * 发送站内消息
     */
    void sendInternalMessage(Long userId, String subject, String content);
    
    /**
     * 发送待办提醒
     */
    void sendTodoReminder(Long userId, String todoType, String content);
    
    /**
     * 发送审批结果通知
     */
    void sendApprovalResult(Long userId, String result, String applyInfo);
    
    /**
     * 批量发送通知
     */
    void batchSendNotifications(List<Notification> notifications);
    
    /**
     * 获取用户未读消息数量
     */
    Integer getUnreadCount(Long userId);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long notificationId);
    
    class Notification {
        private Long userId;
        private String type;
        private String title;
        private String content;
        private Boolean read = false;
        
        public Notification(Long userId, String type, String title) {
            this.userId = userId;
            this.type = type;
            this.title = title;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Boolean getRead() { return read; }
        public void setRead(Boolean read) { this.read = read; }
    }
}