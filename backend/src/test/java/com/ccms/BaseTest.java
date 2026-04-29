package com.ccms;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * 基础测试类
 * 提供测试环境通用配置和工具方法
 */
@ActiveProfiles("test")
public abstract class BaseTest {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @BeforeEach
    public void setUp() {
        // 初始化Mockito注解
        MockitoAnnotations.openMocks(this);
        logger.info("{} 测试开始执行", getClass().getSimpleName());
    }
    

    
    /**
     * 创建默认的JWT token
     */
    protected String createTestToken() {
        return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MjQyNjIyfQ.test_signature";
    }
}