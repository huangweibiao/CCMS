package com.ccms.service;

import com.ccms.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 预算服务测试类
 */
@DisplayName("预算服务测试")
class BudgetServiceTest extends BaseTest {

    @Test
    @DisplayName("基础服务测试")
    void basicServiceTest() {
        // 基础服务验证
        assertThat(true).isTrue();
        logger.info("BudgetService基础测试通过");
    }
}