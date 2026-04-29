package com.ccms.service;

import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.enums.ReimburseStatusEnum;

/**
 * 报销单状态管理服务接口
 */
public interface ExpenseReimburseStatusService {
    
    /**
     * 提交报销单审批
     */
    void submitForApproval(ExpenseReimburseMain reimburse, Long userId);
    
    /**
     * 审批通过报销单
     */
    void approveReimburse(ExpenseReimburseMain reimburse, Long approverId, String remark);
    
    /**
     * 驳回报销单
     */
    void rejectReimburse(ExpenseReimburseMain reimburse, Long approverId, String reason);
    
    /**
     * 标记为待支付
     */
    void markAsPendingPayment(ExpenseReimburseMain reimburse, Long operatorId);
    
    /**
     * 完成支付
     */
    void completePayment(ExpenseReimburseMain reimburse, Long operatorId);
    
    /**
     * 作废报销单
     */
    void cancelReimburse(ExpenseReimburseMain reimburse, Long operatorId, String reason);
    
    /**
     * 验证状态转移是否允许
     */
    boolean validateStatusTransition(ReimburseStatusEnum currentStatus, ReimburseStatusEnum targetStatus);
    
    /**
     * 校验报销单是否可以提交审批
     */
    boolean canSubmitApproval(ExpenseReimburseMain reimburse);
    
    /**
     * 校验报销单是否可以完成支付
     */
    boolean canProcessPayment(ExpenseReimburseMain reimburse);
}