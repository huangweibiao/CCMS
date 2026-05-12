package com.ccms.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 审批操作类型自定义验证注解
 * 用于验证审批操作参数是否符合定义
 */
@Documented
@Constraint(validatedBy = ApprovalActionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApprovalActionValid {
    
    String message() default "无效的审批操作类型";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}