package com.ccms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Redis工具类
 * 提供常用的Redis操作方法，支持Redis不可用时的内存降级
 */
public class RedisUtil {

    private static final Logger log = LoggerFactory.getLogger(RedisUtil.class);
    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean redisAvailable;
    
    // 内存缓存作为降级方案
    private final ConcurrentMap<String, CacheEntry> memoryCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> cleanupTask;

    public RedisUtil(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.redisAvailable = (redisTemplate != null);
        
        if (!redisAvailable) {
            log.warn("Redis不可用，将使用内存缓存作为降级方案");
            // 启动内存缓存清理任务
            this.cleanupTask = cleanupScheduler.scheduleAtFixedRate(this::cleanupExpiredEntries, 1, 1, TimeUnit.MINUTES);
        } else {
            log.info("Redis可用，使用Redis进行缓存");
        }
    }
    
    // 关闭清理任务
    public void destroy() {
        if (cleanupTask != null) {
            cleanupTask.cancel(false);
        }
        cleanupScheduler.shutdown();
    }
    
    // 内存缓存条目
    private static class CacheEntry {
        Object value;
        long expireTime;
        
        CacheEntry(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        boolean isExpired() {
            return expireTime > 0 && System.currentTimeMillis() > expireTime;
        }
    }
    
    // 清理过期内存缓存条目
    private void cleanupExpiredEntries() {
        try {
            memoryCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        } catch (Exception e) {
            log.error("内存缓存清理失败", e);
        }
    }
    
    // 设置内存缓存值
    private void setMemoryCache(String key, Object value, long timeout, TimeUnit timeUnit) {
        long expireTime = timeout > 0 ? System.currentTimeMillis() + timeUnit.toMillis(timeout) : 0;
        memoryCache.put(key, new CacheEntry(value, expireTime));
    }
    
    // 获取内存缓存值
    private Object getMemoryCache(String key) {
        CacheEntry entry = memoryCache.get(key);
        if (entry != null) {
            if (entry.isExpired()) {
                memoryCache.remove(key);
                return null;
            }
            return entry.value;
        }
        return null;
    }
    
    // 删除内存缓存
    private boolean deleteMemoryCache(String key) {
        return memoryCache.remove(key) != null;
    }

    /**
     * 设置缓存值
     */
    public void set(String key, Object value) {
        if (redisAvailable) {
            try {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                ops.set(key, value);
            } catch (Exception e) {
                log.error("Redis set操作失败，降级到内存缓存: key={}", key, e);
                // 降级到内存缓存
                setMemoryCache(key, value, 0, TimeUnit.MILLISECONDS);
            }
        } else {
            setMemoryCache(key, value, 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 设置缓存值并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (redisAvailable) {
            try {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                ops.set(key, value, timeout, timeUnit);
            } catch (Exception e) {
                log.error("Redis set操作失败，降级到内存缓存: key={}, timeout={}", key, timeout, e);
                setMemoryCache(key, value, timeout, timeUnit);
            }
        } else {
            setMemoryCache(key, value, timeout, timeUnit);
        }
    }

    /**
     * 获取缓存值
     */
    public Object get(String key) {
        if (redisAvailable) {
            try {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                return ops.get(key);
            } catch (Exception e) {
                log.error("Redis get操作失败，尝试内存缓存: key={}", key, e);
                return getMemoryCache(key);
            }
        } else {
            return getMemoryCache(key);
        }
    }

    /**
     * 删除缓存
     */
    public boolean delete(String key) {
        if (redisAvailable) {
            try {
                return Boolean.TRUE.equals(redisTemplate.delete(key));
            } catch (Exception e) {
                log.error("Redis delete操作失败，删除内存缓存: key={}", key, e);
                return deleteMemoryCache(key);
            }
        } else {
            return deleteMemoryCache(key);
        }
    }

    /**
     * 设置缓存值，仅当键不存在时
     */
    public Boolean setIfAbsent(String key, Object value) {
        if (redisAvailable) {
            try {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                return ops.setIfAbsent(key, value);
            } catch (Exception e) {
                log.error("Redis setIfAbsent操作失败: key={}", key, e);
                // 降级逻辑：检查内存缓存中是否存在
                return handleMemorySetIfAbsent(key, value);
            }
        } else {
            return handleMemorySetIfAbsent(key, value);
        }
    }
    
    // 内存缓存的setIfAbsent实现
    private Boolean handleMemorySetIfAbsent(String key, Object value) {
        if (getMemoryCache(key) == null) {
            setMemoryCache(key, value, 0, TimeUnit.MILLISECONDS);
            return true;
        }
        return false;
    }

    /**
     * 设置缓存值并指定过期时间，仅当键不存在时
     */
    public Boolean setIfAbsent(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (redisAvailable) {
            try {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                return ops.setIfAbsent(key, value, timeout, timeUnit);
            } catch (Exception e) {
                log.error("Redis setIfAbsent操作失败: key={}, timeout={}", key, timeout, e);
                return handleMemorySetIfAbsentWithTimeout(key, value, timeout, timeUnit);
            }
        } else {
            return handleMemorySetIfAbsentWithTimeout(key, value, timeout, timeUnit);
        }
    }
    
    // 内存缓存的setIfAbsent带超时实现
    private Boolean handleMemorySetIfAbsentWithTimeout(String key, Object value, long timeout, TimeUnit timeUnit) {
        if (getMemoryCache(key) == null) {
            setMemoryCache(key, value, timeout, timeUnit);
            return true;
        }
        return false;
    }

    /**
     * 判断键是否存在
     */
    public Boolean hasKey(String key) {
        if (redisAvailable) {
            try {
                return redisTemplate.hasKey(key);
            } catch (Exception e) {
                log.error("Redis hasKey操作失败: key={}", key, e);
                return getMemoryCache(key) != null;
            }
        } else {
            return getMemoryCache(key) != null;
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
        if (redisAvailable) {
            try {
                ValueOperations<String, Object> ops = redisTemplate.opsForValue();
                return ops.increment(key, delta);
            } catch (Exception e) {
                log.error("Redis increment操作失败: key={}, delta={}", key, delta, e);
                // 内存缓存的简单递增实现
                Object current = getMemoryCache(key);
                long newValue = current instanceof Number ? ((Number) current).longValue() + delta : delta;
                setMemoryCache(key, newValue, 0, TimeUnit.MILLISECONDS);
                return newValue;
            }
        } else {
            Object current = getMemoryCache(key);
            long newValue = current instanceof Number ? ((Number) current).longValue() + delta : delta;
            setMemoryCache(key, newValue, 0, TimeUnit.MILLISECONDS);
            return newValue;
        }
    }

    /**
     * 递减操作
     */
    public Long decrement(String key, long delta) {
        return increment(key, -delta);
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
        if (redisAvailable) {
            try {
                redisTemplate.delete(redisTemplate.keys(pattern));
            } catch (Exception e) {
                log.error("Redis clearByPattern操作失败: pattern={}", pattern, e);
                // 模式清理在内存缓存中不可用，简单日志
                log.warn("模式清理在内存缓存中不可用");
            }
        } else {
            log.warn("模式清理在内存缓存中不可用");
        }
    }

    /**
     * 获取缓存大小
     */
    public Long getSize() {
        if (redisAvailable) {
            try {
                return redisTemplate.getConnectionFactory().getConnection().dbSize();
            } catch (Exception e) {
                log.error("Redis getSize操作失败", e);
                return (long) memoryCache.size();
            }
        } else {
            return (long) memoryCache.size();
        }
    }
}