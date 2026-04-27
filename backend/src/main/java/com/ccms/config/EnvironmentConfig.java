package com.ccms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量配置管理类
 * 统一管理所有应用环境变量，提供类型安全的访问接口
 */
@Component
@Configuration
public class EnvironmentConfig {
    
    private final Environment environment;
    
    // 应用基础配置
    @Value("${app.name:CCMS}")
    private String appName;
    
    @Value("${app.version:1.0.0}")
    private String appVersion;
    
    @Value("${app.description:企业级费控管理系统}")
    private String appDescription;
    
    // 服务器配置
    @Value("${server.port:8080}")
    private int serverPort;
    
    @Value("${server.servlet.context-path:/}")
    private String contextPath;
    
    // 数据库配置
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    
    @Value("${spring.datasource.username}")
    private String datasourceUsername;
    
    // 文件上传配置
    @Value("${file.upload-path:./uploads/}")
    private String fileUploadPath;
    
    @Value("${file.max-size:10485760}")
    private long fileMaxSize;
    
    // JWT配置
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400}")
    private long jwtExpiration;
    
    // 构建信息
    private final String buildTime = LocalDateTime.now().toString();
    private final String activeProfiles;
    
    public EnvironmentConfig(Environment environment) {
        this.environment = environment;
        this.activeProfiles = String.join(",", environment.getActiveProfiles());
    }
    
    /**
     * 初始化配置验证
     */
    @PostConstruct
    public void validateConfiguration() {
        validateRequiredProperties();
        logConfigurationInfo();
    }
    
    /**
     * 验证必需的配置属性
     */
    private void validateRequiredProperties() {
        Map<String, String> requiredProps = new HashMap<>();
        requiredProps.put("spring.datasource.url", datasourceUrl);
        requiredProps.put("jwt.secret", jwtSecret);
        
        for (Map.Entry<String, String> entry : requiredProps.entrySet()) {
            if (entry.getValue() == null || entry.getValue().trim().isEmpty()) {
                throw new IllegalStateException("Required property '" + entry.getKey() + "' is not configured");
            }
        }
    }
    
    /**
     * 记录配置信息
     */
    private void logConfigurationInfo() {
        System.out.println("================================================================");
        System.out.println("CCMS Application Configuration");
        System.out.println("================================================================");
        System.out.println("Application: " + appName + " v" + appVersion);
        System.out.println("Description: " + appDescription);
        System.out.println("Active Profiles: " + activeProfiles);
        System.out.println("Server Port: " + serverPort);
        System.out.println("Context Path: " + contextPath);
        System.out.println("Database URL: " + getMaskedDatabaseUrl());
        System.out.println("Build Time: " + buildTime);
        System.out.println("================================================================");
    }
    
    /**
     * 获取脱敏的数据库URL
     */
    private String getMaskedDatabaseUrl() {
        if (datasourceUrl == null) return "Not configured";
        return datasourceUrl.replaceFirst("//[^/]+@", "//***@");
    }
    
    // ============ Getter 方法 ============
    
    public String getAppName() {
        return appName;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    public String getAppDescription() {
        return appDescription;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public String getDatasourceUrl() {
        return datasourceUrl;
    }
    
    public String getDatasourceUsername() {
        return datasourceUsername;
    }
    
    public String getFileUploadPath() {
        return fileUploadPath;
    }
    
    public long getFileMaxSize() {
        return fileMaxSize;
    }
    
    public String getJwtSecret() {
        return jwtSecret;
    }
    
    public long getJwtExpiration() {
        return jwtExpiration;
    }
    
    public String getBuildTime() {
        return buildTime;
    }
    
    public String getActiveProfiles() {
        return activeProfiles;
    }
    
    // ============ 环境工具方法 ============
    
    /**
     * 检查是否为开发环境
     */
    public boolean isDevelopment() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("dev") || profile.equalsIgnoreCase("development"));
    }
    
    /**
     * 检查是否为生产环境
     */
    public boolean isProduction() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("prod") || profile.equalsIgnoreCase("production"));
    }
    
    /**
     * 检查是否为测试环境
     */
    public boolean isTest() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> profile.equalsIgnoreCase("test"));
    }
    
    /**
     * 启用调试功能的条件
     */
    public boolean canDebug() {
        return isDevelopment() || isTest();
    }
    
    /**
     * 检查JPA是否应该显示SQL
     */
    public boolean shouldShowSql() {
        return !isProduction();
    }
    
    /**
     * 检查是否启用数据初始化
     */
    public boolean shouldInitializeData() {
        return isDevelopment() || isTest();
    }
    
    /**
     * 获取完整的应用URL
     */
    public String getApplicationUrl() {
        String baseUrl = "http://localhost:" + serverPort;
        if (!contextPath.equals("/")) {
            baseUrl += contextPath;
        }
        return baseUrl;
    }
    
    /**
     * 获取所有配置信息（用于管理界面）
     */
    public Map<String, Object> getAllConfigurations() {
        Map<String, Object> configs = new HashMap<>();
        
        configs.put("appName", appName);
        configs.put("appVersion", appVersion);
        configs.put("appDescription", appDescription);
        configs.put("serverPort", serverPort);
        configs.put("contextPath", contextPath);
        configs.put("activeProfiles", activeProfiles);
        configs.put("isDevelopment", isDevelopment());
        configs.put("isProduction", isProduction());
        configs.put("isTest", isTest());
        configs.put("buildTime", buildTime);
        configs.put("fileUploadPath", fileUploadPath);
        configs.put("fileMaxSize", fileMaxSize);
        
        // 敏感信息脱敏
        configs.put("databaseUrl", getMaskedDatabaseUrl());
        configs.put("jwtSecretLength", jwtSecret != null ? jwtSecret.length() : 0);
        configs.put("applicationUrl", getApplicationUrl());
        
        return configs;
    }
    
    /**
     * 验证配置是否完整
     */
    public Map<String, Object> validateConfiguration() {
        Map<String, Object> validation = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        Map<String, String> warnings = new HashMap<>();
        
        // 必需配置检查
        if (datasourceUrl == null || datasourceUrl.trim().isEmpty()) {
            errors.put("datasourceUrl", "数据库连接URL不能为空");
        }
        
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            errors.put("jwtSecret", "JWT密钥不能为空");
        }
        
        // 生产环境安全警告
        if (isProduction()) {
            if (jwtSecret.length() < 32) {
                warnings.put("jwtSecret", "生产环境建议使用更长的JWT密钥");
            }
        }
        
        validation.put("valid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("timestamp", LocalDateTime.now());
        
        return validation;
    }
}