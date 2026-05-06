package com.ccms.service.system;

import com.ccms.entity.system.config.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 */
public interface SystemConfigService {

    /**
     * 根据配置键获取配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值，带默认值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 根据配置键获取整型配置值
     */
    Integer getIntConfigValue(String configKey);

    /**
     * 根据配置键获取整型配置值，带默认值
     */
    Integer getIntConfigValue(String configKey, Integer defaultValue);

    /**
     * 根据配置键获取布尔型配置值
     */
    Boolean getBooleanConfigValue(String configKey);

    /**
     * 根据配置键获取布尔型配置值，带默认值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);

    /**
     * 保存或更新配置
     */
    SystemConfig saveConfig(SystemConfig config);

    /**
     * 根据配置键更新配置值
     */
    SystemConfig updateConfigValue(String configKey, String configValue);

    /**
     * 根据配置键删除配置
     */
    void deleteConfigByKey(String configKey);

    /**
     * 根据配置键获取配置对象
     */
    SystemConfig getConfigByKey(String configKey);

    /**
     * 获取所有启用的配置
     */
    List<SystemConfig> getAllEnabledConfigs();

    /**
     * 根据配置类型获取配置
     */
    List<SystemConfig> getConfigsByType(SystemConfig.ConfigType configType);

    /**
     * 获取所有配置键值对
     */
    Map<String, String> getAllConfigsAsMap();

    /**
     * 重新加载配置缓存
     */
    void reloadConfigCache();

    /**
     * 检查配置键是否存在
     */
    boolean configKeyExists(String configKey);

    /**
     * 批量更新配置
     */
    void batchUpdateConfigs(Map<String, String> configUpdates);

    /**
     * 根据前缀获取配置
     */
    List<SystemConfig> getConfigsByPrefix(String prefix);

    /**
     * 验证配置值格式
     */
    boolean validateConfigValue(SystemConfig.ConfigType configType, String value);
}