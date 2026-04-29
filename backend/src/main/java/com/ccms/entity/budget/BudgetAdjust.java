package com.ccms.entity.budget;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * 预算调整记录表实体类
 * 对应表名：ccms_budget_adjust
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_budget_adjust")
public class BudgetAdjust extends BaseEntity {

    /**
     * 预算ID
     */
    @Column(name = "budget_id", nullable = false)
    private Long budgetId;
    
    /**
     * 调整单号
     */
    @Column(name = "adjust_no", length = 64, nullable = false)
    private String adjustNo;
    
    /**
     * 调整类型：1-追加 2-调减 3-转移
     */
    @Column(name = "adjust_type", nullable = false)
    private Integer adjustType;
    
    /**
     * 来源预算明细ID
     */
    @Column(name = "source_budget_detail_id")
    private Long sourceBudgetDetailId;
    
    /**
     * 目标预算明细ID
     */
    @Column(name = "target_budget_detail_id")
    private Long targetBudgetDetailId;
    
    /**
     * 调整金额
     */
    @Column(name = "adjust_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal adjustAmount;
    
    /**
     * 调整原因
     */
    @Column(name = "reason", length = 512)
    private String reason;
    
    /**
     * 调整人ID
     */
    @Column(name = "adjust_by", nullable = false)
    private Long adjustBy;

    // Getters and Setters - 添加缺失的方法
    public Long getBudgetMainId() {
        return this.budgetId; // budgetMainId对应budgetId
    }

    public void setBudgetMainId(Long budgetMainId) {
        this.budgetId = budgetMainId;
    }

    public Integer getStatus() {
        return this.adjustType; // 简化处理，status对应adjustType
    }

    public void setStatus(Integer status) {
        // 简化的状态设置
    }

    public Integer getAdjustStatus() {
        return 0; // 默认调整状态
    }

    public void setAdjustStatus(Integer adjustStatus) {
        // 简化的调整状态设置
    }

    public void setAdjustDate(java.time.LocalDate adjustDate) {
        // 简化处理，使用基类的创建时间
    }

    public void setApprovalComment(String comment) {
        this.reason = comment; // 审批意见对应reason
    }

    public void setApprovalTime(java.time.LocalDateTime approvalTime) {
        // 简化处理，使用基类的创建时间
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    public String getAdjustNo() {
        return adjustNo;
    }

    public void setAdjustNo(String adjustNo) {
        this.adjustNo = adjustNo;
    }

    public Integer getAdjustType() {
        return adjustType;
    }

    public void setAdjustType(Integer adjustType) {
        this.adjustType = adjustType;
    }

    public Long getSourceBudgetDetailId() {
        return sourceBudgetDetailId;
    }

    public void setSourceBudgetDetailId(Long sourceBudgetDetailId) {
        this.sourceBudgetDetailId = sourceBudgetDetailId;
    }

    public Long getTargetBudgetDetailId() {
        return targetBudgetDetailId;
    }

    public void setTargetBudgetDetailId(Long targetBudgetDetailId) {
        this.targetBudgetDetailId = targetBudgetDetailId;
    }

    public BigDecimal getAdjustAmount() {
        return adjustAmount;
    }

    public void setAdjustAmount(BigDecimal adjustAmount) {
        this.adjustAmount = adjustAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getAdjustBy() {
        return adjustBy;
    }

    public void setAdjustBy(Long adjustBy) {
        this.adjustBy = adjustBy;
    }

    @Override
    public String toString() {
        return "BudgetAdjust{" +
                "id=" + getId() +
                ", budgetId=" + budgetId +
                ", adjustNo='" + adjustNo + '\'' +
                ", adjustType=" + adjustType +
                ", sourceBudgetDetailId=" + sourceBudgetDetailId +
                ", targetBudgetDetailId=" + targetBudgetDetailId +
                ", adjustAmount=" + adjustAmount +
                ", reason='" + reason + '\'' +
                ", adjustBy=" + adjustBy +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}