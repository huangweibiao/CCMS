package com.ccms;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 测试应用配置
 * 为测试环境提供特殊的Bean配置，避免对生产环境的依赖
 */
@TestConfiguration
public class TestApplicationConfiguration {
    
    /**
     * 测试Redis连接工厂
     * 使用内存Redis或模拟实现
     */
    @Bean
    @Primary
    public RedisConnectionFactory testRedisConnectionFactory() {
        // 在生产环境中需要配置为真实的测试Redis服务器
        // 这里返回一个默认的Lettuce连接工厂
        return new LettuceConnectionFactory();
    }
    
    /**
     * 测试Redis模板配置
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> testRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        
        // 设置序列化器
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
    
    /**
     * 测试配置 - JWT密钥
     */
    @Bean
    @Primary
    public String testJwtSecret() {
        return "test-jwt-secret-key-for-unit-testing-only-do-not-use-in-production";
    }
    
    /**
     * 测试配置 - 文件上传路径
     */
    @Bean
    @Primary
    public String testUploadPath() {
        return System.getProperty("java.io.tmpdir") + "/ccms-test-uploads/";
    }
    
    /**
     * 测试配置 - API前缀
     */
    @Bean
    @Primary
    public String testApiPrefix() {
        return "/api/test";
    }
}