package com.ccms;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * 基础测试类
 */
@ActiveProfiles("test")
public abstract class BaseTest {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
        logger.info("{} 测试开始执行", getClass().getSimpleName());
    }

    protected String createTestToken() {
        return "Bearer test-token";
    }
}
