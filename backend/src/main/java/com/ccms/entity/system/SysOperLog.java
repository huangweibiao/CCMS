package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 操作日志表实体类
 * 对应表名：ccms_sys_oper_log
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_oper_log")
public class SysOperLog extends BaseEntity {

    /**
     * 操作人ID
     */
    @Column(name = "oper_user_id", nullable = false)
    private Long operUserId;
    
    /**
     * 操作模块
     */
    @Column(name = "oper_module", length = 64, nullable = false)
    private String operModule;
    
    /**
     * 操作类型：新增/修改/删除/查询/审批
     */
    @Column(name = "oper_type", length = 32, nullable = false)
    private String operType;
    
    /**
     * 操作内容
     */
    @Column(name = "oper_content", length = 1024)
    private String operContent;
    
    /**
     * 业务ID
     */
    @Column(name = "business_id")
    private Long businessId;
    
    /**
     * 操作IP
     */
    @Column(name = "oper_ip", length = 64)
    private String operIp;
    
    /**
     * 操作时间
     */
    @Column(name = "oper_time", nullable = false)
    private LocalDateTime operTime;

    // Getters and Setters
    public Long getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(Long operUserId) {
        this.operUserId = operUserId;
    }

    public String getOperModule() {
        return operModule;
    }

    public void setOperModule(String operModule) {
        this.operModule = operModule;
    }

    public String getOperType() {
        return operType;
    }

    public void setOperType(String operType) {
        this.operType = operType;
    }

    public String getOperContent() {
        return operContent;
    }

    public void setOperContent(String operContent) {
        this.operContent = operContent;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getOperIp() {
        return operIp;
    }

    public void setOperIp(String operIp) {
        this.operIp = operIp;
    }

    public LocalDateTime getOperTime() {
        return operTime;
    }

    public void setOperTime(LocalDateTime operTime) {
        this.operTime = operTime;
    }

    @Override
    public String toString() {
        return "SysOperLog{" +
                "id=" + getId() +
                ", operUserId=" + operUserId +
                ", operModule='" + operModule + '\'' +
                ", operType='" + operType + '\'' +
                ", operContent='" + operContent + '\'' +
                ", businessId=" + businessId +
                ", operIp='" + operIp + '\'' +
                ", operTime=" + operTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}