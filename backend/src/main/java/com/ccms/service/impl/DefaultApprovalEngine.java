package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.ApproverType;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalNodeRepository;
import com.ccms.service.ApprovalEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DefaultApprovalEngine implements ApprovalEngine {

    private static final Logger log = LoggerFactory.getLogger(DefaultApprovalEngine.class);
    
    // 审批者类型常量定义
    private static final String USER = "USER";
    private static final String ROLE = "ROLE";
    private static final String DEPT = "DEPT";
    private static final String POSITION = "POSITION";
    private static final String SELF = "SELF";
    
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalNodeRepository nodeRepository;

    public DefaultApprovalEngine(ApprovalInstanceRepository instanceRepository, ApprovalNodeRepository nodeRepository) {
        this.instanceRepository = instanceRepository;
        this.nodeRepository = nodeRepository;
    }
    
    private final Map<String, ApprovalEngine.NodeProcessor> nodeProcessors = new ConcurrentHashMap<>();
    private final Map<ApprovalStatus, List<ApprovalAction>> transitionRules = createTransitionRules();
    
    private Long totalExecutions = 0L;
    private Long successfulExecutions = 0L;
    private Long failedExecutions = 0L;
    private Long timeoutExecutions = 0L;
    private Long totalExecutionTime = 0L;

    @Override
    public void initialize() {
        log.info("审批流程引擎初始化完成");
        registerDefaultProcessors();
    }

    @Override
    @Transactional
    public void executeNode(ApprovalInstance instance, ApprovalNode node, Map<String, Object> context) {
        long startTime = System.currentTimeMillis();
        totalExecutions++;
        
        try {
            log.info("执行审批节点: 实例ID={}, 节点ID={}, 审批人ID={}", 
                    instance.getId(), node.getId(), node.getApproverId());
            
            // 获取节点处理器
            ApprovalEngine.NodeProcessor processor = getNodeProcessor(node);
            
            // 执行节点处理
            ApprovalEngine.NodeProcessor.NodeResult result = processor.process(node, context);
            
            if (result.isSuccess()) {
                successfulExecutions++;
                log.info("节点执行成功: {}", result.getMessage());
                
                // 更新上下文
                if (result.getOutput() != null) {
                    context.putAll(result.getOutput());
                }
            } else {
                failedExecutions++;
                log.error("节点执行失败: {}", result.getMessage());
                throw new RuntimeException(result.getMessage());
            }
            
            long executionTime = System.currentTimeMillis() - startTime;
            totalExecutionTime += executionTime;
            
        } catch (Exception e) {
            failedExecutions++;
            log.error("执行审批节点异常: 实例ID={}, 节点ID={}", instance.getId(), node.getId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public ApprovalInstance handleApprovalResult(ApprovalInstance instance, ApprovalAction action, 
                                               ApprovalNode currentNode, Map<String, Object> context) {
        log.info("处理审批结果: 实例ID={}, 操作={}, 当前节点={}", instance.getId(), action, currentNode.getStepNumber());
        
        // 验证状态转换是否允许
        if (!isTransitionAllowed(convertIntegerToApprovalStatus(instance.getStatus()), action)) {
            throw new IllegalStateException("不允许的状态转换: " + instance.getStatus() + " -> " + action);
        }
        
        // 更新节点状态
        currentNode.setProcessTime(LocalDateTime.now());
        // 将枚举名称转换为状态码
        currentNode.setStatus(convertActionToStatusCode(action));
        nodeRepository.save(currentNode);
        
        // 更新实例状态
        switch (action) {
            case APPROVE:
                return handleApproveAction(instance, context);
            case REJECT:
                return handleRejectAction(instance, context);
            case TRANSFER:
                return handleTransferAction(instance, currentNode, context);
            case SKIP:
                return handleSkipAction(instance, context);
            case CANCEL:
                return handleCancelAction(instance, context);
            default:
                throw new IllegalArgumentException("未知的审批操作: " + action);
        }
    }

    @Override
    @Transactional
    public ApprovalInstance proceedToNextNode(ApprovalInstance instance, Map<String, Object> context) {
        log.info("流转到下一节点: 实例ID={}, 当前节点={}", instance.getId(), instance.getCurrentNode());
        
        // 获取下一节点
        ApprovalNode nextNode = getNextNode(instance);
        if (nextNode == null) {
            log.info("审批流程已完成: 实例ID={}", instance.getId());
            instance.setStatus(ApprovalStatus.APPROVED.ordinal());
            instance.setFinishTime(LocalDateTime.now());
        } else {
            log.info("流转到节点 {}: 审批人ID={}", nextNode.getStepNumber(), nextNode.getApproverId());
            instance.setCurrentNode(String.valueOf(nextNode.getStepNumber()));
            instance.setProcessedNodes(instance.getProcessedNodes() + 1);
        }
        
        return instanceRepository.save(instance);
    }

    @Override
    @Transactional
    public ApprovalInstance terminateProcess(ApprovalInstance instance, ApprovalStatus finalStatus,
                                           String reason, Map<String, Object> context) {
        log.info("终止审批流程: 实例ID={}, 最终状态={}, 原因={}", instance.getId(), finalStatus, reason);
        
        instance.setStatus(finalStatus.ordinal());
        instance.setFinishTime(LocalDateTime.now());
        instance.setRemarks(reason);
        
        return instanceRepository.save(instance);
    }

    @Override
    public boolean validateFlowConfig(ApprovalFlowConfig flowConfig) {
        if (flowConfig == null) {
            return false;
        }
        
        // 验证基本字段
        if (flowConfig.getFlowCode() == null || flowConfig.getFlowCode().trim().isEmpty()) {
            return false;
        }
        
        if (flowConfig.getFlowName() == null || flowConfig.getFlowName().trim().isEmpty()) {
            return false;
        }
        
        if (flowConfig.getBusinessType() == null) {
            return false;
        }
        
        // 验证节点配置
        List<ApprovalNode> nodes = nodeRepository.findByFlowConfigIdOrderByStepNumber(flowConfig.getId());
        if (nodes.isEmpty()) {
            log.warn("流程配置 {} 没有定义任何审批节点", flowConfig.getFlowCode());
            return false;
        }
        
        // 检查节点顺序是否正确
        Set<Integer> stepNumbers = new HashSet<>();
        for (ApprovalNode node : nodes) {
            if (!stepNumbers.add(node.getStepNumber())) {
                log.error("流程配置 {} 存在重复的步骤编号: {}", flowConfig.getFlowCode(), node.getStepNumber());
                return false;
            }
        }
        
        // 验证审批人配置
        for (ApprovalNode node : nodes) {
            if (node.getApproverType() == null || node.getApproverId() == null) {
                log.error("节点 {} 审批人配置不完整", node.getId());
                return false;
            }
        }
        
        return true;
    }

    @Override
    public List<ApprovalNode> getExecutableNodes(ApprovalInstance instance) {
        List<ApprovalNode> allNodes = nodeRepository.findByFlowConfigIdOrderByStepNumberAsc(instance.getFlowId());
        
        return allNodes.stream()
                .filter(node -> node.getStepNumber() == Integer.parseInt(instance.getCurrentNode()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean canProceed(ApprovalInstance instance) {
        // 检查实例是否已完成或终止
        if (isFinalStatus(instance.getStatus())) {
            return false;
        }
        
        // 检查是否有下一个节点
        ApprovalNode nextNode = getNextNode(instance);
        return nextNode != null;
    }

    @Override
    public ApprovalStatus getCurrentStatus(ApprovalInstance instance) {
        // 将Integer状态转换为ApprovalStatus枚举
        return convertIntegerToApprovalStatus(instance.getStatus());
    }

    @Override
    @Transactional
    public List<ApprovalInstance> handleTimeoutInstances() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(24); // 24小时超时
        List<ApprovalInstance> timeoutInstances = new ArrayList<>();
        
        // 查找超时的运行中实例
        List<ApprovalInstance> runningInstances = instanceRepository.findByStatus(ApprovalStatusEnum.RUNNING);
        
        for (ApprovalInstance instance : runningInstances) {
            if (instance.getCreateTime().isBefore(timeoutThreshold)) {
                // 标记为超时
                instance.setStatus(ApprovalStatus.TIMEOUT.ordinal());
                instance.setFinishTime(LocalDateTime.now());
                instance.setRemarks("审批流程超时");
                instanceRepository.save(instance);
                timeoutInstances.add(instance);
                timeoutExecutions++;
                
                log.warn("处理超时审批实例: ID={}, 创建时间={}", instance.getId(), instance.getCreateTime());
            }
        }
        
        return timeoutInstances;
    }

    @Override
    public ApprovalEngine.EngineStatistics getEngineStatistics() {
        Double averageExecutionTime = totalExecutions > 0 ? totalExecutionTime.doubleValue() / totalExecutions : 0.0;
        return new ApprovalEngine.EngineStatistics(totalExecutions, successfulExecutions, failedExecutions, 
                                   timeoutExecutions, averageExecutionTime);
    }

    @Override
    public void registerNodeProcessor(String nodeType, ApprovalEngine.NodeProcessor processor) {
        nodeProcessors.put(nodeType, processor);
        log.info("注册节点处理器: {}", nodeType);
    }

    // 处理取消操作的私有方法
    private ApprovalInstance handleCancelAction(ApprovalInstance instance, Map<String, Object> context) {
        instance.setStatus(ApprovalStatus.CANCELED.ordinal());
        instance.setFinishTime(LocalDateTime.now());
        log.info("审批流程取消: 实例ID={}", instance.getId());
        return instanceRepository.save(instance);
    }

    // 私有辅助方法
    private void registerDefaultProcessors() {
        // 注册默认的用户审批处理器
        registerNodeProcessor("USER_APPROVAL", new UserApprovalProcessor());
        
        // 注册其他默认处理器
        registerNodeProcessor("ROLE_APPROVAL", new RoleApprovalProcessor());
        registerNodeProcessor("DEPARTMENT_APPROVAL", new DepartmentApprovalProcessor());
        
        log.info("默认节点处理器注册完成");
    }

    private ApprovalEngine.NodeProcessor getNodeProcessor(ApprovalNode node) {
        String processorType = determineProcessorType(node);
        ApprovalEngine.NodeProcessor processor = nodeProcessors.get(processorType);
        
        if (processor == null) {
            throw new IllegalArgumentException("未找到对应的节点处理器: " + processorType);
        }
        
        return processor;
    }

    private String determineProcessorType(ApprovalNode node) {
        switch (node.getApproverType()) {
            case USER:
                return "USER_APPROVAL";
            case ROLE:
                return "ROLE_APPROVAL";
            case DEPT:
                return "DEPARTMENT_APPROVAL";
            case POSITION:
                return "POSITION_APPROVAL";
            case SELF:
                return "SELF_APPROVAL";
            default:
                return "USER_APPROVAL"; // 默认处理器
        }
    }

    private ApprovalNode getNextNode(ApprovalInstance instance) {
        // 需要从currentNode获取节点序号，这里假设currentNode是节点序号的字符串表示
        if (instance.getCurrentNode() == null || instance.getCurrentNode().isEmpty()) {
            // 如果没有当前节点，则从第一个节点开始
            return nodeRepository.findByFlowConfigIdAndStepNumber(instance.getFlowId(), 1)
                    .orElse(null);
        }
        
        try {
            Integer currentStep = Integer.parseInt(instance.getCurrentNode());
            return nodeRepository.findByFlowConfigIdAndStepNumber(instance.getFlowId(), currentStep + 1)
                    .orElse(null);
        } catch (NumberFormatException e) {
            // 如果currentNode不是数字，返回null
            return null;
        }
    }

    private boolean isTransitionAllowed(ApprovalStatus currentStatus, ApprovalAction action) {
        List<ApprovalAction> allowedActions = transitionRules.get(currentStatus);
        return allowedActions != null && allowedActions.contains(action);
    }

    private Map<ApprovalStatus, List<ApprovalAction>> createTransitionRules() {
        Map<ApprovalStatus, List<ApprovalAction>> rules = new EnumMap<>(ApprovalStatus.class);
        
        // 运行中状态允许的操作
        rules.put(ApprovalStatus.RUNNING, Arrays.asList(
            ApprovalAction.APPROVE,
            ApprovalAction.REJECT,
            ApprovalAction.TRANSFER,
            ApprovalAction.SKIP,
            ApprovalAction.CANCEL
        ));
        
        // 其他状态的转换规则
        // ... 可以根据需要添加更多规则
        
        return rules;
    }

    private Integer convertActionToStatusCode(ApprovalAction action) {
        switch (action) {
            case APPROVE: return 1;
            case REJECT: return 2;
            case TRANSFER: return 3;
            case SKIP: return 4;
            case CANCEL: return 5;
            default: return 0;
        }
    }

    private boolean isFinalStatus(Integer status) {
        if (status == null) {
            return false;
        }
        ApprovalStatus approvalStatus = convertIntegerToApprovalStatus(status);
        return approvalStatus == ApprovalStatus.APPROVED || 
               approvalStatus == ApprovalStatus.REJECTED || 
               approvalStatus == ApprovalStatus.CANCELED || 
               approvalStatus == ApprovalStatus.TIMEOUT;
    }

    private ApprovalStatus convertIntegerToApprovalStatus(Integer status) {
        if (status == null) {
            return ApprovalStatus.RUNNING; // 默认状态
        }
        
        // 根据数据库存储的ordinal值转换为枚举
        ApprovalStatus[] values = ApprovalStatus.values();
        if (status >= 0 && status < values.length) {
            return values[status];
        }
        return ApprovalStatus.RUNNING;
    }

    private ApprovalInstance handleApproveAction(ApprovalInstance instance, Map<String, Object> context) {
        // 如果这是最后一个节点，则完成审批
        if (instance.getProcessedNodes() >= instance.getTotalNodes() - 1) {
            instance.setStatus(ApprovalStatus.APPROVED.ordinal());
            instance.setFinishTime(LocalDateTime.now());
            log.info("审批流程完成: 实例ID={}", instance.getId());
        } else {
            // 流转到下一个节点
            return proceedToNextNode(instance, context);
        }
        
        return instanceRepository.save(instance);
    }

    private ApprovalInstance handleRejectAction(ApprovalInstance instance, Map<String, Object> context) {
        instance.setStatus(ApprovalStatus.REJECTED.ordinal());
        instance.setFinishTime(LocalDateTime.now());
        log.info("审批流程拒绝: 实例ID={}", instance.getId());
        return instanceRepository.save(instance);
    }

    private ApprovalInstance handleTransferAction(ApprovalInstance instance, ApprovalNode currentNode, 
                                                Map<String, Object> context) {
        // 转审操作不改变流程状态，只是更新当前节点的审批人
        Object targetApproverId = context.get("targetApproverId");
        if (targetApproverId != null) {
            currentNode.setApproverId(Long.valueOf(targetApproverId.toString()));
            nodeRepository.save(currentNode);
        }
        
        return instance;
    }

    private ApprovalInstance handleSkipAction(ApprovalInstance instance, Map<String, Object> context) {
        // 跳过当前节点，直接进入下一节点
        return proceedToNextNode(instance, context);
    }

    // 节点处理器接口
    public interface NodeProcessor {
        NodeResult process(ApprovalNode node, Map<String, Object> context);
        
        class NodeResult {
            private final boolean success;
            private final String message;
            private final Map<String, Object> output;
            
            public NodeResult(boolean success, String message, Map<String, Object> output) {
                this.success = success;
                this.message = message;
                this.output = output;
            }
            
            public boolean isSuccess() { return success; }
            public String getMessage() { return message; }
            public Map<String, Object> getOutput() { return output; }
        }
    }

    // 引擎统计类
    public static class EngineStatistics {
        private final Long totalExecutions;
        private final Long successfulExecutions;
        private final Long failedExecutions;
        private final Long timeoutExecutions;
        private final Double averageExecutionTime;
        
        public EngineStatistics(Long totalExecutions, Long successfulExecutions, 
                               Long failedExecutions, Long timeoutExecutions, 
                               Double averageExecutionTime) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.timeoutExecutions = timeoutExecutions;
            this.averageExecutionTime = averageExecutionTime;
        }
        
        // Getters
        public Long getTotalExecutions() { return totalExecutions; }
        public Long getSuccessfulExecutions() { return successfulExecutions; }
        public Long getFailedExecutions() { return failedExecutions; }
        public Long getTimeoutExecutions() { return timeoutExecutions; }
        public Double getAverageExecutionTime() { return averageExecutionTime; }
    }

    // 默认节点处理器实现
    private static class UserApprovalProcessor implements ApprovalEngine.NodeProcessor {
        @Override
        public ApprovalEngine.NodeProcessor.NodeResult process(ApprovalNode node, Map<String, Object> context) {
            // 简单实现：等待用户审批，这里只是模拟处理
            return new ApprovalEngine.NodeProcessor.NodeResult(true, "用户审批节点执行成功", null);
        }
    }

    private static class RoleApprovalProcessor implements ApprovalEngine.NodeProcessor {
        @Override
        public ApprovalEngine.NodeProcessor.NodeResult process(ApprovalNode node, Map<String, Object> context) {
            // 角色审批处理器实现
            return new ApprovalEngine.NodeProcessor.NodeResult(true, "角色审批节点执行成功", null);
        }
    }

    private static class DepartmentApprovalProcessor implements ApprovalEngine.NodeProcessor {
        @Override
        public ApprovalEngine.NodeProcessor.NodeResult process(ApprovalNode node, Map<String, Object> context) {
            // 部门审批处理器实现
            return new ApprovalEngine.NodeProcessor.NodeResult(true, "部门审批节点执行成功", null);
        }
    }


}