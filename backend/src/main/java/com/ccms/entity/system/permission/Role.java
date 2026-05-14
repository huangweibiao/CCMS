package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 角色实体类
 * 对应表名：ccms_sys_role
 * 基于D:\\aitols\\base-app项目的权限体系进行标准化
 */
@Entity
@Table(name = "ccms_sys_role")
public class Role extends BaseEntity {

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
     * 角色描述
     */
    @Column(name = "role_desc", length = 256)
    private String roleDesc;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 1")
    private Integer status = 1;
    
    /**
     * 数据范围：1-全部数据 2-本部门数据 3-自定义数据
     */
    @Column(name = "data_scope", nullable = false, columnDefinition = "tinyint default 2")
    private Integer dataScope = 2;
    
    /**
     * 权限列表（逗号分隔的权限编码）
     */
    @Column(name = "permissions", length = 2000)
    private String permissions;
    
    /**
     * 角色所属数据范围，便于数据权限控制
     */
    @Column(name = "data_permission", length = 100)
    private String dataPermission;

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

    public String getRoleDesc() {
        return roleDesc;
    }

    public void setRoleDesc(String roleDesc) {
        this.roleDesc = roleDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDataScope() {
        return dataScope;
    }

    public void setDataScope(Integer dataScope) {
        this.dataScope = dataScope;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public String getDataPermission() {
        return dataPermission;
    }

    public void setDataPermission(String dataPermission) {
        this.dataPermission = dataPermission;
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", roleCode='" + roleCode + '\'' +
                ", roleName='" + roleName + '\'' +
                ", roleDesc='" + roleDesc + '\'' +
                ", status=" + status +
                ", dataScope=" + dataScope +
                ", permissions='" + permissions + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }

    /**
     * 检查是否是超级管理员角色
     */
    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(this.roleCode);
    }
}
