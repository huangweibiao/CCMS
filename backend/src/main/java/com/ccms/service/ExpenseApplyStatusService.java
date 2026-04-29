package com.ccms.service;

/**
 * 费用申请单状态管理服务接口
 */
public interface ExpenseApplyStatusService {
    
    /**
     * 变更申请单状态
     */
    void changeApplyStatus(Long applyId, Integer targetStatus, Long operatorId, String remark, String ip);
    
    /**
     * 提交申请单进行审批
     */
    void submitForApproval(Long applyId, Long operatorId, String ip);
    
    /**
     * 审批通过申请单
     */
    void approveApply(Long applyId, Long operatorId, String remark, String ip);
    
    /**
     * 审批驳回申请单
     */
    void rejectApply(Long applyId, Long operatorId, String remark, String ip);
    
    /**
     * 作废申请单
     */
    void cancelApply(Long applyId, Long operatorId, String remark, String ip);
    
    /**
     * 标记申请单为待支付状态
     */
    void markAsPayment(Long applyId, Long operatorId, String ip);
    
    /**
     * 完成支付操作
     */
    void completePayment(Long applyId, Long operatorId, String ip);
    
    /**
     * 重新提交被驳回的申请单
     */
    void resubmitApply(Long applyId, Long operatorId, String ip);
    
    /**
     * 检查是否可以提交审批
     */
    boolean canSubmitForApproval(Long applyId);
    
    /**
     * 检查是否可以审批
     */
    boolean canApprove(Long applyId);
}