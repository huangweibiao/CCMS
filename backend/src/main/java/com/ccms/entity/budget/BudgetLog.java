package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预算日志表实体类
 * 对应表名：ccms_budget_log
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_budget_log")
public class BudgetLog extends BaseEntity {

    /**
     * 预算ID
     */
    @Column(name = "budget_id", nullable = false)
    private Long budgetId;
    
    /**
     * 操作类型：扣减/释放/冻结/解冻
     */
    @Column(name = "operate_type", length = 32, nullable = false)
    private String operateType;
    
    /**
     * 操作金额
     */
    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal amount;
    
    /**
     * 关联业务单ID（申请/报销单）
     */
    @Column(name = "business_id")
    private Long businessId;
    
    /**
     * 业务类型：APPLY/REIMBURSE
     */
    @Column(name = "business_type", length = 32)
    private String businessType;
    
    /**
     * 操作人ID
     */
    @Column(name = "operate_by", nullable = false)
    private Long operateBy;
    
    /**
     * 操作时间
     */
    @Column(name = "operate_time", nullable = false)
    private LocalDateTime operateTime;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 512)
    private String remark;

    // Getters and Setters
    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Long getOperateBy() {
        return operateBy;
    }

    public void setOperateBy(Long operateBy) {
        this.operateBy = operateBy;
    }

    public LocalDateTime getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(LocalDateTime operateTime) {
        this.operateTime = operateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "BudgetLog{" +
                "id=" + getId() +
                ", budgetId=" + budgetId +
                ", operateType='" + operateType + '\'' +
                ", amount=" + amount +
                ", businessId=" + businessId +
                ", businessType='" + businessType + '\'' +
                ", operateBy=" + operateBy +
                ", operateTime=" + operateTime +
                ", remark='" + remark + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}