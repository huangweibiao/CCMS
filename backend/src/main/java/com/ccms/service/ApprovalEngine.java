package com.ccms.service;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;

import java.util.List;
import java.util.Map;

/**
 * 审批流程执行引擎接口
 * 负责审批流程的流转控制、节点执行和状态管理
 */
public interface ApprovalEngine {

    /**
     * 初始化流程引擎
     */
    void initialize();

    /**
     * 执行流程节点
     */
    void executeNode(ApprovalInstance instance, ApprovalNode node, Map<String, Object> context);

    /**
     * 处理审批操作结果
     */
    ApprovalInstance handleApprovalResult(ApprovalInstance instance, ApprovalAction action, 
                                         ApprovalNode currentNode, Map<String, Object> context);

    /**
     * 流转到下一个节点
     */
    ApprovalInstance proceedToNextNode(ApprovalInstance instance, Map<String, Object> context);

    /**
     * 处理流程终止
     */
    ApprovalInstance terminateProcess(ApprovalInstance instance, ApprovalStatus finalStatus, 
                                     String reason, Map<String, Object> context);

    /**
     * 验证流程配置的合法性
     */
    boolean validateFlowConfig(ApprovalFlowConfig flowConfig);

    /**
     * 获取可执行的节点列表
     */
    List<ApprovalNode> getExecutableNodes(ApprovalInstance instance);

    /**
     * 检查流程是否可继续
     */
    boolean canProceed(ApprovalInstance instance);

    /**
     * 获取流程当前状态
     */
    ApprovalStatus getCurrentStatus(ApprovalInstance instance);

    /**
     * 处理超时的审批实例
     */
    List<ApprovalInstance> handleTimeoutInstances();

    /**
     * 获取流程执行统计信息
     */
    EngineStatistics getEngineStatistics();

    /**
     * 注册自定义节点处理器
     */
    void registerNodeProcessor(String nodeType, NodeProcessor processor);

    /**
     * 节点处理器接口
     */
    interface NodeProcessor {
        /**
         * 处理节点执行
         */
        NodeResult process(ApprovalNode node, Map<String, Object> context);

        /**
         * 节点执行结果
         */
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

    /**
     * 引擎统计信息
     */
    class EngineStatistics {
        private final Long totalExecutions;
        private final Long successfulExecutions;
        private final Long failedExecutions;
        private final Long timeoutExecutions;
        private final Double averageExecutionTime;

        public EngineStatistics(Long totalExecutions, Long successfulExecutions, Long failedExecutions,
                               Long timeoutExecutions, Double averageExecutionTime) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.timeoutExecutions = timeoutExecutions;
            this.averageExecutionTime = averageExecutionTime;
        }

        public Long getTotalExecutions() { return totalExecutions; }
        public Long getSuccessfulExecutions() { return successfulExecutions; }
        public Long getFailedExecutions() { return failedExecutions; }
        public Long getTimeoutExecutions() { return timeoutExecutions; }
        public Double getAverageExecutionTime() { return averageExecutionTime; }
    }

    /**
     * 流程执行上下文
     */
    class ProcessContext {
        private final Map<String, Object> variables;
        private final List<String> executionLogs;

        public ProcessContext() {
            this.variables = new java.util.HashMap<>();
            this.executionLogs = new java.util.ArrayList<>();
        }

        public void setVariable(String key, Object value) {
            variables.put(key, value);
        }

        public Object getVariable(String key) {
            return variables.get(key);
        }

        public void addLog(String log) {
            executionLogs.add(log);
        }

        public List<String> getExecutionLogs() {
            return new java.util.ArrayList<>(executionLogs);
        }

        public Map<String, Object> getVariables() {
            return new java.util.HashMap<>(variables);
        }
    }

    /**
     * 流程状态机
     */
    interface ProcessStateMachine {
        /**
         * 处理状态转换
         */
        boolean transition(ApprovalInstance instance, ApprovalAction action, Map<String, Object> context);

        /**
         * 获取允许的转换操作
         */
        List<ApprovalAction> getAllowedActions(ApprovalStatus currentStatus);

        /**
         * 获取状态转换规则
         */
        Map<ApprovalStatus, List<ApprovalAction>> getTransitionRules();
    }
}