package com.ccms.config;

import com.ccms.util.RedisUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Redis配置类
 * 控制Redis工具类的创建条件
 */
@Configuration
public class RedisConfig {
    
    /**
     * 当RedisTemplate Bean存在时创建RedisUtil
     */
    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RedisUtil redisUtil(RedisTemplate<String, Object> redisTemplate) {
        return new RedisUtil(redisTemplate);
    }
    
    /**
     * 当RedisTemplate Bean不存在时创建支持降级的RedisUtil
     */
    @Bean
    @ConditionalOnMissingBean(RedisTemplate.class)
    public RedisUtil fallbackRedisUtil() {
        return new RedisUtil(null);
    }
}