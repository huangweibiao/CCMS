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
     * 预算明细ID
     */
    @Column(name = "budget_detail_id", nullable = false)
    private Long budgetDetailId;
    
    /**
     * 业务单据类型：1-费用申请 2-费用报销 3-预算调整
     */
    @Column(name = "biz_type", nullable = false)
    private Integer bizType;
    
    /**
     * 业务单据ID
     */
    @Column(name = "biz_id", nullable = false)
    private Long bizId;
    
    /**
     * 操作类型：1-冻结 2-解冻 3-扣减 4-释放 5-调整
     */
    @Column(name = "oper_type", nullable = false)
    private Integer operType;
    
    /**
     * 操作金额
     */
    @Column(name = "oper_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal operAmount;
    
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
    public Long getBudgetDetailId() {
        return budgetDetailId;
    }

    public void setBudgetDetailId(Long budgetDetailId) {
        this.budgetDetailId = budgetDetailId;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public Integer getOperType() {
        return operType;
    }

    public void setOperType(Integer operType) {
        this.operType = operType;
    }

    public BigDecimal getOperAmount() {
        return operAmount;
    }

    public void setOperAmount(BigDecimal operAmount) {
        this.operAmount = operAmount;
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
                ", budgetDetailId=" + budgetDetailId +
                ", bizType=" + bizType +
                ", bizId=" + bizId +
                ", operType=" + operType +
                ", operAmount=" + operAmount +
                ", operateBy=" + operateBy +
                ", operateTime=" + operateTime +
                ", remark='" + remark + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}