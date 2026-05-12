package com.ccms.entity.system.permission;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "ccms_sys_role_permission")
public class SysRolePermission extends BaseEntity {
    
    @Column(name = "role_id", nullable = false)
    private Long roleId;
    
    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
    
    // Manual getters and setters
    public Long getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
    
    public Long getPermissionId() {
        return permissionId;
    }
    
    public void setPermissionId(Long permissionId) {
        this.permissionId = permissionId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SysRolePermission that = (SysRolePermission) obj;
        if (roleId != null ? !roleId.equals(that.roleId) : that.roleId != null) return false;
        return permissionId != null ? permissionId.equals(that.permissionId) : that.permissionId == null;
    }
    
    @Override
    public int hashCode() {
        int result = roleId != null ? roleId.hashCode() : 0;
        result = 31 * result + (permissionId != null ? permissionId.hashCode() : 0);
        return result;
    }
}
