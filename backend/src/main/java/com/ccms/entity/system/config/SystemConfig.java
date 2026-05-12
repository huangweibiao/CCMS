package com.ccms.entity.system.config;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系统参数配置实体类
 */
@Entity
@Table(name = "ccms_sys_config", uniqueConstraints = {
    @UniqueConstraint(columnNames = "configKey")
})
public class SystemConfig extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false)
    private String configKey;

    @NotBlank
    @Size(max = 500)
    private String configValue;

    @Size(max = 200)
    private String configName;

    @Size(max = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ConfigType configType = ConfigType.STRING;

    private boolean enabled = true;

    private boolean systemConfig = false;

    private Integer sortOrder = 0;

    // 构造器
    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue, String configName) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.configName = configName;
    }

    // Getters and Setters
    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public String getConfigName() { return configName; }
    public void setConfigName(String configName) { this.configName = configName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public ConfigType getConfigType() { return configType; }
    public void setConfigType(ConfigType configType) { this.configType = configType; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isSystemConfig() { return systemConfig; }
    public void setSystemConfig(boolean systemConfig) { this.systemConfig = systemConfig; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    /**
     * 配置类型枚举
     */
    public enum ConfigType {
        STRING("字符串"),
        NUMBER("数字"),
        BOOLEAN("布尔值"),
        JSON("JSON对象"),
        LIST("列表");

        private final String description;

        ConfigType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 获取配置值作为数字
     */
    @Transient
    public Integer getValueAsInteger() {
        try {
            return Integer.parseInt(configValue);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取配置值作为布尔值
     */
    @Transient
    public Boolean getValueAsBoolean() {
        return "true".equalsIgnoreCase(configValue) || "1".equals(configValue);
    }

    /**
     * 检查是否为系统级配置
     */
    @Transient
    public boolean isReadonly() {
        return systemConfig;
    }
}
