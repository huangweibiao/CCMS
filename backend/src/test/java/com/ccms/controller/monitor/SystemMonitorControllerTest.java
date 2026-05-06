package com.ccms.controller.monitor;

import com.ccms.controller.BaseControllerTest;
import com.ccms.service.schedule.ScheduledTaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系统监控控制器单元测试
 */
@WebMvcTest(SystemMonitorController.class)
class SystemMonitorControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private ScheduledTaskService scheduledTaskService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldGetSystemInfoSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/monitor/system/info"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.systemTime").exists())
                .andExpect(jsonPath("$.data.jvmUptime").exists())
                .andExpect(jsonPath("$.data.memory").exists());
    }

    @Test
    void shouldGetDatabaseStatusSuccessfully() throws Exception {
        // Given
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM (SELECT 1) AS test"), eq(Integer.class)))
                .thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM sys_user"), eq(Integer.class)))
                .thenReturn(100);

        // When & Then
        mockMvc.perform(get("/api/monitor/database/status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.connection").value("healthy"));
    }

    @Test
    void shouldReturnUnhealthy_whenDatabaseConnectionFails() throws Exception {
        // Given
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        // When & Then
        mockMvc.perform(get("/api/monitor/database/status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.connection").value("unhealthy"));
    }

    @Test
    void shouldGetCacheStatusSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/monitor/cache/status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.cacheType").exists());
    }

    @Test
    void shouldGetTaskStatusSuccessfully() throws Exception {
        // Given
        when(scheduledTaskService.getLastExecutionInfo()).thenReturn("Last execution: 2025-01-15 10:00:00");

        // When & Then
        mockMvc.perform(get("/api/monitor/task/status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.lastTaskExecution").exists());
    }

    @Test
    void shouldHealthCheckSuccessfully() throws Exception {
        // Given
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class))).thenReturn(1);

        // When & Then
        mockMvc.perform(get("/api/monitor/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("UP"))
                .andExpect(jsonPath("$.data.components.database").value("UP"));
    }

    @Test
    void shouldReturnDegraded_whenDatabaseIsDown() throws Exception {
        // Given
        when(jdbcTemplate.queryForObject(eq("SELECT 1"), eq(Integer.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(get("/api/monitor/health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("DEGRADED"))
                .andExpect(jsonPath("$.data.components.database").value("DOWN"));
    }
}
