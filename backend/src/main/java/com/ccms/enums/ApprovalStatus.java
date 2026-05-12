package com.ccms.enums;

/**
 * 审批状态枚举
 * 定义审批流程的所有状态
 */
public enum ApprovalStatus {
    PENDING("PENDING", "待审批", false),
    IN_PROGRESS("IN_PROGRESS", "审批中", false),
    DRAFT("DRAFT", "草稿", false),
    APPROVING("APPROVING", "审批中", false),
    RUNNING("RUNNING", "运行中", false),
    TIMEOUT("TIMEOUT", "超时", true),
    APPROVED("APPROVED", "审批通过", true),
    REJECTED("REJECTED", "审批拒绝", true),
    CANCELED("CANCELED", "审批取消", true),
    WITHDRAWN("WITHDRAWN", "审批撤回", true),
    TERMINATED("TERMINATED", "审批终止", true);

    private final String code;
    private final String description;
    private final boolean finalStatus;

    ApprovalStatus(String code, String description, boolean finalStatus) {
        this.code = code;
        this.description = description;
        this.finalStatus = finalStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 判断是否是最终状态（不可再变更的状态）
     */
    public boolean isFinalStatus() {
        return finalStatus;
    }

    /**
     * 根据状态码获取枚举
     */
    public static ApprovalStatus getByCode(String code) {
        for (ApprovalStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}