package com.ccms.service;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 基础审批服务类
 * 为具体的业务审批服务提供通用功能
 */
@Slf4j
@Service
public abstract class BaseApprovalService {

    protected final ApprovalFlowService approvalFlowService;
    
    public BaseApprovalService(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }
    
    /**
     * 获取业务类型
     */
    public abstract BusinessType getBusinessType();
    
    /**
     * 创建业务审批实例前的验证
     */
    protected abstract boolean validateBeforeCreate(ApprovalRequest request, Map<String, Object> context);
    
    /**
     * 审批完成后的回调处理
     */
    protected abstract void handleApprovalCompleted(ApprovalInstance instance, ApprovalStatus finalStatus);
    
    /**
     * 审批进行中的回调处理
     */
    protected abstract void handleApprovalProgress(ApprovalInstance instance, Map<String, Object> context);
    
    /**
     * 构建审批上下文数据
     */
    protected abstract Map<String, Object> buildApprovalContext(ApprovalRequest request);
    
    /**
     * 匹配流程配置前的检查
     */
    protected abstract boolean preMatchFlowConfig(ApprovalRequest request, Map<String, Object> context);
    
    /**
     * 获取默认流程配置（当没有匹配到流程时使用）
     */
    protected abstract ApprovalFlowConfig getDefaultFlowConfig();
    
    /**
     * 发起业务审批
     */
    public ApprovalInstance startBusinessApproval(ApprovalRequest request) {
        log.info("发起业务审批: 业务类型={}, 业务ID={}", getBusinessType(), request.getBusinessId());
        
        // 构建上下文数据
        Map<String, Object> context = buildApprovalContext(request);
        
        // 创建前验证
        if (!validateBeforeCreate(request, context)) {
            log.error("业务审批验证失败: 业务类型={}, 业务ID={}", getBusinessType(), request.getBusinessId());
            throw new RuntimeException("业务审批验证失败");
        }
        
        // 匹配流程配置前的检查
        if (!preMatchFlowConfig(request, context)) {
            log.warn("流程配置匹配前检查失败: 业务类型={}", getBusinessType());
        }
        
        // 匹配流程配置
        ApprovalFlowConfig flowConfig = matchBusinessFlowConfig(request, context);
        if (flowConfig == null) {
            log.warn("未找到匹配的流程配置，使用默认配置: 业务类型={}", getBusinessType());
            flowConfig = getDefaultFlowConfig();
        }
        
        // 创建审批实例
        ApprovalInstance instance = approvalFlowService.startApprovalInstance(
                request.getBusinessType(),
                request.getBusinessId(),
                request.getApplicantId(),
                request.getTitle(),
                context
        );
        
        // 调用进度回调
        handleApprovalProgress(instance, context);
        
        log.info("业务审批发起成功: 实例ID={}, 业务类型={}", instance.getId(), getBusinessType());
        return instance;
    }
    
    /**
     * 处理业务审批结果
     */
    public void handleBusinessApprovalResult(Long instanceId, ApprovalAction action, String remarks) {
        ApprovalInstance instance = approvalFlowService.getApprovalInstance(instanceId);
        
        if (instance == null) {
            log.error("审批实例不存在: ID={}", instanceId);
            throw new RuntimeException("审批实例不存在");
        }
        
        if (instance.getStatus().isFinalStatus()) {
            log.warn("审批实例已完成，不可操作: ID={}, 状态={}", instanceId, instance.getStatus());
            throw new RuntimeException("审批实例已完成");
        }
        
        // 执行审批操作
        ApprovalInstance updatedInstance = null;
        switch (action) {
            case APPROVE:
                updatedInstance = approvalFlowService.approve(instanceId, getCurrentUserId(), remarks);
                break;
            case REJECT:
                updatedInstance = approvalFlowService.reject(instanceId, getCurrentUserId(), remarks);
                break;
            case TRANSFER:
                // 转审需要目标审批人ID
                throw new UnsupportedOperationException("转审操作需要目标审批人ID");
            case SKIP:
                updatedInstance = approvalFlowService.skip(instanceId, getCurrentUserId(), remarks);
                break;
            case CANCEL:
                updatedInstance = approvalFlowService.cancel(instanceId, getCurrentUserId(), remarks);
                break;
            default:
                throw new IllegalArgumentException("未知的审批操作: " + action);
        }
        
        // 检查是否完成
        if (updatedInstance.getStatus().isFinalStatus()) {
            handleApprovalCompleted(updatedInstance, updatedInstance.getStatus());
            log.info("业务审批完成: 实例ID={}, 最终状态={}", instanceId, updatedInstance.getStatus());
        } else {
            // 处理进度回调
            handleApprovalProgress(updatedInstance, null);
            log.info("业务审批继续处理: 实例ID={}, 当前节点={}", instanceId, updatedInstance.getCurrentNode());
        }
    }
    
    /**
     * 获取业务审批详情
     */
    public ApprovalInstance getBusinessApprovalDetail(String businessId) {
        return approvalFlowService.getApprovalInstanceByBusinessId(businessId, getBusinessType());
    }
    
    /**
     * 取消业务审批
     */
    public void cancelBusinessApproval(String businessId, String remarks) {
        ApprovalInstance instance = getBusinessApprovalDetail(businessId);
        if (instance != null && !instance.getStatus().isFinalStatus()) {
            approvalFlowService.cancel(instance.getId(), instance.getApplicantId(), remarks);
            log.info("业务审批取消: 业务ID={}, 业务类型={}", businessId, getBusinessType());
        }
    }
    
    /**
     * 检查业务审批状态
     */
    public ApprovalStatusEnum checkBusinessApprovalStatus(String businessId) {
        ApprovalInstance instance = getBusinessApprovalDetail(businessId);
        return instance != null ? instance.getStatus() : null;
    }
    
    /**
     * 匹配业务流程配置
     */
    protected ApprovalFlowConfig matchBusinessFlowConfig(ApprovalRequest request, Map<String, Object> context) {
        return approvalFlowService.matchApprovalFlowConfig(
                request.getBusinessType(),
                request.getAmount(),
                null
        );
    }
    
    /**
     * 获取当前用户ID（需要根据认证系统实现）
     */
    protected abstract Long getCurrentUserId();
    
    /**
     * 构建成功响应
     */
    protected Map<String, Object> buildSuccessResponse(ApprovalInstance instance) {
        return Map.of(
                "success", true,
                "instanceId", instance.getId(),
                "status", instance.getStatus(),
                "currentNode", instance.getCurrentNode(),
                "message", "操作成功"
        );
    }
    
    /**
     * 构建失败响应
     */
    protected Map<String, Object> buildFailureResponse(String errorMessage) {
        return Map.of(
                "success", false,
                "error", errorMessage
        );
    }
}