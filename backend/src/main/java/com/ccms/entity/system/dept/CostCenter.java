package com.ccms.entity.system.dept;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * 成本中心表实体类
 * 对应表名：sys_cost_center
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_cost_center")
public class CostCenter extends BaseEntity {

    /**
     * 成本中心编码
     */
    @Column(name = "cost_center_code", length = 32, nullable = false)
    private String costCenterCode;
    
    /**
     * 成本中心名称
     */
    @Column(name = "cost_center_name", length = 128, nullable = false)
    private String costCenterName;
    
    /**
     * 上级成本中心ID（用于树形结构）
     */
    @Column(name = "parent_id")
    private Long parentId;
    
    /**
     * 负责人用户ID
     */
    @Column(name = "manager_user_id")
    private Long managerUserId;
    
    /**
     * 部门ID
     */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;
    
    /**
     * 成本中心类型：1-部门 2-项目 3-其他
     */
    @Column(name = "center_type", nullable = false)
    private Integer centerType;
    
    /**
     * 成本中心描述
     */
    @Column(name = "center_desc", length = 512)
    private String centerDesc;
    
    /**
     * 状态：0-停用 1-启用
     */
    @Column(name = "status", nullable = false, columnDefinition = "tinyint default 1")
    private Integer status = 1;
    
    /**
     * 预算控制级别：1-严格 2-警告 3-宽松
     */
    @Column(name = "budget_control_level", nullable = false, columnDefinition = "tinyint default 1")
    private Integer budgetControlLevel = 1;

    // Getters and Setters
    public String getCostCenterCode() {
        return costCenterCode;
    }

    public void setCostCenterCode(String costCenterCode) {
        this.costCenterCode = costCenterCode;
    }

    public String getCostCenterName() {
        return costCenterName;
    }

    public void setCostCenterName(String costCenterName) {
        this.costCenterName = costCenterName;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getManagerUserId() {
        return managerUserId;
    }

    public void setManagerUserId(Long managerUserId) {
        this.managerUserId = managerUserId;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getCenterType() {
        return centerType;
    }

    public void setCenterType(Integer centerType) {
        this.centerType = centerType;
    }

    public String getCenterDesc() {
        return centerDesc;
    }

    public void setCenterDesc(String centerDesc) {
        this.centerDesc = centerDesc;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getBudgetControlLevel() {
        return budgetControlLevel;
    }

    public void setBudgetControlLevel(Integer budgetControlLevel) {
        this.budgetControlLevel = budgetControlLevel;
    }

    @Override
    public String toString() {
        return "CostCenter{" +
                "id=" + getId() +
                ", costCenterCode='" + costCenterCode + '\'' +
                ", costCenterName='" + costCenterName + '\'' +
                ", parentId=" + parentId +
                ", managerUserId=" + managerUserId +
                ", deptId=" + deptId +
                ", centerType=" + centerType +
                ", centerDesc='" + centerDesc + '\'' +
                ", status=" + status +
                ", budgetControlLevel=" + budgetControlLevel +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                '}';
    }
}
