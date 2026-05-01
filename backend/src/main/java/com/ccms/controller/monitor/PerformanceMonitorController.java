package com.ccms.controller.monitor;

import com.ccms.service.monitor.PerformanceMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 性能监控控制器
 */
@RestController
@RequestMapping("/api/monitor/performance")
public class PerformanceMonitorController {

    @Autowired
    private PerformanceMonitor performanceMonitor;

    /**
     * 获取方法调用统计
     */
    @GetMapping("/methods")
    public ResponseEntity<Map<String, PerformanceMonitor.MethodStats>> getMethodStats() {
        Map<String, PerformanceMonitor.MethodStats> methodStats = performanceMonitor.getMethodStats();
        return ResponseEntity.ok(methodStats);
    }

    /**
     * 获取数据库查询统计
     */
    @GetMapping("/queries")
    public ResponseEntity<Map<String, PerformanceMonitor.QueryStats>> getQueryStats() {
        Map<String, PerformanceMonitor.QueryStats> queryStats = performanceMonitor.getQueryStats();
        return ResponseEntity.ok(queryStats);
    }

    /**
     * 获取缓存命中率统计
     */
    @GetMapping("/cache")
    public ResponseEntity<Map<String, PerformanceMonitor.CacheStats>> getCacheStats() {
        Map<String, PerformanceMonitor.CacheStats> cacheStats = performanceMonitor.getCacheStats();
        return ResponseEntity.ok(cacheStats);
    }

    /**
     * 生成完整性能报告
     */
    @GetMapping("/report")
    public ResponseEntity<PerformanceMonitor.PerformanceReport> generateReport() {
        PerformanceMonitor.PerformanceReport report = performanceMonitor.generateReport();
        return ResponseEntity.ok(report);
    }

    /**
     * 重置所有性能统计
     */
    @PostMapping("/reset")
    public ResponseEntity<Void> resetStats() {
        performanceMonitor.resetAllStats();
        return ResponseEntity.ok().build();
    }

    /**
     * 获取慢查询方法列表
     */
    @GetMapping("/slow-methods")
    public ResponseEntity<Map<String, PerformanceMonitor.MethodStats>> getSlowMethods(@RequestParam(defaultValue = "1000") long slowThreshold) {
        Map<String, PerformanceMonitor.MethodStats> allStats = performanceMonitor.getMethodStats();
        
        // 过滤出平均执行时间超过阈值的慢方法
        Map<String, PerformanceMonitor.MethodStats> slowMethods = new java.util.HashMap<>();
        for (Map.Entry<String, PerformanceMonitor.MethodStats> entry : allStats.entrySet()) {
            if (entry.getValue().getAverageTime() > slowThreshold) {
                slowMethods.put(entry.getKey(), entry.getValue());
            }
        }
        
        return ResponseEntity.ok(slowMethods);
    }

    /**
     * 获取高频率调用的方法
     */
    @GetMapping("/high-frequency")
    public ResponseEntity<Map<String, PerformanceMonitor.MethodStats>> getHighFrequencyMethods(@RequestParam(defaultValue = "100") int frequencyThreshold) {
        Map<String, PerformanceMonitor.MethodStats> allStats = performanceMonitor.getMethodStats();
        
        // 过滤出调用频率超过阈值的方法
        Map<String, PerformanceMonitor.MethodStats> highFrequencyMethods = new java.util.HashMap<>();
        for (Map.Entry<String, PerformanceMonitor.MethodStats> entry : allStats.entrySet()) {
            if (entry.getValue().getCallCount() > frequencyThreshold) {
                highFrequencyMethods.put(entry.getKey(), entry.getValue());
            }
        }
        
        return ResponseEntity.ok(highFrequencyMethods);
    }

    /**
     * 获取数据库性能分析
     */
    @GetMapping("/database-health")
    public ResponseEntity<DatabaseHealthReport> getDatabaseHealth() {
        Map<String, PerformanceMonitor.QueryStats> queryStats = performanceMonitor.getQueryStats();
        
        long totalQueries = 0;
        long totalQueryTime = 0;
        long maxQueryTime = 0;
        long slowQueryCount = 0;
        
        for (PerformanceMonitor.QueryStats stats : queryStats.values()) {
            totalQueries += stats.getQueryCount();
            totalQueryTime += stats.getTotalTime();
            maxQueryTime = Math.max(maxQueryTime, stats.getMaxTime());
            
            if (stats.getAverageTime() > 500) { // 超过500ms算作慢查询
                slowQueryCount++;
            }
        }
        
        double averageQueryTime = totalQueries > 0 ? (double) totalQueryTime / totalQueries : 0.0;
        double slowQueryPercentage = totalQueries > 0 ? (double) slowQueryCount / queryStats.size() * 100 : 0.0;
        
        DatabaseHealthReport report = new DatabaseHealthReport(
            totalQueries, 
            averageQueryTime, 
            maxQueryTime, 
            slowQueryCount, 
            slowQueryPercentage, 
            queryStats.size()
        );
        
        return ResponseEntity.ok(report);
    }

    /**
     * 获取缓存健康度报告
     */
    @GetMapping("/cache-health")
    public ResponseEntity<CacheHealthReport> getCacheHealth() {
        Map<String, PerformanceMonitor.CacheStats> cacheStats = performanceMonitor.getCacheStats();
        
        int totalHitCount = 0;
        int totalMissCount = 0;
        int effectiveCacheCount = 0;
        
        for (PerformanceMonitor.CacheStats stats : cacheStats.values()) {
            totalHitCount += stats.getHitCount();
            totalMissCount += stats.getMissCount();
            
            // 有实际请求的缓存才计入有效缓存
            if (stats.getTotalRequests() > 0) {
                effectiveCacheCount++;
            }
        }
        
        int totalRequests = totalHitCount + totalMissCount;
        double overallHitRate = totalRequests > 0 ? (double) totalHitCount / totalRequests * 100 : 0.0;
        
        CacheHealthReport report = new CacheHealthReport(
            cacheStats.size(), 
            effectiveCacheCount, 
            totalRequests, 
            overallHitRate,
            totalHitCount, 
            totalMissCount
        );
        
        return ResponseEntity.ok(report);
    }

    /**
     * 数据库健康度报告类
     */
    public static class DatabaseHealthReport {
        private final long totalQueries;
        private final double averageQueryTime;
        private final long maxQueryTime;
        private final long slowQueryCount;
        private final double slowQueryPercentage;
        private final int uniqueQueryCount;

        public DatabaseHealthReport(long totalQueries, double averageQueryTime, long maxQueryTime, 
                                   long slowQueryCount, double slowQueryPercentage, int uniqueQueryCount) {
            this.totalQueries = totalQueries;
            this.averageQueryTime = averageQueryTime;
            this.maxQueryTime = maxQueryTime;
            this.slowQueryCount = slowQueryCount;
            this.slowQueryPercentage = slowQueryPercentage;
            this.uniqueQueryCount = uniqueQueryCount;
        }

        public long getTotalQueries() { return totalQueries; }
        public double getAverageQueryTime() { return averageQueryTime; }
        public long getMaxQueryTime() { return maxQueryTime; }
        public long getSlowQueryCount() { return slowQueryCount; }
        public double getSlowQueryPercentage() { return slowQueryPercentage; }
        public int getUniqueQueryCount() { return uniqueQueryCount; }
    }

    /**
     * 缓存健康度报告类
     */
    public static class CacheHealthReport {
        private final int totalCacheCount;
        private final int effectiveCacheCount;
        private final int totalRequests;
        private final double overallHitRate;
        private final int totalHitCount;
        private final int totalMissCount;

        public CacheHealthReport(int totalCacheCount, int effectiveCacheCount, int totalRequests, 
                                double overallHitRate, int totalHitCount, int totalMissCount) {
            this.totalCacheCount = totalCacheCount;
            this.effectiveCacheCount = effectiveCacheCount;
            this.totalRequests = totalRequests;
            this.overallHitRate = overallHitRate;
            this.totalHitCount = totalHitCount;
            this.totalMissCount = totalMissCount;
        }

        public int getTotalCacheCount() { return totalCacheCount; }
        public int getEffectiveCacheCount() { return effectiveCacheCount; }
        public int getTotalRequests() { return totalRequests; }
        public double getOverallHitRate() { return overallHitRate; }
        public int getTotalHitCount() { return totalHitCount; }
        public int getTotalMissCount() { return totalMissCount; }
    }
}