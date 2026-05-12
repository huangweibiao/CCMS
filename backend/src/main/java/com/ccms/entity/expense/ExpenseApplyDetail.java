package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 费用申请单明细表实体类
 * 对应表名：ccms_expense_apply_detail
 * 
 * 主要特性：
 * - 与主表的ManyToOne关联关系
 * - 支持费用类型和预算关联
 * - 成本中心管理
 * - 金额验证和业务规则检查
 * 
 * 业务约束：
 * - 必须有关联的主表引用
 * - 金额必须大于0
 * - 费用类型必须指定
 * - 支持预算控制和成本中心分配
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_expense_apply_detail")
public class ExpenseApplyDetail extends BaseEntity {

    /**
     * 申请单主表引用
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_id", nullable = false)
    private ExpenseApplyMain expenseApplyMain;
    
    /**
     * 申请单ID（冗余字段，便于查询）
     */
    @Column(name = "apply_id", nullable = false, insertable = false, updatable = false)
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

    public ExpenseApplyMain getExpenseApplyMain() {
        return expenseApplyMain;
    }

    public void setExpenseApplyMain(ExpenseApplyMain expenseApplyMain) {
        this.expenseApplyMain = expenseApplyMain;
    }
    
    /**
     * 校验明细项信息是否完整
     */
    public boolean validateDetailInfo() {
        return feeTypeId != null &&
               amount != null && amount.compareTo(BigDecimal.ZERO) > 0 &&
               expenseApplyMain != null;
    }
    
    /**
     * 检查是否有关联的预算
     */
    public boolean hasBudget() {
        return budgetId != null;
    }
    
    /**
     * 检查成本中心是否已设置
     */
    public boolean hasCostCenter() {
        return costCenter != null && !costCenter.trim().isEmpty();
    }
    
    /**
     * 获取费用类型名称（通过关联对象查询）
     */
    public String getFeeTypeName() {
        // 这里需要从费用类型服务中获取，暂时返回占位符
        return hasBudget() ? "关联预算费用项" : "普通费用项";
    }
    
    /**
     * 检查金额是否超过预算限制（需要外部服务支持）
     */
    public boolean isAmountWithinBudgetLimit() {
        if (budgetId == null || amount == null) {
            return true; // 没有预算关联，不做限制
        }
        
        // 这里需要调用预算服务检查金额限制
        // 暂时返回true，实际实现时需要调用预算服务API
        return true;
    }
    
    /**
     * 获取预算使用的比例（需要外部服务支持）
     */
    public BigDecimal getBudgetUsageRatio() {
        if (budgetId == null || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // 这里需要调用预算服务获取预算总额和已使用金额
        // 暂时返回0，实际实现时需要调用预算服务API
        return BigDecimal.ZERO;
    }

    @Override
    public String toString() {
        return "ExpenseApplyDetail{" +
                "id=" + getId() +
                ", applyId=" + (expenseApplyMain != null ? expenseApplyMain.getId() : "null") +
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