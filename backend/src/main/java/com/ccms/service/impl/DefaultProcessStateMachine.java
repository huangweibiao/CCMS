package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApprovalActionEnum;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.service.ApprovalEngine;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DefaultProcessStateMachine implements ApprovalEngine.ProcessStateMachine {

    @Override
    public ApprovalStatusEnum getNextState(ApprovalStatusEnum currentState, ApprovalActionEnum action, 
                                         ApprovalInstance instance, Map<String, Object> context) {
        log.debug("状态转换计算: 当前状态={}, 操作={}", currentState, action);
        
        // 基础状态转换规则
        switch (currentState) {
            case RUNNING:
                return handleRunningState(instance, action, context);
                
            case WAITING:
                return handleWaitingState(instance, action, context);
                
            case PENDING:
                return handlePendingState(instance, action, context);
                
            default:
                // 最终状态不允许转换
                if (currentState.isFinalStatus()) {
                    throw new IllegalStateException("最终状态 " + currentState + " 不允许状态转换");
                }
                return currentState;
        }
    }

    @Override
    public boolean isValidTransition(ApprovalStatusEnum currentState, ApprovalStatusEnum nextState) {
        // 简单验证：不允许状态保持不变（除特殊情况外）
        if (currentState == nextState) {
            return false;
        }
        
        // 最终状态不允许转换为其他状态
        if (currentState.isFinalStatus() && currentState != nextState) {
            return false;
        }
        
        // 定义允许的转换
        switch (currentState) {
            case PENDING:
                return nextState == ApprovalStatusEnum.RUNNING || 
                       nextState == ApprovalStatusEnum.CANCELED;
                        
            case RUNNING:
                return nextState == ApprovalStatusEnum.APPROVED ||
                       nextState == ApprovalStatusEnum.REJECTED ||
                       nextState == ApprovalStatusEnum.CANCELED ||
                       nextState == ApprovalStatusEnum.TIMEOUT;
                       
            case WAITING:
                return nextState == ApprovalStatusEnum.RUNNING ||
                       nextState == ApprovalStatusEnum.CANCELED;
                       
            default:
                return false;
        }
    }

    @Override
    public ApprovalEngine.StateTransitionResult buildTransitionContext(ApprovalStatusEnum fromState, 
                                                                      ApprovalStatusEnum toState,
                                                                      Map<String, Object> currentContext) {
        StateTransitionResult result = new StateTransitionResult();
        result.setFromState(fromState);
        result.setToState(toState);
        result.setTimestamp(System.currentTimeMillis());
        
        // 根据状态转换构建上下文
        if (toState.isFinalStatus()) {
            result.getContextUpdates().put("isFinal", true);
            result.getContextUpdates().put("finalStatus", toState.name());
        }
        
        // 添加特定状态转换的上下文
        switch (fromState) {
            case RUNNING:
                if (toState == ApprovalStatusEnum.APPROVED) {
                    result.getContextUpdates().put("completionType", "APPROVED");
                } else if (toState == ApprovalStatusEnum.REJECTED) {
                    result.getContextUpdates().put("completionType", "REJECTED");
                }
                break;
        }
        
        return result;
    }

    private ApprovalStatusEnum handleRunningState(ApprovalInstance instance, ApprovalActionEnum action, 
                                                 Map<String, Object> context) {
        switch (action) {
            case APPROVE:
                // 检查是否是最后一个节点
                if (instance.getProcessedNodes() >= instance.getTotalNodes() - 1) {
                    return ApprovalStatusEnum.APPROVED;
                } else {
                    // 继续到下一个节点
                    return ApprovalStatusEnum.RUNNING;
                }
                
            case REJECT:
                return ApprovalStatusEnum.REJECTED;
                
            case TRANSFER:
                // 转审操作停留在当前状态
                return ApprovalStatusEnum.RUNNING;
                
            case SKIP:
                // 跳过节点，继续流转
                return ApprovalStatusEnum.RUNNING;
                
            case CANCEL:
                return ApprovalStatusEnum.CANCELED;
                
            default:
                log.warn("未知的审批操作: {}", action);
                return instance.getStatus();
        }
    }

    private ApprovalStatusEnum handleWaitingState(ApprovalInstance instance, ApprovalActionEnum action,
                                                 Map<String, Object> context) {
        switch (action) {
            case APPROVE:
            case REJECT:
                return ApprovalStatusEnum.RUNNING;
                
            case CANCEL:
                return ApprovalStatusEnum.CANCELED;
                
            default:
                return instance.getStatus();
        }
    }

    private ApprovalStatusEnum handlePendingState(ApprovalInstance instance, ApprovalActionEnum action,
                                                 Map<String, Object> context) {
        switch (action) {
            case APPROVE:
                return ApprovalStatusEnum.RUNNING;
                
            case CANCEL:
                return ApprovalStatusEnum.CANCELED;
                
            default:
                return instance.getStatus();
        }
    }
}