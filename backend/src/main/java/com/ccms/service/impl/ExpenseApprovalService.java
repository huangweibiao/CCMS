package com.ccms.service.impl;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.repository.expense.ExpenseApplyDetailRepository;
import com.ccms.repository.expense.ExpenseApplyMainRepository;
import com.ccms.service.ApprovalFlowService;
import com.ccms.service.BaseApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 费用审批服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseApprovalService extends BaseApprovalService {

    private final ExpenseApplyMainRepository expenseApplyMainRepository;
    private final ExpenseApplyDetailRepository expenseApplyDetailRepository;
    
    @Override
    public BusinessTypeEnum getBusinessType() {
        return BusinessTypeEnum.EXPENSE_APPLY;
    }
    
    @Override
    protected boolean validateBeforeCreate(ApprovalRequest request, Map<String, Object> context) {
        log.info("费用审批前置验证: 业务ID={}", request.getBusinessId());
        
        // 检查业务数据是否存在
        if (!expenseApplyMainRepository.existsById(Long.valueOf(request.getBusinessId()))) {
            log.error("费用申请数据不存在: ID={}", request.getBusinessId());
            return false;
        }
        
        // 检查是否已经存在审批实例
        if (approvalFlowService.getApprovalInstanceByBusinessId(request.getBusinessId(), getBusinessType()) != null) {
            log.error("费用申请已存在审批实例: 业务ID={}", request.getBusinessId());
            return false;
        }
        
        // 验证金额是否合理
        if (request.getAmount() != null && request.getAmount() < 0) {
            log.error("费用金额不合法: {}", request.getAmount());
            return false;
        }
        
        // 添加费用相关的业务验证
        boolean valid = performExpenseSpecificValidation(request, context);
        if (!valid) {
            log.error("费用业务验证失败: 业务ID={}", request.getBusinessId());
            return false;
        }
        
        return true;
    }
    
    @Override
    protected void handleApprovalCompleted(ApprovalInstance instance, ApprovalStatusEnum finalStatus) {
        log.info("费用审批完成回调: 实例ID={}, 业务ID={}, 状态={}", 
                instance.getId(), instance.getBusinessId(), finalStatus);
        
        try {
            // 更新费用申请单状态
            updateExpenseApplyStatus(instance.getBusinessId(), finalStatus);
            
            // 记录审批完成日志
            logApprovalCompletion(instance, finalStatus);
            
            // 发送通知
            sendApprovalNotification(instance, finalStatus);
            
        } catch (Exception e) {
            log.error("费用审批完成回调处理异常", e);
            // 这里可以选择重试或记录错误，但不应该中断主流程
        }
    }
    
    @Override
    protected void handleApprovalProgress(ApprovalInstance instance, Map<String, Object> context) {
        log.debug("费用审批进度回调: 实例ID={}, 业务ID={}, 当前节点={}", 
                instance.getId(), instance.getBusinessId(), instance.getCurrentNode());
        
        // 记录进度日志
        logProgress(instance);
        
        // 推送进度通知（可选）
        pushProgressNotification(instance);
    }
    
    @Override
    protected Map<String, Object> buildApprovalContext(ApprovalRequest request) {
        Map<String, Object> context = new HashMap<>();
        
        try {
            // 获取费用申请详细数据
            Long expenseId = Long.valueOf(request.getBusinessId());
            var expenseMain = expenseApplyMainRepository.findById(expenseId).orElse(null);
            
            if (expenseMain != null) {
                // 添加费用相关的业务数据到上下文
                context.put("expenseAmount", expenseMain.getApplyAmount());
                context.put("expenseType", expenseMain.getExpenseType());
                context.put("applyUserName", expenseMain.getApplyUserName());
                context.put("department", expenseMain.getDepartment());
                context.put("applyDate", expenseMain.getApplyDate());
                
                // 计算详情统计
                var details = expenseApplyDetailRepository.findByExpenseApplyId(expenseId);
                context.put("detailCount", details.size());
                context.put("categories", details.stream().map(d -> d.getCategory()).distinct().toList());
            }
            
            // 添加费用审批的特殊业务规则
            context.put("requireAttachment", request.getAmount() != null && request.getAmount() > 1000);
            context.put("urgentLevel", determineUrgency(expenseMain));
            context.put("budgetControl", checkBudgetControl(expenseMain));
            
        } catch (Exception e) {
            log.error("构建费用审批上下文异常", e);
        }
        
        return context;
    }
    
    @Override
    protected boolean preMatchFlowConfig(ApprovalRequest request, Map<String, Object> context) {
        // 检查金额阈值，决定流程路径
        if (request.getAmount() != null) {
            if (request.getAmount() < 1000) {
                // 小额审批，简化流程
                context.put("simplifiedFlow", true);
            } else if (request.getAmount() > 10000) {
                // 大额审批，加强流程
                context.put("enhancedApproval", true);
                context.put("requireVPApproval", true);
            }
        }
        
        // 检查时间因素（如月末、季度末）
        context.put("endOfMonth", isEndOfMonth());
        context.put("endOfQuarter", isEndOfQuarter());
        
        return true;
    }
    
    @Override
    protected ApprovalFlowConfig getDefaultFlowConfig() {
        // 返回默认的费用审批流程配置
        log.info("使用默认费用审批流程配置");
        
        // 这里可以查询数据库中的默认配置，或返回预设的配置
        return approvalFlowService.getDefaultFlowConfig(getBusinessType());
    }
    
    @Override
    protected Long getCurrentUserId() {
        // TODO: 根据认证系统获取当前用户ID
        // 暂时返回模拟值
        return 1L;
    }
    
    // 费用特定的业务方法
    
    /**
     * 执行费用特定的验证
     */
    private boolean performExpenseSpecificValidation(ApprovalRequest request, Map<String, Object> context) {
        try {
            Long expenseId = Long.valueOf(request.getBusinessId());
            var expenseMain = expenseApplyMainRepository.findById(expenseId).orElse(null);
            
            if (expenseMain == null) {
                log.error("费用申请主数据不存在: ID={}", expenseId);
                return false;
            }
            
            // 验证费用类型是否合法
            if (expenseMain.getExpenseType() == null || expenseMain.getExpenseType().trim().isEmpty()) {
                log.error("费用类型为空: ID={}", expenseId);
                return false;
            }
            
            // 验证金额是否合理
            if (expenseMain.getApplyAmount() == null || expenseMain.getApplyAmount().compareTo(BigDecimal.ZERO) <= 0) {
                log.error("费用金额不合法: ID={}, 金额={}", expenseId, expenseMain.getApplyAmount());
                return false;
            }
            
            // 验证申请状态
            if (expenseMain.getApplyStatus() != null && expenseMain.getApplyStatus().equals("已提交")) {
                log.error("费用申请已提交，不能重复审批: ID={}", expenseId);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("费用特定验证异常", e);
            return false;
        }
    }
    
    /**
     * 更新费用申请状态
     */
    private void updateExpenseApplyStatus(String businessId, ApprovalStatusEnum status) {
        try {
            Long expenseId = Long.valueOf(businessId);
            var expenseMain = expenseApplyMainRepository.findById(expenseId).orElse(null);
            
            if (expenseMain != null) {
                // 根据审批结果更新状态
                String expenseStatus = convertApprovalStatusToExpenseStatus(status);
                expenseMain.setApplyStatus(expenseStatus);
                
                // TODO: 设置审批完成时间等字段
                expenseApplyMainRepository.save(expenseMain);
                
                log.info("费用申请状态更新成功: ID={}, 新状态={}", expenseId, expenseStatus);
            }
        } catch (Exception e) {
            log.error("更新费用申请状态异常", e);
        }
    }
    
    /**
     * 记录审批完成日志
     */
    private void logApprovalCompletion(ApprovalInstance instance, ApprovalStatusEnum finalStatus) {
        // 这里可以实现详细的日志记录
        log.info("费用审批完成日志: 实例ID={}, 业务ID={}, 状态={}, 完成时间={}", 
                instance.getId(), instance.getBusinessId(), finalStatus, instance.getFinishTime());
    }
    
    /**
     * 发送审批通知
     */
    private void sendApprovalNotification(ApprovalInstance instance, ApprovalStatusEnum finalStatus) {
        // TODO: 集成消息通知系统
        log.info("费用审批通知: 业务ID={}, 状态={}", instance.getBusinessId(), finalStatus);
    }
    
    /**
     * 记录进度日志
     */
    private void logProgress(ApprovalInstance instance) {
        log.debug("费用审批进度: 实例ID={}, 当前节点={}, 进度={}/{}", 
                instance.getId(), instance.getCurrentNode(), 
                instance.getProcessedNodes(), instance.getTotalNodes());
    }
    
    /**
     * 推送进度通知
     */
    private void pushProgressNotification(ApprovalInstance instance) {
        // TODO: 实现进度通知推送
        log.debug("费用审批进度推送: 实例ID={}", instance.getId());
    }
    
    /**
     * 确定紧急程度
     */
    private String determineUrgency(Object expenseMain) {
        // TODO: 根据业务规则确定紧急程度
        return "普通";
    }
    
    /**
     * 检查预算控制
     */
    private boolean checkBudgetControl(Object expenseMain) {
        // TODO: 实现预算控制检查
        return true;
    }
    
    /**
     * 是否月末
     */
    private boolean isEndOfMonth() {
        // TODO: 实现月末判断
        return false;
    }
    
    /**
     * 是否季末
     */
    private boolean isEndOfQuarter() {
        // TODO: 实现季末判断
        return false;
    }
    
    /**
     * 转换审批状态到费用申请状态
     */
    private String convertApprovalStatusToExpenseStatus(ApprovalStatusEnum approvalStatus) {
        switch (approvalStatus) {
            case APPROVED:
                return "审批通过";
            case REJECTED:
                return "审批拒绝";
            case CANCELED:
                return "已取消";
            case TIMEOUT:
                return "审批超时";
            default:
                return "审批中";
        }
    }
}