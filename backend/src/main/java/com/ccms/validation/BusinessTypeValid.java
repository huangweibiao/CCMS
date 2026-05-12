package com.ccms.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 业务类型自定义验证注解
 * 用于验证业务类型参数是否符合系统定义
 */
@Documented
@Constraint(validatedBy = BusinessTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessTypeValid {
    
    String message() default "无效的业务类型";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}