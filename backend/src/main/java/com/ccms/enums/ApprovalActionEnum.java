package com.ccms.enums;

/**
 * 审批动作枚举
 * 定义审批人执行的不同操作类型
 */
public enum ApprovalActionEnum {
    APPROVE(1, "同意", "同意审批请求"),
    REJECT(2, "驳回", "驳回审批请求"),
    TRANSFER(3, "转审", "转交给其他人审批"),
    CANCEL(4, "撤销", "撤销审批操作"),
    SKIP(5, "跳过", "跳过当前节点");

    private final Integer code;
    private final String name;
    private final String description;

    ApprovalActionEnum(Integer code, String name, String description) {
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
    public static ApprovalActionEnum getByCode(Integer code) {
        for (ApprovalActionEnum action : values()) {
            if (action.getCode().equals(code)) {
                return action;
            }
        }
        return null;
    }

    /**
     * 判断是否为终结操作
     */
    public boolean isFinalAction() {
        return this == APPROVE || this == REJECT || this == CANCEL;
    }

    /**
     * 判断是否为继续流程的操作
     */
    public boolean isContinueAction() {
        return this == TRANSFER || this == SKIP;
    }
}