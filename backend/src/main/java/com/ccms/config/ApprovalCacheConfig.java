package com.ccms.config;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 审批模块缓存配置
 * 提供流程配置、实例状态等数据的缓存支持
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ApprovalCacheConfig {

    /**
     * 自定义缓存键生成器
     */
    @Bean("approvalKeyGenerator")
    public KeyGenerator approvalKeyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName());
            sb.append(".").append(method.getName());
            
            // 根据方法参数生成特定键
            for (Object param : params) {
                if (param != null) {
                    // 特殊处理业务类型和金额组合
                    if (param instanceof String && ((String) param).contains("EXPENSE")) {
                        sb.append(":").append(param);
                    } else if (param instanceof java.math.BigDecimal) {
                        sb.append(":$").append(param);
                    } else if (param instanceof Long) {
                        sb.append(":#").append(param);
                    } else {
                        sb.append(":").append(param.toString().hashCode());
                    }
                } else {
                    sb.append(":null");
                }
            }
            return sb.toString();
        };
    }

    /**
     * 审批模块专用缓存管理器
     */
    @Bean("approvalCacheManager")
    public CacheManager approvalCacheManager(RedisConnectionFactory redisConnectionFactory, 
                                           ObjectMapper objectMapper) {
        
        // 配置Jackson序列化器，支持多态类型
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.activateDefaultTyping(
            cacheObjectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        jacksonSerializer.setObjectMapper(cacheObjectMapper);

        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30)) // 默认30分钟
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jacksonSerializer));

        // 不同缓存区域的不同配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 流程配置缓存 - 2小时，较少变动
        cacheConfigurations.put("approvalConfig", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 实例状态缓存 - 5分钟，频繁变动
        cacheConfigurations.put("approvalStatus", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 审批历史缓存 - 1小时
        cacheConfigurations.put("approvalHistory", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 用户权限缓存 - 30分钟
        cacheConfigurations.put("approvalPermission", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 统计数据缓存 - 10分钟
        cacheConfigurations.put("approvalStats", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * 审批流程配置缓存配置
     */
    @Bean
    public RedisCacheConfiguration approvalConfigCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(2))
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                new Jackson2JsonRedisSerializer<>(ApprovalFlowConfig.class)));
    }

    /**
     * 缓存清理工具类
     */
    @Bean
    public ApprovalCacheCleaner approvalCacheCleaner() {
        return new ApprovalCacheCleaner();
    }

    /**
     * 缓存监控指标
     */
    @Bean 
    public ApprovalCacheMetrics approvalCacheMetrics() {
        return new ApprovalCacheMetrics();
    }
}

/**
 * 缓存清理工具
 */
class ApprovalCacheCleaner {
    
    public void clearApprovalConfigCache() {
        // 实现缓存清理逻辑
    }
    
    public void clearInstanceStatusCache(Long instanceId) {
        // 清理特定实例的缓存
    }
    
    public void clearUserPermissionCache(Long userId) {
        // 清理用户权限缓存
    }
}

/**
 * 缓存监控指标
 */
class ApprovalCacheMetrics {
    
    public long getCacheHitCount() {
        // 获取缓存命中次数
        return 0L;
    }
    
    public long getCacheMissCount() {
        // 获取缓存未命中次数
        return 0L;
    }
    
    public double getCacheHitRate() {
        long hitCount = getCacheHitCount();
        long missCount = getCacheMissCount();
        long total = hitCount + missCount;
        return total > 0 ? (double) hitCount / total : 0.0;
    }
}