package com.ccms.enums;

/**
 * 审批人类型枚举
 * 定义不同类型的审批人配置
 */
public enum ApproverTypeEnum {
    USER("USER", "指定用户", "指定具体的用户作为审批人"),
    ROLE("ROLE", "角色", "指定角色下的用户作为审批人"),
    DEPT("DEPT", "部门", "指定部门下的用户作为审批人"),
    POSITION("POSITION", "职位", "指定职位下的用户作为审批人"),
    SELF("SELF", "自己", "申请人自己审批");

    private final String code;
    private final String name;
    private final String description;

    ApproverTypeEnum(String code, String name, String description) {
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
    public static ApproverTypeEnum getByCode(String code) {
        for (ApproverTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return USER;
    }
}