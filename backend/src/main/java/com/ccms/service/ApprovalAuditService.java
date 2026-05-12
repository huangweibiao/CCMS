package com.ccms.service;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalAuditLog;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.AuditActionType;
import com.ccms.repository.approval.ApprovalAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批审计服务
 * 记录所有审批操作的历史记录
 */
@Slf4j
@Service
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class ApprovalAuditService {

    private final ApprovalAuditLogRepository auditLogRepository;

    /**
     * 记录审批实例创建审计
     */
    public void logInstanceCreation(ApprovalInstance instance, Long operatorId, String operatorName) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog();
            auditLog.setInstanceId(instance.getId());
            auditLog.setBusinessType(instance.getBusinessType().getCode());
            auditLog.setBusinessId(instance.getBusinessId());
            auditLog.setActionType(AuditActionType.CREATE_INSTANCE);
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setDescription("创建审批实例: " + instance.getTitle());
            auditLog.setOldStatus(null);
            auditLog.setNewStatus(instance.getStatus());
            auditLog.setLogTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentClientIp());
            auditLog.setUserAgent(getCurrentUserAgent());
            
            auditLogRepository.save(auditLog);
            log.debug("记录审批实例创建审计: 实例ID={}, 业务类型={}", instance.getId(), instance.getBusinessType());
            
        } catch (Exception e) {
            log.error("记录审批实例创建审计失败: 实例ID={}, 错误={}", instance.getId(), e.getMessage(), e);
        }
    }

    /**
     * 记录审批操作审计
     */
    public void logApprovalAction(ApprovalInstance instance, ApprovalAction action, 
                                  Long operatorId, String operatorName, String remarks) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog();
            auditLog.setInstanceId(instance.getId());
            auditLog.setBusinessType(instance.getBusinessType().getCode());
            auditLog.setBusinessId(instance.getBusinessId());
            auditLog.setActionType(getActionTypeFromApprovalAction(action));
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setDescription(getActionDescription(action, remarks));
            auditLog.setOldStatus(null); // 在状态变化时记录
            auditLog.setNewStatus(instance.getStatus());
            auditLog.setLogTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentClientIp());
            auditLog.setUserAgent(getCurrentUserAgent());
            auditLog.setRemarks(remarks);
            
            auditLogRepository.save(auditLog);
            log.debug("记录审批操作审计: 实例ID={}, 操作={}, 操作人={}", 
                     instance.getId(), action, operatorName);
            
        } catch (Exception e) {
            log.error("记录审批操作审计失败: 实例ID={}, 操作={}, 错误={}", 
                     instance.getId(), action, e.getMessage(), e);
        }
    }

    /**
     * 记录审批状态变更审计
     */
    public void logStatusChange(ApprovalInstance instance, ApprovalStatus oldStatus, 
                                ApprovalStatus newStatus, Long operatorId, String operatorName) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog();
            auditLog.setInstanceId(instance.getId());
            auditLog.setBusinessType(instance.getBusinessType().getCode());
            auditLog.setBusinessId(instance.getBusinessId());
            auditLog.setActionType(AuditActionType.STATUS_CHANGE);
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setDescription("状态变更: " + oldStatus + " -> " + newStatus);
            auditLog.setOldStatus(oldStatus);
            auditLog.setNewStatus(newStatus);
            auditLog.setLogTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentClientIp());
            auditLog.setUserAgent(getCurrentUserAgent());
            
            auditLogRepository.save(auditLog);
            log.debug("记录审批状态变更审计: 实例ID={}, 状态变更 {} -> {}", 
                     instance.getId(), oldStatus, newStatus);
            
        } catch (Exception e) {
            log.error("记录审批状态变更审计失败: 实例ID={}, 错误={}", instance.getId(), e.getMessage(), e);
        }
    }

    /**
     * 记录审批记录创建审计
     */
    public void logRecordCreation(ApprovalRecord record, Long operatorId, String operatorName) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog();
            auditLog.setInstanceId(record.getInstanceId());
            auditLog.setRecordId(record.getId());
            auditLog.setBusinessType(record.getBusinessType());
            auditLog.setBusinessId(record.getBusinessId().toString());
            auditLog.setActionType(AuditActionType.CREATE_RECORD);
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setDescription("创建审批记录: " + record.getApprovalNode());
            auditLog.setLogTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentClientIp());
            auditLog.setUserAgent(getCurrentUserAgent());
            
            auditLogRepository.save(auditLog);
            log.debug("记录审批记录创建审计: 记录ID={}, 实例ID={}", record.getId(), record.getInstanceId());
            
        } catch (Exception e) {
            log.error("记录审批记录创建审计失败: 记录ID={}, 错误={}", record.getId(), e.getMessage(), e);
        }
    }

    /**
     * 记录流程配置变更审计
     */
    public void logFlowConfigChange(Long configId, String operation, 
                                    Long operatorId, String operatorName, String changeDetails) {
        try {
            ApprovalAuditLog auditLog = new ApprovalAuditLog();
            auditLog.setConfigId(configId);
            auditLog.setActionType(AuditActionType.FLOW_CONFIG_CHANGE);
            auditLog.setOperatorId(operatorId);
            auditLog.setOperatorName(operatorName);
            auditLog.setDescription("流程配置变更: " + operation + " - " + changeDetails);
            auditLog.setLogTime(LocalDateTime.now());
            auditLog.setIpAddress(getCurrentClientIp());
            auditLog.setUserAgent(getCurrentUserAgent());
            
            auditLogRepository.save(auditLog);
            log.debug("记录流程配置变更审计: 配置ID={}, 操作={}", configId, operation);
            
        } catch (Exception e) {
            log.error("记录流程配置变更审计失败: 配置ID={}, 错误={}", configId, e.getMessage(), e);
        }
    }

    /**
     * 查询审批实例的审计日志
     */
    @Transactional(readOnly = true)
    public List<ApprovalAuditLog> getAuditLogsByInstanceId(Long instanceId) {
        return auditLogRepository.findByInstanceIdOrderByLogTimeDesc(instanceId);
    }

    /**
     * 查询业务数据的审计日志
     */
    @Transactional(readOnly = true)
    public List<ApprovalAuditLog> getAuditLogsByBusiness(String businessType, String businessId) {
        return auditLogRepository.findByBusinessTypeAndBusinessIdOrderByLogTimeDesc(
                businessType, businessId);
    }

    /**
     * 查询操作者的审计日志
     */
    @Transactional(readOnly = true)
    public List<ApprovalAuditLog> getAuditLogsByOperator(String operatorName, LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null && endDate == null) {
            return auditLogRepository.findByOperatorNameOrderByLogTimeDesc(operatorName);
        } else if (endDate == null) {
            return auditLogRepository.findByOperatorNameAndLogTimeAfterOrderByLogTimeDesc(operatorName, startDate);
        } else if (startDate == null) {
            return auditLogRepository.findByOperatorNameAndLogTimeBeforeOrderByLogTimeDesc(operatorName, endDate);
        } else {
            return auditLogRepository.findByOperatorNameAndLogTimeBetweenOrderByLogTimeDesc(
                    operatorName, startDate, endDate);
        }
    }

    /**
     * 检查最近的操作频率（防止恶意操作）
     */
    @Transactional(readOnly = true)
    public boolean isFrequentOperation(Long operatorId, AuditActionType actionType, int maxOperations, long timeWindowMinutes) {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(timeWindowMinutes);
        long operationCount = auditLogRepository.countByOperatorIdAndActionTypeAndLogTimeAfter(
                operatorId, actionType, startTime);
        
        return operationCount >= maxOperations;
    }

    /**
     * 获取操作统计信息
     */
    @Transactional(readOnly = true)
    public Map<AuditActionType, Long> getOperationStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.countOperationsByType(startDate, endDate);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        record -> (AuditActionType) record[0],
                        record -> (Long) record[1]
                ));
    }

    /**
     * 私有方法：将审批动作映射到审计动作类型
     */
    private AuditActionType getActionTypeFromApprovalAction(ApprovalAction action) {
        switch (action) {
            case APPROVE:
                return AuditActionType.APPROVE;
            case REJECT:
                return AuditActionType.REJECT;
            case TRANSFER:
                return AuditActionType.TRANSFER;
            case CANCEL:
                return AuditActionType.CANCEL;
            case SKIP:
                return AuditActionType.SKIP_NODE;
            default:
                return AuditActionType.OTHER;
        }
    }

    /**
     * 私有方法：生成操作描述
     */
    private String getActionDescription(ApprovalAction action, String remarks) {
        String actionText = getActionText(action);
        if (remarks != null && !remarks.trim().isEmpty()) {
            return actionText + " - " + remarks;
        }
        return actionText;
    }

    /**
     * 私有方法：获取操作文本
     */
    private String getActionText(ApprovalAction action) {
        switch (action) {
            case APPROVE:
                return "审批通过";
            case REJECT:
                return "审批驳回";
            case TRANSFER:
                return "转审";
            case CANCEL:
                return "取消审批";
            case SKIP:
                return "跳过节点";
            default:
                return "其他操作";
        }
    }

    /**
     * 私有方法：获取当前客户端IP
     */
    private String getCurrentClientIp() {
        // 实际项目中可以从请求上下文中获取
        // 这里返回默认值
        return "127.0.0.1";
    }

    /**
     * 私有方法：获取当前用户代理
     */
    private String getCurrentUserAgent() {
        // 实际项目中可以从请求上下文中获取
        // 这里返回默认值
        return "Unknown";
    }
}

/**
 * 审计动作类型枚举
 */
enum AuditActionType {
    CREATE_INSTANCE,      // 创建审批实例
    CREATE_RECORD,        // 创建审批记录
    APPROVE,              // 审批通过
    REJECT,               // 审批驳回
    TRANSFER,             // 转审
    CANCEL,               // 取消审批
    SKIP_NODE,            // 跳过节点
    STATUS_CHANGE,        // 状态变更
    FLOW_CONFIG_CHANGE,   // 流程配置变更
    OTHER                 // 其他操作
}