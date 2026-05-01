package com.ccms.controller.monitor;

import com.ccms.service.cache.CacheService;
import com.ccms.service.monitor.PerformanceMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 缓存管理控制器
 */
@RestController
@RequestMapping("/api/monitor/cache")
public class CacheManagementController {

    @Autowired
    private CacheService cacheService;

    @Autowired
    private PerformanceMonitor performanceMonitor;

    /**
     * 获取缓存统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, CacheService.CacheStats>> getCacheStats() {
        // 获取所有缓存的统计信息
        List<String> cacheNames = List.of("system_config", "data_dict", "user_permissions", 
                                         "expense_statistics", "budget_info");
        
        Map<String, CacheService.CacheStats> stats = new java.util.HashMap<>();
        for (String cacheName : cacheNames) {
            stats.put(cacheName, cacheService.getCacheStats(cacheName));
        }
        
        // 添加性能监控的缓存命中率统计
        Map<String, PerformanceMonitor.CacheStats> monitorStats = performanceMonitor.getCacheStats();
        for (Map.Entry<String, PerformanceMonitor.CacheStats> entry : monitorStats.entrySet()) {
            CacheService.CacheStats serviceStats = stats.get(entry.getKey());
            if (serviceStats == null) {
                stats.put(entry.getKey(), new CacheService.CacheStats(entry.getKey(), 0, (int) (entry.getValue().getHitRate() * 100)));
            }
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     * 清除指定缓存
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<Void> clearCache(@PathVariable String cacheName) {
        cacheService.clear(cacheName);
        return ResponseEntity.ok().build();
    }

    /**
     * 清除所有缓存
     */
    @DeleteMapping("/all")
    public ResponseEntity<Void> clearAllCaches() {
        List<String> cacheNames = List.of("system_config", "data_dict", "user_permissions", 
                                         "expense_statistics", "budget_info");
        
        for (String cacheName : cacheNames) {
            cacheService.clear(cacheName);
        }
        
        return ResponseEntity.ok().build();
    }

    /**
     * 重新加载配置缓存
     */
    @PostMapping("/system-config/reload")
    public ResponseEntity<Void> reloadSystemConfigCache() {
        cacheService.clear("system_config");
        // 这里可以添加重新加载配置的逻辑
        return ResponseEntity.ok().build();
    }

    /**
     * 重新加载数据字典缓存
     */
    @PostMapping("/data-dict/reload")
    public ResponseEntity<Void> reloadDataDictCache() {
        cacheService.clear("data_dict");
        // 这里可以添加重新加载字典的逻辑
        return ResponseEntity.ok().build();
    }

    /**
     * 预热缓存
     */
    @PostMapping("/preload/{cacheName}")
    public ResponseEntity<Void> preloadCache(@PathVariable String cacheName) {
        cacheService.preloadCache(cacheName, () -> {
            // 这里可以根据缓存名称添加相应的预热逻辑
            switch (cacheName) {
                case "system_config":
                    return "系统配置预热完成";
                case "data_dict":
                    return "数据字典预热完成";
                case "user_permissions":
                    return "用户权限预热完成";
                case "expense_statistics":
                    return "费用统计预热完成";
                case "budget_info":
                    return "预算信息预热完成";
                default:
                    return "未知缓存类型";
            }
        });
        
        return ResponseEntity.ok().build();
    }

    /**
     * 获取缓存详情
     */
    @GetMapping("/{cacheName}/details")
    public ResponseEntity<CacheDetails> getCacheDetails(@PathVariable String cacheName) {
        // 获取缓存大小和命中率信息
        CacheService.CacheStats stats = cacheService.getCacheStats(cacheName);
        
        // 获取缓存键列表（需要根据实际缓存实现调整）
        List<String> cacheKeys = getCacheKeys(cacheName);
        
        CacheDetails details = new CacheDetails(cacheName, stats.getSize(), stats.getHitCount(), cacheKeys);
        return ResponseEntity.ok(details);
    }

    /**
     * 获取所有缓存名称
     */
    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllCacheNames() {
        List<String> cacheNames = List.of("system_config", "data_dict", "user_permissions", 
                                         "expense_statistics", "budget_info");
        return ResponseEntity.ok(cacheNames);
    }

    /**
     * 获取缓存中指定键的值
     */
    @GetMapping("/{cacheName}/{key}")
    public ResponseEntity<Object> getCacheValue(@PathVariable String cacheName, @PathVariable String key) {
        Object value = cacheService.get(cacheName, key);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    /**
     * 删除缓存中指定键
     */
    @DeleteMapping("/{cacheName}/{key}")
    public ResponseEntity<Void> deleteCacheKey(@PathVariable String cacheName, @PathVariable String key) {
        cacheService.evict(cacheName, key);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取缓存键列表（简化实现）
     */
    private List<String> getCacheKeys(String cacheName) {
        // 这里简化处理，实际应用中需要根据缓存实现来获取键列表
        // 可以集成具体的缓存实现（如RedisTemplate）来获取所有键
        List<String> keys = new ArrayList<>();
        switch (cacheName) {
            case "system_config":
                keys.add("system.config.setting");
                keys.add("system.config.database");
                break;
            case "data_dict":
                keys.add("dict.expense_type");
                keys.add("dict.approval_status");
                break;
            default:
                keys.add("sample_key");
                break;
        }
        return keys;
    }

    /**
     * 缓存详情类
     */
    public static class CacheDetails {
        private final String cacheName;
        private final int size;
        private final int hitCount;
        private final List<String> cacheKeys;

        public CacheDetails(String cacheName, int size, int hitCount, List<String> cacheKeys) {
            this.cacheName = cacheName;
            this.size = size;
            this.hitCount = hitCount;
            this.cacheKeys = cacheKeys;
        }

        public String getCacheName() { return cacheName; }
        public int getSize() { return size; }
        public int getHitCount() { return hitCount; }
        public List<String> getCacheKeys() { return cacheKeys; }
    }
}