package com.ccms.service;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.expense.ExpenseApplyMain;
import com.ccms.entity.expense.ExpenseApplyDetail;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.BusinessType;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 费用申请服务接口
 * 集成审批流程引擎，为费用申请业务提供完整的审批支持
 */
public interface ExpenseApplyService {
    
    /**
     * 创建费用申请
     */
    ExpenseApplyMain createExpenseApply(ExpenseApplyMain expenseApply);
    
    /**
     * 更新费用申请
     */
    ExpenseApplyMain updateExpenseApply(ExpenseApplyMain expenseApply);
    
    /**
     * 提交费用申请审批
     */
    ApprovalInstance submitForApproval(Long applyId, Long applicantId, String title);
    
    /**
     * 撤回费用申请
     */
    void withdrawExpenseApply(Long applyId);
    
    /**
     * 根据ID获取费用申请
     */
    ExpenseApplyMain getExpenseApplyById(Long applyId);
    
    /**
     * 根据用户ID获取费用申请列表
     */
    List<ExpenseApplyMain> getExpenseAppliesByUser(Long userId);
    
    /**
     * 添加费用申请明细
     */
    ExpenseApplyDetail addExpenseDetail(ExpenseApplyDetail detail);
    
    /**
     * 更新费用申请明细
     */
    ExpenseApplyDetail updateExpenseDetail(ExpenseApplyDetail detail);
    
    /**
     * 删除费用申请明细
     */
    void deleteExpenseDetail(Long detailId);
    
    /**
     * 获取费用申请的所有明细
     */
    List<ExpenseApplyDetail> getExpenseDetails(Long applyId);
    
    /**
     * 计算费用申请的总金额
     */
    BigDecimal calculateTotalAmount(Long applyId);
    
    /**
     * 审批费用申请
     */
    boolean approveExpenseApply(Long applyId, Long approverId, ApprovalAction action, String comment);
    
    /**
     * 获取费用申请的审批状态
     */
    ApprovalStatus getApprovalStatus(Long applyId);
    
    /**
     * 获取费用申请的审批记录
     */
    List<Map<String, Object>> getApprovalRecords(Long applyId);
    
    /**
     * 根据状态筛选费用申请
     */
    List<ExpenseApplyMain> getExpenseAppliesByStatus(Integer status, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取费用申请统计信息
     */
    Map<String, Object> getExpenseApplyStatistics(Long userId, Long deptId, Integer year);
    
    /**
     * 检查费用申请是否在审批中
     */
    boolean isUnderApproval(Long applyId);
    
    /**
     * 处理审批完成回调
     */
    void onApprovalCompleted(Long applyId, ApprovalStatus finalStatus);
    
    /**
     * 检查用户是否可以创建费用申请
     */
    boolean canUserCreateApply(Long userId);
    
    /**
     * 获取费用申请分页列表
     */
    Page<ExpenseApplyMain> getExpenseApplyPage(int pageNum, int pageSize, Long userId, Long deptId, Integer status);
    
    /**
     * 费用申请业务审批实现
     */
    class ExpenseApplyApprovalService extends BaseApprovalService {
        private final ExpenseApplyService expenseApplyService;
        
        public ExpenseApplyApprovalService(ApprovalFlowService approvalFlowService, ExpenseApplyService expenseApplyService) {
            super();
            this.expenseApplyService = expenseApplyService;
        }
        
        @Override
        public BusinessType getBusinessType() {
            return BusinessType.EXPENSE_APPLY;
        }
        
        @Override
        protected boolean validateBeforeCreate(ApprovalRequest request, Map<String, Object> context) {
            ExpenseApplyMain apply = expenseApplyService.getExpenseApplyById(Long.parseLong(request.getBusinessId()));
            return apply != null && !expenseApplyService.isUnderApproval(Long.parseLong(request.getBusinessId()));
        }
        
        @Override
        protected void handleApprovalCompleted(ApprovalInstance instance, ApprovalStatus finalStatus) {
            expenseApplyService.onApprovalCompleted(Long.parseLong(instance.getBusinessId()), finalStatus);
        }
        
        @Override
        protected void handleApprovalProgress(ApprovalInstance instance, Map<String, Object> context) {
            // 审批进度处理，可发送通知等
        }
        
        @Override
        protected Map<String, Object> buildApprovalContext(ApprovalRequest request) {
            ExpenseApplyMain apply = expenseApplyService.getExpenseApplyById(Long.parseLong(request.getBusinessId()));
            return Map.of(
                "amount", apply.getTotalAmount(),
                "userId", apply.getApplicantId(),
                "deptId", apply.getDeptId(),
                "applyType", apply.getApplyType()
            );
        }
        
        @Override
        protected boolean preMatchFlowConfig(ApprovalRequest request, Map<String, Object> context) {
            return true;
        }
        
        @Override
        protected ApprovalFlowConfig getDefaultFlowConfig() {
            // 返回费用申请默认流程配置
            return approvalFlowService.getDefaultFlowConfig(BusinessType.EXPENSE_APPLY);
        }
        
        @Override
        protected Long getCurrentUserId() {
            // 从认证上下文中获取当前用户ID
            // 这里需要根据实际认证系统实现
            return 1L; // 临时返回固定值
        }
    }
}