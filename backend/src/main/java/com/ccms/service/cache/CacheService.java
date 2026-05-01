package com.ccms.service.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;

/**
 * 缓存服务工具类
 */
@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Autowired
    private CacheManager cacheManager;

    /**
     * 获取缓存
     */
    public Object get(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("缓存 {} 不存在", cacheName);
            return null;
        }
        
        Cache.ValueWrapper valueWrapper = cache.get(key);
        return valueWrapper != null ? valueWrapper.get() : null;
    }

    /**
     * 获取缓存，如果不存在则调用valueLoader获取
     */
    public <T> T get(String cacheName, Object key, Callable<T> valueLoader) {
        try {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                logger.warn("缓存 {} 不存在，直接调用valueLoader", cacheName);
                return valueLoader.call();
            }
            
            return cache.get(key, valueLoader);
        } catch (Exception e) {
            logger.error("获取缓存失败: {} - {}", cacheName, key, e);
            try {
                return valueLoader.call();
            } catch (Exception ex) {
                throw new RuntimeException("缓存和valueLoader都失败", ex);
            }
        }
    }

    /**
     * 保存缓存
     */
    public void put(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("缓存 {} 不存在，无法保存", cacheName);
            return;
        }
        
        cache.put(key, value);
        logger.debug("缓存已保存: {} - {}", cacheName, key);
    }

    /**
     * 删除缓存
     */
    public void evict(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("缓存 {} 不存在，无法删除", cacheName);
            return;
        }
        
        cache.evict(key);
        logger.debug("缓存已删除: {} - {}", cacheName, key);
    }

    /**
     * 清空整个缓存
     */
    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("缓存 {} 不存在，无法清空", cacheName);
            return;
        }
        
        cache.clear();
        logger.info("缓存已清空: {}", cacheName);
    }

    /**
     * 获取或创建缓存
     */
    public Object getOrCreate(String cacheName, Object key, Callable<Object> valueLoader) {
        Object cachedValue = get(cacheName, key);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        try {
            Object newValue = valueLoader.call();
            put(cacheName, key, newValue);
            return newValue;
        } catch (Exception e) {
            logger.error("创建缓存值失败: {} - {}", cacheName, key, e);
            return null;
        }
    }

    /**
     * 批量删除缓存
     */
    public void batchEvict(String cacheName, Iterable<Object> keys) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            logger.warn("缓存 {} 不存在，无法批量删除", cacheName);
            return;
        }
        
        for (Object key : keys) {
            cache.evict(key);
        }
        logger.debug("批量删除缓存完成: {} - {} 条记录", cacheName, getIterableCount(keys));
    }

    /**
     * 检查缓存是否存在
     */
    public boolean exists(String cacheName, Object key) {
        return get(cacheName, key) != null;
    }

    /**
     * 预热缓存
     */
    public void preloadCache(String cacheName, Callable<Object> dataLoader) {
        try {
            Object data = dataLoader.call();
            if (data != null) {
                // 根据数据类型选择合适的键，这里简单处理
                put(cacheName, "preload_data", data);
                logger.info("预热缓存完成: {}", cacheName);
            }
        } catch (Exception e) {
            logger.error("预热缓存失败: {}", cacheName, e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getCacheStats(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return new CacheStats(cacheName, 0, 0);
        }
        
        // 这里简化处理，实际应用中可以使用更复杂的统计
        return new CacheStats(cacheName, 0, 0);
    }

    /**
     * 计算Iterable的大小
     */
    private int getIterableCount(Iterable<Object> iterable) {
        int count = 0;
        for (Object obj : iterable) {
            count++;
        }
        return count;
    }

    /**
     * 缓存统计信息类
     */
    public static class CacheStats {
        private final String cacheName;
        private final int size;
        private final int hitCount;

        public CacheStats(String cacheName, int size, int hitCount) {
            this.cacheName = cacheName;
            this.size = size;
            this.hitCount = hitCount;
        }

        public String getCacheName() { return cacheName; }
        public int getSize() { return size; }
        public int getHitCount() { return hitCount; }
    }
}