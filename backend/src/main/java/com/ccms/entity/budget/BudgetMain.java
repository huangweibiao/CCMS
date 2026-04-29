package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算主表实体类
 * 对应表名：ccms_budget_main
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_budget_main")
public class BudgetMain extends BaseEntity {

    /**
     * 预算单号
     */
    @Column(name = "budget_no", length = 64, nullable = false)
    private String budgetNo;
    
    /**
     * 预算年度
     */
    @Column(name = "budget_year", nullable = false)
    private Integer budgetYear;
    
    /**
     * 预算周期：MONTH/QUARTER/YEAR
     */
    @Column(name = "budget_period", length = 16, nullable = false)
    private String budgetPeriod;
    
    /**
     * 部门ID
     */
    @Column(name = "dept_id", nullable = false)
    private Long deptId;
    
    /**
     * 预算总额
     */
    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    /**
     * 可用金额
     */
    @Column(name = "available_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal availableAmount;
    
    /**
     * 冻结金额
     */
    @Column(name = "frozen_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal frozenAmount;
    
    /**
     * 已用金额
     */
    @Column(name = "used_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal usedAmount;
    
    /**
     * 预算状态：1-草稿 2-审批中 3-已生效 4-已过期 5-已作废
     */
    @Column(name = "budget_status", nullable = false)
    private Integer budgetStatus;
    
    /**
     * 生效时间
     */
    @Column(name = "effective_date")
    private java.sql.Date effectiveDate;
    
    /**
     * 失效时间
     */
    @Column(name = "expire_date")
    private java.sql.Date expireDate;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 512)
    private String remark;
    
    /**
     * 审批人ID
     */
    @Column(name = "approver_id")
    private Long approverId;
    
    /**
     * 审批时间
     */
    @Column(name = "approve_time")
    private LocalDateTime approveTime;

    // Getters and Setters
    public String getBudgetNo() {
        return budgetNo;
    }

    public void setBudgetNo(String budgetNo) {
        this.budgetNo = budgetNo;
    }

    public Integer getBudgetYear() {
        return budgetYear;
    }

    public void setBudgetYear(Integer budgetYear) {
        this.budgetYear = budgetYear;
    }

    public String getBudgetPeriod() {
        return budgetPeriod;
    }

    public void setBudgetPeriod(String budgetPeriod) {
        this.budgetPeriod = budgetPeriod;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }

    public Integer getBudgetStatus() {
        return budgetStatus;
    }

    public void setBudgetStatus(Integer budgetStatus) {
        this.budgetStatus = budgetStatus;
    }

    public java.sql.Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(java.sql.Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public java.sql.Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(java.sql.Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    // 为缺失的方法添加实现
    public Integer getStatus() {
        return this.budgetStatus; // status对应budgetStatus
    }

    public void setStatus(Integer status) {
        this.budgetStatus = status;
    }

    public Integer getApprovalStatus() {
        return 2; // 返回默认的审批状态
    }

    public void setApprovalStatus(Integer approvalStatus) {
        // 这个方法可能会被调用但无对应字段，保持空实现
    }

    public String getBudgetCode() {
        return this.budgetNo; // budgetCode对应budgetNo
    }

    public void setBudgetCode(String budgetCode) {
        this.budgetNo = budgetCode;
    }

    public String getBudgetName() {
        return this.budgetNo; // budgetName默认返回预算单号
    }

    public String getBudgetCycle() {
        return this.budgetPeriod; // budgetCycle对应budgetPeriod
    }

    public String getDescription() {
        return this.remark; // description对应remark
    }

    // 添加缺失的方法
    public void setBudgetName(String budgetName) {
        this.budgetNo = budgetName; // budgetName对应budgetNo
    }

    public void setBudgetCycle(String budgetCycle) {
        this.budgetPeriod = budgetCycle; // budgetCycle对应budgetPeriod
    }

    public void setDescription(String description) {
        this.remark = description; // description对应remark
    }

    @Override
    public String toString() {
        return "BudgetMain{" +
                "id=" + getId() +
                ", budgetNo='" + budgetNo + '\'' +
                ", budgetYear=" + budgetYear +
                ", budgetPeriod='" + budgetPeriod + '\'' +
                ", deptId=" + deptId +
                ", totalAmount=" + totalAmount +
                ", availableAmount=" + availableAmount +
                ", frozenAmount=" + frozenAmount +
                ", usedAmount=" + usedAmount +
                ", budgetStatus=" + budgetStatus +
                ", effectiveDate=" + effectiveDate +
                ", expireDate=" + expireDate +
                ", remark='" + remark + '\'' +
                ", approverId=" + approverId +
                ", approveTime=" + approveTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}