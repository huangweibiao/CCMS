package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 角色权限关联表实体类
 * 对应表名：ccms_sys_role_perm
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_role_perm")
public class SysRolePerm extends BaseEntity {

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    /**
     * 权限ID
     */
    @Column(name = "perm_id", nullable = false)
    private Long permId;

    // Getters and Setters
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getPermId() {
        return permId;
    }

    public void setPermId(Long permId) {
        this.permId = permId;
    }

    @Override
    public String toString() {
        return "SysRolePerm{" +
                "id=" + getId() +
                ", roleId=" + roleId +
                ", permId=" + permId +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}