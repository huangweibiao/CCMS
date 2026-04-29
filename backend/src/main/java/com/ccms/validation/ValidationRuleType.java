package com.ccms.validation;

/**
 * 校验规则类型枚举
 */
public enum ValidationRuleType {
    // 基本规则
    NOT_NULL("字段不能为空"),
    NOT_BLANK("字段不能为空字符串"),
    EMAIL("邮箱格式不正确"),
    PHONE("手机号格式不正确"),
    
    // 数值规则
    MIN_VALUE("数值不能小于最小值"),
    MAX_VALUE("数值不能大于最大值"),
    POSITIVE("数值必须为正数"),
    
    // 金额规则
    AMOUNT_NOT_NEGATIVE("金额不能为负数"),
    AMOUNT_MAX_LIMIT("金额超过限制"),
    
    // 费用申请规则
    EXPENSE_TYPE_VALID("费用类型不合法"),
    EXPENSE_DATE_VALID("费用日期不合法"),
    
    // 业务状态规则
    STATUS_TRANSITION_VALID("状态流转不合法"),
    OPERATION_PERMISSION_VALID("操作权限不足"),
    
    // 关联数据规则
    RELATED_ENTITY_EXISTS("关联数据不存在"),
    BUDGET_AVAILABLE("预算不足"),
    
    // 发票规则
    INVOICE_AMOUNT_VALID("发票金额格式不正确"),
    INVOICE_TYPE_MATCH("发票类型不匹配");
    
    private final String description;
    
    ValidationRuleType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}