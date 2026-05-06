package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 权限表实体类
 * 对应表名：sys_permission
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "sys_permission")
public class Permission extends BaseEntity {

    /**
     * 权限标识（如：user:add, expense:approve）
     */
    @Column(name = "permission_code", length = 64, nullable = false)
    private String permissionCode;
    
    /**
     * 权限名称
     */
    @Column(name = "permission_name", length = 64, nullable = false)
    private String permissionName;
    
    /**
     * 权限描述
     */
    @Column(name = "permission_desc", length = 256)
    private String permissionDesc;
    
    /**
     * 权限类型：1-菜单 2-按钮 3-接口
     */
    @Column(name = "permission_type", nullable = false)
    private Integer permissionType;
    
    /**
     * 上级权限ID（用于树形结构）
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 排序号
     */
    @Column(name = "order_num", columnDefinition = "int default 999")
    private Integer orderNum = 999;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 1")
    private Integer status = 1;

    // Getters and Setters
    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public void setPermissionName(String permissionName) {
        this.permissionName = permissionName;
    }

    public String getPermissionDesc() {
        return permissionDesc;
    }

    public void setPermissionDesc(String permissionDesc) {
        this.permissionDesc = permissionDesc;
    }

    public Integer getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(Integer permissionType) {
        this.permissionType = permissionType;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + getId() +
                ", permissionCode='" + permissionCode + '\'' +
                ", permissionName='" + permissionName + '\'' +
                ", permissionDesc='" + permissionDesc + '\'' +
                ", permissionType=" + permissionType +
                ", parentId=" + parentId +
                ", orderNum=" + orderNum +
                ", status=" + status +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}
