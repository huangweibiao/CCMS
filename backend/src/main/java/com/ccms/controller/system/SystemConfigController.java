package com.ccms.controller.system;

import com.ccms.entity.system.SystemConfig;
import com.ccms.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 */
@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    /**
     * 获取所有启用的系统配置
     */
    @GetMapping
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllEnabledConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * 根据配置键获取配置值
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<String> getConfigValue(@PathVariable String configKey) {
        String value = systemConfigService.getConfigValue(configKey);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    /**
     * 根据配置键获取配置对象
     */
    @GetMapping("/{configKey}/detail")
    public ResponseEntity<SystemConfig> getConfigDetail(@PathVariable String configKey) {
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        return config != null ? ResponseEntity.ok(config) : ResponseEntity.notFound().build();
    }

    /**
     * 获取所有配置的键值对
     */
    @GetMapping("/map")
    public ResponseEntity<Map<String, String>> getAllConfigsAsMap() {
        Map<String, String> configMap = systemConfigService.getAllConfigsAsMap();
        return ResponseEntity.ok(configMap);
    }

    /**
     * 创建新的系统配置
     */
    @PostMapping
    public ResponseEntity<SystemConfig> createConfig(@RequestBody SystemConfig config) {
        try {
            SystemConfig savedConfig = systemConfigService.saveConfig(config);
            return ResponseEntity.ok(savedConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新系统配置
     */
    @PutMapping("/{configKey}")
    public ResponseEntity<SystemConfig> updateConfig(@PathVariable String configKey, @RequestBody SystemConfig config) {
        if (!configKey.equals(config.getConfigKey())) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            SystemConfig existingConfig = systemConfigService.getConfigByKey(configKey);
            if (existingConfig == null) {
                return ResponseEntity.notFound().build();
            }
            
            config.setId(existingConfig.getId());
            SystemConfig updatedConfig = systemConfigService.saveConfig(config);
            return ResponseEntity.ok(updatedConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新配置值
     */
    @PutMapping("/{configKey}/value")
    public ResponseEntity<SystemConfig> updateConfigValue(@PathVariable String configKey, @RequestBody Map<String, String> request) {
        String configValue = request.get("configValue");
        if (configValue == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            SystemConfig updatedConfig = systemConfigService.updateConfigValue(configKey, configValue);
            return ResponseEntity.ok(updatedConfig);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量更新配置值
     */
    @PutMapping("/batch")
    public ResponseEntity<Void> batchUpdateConfigs(@RequestBody Map<String, String> configUpdates) {
        try {
            systemConfigService.batchUpdateConfigs(configUpdates);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/{configKey}")
    public ResponseEntity<Void> deleteConfig(@PathVariable String configKey) {
        try {
            systemConfigService.deleteConfigByKey(configKey);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 根据配置类型获取配置
     */
    @GetMapping("/type/{configType}")
    public ResponseEntity<List<SystemConfig>> getConfigsByType(@PathVariable SystemConfig.ConfigType configType) {
        List<SystemConfig> configs = systemConfigService.getConfigsByType(configType);
        return ResponseEntity.ok(configs);
    }

    /**
     * 根据前缀获取配置
     */
    @GetMapping("/prefix/{prefix}")
    public ResponseEntity<List<SystemConfig>> getConfigsByPrefix(@PathVariable String prefix) {
        List<SystemConfig> configs = systemConfigService.getConfigsByPrefix(prefix);
        return ResponseEntity.ok(configs);
    }

    /**
     * 重新加载配置缓存
     */
    @PostMapping("/reload-cache")
    public ResponseEntity<Void> reloadConfigCache() {
        systemConfigService.reloadConfigCache();
        return ResponseEntity.ok().build();
    }

    /**
     * 检查配置键是否存在
     */
    @GetMapping("/{configKey}/exists")
    public ResponseEntity<Boolean> checkConfigKeyExists(@PathVariable String configKey) {
        boolean exists = systemConfigService.configKeyExists(configKey);
        return ResponseEntity.ok(exists);
    }
}