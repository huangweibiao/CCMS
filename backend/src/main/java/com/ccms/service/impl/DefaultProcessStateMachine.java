package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.service.ApprovalEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultProcessStateMachine implements ApprovalEngine.ProcessStateMachine {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultProcessStateMachine.class);

    /**
     * 状态转换结果类
     */
    public static class StateTransitionResult {
        private ApprovalStatus fromState;
        private ApprovalStatus toState;
        private Long timestamp;
        private Map<String, Object> contextUpdates;

        public StateTransitionResult() {
            this.contextUpdates = new HashMap<>();
            this.timestamp = System.currentTimeMillis();
        }

        public ApprovalStatus getFromState() { return fromState; }
        public void setFromState(ApprovalStatus fromState) { this.fromState = fromState; }

        public ApprovalStatus getToState() { return toState; }
        public void setToState(ApprovalStatus toState) { this.toState = toState; }

        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

        public Map<String, Object> getContextUpdates() { return contextUpdates; }
        public void setContextUpdates(Map<String, Object> contextUpdates) { this.contextUpdates = contextUpdates; }
    }

    @Override
    public boolean transition(ApprovalInstance instance, ApprovalAction action, Map<String, Object> context) {
        log.debug("执行状态转换: 实例ID={}, 当前状态={}, 操作={}", instance.getId(), instance.getStatus(), action);
        
        ApprovalStatus currentStatus = instance.getStatusEnum();
        
        // 检查是否允许的状态转换
        if (!isValidTransition(currentStatus, action)) {
            log.warn("不允许的状态转换: 当前状态={}, 操作={}", currentStatus, action);
            return false;
        }
        
        // 执行状态转换逻辑
        ApprovalStatus newStatus = calculateNextStatus(currentStatus, action, instance);
        instance.setStatus(newStatus.ordinal());
        
        log.info("状态转换成功: 实例ID={}, 新状态={}", instance.getId(), newStatus);
        return true;
    }

    @Override
    public List<ApprovalAction> getAllowedActions(ApprovalStatus currentStatus) {
        List<ApprovalAction> allowedActions = new ArrayList<>();
        
        switch (currentStatus) {
            case PENDING:
                allowedActions.add(ApprovalAction.APPROVE);
                allowedActions.add(ApprovalAction.CANCEL);
                break;
                
            case RUNNING:
                allowedActions.add(ApprovalAction.APPROVE);
                allowedActions.add(ApprovalAction.REJECT);
                allowedActions.add(ApprovalAction.TRANSFER);
                allowedActions.add(ApprovalAction.SKIP);
                allowedActions.add(ApprovalAction.CANCEL);
                break;
                
            default:
                // 最终状态不允许任何操作
                break;
        }
        
        return allowedActions;
    }

    @Override
    public Map<ApprovalStatus, List<ApprovalAction>> getTransitionRules() {
        Map<ApprovalStatus, List<ApprovalAction>> rules = new HashMap<>();
        
        rules.put(ApprovalStatus.PENDING, Arrays.asList(ApprovalAction.APPROVE, ApprovalAction.CANCEL));
        rules.put(ApprovalStatus.RUNNING, Arrays.asList(ApprovalAction.APPROVE, ApprovalAction.REJECT, 
                                                        ApprovalAction.TRANSFER, ApprovalAction.SKIP, ApprovalAction.CANCEL));
        rules.put(ApprovalStatus.APPROVING, Arrays.asList(ApprovalAction.APPROVE, ApprovalAction.REJECT, ApprovalAction.CANCEL));
        
        // 最终状态不允许任何转换
        rules.put(ApprovalStatus.APPROVED, Collections.emptyList());
        rules.put(ApprovalStatus.REJECTED, Collections.emptyList());
        rules.put(ApprovalStatus.CANCELED, Collections.emptyList());
        rules.put(ApprovalStatus.TIMEOUT, Collections.emptyList());
        
        return rules;
    }

    private boolean isValidTransition(ApprovalStatus currentStatus, ApprovalAction action) {
        List<ApprovalAction> allowedActions = getAllowedActions(currentStatus);
        return allowedActions.contains(action);
    }

    private ApprovalStatus calculateNextStatus(ApprovalStatus currentStatus, ApprovalAction action, ApprovalInstance instance) {
        switch (currentStatus) {
            case PENDING:
                switch (action) {
                    case APPROVE: return ApprovalStatus.RUNNING;
                    case CANCEL: return ApprovalStatus.CANCELED;
                    default: return currentStatus;
                }
                
            case RUNNING:
                switch (action) {
                    case APPROVE:
                        // 检查是否是最后一个节点
                        if (instance.getProcessedNodes() >= instance.getTotalNodes() - 1) {
                            return ApprovalStatus.APPROVED;
                        } else {
                            return ApprovalStatus.RUNNING;
                        }
                    case REJECT: return ApprovalStatus.REJECTED;
                    case TRANSFER: return ApprovalStatus.RUNNING;
                    case SKIP: return ApprovalStatus.RUNNING;
                    case CANCEL: return ApprovalStatus.CANCELED;
                    default: return currentStatus;
                }
                
            case APPROVING:
                switch (action) {
                    case APPROVE:
                    case REJECT: return ApprovalStatus.RUNNING;
                    case CANCEL: return ApprovalStatus.CANCELED;
                    default: return currentStatus;
                }
                
            default:
                return currentStatus;
        }
    }

}