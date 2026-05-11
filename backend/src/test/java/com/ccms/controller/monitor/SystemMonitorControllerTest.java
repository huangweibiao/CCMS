package com.ccms.controller.monitor;

import com.ccms.controller.ControllerTestBase;
import com.ccms.service.schedule.ScheduledTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系统监控控制器单元测试
 */
@WebMvcTest(SystemMonitorController.class)
class SystemMonitorControllerTest extends ControllerTestBase {

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private ScheduledTaskService scheduledTaskService;

    @MockBean
    private CacheManager cacheManager;

    @Test
    void shouldReturnSystemInfo() throws Exception {
        performGet("/api/monitor/system/info")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.systemTime").exists())
                .andExpect(jsonPath("$.data.jvmUptime").exists())
                .andExpect(jsonPath("$.data.memory").exists())
                .andExpect(jsonPath("$.data.os").exists())
                .andExpect(jsonPath("$.data.availableProcessors").exists());
    }

    @Test
    void shouldReturnDatabaseStatusWhenHealthy() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (SELECT 1) AS test", Integer.class)).thenReturn(1);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class)).thenReturn(10);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense_reimburse", Integer.class)).thenReturn(5);
        when(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_attachment", Integer.class)).thenReturn(3);

        performGet("/api/monitor/database/status")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.connection").value("healthy"))
                .andExpect(jsonPath("$.data.testQuerySuccess").value(true))
                .andExpect(jsonPath("$.data.userCount").value(10))
                .andExpect(jsonPath("$.data.expenseCount").value(5))
                .andExpect(jsonPath("$.data.attachmentCount").value(3));
    }

    @Test
    void shouldReturnDatabaseStatusWhenUnhealthy() throws Exception {
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        performGet("/api/monitor/database/status")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.connection").value("unhealthy"))
                .andExpect(jsonPath("$.data.error").exists());
    }

    @Test
    void shouldReturnCacheStatus() throws Exception {
        performGet("/api/monitor/cache/status")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.cacheType").exists())
                .andExpect(jsonPath("$.data.cacheManager").exists())
                .andExpect(jsonPath("$.data.redis").exists());
    }

    @Test
    void shouldReturnTaskStatus() throws Exception {
        Map<String, Object> lastExecution = new HashMap<>();
        lastExecution.put("taskName", "testTask");
        lastExecution.put("lastRun", "2024-01-01T00:00:00");
        
        when(scheduledTaskService.getLastExecutionInfo()).thenReturn(lastExecution);

        performGet("/api/monitor/task/status")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.lastTaskExecution").exists());
    }

    @Test
    void shouldReturnHealthCheckWhenAllUp() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        performGet("/api/monitor/health")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.timestamp").exists())
                .andExpect(jsonPath("$.data.components.database").value("UP"))
                .andExpect(jsonPath("$.data.components.application").value("UP"));
    }

    @Test
    void shouldReturnHealthCheckWhenDatabaseDown() throws Exception {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new RuntimeException("Database connection failed"));

        performGet("/api/monitor/health")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("DEGRADED"))
                .andExpect(jsonPath("$.data.components.database").value("DOWN"));
    }
}
