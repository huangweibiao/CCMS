package com.ccms;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * 服务层测试基类
 * 提供服务层测试的通用配置和工具方法
 */
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public abstract class BaseServiceTest {
    
    /**
     * 验证对象相等性的工具方法
     */
    protected <T> void assertObjectEquals(T expected, T actual, String message) {
        assert expected != null : "预期对象不能为空";
        assert actual != null : "实际对象不能为空";
        assert expected.equals(actual) : message + ", 预期: " + expected + ", 实际: " + actual;
    }
    
    /**
     * 验证字符串不为空
     */
    protected void assertNotEmpty(String value, String message) {
        assert value != null && !value.trim().isEmpty() : message + "不能为空";
    }
    
    /**
     * 验证数字大于0
     */
    protected void assertPositive(Number value, String message) {
        assert value != null && value.doubleValue() > 0 : message + "必须大于0";
    }
    
    /**
     * 验证数字大于等于0
     */
    protected void assertNotNegative(Number value, String message) {
        assert value != null && value.doubleValue() >= 0 : message + "不能为负数";
    }
    
    /**
     * 验证布尔值为true
     */
    protected void assertTrue(boolean condition, String message) {
        assert condition : message;
    }
    
    /**
     * 验证布尔值为false
     */
    protected void assertFalse(boolean condition, String message) {
        assert !condition : message;
    }
    
    /**
     * 验证对象不为null
     */
    protected void assertNotNull(Object obj, String message) {
        assert obj != null : message + "不能为null";
    }
    
    /**
     * 验证对象为null
     */
    protected void assertNull(Object obj, String message) {
        assert obj == null : message + "应该为null";
    }
}