package com.ccms.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 状态转换规则枚举
 * 定义审批流程中各个状态之间允许的转换规则
 */
public enum TransitionRule {
    
    // 草稿状态转换规则
    DRAFT_TO_APPROVING(ApprovalStatus.DRAFT, ApprovalStatus.APPROVING, 
                       Arrays.asList(ApprovalAction.SUBMIT)),
    DRAFT_TO_CANCELLED(ApprovalStatus.DRAFT, ApprovalStatus.CANCELED,
                       Arrays.asList(ApprovalAction.CANCEL)),
    
    // 审批中状态转换规则
    APPROVING_TO_APPROVING(ApprovalStatus.APPROVING, ApprovalStatus.APPROVING,
                          Arrays.asList(ApprovalAction.TRANSFER, ApprovalAction.SKIP)),
    APPROVING_TO_APPROVED(ApprovalStatus.APPROVING, ApprovalStatus.APPROVED,
                         Arrays.asList(ApprovalAction.APPROVE, ApprovalAction.COMPLETE)),
    APPROVING_TO_REJECTED(ApprovalStatus.APPROVING, ApprovalStatus.REJECTED,
                         Arrays.asList(ApprovalAction.REJECT)),
    APPROVING_TO_CANCELLED(ApprovalStatus.APPROVING, ApprovalStatus.CANCELED,
                          Arrays.asList(ApprovalAction.CANCEL)),
    APPROVING_TO_WITHDRAWN(ApprovalStatus.APPROVING, ApprovalStatus.WITHDRAWN,
                          Arrays.asList(ApprovalAction.WITHDRAW)),
    APPROVING_TO_TERMINATED(ApprovalStatus.APPROVING, ApprovalStatus.TERMINATED,
                          Arrays.asList(ApprovalAction.TERMINATE)),
    
    // 驳回后的重新提交规则
    REJECTED_TO_APPROVING(ApprovalStatus.REJECTED, ApprovalStatus.APPROVING,
                         Arrays.asList(ApprovalAction.RESUBMIT)),
    
    // 最终状态（不允许转换）
    FINAL_STATE(ApprovalStatus.APPROVED, ApprovalStatus.APPROVED, Arrays.asList()),
    FINAL_STATE_REJECTED(ApprovalStatus.REJECTED, ApprovalStatus.REJECTED, Arrays.asList()),
    FINAL_STATE_CANCELLED(ApprovalStatus.CANCELED, ApprovalStatus.CANCELED, Arrays.asList()),
    FINAL_STATE_WITHDRAWN(ApprovalStatus.WITHDRAWN, ApprovalStatus.WITHDRAWN, Arrays.asList()),
    FINAL_STATE_TERMINATED(ApprovalStatus.TERMINATED, ApprovalStatus.TERMINATED, Arrays.asList());

    private final ApprovalStatus fromStatus;
    private final ApprovalStatus toStatus;
    private final List<ApprovalAction> allowedActions;

    TransitionRule(ApprovalStatus fromStatus, ApprovalStatus toStatus, List<ApprovalAction> allowedActions) {
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.allowedActions = allowedActions;
    }

    public ApprovalStatus getFromStatus() {
        return fromStatus;
    }

    public ApprovalStatus getToStatus() {
        return toStatus;
    }

    public List<ApprovalAction> getAllowedActions() {
        return allowedActions;
    }

    /**
     * 检查指定转换是否允许
     */
    public static boolean isTransitionAllowed(ApprovalStatus fromStatus, ApprovalStatus toStatus, ApprovalAction action) {
        for (TransitionRule rule : values()) {
            if (rule.fromStatus == fromStatus && rule.toStatus == toStatus) {
                return rule.allowedActions.contains(action);
            }
        }
        return false;
    }

    /**
     * 获取从指定状态可以转换到的所有目标状态
     */
    public static List<ApprovalStatus> getTargetStatuses(ApprovalStatus fromStatus) {
        return Arrays.stream(values())
                .filter(rule -> rule.fromStatus == fromStatus)
                .map(TransitionRule::getToStatus)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取从指定状态允许的所有操作
     */
    public static List<ApprovalAction> getAllowedActions(ApprovalStatus fromStatus) {
        return Arrays.stream(values())
                .filter(rule -> rule.fromStatus == fromStatus)
                .flatMap(rule -> rule.allowedActions.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 获取指定操作对应的目标状态
     */
    public static ApprovalStatus getTargetStatus(ApprovalStatus fromStatus, ApprovalAction action) {
        for (TransitionRule rule : values()) {
            if (rule.fromStatus == fromStatus && rule.allowedActions.contains(action)) {
                return rule.toStatus;
            }
        }
        return null;
    }

    /**
     * 获取转换规则的描述
     */
    public String getDescription() {
        return String.format("从 %s 转换到 %s，允许操作：%s", 
                fromStatus.getDescription(), 
                toStatus.getDescription(),
                allowedActions.stream().map(ApprovalAction::getDescription).collect(Collectors.joining(", ")));
    }
}