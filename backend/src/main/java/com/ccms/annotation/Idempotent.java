package com.ccms.annotation;

import java.lang.annotation.*;

/**
 * 幂等性注解
 * 用于标记需要防重复提交的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    
    /**
     * 令牌过期时间（秒）
     * 默认5分钟
     */
    long expireTime() default 300;
    
    /**
     * 错误消息
     */
    String message() default "请勿重复提交";
}