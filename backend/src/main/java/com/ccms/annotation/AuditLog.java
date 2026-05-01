package com.ccms.annotation;

import java.lang.annotation.*;

/**
 * 审计日志注解
 * 用于标记需要记录审计日志的业务方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuditLog {
    
    /**
     * 模块名称
     */
    String module() default "System";
    
    /**
     * 操作类型
     */
    String operation() default "";
    
    /**
     * 操作描述
     */
    String description() default "";
    
    /**
     * 是否记录请求参数
     */
    boolean logParams() default true;
    
    /**
     * 是否记录返回结果
     */
    boolean logResult() default false;
}