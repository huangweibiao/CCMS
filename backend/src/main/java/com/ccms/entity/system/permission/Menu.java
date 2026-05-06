package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系统菜单实体类
 */
@Entity
@Table(name = "sys_menu")
public class Menu extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    private String menuName;

    @Size(max = 100)
    private String menuCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Menu parent;

    private String menuType;

    @Size(max = 200)
    private String menuUrl;

    @Size(max = 100)
    private String menuIcon;

    private String component;

    private String perms;

    private String target;

    private Integer sortOrder = 0;

    private boolean visible = true;

    private boolean enabled = true;

    private boolean isFrame = false;

    private Integer level = 1;

    // 构造器
    public Menu() {}

    public Menu(String menuName, String menuCode, String menuType) {
        this.menuName = menuName;
        this.menuCode = menuCode;
        this.menuType = menuType;
    }

    // Getters and Setters
    public String getMenuName() { return menuName; }
    public void setMenuName(String menuName) { this.menuName = menuName; }

    public String getMenuCode() { return menuCode; }
    public void setMenuCode(String menuCode) { this.menuCode = menuCode; }

    public Menu getParent() { return parent; }
    public void setParent(Menu parent) { this.parent = parent; }

    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }

    public String getMenuUrl() { return menuUrl; }
    public void setMenuUrl(String menuUrl) { this.menuUrl = menuUrl; }

    public String getMenuIcon() { return menuIcon; }
    public void setMenuIcon(String menuIcon) { this.menuIcon = menuIcon; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public String getPerms() { return perms; }
    public void setPerms(String perms) { this.perms = perms; }

    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isFrame() { return isFrame; }
    public void setFrame(boolean frame) { isFrame = frame; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    
    /**
     * 获取菜单完整路径
     */
    @Transient
    public String getFullPath() {
        if (parent == null) {
            return menuName;
        }
        return parent.getFullPath() + "/" + menuName;
    }
    
    /**
     * 检查是否是一级菜单
     */
    @Transient
    public boolean isTopMenu() {
        return parent == null;
    }
    
    /**
     * 检查是否是按钮权限
     */
    @Transient
    public boolean isButton() {
        return "BUTTON".equals(menuType);
    }
    
    /**
     * 检查是否是菜单项
     */
    @Transient
    public boolean isMenu() {
        return "MENU".equals(menuType);
    }
    
    /**
     * 检查是否有权限
     */
    @Transient
    public boolean hasPermission() {
        return perms != null && !perms.trim().isEmpty();
    }
}
