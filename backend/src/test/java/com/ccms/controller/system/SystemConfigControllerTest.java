package com.ccms.controller.system;

import com.ccms.controller.BaseControllerTest;
import com.ccms.entity.system.SystemConfig;
import com.ccms.service.system.SystemConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系统配置控制器单元测试
 */
@WebMvcTest(SystemConfigController.class)
class SystemConfigControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SystemConfigService systemConfigService;

    @Autowired
    private ObjectMapper objectMapper;

    private SystemConfig testConfig;

    @BeforeEach
    void setUp() {
        testConfig = createTestConfig();
    }

    @Test
    void shouldGetAllConfigsSuccessfully() throws Exception {
        // Given
        List<SystemConfig> configs = Arrays.asList(testConfig, createTestConfig(2L, "APP_NAME", "CCMS"));
        when(systemConfigService.getAllEnabledConfigs()).thenReturn(configs);

        // When & Then
        mockMvc.perform(get("/api/system/config"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetConfigValueSuccessfully() throws Exception {
        // Given
        when(systemConfigService.getConfigValue(eq("MAX_UPLOAD_SIZE"))).thenReturn("10485760");

        // When & Then
        mockMvc.perform(get("/api/system/config/{configKey}", "MAX_UPLOAD_SIZE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("10485760"));
    }

    @Test
    void shouldReturnNotFound_whenConfigNotExists() throws Exception {
        // Given
        when(systemConfigService.getConfigValue(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/system/config/{configKey}", "NONEXISTENT"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetConfigDetailSuccessfully() throws Exception {
        // Given
        when(systemConfigService.getConfigByKey(eq("MAX_UPLOAD_SIZE"))).thenReturn(testConfig);

        // When & Then
        mockMvc.perform(get("/api/system/config/{configKey}/detail", "MAX_UPLOAD_SIZE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value("MAX_UPLOAD_SIZE"));
    }

    @Test
    void shouldGetAllConfigsAsMapSuccessfully() throws Exception {
        // Given
        Map<String, String> configMap = new HashMap<>();
        configMap.put("MAX_UPLOAD_SIZE", "10485760");
        configMap.put("APP_NAME", "CCMS");
        when(systemConfigService.getAllConfigsAsMap()).thenReturn(configMap);

        // When & Then
        mockMvc.perform(get("/api/system/config/map"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.MAX_UPLOAD_SIZE").value("10485760"));
    }

    @Test
    void shouldCreateConfigSuccessfully() throws Exception {
        // Given
        when(systemConfigService.saveConfig(any(SystemConfig.class))).thenReturn(testConfig);

        // When & Then
        mockMvc.perform(post("/api/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfig)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value("MAX_UPLOAD_SIZE"));
    }

    @Test
    void shouldReturnBadRequest_whenCreateConfigFails() throws Exception {
        // Given
        when(systemConfigService.saveConfig(any(SystemConfig.class)))
                .thenThrow(new IllegalArgumentException("配置键已存在"));

        // When & Then
        mockMvc.perform(post("/api/system/config")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfig)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateConfigSuccessfully() throws Exception {
        // Given
        SystemConfig existing = createTestConfig();
        when(systemConfigService.getConfigByKey(eq("MAX_UPLOAD_SIZE"))).thenReturn(existing);
        when(systemConfigService.saveConfig(any(SystemConfig.class))).thenReturn(existing);

        // When & Then
        mockMvc.perform(put("/api/system/config/{configKey}", "MAX_UPLOAD_SIZE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfig)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFound_whenUpdateNonExistentConfig() throws Exception {
        // Given
        when(systemConfigService.getConfigByKey(anyString())).thenReturn(null);

        // When & Then
        mockMvc.perform(put("/api/system/config/{configKey}", "NONEXISTENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testConfig)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateConfigValueSuccessfully() throws Exception {
        // Given
        SystemConfig updated = createTestConfig();
        updated.setConfigValue("20971520");
        when(systemConfigService.updateConfigValue(eq("MAX_UPLOAD_SIZE"), eq("20971520")))
                .thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/system/config/{configKey}/value", "MAX_UPLOAD_SIZE")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"configValue\": \"20971520\"}"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("20971520"));
    }

    @Test
    void shouldBatchUpdateConfigsSuccessfully() throws Exception {
        // Given
        doNothing().when(systemConfigService).batchUpdateConfigs(anyMap());

        Map<String, String> updates = new HashMap<>();
        updates.put("MAX_UPLOAD_SIZE", "20971520");
        updates.put("SESSION_TIMEOUT", "3600");

        // When & Then
        mockMvc.perform(put("/api/system/config/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldDeleteConfigSuccessfully() throws Exception {
        // Given
        doNothing().when(systemConfigService).deleteConfigByKey(eq("MAX_UPLOAD_SIZE"));

        // When & Then
        mockMvc.perform(delete("/api/system/config/{configKey}", "MAX_UPLOAD_SIZE"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetConfigsByTypeSuccessfully() throws Exception {
        // Given
        List<SystemConfig> configs = Arrays.asList(testConfig);
        when(systemConfigService.getConfigsByType(eq(SystemConfig.ConfigType.SYSTEM)))
                .thenReturn(configs);

        // When & Then
        mockMvc.perform(get("/api/system/config/type/{configType}", "SYSTEM"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetConfigsByPrefixSuccessfully() throws Exception {
        // Given
        List<SystemConfig> configs = Arrays.asList(testConfig);
        when(systemConfigService.getConfigsByPrefix(eq("MAX_"))).thenReturn(configs);

        // When & Then
        mockMvc.perform(get("/api/system/config/prefix/{prefix}", "MAX_"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldReloadConfigCacheSuccessfully() throws Exception {
        // Given
        doNothing().when(systemConfigService).reloadConfigCache();

        // When & Then
        mockMvc.perform(post("/api/system/config/reload-cache"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldCheckConfigKeyExistsSuccessfully() throws Exception {
        // Given
        when(systemConfigService.configKeyExists(eq("MAX_UPLOAD_SIZE"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/system/config/{configKey}/exists", "MAX_UPLOAD_SIZE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    private SystemConfig createTestConfig() {
        return createTestConfig(1L, "MAX_UPLOAD_SIZE", "10485760");
    }

    private SystemConfig createTestConfig(Long id, String key, String value) {
        SystemConfig config = new SystemConfig();
        config.setId(id);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType(SystemConfig.ConfigType.SYSTEM);
        config.setDescription("Test config");
        config.setStatus(1);
        return config;
    }
}
