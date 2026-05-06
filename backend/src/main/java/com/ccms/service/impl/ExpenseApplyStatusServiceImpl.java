package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseApplyMain;
import com.ccms.entity.system.log.OperateLog;
import com.ccms.enums.ApplyStatusEnum;
import com.ccms.exception.BusinessException;
import com.ccms.repository.expense.ExpenseApplyMainRepository;
import com.ccms.repository.system.log.OperateLogRepository;
import com.ccms.service.ExpenseApplyStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

/**
 * 费用申请单状态管理服务实现
 */
@Service
@Transactional
public class ExpenseApplyStatusServiceImpl implements ExpenseApplyStatusService {

    @Autowired
    private ExpenseApplyMainRepository expenseApplyMainRepository;

    @Autowired
    private OperateLogRepository operateLogRepository;

    @Override
    public void changeApplyStatus(Long applyId, Integer targetStatus, Long operatorId, String remark, String ip) {
        // 1. 查询申请单
        ExpenseApplyMain apply = expenseApplyMainRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException.ApplyStatusException("申请单不存在"));

        // 2. 校验状态流转是否合法
        if (!ApplyStatusEnum.isTransitionAllowed(apply.getStatus(), targetStatus)) {
            throw new BusinessException.ApplyStatusException(
                    ApplyStatusEnum.getTransitionDescription(apply.getStatus(), targetStatus)
            );
        }

        // 3. 记录原状态
        Integer originalStatus = apply.getStatus();

        // 4. 更新状态
        apply.setStatus(targetStatus);
        apply.setUpdateTime(LocalDateTime.now());
        expenseApplyMainRepository.save(apply);

        // 5. 记录操作日志
        recordOperateLog(operatorId, applyId, originalStatus, targetStatus, remark, ip);
    }

    @Override
    public void submitForApproval(Long applyId, Long operatorId, String ip) {
        // 提交审批：从草稿状态变为审批中
        changeApplyStatus(applyId, ApplyStatusEnum.APPROVING.getCode(), operatorId, "提交审批", ip);
    }

    @Override
    public void approveApply(Long applyId, Long operatorId, String remark, String ip) {
        // 审批通过：从审批中状态变为已通过
        changeApplyStatus(applyId, ApplyStatusEnum.APPROVED.getCode(), operatorId, 
                "审批通过" + (remark != null ? "：" + remark : ""), ip);
    }

    @Override
    public void rejectApply(Long applyId, Long operatorId, String remark, String ip) {
        // 审批驳回：从审批中状态变为已驳回
        if (remark == null || remark.trim().isEmpty()) {
            throw new BusinessException.ApplyStatusException("驳回审批必须填写驳回原因");
        }
        changeApplyStatus(applyId, ApplyStatusEnum.REJECTED.getCode(), operatorId, 
                "审批驳回：" + remark, ip);
    }

    @Override
    public void cancelApply(Long applyId, Long operatorId, String remark, String ip) {
        // 作废申请：从任意状态变为作废（需有权限校验）
        changeApplyStatus(applyId, ApplyStatusEnum.CANCELLED.getCode(), operatorId, 
                "作废申请" + (remark != null ? "：" + remark : ""), ip);
    }

    @Override
    public void markAsPayment(Long applyId, Long operatorId, String ip) {
        // 标记为待支付：从已通过状态变为待支付
        ExpenseApplyMain apply = expenseApplyMainRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException.ApplyStatusException("申请单不存在"));
        
        if (!apply.getStatus().equals(ApplyStatusEnum.APPROVED.getCode())) {
            throw new BusinessException.ApplyStatusException("只有已通过的申请单才能标记为待支付");
        }
        
        changeApplyStatus(applyId, ApplyStatusEnum.TO_BE_PAID.getCode(), operatorId, "标记为待支付", ip);
    }

    @Override
    public void completePayment(Long applyId, Long operatorId, String ip) {
        // 完成支付：从待支付状态变为已支付
        ExpenseApplyMain apply = expenseApplyMainRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException.ApplyStatusException("申请单不存在"));
        
        if (!apply.getStatus().equals(ApplyStatusEnum.TO_BE_PAID.getCode())) {
            throw new BusinessException.ApplyStatusException("只有待支付的申请单才能完成支付");
        }
        
        changeApplyStatus(applyId, ApplyStatusEnum.PAID.getCode(), operatorId, "完成支付", ip);
    }

    @Override
    public void resubmitApply(Long applyId, Long operatorId, String ip) {
        // 重新提交：从已驳回状态变为草稿状态，便于重新编辑提交
        ExpenseApplyMain apply = expenseApplyMainRepository.findById(applyId)
                .orElseThrow(() -> new BusinessException.ApplyStatusException("申请单不存在"));
        
        if (!apply.getStatus().equals(ApplyStatusEnum.REJECTED.getCode())) {
            throw new BusinessException.ApplyStatusException("只有被驳回的申请单才能重新提交");
        }
        
        changeApplyStatus(applyId, ApplyStatusEnum.DRAFT.getCode(), operatorId, "重新提交申请", ip);
    }

    /**
     * 记录操作日志
     */
    private void recordOperateLog(Long userId, Long businessId, Integer fromStatus, Integer toStatus, String remark, String ip) {
        OperateLog log = new OperateLog();
        log.setOperUserId(userId);
        log.setOperModule("费用申请");
        log.setOperType("状态变更");
        
        String fromStatusDesc = ApplyStatusEnum.getByCode(fromStatus) != null ? 
                ApplyStatusEnum.getByCode(fromStatus).getDescription() : String.valueOf(fromStatus);
        String toStatusDesc = ApplyStatusEnum.getByCode(toStatus) != null ? 
                ApplyStatusEnum.getByCode(toStatus).getDescription() : String.valueOf(toStatus);
        
        log.setOperContent(String.format("费用申请单状态变更：%s → %s%s", 
                fromStatusDesc, toStatusDesc, 
                remark != null ? "，备注：" + remark : ""));
        
        log.setBusinessId(businessId);
        log.setBusinessType("EXPENSE_APPLY");
        log.setOperIp(ip);
        log.setOperTime(LocalDateTime.now());
        
        operateLogRepository.save(log);
    }

    @Override
    public boolean canSubmitForApproval(Long applyId) {
        ExpenseApplyMain apply = expenseApplyMainRepository.findById(applyId).orElse(null);
        return apply != null && ApplyStatusEnum.isTransitionAllowed(
                apply.getStatus(), ApplyStatusEnum.APPROVING.getCode());
    }

    @Override
    public boolean canApprove(Long applyId) {
        ExpenseApplyMain apply = expenseApplyMainRepository.findById(applyId).orElse(null);
        return apply != null && apply.getStatus().equals(ApplyStatusEnum.APPROVING.getCode());
    }
}