package com.ccms.validation;

import com.ccms.enums.BusinessType;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 业务类型验证器实现
 */
public class BusinessTypeValidator implements ConstraintValidator<BusinessTypeValid, String> {

    @Override
    public void initialize(BusinessTypeValid constraintAnnotation) {
        // 初始化方法
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        
        // 检查是否是有效的业务类型
        try {
            BusinessType.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}