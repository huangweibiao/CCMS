package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import com.ccms.enums.ApplyStatusEnum;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 费用申请单主表实体类
 * 对应表名：expense_apply_main
 * 
 * 状态说明：
 * - 0: 草稿    - 1: 审批中  - 2: 已通过
 * - 3: 已驳回  - 4: 待支付  - 5: 已支付
 * - 6: 已作废
 * 
 * 主要特性：
 * - 支持完整的状态流转控制
 * - 自动金额计算与同步
 * - 关联明细表的一对多关系
 * - 集成审批流程管理
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_expense_apply_main")
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
    private LocalDate expectedDate;
    
    /**
     * 状态：0-草稿 1-审批中 2-已通过 3-已驳回 4-待支付 5-已支付 6-已作废
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
    
    /**
     * 费用申请明细列表
     */
    @OneToMany(mappedBy = "expenseApplyMain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ExpenseApplyDetail> expenseApplyDetails;

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

    public LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(LocalDate expectedDate) {
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

    public List<ExpenseApplyDetail> getExpenseApplyDetails() {
        return expenseApplyDetails;
    }

    public void setExpenseApplyDetails(List<ExpenseApplyDetail> expenseApplyDetails) {
        this.expenseApplyDetails = expenseApplyDetails;
    }

    /**
     * 添加费用申请明细项
     */
    public void addExpenseApplyDetail(ExpenseApplyDetail detail) {
        if (expenseApplyDetails == null) {
            expenseApplyDetails = new ArrayList<>();
        }
        detail.setExpenseApplyMain(this);
        expenseApplyDetails.add(detail);
    }

    /**
     * 移除费用申请明细项
     */
    public void removeExpenseApplyDetail(ExpenseApplyDetail detail) {
        if (expenseApplyDetails != null) {
            expenseApplyDetails.remove(detail);
            detail.setExpenseApplyMain(null);
        }
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
    
    /**
     * 计算申请明细的合计金额
     */
    public BigDecimal calculateTotalAmount() {
        if (expenseApplyDetails == null || expenseApplyDetails.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        for (ExpenseApplyDetail detail : expenseApplyDetails) {
            if (detail.getAmount() != null) {
                total = total.add(detail.getAmount());
            }
        }
        return total;
    }
    
    /**
     * 校验申请金额是否与明细合计一致
     */
    public boolean isAmountConsistent() {
        if (applyAmount == null || totalAmount == null) {
            return false;
        }
        
        // 检查申请金额是否等于明细合计
        BigDecimal calculatedTotal = calculateTotalAmount();
        boolean totalMatch = totalAmount.compareTo(calculatedTotal) == 0;
        
        // 检查申请金额是否等于合计金额
        boolean applyMatch = applyAmount.compareTo(totalAmount) == 0;
        
        return totalMatch && applyMatch;
    }
    
    /**
     * 更新金额到明细合计自动同步
     */
    public void syncAmountFromDetails() {
        BigDecimal calculatedTotal = calculateTotalAmount();
        this.totalAmount = calculatedTotal;
        this.applyAmount = calculatedTotal;
    }
    
    /**
     * 校验申请单基本信息是否完整
     */
    public boolean validateBasicInfo() {
        return applyNo != null && !applyNo.trim().isEmpty() &&
               applyUserId != null &&
               applyDeptId != null &&
               applyType != null &&
               reason != null && !reason.trim().isEmpty() &&
               status != null;
    }
    
    /**
     * 校验申请单是否可以提交审批
     */
    public boolean canSubmitForApproval() {
        // 必须是草稿或已驳回状态
        if (!isEditable()) {
            return false;
        }
        
        // 基本信息必须完整
        if (!validateBasicInfo()) {
            return false;
        }
        
        // 必须至少有一个明细项
        if (expenseApplyDetails == null || expenseApplyDetails.isEmpty()) {
            return false;
        }
        
        // 金额必须一致
        if (!isAmountConsistent()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 提交申请单到审批流程
     */
    public void submitForApproval() {
        if (canSubmitForApproval()) {
            this.status = ApplyStatusEnum.APPROVING.getCode();
        } else {
            throw new IllegalStateException("申请单不满足提交审批的条件");
        }
    }
    
    /**
     * 获取明细的数量
     */
    public int getDetailCount() {
        return expenseApplyDetails != null ? expenseApplyDetails.size() : 0;
    }
    
    /**
     * 检查是否有关联的预算项
     */
    public boolean hasBudgetItems() {
        if (expenseApplyDetails == null || expenseApplyDetails.isEmpty()) {
            return false;
        }
        
        for (ExpenseApplyDetail detail : expenseApplyDetails) {
            if (detail.getBudgetId() != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取费用类型（兼容性方法）
     */
    public String getExpenseType() {
        return applyType != null ? applyType.toString() : "STANDARD";
    }

    /**
     * 获取申请人姓名（需要关联用户表）
     */
    public String getApplyUserName() {
        // 这里需要根据applyUserId查询用户表，暂时返回"未知用户"
        return "未知用户";
    }

    /**
     * 获取部门（兼容性方法）
     */
    public String getDepartment() {
        // 需要根据applyDeptId查询部门表，暂时返回"未知部门"
        return "未知部门";
    }

    /**
     * 获取申请日期
     */
    public LocalDate getApplyDate() {
        return getCreateTime() != null ? getCreateTime().toLocalDate() : LocalDate.now();
    }

    /**
     * 获取申请状态
     */
    public String getApplyStatus() {
        return status != null ? status.toString() : "0";
    }

    /**
     * 设置申请状态
     */
    public void setApplyStatus(String status) {
        try {
            this.status = Integer.parseInt(status);
        } catch(NumberFormatException e) {
            this.status = 0; // 默认值
        }
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