package com.ccms.cache;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 审批模块缓存服务
 * 提供缓存的读取、写入和删除操作
 */
@Service
public class ApprovalCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private com.ccms.service.approval.ApprovalAuditService auditService;

    /**
     * 缓存审批流程配置
     */
    @Cacheable(value = ApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigKey(#id)")
    public ApprovalFlowConfig getFlowConfigById(Long id) {
        return null; // 实际由Repository查询
    }

    /**
     * 更新审批流程配置缓存
     */
    @CachePut(value = ApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
              key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigKey(#config.id)")
    public ApprovalFlowConfig updateFlowConfigCache(ApprovalFlowConfig config) {
        return config;
    }

    /**
     * 删除审批流程配置缓存
     */
    @CacheEvict(value = ApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
                key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigKey(#id)")
    public void evictFlowConfigCache(Long id) {
        // 同时清除相关的列表缓存
        String listKeyPattern = ApprovalCacheConfig.CacheNames.FLOW_CONFIG_KEY_PREFIX + "list:*";
        redisTemplate.keys(listKeyPattern).forEach(key -> redisTemplate.delete(key));
    }

    /**
     * 缓存根据业务类型和类别的流程配置列表
     */
    @Cacheable(value = ApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigListKey(#businessType, #category)")
    public List<ApprovalFlowConfig> getFlowConfigsByTypeAndCategory(String businessType, String category) {
        return null; // 实际由Repository查询
    }

    /**
     * 缓存审批实例
     */
    @Cacheable(value = ApprovalCacheConfig.CacheNames.INSTANCE, 
               key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateInstanceKey(#id)")
    public ApprovalInstance getInstanceById(Long id) {
        return null; // 实际由Repository查询
    }

    /**
     * 更新审批实例缓存
     */
    @CachePut(value = ApprovalCacheConfig.CacheNames.INSTANCE, 
              key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateInstanceKey(#instance.id)")
    public ApprovalInstance updateInstanceCache(ApprovalInstance instance) {
        return instance;
    }

    /**
     * 删除审批实例缓存
     */
    @CacheEvict(value = ApprovalCacheConfig.CacheNames.INSTANCE, 
                key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateInstanceKey(#id)")
    public void evictInstanceCache(Long id) {
        // 清除相关的状态缓存
        String statusKey = ApprovalCacheConfig.CacheNames.INSTANCE_KEY_PREFIX + "status:" + id;
        redisTemplate.delete(statusKey);
    }

    /**
     * 缓存审批记录
     */
    @Cacheable(value = ApprovalCacheConfig.CacheNames.RECORD, 
               key = "T(com.ccms.cache.ApprovalCacheConfig$CacheKeyGenerator).generateRecordKey(#id)")
    public ApprovalRecord getRecordById(Long id) {
        return null; // 实际由Repository查询
    }

    /**
     * 缓存审批统计数据
     */
    public Map<String, Object> getApprovalStatistics(String type, String period) {
        String key = ApprovalCacheConfig.CacheKeyGenerator.generateStatisticsKey(type, period);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedStats = (Map<String, Object>) redisTemplate.opsForValue().get(key);
        
        if (cachedStats != null) {
            return cachedStats;
        }
        
        // 从数据库查询统计数据（这里需要实现实际的统计逻辑）
        Map<String, Object> statistics = calculateStatistics(type, period);
        
        // 缓存统计结果，5分钟过期
        redisTemplate.opsForValue().set(key, statistics, 5, TimeUnit.MINUTES);
        
        return statistics;
    }

    /**
     * 清除审批统计数据缓存
     */
    public void clearStatisticsCache(String type, String period) {
        String key = ApprovalCacheConfig.CacheKeyGenerator.generateStatisticsKey(type, period);
        redisTemplate.delete(key);
    }

    /**
     * 缓存审批条件配置
     */
    public Object getApprovalConditions(String businessType) {
        String key = ApprovalCacheConfig.CacheKeyGenerator.generateConditionsKey(businessType);
        
        Object conditions = redisTemplate.opsForValue().get(key);
        if (conditions != null) {
            return conditions;
        }
        
        // 从数据库查询条件配置（这里需要实现实际的查询逻辑）
        Object conditionsConfig = loadConditionsFromDB(businessType);
        
        // 缓存条件配置，6小时过期
        redisTemplate.opsForValue().set(key, conditionsConfig, 6, TimeUnit.HOURS);
        
        return conditionsConfig;
    }

    /**
     * 设置审批实例状态缓存（用于快速状态查询）
     */
    public void setInstanceStatusCache(Long instanceId, String status) {
        String key = ApprovalCacheConfig.CacheNames.INSTANCE_KEY_PREFIX + "status:" + instanceId;
        redisTemplate.opsForValue().set(key, status, 10, TimeUnit.MINUTES);
    }

    /**
     * 获取审批实例状态缓存
     */
    public String getInstanceStatusCache(Long instanceId) {
        String key = ApprovalCacheConfig.CacheNames.INSTANCE_KEY_PREFIX + "status:" + instanceId;
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 清除所有审批相关缓存
     */
    public void clearAllApprovalCache() {
        String pattern = "approval:*";
        redisTemplate.keys(pattern).forEach(key -> redisTemplate.delete(key));
    }

    /**
     * 获取缓存命中率统计
     */
    public Map<String, Object> getCacheStats() {
        // 获取各类缓存的键数量
        long flowConfigCount = redisTemplate.keys(ApprovalCacheConfig.CacheNames.FLOW_CONFIG_KEY_PREFIX + "*").size();
        long instanceCount = redisTemplate.keys(ApprovalCacheConfig.CacheNames.INSTANCE_KEY_PREFIX + "*").size();
        long recordCount = redisTemplate.keys(ApprovalCacheConfig.CacheNames.RECORD_KEY_PREFIX + "*").size();
        
        // 这里可以添加更详细的缓存统计信息
        return Map.of(
            "flowConfigCount", flowConfigCount,
            "instanceCount", instanceCount,
            "recordCount", recordCount,
            "totalCacheCount", flowConfigCount + instanceCount + recordCount
        );
    }

    /**
     * 缓存预热 - 预加载常用数据到缓存
     */
    @Async("approvalAsyncExecutor")
    public void warmUpCache() {
        try {
            // 预热常用审批流程配置
            preloadFlowConfigs();
            
            // 预热热点审批实例（最近30天活跃）
            preloadRecentApprovalInstances();
            
            // 预热审批统计数据
            preloadStatistics();
            
            // 记录预热完成日志
            auditService.logApprovalOperation("CACHE_WARMUP", "CacheService", null,
                    -1L, "System", "缓存预热完成", Map.of("timestamp", System.currentTimeMillis()));
        } catch (Exception e) {
            // 记录预热失败日志
            auditService.logApprovalOperation("CACHE_WARMUP_ERROR", "CacheService", null,
                    -1L, "System", "缓存预热失败: " + e.getMessage(), 
                    Map.of("error", e.getMessage(), "timestamp", System.currentTimeMillis()));
        }
    }

    /**
     * 批量获取审批实例缓存
     */
    public Map<Long, ApprovalInstance> batchGetInstances(List<Long> instanceIds) {
        Map<Long, ApprovalInstance> result = new HashMap<>();
        List<Long> missingIds = new ArrayList<>();
        
        // 批量从缓存获取
        for (Long instanceId : instanceIds) {
            ApprovalInstance cached = getInstanceById(instanceId);
            if (cached != null) {
                result.put(instanceId, cached);
            } else {
                missingIds.add(instanceId);
            }
        }
        
        // 批量从数据库加载缺失的数据
        if (!missingIds.isEmpty()) {
            Map<Long, ApprovalInstance> dbInstances = batchLoadInstancesFromDB(missingIds);
            result.putAll(dbInstances);
            
            // 批量更新缓存
            batchUpdateInstanceCache(dbInstances.values());
        }
        
        return result;
    }

    /**
     * 批量更新实例缓存
     */
    public void batchUpdateInstanceCache(Collection<ApprovalInstance> instances) {
        for (ApprovalInstance instance : instances) {
            updateInstanceCache(instance);
        }
    }

    /**
     * 智能缓存清理 - 根据内存使用情况自动清理
     */
    public void smartCacheCleanup() {
        try {
            Properties info = redisTemplate.getConnectionFactory().getConnection().info("memory");
            long usedMemory = Long.parseLong(info.getProperty("used_memory"));
            long maxMemory = Long.parseLong(info.getProperty("maxmemory"));
            
            // 如果内存使用率超过80%，执行清理
            if (maxMemory > 0 && (double) usedMemory / maxMemory > 0.8) {
                // 清理过期较早的审批实例缓存
                cleanupOldInstanceCache();
                
                // 清理不常用的统计缓存
                cleanupLowPriorityCache();
                
                // 记录清理操作
                auditService.logApprovalOperation("CACHE_CLEANUP", "CacheService", null,
                        -1L, "System", "智能缓存清理完成", 
                        Map.of("beforeCleanup", usedMemory, "threshold", 0.8));
            }
        } catch (Exception e) {
            auditService.logApprovalOperation("CACHE_CLEANUP_ERROR", "CacheService", null,
                    -1L, "System", "缓存清理失败: " + e.getMessage(), 
                    Map.of("error", e.getMessage()));
        }
    }

    /**
     * 缓存性能监控
     */
    public Map<String, Object> getPerformanceMetrics() {
        long startTime = System.currentTimeMillis();
        
        // 模拟性能测试
        testCachePerformance();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        Map<String, Object> stats = getCacheStats();
        stats.put("performanceTestDuration", duration + "ms");
        stats.put("timestamp", System.currentTimeMillis());
        
        return stats;
    }

    // 私有方法 - 实际实现需要根据业务逻辑填充
    
    private Map<String, Object> calculateStatistics(String type, String period) {
        // 实现实际的统计计算逻辑
        return Map.of("pending", 0, "approved", 0, "rejected", 0, "total", 0);
    }
    
    private Object loadConditionsFromDB(String businessType) {
        // 实现从数据库加载条件配置的逻辑
        return null;
    }

    // 性能优化相关私有方法

    private void preloadFlowConfigs() {
        // 预加载常用的流程配置到缓存
        // 这里可以加载活跃的业务类型配置
        try {
            // 示例：预加载常用业务类型配置
            List<String> commonBusinessTypes = Arrays.asList("EXPENSE", "LOAN", "TRAVEL");
            for (String businessType : commonBusinessTypes) {
                getFlowConfigsByTypeAndCategory(businessType, "default");
            }
        } catch (Exception e) {
            // 记录警告但不中断预热流程
        }
    }

    private void preloadRecentApprovalInstances() {
        // 预加载最近30天的活跃审批实例到缓存
        // 这里可以添加实际的数据库查询逻辑
        try {
            // 查询最近活跃的实例ID，这里需要实际的Repository调用
            List<Long> recentInstanceIds = new ArrayList<>();
            for (Long instanceId : recentInstanceIds) {
                getInstanceById(instanceId);
            }
        } catch (Exception e) {
            // 记录警告但不中断预热流程
        }
    }

    private void preloadStatistics() {
        // 预加载常用的统计数据到缓存
        try {
            // 预加载今日、本周、本月的统计数据
            getApprovalStatistics("daily", "today");
            getApprovalStatistics("daily", "yesterday");
            getApprovalStatistics("weekly", "current");
            getApprovalStatistics("monthly", "current");
        } catch (Exception e) {
            // 记录警告但不中断预热流程
        }
    }

    private Map<Long, ApprovalInstance> batchLoadInstancesFromDB(List<Long> instanceIds) {
        // 批量从数据库加载审批实例
        // 这里需要实际的Repository调用
        Map<Long, ApprovalInstance> result = new HashMap<>();
        try {
            // 实际实现：调用Repository的批量查询方法
            // 这里返回空Map，实际需要实现数据库查询逻辑
        } catch (Exception e) {
            auditService.logApprovalOperation("BATCH_LOAD_ERROR", "CacheService", null,
                    -1L, "System", "批量加载实例失败: " + e.getMessage(), 
                    Map.of("error", e.getMessage(), "instanceIds", instanceIds));
        }
        return result;
    }

    private void cleanupOldInstanceCache() {
        // 清理过期的审批实例缓存
        try {
            // 获取所有实例缓存键
            Set<String> instanceKeys = redisTemplate.keys(ApprovalCacheConfig.CacheNames.INSTANCE_KEY_PREFIX + "*");
            
            // 实现基于时间戳的清理逻辑
            // 这里可以添加复杂的过期判断逻辑
            for (String key : instanceKeys) {
                // 根据键名解析实例ID，并根据最后访问时间决定是否清理
                // 实现省略
            }
        } catch (Exception e) {
            // 记录错误但不中断清理流程
        }
    }

    private void cleanupLowPriorityCache() {
        // 清理低优先级的缓存（如统计数据缓存）
        try {
            // 清理旧版本的统计数据缓存
            Set<String> statsKeys = redisTemplate.keys(ApprovalCacheConfig.CacheNames.STATISTICS_KEY_PREFIX + "*");
            for (String key : statsKeys) {
                // 根据时间判断是否清理
                // 实现省略
            }
        } catch (Exception e) {
            // 记录错误但不中断清理流程
        }
    }

    private void testCachePerformance() {
        // 缓存性能测试方法
        try {
            // 测试读取性能
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 1000; i++) {
                getInstanceById((long) (i % 100 + 1));
            }
            long readTime = System.currentTimeMillis() - startTime;
            
            // 测试写入性能
            startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                // 测试写入性能
                setInstanceStatusCache((long) i, "TEST_STATUS");
            }
            long writeTime = System.currentTimeMillis() - startTime;
            
            // 记录性能测试结果
            auditService.logApprovalOperation("CACHE_PERF_TEST", "CacheService", null,
                    -1L, "System", "缓存性能测试完成", 
                    Map.of("readTime", readTime, "writeTime", writeTime));
            
        } catch (Exception e) {
            auditService.logApprovalOperation("CACHE_PERF_TEST_ERROR", "CacheService", null,
                    -1L, "System", "缓存性能测试失败: " + e.getMessage(), 
                    Map.of("error", e.getMessage()));
        }
    }
}