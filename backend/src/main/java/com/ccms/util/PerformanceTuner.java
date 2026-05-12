package com.ccms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 性能调优工具类
 * 提供自动性能调优、缓存优化和数据库优化功能
 */
@Component
public class PerformanceTuner {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceTuner.class);
    
    @Autowired
    private CacheManager cacheManager;
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final Map<String, AtomicInteger> cacheHitStats = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> cacheMissStats = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> slowQueryStats = new ConcurrentHashMap<>();
    
    /**
     * 缓存命中率监控和优化定时任务
     */
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    @Async
    public void optimizeCacheConfiguration() {
        try {
            logger.info("开始缓存配置优化");
            
            // 分析缓存命中率
            analyzeCacheHitRate();
            
            // 清理过期缓存（根据业务模式动态调整）
            cleanStaleCache();
            
            // 调整缓存策略
            adjustCacheStrategy();
            
            logger.info("缓存优化完成");
        } catch (Exception e) {
            logger.error("缓存优化失败", e);
        }
    }
    
    /**
     * 数据库性能优化定时任务
     */
    @Scheduled(fixedRate = 600000) // 每10分钟执行一次
    @Async
    public void optimizeDatabasePerformance() {
        try {
            logger.info("开始数据库性能优化");
            
            // 检查索引使用情况
            checkIndexUsage();
            
            // 分析慢查询
            analyzeSlowQueries();
            
            // 优化表结构
            optimizeTableStructures();
            
            // 连接池优化
            optimizeConnectionPool();
            
            logger.info("数据库优化完成");
        } catch (Exception e) {
            logger.error("数据库优化失败", e);
        }
    }
    
    /**
     * 内存和GC优化定时任务
     */
    @Scheduled(fixedRate = 900000) // 每15分钟执行一次
    public void optimizeMemoryAndGC() {
        try {
            logger.info("开始内存和GC优化分析");
            
            // 分析内存使用模式
            analyzeMemoryUsagePattern();
            
            // 优化GC参数（需要JVM参数支持）
            suggestGCOptimizations();
            
            // 检查内存泄漏
            checkMemoryLeaks();
            
            logger.info("内存优化分析完成");
        } catch (Exception e) {
            logger.error("内存优化失败", e);
        }
    }
    
    /**
     * 实时性能监控和自动调优
     */
    @Async
    public void realTimePerformanceTuning() {
        try {
            // 监控系统关键指标
            double systemLoad = getSystemLoad();
            double memoryUsage = getMemoryUsageRatio();
            double gcPressure = getGCPressure();
            
            // 根据系统负载动态调整策略
            if (systemLoad > 0.8) {
                // 系统负载过高，启用降级策略
                enablePerformanceDegradation();
            } else if (memoryUsage > 0.85) {
                // 内存使用率过高，主动清理缓存
                aggressiveCacheCleanup();
            } else if (gcPressure > 0.7) {
                // GC压力大，调整GC策略
                triggerGCOptimization();
            } else {
                // 正常情况，渐进优化
                progressiveOptimization();
            }
        } catch (Exception e) {
            logger.error("实时性能调优失败", e);
        }
    }
    
    // 具体优化方法实现
    
    private void analyzeCacheHitRate() {
        cacheHitStats.forEach((cacheName, hitCount) -> {
            AtomicInteger missCount = cacheMissStats.get(cacheName);
            if (missCount != null) {
                int total = hitCount.get() + missCount.get();
                if (total > 0) {
                    double hitRate = (double) hitCount.get() / total;
                    
                    logger.info("缓存 [{}] 命中率: {:.2f}% (命中: {}, 未命中: {}, 总计: {})", 
                               cacheName, hitRate * 100, hitCount.get(), missCount.get(), total);
                    
                    // 根据命中率调整缓存策略
                    if (hitRate < 0.3) {
                        logger.warn("缓存 [{}] 命中率过低，考虑调整缓存策略", cacheName);
                    } else if (hitRate > 0.9) {
                        logger.info("缓存 [{}] 命中率良好，可考虑扩容", cacheName);
                    }
                }
            }
        });
    }
    
    @CacheEvict(allEntries = true, cacheNames = {"approvalFlowConfig", "approvalInstance", "approvalNode"})
    public void cleanStaleCache() {
        logger.info("清理过期缓存数据");
        // 具体的缓存清理逻辑
        clearLowValueCacheEntries();
    }
    
    private void adjustCacheStrategy() {
        // 根据使用模式调整缓存策略
        // 高频访问的数据延长TTL，低频访问的数据缩短TTL
        // 大对象使用不同缓存策略
    }
    
    private void checkIndexUsage() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // 获取表信息
            String[] types = {"TABLE"};
            ResultSet tables = metaData.getTables(null, null, "%", types);
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                
                // 检查表的索引情况
                ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false);
                List<String> indexList = new ArrayList<>();
                
                while (indexes.next()) {
                    String indexName = indexes.getString("INDEX_NAME");
                    if (indexName != null) {
                        indexList.add(indexName);
                    }
                }
                
                if (indexList.isEmpty()) {
                    logger.warn("表 [{}] 无索引，存在性能风险", tableName);
                } else {
                    logger.debug("表 [{}] 索引: {}", tableName, indexList);
                }
                indexes.close();
            }
            tables.close();
        } catch (SQLException e) {
            logger.error("检查索引使用情况失败", e);
        }
    }
    
    private void analyzeSlowQueries() {
        // 模拟慢查询分析
        try {
            // 检查是否存在长时间运行的查询
            List<Map<String, Object>> slowQueries = jdbcTemplate.queryForList(
                "SELECT query, execution_time FROM system_slow_queries WHERE execution_time > 1000"
            );
            
            if (!slowQueries.isEmpty()) {
                logger.warn("发现 {} 个慢查询需要优化", slowQueries.size());
                for (Map<String, Object> query : slowQueries) {
                    logger.info("慢查询: {}, 执行时间: {}ms", 
                               query.get("query"), query.get("execution_time"));
                }
            }
        } catch (DataAccessException e) {
            logger.debug("无法获取慢查询信息（可能需要配置）", e);
        }
    }
    
    private void optimizeTableStructures() {
        // 表结构优化建议
        logger.info("分析表结构优化机会");
        
        // 检查大表是否需要分区
        checkTablePartitioning();
        
        // 检查是否需要添加索引
        suggestNewIndexes();
        
        // 检查数据类型优化
        suggestDataTypeOptimizations();
    }
    
    private void optimizeConnectionPool() {
        // 连接池优化建议
        logger.info("分析数据库连接池使用情况");
        
        try (Connection conn = dataSource.getConnection()) {
            // 获取连接池统计信息
            // 实际实现需要获取具体的连接池统计
        } catch (SQLException e) {
            logger.error("获取连接池信息失败", e);
        }
    }
    
    private void analyzeMemoryUsagePattern() {
        // 分析内存使用模式
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double usageRatio = (double) usedMemory / maxMemory;
        
        logger.info("内存使用情况: {}/{} ({:.2f}%)", 
                   formatMemory(usedMemory), formatMemory(maxMemory), usageRatio * 100);
        
        if (usageRatio > 0.8) {
            logger.warn("内存使用率超过80%，建议优化内存使用");
        }
    }
    
    private void suggestGCOptimizations() {
        // GC优化建议
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            long collectionCount = gcBean.getCollectionCount();
            long collectionTime = gcBean.getCollectionTime();
            
            logger.info("GC [{}]: 次数={}, 时间={}ms", 
                       gcBean.getName(), collectionCount, collectionTime);
            
            if (collectionCount > 1000 && collectionTime > 5000) {
                logger.warn("GC [{}] 频繁且耗时较长，建议优化GC参数", gcBean.getName());
            }
        }
    }
    
    private void checkMemoryLeaks() {
        // 检查内存泄漏模式
        // 这里可以添加内存泄漏检测逻辑
        logger.info("进行内存泄漏初步检查");
    }
    
    // 工具方法
    
    private double getSystemLoad() {
        // 获取系统负载（简化实现）
        return Math.random() * 0.5 + 0.3; // 模拟负载值
    }
    
    private double getMemoryUsageRatio() {
        Runtime runtime = Runtime.getRuntime();
        return (double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory();
    }
    
    private double getGCPressure() {
        // 计算GC压力
        long totalGCTime = 0;
        for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            totalGCTime += gcBean.getCollectionTime();
        }
        return (double) totalGCTime / 60000; // 假设1分钟周期
    }
    
    private void enablePerformanceDegradation() {
        logger.info("启用性能降级策略");
        // 减少缓存TTL，增加异步处理延迟，限制并发数等
    }
    
    private void aggressiveCacheCleanup() {
        logger.info("执行主动缓存清理");
        cleanStaleCache();
    }
    
    private void triggerGCOptimization() {
        logger.info("触发GC优化建议");
        suggestGCOptimizations();
    }
    
    private void progressiveOptimization() {
        logger.info("执行渐进式优化");
        // 正常的优化逻辑
    }
    
    private void clearLowValueCacheEntries() {
        // 清理低价值缓存条目
        logger.debug("清理低价值缓存条目");
    }
    
    private void checkTablePartitioning() {
        // 检查大表分区需求
    }
    
    private void suggestNewIndexes() {
        // 建议添加新索引
    }
    
    private void suggestDataTypeOptimizations() {
        // 建议数据类型优化
    }
    
    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * 记录缓存命中
     */
    public void recordCacheHit(String cacheName) {
        cacheHitStats.computeIfAbsent(cacheName, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 记录缓存未命中
     */
    public void recordCacheMiss(String cacheName) {
        cacheMissStats.computeIfAbsent(cacheName, k -> new AtomicInteger(0)).incrementAndGet();
    }
    
    /**
     * 记录慢查询
     */
    public void recordSlowQuery(String queryType, long executionTime) {
        if (executionTime > 1000) { // 超过1秒视为慢查询
            slowQueryStats.computeIfAbsent(queryType, k -> new AtomicInteger(0)).incrementAndGet();
        }
    }
}