package com.ccms.config;

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
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 内存缓存管理器（开发环境使用）
     */
    @Bean
    public CacheManager memoryCacheManager() {
        return new ConcurrentMapCacheManager("reportData", "templateConfig", "userSession");
    }

    /**
     * Redis缓存管理器（生产环境使用）
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)) // 默认1小时过期
                .disableCachingNullValues() // 不缓存null值
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 针对不同类型的缓存设置不同过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 报表数据缓存 - 30分钟
        cacheConfigurations.put("reportData", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 模板配置缓存 - 24小时
        cacheConfigurations.put("templateConfig", defaultCacheConfig.entryTtl(Duration.ofHours(24)));
        
        // 用户会话缓存 - 2小时
        cacheConfigurations.put("userSession", defaultCacheConfig.entryTtl(Duration.ofHours(2)));
        
        // 统计数据缓存 - 1小时
        cacheConfigurations.put("statistics", defaultCacheConfig.entryTtl(Duration.ofHours(1)));
        
        // 字典数据缓存 - 6小时
        cacheConfigurations.put("dictionary", defaultCacheConfig.entryTtl(Duration.ofHours(6)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * 获取当前环境的缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 根据环境配置选择缓存管理器
        String env = System.getProperty("spring.profiles.active", "dev");
        
        if ("prod".equals(env) || "staging".equals(env)) {
            return redisCacheManager(redisConnectionFactory);
        } else {
            return memoryCacheManager();
        }
    }
}