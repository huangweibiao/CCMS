package com.ccms.controller.monitor;

import com.ccms.controller.BaseControllerTest;
import com.ccms.service.cache.CacheService;
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
 * 缓存管理控制器单元测试
 */
@WebMvcTest(CacheManagementController.class)
class CacheManagementControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CacheService cacheService;

    @MockBean
    private PerformanceMonitor performanceMonitor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldGetCacheStatsSuccessfully() throws Exception {
        // Given
        CacheService.CacheStats stats = new CacheService.CacheStats("system_config", 100, 85);
        when(cacheService.getCacheStats(anyString())).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/cache/stats"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.system_config").exists());
    }

    @Test
    void shouldClearCacheSuccessfully() throws Exception {
        // Given
        doNothing().when(cacheService).clear(eq("system_config"));

        // When & Then
        mockMvc.perform(delete("/api/monitor/cache/{cacheName}", "system_config"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldClearAllCachesSuccessfully() throws Exception {
        // Given
        doNothing().when(cacheService).clear(anyString());

        // When & Then
        mockMvc.perform(delete("/api/monitor/cache/all"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReloadSystemConfigCacheSuccessfully() throws Exception {
        // Given
        doNothing().when(cacheService).clear(eq("system_config"));

        // When & Then
        mockMvc.perform(post("/api/monitor/cache/system-config/reload"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReloadDataDictCacheSuccessfully() throws Exception {
        // Given
        doNothing().when(cacheService).clear(eq("data_dict"));

        // When & Then
        mockMvc.perform(post("/api/monitor/cache/data-dict/reload"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldPreloadCacheSuccessfully() throws Exception {
        // Given
        doNothing().when(cacheService).preloadCache(anyString(), any());

        // When & Then
        mockMvc.perform(post("/api/monitor/cache/preload/{cacheName}", "system_config"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetCacheDetailsSuccessfully() throws Exception {
        // Given
        CacheService.CacheStats stats = new CacheService.CacheStats("system_config", 100, 85);
        when(cacheService.getCacheStats(eq("system_config"))).thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/monitor/cache/{cacheName}/details", "system_config"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheName").value("system_config"));
    }

    @Test
    void shouldGetAllCacheNamesSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/monitor/cache/names"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetCacheValueSuccessfully() throws Exception {
        // Given
        when(cacheService.get(eq("system_config"), eq("setting"))).thenReturn("test_value");

        // When & Then
        mockMvc.perform(get("/api/monitor/cache/{cacheName}/{key}", "system_config", "setting"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("\"test_value\""));
    }

    @Test
    void shouldReturnNotFound_whenCacheKeyNotExists() throws Exception {
        // Given
        when(cacheService.get(eq("system_config"), eq("nonexistent"))).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/monitor/cache/{cacheName}/{key}", "system_config", "nonexistent"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCacheKeySuccessfully() throws Exception {
        // Given
        doNothing().when(cacheService).evict(eq("system_config"), eq("setting"));

        // When & Then
        mockMvc.perform(delete("/api/monitor/cache/{cacheName}/{key}", "system_config", "setting"))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
