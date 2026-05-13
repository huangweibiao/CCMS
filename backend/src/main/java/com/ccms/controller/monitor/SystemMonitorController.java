package com.ccms.controller.monitor;

import com.ccms.common.response.ApiResponse;
import com.ccms.service.schedule.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 系统监控控制器
 */
@RestController
@RequestMapping("/api/monitor")
public class SystemMonitorController {
    
    private static final Logger log = LoggerFactory.getLogger(SystemMonitorController.class);

    @Autowired
    private CacheManager cacheManager;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ScheduledTaskService scheduledTaskService;

    /**
     * 获取系统基本信息
     */
    @GetMapping("/system/info")
    public ApiResponse<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // JVM信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemory = memoryBean.getNonHeapMemoryUsage();
        
        info.put("systemTime", LocalDateTime.now());
        info.put("jvmUptime", ManagementFactory.getRuntimeMXBean().getUptime());
        
        Map<String, Object> memoryInfo = new HashMap<>();
        memoryInfo.put("heapUsed", heapMemory.getUsed() / 1024 / 1024);
        memoryInfo.put("heapMax", heapMemory.getMax() / 1024 / 1024);
        memoryInfo.put("nonHeapUsed", nonHeapMemory.getUsed() / 1024 / 1024);
        info.put("memory", memoryInfo);
        
        // 操作系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        info.put("os", osBean.getName() + " " + osBean.getVersion());
        info.put("availableProcessors", osBean.getAvailableProcessors());
        info.put("systemLoadAverage", osBean.getSystemLoadAverage());
        
        return ApiResponse.success(info);
    }

    /**
     * 获取数据库状态
     */
    @GetMapping("/database/status")
    public ApiResponse<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 测试数据库连接
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM (SELECT 1) AS test", Integer.class);
            status.put("connection", "healthy");
            status.put("testQuerySuccess", count != null);
            
            // 获取基本统计信息
            try {
                Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
                Integer expenseCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM expense_reimburse", Integer.class);
                Integer attachmentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_attachment", Integer.class);
                
                status.put("userCount", userCount);
                status.put("expenseCount", expenseCount);
                status.put("attachmentCount", attachmentCount);
            } catch (Exception e) {
                log.warn("获取数据库统计信息失败: {}", e.getMessage());
            }
            
        } catch (Exception e) {
            status.put("connection", "unhealthy");
            status.put("error", e.getMessage());
        }
        
        return ApiResponse.success(status);
    }

    /**
     * 获取缓存状态
     */
    @GetMapping("/cache/status")
    public ApiResponse<Map<String, Object>> getCacheStatus() {
        Map<String, Object> status = new HashMap<>();
        
        String cacheType = cacheManager.getClass().getSimpleName();
        status.put("cacheType", cacheType);
        status.put("cacheManager", cacheManager.getClass().getName());
        
        // Redis状态检查
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().get("health_check");
                status.put("redis", "connected");
            } catch (Exception e) {
                status.put("redis", "disconnected");
                status.put("redisError", e.getMessage());
            }
        } else {
            status.put("redis", "not_configured");
        }
        
        return ApiResponse.success(status);
    }

    /**
     * 获取任务执行状态
     */
    @GetMapping("/task/status")
    public ApiResponse<Map<String, Object>> getTaskStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 获取异步任务线程池状态
            Map<String, Object> threadPools = new HashMap<>();
            
            // 这里可以添加线程池状态检查
            // 需要通过特殊方式获取AsyncConfig中的线程池实例
            
            status.put("lastTaskExecution", scheduledTaskService.getLastExecutionInfo());
            status.put("threadPools", threadPools);
            
        } catch (Exception e) {
            log.error("获取任务状态失败: {}", e.getMessage());
        }
        
        return ApiResponse.success(status);
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        
        Map<String, String> components = new HashMap<>();
        
        // 检查数据库
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            components.put("database", "UP");
        } catch (Exception e) {
            components.put("database", "DOWN");
            health.put("status", "DEGRADED");
        }
        
        // 检查缓存
        try {
            if (redisTemplate != null) {
                redisTemplate.opsForValue().get("health_check");
                components.put("cache", "UP");
            } else {
                components.put("cache", "NO_REDIS");
            }
        } catch (Exception e) {
            components.put("cache", "DOWN");
            health.put("status", "DEGRADED");
        }
        
        components.put("application", "UP");
        health.put("components", components);
        
        return ApiResponse.success(health);
    }
}