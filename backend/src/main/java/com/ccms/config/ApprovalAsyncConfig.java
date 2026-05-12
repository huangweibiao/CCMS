package com.ccms.config;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 审批模块异步任务配置
 * 针对不同类型的异步任务配置独立的线程池
 */
@Configuration
@EnableAsync
public class ApprovalAsyncConfig implements AsyncConfigurer {

    /**
     * 审批操作线程池 - 核心业务处理
     */
    @Bean("approvalTaskExecutor")
    public Executor approvalTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("approval-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * 审计日志线程池 - 日志记录处理
     */
    @Bean("auditTaskExecutor")
    public Executor auditTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(200);
        executor.setKeepAliveSeconds(120);
        executor.setThreadNamePrefix("audit-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); // 审计日志可以丢弃
        executor.initialize();
        return executor;
    }

    /**
     * 通知发送线程池 - 邮件、短信通知
     */
    @Bean("notificationTaskExecutor")
    public Executor notificationTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(180);
        executor.setThreadNamePrefix("notification-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 业务处理线程池 - 业务数据更新
     */
    @Bean("businessTaskExecutor")
    public Executor businessTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(80);
        executor.setKeepAliveSeconds(90);
        executor.setThreadNamePrefix("business-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 数据清理线程池 - 过期数据清理
     */
    @Bean("cleanupTaskExecutor")
    public Executor cleanupTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("cleanup-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(300);
        executor.initialize();
        return executor;
    }

    /**
     * 批量操作线程池 - 批量处理任务
     */
    @Bean("batchTaskExecutor")
    public Executor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(12);
        executor.setQueueCapacity(30);
        executor.setKeepAliveSeconds(150);
        executor.setThreadNamePrefix("batch-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 报表生成线程池 - 复杂计算任务
     */
    @Bean("reportTaskExecutor")
    public Executor reportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(6);
        executor.setQueueCapacity(5);
        executor.setKeepAliveSeconds(240);
        executor.setThreadNamePrefix("report-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 监控任务线程池 - 性能指标收集
     */
    @Bean("monitorTaskExecutor")
    public Executor monitorTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(20);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("monitor-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 缓存操作线程池 - 缓存预热和清理
     */
    @Bean("cacheTaskExecutor")
    public Executor cacheTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(15);
        executor.setKeepAliveSeconds(180);
        executor.setThreadNamePrefix("cache-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();
        return executor;
    }

    /**
     * 默认异步执行器
     */
    @Override
    public Executor getAsyncExecutor() {
        return approvalTaskExecutor();
    }

    /**
     * 异步异常处理
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new ApprovalAsyncExceptionHandler();
    }

    /**
     * 自定义异步异常处理器
     */
    static class ApprovalAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
        
        private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApprovalAsyncExceptionHandler.class);

        @Override
        public void handleUncaughtException(Throwable ex, Method method, Object... params) {
            logger.error("异步任务执行异常 - 方法: {}, 参数: {}", method.getName(), params, ex);
            
            // 可以根据异常类型进行不同的处理
            if (ex instanceof NullPointerException) {
                logger.warn("空指针异常，可能数据不完整");
            } else if (ex instanceof IllegalStateException) {
                logger.warn("状态异常，需要检查业务流程");
            }
            
            // 记录到专门的错误监控系统
            // errorTrackingService.trackAsyncError(method, params, ex);
        }
    }

    /**
     * 线程池监控器
     */
    @Bean
    public ApprovalThreadPoolMonitor threadPoolMonitor() {
        return new ApprovalThreadPoolMonitor();
    }

    /**
     * 线程池性能监控器
     */
    public static class ApprovalThreadPoolMonitor {
        
        public void monitorThreadPool(ThreadPoolTaskExecutor executor, String poolName) {
            logger.info("线程池 [{}] 状态: 活跃线程: {}, 队列大小: {}, 完成任务: {}", 
                poolName, 
                executor.getActiveCount(), 
                executor.getQueueSize(), 
                executor.getThreadPoolExecutor().getCompletedTaskCount());
        }
        
        public void monitorAllThreadPools() {
            // 监控所有线程池的状态
            // 可以通过注册的Bean来获取所有线程池
        }
        
        @Scheduled(fixedRate = 60000) // 每分钟监控一次
        public void scheduledMonitor() {
            // 定期监控线程池状态
            logger.debug("开始监控审批模块线程池状态");
        }
        
        private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApprovalThreadPoolMonitor.class);
    }

    /**
     * 线程池调优建议器
     */
    @Bean
    public ApprovalThreadPoolTuner threadPoolTuner() {
        return new ApprovalThreadPoolTuner();
    }

    /**
     * 线程池参数调优
     */
    public static class ApprovalThreadPoolTuner {
        
        /**
         * 根据系统负载动态调整线程池参数
         */
        public void tuneThreadPool(ThreadPoolTaskExecutor executor, String poolName, double loadFactor) {
            int currentCorePoolSize = executor.getCorePoolSize();
            int currentMaxPoolSize = executor.getMaxPoolSize();
            
            if (loadFactor > 0.8) {
                // 负载较高，增加线程数
                int newCorePoolSize = Math.min(currentCorePoolSize + 2, currentMaxPoolSize);
                executor.setCorePoolSize(newCorePoolSize);
                logger.info("线程池 [{}] 负载较高，调整核心线程数: {} -> {}", 
                    poolName, currentCorePoolSize, newCorePoolSize);
            } else if (loadFactor < 0.3) {
                // 负载较低，减少线程数
                int newCorePoolSize = Math.max(currentCorePoolSize - 1, 1);
                executor.setCorePoolSize(newCorePoolSize);
                logger.info("线程池 [{}] 负载较低，调整核心线程数: {} -> {}", 
                    poolName, currentCorePoolSize, newCorePoolSize);
            }
        }
        
        /**
         * 计算线程池负载因子
         */
        public double calculateLoadFactor(ThreadPoolTaskExecutor executor) {
            int activeCount = executor.getActiveCount();
            int maxPoolSize = executor.getMaxPoolSize();
            int queueSize = executor.getQueueSize();
            int queueCapacity = executor.getQueueCapacity();
            
            // 综合计算负载因子
            double threadLoad = (double) activeCount / maxPoolSize;
            double queueLoad = queueCapacity > 0 ? (double) queueSize / queueCapacity : 0;
            
            return Math.max(threadLoad, queueLoad);
        }
        
        private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ApprovalThreadPoolTuner.class);
    }
}