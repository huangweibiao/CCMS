package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 预算明细表实体类
 * 对应设计文档中的budget_detail表
 * 
 * @author CCMS系统
 */
@Entity
@Table(name = "budget_detail")
public class BudgetDetail extends BaseEntity {

    /**
     * 预算主表ID
     */
    @Column(name = "budget_id", nullable = false)
    private Long budgetId;
    
    /**
     * 费用类型ID
     */
    @Column(name = "fee_type_id", nullable = false)
    private Long feeTypeId;
    
    /**
     * 预算金额
     */
    @Column(name = "budget_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal budgetAmount = BigDecimal.ZERO;
    
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
     * 上期结转
     */
    @Column(name = "carry_over", precision = 18, scale = 2)
    private BigDecimal carryOver = BigDecimal.ZERO;
    
    /**
     * 明细描述/备注
     */
    @Column(name = "description", length = 500)
    private String description;

    // Constructors
    public BudgetDetail() {
        // 默认构造函数
    }
    
    public BudgetDetail(Long budgetId, Long feeTypeId, BigDecimal budgetAmount, BigDecimal carryOver) {
        this.budgetId = budgetId;
        this.feeTypeId = feeTypeId;
        this.budgetAmount = budgetAmount != null ? budgetAmount : BigDecimal.ZERO;
        this.carryOver = carryOver != null ? carryOver : BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public Long getFeeTypeId() {
        return feeTypeId;
    }

    public void setFeeTypeId(Long feeTypeId) {
        this.feeTypeId = feeTypeId;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount != null ? budgetAmount : BigDecimal.ZERO;
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

    public BigDecimal getCarryOver() {
        return carryOver;
    }

    public void setCarryOver(BigDecimal carryOver) {
        this.carryOver = carryOver != null ? carryOver : BigDecimal.ZERO;
    }

    // Business logic methods
    /**
     * 获取预算余额
     */
    @Transient
    public BigDecimal getRemainingAmount() {
        return budgetAmount.add(carryOver).subtract(usedAmount).subtract(frozenAmount);
    }
    
    /**
     * 检查预算是否足够
     */
    @Transient
    public boolean isBudgetSufficient(BigDecimal requiredAmount) {
        if (requiredAmount == null || requiredAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return true;
        }
        return getRemainingAmount().compareTo(requiredAmount) >= 0;
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
            // 同步释放冻结金额
            releaseFrozenAmount(amount);
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
    
    /**
     * 总预算（包含上期结转）
     */
    @Transient
    public BigDecimal getTotalBudgetAmount() {
        return budgetAmount.add(carryOver);
    }

    /**
     * 设置剩余金额（计算逻辑设置，实际是调整预算金额或结转金额）
     */
    @Transient
    public void setRemainingAmount(BigDecimal remainingAmount) {
        // 基于现有预算和结转计算需要调整的数量
        BigDecimal currentTotal = budgetAmount.add(carryOver);
        BigDecimal currentUsedFrozen = usedAmount.add(frozenAmount);
        
        // 计算需要调整的预算金额
        BigDecimal targetTotal = remainingAmount.add(currentUsedFrozen);
        
        // 简单的调整策略：优先调整预算金额
        budgetAmount = targetTotal.subtract(carryOver);
    }

    /**
     * 获取预算主表ID
     * 兼容已有代码调用的getBudgetMainId()方法
     */
    public Long getBudgetMainId() {
        return budgetId;
    }

    /**
     * 设置预算主表ID
     */
    public void setBudgetMainId(Long budgetMainId) {
        this.budgetId = budgetMainId;
    }

    /**
     * 获取费用类型名称
     * 兼容已有代码调用的getExpenseTypeName()方法
     */
    @Transient
    public String getExpenseTypeName() {
        // 默认返回空字符串，实际应该通过关联查询获取
        return "";
    }

    /**
     * 设置费用类型名称
     */
    @Transient
    public void setExpenseTypeName(String expenseTypeName) {
        // 临时字段存储费用类型名称，实际应通过关联查询设置
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "BudgetDetail{" +
                "id=" + getId() +
                ", budgetId=" + budgetId +
                ", feeTypeId=" + feeTypeId +
                ", budgetAmount=" + budgetAmount +
                ", usedAmount=" + usedAmount +
                ", frozenAmount=" + frozenAmount +
                ", carryOver=" + carryOver +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}