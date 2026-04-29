package com.ccms.enums;

/**
 * 报销单状态枚举
 * 定义报销单的完整状态流程
 * 状态流转：草稿→审批中→已通过/驳回→待支付→已支付→已作废
 */
public enum ReimburseStatusEnum {
    
    DRAFT(0, "草稿", "报销单初始状态"),
    APPROVAL_PENDING(1, "审批中", "报销单已提交审批"),
    APPROVED(2, "已通过", "报销审批已通过"),
    REJECTED(3, "已驳回", "报销审批被驳回"),
    PENDING_PAYMENT(4, "待支付", "报销已通过，等待支付"),
    PAID(5, "已支付", "报销金额已支付"),
    CANCELLED(6, "已作废", "报销单已作废");
    
    private final Integer code;
    private final String name;
    private final String description;
    
    ReimburseStatusEnum(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态码获取枚举
     */
    public static ReimburseStatusEnum getByCode(Integer code) {
        for (ReimburseStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 检查是否可以转换到目标状态
     */
    public boolean canTransitionTo(ReimburseStatusEnum targetStatus) {
        switch (this) {
            case DRAFT:
                return targetStatus == APPROVAL_PENDING || targetStatus == CANCELLED;
            case APPROVAL_PENDING:
                return targetStatus == APPROVED || targetStatus == REJECTED || targetStatus == DRAFT;
            case APPROVED:
                return targetStatus == PENDING_PAYMENT || targetStatus == CANCELLED;
            case REJECTED:
                return targetStatus == DRAFT || targetStatus == CANCELLED;
            case PENDING_PAYMENT:
                return targetStatus == PAID || targetStatus == CANCELLED;
            case PAID:
                return targetStatus == CANCELLED;
            case CANCELLED:
                return false; // 已作废状态不可变更
            default:
                return false;
        }
    }
    
    /**
     * 审批通过后的下一个状态
     */
    public ReimburseStatusEnum getNextStatusAfterApproval() {
        switch (this) {
            case APPROVAL_PENDING:
                return APPROVED;
            case APPROVED:
                return PENDING_PAYMENT;
            case PENDING_PAYMENT:
                return PAID;
            default:
                return null;
        }
    }
    
    /**
     * 检查是否可提交审批
     */
    public boolean canSubmitApproval() {
        return this == DRAFT;
    }
    
    /**
     * 检查是否可进行审批操作
     */
    public boolean canProcessApproval() {
        return this == APPROVAL_PENDING;
    }
    
    /**
     * 检查是否可进行支付操作
     */
    public boolean canProcessPayment() {
        return this == PENDING_PAYMENT;
    }
    
    /**
     * 检查是否可撤销操作
     */
    public boolean canCancel() {
        return this != PAID && this != CANCELLED;
    }
    
    /**
     * 检查是否已完成流程
     */
    public boolean isFinalStatus() {
        return this == PAID || this == CANCELLED;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDetail() {
        return "报销单状态：" + name + " - " + description;
    }
}