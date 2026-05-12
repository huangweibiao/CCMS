package com.ccms.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 审批模块基础缓存配置类
 * 配置不同类型的缓存策略和过期时间
 */
@Configuration
@EnableCaching
public class BasicApprovalCacheConfig {

    /**
     * 缓存管理器配置
     */
    @Bean
    public CacheManager basicApprovalCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认1小时过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存null值

        // 针对不同类型的缓存定义不同的策略
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 审批流程配置缓存 - 较长时间，很少变更
        cacheConfigurations.put("approval-flow-config", defaultCacheConfig.entryTtl(Duration.ofHours(12)));
        
        // 审批实例缓存 - 中等时间，状态会变化
        cacheConfigurations.put("approval-instance", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 审批记录缓存 - 较短时间，频繁更新
        cacheConfigurations.put("approval-record", defaultCacheConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 审批统计缓存 - 较短时间，实时性要求高
        cacheConfigurations.put("approval-statistics", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));
        
        // 审批配置条件缓存 - 较长时间，很少变更
        cacheConfigurations.put("approval-conditions", defaultCacheConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
    
    /**
     * 缓存常量定义
     */
    public static class CacheNames {
        public static final String FLOW_CONFIG = "approval-flow-config";
        public static final String INSTANCE = "approval-instance";
        public static final String RECORD = "approval-record";
        public static final String STATISTICS = "approval-statistics";
        public static final String CONDITIONS = "approval-conditions";
        
        // 缓存键前缀
        public static final String FLOW_CONFIG_KEY_PREFIX = "approval:flow:config:";
        public static final String INSTANCE_KEY_PREFIX = "approval:instance:";
        public static final String RECORD_KEY_PREFIX = "approval:record:";
        public static final String STATISTICS_KEY_PREFIX = "approval:statistics:";
        public static final String CONDITIONS_KEY_PREFIX = "approval:conditions:";
    }
    
    /**
     * 缓存键生成工具类
     */
    public static class CacheKeyGenerator {
        
        public static String generateFlowConfigKey(Long id) {
            return CacheNames.FLOW_CONFIG_KEY_PREFIX + id;
        }
        
        public static String generateFlowConfigListKey(String businessType, String category) {
            return CacheNames.FLOW_CONFIG_KEY_PREFIX + "list:" + businessType + ":" + category;
        }
        
        public static String generateInstanceKey(Long id) {
            return CacheNames.INSTANCE_KEY_PREFIX + id;
        }
        
        public static String generateRecordKey(Long id) {
            return CacheNames.RECORD_KEY_PREFIX + id;
        }
        
        public static String generateStatisticsKey(String type, String period) {
            return CacheNames.STATISTICS_KEY_PREFIX + type + ":" + period;
        }
        
        public static String generateConditionsKey(String businessType) {
            return CacheNames.CONDITIONS_KEY_PREFIX + businessType;
        }
    }
}