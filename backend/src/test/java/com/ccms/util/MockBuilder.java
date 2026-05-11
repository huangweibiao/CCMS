package com.ccms.util;

import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Mock对象构建工具类
 * 用于简化Mock对象的创建和配置
 */
public class MockBuilder {

    /**
     * 创建Mock对象
     */
    public static <T> T mock(Class<T> classToMock) {
        return Mockito.mock(classToMock);
    }

    /**
     * 创建带有预设行为的Mock对象
     */
    public static <T> T mockWithBehavior(Class<T> classToMock, java.util.function.Consumer<T> behaviorSetter) {
        T mock = Mockito.mock(classToMock);
        if (behaviorSetter != null) {
            behaviorSetter.accept(mock);
        }
        return mock;
    }

    /**
     * 创建Optional.empty()的Mock
     */
    public static <T> Optional<T> emptyOptional() {
        return Optional.empty();
    }

    /**
     * 创建包含指定对象的Optional Mock
     */
    public static <T> Optional<T> optionalOf(T value) {
        return Optional.ofNullable(value);
    }

    /**
     * 创建空列表Mock
     */
    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    /**
     * 创建包含指定元素的列表Mock
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }

    /**
     * 创建单元素列表Mock
     */
    public static <T> List<T> singleList(T element) {
        return Collections.singletonList(element);
    }

    /**
     * 创建分页结果Mock
     */
    public static <T> org.springframework.data.domain.Page<T> pageOf(List<T> content, int page, int size, long total) {
        return new org.springframework.data.domain.PageImpl<>(
                content,
                org.springframework.data.domain.PageRequest.of(page, size),
                total
        );
    }

    /**
     * 创建空分页结果Mock
     */
    public static <T> org.springframework.data.domain.Page<T> emptyPage(int page, int size) {
        return pageOf(Collections.emptyList(), page, size, 0);
    }

    /**
     * 创建成功的响应Map
     */
    public static java.util.Map<String, Object> successResponse(Object data) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "操作成功");
        response.put("data", data);
        return response;
    }

    /**
     * 创建失败的响应Map
     */
    public static java.util.Map<String, Object> failureResponse(String message) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return response;
    }

    /**
     * 创建统计信息Map
     */
    public static java.util.Map<String, Object> statistics(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("键值对数量必须为偶数");
        }
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            String key = keyValuePairs[i];
            String value = keyValuePairs[i + 1];
            
            // 尝试将值转换为适当的类型
            Object parsedValue = parseValue(value);
            stats.put(key, parsedValue);
        }
        return stats;
    }

    /**
     * 解析字符串值为适当的类型
     */
    private static Object parseValue(String value) {
        if (value == null) {
            return null;
        }
        
        // 尝试解析为数字
        try {
            if (value.contains(".")) {
                return new BigDecimal(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            // 不是数字，返回字符串
            return value;
        }
    }

    /**
     * 创建通用的实体Mock配置器
     */
    public static <T> java.util.function.Consumer<T> defaultEntityMock() {
        return mock -> {
            // 可以在这里设置通用的Mock行为
            if (mock instanceof com.ccms.entity.BaseEntity) {
                com.ccms.entity.BaseEntity entity = (com.ccms.entity.BaseEntity) mock;
                entity.setId(TestDataFactory.generateId());
                entity.setCreateTime(LocalDateTime.now());
                entity.setUpdateTime(LocalDateTime.now());
            }
        };
    }

    /**
     * 创建Repository Mock配置器
     */
    public static <T> java.util.function.Consumer<T> repositoryMock() {
        return mock -> {
            // 可以在这里设置Repository特定的Mock行为
        };
    }

    /**
     * 创建Service Mock配置器
     */
    public static <T> java.util.function.Consumer<T> serviceMock() {
        return mock -> {
            // 可以在这里设置Service特定的Mock行为
        };
    }

    /**
     * 链式Mock构建器
     */
    public static class ChainBuilder<T> {
        private final T mock;
        
        private ChainBuilder(T mock) {
            this.mock = mock;
        }
        
        public <R> ChainBuilder<T> when(java.util.function.Function<T, R> methodCall) {
            return this;
        }
        
        public <R> ChainBuilder<T> thenReturn(R value) {
            return this;
        }
        
        public T build() {
            return mock;
        }
    }
    
    /**
     * 开始链式Mock构建
     */
    public static <T> ChainBuilder<T> mockChain(Class<T> classToMock) {
        T mock = Mockito.mock(classToMock);
        return new ChainBuilder<>(mock);
    }

    /**
     * 创建验证规则Mock
     */
    public static java.util.Map<String, Object> validationError(String field, String message) {
        java.util.Map<String, Object> error = new java.util.HashMap<>();
        error.put("field", field);
        error.put("message", message);
        return error;
    }

    /**
     * 创建多个验证错误
     */
    public static List<java.util.Map<String, Object>> validationErrors(java.util.Map<String, String>... errors) {
        return Arrays.stream(errors)
                .map(error -> validationError(error.get("field"), error.get("message")))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 创建权限检查结果
     */
    public static java.util.Map<String, Object> permissionCheck(boolean hasPermission, String permissionCode) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("hasPermission", hasPermission);
        result.put("permissionCode", permissionCode);
        return result;
    }

    /**
     * 创建用户权限上下文
     */
    public static java.util.Map<String, Object> userContext(Long userId, String username, List<String> roles) {
        java.util.Map<String, Object> context = new java.util.HashMap<>();
        context.put("userId", userId);
        context.put("username", username);
        context.put("roles", roles);
        context.put("permissions", Collections.emptyList());
        return context;
    }
}