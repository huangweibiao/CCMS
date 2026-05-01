package com.ccms.service.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能监控服务
 */
@Service
public class PerformanceMonitor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);

    // 方法调用统计
    private final Map<String, MethodStats> methodStats = new ConcurrentHashMap<>();
    
    // 数据库查询统计
    private final Map<String, QueryStats> queryStats = new ConcurrentHashMap<>();
    
    // 缓存命中率统计
    private final Map<String, CacheStats> cacheStats = new ConcurrentHashMap<>();

    /**
     * 记录方法调用开始时间
     */
    public void startMethod(String methodName) {
        MethodStats stats = methodStats.computeIfAbsent(methodName, k -> new MethodStats());
        stats.start();
    }

    /**
     * 记录方法调用结束时间
     */
    public void endMethod(String methodName) {
        MethodStats stats = methodStats.get(methodName);
        if (stats != null) {
            stats.end();
        }
    }

    /**
     * 记录数据库查询
     */
    public void recordQuery(String queryName, long duration) {
        QueryStats stats = queryStats.computeIfAbsent(queryName, k -> new QueryStats());
        stats.record(duration);
    }

    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheName) {
        CacheStats stats = cacheStats.computeIfAbsent(cacheName, k -> new CacheStats());
        stats.recordHit();
    }

    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheName) {
        CacheStats stats = cacheStats.computeIfAbsent(cacheName, k -> new CacheStats());
        stats.recordMiss();
    }

    /**
     * 获取方法性能统计
     */
    public Map<String, MethodStats> getMethodStats() {
        return new ConcurrentHashMap<>(methodStats);
    }

    /**
     * 获取数据库查询统计
     */
    public Map<String, QueryStats> getQueryStats() {
        return new ConcurrentHashMap<>(queryStats);
    }

    /**
     * 获取缓存命中率统计
     */
    public Map<String, CacheStats> getCacheStats() {
        return new ConcurrentHashMap<>(cacheStats);
    }

    /**
     * 重置所有统计
     */
    public void resetAllStats() {
        methodStats.clear();
        queryStats.clear();
        cacheStats.clear();
        logger.info("性能监控统计已重置");
    }

    /**
     * 生成性能报告
     */
    public PerformanceReport generateReport() {
        return new PerformanceReport(methodStats, queryStats, cacheStats);
    }

    /**
     * 方法性能统计类
     */
    public static class MethodStats {
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicInteger callCount = new AtomicInteger(0);
        private final ThreadLocal<Long> startTime = new ThreadLocal<>();

        public void start() {
            startTime.set(System.currentTimeMillis());
        }

        public void end() {
            Long start = startTime.get();
            if (start != null) {
                long duration = System.currentTimeMillis() - start;
                totalTime.addAndGet(duration);
                callCount.incrementAndGet();
                startTime.remove();
            }
        }

        public long getTotalTime() { return totalTime.get(); }
        public int getCallCount() { return callCount.get(); }
        public double getAverageTime() { 
            return callCount.get() > 0 ? (double) totalTime.get() / callCount.get() : 0.0; 
        }
    }

    /**
     * 数据库查询统计类
     */
    public static class QueryStats {
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicInteger queryCount = new AtomicInteger(0);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);

        public void record(long duration) {
            totalTime.addAndGet(duration);
            queryCount.incrementAndGet();
            
            // 更新最大时间
            long currentMax;
            long newMax;
            do {
                currentMax = maxTime.get();
                newMax = Math.max(currentMax, duration);
            } while (!maxTime.compareAndSet(currentMax, newMax));
            
            // 更新最小时间
            long currentMin;
            long newMin;
            do {
                currentMin = minTime.get();
                newMin = Math.min(currentMin, duration);
            } while (currentMin != newMin && !minTime.compareAndSet(currentMin, newMin));
        }

        public long getTotalTime() { return totalTime.get(); }
        public int getQueryCount() { return queryCount.get(); }
        public long getMaxTime() { return maxTime.get() == 0 ? 0 : maxTime.get(); }
        public long getMinTime() { return minTime.get() == Long.MAX_VALUE ? 0 : minTime.get(); }
        public double getAverageTime() { 
            return queryCount.get() > 0 ? (double) totalTime.get() / queryCount.get() : 0.0; 
        }
    }

    /**
     * 缓存命中率统计类
     */
    public static class CacheStats {
        private final AtomicInteger hitCount = new AtomicInteger(0);
        private final AtomicInteger missCount = new AtomicInteger(0);

        public void recordHit() {
            hitCount.incrementAndGet();
        }

        public void recordMiss() {
            missCount.incrementAndGet();
        }

        public int getHitCount() { return hitCount.get(); }
        public int getMissCount() { return missCount.get(); }
        public int getTotalRequests() { return hitCount.get() + missCount.get(); }
        public double getHitRate() { 
            int total = getTotalRequests();
            return total > 0 ? (double) hitCount.get() / total : 0.0; 
        }
    }

    /**
     * 性能报告类
     */
    public static class PerformanceReport {
        private final Map<String, MethodStats> methodStats;
        private final Map<String, QueryStats> queryStats;
        private final Map<String, CacheStats> cacheStats;

        public PerformanceReport(Map<String, MethodStats> methodStats, 
                                Map<String, QueryStats> queryStats, 
                                Map<String, CacheStats> cacheStats) {
            this.methodStats = methodStats;
            this.queryStats = queryStats;
            this.cacheStats = cacheStats;
        }

        public Map<String, MethodStats> getMethodStats() { return methodStats; }
        public Map<String, QueryStats> getQueryStats() { return queryStats; }
        public Map<String, CacheStats> getCacheStats() { return cacheStats; }
    }
}