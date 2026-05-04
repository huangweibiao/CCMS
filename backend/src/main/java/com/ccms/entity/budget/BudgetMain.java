package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算主表实体类
 * 对应设计文档中的budget_main表
 * 
 * @author CCMS系统
 */
@Entity
@Table(name = "budget_main")
public class BudgetMain extends BaseEntity {

    /**
     * 预算单号
     */
    @Column(name = "budget_no", length = 64, nullable = false, unique = true)
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
     * 成本中心ID
     */
    @Column(name = "cost_center_id", nullable = false)
    private Long costCenterId;
    
    /**
     * 预算总额
     */
    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    /**
     * 已用金额
     */
    @Column(name = "used_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal usedAmount = BigDecimal.ZERO;
    
    /**
     * 冻结金额
     */
    @Column(name = "frozen_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal frozenAmount = BigDecimal.ZERO;
    
    /**
     * 状态：0-草稿 1-生效 2-冻结 3-作废
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
    
    /**
     * 审批状态：0-未提交 1-审批中 2-审批通过 3-审批驳回
     */
    @Column(name = "approval_status")
    private Integer approvalStatus = 0;
    
    /**
     * 预算名称
     */
    @Column(name = "budget_name", length = 200)
    private String budgetName;
    
    /**
     * 预算周期描述
     */
    @Column(name = "budget_cycle", length = 100)
    private String budgetCycle;
    
    /**
     * 预算描述
     */
    @Column(name = "description", length = 500)
    private String description;
    
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
    
    /**
     * 创建人ID
     */
    @Column(name = "create_by", nullable = false)
    private Long createBy;
    
    /**
     * 更新人ID
     */
    @Column(name = "update_by")
    private Long updateBy;

    // Constructors
    public BudgetMain() {
        // 默认构造函数
    }
    
    public BudgetMain(String budgetNo, Integer budgetYear, String budgetPeriod, 
                     Long deptId, Long costCenterId, BigDecimal totalAmount, Long createBy) {
        this.budgetNo = budgetNo;
        this.budgetYear = budgetYear;
        this.budgetPeriod = budgetPeriod;
        this.deptId = deptId;
        this.costCenterId = costCenterId;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.createBy = createBy;
    }

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

    public Long getCostCenterId() {
        return costCenterId;
    }

    public void setCostCenterId(Long costCenterId) {
        this.costCenterId = costCenterId;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount != null ? usedAmount : BigDecimal.ZERO;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount != null ? frozenAmount : BigDecimal.ZERO;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getCreateBy() {
        return createBy;
    }

    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }

    public Long getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getBudgetName() {
        return budgetName;
    }

    public void setBudgetName(String budgetName) {
        this.budgetName = budgetName;
    }

    public String getBudgetCycle() {
        return budgetCycle;
    }

    public void setBudgetCycle(String budgetCycle) {
        this.budgetCycle = budgetCycle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    /**
     * 获取预算状态
     * 兼容已有代码调用的getBudgetStatus()方法
     */
    public Integer getBudgetStatus() {
        return status;
    }

    /**
     * 设置预算状态
     */
    public void setBudgetStatus(Integer budgetStatus) {
        this.status = budgetStatus;
    }

    /**
     * 获取预算编码
     * 兼容已有代码调用的getBudgetCode()方法
     */
    public String getBudgetCode() {
        return budgetNo;
    }

    /**
     * 设置预算编码
     */
    public void setBudgetCode(String budgetCode) {
        this.budgetNo = budgetCode;
    }

    // Business logic methods
    /**
     * 获取预算余额
     */
    @Transient
    public BigDecimal getAvailableAmount() {
        return totalAmount.subtract(usedAmount).subtract(frozenAmount);
    }
    
    /**
     * 检查预算是否足够
     */
    @Transient
    public boolean isBudgetSufficient(BigDecimal requiredAmount) {
        if (requiredAmount == null || requiredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return getAvailableAmount().compareTo(requiredAmount) >= 0;
    }
    
    /**
     * 冻结预算金额
     */
    public void freezeAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.frozenAmount = this.frozenAmount.add(amount);
        }
    }
    
    /**
     * 释放冻结金额
     */
    public void releaseFrozenAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.frozenAmount = this.frozenAmount.subtract(amount).max(BigDecimal.ZERO);
        }
    }
    
    /**
     * 扣减预算金额
     */
    public void deductAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.usedAmount = this.usedAmount.add(amount);
        }
    }
    
    /**
     * 退回预算金额
     */
    public void refundAmount(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.usedAmount = this.usedAmount.subtract(amount).max(BigDecimal.ZERO);
        }
    }

    @Override
    public String toString() {
        return "BudgetMain{" +
                "id=" + getId() +
                ", budgetNo='" + budgetNo + '\'' +
                ", budgetYear=" + budgetYear +
                ", budgetPeriod='" + budgetPeriod + '\'' +
                ", deptId=" + deptId +
                ", costCenterId=" + costCenterId +
                ", totalAmount=" + totalAmount +
                ", usedAmount=" + usedAmount +
                ", frozenAmount=" + frozenAmount +
                ", status=" + status +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}