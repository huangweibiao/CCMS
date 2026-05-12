package com.ccms.monitor;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 审批模块指标收集器
 * 收集审批流程的性能指标、业务指标和系统健康指标
 */
@Component
public class ApprovalMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final ApprovalInstanceRepository instanceRepository;
    
    // 指标计数器
    private Counter submissionCounter;
    private Counter approvalCounter;
    private Counter rejectionCounter;
    private Counter cancellationCounter;
    private Counter errorCounter;
    
    // 性能计时器
    private Timer submissionTimer;
    private Timer approvalTimer;
    private Timer queryTimer;
    
    // 业务统计
    private Map<BusinessTypeEnum, Counter> businessTypeCounters;
    private Map<ApprovalStatus, AtomicLong> statusCounters;
    
    // 性能指标
    private AtomicLong averageApprovalTime;
    private AtomicLong maxApprovalTime;
    private AtomicLong minApprovalTime;
    
    public ApprovalMetricsCollector(MeterRegistry meterRegistry, 
                                   ApprovalInstanceRepository instanceRepository) {
        this.meterRegistry = meterRegistry;
        this.instanceRepository = instanceRepository;
        this.businessTypeCounters = new ConcurrentHashMap<>();
        this.statusCounters = new ConcurrentHashMap<>();
        this.averageApprovalTime = new AtomicLong(0);
        this.maxApprovalTime = new AtomicLong(0);
        this.minApprovalTime = new AtomicLong(Long.MAX_VALUE);
    }
    
    @PostConstruct
    public void initMetrics() {
        // 初始化计数器
        submissionCounter = Counter.builder("approval.submissions.total")
            .description("总审批提交次数")
            .register(meterRegistry);
            
        approvalCounter = Counter.builder("approval.approvals.total")
            .description("总审批通过次数")
            .register(meterRegistry);
            
        rejectionCounter = Counter.builder("approval.rejections.total")
            .description("总审批驳回次数")
            .register(meterRegistry);
            
        cancellationCounter = Counter.builder("approval.cancellations.total")
            .description("总取消次数")
            .register(meterRegistry);
            
        errorCounter = Counter.builder("approval.errors.total")
            .description("总错误次数")
            .register(meterRegistry);
        
        // 初始化计时器
        submissionTimer = Timer.builder("approval.submission.duration")
            .description("审批提交耗时")
            .register(meterRegistry);
            
        approvalTimer = Timer.builder("approval.approval.duration")
            .description("审批操作耗时")
            .register(meterRegistry);
            
        queryTimer = Timer.builder("approval.query.duration")
            .description("查询操作耗时")
            .register(meterRegistry);
        
        // 初始化业务类型计数器
        for (BusinessTypeEnum businessType : BusinessTypeEnum.values()) {
            businessTypeCounters.put(businessType, 
                Counter.builder("approval.business.submissions")
                    .tag("businessType", businessType.name())
                    .description("按业务类型统计的提交次数")
                    .register(meterRegistry));
        }
        
        // 初始化状态计数器
        for (ApprovalStatus status : ApprovalStatus.values()) {
            statusCounters.put(status, new AtomicLong(0));
            
            Gauge.builder("approval.instances.by.status", 
                    () -> statusCounters.get(status).get())
                .tag("status", status.name())
                .description("按状态统计的审批实例数量")
                .register(meterRegistry);
        }
        
        // 初始化性能指标Gauge
        Gauge.builder("approval.average.duration", averageApprovalTime, AtomicLong::get)
            .description("平均审批耗时（毫秒）")
            .register(meterRegistry);
            
        Gauge.builder("approval.max.duration", maxApprovalTime, AtomicLong::get)
            .description("最大审批耗时（毫秒）")
            .register(meterRegistry);
            
        Gauge.builder("approval.min.duration", minApprovalTime, AtomicLong::get)
            .description("最小审批耗时（毫秒）")
            .register(meterRegistry);
    }
    
    /**
     * 记录审批提交指标
     */
    public void recordSubmission(BusinessTypeEnum businessType, long durationMs) {
        submissionCounter.increment();
        submissionTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        if (businessTypeCounters.containsKey(businessType)) {
            businessTypeCounters.get(businessType).increment();
        }
    }
    
    /**
     * 记录审批操作指标
     */
    public void recordApproval(long durationMs) {
        approvalCounter.increment();
        approvalTimer.record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录驳回操作指标
     */
    public void recordRejection() {
        rejectionCounter.increment();
    }
    
    /**
     * 记录取消操作指标
     */
    public void recordCancellation() {
        cancellationCounter.increment();
    }
    
    /**
     * 记录错误指标
     */
    public void recordError(String errorType) {
        errorCounter.increment();
        
        // 按错误类型记录详细指标
        Counter.builder("approval.errors.by.type")
            .tag("errorType", errorType)
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * 记录查询性能指标
     */
    public void recordQuery(String queryType, long durationMs) {
        queryTimer.record(durationMs, TimeUnit.MILLISECONDS);
        
        Timer.builder("approval.query.by.type")
            .tag("queryType", queryType)
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 记录审批耗时统计
     */
    public void recordApprovalDuration(Long instanceId, long durationMs) {
        // 更新平均耗时（滑动平均）
        long currentAvg = averageApprovalTime.get();
        long newAvg = (currentAvg * 9 + durationMs) / 10; // 90%旧值 + 10%新值
        averageApprovalTime.set(newAvg);
        
        // 更新最大最小耗时
        if (durationMs > maxApprovalTime.get()) {
            maxApprovalTime.set(durationMs);
        }
        if (durationMs < minApprovalTime.get() && durationMs > 0) {
            minApprovalTime.set(durationMs);
        }
        
        // 记录按时间段的分布
        Timer.builder("approval.duration.distribution")
            .tag("durationRange", getDurationRange(durationMs))
            .register(meterRegistry)
            .record(durationMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * 获取审批实例统计数据
     */
    @Scheduled(fixedRate = 60000) // 每分钟更新一次
    public void refreshInstanceStats() {
        try {
            // 统计各状态实例数量
            for (ApprovalStatus status : ApprovalStatus.values()) {
                long count = instanceRepository.countByStatus(status);
                statusCounters.get(status).set(count);
            }
            
            // 统计今天的审批量
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            long todayCount = instanceRepository.countByCreateTimeAfter(today);
            
            Gauge.builder("approval.instances.today", 
                    () -> todayCount)
                .description("今日审批实例数量")
                .register(meterRegistry);
                
            // 统计未处理审批
            long pendingCount = instanceRepository.countByStatusIn(
                Arrays.asList(ApprovalStatus.APPROVING));
                
            Gauge.builder("approval.instances.pending", 
                    () -> pendingCount)
                .description("待处理审批数量")
                .register(meterRegistry);
                
        } catch (Exception e) {
            recordError("STATS_REFRESH_ERROR");
        }
    }
    
    /**
     * 获取详细的审批统计报告
     */
    public ApprovalMetricsReport getMetricsReport() {
        ApprovalMetricsReport report = new ApprovalMetricsReport();
        
        // 基本统计
        report.setTotalSubmissions(submissionCounter.count());
        report.setTotalApprovals(approvalCounter.count());
        report.setTotalRejections(rejectionCounter.count());
        report.setTotalCancellations(cancellationCounter.count());
        report.setTotalErrors(errorCounter.count());
        
        // 状态分布
        Map<ApprovalStatus, Long> statusDistribution = new HashMap<>();
        for (ApprovalStatus status : ApprovalStatus.values()) {
            statusDistribution.put(status, statusCounters.get(status).get());
        }
        report.setStatusDistribution(statusDistribution);
        
        // 业务类型分布
        Map<BusinessTypeEnum, Double> businessDistribution = new HashMap<>();
        double total = submissionCounter.count();
        for (BusinessTypeEnum businessType : BusinessTypeEnum.values()) {
            double count = businessTypeCounters.get(businessType).count();
            double percentage = total > 0 ? (count / total) * 100 : 0;
            businessDistribution.put(businessType, percentage);
        }
        report.setBusinessDistribution(businessDistribution);
        
        // 性能指标
        report.setAverageApprovalTime(averageApprovalTime.get());
        report.setMaxApprovalTime(maxApprovalTime.get());
        report.setMinApprovalTime(minApprovalTime.get() == Long.MAX_VALUE ? 0 : minApprovalTime.get());
        
        // 吞吐量（最近1分钟）
        report.setThroughputLastMinute(getThroughputLastMinute());
        
        return report;
    }
    
    /**
     * 获取健康状态
     */
    public ApprovalHealthStatus getHealthStatus() {
        ApprovalHealthStatus health = new ApprovalHealthStatus();
        
        health.setServiceStatus("UP");
        health.setLastUpdated(LocalDateTime.now());
        
        // 检查数据库连接
        try {
            long totalCount = instanceRepository.count();
            health.setDatabaseStatus("CONNECTED");
        } catch (Exception e) {
            health.setDatabaseStatus("DISCONNECTED");
        }
        
        // 检查错误率
        double errorRate = submissionCounter.count() > 0 ? 
            errorCounter.count() / submissionCounter.count() : 0;
        health.setErrorRate(errorRate);
        
        // 判断健康状态
        if (errorRate > 0.05) { // 错误率超过5%
            health.setOverallStatus("DEGRADED");
        } else if (errorRate > 0.1) { // 错误率超过10%
            health.setOverallStatus("DOWN");
        } else {
            health.setOverallStatus("HEALTHY");
        }
        
        return health;
    }
    
    private String getDurationRange(long durationMs) {
        if (durationMs < 1000) return "<1s";
        else if (durationMs < 5000) return "1-5s";
        else if (durationMs < 30000) return "5-30s";
        else if (durationMs < 60000) return "30-60s";
        else return ">60s";
    }
    
    private double getThroughputLastMinute() {
        // 实现最近1分钟吞吐量计算逻辑
        return submissionCounter.count() / 60.0; // 简化计算
    }
}

/**
 * 审批指标报告
 */
class ApprovalMetricsReport {
    private double totalSubmissions;
    private double totalApprovals;
    private double totalRejections;
    private double totalCancellations;
    private double totalErrors;
    private Map<ApprovalStatus, Long> statusDistribution;
    private Map<BusinessTypeEnum, Double> businessDistribution;
    private long averageApprovalTime;
    private long maxApprovalTime;
    private long minApprovalTime;
    private double throughputLastMinute;
    
    // getters and setters
    public double getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(double totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    
    public double getTotalApprovals() { return totalApprovals; }
    public void setTotalApprovals(double totalApprovals) { this.totalApprovals = totalApprovals; }
    
    public double getTotalRejections() { return totalRejections; }
    public void setTotalRejections(double totalRejections) { this.totalRejections = totalRejections; }
    
    public double getTotalCancellations() { return totalCancellations; }
    public void setTotalCancellations(double totalCancellations) { this.totalCancellations = totalCancellations; }
    
    public double getTotalErrors() { return totalErrors; }
    public void setTotalErrors(double totalErrors) { this.totalErrors = totalErrors; }
    
    public Map<ApprovalStatus, Long> getStatusDistribution() { return statusDistribution; }
    public void setStatusDistribution(Map<ApprovalStatus, Long> statusDistribution) { this.statusDistribution = statusDistribution; }
    
    public Map<BusinessTypeEnum, Double> getBusinessDistribution() { return businessDistribution; }
    public void setBusinessDistribution(Map<BusinessTypeEnum, Double> businessDistribution) { this.businessDistribution = businessDistribution; }
    
    public long getAverageApprovalTime() { return averageApprovalTime; }
    public void setAverageApprovalTime(long averageApprovalTime) { this.averageApprovalTime = averageApprovalTime; }
    
    public long getMaxApprovalTime() { return maxApprovalTime; }
    public void setMaxApprovalTime(long maxApprovalTime) { this.maxApprovalTime = maxApprovalTime; }
    
    public long getMinApprovalTime() { return minApprovalTime; }
    public void setMinApprovalTime(long minApprovalTime) { this.minApprovalTime = minApprovalTime; }
    
    public double getThroughputLastMinute() { return throughputLastMinute; }
    public void setThroughputLastMinute(double throughputLastMinute) { this.throughputLastMinute = throughputLastMinute; }
}

/**
 * 审批健康状态
 */
class ApprovalHealthStatus {
    private String serviceStatus;
    private String databaseStatus;
    private String overallStatus;
    private double errorRate;
    private LocalDateTime lastUpdated;
    
    // getters and setters
    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }
    
    public String getDatabaseStatus() { return databaseStatus; }
    public void setDatabaseStatus(String databaseStatus) { this.databaseStatus = databaseStatus; }
    
    public String getOverallStatus() { return overallStatus; }
    public void setOverallStatus(String overallStatus) { this.overallStatus = overallStatus; }
    
    public double getErrorRate() { return errorRate; }
    public void setErrorRate(double errorRate) { this.errorRate = errorRate; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}