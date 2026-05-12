package com.ccms.enums;

/**
 * 业务类型枚举
 * 定义不同业务类型对应的审批流程
 */
public enum BusinessTypeEnum {
    EXPENSE_APPLY("EXPENSE_APPLY", "费用申请", "费用申请业务"),
    EXPENSE_REIMBURSE("EXPENSE_REIMBURSE", "费用报销", "费用报销业务"),
    LOAN("LOAN", "借款", "借款业务"),
    BUDGET_ADJUST("BUDGET_ADJUST", "预算调整", "预算调整业务"),
    OTHER("OTHER", "其他", "其他业务类型");

    private final String code;
    private final String name;
    private final String description;

    BusinessTypeEnum(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
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
    public static BusinessTypeEnum getByCode(String code) {
        for (BusinessTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}