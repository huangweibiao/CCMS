package com.ccms.utils;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.TransitionRule;

import java.math.BigDecimal;
import java.util.List;

/**
 * 审批验证工具类
 * 提供静态方法用于审批流程的各种验证
 */
public final class ApprovalValidator {
    
    // 私有构造函数防止实例化
    private ApprovalValidator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 验证审批请求的完整性
     */
    public static void validateApprovalRequest(ApprovalRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("审批请求不能为空");
        }
        
        if (request.getBusinessType() == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        
        if (request.getBusinessId() == null || request.getBusinessId().trim().isEmpty()) {
            throw new IllegalArgumentException("业务ID不能为空");
        }
        
        if (request.getApplicantId() == null) {
            throw new IllegalArgumentException("申请人ID不能为空");
        }
        
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("审批标题不能为空");
        }
        
        // 验证金额格式
        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数");
        }
    }

    /**
     * 验证审批流程配置的完整性
     */
    public static void validateFlowConfig(ApprovalFlowConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("流程配置不能为空");
        }
        
        if (config.getBusinessType() == null || config.getBusinessType().trim().isEmpty()) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        
        if (config.getFlowName() == null || config.getFlowName().trim().isEmpty()) {
            throw new IllegalArgumentException("流程名称不能为空");
        }
        
        if (config.getMinAmount() != null && config.getMaxAmount() != null 
                && config.getMinAmount().compareTo(config.getMaxAmount()) > 0) {
            throw new IllegalArgumentException("最小金额不能大于最大金额");
        }
    }

    /**
     * 验证审批节点的完整性
     */
    public static void validateApprovalNode(ApprovalNode node) {
        if (node == null) {
            throw new IllegalArgumentException("审批节点不能为空");
        }
        
        if (node.getNodeName() == null || node.getNodeName().trim().isEmpty()) {
            throw new IllegalArgumentException("节点名称不能为空");
        }
        
        if (node.getApproverType() == null) {
            throw new IllegalArgumentException("审批人类型不能为空");
        }
        
        if (node.getStepNumber() == null || node.getStepNumber() < 0) {
            throw new IllegalArgumentException("节点顺序不能为空且必须大于等于0");
        }
    }

    /**
     * 验证是否可以执行审批操作
     */
    public static void validateApprovalAction(ApprovalInstance instance, Long approverId, ApprovalAction action) {
        if (instance == null) {
            throw new IllegalArgumentException("审批实例不能为空");
        }
        
        if (approverId == null) {
            throw new IllegalArgumentException("审批人ID不能为空");
        }
        
        if (action == null) {
            throw new IllegalArgumentException("审批操作不能为空");
        }
        
        // 检查实例状态
        if (ApprovalStatusEnum.isFinalStatus(instance.getStatus())) {
            throw new IllegalStateException("审批实例已完成，无法执行操作");
        }
        
        // 检查是否是当前审批人
        if (instance.getCurrentApproverId() != null && !instance.getCurrentApproverId().equals(approverId)) {
            throw new SecurityException("当前用户不是审批节点的审批人");
        }
        
        // 检查操作是否支持
        switch (action) {
            case APPROVE:
            case REJECT:
            case TRANSFER:
            case SKIP:
            case CANCEL:
                // 支持的操作
                break;
            default:
                throw new IllegalArgumentException("不支持的审批操作: " + action);
        }
    }

    /**
     * 验证业务类型是否支持
     */
    public static void validateBusinessType(BusinessTypeEnum businessType) {
        if (businessType == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        
        // 检查是否在支持的范围内
        List<BusinessTypeEnum> supportedTypes = List.of(
                BusinessTypeEnum.EXPENSE_REIMBURSE,
                BusinessTypeEnum.LOAN,
                BusinessTypeEnum.EXPENSE_APPLY
        );
        
        if (!supportedTypes.contains(businessType)) {
            throw new IllegalArgumentException("不支持的业务类型: " + businessType);
        }
    }

    /**
     * 验证审批记录
     */
    public static void validateApprovalRecord(ApprovalRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("审批记录不能为空");
        }
        
        if (record.getInstanceId() == null) {
            throw new IllegalArgumentException("审批实例ID不能为空");
        }
        
        if (record.getApproverId() == null) {
            throw new IllegalArgumentException("审批人ID不能为空");
        }
        
        if (record.getApprovalAction() == null) {
            throw new IllegalArgumentException("审批操作不能为空");
        }
        
        if (record.getApprovalResult() == null) {
            throw new IllegalArgumentException("审批结果不能为空");
        }
    }

    /**
     * 验证金额范围
     */
    public static boolean isAmountInRange(BigDecimal amount, BigDecimal minAmount, BigDecimal maxAmount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        
        boolean valid = true;
        
        if (minAmount != null) {
            valid = valid && amount.compareTo(minAmount) >= 0;
        }
        
        if (maxAmount != null) {
            valid = valid && amount.compareTo(maxAmount) <= 0;
        }
        
        return valid;
    }

    /**
     * 验证节点顺序的连续性
     */
    public static void validateNodeOrder(List<ApprovalNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("节点列表不能为空");
        }
        
        // 检查节点顺序是否连续
        for (int i = 0; i < nodes.size(); i++) {
            ApprovalNode node = nodes.get(i);
            if (node.getStepNumber() != i + 1) {
                throw new IllegalArgumentException("节点顺序不连续，期望: " + (i + 1) + ", 实际: " + node.getStepNumber());
            }
        }
    }

    /**
     * 验证状态转换是否合法
     */
    public static boolean isValidStatusTransition(ApprovalStatus fromStatus, ApprovalStatus toStatus, ApprovalAction action) {
        if (fromStatus == null || toStatus == null || action == null) {
            return false;
        }
        
        // 使用统一的TransitionRule检查转换是否允许
        return TransitionRule.isTransitionAllowed(fromStatus, toStatus, action);
    }
    
    /**
     * 验证状态转换并抛出异常
     */
    public static void validateStatusTransition(ApprovalStatus fromStatus, ApprovalStatus toStatus, ApprovalAction action) {
        if (!isValidStatusTransition(fromStatus, toStatus, action)) {
            throw new IllegalArgumentException(String.format(
                "状态转换不合法：从 %s 到 %s，操作 %s 不被允许",
                fromStatus.getDescription(), toStatus.getDescription(), action.name()));
        }
    }
    
    /**
     * 获取指定状态允许的所有操作
     */
    public static List<ApprovalAction> getAllowedActions(ApprovalStatus status) {
        return TransitionRule.getAllowedActions(status);
    }
    
    /**
     * 获取指定状态可以转换的所有目标状态
     */
    public static List<ApprovalStatus> getTargetStatuses(ApprovalStatus status) {
        return TransitionRule.getTargetStatuses(status);
    }
    
    /**
     * 检查操作是否在当前状态下允许
     */
    public static boolean isActionAllowed(ApprovalStatus status, ApprovalAction action) {
        List<ApprovalAction> allowedActions = getAllowedActions(status);
        return allowedActions.contains(action);
    }
}