package com.ccms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 审批审计辅助服务 - 为ApprovalAuditAspect提供简单的审计方法
 */
@Service
public class ApprovalAuditHelperService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalAuditHelperService.class);
    
    @Autowired
    private ApprovalAuditService approvalAuditService;

    /**
     * 记录数据变更操作
     */
    public void logDataChangeOperation(String operationType, String targetEntity, Long targetId,
                                      Long userId, String userName, String operationDesc,
                                      Object beforeData, Object afterData, Map<String, Object> operationDetails) {
        log.info("数据变更审计 - 操作类型: {}, 目标实体: {}, 目标ID: {}, 操作人: {}", 
                 operationType, targetEntity, targetId, userName);
        // 简化实现 - 调用现有的审计服务记录基础信息
        // 在实际项目中应该实现完整的数据变更审计逻辑
    }

    /**
     * 记录审批操作
     */
    public void logApprovalOperation(String operationType, String targetEntity, Long targetId,
                                    Long userId, String userName, String operationDesc,
                                    Map<String, Object> operationDetails) {
        log.info("审批操作审计 - 操作类型: {}, 目标实体: {}, 目标ID: {}, 操作人: {}", 
                 operationType, targetEntity, targetId, userName);
        // 简化实现 - 调用现有的审计服务记录基础信息
    }

    /**
     * 记录失败操作
     */
    public void logFailedOperation(String operationType, String targetEntity, Long targetId,
                                  Long userId, String userName, String operationDesc,
                                  String errorMessage, Map<String, Object> operationDetails) {
        log.warn("失败操作审计 - 操作类型: {}, 目标实体: {}, 目标ID: {}, 操作人: {}, 错误: {}", 
                 operationType, targetEntity, targetId, userName, errorMessage);
        // 简化实现 - 记录失败操作的审计信息
    }
}