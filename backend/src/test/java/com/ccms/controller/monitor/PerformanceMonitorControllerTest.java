package com.ccms.controller.monitor;

import com.ccms.controller.BaseControllerTest;
import com.ccms.service.monitor.PerformanceMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 性能监控控制器单元测试
 */
@WebMvcTest(PerformanceMonitorController.class)
class PerformanceMonitorControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PerformanceMonitor performanceMonitor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldGetMethodStatsSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.MethodStats> stats = new HashMap<>();
        PerformanceMonitor.MethodStats methodStats = new PerformanceMonitor.MethodStats();
        methodStats.setCallCount(100);
        methodStats.setAverageTime(50);
        stats.put("com.ccms.service.UserService.getUser", methodStats);
        when(performanceMonitor.getMethodStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/methods"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['com.ccms.service.UserService.getUser'].callCount").value(100));
    }

    @Test
    void shouldGetQueryStatsSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.QueryStats> stats = new HashMap<>();
        PerformanceMonitor.QueryStats queryStats = new PerformanceMonitor.QueryStats();
        queryStats.setQueryCount(50);
        queryStats.setTotalTime(1000);
        stats.put("SELECT * FROM sys_user", queryStats);
        when(performanceMonitor.getQueryStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/queries"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['SELECT * FROM sys_user'].queryCount").value(50));
    }

    @Test
    void shouldGetCacheStatsSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.CacheStats> stats = new HashMap<>();
        PerformanceMonitor.CacheStats cacheStats = new PerformanceMonitor.CacheStats();
        cacheStats.setHitCount(80);
        cacheStats.setMissCount(20);
        stats.put("user_permissions", cacheStats);
        when(performanceMonitor.getCacheStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/cache"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_permissions.hitCount").value(80));
    }

    @Test
    void shouldGenerateReportSuccessfully() throws Exception {
        // Given
        PerformanceMonitor.PerformanceReport report = new PerformanceMonitor.PerformanceReport();
        report.setTotalMethodCalls(1000);
        report.setTotalQueryCount(500);
        when(performanceMonitor.generateReport()).thenReturn(report);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/report"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMethodCalls").value(1000));
    }

    @Test
    void shouldResetStatsSuccessfully() throws Exception {
        // Given
        doNothing().when(performanceMonitor).resetAllStats();

        // When & Then
        mockMvc.perform(post("/api/monitor/performance/reset"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetSlowMethodsSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.MethodStats> stats = new HashMap<>();
        PerformanceMonitor.MethodStats slowMethod = new PerformanceMonitor.MethodStats();
        slowMethod.setAverageTime(1500);
        slowMethod.setCallCount(10);
        stats.put("slowMethod", slowMethod);
        when(performanceMonitor.getMethodStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/slow-methods")
                        .param("slowThreshold", "1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slowMethod.averageTime").value(1500));
    }

    @Test
    void shouldGetHighFrequencyMethodsSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.MethodStats> stats = new HashMap<>();
        PerformanceMonitor.MethodStats highFreqMethod = new PerformanceMonitor.MethodStats();
        highFreqMethod.setCallCount(200);
        highFreqMethod.setAverageTime(10);
        stats.put("highFreqMethod", highFreqMethod);
        when(performanceMonitor.getMethodStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/high-frequency")
                        .param("frequencyThreshold", "100"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highFreqMethod.callCount").value(200));
    }

    @Test
    void shouldGetDatabaseHealthSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.QueryStats> stats = new HashMap<>();
        PerformanceMonitor.QueryStats queryStats = new PerformanceMonitor.QueryStats();
        queryStats.setQueryCount(100);
        queryStats.setTotalTime(5000);
        queryStats.setMaxTime(800);
        stats.put("query1", queryStats);
        when(performanceMonitor.getQueryStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/database-health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQueries").value(100));
    }

    @Test
    void shouldGetCacheHealthSuccessfully() throws Exception {
        // Given
        Map<String, PerformanceMonitor.CacheStats> stats = new HashMap<>();
        PerformanceMonitor.CacheStats cacheStats = new PerformanceMonitor.CacheStats();
        cacheStats.setHitCount(90);
        cacheStats.setMissCount(10);
        stats.put("cache1", cacheStats);
        when(performanceMonitor.getCacheStats()).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/performance/cache-health"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCacheCount").value(1));
    }
}
