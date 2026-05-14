package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 角色菜单关联实体类
 * 对应表名：ccms_sys_role_menu
 * 管理角色和菜单的关联关系，支持角色的菜单权限分配
 */
@Entity
@Table(name = "ccms_sys_role_menu")
public class RoleMenu extends BaseEntity {

    /**
     * 角色ID
     */
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    /**
     * 菜单ID
     */
    @Column(name = "menu_id", nullable = false)
    private Long menuId;
    
    /**
     * 权限类型：0-可见 1-可操作 2-可管理
     */
    @Column(name = "permission_type", nullable = false, columnDefinition = "tinyint default 0")
    private Integer permissionType = 0;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 1")
    private Integer status = 1;
    
    /**
     * 关联的角色实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;
    
    /**
     * 关联的菜单实体
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", insertable = false, updatable = false)
    private Menu menu;

    // 构造器
    public RoleMenu() {}

    public RoleMenu(Long roleId, Long menuId, Integer permissionType) {
        this.roleId = roleId;
        this.menuId = menuId;
        this.permissionType = permissionType;
    }

    // Getters and Setters
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }

    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    /**
     * 检查是否有查看权限
     */
    @Transient
    public boolean hasViewPermission() {
        return permissionType >= 0 && status == 1;
    }
    
    /**
     * 检查是否有操作权限
     */
    @Transient
    public boolean hasOperatePermission() {
        return permissionType >= 1 && status == 1;
    }
    
    /**
     * 检查是否有管理权限
     */
    @Transient
    public boolean hasManagePermission() {
        return permissionType >= 2 && status == 1;
    }
    
    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        VIEW("查看权限", 0),
        OPERATE("操作权限", 1),
        MANAGE("管理权限", 2);

        private final String description;
        private final int value;

        PermissionType(String description, int value) {
            this.description = description;
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public int getValue() {
            return value;
        }
    }
}