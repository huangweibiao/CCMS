package com.ccms.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 基础实体类
 * 为所有实体类提供统一的基础字段和管理
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    
    /**
     * 乐观锁版本号
     */
    @Version
    protected Integer version;
    
    /**
     * 删除标记 (0-正常, 1-已删除)
     */
    @Column(name = "is_deleted", columnDefinition = "tinyint(1) default 0")
    protected Boolean deleted = false;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    protected LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    protected LocalDateTime updateTime;
    
    /**
     * 创建人ID
     */
    @CreatedBy
    @Column(name = "create_by")
    protected Long createBy;
    
    /**
     * 更新人ID
     */
    @LastModifiedBy
    @Column(name = "update_by")
    protected Long updateBy;

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}