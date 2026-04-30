package com.ccms.service;

import com.ccms.entity.SysMessage;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 消息通知服务接口
 * 
 * @author 系统生成
 */
public interface MessageNotifyService {
    
    /**
     * 发送站内消息
     * 
     * @param message 消息内容
     * @return 发送结果
     */
    NotifyResult sendInnerMessage(InnerMessage message);
    
    /**
     * 发送邮件通知
     * 
     * @param emailMessage 邮件内容
     * @return 发送结果
     */
    NotifyResult sendEmail(EmailMessage emailMessage);
    
    /**
     * 发送短信通知
     * 
     * @param smsMessage 短信内容
     * @return 发送结果
     */
    NotifyResult sendSms(SmsMessage smsMessage);
    
    /**
     * 发送钉钉/企业微信通知
     * 
     * @param chatMessage 聊天消息
     * @return 发送结果
     */
    NotifyResult sendChatMessage(ChatMessage chatMessage);
    
    /**
     * 批量发送消息
     * 
     * @param messages 消息列表
     * @return 发送结果
     */
    BatchNotifyResult batchSendMessages(List<Message> messages);
    
    /**
     * 发送待办提醒
     * 
     * @param todoMessage 待办消息
     * @return 发送结果
     */
    NotifyResult sendTodoReminder(TodoMessage todoMessage);
    
    /**
     * 获取用户未读消息数量
     * 
     * @param userId 用户ID
     * @return 未读消息统计
     */
    UnreadStats getUnreadStatistics(Long userId);
    
    /**
     * 标记消息为已读
     * 
     * @param messageIds 消息ID列表
     * @param userId 用户ID
     * @return 标记结果
     */
    MarkResult markMessagesAsRead(List<Long> messageIds, Long userId);
    
    /**
     * 获取消息模板
     * 
     * @param templateCode 模板代码
     * @return 模板内容
     */
    MessageTemplate getMessageTemplate(String templateCode);
    
    /**
     * 根据模板发送消息
     * 
     * @param templateCode 模板代码
     * @param params 模板参数
     * @param receivers 接收者列表
     * @return 发送结果
     */
    BatchNotifyResult sendTemplateMessage(String templateCode, Map<String, Object> params, List<Receiver> receivers);
    
    /**
     * 处理延迟消息
     */
    void processDelayedMessages();
    
    // ========== 消息类型定义 ==========
    
    /**
     * 消息发送结果
     */
    class NotifyResult {
        private final boolean success;
        private final String message;
        private final Long messageId;
        private final String channel;
        
        public NotifyResult(boolean success, String message, Long messageId, String channel) {
            this.success = success;
            this.message = message;
            this.messageId = messageId;
            this.channel = channel;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Long getMessageId() { return messageId; }
        public String getChannel() { return channel; }
    }
    
    /**
     * 批量发送结果
     */
    class BatchNotifyResult {
        private final int totalCount;
        private final int successCount;
        private final int failedCount;
        private final List<NotifyResult> results;
        
        public BatchNotifyResult(int totalCount, int successCount, int failedCount, List<NotifyResult> results) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.results = results != null ? results : java.util.Collections.emptyList();
        }
        
        public int getTotalCount() { return totalCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailedCount() { return failedCount; }
        public List<NotifyResult> getResults() { return results; }
    }
    
    /**
     * 未读消息统计
     */
    class UnreadStats {
        private final Long userId;
        private final int totalUnread;
        private final int urgentUnread;
        private final int todoCount;
        private final Map<String, Integer> typeUnreadCount;
        
        public UnreadStats(Long userId, int totalUnread, int urgentUnread, int todoCount, Map<String, Integer> typeUnreadCount) {
            this.userId = userId;
            this.totalUnread = totalUnread;
            this.urgentUnread = urgentUnread;
            this.todoCount = todoCount;
            this.typeUnreadCount = typeUnreadCount != null ? typeUnreadCount : java.util.Collections.emptyMap();
        }
        
        public Long getUserId() { return userId; }
        public int getTotalUnread() { return totalUnread; }
        public int getUrgentUnread() { return urgentUnread; }
        public int getTodoCount() { return todoCount; }
        public Map<String, Integer> getTypeUnreadCount() { return typeUnreadCount; }
    }
    
    /**
     * 标记操作结果
     */
    class MarkResult {
        private final boolean success;
        private final int markedCount;
        private final String message;
        
        public MarkResult(boolean success, int markedCount, String message) {
            this.success = success;
            this.markedCount = markedCount;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public int getMarkedCount() { return markedCount; }
        public String getMessage() { return message; }
    }
    
    /**
     * 消息模板
     */
    class MessageTemplate {
        private final Long id;
        private final String templateCode;
        private final String templateName;
        private final String titleTemplate;
        private final String contentTemplate;
        private final String messageType;
        private final Map<String, Object> defaultParams;
        
        public MessageTemplate(Long id, String templateCode, String templateName, 
                              String titleTemplate, String contentTemplate, 
                              String messageType, Map<String, Object> defaultParams) {
            this.id = id;
            this.templateCode = templateCode;
            this.templateName = templateName;
            this.titleTemplate = titleTemplate;
            this.contentTemplate = contentTemplate;
            this.messageType = messageType;
            this.defaultParams = defaultParams != null ? defaultParams : java.util.Collections.emptyMap();
        }
        
        public Long getId() { return id; }
        public String getTemplateCode() { return templateCode; }
        public String getTemplateName() { return templateName; }
        public String getTitleTemplate() { return titleTemplate; }
        public String getContentTemplate() { return contentTemplate; }
        public String getMessageType() { return messageType; }
        public Map<String, Object> getDefaultParams() { return defaultParams; }
    }
    
    /**
     * 接收者信息
     */
    class Receiver {
        private final Long userId;
        private final String userName;
        private final String email;
        private final String phone;
        private final String chatUserId;
        
        public Receiver(Long userId, String userName, String email, String phone, String chatUserId) {
            this.userId = userId;
            this.userName = userName;
            this.email = email;
            this.phone = phone;
            this.chatUserId = chatUserId;
        }
        
        public Long getUserId() { return userId; }
        public String getUserName() { return userName; }
        public String getEmail() { return email; }
        public String getPhone() { return phone; }
        public String getChatUserId() { return chatUserId; }
    }
    
    /**
     * 消息基类
     */
    abstract class Message {
        protected String title;
        protected String content;
        protected String messageType;
        protected String msgLevel;
        protected List<Receiver> receivers;
        protected String bizType;
        protected Long bizId;
        protected Map<String, Object> bizData;
        protected String sourceType;
        protected Long sourceId;
        protected Long createdBy;
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getMessageType() { return messageType; }
        public void setMessageType(String messageType) { this.messageType = messageType; }
        
        public String getMsgLevel() { return msgLevel; }
        public void setMsgLevel(String msgLevel) { this.msgLevel = msgLevel; }
        
        public List<Receiver> getReceivers() { return receivers; }
        public void setReceivers(List<Receiver> receivers) { this.receivers = receivers; }
        
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public Long getBizId() { return bizId; }
        public void setBizId(Long bizId) { this.bizId = bizId; }
        
        public Map<String, Object> getBizData() { return bizData; }
        public void setBizData(Map<String, Object> bizData) { this.bizData = bizData; }
        
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        
        public Long getSourceId() { return sourceId; }
        public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
        
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    }
    
    /**
     * 站内消息
     */
    class InnerMessage extends Message {
        private boolean requireConfirm;
        private String confirmMethod;
        private Long expireHours;
        
        public InnerMessage(String title, String content, List<Receiver> receivers) {
            this.title = title;
            this.content = content;
            this.receivers = receivers;
            this.messageType = "SYSTEM";
            this.msgLevel = "INFO";
        }
        
        public boolean isRequireConfirm() { return requireConfirm; }
        public void setRequireConfirm(boolean requireConfirm) { this.requireConfirm = requireConfirm; }
        
        public String getConfirmMethod() { return confirmMethod; }
        public void setConfirmMethod(String confirmMethod) { this.confirmMethod = confirmMethod; }
        
        public Long getExpireHours() { return expireHours; }
        public void setExpireHours(Long expireHours) { this.expireHours = expireHours; }
    }
    
    /**
     * 邮件消息
     */
    class EmailMessage extends Message {
        private String subject;
        private String htmlContent;
        private List<String> attachments;
        private String replyTo;
        private String cc;
        private String bcc;
        
        public EmailMessage(String subject, String content, List<Receiver> receivers) {
            this.subject = subject;
            this.content = content;
            this.receivers = receivers;
            this.messageType = "NOTIFICATION";
            this.msgLevel = "INFO";
        }
        
        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }
        
        public String getHtmlContent() { return htmlContent; }
        public void setHtmlContent(String htmlContent) { this.htmlContent = htmlContent; }
        
        public List<String> getAttachments() { return attachments; }
        public void setAttachments(List<String> attachments) { this.attachments = attachments; }
        
        public String getReplyTo() { return replyTo; }
        public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
        
        public String getCc() { return cc; }
        public void setCc(String cc) { this.cc = cc; }
        
        public String getBcc() { return bcc; }
        public void setBcc(String bcc) { this.bcc = bcc; }
    }
    
    /**
     * 短信消息
     */
    class SmsMessage extends Message {
        private String templateCode;
        private Map<String, String> templateParams;
        private String signName;
        
        public SmsMessage(String content, List<Receiver> receivers) {
            this.content = content;
            this.receivers = receivers;
            this.messageType = "REMINDER";
            this.msgLevel = "INFO";
        }
        
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        
        public Map<String, String> getTemplateParams() { return templateParams; }
        public void setTemplateParams(Map<String, String> templateParams) { this.templateParams = templateParams; }
        
        public String getSignName() { return signName; }
        public void setSignName(String signName) { this.signName = signName; }
    }
    
    /**
     * 聊天消息（钉钉/企业微信）
     */
    class ChatMessage extends Message {
        private String chatType; // DINGDING/WECHAT_WORK
        private String msgType;  // TEXT/LINK/MARKDOWN
        private String url;
        private String picUrl;
        private String btnOrientation; // 按钮排列方向
        private List<Button> buttons;
        
        public ChatMessage(String title, String content, List<Receiver> receivers) {
            this.title = title;
            this.content = content;
            this.receivers = receivers;
            this.messageType = "ALERT";
            this.msgLevel = "INFO";
        }
        
        public String getChatType() { return chatType; }
        public void setChatType(String chatType) { this.chatType = chatType; }
        
        public String getMsgType() { return msgType; }
        public void setMsgType(String msgType) { this.msgType = msgType; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getPicUrl() { return picUrl; }
        public void setPicUrl(String picUrl) { this.picUrl = picUrl; }
        
        public String getBtnOrientation() { return btnOrientation; }
        public void setBtnOrientation(String btnOrientation) { this.btnOrientation = btnOrientation; }
        
        public List<Button> getButtons() { return buttons; }
        public void setButtons(List<Button> buttons) { this.buttons = buttons; }
    }
    
    /**
     * 待办提醒消息
     */
    class TodoMessage extends Message {
        private String todoType;
        private LocalDateTime deadline;
        private String actionUrl;
        private String priority; // LOW/NORMAL/HIGH/URGENT
        
        public TodoMessage(String title, String content, List<Receiver> receivers) {
            this.title = title;
            this.content = content;
            this.receivers = receivers;
            this.messageType = "TODO";
            this.msgLevel = "INFO";
        }
        
        public String getTodoType() { return todoType; }
        public void setTodoType(String todoType) { this.todoType = todoType; }
        
        public LocalDateTime getDeadline() { return deadline; }
        public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
        
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
        
        public String getPriority() { return priority; }
        public void setPriority(String priority) { this.priority = priority; }
    }
    
    /**
     * 聊天消息按钮
     */
    class Button {
        private final String title;
        private final String actionUrl;
        
        public Button(String title, String actionUrl) {
            this.title = title;
            this.actionUrl = actionUrl;
        }
        
        public String getTitle() { return title; }
        public String getActionUrl() { return actionUrl; }
    }
}