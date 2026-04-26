package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 角色表实体类
 * 对应表名：ccms_sys_role
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_role")
public class SysRole extends BaseEntity {

    /**
     * 角色编码
     */
    @Column(name = "role_code", length = 32, nullable = false)
    private String roleCode;
    
    /**
     * 角色名称
     */
    @Column(name = "role_name", length = 64, nullable = false)
    private String roleName;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    // Getters and Setters
    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SysRole{" +
                "id=" + getId() +
                ", roleCode='" + roleCode + '\'' +
                ", roleName='" + roleName + '\'' +
                ", status=" + status +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}