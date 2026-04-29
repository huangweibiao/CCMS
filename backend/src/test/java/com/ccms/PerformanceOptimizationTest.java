package com.ccms;

import com.ccms.common.response.ApiResponse;
import com.ccms.config.AsyncConfig;
import com.ccms.config.CacheConfig;
import com.ccms.controller.monitor.SystemMonitorController;
import com.ccms.service.schedule.ScheduledTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能优化功能集成测试
 */
@SpringBootTest
class PerformanceOptimizationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SystemMonitorController systemMonitorController;

    @Autowired
    private ScheduledTaskService scheduledTaskService;

    @Test
    void testPerformanceConfigBeansExist() {
        // 验证缓存配置是否存在
        assertNotNull(applicationContext.getBean(CacheManager.class), "缓存管理器应该存在");
        
        // 验证异步配置是否存在
        assertNotNull(applicationContext.getBean(AsyncConfig.class), "异步配置应该存在");
    }

    @Test
    void testSystemMonitorEndpoints() {
        // 测试健康检查端点
        ApiResponse<Map<String, Object>> healthResponse = systemMonitorController.healthCheck();
        assertNotNull(healthResponse, "健康检查响应不应为空");
        assertEquals(200, healthResponse.getCode(), "健康检查状态码应该是200");
        
        Map<String, Object> healthData = healthResponse.getData();
        assertNotNull(healthData, "健康检查数据不应为空");
        assertTrue(healthData.containsKey("status"), "健康检查应包含状态字段");
        
        // 测试系统信息端点
        ApiResponse<Map<String, Object>> systemInfoResponse = systemMonitorController.getSystemInfo();
        assertNotNull(systemInfoResponse, "系统信息响应不应为空");
        assertEquals(200, systemInfoResponse.getCode(), "系统信息状态码应该是200");
        
        Map<String, Object> systemInfo = systemInfoResponse.getData();
        assertNotNull(systemInfo, "系统信息数据不应为空");
        assertTrue(systemInfo.containsKey("jvmUptime"), "系统信息应包含JVM运行时间");
        
        // 测试数据库状态端点
        ApiResponse<Map<String, Object>> dbStatusResponse = systemMonitorController.getDatabaseStatus();
        assertNotNull(dbStatusResponse, "数据库状态响应不应为空");
        assertEquals(200, dbStatusResponse.getCode(), "数据库状态状态码应该是200");
        
        // 测试缓存状态端点
        ApiResponse<Map<String, Object>> cacheStatusResponse = systemMonitorController.getCacheStatus();
        assertNotNull(cacheStatusResponse, "缓存状态响应不应为空");
        assertEquals(200, cacheStatusResponse.getCode(), "缓存状态状态码应该是200");
    }

    @Test
    void testScheduledTaskService() {
        // 验证定时任务服务可用
        assertNotNull(scheduledTaskService, "定时任务服务不应为空");
        
        // 测试获取执行信息方法
        Map<String, Object> lastExecutionInfo = scheduledTaskService.getLastExecutionInfo();
        assertNotNull(lastExecutionInfo, "最后执行信息不应为空");
        assertTrue(lastExecutionInfo.containsKey("lastHealthCheck"), "执行信息应包含健康检查时间");
        assertTrue(lastExecutionInfo.containsKey("taskCount"), "执行信息应包含任务计数");
    }

    @Test
    void testApiResponseStructure() {
        // 测试成功响应
        ApiResponse<String> successResponse = ApiResponse.success("test data");
        assertNotNull(successResponse, "成功响应不应为空");
        assertEquals(200, successResponse.getCode(), "成功响应状态码应为200");
        assertEquals("操作成功", successResponse.getMessage(), "成功响应消息应正确");
        assertEquals("test data", successResponse.getData(), "响应数据应正确");
        
        // 测试错误响应
        ApiResponse<String> errorResponse = ApiResponse.error("测试错误");
        assertNotNull(errorResponse, "错误响应不应为空");
        assertEquals(500, errorResponse.getCode(), "错误响应状态码应为500");
        assertEquals("测试错误", errorResponse.getMessage(), "错误响应消息应正确");
        assertNull(errorResponse.getData(), "错误响应数据应为空");
        
        // 测试自定义错误响应
        ApiResponse<String> customError = ApiResponse.error(400, "参数错误");
        assertNotNull(customError, "自定义错误响应不应为空");
        assertEquals(400, customError.getCode(), "自定义错误状态码应正确");
        assertEquals("参数错误", customError.getMessage(), "自定义错误消息应正确");
    }

    @Test
    void testCacheConfig() {
        // 验证缓存配置是否正确加载
        CacheConfig cacheConfig = applicationContext.getBean(CacheConfig.class);
        assertNotNull(cacheConfig, "缓存配置类应该存在");
        
        // 获取缓存管理器
        CacheManager cacheManager = applicationContext.getBean(CacheManager.class);
        assertNotNull(cacheManager, "缓存管理器应该存在");
        
        // 验证缓存管理器的类型
        String cacheManagerClass = cacheManager.getClass().getSimpleName();
        assertTrue(cacheManagerClass.contains("CacheManager"), "缓存管理器类型应该正确");
    }

    @Test
    void testAsyncConfig() {
        // 验证异步配置是否正确加载
        AsyncConfig asyncConfig = applicationContext.getBean(AsyncConfig.class);
        assertNotNull(asyncConfig, "异步配置类应该存在");
        
        // 检查是否有异步任务执行器  
        // 实际应用中，应该通过bean名称查找具体的执行器
    }

    @Test
    void testPerformanceIntegration() {
        // 综合性能测试：验证各组件协同工作
        ApiResponse<Map<String, Object>> healthResponse = systemMonitorController.healthCheck();
        Map<String, Object> healthData = healthResponse.getData();
        
        assertNotNull(healthData.get("components"), "健康检查应包含组件信息");
        
        @SuppressWarnings("unchecked")
        Map<String, String> components = (Map<String, String>) healthData.get("components");
        assertTrue(components.containsKey("application"), "组件应包含应用状态");
        assertEquals("UP", components.get("application"), "应用状态应该是UP");
        
        // 验证性能监控功能通过
        ApiResponse<Map<String, Object>> systemInfoResponse = systemMonitorController.getSystemInfo();
        assertNotNull(systemInfoResponse.getData().get("memory"), "系统信息应包含内存使用情况");
        
        @SuppressWarnings("unchecked")
        Map<String, Object> memoryInfo = (Map<String, Object>) systemInfoResponse.getData().get("memory");
        assertNotNull(memoryInfo.get("heapUsed"), "内存信息应包含堆内存使用");
    }

    @Test
    void testErrorHandlingScenario() {
        // 模拟错误场景下的响应
        ApiResponse<String> errorResponse = ApiResponse.error(404, "资源未找到");
        
        assertEquals(404, errorResponse.getCode(), "自定义错误码应正确");
        assertEquals("资源未找到", errorResponse.getMessage(), "错误消息应正确");
        assertNull(errorResponse.getData(), "错误响应数据应为空");
        assertNotNull(errorResponse.getTimestamp(), "响应时间戳不应为空");
    }
}