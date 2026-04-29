package com.ccms.validation;

/**
 * 校验器接口
 */
public interface Validator<T> {
    
    /**
     * 校验目标对象
     * @param target 待校验的目标对象
     * @param context 校验上下文
     * @return 校验结果
     */
    ValidationResult validate(T target, ValidationContext context);
    
    /**
     * 校验目标对象（简化版本）
     * @param target 待校验的目标对象
     * @return 校验结果
     */
    default ValidationResult validate(T target) {
        return validate(target, new ValidationContext(target));
    }
    
    /**
     * 判断是否支持校验指定类型
     * @param clazz 目标类型
     * @return 是否支持
     */
    default boolean supports(Class<?> clazz) {
        return true; // 默认支持所有类型，子类可重写
    }
}