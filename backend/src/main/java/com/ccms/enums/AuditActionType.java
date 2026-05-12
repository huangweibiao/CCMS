package com.ccms.enums;

/**
 * 审计动作类型枚举
 * 定义审批系统中各种操作的类型
 */
public enum AuditActionType {
    
    /**
     * 创建审批实例
     */
    CREATE_INSTANCE("CREATE_INSTANCE", "创建审批实例"),
    
    /**
     * 创建审批记录
     */
    CREATE_RECORD("CREATE_RECORD", "创建审批记录"),
    
    /**
     * 审批通过
     */
    APPROVE("APPROVE", "审批通过"),
    
    /**
     * 审批驳回
     */
    REJECT("REJECT", "审批驳回"),
    
    /**
     * 转审
     */
    TRANSFER("TRANSFER", "转审"),
    
    /**
     * 取消审批
     */
    CANCEL("CANCEL", "取消审批"),
    
    /**
     * 跳过节点
     */
    SKIP_NODE("SKIP_NODE", "跳过节点"),
    
    /**
     * 状态变更
     */
    STATUS_CHANGE("STATUS_CHANGE", "状态变更"),
    
    /**
     * 流程配置变更
     */
    FLOW_CONFIG_CHANGE("FLOW_CONFIG_CHANGE", "流程配置变更"),
    
    /**
     * 其他操作
     */
    OTHER("OTHER", "其他操作");
    
    private final String code;
    private final String description;
    
    AuditActionType(String code, String description) {
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
    public static AuditActionType getByCode(String code) {
        for (AuditActionType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return OTHER;
    }
}