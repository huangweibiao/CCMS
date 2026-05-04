package com.ccms.service.impl.system;

import com.ccms.entity.system.SystemConfig;
import com.ccms.repository.SystemConfigRepository;
import com.ccms.service.system.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统配置服务实现类
 */
@Service
@Transactional
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final Logger logger = LoggerFactory.getLogger(SystemConfigServiceImpl.class);

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    // 配置缓存
    private Map<String, String> configCache = new HashMap<>();
    private volatile boolean cacheInitialized = false;

    @Override
    public String getConfigValue(String configKey) {
        return getConfigValue(configKey, null);
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        initializeCacheIfNeeded();
        
        String value = configCache.get(configKey);
        if (value != null) {
            return value;
        }
        
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        if (config.isPresent() && config.get().isEnabled()) {
            String configValue = config.get().getConfigValue();
            configCache.put(configKey, configValue);
            return configValue;
        }
        
        return defaultValue;
    }

    @Override
    public Integer getIntConfigValue(String configKey) {
        return getIntConfigValue(configKey, null);
    }

    @Override
    public Integer getIntConfigValue(String configKey, Integer defaultValue) {
        String value = getConfigValue(configKey);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("配置值无法转换为整数: {}={}", configKey, value);
            }
        }
        return defaultValue;
    }

    @Override
    public Boolean getBooleanConfigValue(String configKey) {
        return getBooleanConfigValue(configKey, null);
    }

    @Override
    public Boolean getBooleanConfigValue(String configKey, Boolean defaultValue) {
        String value = getConfigValue(configKey);
        if (value != null) {
            return Boolean.parseBoolean(value) || "1".equals(value) || "true".equalsIgnoreCase(value);
        }
        return defaultValue;
    }

    @Override
    public SystemConfig saveConfig(SystemConfig config) {
        // 验证配置键唯一性
        if (config.getId() == null) {
            if (systemConfigRepository.existsByConfigKey(config.getConfigKey())) {
                throw new IllegalArgumentException("配置键已存在: " + config.getConfigKey());
            }
        } else {
            if (systemConfigRepository.existsByConfigKeyAndIdNot(config.getConfigKey(), config.getId())) {
                throw new IllegalArgumentException("配置键已存在: " + config.getConfigKey());
            }
        }

        // 验证配置值格式
        if (!validateConfigValue(config.getConfigType(), config.getConfigValue())) {
            throw new IllegalArgumentException("配置值格式不正确");
        }

        SystemConfig savedConfig = systemConfigRepository.save(config);
        
        // 更新缓存
        if (savedConfig.isEnabled()) {
            configCache.put(savedConfig.getConfigKey(), savedConfig.getConfigValue());
        } else {
            configCache.remove(savedConfig.getConfigKey());
        }
        
        logger.info("保存系统配置: {}", savedConfig.getConfigKey());
        return savedConfig;
    }

    @Override
    public SystemConfig updateConfigValue(String configKey, String configValue) {
        Optional<SystemConfig> existingConfig = systemConfigRepository.findByConfigKey(configKey);
        if (existingConfig.isPresent()) {
            SystemConfig config = existingConfig.get();
            
            // 验证配置值格式
            if (!validateConfigValue(config.getConfigType(), configValue)) {
                throw new IllegalArgumentException("配置值格式不正确");
            }
            
            config.setConfigValue(configValue);
            SystemConfig updatedConfig = systemConfigRepository.save(config);
            
            // 更新缓存
            if (updatedConfig.isEnabled()) {
                configCache.put(configKey, configValue);
            }
            
            logger.info("更新系统配置值: {}={}", configKey, configValue);
            return updatedConfig;
        }
        
        throw new IllegalArgumentException("配置不存在: " + configKey);
    }

    @Override
    public void deleteConfigByKey(String configKey) {
        Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(configKey);
        if (config.isPresent()) {
            systemConfigRepository.delete(config.get());
            
            // 清除缓存
            configCache.remove(configKey);
            logger.info("删除系统配置: {}", configKey);
        }
    }

    @Override
    public SystemConfig getConfigByKey(String configKey) {
        return systemConfigRepository.findByConfigKey(configKey).orElse(null);
    }

    @Override
    public List<SystemConfig> getAllEnabledConfigs() {
        return systemConfigRepository.findByEnabledTrueOrderByConfigTypeAscCreatedTimeDesc();
    }

    @Override
    public List<SystemConfig> getConfigsByType(SystemConfig.ConfigType configType) {
        return systemConfigRepository.findByConfigTypeAndEnabledTrueOrderByCreatedTimeDesc(configType);
    }

    @Override
    public Map<String, String> getAllConfigsAsMap() {
        initializeCacheIfNeeded();
        return new HashMap<>(configCache);
    }

    @Override
    public void reloadConfigCache() {
        configCache.clear();
        cacheInitialized = false;
        initializeCacheIfNeeded();
        logger.info("重新加载系统配置缓存");
    }

    @Override
    public boolean configKeyExists(String configKey) {
        return systemConfigRepository.existsByConfigKey(configKey);
    }

    @Override
    public void batchUpdateConfigs(Map<String, String> configUpdates) {
        for (Map.Entry<String, String> entry : configUpdates.entrySet()) {
            Optional<SystemConfig> config = systemConfigRepository.findByConfigKey(entry.getKey());
            if (config.isPresent()) {
                SystemConfig existingConfig = config.get();
                
                // 验证配置值格式
                if (validateConfigValue(existingConfig.getConfigType(), entry.getValue())) {
                    existingConfig.setConfigValue(entry.getValue());
                    systemConfigRepository.save(existingConfig);
                    
                    // 更新缓存
                    if (existingConfig.isEnabled()) {
                        configCache.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        logger.info("批量更新系统配置: {} 条记录", configUpdates.size());
    }

    @Override
    public List<SystemConfig> getConfigsByPrefix(String prefix) {
        return systemConfigRepository.findByConfigKeyStartingWithAndEnabledTrue(prefix);
    }

    @Override
    public boolean validateConfigValue(SystemConfig.ConfigType configType, String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        try {
            switch (configType) {
                case NUMBER:
                    new BigDecimal(value);
                    break;
                case BOOLEAN:
                    // 布尔值可以是 true/false/1/0
                    if (!value.equals("true") && !value.equals("false") && 
                        !value.equals("1") && !value.equals("0")) {
                        return false;
                    }
                    break;
                case JSON:
                    // 简单的JSON格式验证
                    if (!value.startsWith("{") || !value.endsWith("}")) {
                        return false;
                    }
                    break;
                case LIST:
                    // 列表格式验证（逗号分隔）
                    if (!value.contains(",")) {
                        return false;
                    }
                    break;
                default:
                    // STRING 类型没有额外验证
                    break;
            }
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 初始化配置缓存
     */
    private void initializeCacheIfNeeded() {
        if (!cacheInitialized) {
            synchronized (this) {
                if (!cacheInitialized) {
                    List<SystemConfig> enabledConfigs = getAllEnabledConfigs();
                    for (SystemConfig config : enabledConfigs) {
                        configCache.put(config.getConfigKey(), config.getConfigValue());
                    }
                    cacheInitialized = true;
                    logger.info("系统配置缓存初始化完成，加载 {} 条配置", enabledConfigs.size());
                }
            }
        }
    }
}