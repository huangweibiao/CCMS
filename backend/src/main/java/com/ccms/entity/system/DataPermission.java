package com.ccms.entity.system;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 数据权限控制配置
 */
@Entity
@Table(name = "data_permission")
public class DataPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column
    private Long departmentId;
    
    @Column(nullable = false, length = 100)
    private String permissionType;
    
    @Column(nullable = false, length = 100)
    private String dataType;
    
    @Column(nullable = false)
    private String permissionScope;
    
    @Column(nullable = false)
    private Boolean canRead = true;
    
    @Column(nullable = false)
    private Boolean canWrite = false;
    
    @Column(nullable = false)
    private Boolean canDelete = false;
    
    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column
    private LocalDateTime updateTime;
    
    @Version
    private Long version;
    
    // Constructors
    public DataPermission() {}
    
    public DataPermission(Long userId, String permissionType, String dataType, String permissionScope) {
        this.userId = userId;
        this.permissionType = permissionType;
        this.dataType = dataType;
        this.permissionScope = permissionScope;
    }
    
    // Business methods
    public boolean hasReadPermission() {
        return canRead != null && canRead;
    }
    
    public boolean hasWritePermission() {
        return canWrite != null && canWrite;
    }
    
    public boolean hasDeletePermission() {
        return canDelete != null && canDelete;
    }
    
    public boolean canAccessDepartment(Long targetDepartmentId) {
        if (departmentId == null) {
            return true; // 无部门限制
        }
        return departmentId.equals(targetDepartmentId);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public String getPermissionType() { return permissionType; }
    public void setPermissionType(String permissionType) { this.permissionType = permissionType; }
    
    public String getDataType() { return dataType; }
    public void setDataType(String dataType) { this.dataType = dataType; }
    
    public String getPermissionScope() { return permissionScope; }
    public void setPermissionScope(String permissionScope) { this.permissionScope = permissionScope; }
    
    public Boolean getCanRead() { return canRead; }
    public void setCanRead(Boolean canRead) { this.canRead = canRead; }
    
    public Boolean getCanWrite() { return canWrite; }
    public void setCanWrite(Boolean canWrite) { this.canWrite = canWrite; }
    
    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean canDelete) { this.canDelete = canDelete; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}