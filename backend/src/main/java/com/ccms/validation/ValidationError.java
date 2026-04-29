package com.ccms.validation;

/**
 * 校验错误信息
 */
public class ValidationError {
    private ValidationRuleType ruleType;
    private String field;
    private String message;
    private Object invalidValue;
    
    public ValidationError(ValidationRuleType ruleType, String field, String message) {
        this.ruleType = ruleType;
        this.field = field;
        this.message = message;
    }
    
    public ValidationError(ValidationRuleType ruleType, String field, String message, Object invalidValue) {
        this.ruleType = ruleType;
        this.field = field;
        this.message = message;
        this.invalidValue = invalidValue;
    }
    
    public ValidationRuleType getRuleType() {
        return ruleType;
    }
    
    public void setRuleType(ValidationRuleType ruleType) {
        this.ruleType = ruleType;
    }
    
    public String getField() {
        return field;
    }
    
    public void setField(String field) {
        this.field = field;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Object getInvalidValue() {
        return invalidValue;
    }
    
    public void setInvalidValue(Object invalidValue) {
        this.invalidValue = invalidValue;
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s (字段: %s, 值: %s)", 
            ruleType.name(), message, field, invalidValue);
    }
}