package com.ccms.validation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 基础校验器实现
 */
public class BasicValidator implements Validator<Object> {
    
    @Override
    public ValidationResult validate(Object target, ValidationContext context) {
        ValidationResult result = new ValidationResult();
        
        // 这里可以实现一些通用的基础校验逻辑
        if (target == null) {
            result.addError(ValidationRuleType.NOT_NULL, "target", "校验对象不能为空");
        }
        
        return result;
    }
    
    /**
     * 校验字段不能为空
     */
    public ValidationResult validateNotNull(Object value, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (value == null) {
            result.addError(ValidationRuleType.NOT_NULL, fieldName, 
                fieldName + "不能为空");
        }
        return result;
    }
    
    /**
     * 校验字符串不能为空或空白
     */
    public ValidationResult validateNotBlank(String value, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (value == null || value.trim().isEmpty()) {
            result.addError(ValidationRuleType.NOT_BLANK, fieldName, 
                fieldName + "不能为空");
        }
        return result;
    }
    
    /**
     * 校验金额不能为负数
     */
    public ValidationResult validateAmountPositive(BigDecimal amount, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (amount != null && amount.compareTo(BigDecimal.ZERO) < 0) {
            result.addError(ValidationRuleType.AMOUNT_NOT_NEGATIVE, fieldName, 
                fieldName + "不能为负数");
        }
        return result;
    }
    
    /**
     * 校验金额范围
     */
    public ValidationResult validateAmountRange(BigDecimal amount, BigDecimal min, BigDecimal max, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (amount != null) {
            if (min != null && amount.compareTo(min) < 0) {
                result.addError(ValidationRuleType.MIN_VALUE, fieldName, 
                    fieldName + "不能小于" + min);
            }
            if (max != null && amount.compareTo(max) > 0) {
                result.addError(ValidationRuleType.MAX_VALUE, fieldName, 
                    fieldName + "不能大于" + max);
            }
        }
        return result;
    }
    
    /**
     * 校验日期范围
     */
    public ValidationResult validateDateRange(LocalDate date, LocalDate minDate, LocalDate maxDate, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (date != null) {
            if (minDate != null && date.isBefore(minDate)) {
                result.addError(ValidationRuleType.EXPENSE_DATE_VALID, fieldName, 
                    fieldName + "不能早于" + minDate);
            }
            if (maxDate != null && date.isAfter(maxDate)) {
                result.addError(ValidationRuleType.EXPENSE_DATE_VALID, fieldName, 
                    fieldName + "不能晚于" + maxDate);
            }
        }
        return result;
    }
    
    /**
     * 校验数值范围
     */
    public ValidationResult validateNumberRange(Number value, Number min, Number max, String fieldName) {
        ValidationResult result = new ValidationResult();
        if (value != null) {
            double doubleValue = value.doubleValue();
            if (min != null && doubleValue < min.doubleValue()) {
                result.addError(ValidationRuleType.MIN_VALUE, fieldName, 
                    fieldName + "不能小于" + min);
            }
            if (max != null && doubleValue > max.doubleValue()) {
                result.addError(ValidationRuleType.MAX_VALUE, fieldName, 
                    fieldName + "不能大于" + max);
            }
        }
        return result;
    }
}