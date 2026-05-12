package com.ccms.enums;

/**
 * 审批人类型枚举
 * 定义审批节点的审批人类型
 */
public enum ApproverType {
    /**
     * 指定用户 - 明确指定具体用户作为审批人
     */
    USER("USER", "指定用户"),
    
    /**
     * 角色 - 拥有特定角色的用户作为审批人
     */
    ROLE("ROLE", "角色"),
    
    /**
     * 部门 - 部门负责人作为审批人
     */
    DEPT("DEPT", "部门"),
    
    /**
     * 职位 - 特定职位的用户作为审批人
     */
    POSITION("POSITION", "职位"),
    
    /**
     * 自审批 - 发起人自己审批（用于测试或简单流程）
     */
    SELF("SELF", "自审批"),
    
    /**
     * 部门全体 - 部门内所有用户都可以审批
     */
    DEPARTMENT_ALL("DEPARTMENT_ALL", "部门全体"),
    
    /**
     * 申请人上级 - 申请人的直接上级审批
     */
    APPLICANT_SUPERIOR("APPLICANT_SUPERIOR", "申请人上级"),
    
    /**
     * 申请人部门领导 - 申请人所在部门的领导审批
     */
    DEPARTMENT_LEADER("DEPARTMENT_LEADER", "部门领导"),
    
    /**
     * 财务部门 - 财务部门相关人员审批
     */
    FINANCE_DEPT("FINANCE_DEPT", "财务部门"),
    
    /**
     * 高层管理 - 高层管理人员审批
     */
    SENIOR_MANAGEMENT("SENIOR_MANAGEMENT", "高层管理");

    private final String code;
    private final String description;

    ApproverType(String code, String description) {
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