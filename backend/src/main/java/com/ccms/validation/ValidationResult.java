package com.ccms.validation;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验结果
 */
public class ValidationResult {
    private boolean valid;
    private List<ValidationError> errors;
    
    public ValidationResult() {
        this.valid = true;
        this.errors = new ArrayList<>();
    }
    
    public void addError(ValidationError error) {
        this.errors.add(error);
        this.valid = false;
    }
    
    public void addError(ValidationRuleType ruleType, String field, String message) {
        addError(new ValidationError(ruleType, field, message));
    }
    
    public void addError(ValidationRuleType ruleType, String field, String message, Object invalidValue) {
        addError(new ValidationError(ruleType, field, message, invalidValue));
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public List<ValidationError> getErrors() {
        return new ArrayList<>(errors);
    }
    
    public String getFirstErrorMessage() {
        if (errors.isEmpty()) {
            return "";
        }
        return errors.get(0).getMessage();
    }
    
    public List<String> getAllErrorMessages() {
        List<String> messages = new ArrayList<>();
        for (ValidationError error : errors) {
            messages.add(error.getMessage());
        }
        return messages;
    }
    
    public ValidationResult combine(ValidationResult other) {
        if (other != null) {
            for (ValidationError error : other.errors) {
                this.addError(error);
            }
        }
        return this;
    }
}