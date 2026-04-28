package com.ccms.entity.message;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
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
}