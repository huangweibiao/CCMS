package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 费用申请单明细表实体类
 * 对应表名：ccms_expense_apply_detail
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_expense_apply_detail")
public class ExpenseApplyDetail extends BaseEntity {

    /**
     * 申请单ID
     */
    @Column(name = "apply_id", nullable = false)
    private Long applyId;
    
    /**
     * 费用类型ID
     */
    @Column(name = "fee_type_id", nullable = false)
    private Long feeTypeId;
    
    /**
     * 金额
     */
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 用途描述
     */
    @Column(name = "description", length = 256)
    private String description;
    
    /**
     * 成本中心
     */
    @Column(name = "cost_center", length = 64)
    private String costCenter;
    
    /**
     * 关联预算ID
     */
    @Column(name = "budget_id")
    private Long budgetId;

    // Getters and Setters
    public Long getApplyId() {
        return applyId;
    }

    public void setApplyId(Long applyId) {
        this.applyId = applyId;
    }

    public Long getFeeTypeId() {
        return feeTypeId;
    }

    public void setFeeTypeId(Long feeTypeId) {
        this.feeTypeId = feeTypeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCostCenter() {
        return costCenter;
    }

    public void setCostCenter(String costCenter) {
        this.costCenter = costCenter;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    @Override
    public String toString() {
        return "ExpenseApplyDetail{" +
                "id=" + getId() +
                ", applyId=" + applyId +
                ", feeTypeId=" + feeTypeId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", costCenter='" + costCenter + '\'' +
                ", budgetId=" + budgetId +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}