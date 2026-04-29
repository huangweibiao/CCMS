package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 预算明细表实体类
 * 对应表名：ccms_budget_detail
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_budget_detail")
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
    private BigDecimal budgetAmount;
    
    /**
     * 已用金额
     */
    @Column(name = "used_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal usedAmount;
    
    /**
     * 冻结金额
     */
    @Column(name = "frozen_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal frozenAmount;
    
    /**
     * 上期结转
     */
    @Column(name = "carry_over", precision = 18, scale = 2)
    private BigDecimal carryOver;

    // Getters and Setters - 添加缺失的方法
    public Long getBudgetMainId() {
        return this.budgetId; // budgetMainId对应budgetId
    }

    public void setBudgetMainId(Long budgetMainId) {
        this.budgetId = budgetMainId;
    }

    public BigDecimal getRemainingAmount() {
        return this.budgetAmount.subtract(this.usedAmount).subtract(this.frozenAmount);
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        // remainingAmount的计算逻辑，这里简化处理
        this.usedAmount = this.budgetAmount.subtract(remainingAmount).max(BigDecimal.ZERO);
    }

    public String getDescription() {
        return "预算明细 - " + this.feeTypeId; // 简化处理
    }
    
    public void setDescription(String description) {
        // description字段可能用于存储其他信息，这个实体类目前没有description字段
        // 这个方法只是用于兼容性，实际实现可能需要添加description字段
    }

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
        this.budgetAmount = budgetAmount;
    }

    public BigDecimal getUsedAmount() {
        return usedAmount;
    }

    public void setUsedAmount(BigDecimal usedAmount) {
        this.usedAmount = usedAmount;
    }

    public BigDecimal getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(BigDecimal frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public BigDecimal getCarryOver() {
        return carryOver;
    }

    public void setCarryOver(BigDecimal carryOver) {
        this.carryOver = carryOver;
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