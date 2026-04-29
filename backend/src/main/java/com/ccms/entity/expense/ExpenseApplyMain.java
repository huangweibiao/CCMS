package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import com.ccms.enums.ApplyStatusEnum;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 费用申请单主表实体类
 * 对应表名：ccms_expense_apply_main
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "expense_apply_main")
public class ExpenseApplyMain extends BaseEntity {

    /**
     * 申请单号
     */
    @Column(name = "apply_no", length = 64, nullable = false)
    private String applyNo;
    
    /**
     * 申请类型：1-普通申请 2-借款申请
     */
    @Column(name = "apply_type", nullable = false)
    private Integer applyType;
    
    /**
     * 申请人ID
     */
    @Column(name = "apply_user_id", nullable = false)
    private Long applyUserId;
    
    /**
     * 申请部门ID
     */
    @Column(name = "apply_dept_id", nullable = false)
    private Long applyDeptId;
    
    /**
     * 申请金额
     */
    @Column(name = "apply_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal applyAmount;
    
    /**
     * 合计金额
     */
    @Column(name = "total_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    /**
     * 事由说明
     */
    @Column(name = "reason", length = 512)
    private String reason;
    
    /**
     * 预计使用日期
     */
    @Column(name = "expected_date")
    private Date expectedDate;
    
    /**
     * 状态：0-草稿 1-审批中 2-通过 3-驳回 4-作废
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 审批状态
     */
    @Column(name = "approval_status")
    private Integer approvalStatus;
    
    /**
     * 当前审批节点
     */
    @Column(name = "current_node", length = 64)
    private String currentNode;
    
    /**
     * 成本中心ID
     */
    @Column(name = "cost_center_id")
    private Long costCenterId;
    
    /**
     * 审批流实例ID
     */
    @Column(name = "approval_instance_id")
    private Long approvalInstanceId;

    // Getters and Setters
    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public Integer getApplyType() {
        return applyType;
    }

    public void setApplyType(Integer applyType) {
        this.applyType = applyType;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public Long getApplyDeptId() {
        return applyDeptId;
    }

    public void setApplyDeptId(Long applyDeptId) {
        this.applyDeptId = applyDeptId;
    }

    public BigDecimal getApplyAmount() {
        return applyAmount;
    }

    public void setApplyAmount(BigDecimal applyAmount) {
        this.applyAmount = applyAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Date getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(Date expectedDate) {
        this.expectedDate = expectedDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
    }

    public Long getApprovalInstanceId() {
        return approvalInstanceId;
    }

    public void setApprovalInstanceId(Long approvalInstanceId) {
        this.approvalInstanceId = approvalInstanceId;
    }

    public Long getCostCenterId() {
        return costCenterId;
    }

    public void setCostCenterId(Long costCenterId) {
        this.costCenterId = costCenterId;
    }

    // 状态管理相关方法
    
    /**
     * 获取状态枚举描述
     */
    public String getStatusDescription() {
        ApplyStatusEnum statusEnum = ApplyStatusEnum.getByCode(this.status);
        return statusEnum != null ? statusEnum.getDescription() : "未知状态";
    }
    
    /**
     * 检查是否允许变更为目标状态
     */
    public boolean canTransitionTo(Integer targetStatus) {
        return ApplyStatusEnum.isTransitionAllowed(this.status, targetStatus);
    }
    
    /**
     * 检查是否处于可编辑状态（草稿或已驳回）
     */
    public boolean isEditable() {
        return status != null && (status.equals(ApplyStatusEnum.DRAFT.getCode()) || 
                                 status.equals(ApplyStatusEnum.REJECTED.getCode()));
    }
    
    /**
     * 检查是否处于审批流程中
     */
    public boolean isInApprovalProcess() {
        return status != null && status.equals(ApplyStatusEnum.APPROVING.getCode());
    }
    
    /**
     * 检查是否已完成审批
     */
    public boolean isApprovalCompleted() {
        return status != null && 
               (status.equals(ApplyStatusEnum.APPROVED.getCode()) || 
                status.equals(ApplyStatusEnum.REJECTED.getCode()) ||
                status.equals(ApplyStatusEnum.CANCELLED.getCode()));
    }
    
    /**
     * 检查是否处于支付相关状态
     */
    public boolean isPaymentRelated() {
        return status != null && 
               (status.equals(ApplyStatusEnum.TO_BE_PAID.getCode()) || 
                status.equals(ApplyStatusEnum.PAID.getCode()));
    }

    @Override
    public String toString() {
        return "ExpenseApplyMain{" +
                "id=" + getId() +
                ", applyNo='" + applyNo + '\'' +
                ", applyType=" + applyType +
                ", applyUserId=" + applyUserId +
                ", applyDeptId=" + applyDeptId +
                ", applyAmount=" + applyAmount +
                ", totalAmount=" + totalAmount +
                ", reason='" + reason + '\'' +
                ", expectedDate=" + expectedDate +
                ", status=" + status +
                ", statusDescription='" + getStatusDescription() + '\'' +
                ", approvalStatus=" + approvalStatus +
                ", currentNode='" + currentNode + '\'' +
                ", approvalInstanceId=" + approvalInstanceId +
                ", costCenterId=" + costCenterId +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}