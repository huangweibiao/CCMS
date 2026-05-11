package com.ccms.controller.monitor;

import com.ccms.controller.ControllerTestBase;
import com.ccms.service.cache.CacheService;
import com.ccms.service.monitor.PerformanceMonitor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 缓存管理控制器单元测试
 */
@WebMvcTest(CacheManagementController.class)
class CacheManagementControllerTest extends ControllerTestBase {

    @MockBean
    private CacheService cacheService;

    @MockBean
    private PerformanceMonitor performanceMonitor;

    @Test
    void shouldReturnCacheStats() throws Exception {
        CacheService.CacheStats stats = new CacheService.CacheStats("system_config", 10, 80);
        when(cacheService.getCacheStats(anyString())).thenReturn(stats);

        performGet("/api/monitor/cache/stats")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.system_config").exists())
                .andExpect(jsonPath("$.system_config.size").value(10))
                .andExpect(jsonPath("$.system_config.hitRate").value(80));
    }

    @Test
    void shouldClearCache() throws Exception {
        doNothing().when(cacheService).clear("system_config");

        performDelete("/api/monitor/cache/system_config")
                .andExpect(status().isOk());

        verify(cacheService, times(1)).clear("system_config");
    }

    @Test
    void shouldClearAllCaches() throws Exception {
        doNothing().when(cacheService).clear(anyString());

        performDelete("/api/monitor/cache/all")
                .andExpect(status().isOk());

        verify(cacheService, times(5)).clear(anyString());
    }

    @Test
    void shouldReloadSystemConfigCache() throws Exception {
        doNothing().when(cacheService).clear("system_config");

        performPost("/api/monitor/cache/system-config/reload")
                .andExpect(status().isOk());

        verify(cacheService, times(1)).clear("system_config");
    }

    @Test
    void shouldReloadDataDictCache() throws Exception {
        doNothing().when(cacheService).clear("data_dict");

        performPost("/api/monitor/cache/data-dict/reload")
                .andExpect(status().isOk());

        verify(cacheService, times(1)).clear("data_dict");
    }

    @Test
    void shouldPreloadCache() throws Exception {
        doNothing().when(cacheService).preloadCache(anyString(), any());

        performPost("/api/monitor/cache/preload/system_config")
                .andExpect(status().isOk());

        verify(cacheService, times(1)).preloadCache(eq("system_config"), any());
    }

    @Test
    void shouldReturnCacheDetails() throws Exception {
        CacheService.CacheStats stats = new CacheService.CacheStats("system_config", 2, 80);
        when(cacheService.getCacheStats("system_config")).thenReturn(stats);

        performGet("/api/monitor/cache/system_config/details")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cacheName").value("system_config"))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.hitCount").value(80))
                .andExpect(jsonPath("$.cacheKeys").exists());
    }

    @Test
    void shouldReturnAllCacheNames() throws Exception {
        performGet("/api/monitor/cache/names")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                .andExpect(jsonPath("$[0]").value("system_config"))
                .andExpect(jsonPath("$[1]").value("data_dict"));
    }

    @Test
    void shouldReturnCacheValue() throws Exception {
        when(cacheService.get("system_config", "test_key")).thenReturn("test_value");

        performGet("/api/monitor/cache/system_config/test_key")
                .andExpect(status().isOk())
                .andExpect(content().string("\"test_value\""));
    }

    @Test
    void shouldReturnNotFoundWhenCacheKeyNotExist() throws Exception {
        when(cacheService.get("system_config", "non_existent_key")).thenReturn(null);

        performGet("/api/monitor/cache/system_config/non_existent_key")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteCacheKey() throws Exception {
        doNothing().when(cacheService).evict("system_config", "test_key");

        performDelete("/api/monitor/cache/system_config/test_key")
                .andExpect(status().isOk());

        verify(cacheService, times(1)).evict("system_config", "test_key");
    }
}
