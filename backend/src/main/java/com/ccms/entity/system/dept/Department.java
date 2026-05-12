package com.ccms.entity.system.dept;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 部门组织架构实体类
 */
@Entity
@Table(name = "ccms_sys_department", uniqueConstraints = {
    @UniqueConstraint(columnNames = "deptCode")
})
public class Department extends BaseEntity {

    @NotBlank
    @Size(max = 50)
    @Column(unique = true, nullable = false)
    private String deptCode;

    @NotBlank
    @Size(max = 100)
    private String deptName;

    @Size(max = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parent;

    private Integer sortOrder = 0;

    private String deptType;

    private String contactPerson;

    private String contactPhone;

    private String contactEmail;

    private boolean enabled = true;

    // 构造器
    public Department() {}

    public Department(String deptCode, String deptName) {
        this.deptCode = deptCode;
        this.deptName = deptName;
    }

    // Getters and Setters
    public String getDeptCode() { return deptCode; }
    public void setDeptCode(String deptCode) { this.deptCode = deptCode; }

    public String getDeptName() { return deptName; }
    public void setDeptName(String deptName) { this.deptName = deptName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Department getParent() { return parent; }
    public void setParent(Department parent) { this.parent = parent; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public String getDeptType() { return deptType; }
    public void setDeptType(String deptType) { this.deptType = deptType; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    
    /**
     * 获取部门完整路径
     */
    @Transient
    public String getFullPath() {
        if (parent == null) {
            return deptName;
        }
        return parent.getFullPath() + "/" + deptName;
    }
    
    /**
     * 检查是否为顶层部门
     */
    @Transient
    public boolean isRoot() {
        return parent == null;
    }
    
    /**
     * 检查是否为叶子部门
     */
    @Transient
    public boolean isLeaf() {
        // 实际实现需要查询子部门数量
        // 这里简化处理
        return true;
    }
}
