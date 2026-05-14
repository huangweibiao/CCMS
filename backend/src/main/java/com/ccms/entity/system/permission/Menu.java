package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 菜单实体类
 * 对应表名：ccms_sys_menu
 * 基于D:\aitols\base-app项目的权限体系进行标准化
 */
@Entity
@Table(name = "ccms_sys_menu")
public class Menu extends BaseEntity {

    /**
     * 菜单名称
     */
    @Column(name = "menu_name", length = 64, nullable = false)
    private String menuName;
    
    /**
     * 菜单类型：DIR-目录 MENU-菜单 BUTTON-按钮
     */
    @Column(name = "menu_type", length = 20, nullable = false)
    private String menuType;
    
    /**
     * 路由路径
     */
    @Column(name = "path", length = 200)
    private String path;
    
    /**
     * 组件路径
     */
    @Column(name = "component", length = 200)
    private String component;
    
    /**
     * 权限标识
     */
    @Column(name = "permission_code", length = 100)
    private String permissionCode;
    
    /**
     * 图标
     */
    @Column(name = "icon", length = 100)
    private String icon;
    
    /**
     * 排序
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;
    
    /**
     * 是否可见
     */
    @Column(name = "visible", nullable = false)
    private Boolean visible = true;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 1")
    private Integer status = 1;
    
    /**
     * 父菜单ID
     */
    @Column(name = "parent_id", nullable = false, columnDefinition = "bigint default 0")
    private Long parentId = 0L;
    
    /**
     * 菜单备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

    // 构造器
    public Menu() {}

    public Menu(String menuName, String menuType, String path, Integer sortOrder) {
        this.menuName = menuName;
        this.menuType = menuType;
        this.path = path;
        this.sortOrder = sortOrder;
    }

    // Getters and Setters
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getVisible() { return visible; }
    public void setVisible(Boolean visible) { this.visible = visible; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    
    /**
     * 检查是否是目录
     */
    @Transient
    public boolean isDir() {
        return "DIR".equals(menuType);
    }
    
    /**
     * 检查是否是菜单项
     */
    @Transient
    public boolean isMenu() {
        return "MENU".equals(menuType);
    }
    
    /**
     * 检查是否是按钮权限
     */
    @Transient
    public boolean isButton() {
        return "BUTTON".equals(menuType);
    }
    
    /**
     * 检查是否是一级菜单
     */
    @Transient
    public boolean isTopMenu() {
        return parentId == 0L || parentId == null;
    }
    
    /**
     * 检查是否有权限标识
     */
    @Transient
    public boolean hasPermission() {
        return permissionCode != null && !permissionCode.trim().isEmpty();
    }
    
    /**
     * 检查菜单是否激活
     */
    @Transient
    public boolean isActive() {
        return status != null && status == 1;
    }
    
    /**
     * 检查菜单是否可见
     */
    @Transient
    public boolean isVisibleMenu() {
        return visible != null && visible && isActive();
    }
}
