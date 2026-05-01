package com.ccms.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 提供常用的Redis操作方法
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存值
     */
    public void set(String key, Object value) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, value);
        } catch (Exception e) {
            log.error("Redis set操作失败: key={}", key, e);
        }
    }

    /**
     * 设置缓存值并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            ops.set(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis set操作失败: key={}, timeout={}", key, timeout, e);
        }
    }

    /**
     * 获取缓存值
     */
    public Object get(String key) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            return ops.get(key);
        } catch (Exception e) {
            log.error("Redis get操作失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis delete操作失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置缓存值，仅当键不存在时
     */
    public Boolean setIfAbsent(String key, Object value) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            return ops.setIfAbsent(key, value);
        } catch (Exception e) {
            log.error("Redis setIfAbsent操作失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置缓存值并指定过期时间，仅当键不存在时
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            return ops.setIfAbsent(key, value, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis setIfAbsent操作失败: key={}, timeout={}", key, timeout, e);
            return false;
        }
    }

    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Redis hasKey操作失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        try {
            return redisTemplate.expire(key, timeout, timeUnit);
        } catch (Exception e) {
            log.error("Redis expire操作失败: key={}, timeout={}", key, timeout, e);
            return false;
        }
    }

    /**
     * 获取过期时间
     */
    public Long getExpire(String key, TimeUnit timeUnit) {
        try {
            return redisTemplate.getExpire(key, timeUnit);
        } catch (Exception e) {
            log.error("Redis getExpire操作失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 递增操作
     */
    public Long increment(String key, long delta) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            return ops.increment(key, delta);
        } catch (Exception e) {
            log.error("Redis increment操作失败: key={}, delta={}", key, delta, e);
            return null;
        }
    }

    /**
     * 递减操作
     */
    public Long decrement(String key, long delta) {
        try {
            ValueOperations<String, Object> ops = redisTemplate.opsForValue();
            return ops.decrement(key, delta);
        } catch (Exception e) {
            log.error("Redis decrement操作失败: key={}, delta={}", key, delta, e);
            return null;
        }
    }

    /**
     * 生成分布式锁
     */
    public boolean tryLock(String key, long timeout, TimeUnit timeUnit) {
        String lockKey = "lock:" + key;
        return setIfAbsent(lockKey, "locked", timeout, timeUnit);
    }

    /**
     * 释放分布式锁
     */
    public boolean releaseLock(String key) {
        String lockKey = "lock:" + key;
        return delete(lockKey);
    }

    /**
     * 生成幂等性令牌
     */
    public String generateIdempotentToken() {
        String token = java.util.UUID.randomUUID().toString();
        String tokenKey = "idempotent:token:" + token;
        set(tokenKey, "1", 5, TimeUnit.MINUTES); // 5分钟有效期
        return token;
    }

    /**
     * 验证幂等性令牌
     */
    public boolean validateIdempotentToken(String token) {
        String tokenKey = "idempotent:token:" + token;
        Object value = get(tokenKey);
        if (value != null) {
            delete(tokenKey); // 使用后立即删除
            return true;
        }
        return false;
    }

    /**
     * 清理缓存（根据模式）
     */
    public void clearByPattern(String pattern) {
        try {
            redisTemplate.delete(redisTemplate.keys(pattern));
        } catch (Exception e) {
            log.error("Redis clearByPattern操作失败: pattern={}", pattern, e);
        }
    }

    /**
     * 获取缓存大小
     */
    public Long getSize() {
        try {
            return redisTemplate.getConnectionFactory().getConnection().dbSize();
        } catch (Exception e) {
            log.error("Redis getSize操作失败", e);
            return null;
        }
    }
}