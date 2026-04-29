package com.ccms.validation;

import java.util.HashMap;
import java.util.Map;

/**
 * 校验上下文，包含校验过程所需的信息
 */
public class ValidationContext {
    private Object target;
    private Map<String, Object> contextData;
    private Class<?> validatorClass;
    
    public ValidationContext(Object target) {
        this.target = target;
        this.contextData = new HashMap<>();
    }
    
    public ValidationContext(Object target, Map<String, Object> contextData) {
        this.target = target;
        this.contextData = contextData != null ? new HashMap<>(contextData) : new HashMap<>();
    }
    
    public Object getTarget() {
        return target;
    }
    
    public void setTarget(Object target) {
        this.target = target;
    }
    
    public Map<String, Object> getContextData() {
        return new HashMap<>(contextData);
    }
    
    public void setContextData(Map<String, Object> contextData) {
        this.contextData = contextData != null ? new HashMap<>(contextData) : new HashMap<>();
    }
    
    public void putContextData(String key, Object value) {
        contextData.put(key, value);
    }
    
    public Object getContextData(String key) {
        return contextData.get(key);
    }
    
    public Class<?> getValidatorClass() {
        return validatorClass;
    }
    
    public void setValidatorClass(Class<?> validatorClass) {
        this.validatorClass = validatorClass;
    }
    
    public boolean hasContextData(String key) {
        return contextData.containsKey(key);
    }
}