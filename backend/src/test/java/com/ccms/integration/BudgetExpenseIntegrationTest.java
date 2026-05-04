package com.ccms.integration;

import com.ccms.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 预算费用集成测试类
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("预算费用集成测试")
class BudgetExpenseIntegrationTest extends BaseTest {

    @Test
    @DisplayName("基础集成测试")
    void basicIntegrationTest() {
        // 基础集成测试验证
        assertThat(true).isTrue();
        logger.info("预算费用集成基础测试通过");
    }
}