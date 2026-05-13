package com.ccms.cache;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * 审批模块基础缓存配置类
 * 配置不同类型的缓存策略
 */
@Configuration
@EnableCaching
public class BasicApprovalCacheConfig {

    /**
     * 基础审批缓存管理器
     * 基于Redis的分布式缓存管理器
     */
    @Bean
    public CacheManager basicApprovalCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(2))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .withCacheConfiguration("approval-flow-config", cacheConfiguration.entryTtl(Duration.ofDays(7)))
                .withCacheConfiguration("approval-instance", cacheConfiguration.entryTtl(Duration.ofHours(24)))
                .withCacheConfiguration("approval-record", cacheConfiguration.entryTtl(Duration.ofDays(30)))
                .withCacheConfiguration("approval-statistics", cacheConfiguration.entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration("approval-conditions", cacheConfiguration.entryTtl(Duration.ofDays(7)))
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