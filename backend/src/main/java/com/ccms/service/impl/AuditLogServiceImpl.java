package com.ccms.service.impl;

import com.ccms.entity.audit.AuditLog;
import com.ccms.repository.audit.AuditLogRepository;
import com.ccms.service.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审计日志服务实现类
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;
    
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(String module, String operation, String description,
                           Long userId, String username, String userIp, Boolean success) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setModule(module);
            auditLog.setOperation(operation);
            auditLog.setDescription(description);
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setUserIp(userIp);
            
            if (success != null) {
                if (success) {
                    auditLog.markSuccess();
                } else {
                    auditLog.markFailure("操作失败", 500);
                }
            }
            
            auditLog.setCreateTime(LocalDateTime.now());
            auditLog.setUpdateTime(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志失败: module={}, operation={}, user={}", module, operation, username, e);
            // 审计日志记录失败不应影响主业务流程
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOperation(String module, String operation, String description,
                           Long userId, String username, String userIp, Boolean success,
                           String requestMethod, String requestUrl, String requestParams) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setModule(module);
            auditLog.setOperation(operation);
            auditLog.setDescription(description);
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setUserIp(userIp);
            
            if (success != null) {
                if (success) {
                    auditLog.markSuccess();
                } else {
                    auditLog.markFailure("操作失败", 500);
                }
            }
            
            auditLog.setRequestMethod(requestMethod);
            auditLog.setRequestUrl(requestUrl);
            auditLog.setRequestParams(requestParams);
            auditLog.setCreateTime(LocalDateTime.now());
            auditLog.setUpdateTime(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("记录审计日志失败: module={}, operation={}, user={}", module, operation, username, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logEntityOperation(String module, String operation, String description,
                                 Long userId, String username, String userIp, Boolean success,
                                 String entityType, Long entityId, String entityData) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setModule(module);
            auditLog.setOperation(operation);
            auditLog.setDescription(description);
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setUserIp(userIp);
            
            if (success != null) {
                if (success) {
                    auditLog.markSuccess();
                } else {
                    auditLog.markFailure("操作失败", 500);
                }
            }
            
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setEntityName(entityData);  // 使用entityName字段存储entityData
            auditLog.setCreateTime(LocalDateTime.now());
            auditLog.setUpdateTime(LocalDateTime.now());
            
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("记录实体操作日志失败: module={}, operation={}, entity={}", module, operation, entityType, e);
        }
    }

    @Override
    public Page<AuditLog> findAuditLogs(String module, String operation, String username,
                                      Boolean success, LocalDateTime startTime, LocalDateTime endTime,
                                      Pageable pageable) {
        return auditLogRepository.findByComplexConditions(module, operation, username, 
                                                         success, startTime, endTime, pageable);
    }

    @Override
    public AuditLog findById(Long id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    @Override
    public Page<AuditLog> getEntityAuditHistory(String entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreateTimeDesc(entityType, entityId, pageable);
    }

    @Override
    public Map<String, Object> getUserOperationStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取操作计数
        List<Object[]> operationCounts = auditLogRepository.getOperationStatsByUser(userId, startTime, endTime);
        Map<String, Long> operationStats = operationCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], 
                        arr -> (Long) arr[1]
                ));
        stats.put("operationStats", operationStats);
        
        // 获取成功率
        List<Object[]> successStats = auditLogRepository.getUserSuccessRate(userId, startTime, endTime);
        if (!successStats.isEmpty()) {
            Object[] successData = successStats.get(0);
            stats.put("totalOperations", successData[0]);
            stats.put("successfulOperations", successData[1]);
            stats.put("successRate", successData[2]);
        }
        
        // 获取模块分布
        List<Object[]> moduleStats = auditLogRepository.getModuleStatsByUser(userId, startTime, endTime);
        Map<String, Long> moduleOperationStats = moduleStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], 
                        arr -> (Long) arr[1]
                ));
        stats.put("moduleStats", moduleOperationStats);
        
        return stats;
    }

    @Override
    public Map<String, Object> analyzeErrorPatterns(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 错误操作分类统计
        List<Object[]> errorByModule = auditLogRepository.getErrorStatsByModule(startTime, endTime);
        Map<String, Long> moduleErrorStats = errorByModule.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], 
                        arr -> (Long) arr[1]
                ));
        analysis.put("errorByModule", moduleErrorStats);
        
        // 错误操作时间段分布
        List<Object[]> errorByHour = auditLogRepository.getErrorStatsByHour(startTime, endTime);
        Map<String, Long> hourErrorStats = errorByHour.stream()
                .collect(Collectors.toMap(
                        arr -> String.format("%02d:00", arr[0]), 
                        arr -> (Long) arr[1]
                ));
        analysis.put("errorByHour", hourErrorStats);
        
        // 常见错误类型
        List<Object[]> commonErrors = auditLogRepository.getCommonErrorOperations(startTime, endTime);
        Map<String, Long> commonErrorStats = commonErrors.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0] + ":" + arr[1], 
                        arr -> (Long) arr[2]
                ));
        analysis.put("commonErrors", commonErrorStats);
        
        return analysis;
    }

    @Override
    public Map<String, Long> getActiveUsersRanking(LocalDateTime startTime, LocalDateTime endTime, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> userStats = auditLogRepository.getTopActiveUsers(startTime, endTime, pageable);
        return userStats.stream()
                .collect(Collectors.toMap(
                        arr -> (String) arr[0], 
                        arr -> (Long) arr[1]
                ));
    }

    @Override
    @Transactional
    public int cleanupExpiredLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = auditLogRepository.deleteByCreateTimeBefore(cutoffDate);
        log.info("清理过期审计日志: 保留天数={}, 清理数量={}", retentionDays, deletedCount);
        return deletedCount;
    }

    @Override
    public String exportAuditLogs(String module, String operation, String username,
                                Boolean success, LocalDateTime startTime, LocalDateTime endTime) {
        // 获取审计日志数据
        Page<AuditLog> auditLogs = auditLogRepository.findByComplexConditions(
                module, operation, username, success, startTime, endTime, Pageable.unpaged());
        
        // 生成导出文件内容
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("时间,模块,操作,用户名,IP地址,状态,描述,实体类型,实体ID,请求方法,请求URL\n");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AuditLog log : auditLogs.getContent()) {
            csvContent.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    log.getCreateTime().format(formatter),
                    log.getModule(),
                    log.getOperation(),
                    log.getUsername(),
                    log.getUserIp(),
                    log.getSuccess() ? "成功" : "失败",
                    log.getDescription(),
                    log.getEntityType() != null ? log.getEntityType() : "",
                    log.getEntityId() != null ? log.getEntityId().toString() : "",
                    log.getRequestMethod() != null ? log.getRequestMethod() : "",
                    log.getRequestUrl() != null ? log.getRequestUrl() : ""
            ));
        }
        
        // 生成导出文件路径
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = "audit_logs_" + timestamp + ".csv";
        String filePath = "/tmp/export/" + fileName;
        
        // 在实际实现中，这里应该将文件内容写入到指定的路径
        // 这里简化处理，返回文件路径
        return filePath;
    }
}