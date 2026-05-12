package com.ccms.monitor;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 审批性能监控配置类
 * 配置各类性能监控指标和仪表板
 */
@Configuration
public class ApprovalMonitorConfig {

    @Autowired
    private MeterRegistry meterRegistry;

    /**
     * 审批操作耗时监控
     */
    @Bean
    public Timer approvalOperationTimer() {
        return Timer.builder("approval.operation.duration")
                .description("审批操作执行时间")
                .register(meterRegistry);
    }

    /**
     * 审批操作计数监控
     */
    @Bean
    public Counter approvalOperationCounter() {
        return Counter.builder("approval.operation.count")
                .description("审批操作执行次数")
                .register(meterRegistry);
    }

    /**
     * 审批实例状态监控
     */
    @Bean
    public AtomicLong approvalPendingCount() {
        AtomicLong pendingCount = new AtomicLong(0);
        Gauge.builder("approval.instance.pending", pendingCount, AtomicLong::get)
                .description("待审批实例数量")
                .register(meterRegistry);
        return pendingCount;
    }

    /**
     * 审批队列长度监控
     */
    @Bean
    public AtomicLong approvalQueueLength() {
        AtomicLong queueLength = new AtomicLong(0);
        Gauge.builder("approval.queue.length", queueLength, AtomicLong::get)
                .description("审批队列长度")
                .register(meterRegistry);
        return queueLength;
    }

    /**
     * 缓存命中率监控
     */
    @Bean
    public Timer cacheHitTimer() {
        return Timer.builder("approval.cache.hit.ratio")
                .description("缓存命中率统计")
                .register(meterRegistry);
    }

    /**
     * 数据库查询耗时监控
     */
    @Bean
    public Timer databaseQueryTimer() {
        return Timer.builder("approval.database.query.duration")
                .description("数据库查询执行时间")
                .register(meterRegistry);
    }

    /**
     * 审批流程处理耗时监控
     */
    @Bean
    public Timer approvalProcessTimer() {
        return Timer.builder("approval.process.duration")
                .description("审批流程完整处理时间")
                .register(meterRegistry);
    }

    /**
     * 错误率监控
     */
    @Bean
    public Counter errorCounter() {
        return Counter.builder("approval.error.count")
                .description("审批操作错误次数")
                .register(meterRegistry);
    }

    /**
     * 并发用户数监控
     */
    @Bean
    public AtomicLong concurrentUsers() {
        AtomicLong users = new AtomicLong(0);
        Gauge.builder("approval.concurrent.users", users, AtomicLong::get)
                .description("并发审批用户数量")
                .register(meterRegistry);
        return users;
    }

    /**
     * 内存使用率监控
     */
    @Bean
    public AtomicLong memoryUsage() {
        AtomicLong usage = new AtomicLong(0);
        Gauge.builder("approval.memory.usage", usage, AtomicLong::get)
                .description("审批模块内存使用率")
                .register(meterRegistry);
        return usage;
    }

    /**
     * 审批成功率监控
     */
    @Bean
    public Counter approvalSuccessCounter() {
        return Counter.builder("approval.success.count")
                .description("审批成功次数")
                .register(meterRegistry);
    }

    /**
     * 审批拒绝率监控
     */
    @Bean
    public Counter approvalRejectCounter() {
        return Counter.builder("approval.reject.count")
                .description("审批拒绝次数")
                .register(meterRegistry);
    }

    /**
     * 监控指标常量定义
     */
    public static class MetricNames {
        public static final String OPERATION_DURATION = "approval.operation.duration";
        public static final String OPERATION_COUNT = "approval.operation.count";
        public static final String INSTANCE_PENDING = "approval.instance.pending";
        public static final String QUEUE_LENGTH = "approval.queue.length";
        public static final String CACHE_HIT_RATIO = "approval.cache.hit.ratio";
        public static final String DATABASE_QUERY_DURATION = "approval.database.query.duration";
        public static final String PROCESS_DURATION = "approval.process.duration";
        public static final String ERROR_COUNT = "approval.error.count";
        public static final String CONCURRENT_USERS = "approval.concurrent.users";
        public static final String MEMORY_USAGE = "approval.memory.usage";
        public static final String SUCCESS_COUNT = "approval.success.count";
        public static final String REJECT_COUNT = "approval.reject.count";
    }
}