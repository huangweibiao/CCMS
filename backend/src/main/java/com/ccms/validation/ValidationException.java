package com.ccms.validation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 校验异常
 */
public class ValidationException extends RuntimeException {
    private ValidationResult validationResult;
    
    public ValidationException(ValidationResult validationResult) {
        super(buildMessage(validationResult));
        this.validationResult = validationResult;
    }
    
    public ValidationException(String message) {
        super(message);
        this.validationResult = new ValidationResult();
        this.validationResult.addError(ValidationRuleType.NOT_NULL, "general", message);
    }
    
    public ValidationResult getValidationResult() {
        return validationResult;
    }
    
    public List<String> getErrorMessages() {
        return validationResult.getAllErrorMessages();
    }
    
    public String getFirstErrorMessage() {
        return validationResult.getFirstErrorMessage();
    }
    
    private static String buildMessage(ValidationResult validationResult) {
        if (validationResult == null || validationResult.getErrors().isEmpty()) {
            return "校验失败";
        }
        
        return validationResult.getErrors().stream()
            .map(ValidationError::getMessage)
            .collect(Collectors.joining("; "));
    }
}