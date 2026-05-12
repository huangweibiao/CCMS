package com.ccms.service.audit;

import com.ccms.audit.ApprovalAuditLog;
import com.ccms.repository.audit.ApprovalAuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 审批审计服务
 * 提供审计日志的记录、查询和分析功能
 */
@Service
public class ApprovalAuditService {

    @Autowired
    private ApprovalAuditLogRepository approvalAuditLogRepository;

    /**
     * 异步记录审批操作审计日志
     */
    @Async("approvalLogExecutor")
    public void logApprovalOperation(String operationType, String targetEntity, Long targetId,
                                   Long userId, String userName, String operationDesc,
                                   Map<String, Object> operationDetails) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog(operationType, targetEntity, targetId,
                    userId, userName, operationDesc);
            
            // 设置操作详情
            if (operationDetails != null) {
                auditLog.setOperationDetails(convertMapToJson(operationDetails));
            }
            
            // 设置用户IP和客户端信息（需要从请求上下文中获取）
            auditLog.setUserIp(getCurrentUserIp());
            auditLog.setClientInfo(getClientInfo());
            
            // 保存审计日志
            approvalAuditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            // 审计日志记录失败不影响主流程，但需要记录错误
            logAuditError("logApprovalOperation", e.getMessage(), operationType, targetEntity, targetId);
        }
    }

    /**
     * 记录带数据变更的审计日志
     */
    @Async("approvalLogExecutor")
    public void logDataChangeOperation(String operationType, String targetEntity, Long targetId,
                                     Long userId, String userName, String operationDesc,
                                     Object beforeData, Object afterData, Map<String, Object> details) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog(operationType, targetEntity, targetId,
                    userId, userName, operationDesc);
            
            // 设置数据快照
            if (beforeData != null) {
                auditLog.setBeforeData(convertObjectToJson(beforeData));
            }
            if (afterData != null) {
                auditLog.setAfterData(convertObjectToJson(afterData));
            }
            
            // 设置操作详情
            if (details != null) {
                auditLog.setOperationDetails(convertMapToJson(details));
            }
            
            // 设置执行时间（需要从调用方传入或计算）
            auditLog.setExecutionTime(calculateExecutionTime());
            
            approvalAuditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            logAuditError("logDataChangeOperation", e.getMessage(), operationType, targetEntity, targetId);
        }
    }

    /**
     * 记录失败的操作审计日志
     */
    @Async("approvalLogExecutor")
    public void logFailedOperation(String operationType, String targetEntity, Long targetId,
                                 Long userId, String userName, String operationDesc,
                                 String errorMessage, Map<String, Object> errorDetails) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog(operationType, targetEntity, targetId,
                    userId, userName, operationDesc);
            
            auditLog.setOperationResult("FAILED");
            auditLog.setErrorMessage(errorMessage);
            
            if (errorDetails != null) {
                auditLog.setOperationDetails(convertMapToJson(errorDetails));
            }
            
            approvalAuditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            logAuditError("logFailedOperation", e.getMessage(), operationType, targetEntity, targetId);
        }
    }

    /**
     * 查询审计日志
     */
    public Page<ApprovalAuditLog> queryAuditLogs(String operationType, String targetEntity,
                                               Long userId, String businessType,
                                               String operationResult,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               Pageable pageable) {
        return approvalAuditLogRepository.findAuditLogsByConditions(
                operationType, targetEntity, userId, businessType, operationResult,
                startTime, endTime, pageable);
    }

    /**
     * 获取实体的完整操作历史
     */
    public List<ApprovalAuditLog> getEntityAuditHistory(String targetEntity, Long targetId) {
        return approvalAuditLogRepository.findByTargetEntityAndTargetIdOrderByOperationTimeDesc(
                targetEntity, targetId);
    }

    /**
     * 生成审计报告
     */
    public Map<String, Object> generateAuditReport(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> report = new HashMap<>();
        
        // 统计操作类型分布
        List<Object[]> operationStats = approvalAuditLogRepository.countOperationsByType(startTime, endTime);
        report.put("operationTypeDistribution", operationStats);
        
        // 统计用户操作频率
        List<Object[]> userStats = approvalAuditLogRepository.countOperationsByUser(startTime, endTime);
        report.put("userOperationFrequency", userStats);
        
        // 统计操作成功率
        List<Object[]> successRates = approvalAuditLogRepository.calculateOperationSuccessRate(startTime, endTime);
        report.put("operationSuccessRates", successRates);
        
        // 统计每日操作量
        List<Object[]> dailyStats = approvalAuditLogRepository.countOperationsByDaily(startTime);
        report.put("dailyOperationStats", dailyStats);
        
        return report;
    }

    /**
     * 分析性能问题
     */
    public List<ApprovalAuditLog> analyzePerformanceIssues(Long executionTimeThreshold) {
        return approvalAuditLogRepository.findSlowOperations(executionTimeThreshold);
    }

    /**
     * 清理过期审计数据
     */
    public int cleanupOldAuditData(LocalDateTime beforeTime) {
        return approvalAuditLogRepository.deleteByOperationTimeBefore(beforeTime);
    }

    /**
     * 导出审计日志
     */
    public String exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime, String format) {
        List<ApprovalAuditLog> logs = approvalAuditLogRepository.findByOperationTimeBetween(startTime, endTime);
        
        if ("csv".equalsIgnoreCase(format)) {
            return exportToCsv(logs);
        } else if ("json".equalsIgnoreCase(format)) {
            return exportToJson(logs);
        } else {
            return exportToText(logs);
        }
    }

    // 私有工具方法
    
    private String convertMapToJson(Map<String, Object> map) {
        // 简化实现，实际应该使用Jackson等JSON库
        try {
            StringBuilder json = new StringBuilder("{");
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\",");
            }
            if (json.length() > 1) {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
    
    private String convertObjectToJson(Object obj) {
        // 简化实现
        return obj != null ? obj.toString() : "";
    }
    
    private String getCurrentUserIp() {
        // 从请求上下文中获取用户IP
        // 实际实现中应该从ServletRequestAttributes获取
        return "127.0.0.1"; // 默认值
    }
    
    private String getClientInfo() {
        // 获取客户端信息
        // 实际实现中应该从User-Agent头中解析
        return "Unknown Client";
    }
    
    private Long calculateExecutionTime() {
        // 计算执行时间（毫秒）
        // 实际实现中应该在方法开始时记录时间，结束时计算差值
        return 0L; // 默认值
    }
    
    private void logAuditError(String method, String error, String operationType, 
                             String targetEntity, Long targetId) {
        // 记录审计日志记录失败的错误
        // 实际应该使用日志框架记录
        System.err.println("Audit log failed: " + method + " - " + error + 
                         " [" + operationType + ", " + targetEntity + ", " + targetId + "]");
    }
    
    private String exportToCsv(List<ApprovalAuditLog> logs) {
        // 导出为CSV格式
        StringBuilder csv = new StringBuilder();
        csv.append("ID,OperationType,TargetEntity,TargetId,UserId,UserName,OperationTime,Result\n");
        
        for (ApprovalAuditLog log : logs) {
            csv.append(log.getId()).append(",")
               .append(log.getOperationType()).append(",")
               .append(log.getTargetEntity()).append(",")
               .append(log.getTargetId()).append(",")
               .append(log.getUserId()).append(",")
               .append(escapeCsv(log.getUserName())).append(",")
               .append(log.getOperationTime()).append(",")
               .append(log.getOperationResult()).append("\n");
        }
        
        return csv.toString();
    }
    
    private String exportToJson(List<ApprovalAuditLog> logs) {
        // 导出为JSON格式
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < logs.size(); i++) {
            ApprovalAuditLog log = logs.get(i);
            json.append("  {\n")
                .append("    \"id\": ").append(log.getId()).append(",\n")
                .append("    \"operationType\": \"").append(log.getOperationType()).append("\",\n")
                .append("    \"targetEntity\": \"").append(log.getTargetEntity()).append("\",\n")
                .append("    \"targetId\": ").append(log.getTargetId()).append(",\n")
                .append("    \"userId\": ").append(log.getUserId()).append(",\n")
                .append("    \"userName\": \"").append(escapeJson(log.getUserName())).append("\",\n")
                .append("    \"operationTime\": \"").append(log.getOperationTime()).append("\",\n")
                .append("    \"operationResult\": \"").append(log.getOperationResult()).append("\"\n")
                .append("  }").append(i < logs.size() - 1 ? "," : "").append("\n");
        }
        json.append("]");
        return json.toString();
    }
    
    private String exportToText(List<ApprovalAuditLog> logs) {
        // 导出为文本格式
        StringBuilder text = new StringBuilder();
        for (ApprovalAuditLog log : logs) {
            text.append("ID: ").append(log.getId()).append("\n")
                .append("操作类型: ").append(log.getOperationType()).append("\n")
                .append("目标实体: ").append(log.getTargetEntity()).append(" - ").append(log.getTargetId()).append("\n")
                .append("用户: ").append(log.getUserName()).append(" (" + log.getUserId() + ")\n")
                .append("时间: ").append(log.getOperationTime()).append("\n")
                .append("结果: ").append(log.getOperationResult()).append("\n\n");
        }
        return text.toString();
    }
    
    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.contains(",") ? "\"" + value.replace("\"", "\"\"") + "\"" : value;
    }
    
    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\t", "\\t");
    }
}