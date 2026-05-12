package com.ccms.enums;

/**
 * 审批操作枚举
 * 定义审批过程中的所有操作类型
 */
public enum ApprovalAction {
    /**
     * 提交审批
     */
    SUBMIT("SUBMIT", "提交审批"),
    
    /**
     * 同意
     */
    APPROVE("APPROVE", "同意"),
    
    /**
     * 拒绝
     */
    REJECT("REJECT", "拒绝"),
    
    /**
     * 转交
     */
    TRANSFER("TRANSFER", "转交"),
    
    /**
     * 跳过
     */
    SKIP("SKIP", "跳过"),
    
    /**
     * 取消
     */
    CANCEL("CANCEL", "取消"),
    
    /**
     * 撤回
     */
    WITHDRAW("WITHDRAW", "撤回"),
    
    /**
     * 重新提交
     */
    RESUBMIT("RESUBMIT", "重新提交"),
    
    /**
     * 终止
     */
    TERMINATE("TERMINATE", "终止"),
    
    /**
     * 完成
     */
    COMPLETE("COMPLETE", "完成");

    private final String code;
    private final String description;

    ApprovalAction(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ApprovalAction getByCode(String code) {
        for (ApprovalAction action : values()) {
            if (action.getCode().equals(code)) {
                return action;
            }
        }
        return null;
    }
}