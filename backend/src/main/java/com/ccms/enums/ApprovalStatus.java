package com.ccms.enums;

/**
 * 审批状态枚举
 * 定义审批流程的所有状态
 */
public enum ApprovalStatus {
    /**
     * 草稿状态 - 流程创建但未提交
     */
    DRAFT("DRAFT", "草稿") {
        @Override
        public boolean isFinalStatus() {
            return false;
        }
    },
    
    /**
     * 审批中 - 流程已提交，正在审批流程中
     */
    APPROVING("APPROVING", "审批中") {
        @Override
        public boolean isFinalStatus() {
            return false;
        }
    },
    
    /**
     * 审批通过 - 流程已审批通过
     */
    APPROVED("APPROVED", "审批通过") {
        @Override
        public boolean isFinalStatus() {
            return true;
        }
    },
    
    /**
     * 审批拒绝 - 流程被拒绝
     */
    REJECTED("REJECTED", "审批拒绝") {
        @Override
        public boolean isFinalStatus() {
            return true;
        }
    },
    
    /**
     * 审批取消 - 流程被取消
     */
    CANCELED("CANCELED", "审批取消") {
        @Override
        public boolean isFinalStatus() {
            return true;
        }
    },
    
    /**
     * 审批撤回 - 流程被申请人撤回
     */
    WITHDRAWN("WITHDRAWN", "审批撤回") {
        @Override
        public boolean isFinalStatus() {
            return true;
        }
    },
    
    /**
     * 审批终止 - 流程被系统终止
     */
    TERMINATED("TERMINATED", "审批终止") {
        @Override
        public boolean isFinalStatus() {
            return true;
        }
    };

    private final String code;
    private final String description;

    ApprovalStatus(String code, String description) {
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
     * 判断是否是最终状态（不可再变更的状态）
     */
    public abstract boolean isFinalStatus();
}