package com.ccms.enums;

/**
 * 审批状态枚举
 * 定义审批实例的不同状态
 */
public enum ApprovalStatusEnum {
    RUNNING(0, "运行中", "审批流程正在进行中"),
    APPROVED(1, "已通过", "审批流程已通过"),
    REJECTED(2, "已驳回", "审批流程被驳回"),
    CANCELED(3, "已撤销", "审批流程已撤销"),
    TIMEOUT(4, "已超时", "审批流程已超时"),
    TERMINATED(5, "已终止", "审批流程异常终止");

    private final Integer code;
    private final String name;
    private final String description;

    ApprovalStatusEnum(Integer code, String name, String description) {
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
     * 获取枚举值（兼容性方法，与getCode()相同）
     */
    public Integer getValue() {
        return code;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ApprovalStatusEnum getByCode(Integer code) {
        for (ApprovalStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为终态（审批结束）
     */
    public boolean isFinalStatus() {
        return this == APPROVED || this == REJECTED || this == CANCELED || this == TIMEOUT || this == TERMINATED;
    }
    
    /**
     * 根据状态码判断是否为终态
     */
    public static boolean isFinalStatus(Integer statusCode) {
        ApprovalStatusEnum status = getByCode(statusCode);
        return status != null && status.isFinalStatus();
    }

    /**
     * 判断是否为需要继续处理的状态
     */
    public boolean isActiveStatus() {
        return this == RUNNING;
    }
}