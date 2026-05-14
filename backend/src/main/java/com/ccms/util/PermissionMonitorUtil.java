package com.ccms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 权限系统监控工具类
 * 记录权限验证、登录认证、菜单访问等关键指标
 */
@Component
public class PermissionMonitorUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(PermissionMonitorUtil.class);
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT_PERMISSION");
    
    // 权限验证统计
    private final Map<String, AtomicLong> permissionCheckCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> permissionCheckTime = new ConcurrentHashMap<>();
    
    // 登录统计
    private final Map<String, AtomicLong> loginSuccessCount = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> loginFailureCount = new ConcurrentHashMap<>();
    
    // 监控配置
    @Value("${monitoring.permission.validation-timeout-ms:500}")
    private long validationTimeoutMs;
    
    @Value("${monitoring.permission.login-failure-alert-threshold:5}")
    private int loginFailureAlertThreshold;
    
    /**
     * 记录权限验证结果
     */
    public void logPermissionCheck(String userId, String permission, boolean granted, long executionTime) {
        String key = userId != null ? userId : "anonymous";
        
        // 统计计数
        permissionCheckCount.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
        permissionCheckTime.computeIfAbsent(key, k -> new AtomicLong(0)).addAndGet(executionTime);
        
        MDC.put("userId", userId);
        
        if (executionTime > validationTimeoutMs) {
            logger.warn("权限验证耗时过长 - userId: {}, permission: {}, time: {}ms", 
                       userId, permission, executionTime);
        }
        
        auditLogger.info("权限验证 - userId: {}, permission: {}, granted: {}, time: {}ms", 
                        userId, permission, granted, executionTime);
        
        MDC.clear();
    }
    
    /**
     * 记录登录成功
     */
    public void logLoginSuccess(String userId, String username, String loginIp) {
        loginSuccessCount.computeIfAbsent(userId, k -> new AtomicLong(0)).incrementAndGet();
        
        MDC.put("userId", userId);
        auditLogger.info("登录成功 - userId: {}, username: {}, ip: {}, time: {}", 
                        userId, username, loginIp, LocalDateTime.now());
        MDC.clear();
    }
    
    /**
     * 记录登录失败
     */
    public void logLoginFailure(String username, String loginIp, String reason) {
        loginFailureCount.computeIfAbsent(username, k -> new AtomicLong(0)).incrementAndGet();
        
        long failureCount = loginFailureCount.getOrDefault(username, new AtomicLong(0)).get();
        
        if (failureCount >= loginFailureAlertThreshold) {
            logger.warn("登录失败次数超限 - username: {}, count: {}, ip: {}", 
                       username, failureCount, loginIp);
        }
        
        MDC.put("userId", "unknown");
        auditLogger.warn("登录失败 - username: {}, ip: {}, reason: {}, count: {}", 
                        username, loginIp, reason, failureCount);
        MDC.clear();
    }
    
    /**
     * 记录菜单访问
     */
    public void logMenuAccess(String userId, String menuCode, String menuName) {
        MDC.put("userId", userId);
        auditLogger.info("菜单访问 - userId: {}, menuCode: {}, menuName: {}", 
                        userId, menuCode, menuName);
        MDC.clear();
    }
    
    /**
     * 记录权限变更
     */
    public void logPermissionChange(String operator, String targetUser, String changeType, String details) {
        MDC.put("userId", operator);
        auditLogger.info("权限变更 - operator: {}, targetUser: {}, type: {}, details: {}", 
                        operator, targetUser, changeType, details);
        MDC.clear();
    }
    
    /**
     * 记录权限允许访问
     */
    public static void logPermissionAllowed(String userId, String permissionCode, String grantMethod) {
        MDC.put("userId", userId);
        auditLogger.info("权限允许 - userId: {}, permission: {}, grantMethod: {}", 
                        userId, permissionCode, grantMethod);
        MDC.clear();
    }
    
    /**
     * 记录无权限访问尝试
     */
    public static void logPermissionDenied(String userId, String resource, String operation) {
        MDC.put("userId", userId);
        auditLogger.warn("无权限访问 - userId: {}, resource: {}, operation: {}", 
                        userId, resource, operation);
        MDC.clear();
    }
    
    /**
     * 记录权限查询
     */
    public static void logPermissionQuery(String userId, String queryType, String result) {
        MDC.put("userId", userId);
        auditLogger.info("权限查询 - userId: {}, type: {}, result: {}", 
                        userId, queryType, result);
        MDC.clear();
    }
    
    /**
     * 记录菜单访问（重载方法，支持数量参数）
     */
    public static void logMenuAccess(String userId, String operationType, int count) {
        MDC.put("userId", userId);
        auditLogger.info("菜单操作 - userId: {}, type: {}, count: {}", 
                        userId, operationType, count);
        MDC.clear();
    }
    
    /**
     * 记录菜单访问（重载方法，支持long参数）
     */
    public static void logMenuAccess(String userId, String operationType, long count) {
        MDC.put("userId", userId);
        auditLogger.info("菜单操作 - userId: {}, type: {}, count: {}", 
                        userId, operationType, count);
        MDC.clear();
    }
    
    /**
     * 获取用户权限验证统计
     */
    public Map<String, Object> getUserPermissionStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("permissionCheckCount", permissionCheckCount.getOrDefault(userId, new AtomicLong(0)).get());
        stats.put("permissionCheckTime", permissionCheckTime.getOrDefault(userId, new AtomicLong(0)).get());
        stats.put("loginSuccessCount", loginSuccessCount.getOrDefault(userId, new AtomicLong(0)).get());
        stats.put("loginFailureCount", loginFailureCount.getOrDefault(userId, new AtomicLong(0)).get());
        
        long totalChecks = stats.get("permissionCheckCount").equals(0) ? 1 : (long) stats.get("permissionCheckCount");
        long totalTime = (long) stats.get("permissionCheckTime");
        stats.put("averageCheckTime", totalTime / totalChecks);
        
        return stats;
    }
    
    /**
     * 获取系统权限总体统计
     */
    public Map<String, Object> getSystemPermissionStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalUsers", permissionCheckCount.size());
        stats.put("totalPermissionChecks", permissionCheckCount.values().stream().mapToLong(AtomicLong::get).sum());
        stats.put("totalLoginSuccess", loginSuccessCount.values().stream().mapToLong(AtomicLong::get).sum());
        stats.put("totalLoginFailure", loginFailureCount.values().stream().mapToLong(AtomicLong::get).sum());
        
        return stats;
    }
    
    /**
     * 清理过期的统计数据
     */
    public void cleanupExpiredStats(int retentionDays) {
        // 实现定期清理逻辑
        logger.info("清理过期的权限监控数据，保留天数: {}", retentionDays);
        // 实际实现需要根据时间戳来清理过期数据
    }
    

}