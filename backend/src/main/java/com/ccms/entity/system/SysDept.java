package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 部门表实体类
 * 对应表名：ccms_sys_dept
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_dept")
public class SysDept extends BaseEntity {

    /**
     * 部门编码
     */
    @Column(name = "dept_code", length = 32, nullable = false)
    private String deptCode;
    
    /**
     * 部门名称
     */
    @Column(name = "dept_name", length = 64, nullable = false)
    private String deptName;
    
    /**
     * 上级部门ID
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 负责人ID
     */
    @Column(name = "leader_id")
    private Long leaderId;
    
    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用 1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status;

    // Getters and Setters
    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SysDept{" +
                "id=" + getId() +
                ", deptCode='" + deptCode + '\'' +
                ", deptName='" + deptName + '\'' +
                ", parentId=" + parentId +
                ", leaderId=" + leaderId +
                ", sortOrder=" + sortOrder +
                ", status=" + status +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}