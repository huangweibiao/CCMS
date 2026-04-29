package com.ccms.service.impl;

import com.ccms.entity.system.OperateLog;
import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.enums.OperateTypeEnum;
import com.ccms.enums.ReimburseStatusEnum;
import com.ccms.exception.BusinessException;
import com.ccms.service.ExpenseReimburseStatusService;
import com.ccms.service.OperateLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 报销单状态管理服务实现
 */
@Service
public class ExpenseReimburseStatusServiceImpl implements ExpenseReimburseStatusService {

    private static final Logger log = LoggerFactory.getLogger(ExpenseReimburseStatusServiceImpl.class);

    @Autowired
    private OperateLogService operateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitForApproval(ExpenseReimburseMain reimburse, Long userId) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        
        // 校验状态转移是否允许
        if (currentStatus == null || !currentStatus.canSubmitApproval()) {
            throw new RuntimeException( 
                String.format("报销单当前状态[%s]不允许提交审批", currentStatus != null ? currentStatus.getName() : "未知"));
        }
        
        // 业务校验：报销单金额必须大于0
        if (reimburse.getTotalAmount() == null || reimburse.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("报销金额必须大于0");
        }
        
        // 业务校验：报销明细不能为空（通过关联检查）
        if (reimburse.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) == 0) {
            throw new RuntimeException("报销单必须包含有效明细");
        }
        
        // 更新状态
        reimburse.setStatus(ReimburseStatusEnum.APPROVAL_PENDING.getCode());
        reimburse.setUpdateTime(LocalDateTime.now());
        
        // 记录操作日志
        recordOperateLog(reimburse.getId(), userId, OperateTypeEnum.REIMBURSE_SUBMIT, 
            "提交报销单审批");
        
        log.info("报销单[{}]提交审批成功，用户ID：{}", reimburse.getReimburseNo(), userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveReimburse(ExpenseReimburseMain reimburse, Long approverId, String remark) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        
        // 校验状态
        if (currentStatus != ReimburseStatusEnum.APPROVAL_PENDING) {
            throw new RuntimeException( 
                String.format("当前状态[%s]不允许审批操作", currentStatus != null ? currentStatus.getName() : "未知"));
        }
        
        // 更新状态
        reimburse.setStatus(ReimburseStatusEnum.APPROVED.getCode());
        reimburse.setApprovalStatus(1); // 审批通过
        reimburse.setUpdateTime(LocalDateTime.now());
        
        // 记录操作日志
        recordOperateLog(reimburse.getId(), approverId, OperateTypeEnum.REIMBURSE_APPROVE, 
            "审批通过报销单" + (remark != null ? ", 备注：" + remark : ""));
        
        log.info("报销单[{}]审批通过，审批人ID：{}, 备注：{}", reimburse.getReimburseNo(), approverId, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectReimburse(ExpenseReimburseMain reimburse, Long approverId, String reason) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        
        // 校验状态
        if (currentStatus != ReimburseStatusEnum.APPROVAL_PENDING) {
            throw new RuntimeException( 
                String.format("当前状态[%s]不允许驳回操作", currentStatus != null ? currentStatus.getName() : "未知"));
        }
        
        // 必须有驳回原因
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("驳回原因不能为空");
        }
        
        // 更新状态
        reimburse.setStatus(ReimburseStatusEnum.REJECTED.getCode());
        reimburse.setApprovalStatus(2); // 审批驳回
        reimburse.setUpdateTime(LocalDateTime.now());
        
        // 记录操作日志
        recordOperateLog(reimburse.getId(), approverId, OperateTypeEnum.REIMBURSE_REJECT, 
            "驳回报销单，原因：" + reason);
        
        log.info("报销单[{}]被驳回，审批人ID：{}, 原因：{}", reimburse.getReimburseNo(), approverId, reason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsPendingPayment(ExpenseReimburseMain reimburse, Long operatorId) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        
        // 校验状态转移
        if (currentStatus != ReimburseStatusEnum.APPROVED) {
            throw new RuntimeException( 
                String.format("报销单必须为已通过状态才能标记为待支付，当前状态：%s", 
                    currentStatus != null ? currentStatus.getName() : "未知"));
        }
        
        // 更新状态
        reimburse.setStatus(ReimburseStatusEnum.PENDING_PAYMENT.getCode());
        reimburse.setUpdateTime(LocalDateTime.now());
        
        // 记录操作日志
        recordOperateLog(reimburse.getId(), operatorId, OperateTypeEnum.REIMBURSE_PAYMENT_PENDING, 
            "标记报销单为待支付状态");
        
        log.info("报销单[{}]标记为待支付，操作人ID：{}", reimburse.getReimburseNo(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completePayment(ExpenseReimburseMain reimburse, Long operatorId) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        
        // 校验状态
        if (currentStatus != ReimburseStatusEnum.PENDING_PAYMENT) {
            throw new RuntimeException( 
                String.format("报销单必须为待支付状态才能完成支付，当前状态：%s", 
                    currentStatus != null ? currentStatus.getName() : "未知"));
        }
        
        // 校验支付信息
        if (reimburse.getPaymentMethod() == null) {
            throw new RuntimeException("支付方式不能为空");
        }
        
        // 更新状态和支付信息
        reimburse.setStatus(ReimburseStatusEnum.PAID.getCode());
        reimburse.setPaymentTime(java.sql.Date.valueOf(LocalDateTime.now().toLocalDate()));
        reimburse.setUpdateTime(LocalDateTime.now());
        
        // 记录操作日志
        recordOperateLog(reimburse.getId(), operatorId, OperateTypeEnum.REIMBURSE_PAYMENT_COMPLETE, 
            "报销单支付完成");
        
        log.info("报销单[{}]支付完成，操作人ID：{}", reimburse.getReimburseNo(), operatorId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelReimburse(ExpenseReimburseMain reimburse, Long operatorId, String reason) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        
        // 校验是否可以作废
        if (currentStatus == null || !currentStatus.canCancel()) {
            throw new RuntimeException( 
                String.format("报销单当前状态[%s]不允许作废操作", currentStatus != null ? currentStatus.getName() : "未知"));
        }
        
        // 必须有作废原因
        if (reason == null || reason.trim().isEmpty()) {
            throw new RuntimeException("作废原因不能为空");
        }
        
        // 更新状态
        reimburse.setStatus(ReimburseStatusEnum.CANCELLED.getCode());
        reimburse.setUpdateTime(LocalDateTime.now());
        
        // 记录操作日志
        recordOperateLog(reimburse.getId(), operatorId, OperateTypeEnum.REIMBURSE_CANCEL, 
            "作废报销单，原因：" + reason);
        
        log.info("报销单[{}]已作废，操作人ID：{}, 原因：{}", reimburse.getReimburseNo(), operatorId, reason);
    }

    @Override
    public boolean validateStatusTransition(ReimburseStatusEnum currentStatus, ReimburseStatusEnum targetStatus) {
        return currentStatus != null && targetStatus != null && currentStatus.canTransitionTo(targetStatus);
    }

    @Override
    public boolean canSubmitApproval(ExpenseReimburseMain reimburse) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        return currentStatus != null && currentStatus.canSubmitApproval() && 
               reimburse.getTotalAmount() != null && 
               reimburse.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    @Override
    public boolean canProcessPayment(ExpenseReimburseMain reimburse) {
        ReimburseStatusEnum currentStatus = ReimburseStatusEnum.getByCode(reimburse.getStatus());
        return currentStatus != null && currentStatus.canProcessPayment() && 
               reimburse.getPaymentMethod() != null;
    }
    
    /**
     * 记录操作日志
     */
    private void recordOperateLog(Long reimburseId, Long userId, OperateTypeEnum operateType, String content) {
        OperateLog operateLog = new OperateLog();
        operateLog.setBusinessType("expense_reimburse");
        operateLog.setBusinessId(reimburseId);
        operateLog.setOperModule("费用报销");
        operateLog.setOperType(operateType.getCode().toString());
        operateLog.setOperUserId(userId);
        operateLog.setOperContent(content);
        operateLog.setOperTime(LocalDateTime.now());
        
        operateLogService.saveOperateLog(operateLog);
    }
}