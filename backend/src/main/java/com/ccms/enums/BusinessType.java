package com.ccms.enums;

/**
 * 业务类型枚举
 * 定义系统支持的所有业务类型
 */
public enum BusinessType {
    /**
     * 费用报销
     */
    EXPENSE_REIMBURSEMENT("EXPENSE_REIMBURSEMENT", "费用报销"),
    
    /**
     * 差旅申请
     */
    TRAVEL_APPLY("TRAVEL_APPLY", "差旅申请"),
    
    /**
     * 采购申请
     */
    PURCHASE_APPLY("PURCHASE_APPLY", "采购申请"),
    
    /**
     * 合同审批
     */
    CONTRACT_APPROVAL("CONTRACT_APPROVAL", "合同审批"),
    
    /**
     * 请假申请
     */
    LEAVE_APPLY("LEAVE_APPLY", "请假申请"),
    
    /**
     * 加班申请
     */
    OVERTIME_APPLY("OVERTIME_APPLY", "加班申请"),
    
    /**
     * 物品领用
     */
    ITEM_RECEPTION("ITEM_RECEPTION", "物品领用"),
    
    /**
     * 费用预算
     */
    EXPENSE_BUDGET("EXPENSE_BUDGET", "费用预算"),
    
    /**
     * 资产处置
     */
    ASSET_DISPOSAL("ASSET_DISPOSAL", "资产处置"),
    
    /**
     * 项目立项
     */
    PROJECT_APPROVAL("PROJECT_APPROVAL", "项目立项"),
    
    /**
     * 借款业务
     */
    LOAN("LOAN", "借款");

    private final String code;
    private final String description;

    BusinessType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}