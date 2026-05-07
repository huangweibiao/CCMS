package com.ccms.controller.system;

import com.ccms.entity.system.config.SystemConfig;
import com.ccms.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器
 * 对应设计文档：4.9 系统通用表 - 系统配置相关接口
 */
@RestController
@RequestMapping("/api/system/config")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Autowired
    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    /**
     * 根据配置键获取配置值
     */
    @GetMapping("/{configKey}")
    public ResponseEntity<String> getConfigValue(@PathVariable String configKey) {
        String value = systemConfigService.getConfigValue(configKey);
        return ResponseEntity.ok(value);
    }

    /**
     * 根据配置键获取配置值（带默认值）
     */
    @GetMapping("/{configKey}/default")
    public ResponseEntity<String> getConfigValueWithDefault(
            @PathVariable String configKey,
            @RequestParam String defaultValue) {
        String value = systemConfigService.getConfigValue(configKey, defaultValue);
        return ResponseEntity.ok(value);
    }

    /**
     * 根据配置键获取整型配置值
     */
    @GetMapping("/{configKey}/int")
    public ResponseEntity<Integer> getIntConfigValue(@PathVariable String configKey) {
        Integer value = systemConfigService.getIntConfigValue(configKey);
        return ResponseEntity.ok(value);
    }

    /**
     * 根据配置键获取布尔型配置值
     */
    @GetMapping("/{configKey}/boolean")
    public ResponseEntity<Boolean> getBooleanConfigValue(@PathVariable String configKey) {
        Boolean value = systemConfigService.getBooleanConfigValue(configKey);
        return ResponseEntity.ok(value);
    }

    /**
     * 根据配置键获取配置对象
     */
    @GetMapping("/{configKey}/detail")
    public ResponseEntity<SystemConfig> getConfigByKey(@PathVariable String configKey) {
        SystemConfig config = systemConfigService.getConfigByKey(configKey);
        if (config != null) {
            return ResponseEntity.ok(config);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 获取所有启用的配置
     */
    @GetMapping("/all-enabled")
    public ResponseEntity<List<SystemConfig>> getAllEnabledConfigs() {
        List<SystemConfig> configs = systemConfigService.getAllEnabledConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * 根据配置类型获取配置
     */
    @GetMapping("/type/{configType}")
    public ResponseEntity<List<SystemConfig>> getConfigsByType(
            @PathVariable SystemConfig.ConfigType configType) {
        List<SystemConfig> configs = systemConfigService.getConfigsByType(configType);
        return ResponseEntity.ok(configs);
    }

    /**
     * 获取所有配置键值对
     */
    @GetMapping("/all-map")
    public ResponseEntity<Map<String, String>> getAllConfigsAsMap() {
        Map<String, String> configs = systemConfigService.getAllConfigsAsMap();
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
     * 创建或更新配置
     */
    @PostMapping
    public ResponseEntity<SystemConfig> saveConfig(@RequestBody SystemConfig config) {
        SystemConfig saved = systemConfigService.saveConfig(config);
        return ResponseEntity.ok(saved);
    }

    /**
     * 根据配置键更新配置值
     */
    @PutMapping("/{configKey}/value")
    public ResponseEntity<SystemConfig> updateConfigValue(
            @PathVariable String configKey,
            @RequestParam String configValue) {
        SystemConfig updated = systemConfigService.updateConfigValue(configKey, configValue);
        return ResponseEntity.ok(updated);
    }

    /**
     * 根据配置键删除配置
     */
    @DeleteMapping("/{configKey}")
    public ResponseEntity<Void> deleteConfigByKey(@PathVariable String configKey) {
        systemConfigService.deleteConfigByKey(configKey);
        return ResponseEntity.ok().build();
    }

    /**
     * 检查配置键是否存在
     */
    @GetMapping("/{configKey}/exists")
    public ResponseEntity<Map<String, Boolean>> checkConfigKeyExists(@PathVariable String configKey) {
        boolean exists = systemConfigService.configKeyExists(configKey);
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", exists);
        return ResponseEntity.ok(result);
    }

    /**
     * 批量更新配置
     */
    @PostMapping("/batch-update")
    public ResponseEntity<Void> batchUpdateConfigs(@RequestBody Map<String, String> configUpdates) {
        systemConfigService.batchUpdateConfigs(configUpdates);
        return ResponseEntity.ok().build();
    }

    /**
     * 验证配置值格式
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Boolean>> validateConfigValue(
            @RequestParam SystemConfig.ConfigType configType,
            @RequestParam String value) {
        boolean valid = systemConfigService.validateConfigValue(configType, value);
        Map<String, Boolean> result = new HashMap<>();
        result.put("valid", valid);
        return ResponseEntity.ok(result);
    }

    /**
     * 重新加载配置缓存
     */
    @PostMapping("/reload-cache")
    public ResponseEntity<Void> reloadConfigCache() {
        systemConfigService.reloadConfigCache();
        return ResponseEntity.ok().build();
    }
}
