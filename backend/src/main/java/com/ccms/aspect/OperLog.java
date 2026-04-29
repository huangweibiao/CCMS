package com.ccms.aspect;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 用于标记需要记录操作日志的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperLog {
    
    /**
     * 模块标题
     */
    String title() default "";
    
    /**
     * 业务类型
     */
    BusinessType businessType() default BusinessType.OTHER;
    
    /**
     * 是否保存请求参数
     */
    boolean saveRequestParam() default true;
    
    /**
     * 是否保存返回参数
     */
    boolean saveResponseParam() default false;
    
    /**
     * 业务模块
     */
    String businessModule() default "";
    
    /**
     * 业务ID（SpEL表达式）
     */
    String businessId() default "";
    
    /**
     * 业务类型枚举
     */
    enum BusinessType {
        INSERT(0, "新增"),
        UPDATE(1, "修改"),
        DELETE(2, "删除"),
        SELECT(3, "查询"),
        IMPORT(4, "导入"),
        EXPORT(5, "导出"),
        OTHER(9, "其他");
        
        private final int value;
        private final String description;
        
        BusinessType(int value, String description) {
            this.value = value;
            this.description = description;
        }
        
        public int getValue() {
            return value;
        }
        
        public String getDescription() {
            return description;
        }
    }
}