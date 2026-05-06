package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 权限表实体类
 * 对应表名：ccms_sys_permission
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_permission")
public class SysPermission extends BaseEntity {

    /**
     * 权限编码
     */
    @Column(name = "perm_code", length = 64, nullable = false)
    private String permCode;
    
    /**
     * 权限名称
     */
    @Column(name = "perm_name", length = 64, nullable = false)
    private String permName;
    
    /**
     * 权限类型：1-菜单 2-按钮 3-接口
     */
    @Column(name = "perm_type", nullable = false)
    private Integer permType;
    
    /**
     * 父权限ID
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    // Getters and Setters
    public String getPermCode() {
        return permCode;
    }

    public void setPermCode(String permCode) {
        this.permCode = permCode;
    }

    public String getPermName() {
        return permName;
    }

    public void setPermName(String permName) {
        this.permName = permName;
    }

    public Integer getPermType() {
        return permType;
    }

    public void setPermType(Integer permType) {
        this.permType = permType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public String toString() {
        return "SysPermission{" +
                "id=" + getId() +
                ", permCode='" + permCode + '\'' +
                ", permName='" + permName + '\'' +
                ", permType=" + permType +
                ", parentId=" + parentId +
                ", sortOrder=" + sortOrder +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}
