package com.ccms.integration;

import com.ccms.BaseTest;
import com.ccms.common.response.ApiResponse;
import com.ccms.controller.LoanController;
import com.ccms.controller.MessageNotifyController;
import com.ccms.controller.monitor.SystemMonitorController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 系统集成测试
 * 验证核心模块的基本功能和接口可用性
 */
@SpringBootTest
class SystemIntegrationTest extends BaseTest {

    @Autowired(required = false)
    private LoanController loanController;

    @Autowired(required = false)
    private MessageNotifyController messageNotifyController;

    @Autowired(required = false)
    private SystemMonitorController systemMonitorController;

    @Test
    void testContextLoads() {
        // 验证Spring上下文加载成功
        assertThat(loanController).isNotNull();
        assertThat(systemMonitorController).isNotNull();
    }

    @Test
    void testSystemMonitorEndpoint() {
        // 验证系统监控接口可用
        if (systemMonitorController != null) {
            ApiResponse<?> response = systemMonitorController.getSystemInfo();
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
        }
    }

    @Test
    void testHealthCheck() {
        // 验证健康检查接口
        if (systemMonitorController != null) {
            ApiResponse<?> response = systemMonitorController.healthCheck();
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
        }
    }

    @Test
    void testDatabaseStatus() {
        // 验证数据库状态接口
        if (systemMonitorController != null) {
            ApiResponse<?> response = systemMonitorController.getDatabaseStatus();
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
        }
    }
}