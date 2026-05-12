package com.ccms.validation;

import com.ccms.enums.ApprovalAction;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 审批操作类型验证器实现
 */
public class ApprovalActionValidator implements ConstraintValidator<ApprovalActionValid, String> {

    @Override
    public void initialize(ApprovalActionValid constraintAnnotation) {
        // 初始化方法
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }
        
        // 检查是否是有效的审批操作类型
        try {
            ApprovalAction.valueOf(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}