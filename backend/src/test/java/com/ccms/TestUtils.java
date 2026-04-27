package com.ccms;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 测试工具类
 * 提供测试中使用的通用方法和辅助函数
 */
public class TestUtils {
    
    private TestUtils() {
        // 工具类，防止实例化
    }
    
    /**
     * 通过反射设置对象的私有字段值
     */
    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("设置私有字段失败: " + fieldName, e);
        }
    }
    
    /**
     * 通过反射获取对象的私有字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getPrivateField(Object target, String fieldName) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("获取私有字段失败: " + fieldName, e);
        }
    }
    
    /**
     * 创建测试时间范围
     */
    public static LocalDateTime[] createTestTimeRange() {
        LocalDateTime startTime = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2024, 12, 31, 23, 59);
        return new LocalDateTime[]{startTime, endTime};
    }
    
    /**
     * 创建测试JSON字符串
     */
    public static class JsonBuilder {
        
        public static String createLoginRequest(String username, String password) {
            return String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password);
        }
        
        public static String createExpenseApplyRequest(Double amount, String reason) {
            return String.format("{\"amount\": %s, \"reason\": \"%s\"}", amount, reason);
        }
        
        public static String createBudgetRequest(String department, Integer year, Integer month, Double totalAmount) {
            return String.format("{\"department\": \"%s\", \"year\": %d, \"month\": %d, \"totalAmount\": %s}", 
                department, year, month, totalAmount);
        }
    }
    
    /**
     * 常用的测试数据常量
     */
    public static class TestData {
        public static final Long VALID_USER_ID = 1L;
        public static final Long INVALID_USER_ID = 999L;
        public static final String VALID_USERNAME = "testuser";
        public static final String INVALID_USERNAME = "nonexistent";
        public static final String VALID_PASSWORD = "password";
        public static final String INVALID_PASSWORD = "wrongpassword";
        public static final String ADMIN_ROLE = "admin";
        public static final String USER_ROLE = "user";
        public static final Double VALID_AMOUNT = 1000.0;
        public static final Double INVALID_AMOUNT = -1000.0;
        public static final String VALID_DEPARTMENT = "测试部门";
        public static final String INVALID_DEPARTMENT = "不存在的部门";
        
        // 审批状态常量
        public static final Integer STATUS_PENDING = 1;
        public static final Integer STATUS_APPROVED = 2;
        public static final Integer STATUS_REJECTED = 3;
        public static final Integer STATUS_COMPLETED = 4;
        
        // 常见的错误消息
        public static final String ERROR_USER_NOT_FOUND = "用户不存在";
        public static final String ERROR_INVALID_PASSWORD = "密码错误";
        public static final String ERROR_INSUFFICIENT_BUDGET = "预算不足";
        public static final String ERROR_INVALID_STATUS = "状态不合法";
    }
    
    /**
     * 测试注解常量
     */
    public static class TestAnnotations {
        @AutoConfigureMockMvc
        @ExtendWith({SpringExtension.class, MockitoExtension.class})
        public @interface ControllerTestConfig {}
        
        @ExtendWith(MockitoExtension.class)
        public @interface ServiceTestConfig {}
    }
    
    /**
     * 常用的测试验证方法
     */
    public static class Assertions {
        
        public static <T> void assertResponseContains(List<T> items, T expectedItem) {
            if (!items.contains(expectedItem)) {
                throw new AssertionError("期望的元素不在响应列表中: " + expectedItem);
            }
        }
        
        public static void assertValidId(Long id) {
            if (id == null || id <= 0) {
                throw new AssertionError("ID必须大于0: " + id);
            }
        }
        
        public static void assertValidAmount(Double amount) {
            if (amount == null || amount <= 0) {
                throw new AssertionError("金额必须大于0: " + amount);
            }
        }
    }
}