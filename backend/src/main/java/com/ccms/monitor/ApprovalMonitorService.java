package com.ccms.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 审批性能监控服务
 * 提供性能指标的记录和查询功能
 */
@Service
public class ApprovalMonitorService {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ApprovalMonitorConfig approvalMonitorConfig;

    private final Map<String, AtomicLong> gaugeMetrics = new ConcurrentHashMap<>();

    /**
     * 记录审批操作耗时
     */
    public void recordApprovalOperation(String operation, Duration duration, boolean success) {
        Timer timer = Timer.builder(ApprovalMonitorConfig.MetricNames.OPERATION_DURATION)
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry);
        
        timer.record(duration);
        
        // 记录操作计数
        Counter counter = Counter.builder(ApprovalMonitorConfig.MetricNames.OPERATION_COUNT)
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry);
        
        counter.increment();
    }

    /**
     * 记录审批流程处理时间
     */
    public void recordApprovalProcessTime(String processType, Duration duration, boolean success) {
        Timer timer = Timer.builder(ApprovalMonitorConfig.MetricNames.PROCESS_DURATION)
                .tag("process_type", processType)
                .tag("success", String.valueOf(success))
                .register(meterRegistry);
        
        timer.record(duration);
    }

    /**
     * 记录数据库查询性能
     */
    public void recordDatabaseQuery(String queryType, Duration duration, boolean cached) {
        Timer timer = Timer.builder(ApprovalMonitorConfig.MetricNames.DATABASE_QUERY_DURATION)
                .tag("query_type", queryType)
                .tag("cached", String.valueOf(cached))
                .register(meterRegistry);
        
        timer.record(duration);
    }

    /**
     * 记录缓存命中率
     */
    public void recordCacheHit(String cacheType, boolean hit) {
        Timer timer = Timer.builder(ApprovalMonitorConfig.MetricNames.CACHE_HIT_RATIO)
                .tag("cache_type", cacheType)
                .tag("hit", String.valueOf(hit))
                .register(meterRegistry);
        
        timer.record(Duration.ofNanos(1)); // 简化记录
    }

    /**
     * 记录错误
     */
    public void recordError(String errorType, String operation) {
        Counter counter = Counter.builder(ApprovalMonitorConfig.MetricNames.ERROR_COUNT)
                .tag("error_type", errorType)
                .tag("operation", operation)
                .register(meterRegistry);
        
        counter.increment();
    }

    /**
     * 更新待审批实例数量
     */
    public void updatePendingInstanceCount(long count) {
        updateGaugeMetric(ApprovalMonitorConfig.MetricNames.INSTANCE_PENDING, count);
    }

    /**
     * 更新审批队列长度
     */
    public void updateQueueLength(long length) {
        updateGaugeMetric(ApprovalMonitorConfig.MetricNames.QUEUE_LENGTH, length);
    }

    /**
     * 更新并发用户数
     */
    public void updateConcurrentUsers(long users) {
        updateGaugeMetric(ApprovalMonitorConfig.MetricNames.CONCURRENT_USERS, users);
    }

    /**
     * 更新内存使用率
     */
    public void updateMemoryUsage(long usage) {
        updateGaugeMetric(ApprovalMonitorConfig.MetricNames.MEMORY_USAGE, usage);
    }

    /**
     * 记录审批成功
     */
    public void recordApprovalSuccess(String actionType) {
        Counter counter = Counter.builder(ApprovalMonitorConfig.MetricNames.SUCCESS_COUNT)
                .tag("action_type", actionType)
                .register(meterRegistry);
        
        counter.increment();
    }

    /**
     * 记录审批拒绝
     */
    public void recordApprovalReject(String actionType) {
        Counter counter = Counter.builder(ApprovalMonitorConfig.MetricNames.REJECT_COUNT)
                .tag("action_type", actionType)
                .register(meterRegistry);
        
        counter.increment();
    }

    /**
     * 获取性能统计摘要
     */
    public Map<String, Object> getPerformanceSummary() {
        // 这里可以获取各种指标的统计信息
        // 实际实现中可以使用meterRegistry获取更详细的统计信息
        return Map.of(
            "total_operations", getTotalOperations(),
            "average_duration", getAverageDuration(),
            "pending_instances", getPendingInstanceCount(),
            "error_rate", getErrorRate(),
            "cache_hit_rate", getCacheHitRate()
        );
    }

    /**
     * 生成性能报告
     */
    public String generatePerformanceReport() {
        Map<String, Object> summary = getPerformanceSummary();
        
        StringBuilder report = new StringBuilder();
        report.append("=== 审批性能监控报告 ===\n");
        report.append(String.format("总操作次数: %d\n", summary.get("total_operations")));
        report.append(String.format("平均处理时间: %.2f ms\n", summary.get("average_duration")));
        report.append(String.format("待审批实例: %d\n", summary.get("pending_instances")));
        report.append(String.format("错误率: %.2f%%\n", summary.get("error_rate")));
        report.append(String.format("缓存命中率: %.2f%%\n", summary.get("cache_hit_rate")));
        
        return report.toString();
    }

    /**
     * 重置监控指标
     */
    public void resetMetrics() {
        // 注意：在Micrometer中，指标通常是累积的，不能直接重置
        // 这个方法主要用于测试环境
        gaugeMetrics.values().forEach(atomic -> atomic.set(0));
    }

    /**
     * 更新仪表盘指标
     */
    private void updateGaugeMetric(String metricName, long value) {
        AtomicLong gauge = gaugeMetrics.computeIfAbsent(metricName, 
            k -> new AtomicLong(0));
        gauge.set(value);
    }

    // 私有方法 - 实际的统计计算逻辑（简化实现）
    
    private long getTotalOperations() {
        return (long) meterRegistry.find(ApprovalMonitorConfig.MetricNames.OPERATION_COUNT)
                .counters()
                .stream()
                .mapToDouble(c -> c.count())
                .sum();
    }
    
    private double getAverageDuration() {
        return meterRegistry.find(ApprovalMonitorConfig.MetricNames.OPERATION_DURATION)
                .timers()
                .stream()
                .mapToDouble(t -> t.mean(TimeUnit.MILLISECONDS))
                .average()
                .orElse(0.0);
    }
    
    private long getPendingInstanceCount() {
        return gaugeMetrics.getOrDefault(ApprovalMonitorConfig.MetricNames.INSTANCE_PENDING, 
            new AtomicLong(0)).get();
    }
    
    private double getErrorRate() {
        long totalOps = getTotalOperations();
        double errorCount = meterRegistry.find(ApprovalMonitorConfig.MetricNames.ERROR_COUNT)
                .counters()
                .stream()
                .mapToDouble(c -> c.count())
                .sum();
        
        return totalOps > 0 ? (errorCount * 100.0 / totalOps) : 0.0;
    }
    
    private double getCacheHitRate() {
        // 简化实现，实际需要更复杂的计算
        return 85.0; // 示例值
    }
}