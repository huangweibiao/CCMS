package com.ccms.entity.message;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 消息表实体类
 * 对应表名：ccms_sys_message
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_message")
public class SysMessage extends BaseEntity {

    /**
     * 接收人ID
     */
    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;
    
    /**
     * 消息类型：1-待办 2-提醒 3-通知
     */
    @Column(name = "message_type", nullable = false)
    private Integer messageType;
    
    /**
     * 消息标题
     */
    @Column(name = "title", length = 128, nullable = false)
    private String title;
    
    /**
     * 消息内容
     */
    @Column(name = "content", length = 512, nullable = false)
    private String content;
    
    /**
     * 业务ID
     */
    @Column(name = "business_id")
    private Long businessId;
    
    /**
     * 业务类型
     */
    @Column(name = "business_type", length = 32)
    private String businessType;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    @Column(name = "is_read", nullable = false)
    private Integer isRead;
    
    /**
     * 阅读时间
     */
    @Column(name = "read_time")
    private LocalDateTime readTime;

    // Getters and Setters
    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

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

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public void setReadTime(LocalDateTime readTime) {
        this.readTime = readTime;
    }

    @Override
    public String toString() {
        return "SysMessage{" +
                "id=" + getId() +
                ", receiverId=" + receiverId +
                ", messageType=" + messageType +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", businessId=" + businessId +
                ", businessType='" + businessType + '\'' +
                ", isRead=" + isRead +
                ", readTime=" + readTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}
