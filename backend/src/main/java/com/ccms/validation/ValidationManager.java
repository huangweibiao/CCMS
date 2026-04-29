package com.ccms.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 校验管理器
 */
@Component
public class ValidationManager {
    
    @Autowired(required = false)
    private List<Validator<?>> validators;
    
    /**
     * 校验对象
     */
    @SuppressWarnings("unchecked")
    public ValidationResult validate(Object target) {
        return validate(target, new ValidationContext(target));
    }
    
    /**
     * 校验对象（带上下文）
     */
    @SuppressWarnings("unchecked")
    public ValidationResult validate(Object target, ValidationContext context) {
        ValidationResult result = new ValidationResult();
        
        if (validators != null) {
            for (Validator<?> validator : validators) {
                if (validator.supports(target.getClass())) {
                    ValidationResult validatorResult = ((Validator<Object>) validator).validate(target, context);
                    result.combine(validatorResult);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 校验并抛出异常（校验失败时抛出）
     */
    public void validateAndThrow(Object target) throws ValidationException {
        validateAndThrow(target, new ValidationContext(target));
    }
    
    /**
     * 校验并抛出异常（带上下文）
     */
    public void validateAndThrow(Object target, ValidationContext context) throws ValidationException {
        ValidationResult result = validate(target, context);
        if (!result.isValid()) {
            throw new ValidationException(result);
        }
    }
    
    /**
     * 快速校验（仅返回布尔值）
     */
    public boolean quickValidate(Object target) {
        return validate(target).isValid();
    }
}