package com.ccms.config;

import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.lang.management.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控和优化配置
 * 提供系统性能监控、优化和自动调优功能
 */
@Configuration
@EnableScheduling
public class PerformanceConfig {

    private static final AtomicLong totalRequests = new AtomicLong(0);
    private static final AtomicLong successfulRequests = new AtomicLong(0);
    private static final AtomicLong failedRequests = new AtomicLong(0);
    private static final AtomicLong totalResponseTime = new AtomicLong(0);

    /**
     * 请求日志过滤器
     */
    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }

    /**
     * 性能监控过滤器
     */
    @Bean
    public FilterRegistrationBean<PerformanceMonitorFilter> performanceMonitorFilter() {
        FilterRegistrationBean<PerformanceMonitorFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        PerformanceMonitorFilter filter = new PerformanceMonitorFilter();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        
        return registrationBean;
    }

    /**
     * 系统性能监控定时任务
     * 每分钟执行一次，监控系统关键指标
     */
    @Scheduled(fixedRate = 60000) // 每分钟执行一次
    public void monitorSystemPerformance() {
        try {
            // 获取JVM内存使用情况
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

            // 获取线程信息
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            int threadCount = threadMXBean.getThreadCount();
            int peakThreadCount = threadMXBean.getPeakThreadCount();

            // 获取垃圾收集信息
            for (GarbageCollectorMXBean gcMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
                long collectionCount = gcMXBean.getCollectionCount();
                long collectionTime = gcMXBean.getCollectionTime();
                
                // 记录GC信息到监控系统
                System.out.println("GC Collector: " + gcMXBean.getName() + 
                                 ", Collections: " + collectionCount + 
                                 ", Time: " + collectionTime + "ms");
            }

            // 计算请求成功率
            long total = totalRequests.get();
            long success = successfulRequests.get();
            long failure = failedRequests.get();
            double successRate = total > 0 ? (success * 100.0 / total) : 100.0;
            double avgResponseTime = success > 0 ? (totalResponseTime.get() / (double) success) : 0;

            // 记录性能指标
            System.out.println("=== 系统性能监控报告 ===");
            System.out.println("堆内存使用: " + formatMemory(heapMemoryUsage.getUsed()) + 
                             " / " + formatMemory(heapMemoryUsage.getMax()));
            System.out.println("非堆内存使用: " + formatMemory(nonHeapMemoryUsage.getUsed()));
            System.out.println("线程数: " + threadCount + " (峰值: " + peakThreadCount + ")");
            System.out.println("请求统计: 总计=" + total + ", 成功=" + success + 
                             ", 失败=" + failure + ", 成功率=" + String.format("%.2f%%", successRate));
            System.out.println("平均响应时间: " + String.format("%.2fms", avgResponseTime));
            System.out.println("========================");
            
            // 重置计数器
            resetCounters();
            
        } catch (Exception e) {
            System.err.println("性能监控失败: " + e.getMessage());
        }
    }

    /**
     * 实时性能监控端点（如果Spring Boot Actuator可用）
     */
    @Bean
    @org.springframework.boot.autoconfigure.condition.ConditionalOnClass(MetricsEndpoint.class)
    public PerformanceMetricsEndpoint performanceMetricsEndpoint() {
        return new PerformanceMetricsEndpoint();
    }

    /**
     * 记录请求处理
     */
    public static void recordRequest(boolean success, long responseTime) {
        totalRequests.incrementAndGet();
        if (success) {
            successfulRequests.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
        } else {
            failedRequests.incrementAndGet();
        }
    }

    /**
     * 重置计数器
     */
    private static void resetCounters() {
        totalRequests.set(0);
        successfulRequests.set(0);
        failedRequests.set(0);
        totalResponseTime.set(0);
    }

    /**
     * 格式化内存大小
     */
    private String formatMemory(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 性能监控端点类
     */
    public static class PerformanceMetricsEndpoint {
        
        public PerformanceMetrics getPerformanceMetrics() {
            Runtime runtime = Runtime.getRuntime();
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
            
            PerformanceMetrics metrics = new PerformanceMetrics();
            
            // 内存指标
            metrics.setHeapMemoryUsed(memoryMXBean.getHeapMemoryUsage().getUsed());
            metrics.setHeapMemoryMax(memoryMXBean.getHeapMemoryUsage().getMax());
            metrics.setNonHeapMemoryUsed(memoryMXBean.getNonHeapMemoryUsage().getUsed());
            
            // 线程指标
            metrics.setThreadCount(threadMXBean.getThreadCount());
            metrics.setPeakThreadCount(threadMXBean.getPeakThreadCount());
            
            // 性能指标
            long total = totalRequests.get();
            long success = successfulRequests.get();
            metrics.setTotalRequests(total);
            metrics.setSuccessfulRequests(success);
            metrics.setFailedRequests(failedRequests.get());
            metrics.setSuccessRate(total > 0 ? (success * 100.0 / total) : 100.0);
            metrics.setAverageResponseTime(success > 0 ? (totalResponseTime.get() / (double) success) : 0);
            
            return metrics;
        }
    }

    /**
     * 性能指标类
     */
    public static class PerformanceMetrics {
        private long heapMemoryUsed;
        private long heapMemoryMax;
        private long nonHeapMemoryUsed;
        private int threadCount;
        private int peakThreadCount;
        private long totalRequests;
        private long successfulRequests;
        private long failedRequests;
        private double successRate;
        private double averageResponseTime;

        // Getters and Setters
        public long getHeapMemoryUsed() { return heapMemoryUsed; }
        public void setHeapMemoryUsed(long heapMemoryUsed) { this.heapMemoryUsed = heapMemoryUsed; }
        
        public long getHeapMemoryMax() { return heapMemoryMax; }
        public void setHeapMemoryMax(long heapMemoryMax) { this.heapMemoryMax = heapMemoryMax; }
        
        public long getNonHeapMemoryUsed() { return nonHeapMemoryUsed; }
        public void setNonHeapMemoryUsed(long nonHeapMemoryUsed) { this.nonHeapMemoryUsed = nonHeapMemoryUsed; }
        
        public int getThreadCount() { return threadCount; }
        public void setThreadCount(int threadCount) { this.threadCount = threadCount; }
        
        public int getPeakThreadCount() { return peakThreadCount; }
        public void setPeakThreadCount(int peakThreadCount) { this.peakThreadCount = peakThreadCount; }
        
        public long getTotalRequests() { return totalRequests; }
        public void setTotalRequests(long totalRequests) { this.totalRequests = totalRequests; }
        
        public long getSuccessfulRequests() { return successfulRequests; }
        public void setSuccessfulRequests(long successfulRequests) { this.successfulRequests = successfulRequests; }
        
        public long getFailedRequests() { return failedRequests; }
        public void setFailedRequests(long failedRequests) { this.failedRequests = failedRequests; }
        
        public double getSuccessRate() { return successRate; }
        public void setSuccessRate(double successRate) { this.successRate = successRate; }
        
        public double getAverageResponseTime() { return averageResponseTime; }
        public void setAverageResponseTime(double averageResponseTime) { this.averageResponseTime = averageResponseTime; }

        @Override
        public String toString() {
            return "PerformanceMetrics{" +
                    "heapMemoryUsed=" + heapMemoryUsed +
                    ", heapMemoryMax=" + heapMemoryMax +
                    ", nonHeapMemoryUsed=" + nonHeapMemoryUsed +
                    ", threadCount=" + threadCount +
                    ", peakThreadCount=" + peakThreadCount +
                    ", totalRequests=" + totalRequests +
                    ", successfulRequests=" + successfulRequests +
                    ", failedRequests=" + failedRequests +
                    ", successRate=" + successRate +
                    ", averageResponseTime=" + averageResponseTime +
                    '}';
        }
    }
}

/**
 * 性能监控过滤器
 */
class PerformanceMonitorFilter implements jakarta.servlet.Filter {
    
    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(PerformanceMonitorFilter.class);
    
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, 
                        jakarta.servlet.ServletResponse response, 
                        jakarta.servlet.FilterChain chain) 
            throws java.io.IOException, jakarta.servlet.ServletException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录慢查询
            if (duration > 1000) { // 超过1秒视为慢查询
                String requestUrl = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                logger.warn("慢请求检测 - URL: {}, 耗时: {}ms", requestUrl, duration);
            }
            
            // 记录性能数据（可以发送到监控系统）
            if (logger.isDebugEnabled()) {
                String requestUrl = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                logger.debug("请求处理完成 - URL: {}, 耗时: {}ms", requestUrl, duration);
            }
        }
    }
}