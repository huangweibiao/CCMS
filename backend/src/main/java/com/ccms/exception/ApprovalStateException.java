package com.ccms.exception;

import com.ccms.enums.ApprovalActionEnum;
import com.ccms.enums.ApprovalStatusEnum;

/**
 * 审批状态转换异常
 */
public class ApprovalStateException extends ApprovalException {
    
    private final ApprovalStatusEnum currentStatus;
    private final ApprovalActionEnum attemptedAction;
    private final Long instanceId;
    
    public ApprovalStateException(Long instanceId, ApprovalStatusEnum currentStatus, ApprovalActionEnum attemptedAction) {
        super("STATE_TRANSITION_ERROR", 
              String.format("审批实例状态转换错误: 实例ID=%d, 当前状态=%s, 尝试操作=%s", 
                           instanceId, currentStatus, attemptedAction));
        this.instanceId = instanceId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }
    
    public ApprovalStateException(Long instanceId, ApprovalStatusEnum currentStatus, 
                                 ApprovalActionEnum attemptedAction, Throwable cause) {
        super("STATE_TRANSITION_ERROR", 
              String.format("审批实例状态转换错误: 实例ID=%d, 当前状态=%s, 尝试操作=%s", 
                           instanceId, currentStatus, attemptedAction), cause);
        this.instanceId = instanceId;
        this.currentStatus = currentStatus;
        this.attemptedAction = attemptedAction;
    }
    
    public ApprovalStatusEnum getCurrentStatus() {
        return currentStatus;
    }
    
    public ApprovalActionEnum getAttemptedAction() {
        return attemptedAction;
    }
    
    public Long getInstanceId() {
        return instanceId;
    }
}