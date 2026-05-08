package com.ccms.controller.system;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.system.config.SystemConfig;
import com.ccms.service.system.SystemConfigService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 系统配置控制器单元测试
 */
@WebMvcTest(SystemConfigController.class)
class SystemConfigControllerTest extends ControllerTestBase {

    @MockBean
    private SystemConfigService systemConfigService;

    private SystemConfig createTestConfig(String key, String value, SystemConfig.ConfigType type) {
        SystemConfig config = new SystemConfig();
        config.setId(1L);
        config.setConfigKey(key);
        config.setConfigValue(value);
        config.setConfigType(type);

        return config;
    }

    @Test
    void shouldReturnConfigValue() throws Exception {
        when(systemConfigService.getConfigValue("app.name")).thenReturn("CCMS");

        performGet("/api/system/config/app.name")
                .andExpect(status().isOk())
                .andExpect(content().string("CCMS"));
    }

    @Test
    void shouldReturnConfigValueWithDefault() throws Exception {
        when(systemConfigService.getConfigValue("app.name", "Default")).thenReturn("CCMS");

        performGet("/api/system/config/app.name/default?defaultValue=Default")
                .andExpect(status().isOk())
                .andExpect(content().string("CCMS"));
    }

    @Test
    void shouldReturnIntConfigValue() throws Exception {
        when(systemConfigService.getIntConfigValue("app.timeout")).thenReturn(30);

        performGet("/api/system/config/app.timeout/int")
                .andExpect(status().isOk())
                .andExpect(content().string("30"));
    }

    @Test
    void shouldReturnBooleanConfigValue() throws Exception {
        when(systemConfigService.getBooleanConfigValue("app.debug")).thenReturn(true);

        performGet("/api/system/config/app.debug/boolean")
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void shouldReturnConfigDetailWhenExists() throws Exception {
        SystemConfig config = createTestConfig("app.name", "CCMS", SystemConfig.ConfigType.STRING);
        when(systemConfigService.getConfigByKey("app.name")).thenReturn(config);

        performGet("/api/system/config/app.name/detail")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value("app.name"))
                .andExpect(jsonPath("$.configValue").value("CCMS"));
    }

    @Test
    void shouldReturnNotFoundWhenConfigNotExists() throws Exception {
        when(systemConfigService.getConfigByKey("app.unknown")).thenReturn(null);

        performGet("/api/system/config/app.unknown/detail")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnAllEnabledConfigs() throws Exception {
        SystemConfig config1 = createTestConfig("app.name", "CCMS", SystemConfig.ConfigType.STRING);
        SystemConfig config2 = createTestConfig("app.version", "1.0", SystemConfig.ConfigType.STRING);
        when(systemConfigService.getAllEnabledConfigs()).thenReturn(Arrays.asList(config1, config2));

        performGet("/api/system/config/all-enabled")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturnConfigsByType() throws Exception {
        SystemConfig config = createTestConfig("app.name", "CCMS", SystemConfig.ConfigType.STRING);
        when(systemConfigService.getConfigsByType(SystemConfig.ConfigType.STRING))
                .thenReturn(Arrays.asList(config));

        performGet("/api/system/config/type/STRING")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldReturnAllConfigsAsMap() throws Exception {
        Map<String, String> configMap = new HashMap<>();
        configMap.put("app.name", "CCMS");
        configMap.put("app.version", "1.0");
        when(systemConfigService.getAllConfigsAsMap()).thenReturn(configMap);

        performGet("/api/system/config/all-map")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app.name").value("CCMS"))
                .andExpect(jsonPath("$.app.version").value("1.0"));
    }

    @Test
    void shouldReturnConfigsByPrefix() throws Exception {
        SystemConfig config = createTestConfig("app.name", "CCMS", SystemConfig.ConfigType.STRING);
        when(systemConfigService.getConfigsByPrefix("app."))
                .thenReturn(Arrays.asList(config));

        performGet("/api/system/config/prefix/app.")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldSaveConfigSuccess() throws Exception {
        SystemConfig config = createTestConfig("app.name", "CCMS", SystemConfig.ConfigType.STRING);
        when(systemConfigService.saveConfig(any(SystemConfig.class))).thenReturn(config);

        performPost("/api/system/config", config)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configKey").value("app.name"));
    }

    @Test
    void shouldUpdateConfigValueSuccess() throws Exception {
        SystemConfig config = createTestConfig("app.name", "NewCCMS", SystemConfig.ConfigType.STRING);
        when(systemConfigService.updateConfigValue("app.name", "NewCCMS")).thenReturn(config);

        performPut("/api/system/config/app.name/value?configValue=NewCCMS", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configValue").value("NewCCMS"));
    }

    @Test
    void shouldDeleteConfigByKey() throws Exception {
        performDelete("/api/system/config/app.name")
                .andExpect(status().isOk());

        verify(systemConfigService, times(1)).deleteConfigByKey("app.name");
    }

    @Test
    void shouldCheckConfigKeyExists() throws Exception {
        when(systemConfigService.configKeyExists("app.name")).thenReturn(true);

        performGet("/api/system/config/app.name/exists")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void shouldBatchUpdateConfigs() throws Exception {
        Map<String, String> updates = new HashMap<>();
        updates.put("app.name", "CCMS");
        updates.put("app.version", "2.0");

        performPost("/api/system/config/batch-update", updates)
                .andExpect(status().isOk());

        verify(systemConfigService, times(1)).batchUpdateConfigs(anyMap());
    }

    @Test
    void shouldValidateConfigValue() throws Exception {
        when(systemConfigService.validateConfigValue(SystemConfig.ConfigType.STRING, "test"))
                .thenReturn(true);

        performPost("/api/system/config/validate?configType=STRING&value=test", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void shouldReloadConfigCache() throws Exception {
        performPost("/api/system/config/reload-cache", null)
                .andExpect(status().isOk());

        verify(systemConfigService, times(1)).reloadConfigCache();
    }
}
