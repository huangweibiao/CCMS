package com.ccms.entity.message;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;

@Entity
@Table(name = "sys_message")
public class Message extends BaseEntity {
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;
    
    @Column(name = "sender_id")
    private Long senderId;
    
    @Column(name = "sender_name", length = 100)
    private String senderName;
    
    @Column(name = "receiver_id")
    private Long receiverId;
    
    @Column(name = "receiver_name", length = 100)
    private String receiverName;
    
    @Column(name = "message_type", length = 50)
    private String messageType;
    
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    @Column(name = "business_id")
    private Long businessId;
    
    @Column(name = "is_read", columnDefinition = "tinyint(1) default 0")
    private Boolean read = false;
    
    @Column(name = "read_time")
    private java.time.LocalDateTime readTime;
    
    @Column(name = "priority", length = 20)
    private String priority = "normal";
    
    @Column(name = "expire_time")
    private java.time.LocalDateTime expireTime;
    
    @Column(name = "status", length = 20)
    private String status = "active";

    // Manual getters and setters to fix compilation errors
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public java.time.LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(java.time.LocalDateTime readTime) {
        this.readTime = readTime;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public java.time.LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(java.time.LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}