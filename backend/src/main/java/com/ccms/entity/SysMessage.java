package com.ccms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统消息表实体
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "sys_message")
public class SysMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** 消息标题 */
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    /** 消息内容 */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;
    
    /** 消息类型：SYSTEM/APPROVAL/REMINDER/ALERT */
    @Column(name = "msg_type", length = 50, nullable = false)
    private String msgType;
    
    /** 消息级别：INFO/WARNING/ERROR/URGENT */
    @Column(name = "msg_level", length = 20, nullable = false)
    private String msgLevel;
    
    /** 接收者用户ID */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    /** 接收者用户名 */
    @Column(name = "receiver_name", length = 100)
    private String receiverName;
    
    /** 发送者用户ID（系统消息为0） */
    @Column(name = "sender_id")
    private Long senderId;
    
    /** 发送者用户名 */
    @Column(name = "sender_name", length = 100)
    private String senderName;
    
    /** 关联业务类型：REIMBURSE/LOAN/INVOICE 等 */
    @Column(name = "biz_type", length = 50)
    private String bizType;
    
    /** 关联业务ID */
    @Column(name = "biz_id")
    private Long bizId;
    
    /** 业务数据（JSON格式，存储额外业务信息） */
    @Column(name = "biz_data", columnDefinition = "TEXT")
    private String bizData;
    
    /** 阅读状态：UNREAD/READ */
    @Column(name = "read_status", length = 20, nullable = false)
    private String readStatus;
    
    /** 消息状态：DRAFT/SENT/DELETED */
    @Column(name = "msg_status", length = 20, nullable = false)
    private String msgStatus;
    
    /** 阅读时间 */
    @Column(name = "read_time")
    private LocalDateTime readTime;
    
    /** 发送方式：INNER/EMAIL/SMS/DINGTALK 等 */
    @Column(name = "send_method", length = 50, nullable = false)
    private String sendMethod;
    
    /** 发送状态：PENDING/SENDING/SENT/FAILED */
    @Column(name = "send_status", length = 20, nullable = false)
    private String sendStatus;
    
    /** 发送时间 */
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    
    /** 计划发送时间 */
    @Column(name = "plan_send_time")
    private LocalDateTime planSendTime;
    
    /** 过期时间 */
    @Column(name = "expire_time")
    private LocalDateTime expireTime;
    
    /** 重试次数 */
    @Column(name = "retry_count")
    private Integer retryCount;
    
    /** 最后重试时间 */
    @Column(name = "last_retry_time")
    private LocalDateTime lastRetryTime;
    
    /** 错误信息 */
    @Column(name = "error_msg", columnDefinition = "TEXT")
    private String errorMsg;
    
    /** 是否确认：0-未确认，1-已确认 */
    @Column(name = "is_confirmed")
    private Boolean isConfirmed;
    
    /** 确认时间 */
    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;
    
    /** 确认方式：AUTO/MANUAL */
    @Column(name = "confirm_method", length = 20)
    private String confirmMethod;
    
    /** 消息模板ID */
    @Column(name = "template_id")
    private Long templateId;
    
    /** 消息模板参数（JSON格式） */
    @Column(name = "template_params", columnDefinition = "TEXT")
    private String templateParams;
    
    /** 消息来源：SYSTEM/MANUAL/API */
    @Column(name = "source_type", length = 50)
    private String sourceType;
    
    /** 来源标识 */
    @Column(name = "source_id")
    private Long sourceId;
    
    /** 创建人 */
    @Column(name = "created_by")
    private Long createdBy;
    
    /** 创建时间 */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    /** 更新人 */
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /** 更新时间 */
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    /** 版本号（乐观锁） */
    @Version
    @Column(name = "version")
    private Long version;
    
    /** 删除标识：0-正常，1-删除 */
    @Column(name = "deleted")
    private Integer deleted;
    
    /** 租户ID（预留多租户支持） */
    @Column(name = "tenant_id")
    private Long tenantId;
    
    // 构造函数
    public SysMessage() {
        this.readStatus = "UNREAD";
        this.msgStatus = "DRAFT";
        this.sendStatus = "PENDING";
        this.sendMethod = "INNER";
        this.retryCount = 0;
        this.isConfirmed = false;
        this.deleted = 0;
        this.createdTime = LocalDateTime.now();
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    
    public String getMsgLevel() { return msgLevel; }
    public void setMsgLevel(String msgLevel) { this.msgLevel = msgLevel; }
    
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    
    public Long getBizId() { return bizId; }
    public void setBizId(Long bizId) { this.bizId = bizId; }
    
    public String getBizData() { return bizData; }
    public void setBizData(String bizData) { this.bizData = bizData; }
    
    public String getReadStatus() { return readStatus; }
    public void setReadStatus(String readStatus) { this.readStatus = readStatus; }
    
    public String getMsgStatus() { return msgStatus; }
    public void setMsgStatus(String msgStatus) { this.msgStatus = msgStatus; }
    
    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime readTime) { this.readTime = readTime; }
    
    public String getSendMethod() { return sendMethod; }
    public void setSendMethod(String sendMethod) { this.sendMethod = sendMethod; }
    
    public String getSendStatus() { return sendStatus; }
    public void setSendStatus(String sendStatus) { this.sendStatus = sendStatus; }
    
    public LocalDateTime getSendTime() { return sendTime; }
    public void setSendTime(LocalDateTime sendTime) { this.sendTime = sendTime; }
    
    public LocalDateTime getPlanSendTime() { return planSendTime; }
    public void setPlanSendTime(LocalDateTime planSendTime) { this.planSendTime = planSendTime; }
    
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    
    public LocalDateTime getLastRetryTime() { return lastRetryTime; }
    public void setLastRetryTime(LocalDateTime lastRetryTime) { this.lastRetryTime = lastRetryTime; }
    
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
    
    public Boolean getIsConfirmed() { return isConfirmed; }
    public void setIsConfirmed(Boolean isConfirmed) { this.isConfirmed = isConfirmed; }
    
    public LocalDateTime getConfirmTime() { return confirmTime; }
    public void setConfirmTime(LocalDateTime confirmTime) { this.confirmTime = confirmTime; }
    
    public String getConfirmMethod() { return confirmMethod; }
    public void setConfirmMethod(String confirmMethod) { this.confirmMethod = confirmMethod; }
    
    public Long getTemplateId() { return templateId; }
    public void setTemplateId(Long templateId) { this.templateId = templateId; }
    
    public String getTemplateParams() { return templateParams; }
    public void setTemplateParams(String templateParams) { this.templateParams = templateParams; }
    
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    
    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
    
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    
    // 业务方法
    public void markAsRead() {
        this.readStatus = "READ";
        this.readTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsUnread() {
        this.readStatus = "UNREAD";
        this.readTime = null;
        this.updatedTime = LocalDateTime.now();
    }
    
    public void confirm(String method) {
        this.isConfirmed = true;
        this.confirmMethod = method;
        this.confirmTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsSent() {
        this.msgStatus = "SENT";
        this.sendStatus = "SENT";
        this.sendTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
    
    public void markAsFailed(String error) {
        this.sendStatus = "FAILED";
        this.errorMsg = error;
        this.retryCount = (this.retryCount != null ? this.retryCount : 0) + 1;
        this.lastRetryTime = LocalDateTime.now();
        this.updatedTime = LocalDateTime.now();
    }
}