package com.ccms.service;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseItem;
import com.ccms.entity.expense.ExpenseAttachment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 费用申请服务接口
 * 
 * @author 系统生成
 */
public interface ExpenseApplyService {
    
    /**
     * 创建费用申请
     * 
     * @param apply 费用申请信息
     * @return 创建的费用申请
     */
    ExpenseApply createExpenseApply(ExpenseApply apply);
    
    /**
     * 更新费用申请
     * 
     * @param apply 费用申请信息
     * @return 更新后的费用申请
     */
    ExpenseApply updateExpenseApply(ExpenseApply apply);
    
    /**
     * 提交费用申请审批
     * 
     * @param applyId 费用申请ID
     */
    void submitExpenseApplyForApproval(Long applyId);
    
    /**
     * 撤回费用申请
     * 
     * @param applyId 费用申请ID
     */
    void withdrawExpenseApply(Long applyId);
    
    /**
     * 根据ID获取费用申请
     * 
     * @param applyId 费用申请ID
     * @return 费用申请信息
     */
    ExpenseApply getExpenseApplyById(Long applyId);
    
    /**
     * 根据用户ID获取费用申请列表
     * 
     * @param userId 用户ID
     * @return 费用申请列表
     */
    List<ExpenseApply> getExpenseAppliesByUser(Long userId);
    
    /**
     * 根据部门获取费用申请列表
     * 
     * @param deptId 部门ID
     * @return 费用申请列表
     */
    List<ExpenseApply> getExpenseAppliesByDept(Long deptId);
    
    /**
     * 添加费用申请明细项
     * 
     * @param item 费用明细项
     * @return 创建的费用明细项
     */
    ExpenseItem addExpenseItem(ExpenseItem item);
    
    /**
     * 更新费用申请明细项
     * 
     * @param item 费用明细项
     * @return 更新后的费用明细项
     */
    ExpenseItem updateExpenseItem(ExpenseItem item);
    
    /**
     * 删除费用申请明细项
     * 
     * @param itemId 费用明细项ID
     */
    void deleteExpenseItem(Long itemId);
    
    /**
     * 获取费用申请的明细项列表
     * 
     * @param applyId 费用申请ID
     * @return 费用明细项列表
     */
    List<ExpenseItem> getExpenseItems(Long applyId);
    
    /**
     * 添加费用申请附件
     * 
     * @param attachment 附件信息
     * @return 创建的附件信息
     */
    ExpenseAttachment addExpenseAttachment(ExpenseAttachment attachment);
    
    /**
     * 删除费用申请附件
     * 
     * @param attachmentId 附件ID
     */
    void deleteExpenseAttachment(Long attachmentId);
    
    /**
     * 获取费用申请的附件列表
     * 
     * @param applyId 费用申请ID
     * @return 附件列表
     */
    List<ExpenseAttachment> getExpenseAttachments(Long applyId);
    
    /**
     * 计算费用申请的总金额
     * 
     * @param applyId 费用申请ID
     * @return 总金额
     */
    BigDecimal calculateTotalAmount(Long applyId);
    
    /**
     * 检查预算是否充足
     * 
     * @param deptId 部门ID
     * @param expenseType 费用类型
     * @param amount 金额
     * @param applyDate 申请日期
     * @return 是否充足
     */
    boolean checkBudgetAvailability(Long deptId, Integer expenseType, BigDecimal amount, LocalDate applyDate);
    
    /**
     * 获取待处理的费用申请数量
     * 
     * @param userId 用户ID（审批人）
     * @return 待处理数量
     */
    long getPendingExpenseApplyCount(Long userId);
    
    /**
     * 根据状态筛选费用申请
     * 
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 费用申请列表
     */
    List<ExpenseApply> getExpenseAppliesByStatus(Integer status, LocalDate startDate, LocalDate endDate);
    
    /**
     * 费用申请统计信息
     */
    class ExpenseApplyStatistics {
        private final Long totalCount;
        private final BigDecimal totalAmount;
        private final Long pendingCount;
        private final Long approvedCount;
        private final Long rejectedCount;
        
        public ExpenseApplyStatistics(Long totalCount, BigDecimal totalAmount, 
                                    Long pendingCount, Long approvedCount, Long rejectedCount) {
            this.totalCount = totalCount;
            this.totalAmount = totalAmount;
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.rejectedCount = rejectedCount;
        }
        
        public Long getTotalCount() {
            return totalCount;
        }
        
        public BigDecimal getTotalAmount() {
            return totalAmount;
        }
        
        public Long getPendingCount() {
            return pendingCount;
        }
        
        public Long getApprovedCount() {
            return approvedCount;
        }
        
        public Long getRejectedCount() {
            return rejectedCount;
        }
    }
    
/**
 * 获取费用申请统计信息 (按年份)
 * 
 * @param applicantId 申请人ID
 * @param deptId 部门ID
 * @param year 年份
 * @return 统计信息
 */
Map<String, Object> getExpenseApplyStatistics(Long applicantId, Long deptId, Integer year);

/**
 * 获取费用申请统计信息 (按日期范围)
 * 
 * @param deptId 部门ID
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @return 统计信息
 */
ExpenseApplyStatistics getExpenseApplyStatistics(Long deptId, LocalDate startDate, LocalDate endDate);

// 控制器的额外方法定义

/**
 * 检查权限
 */
boolean checkPermission(String token, String permission);

/**
 * 获取费用申请分页列表
 */
org.springframework.data.domain.Page<ExpenseApply> getExpenseApplyList(int page, int size, Long applicantId, Long deptId, Integer status, Integer year);

/**
 * 删除费用申请
 */
ExpenseApply deleteExpenseApply(Long applyId);

/**
 * 审批费用申请
 */
ExpenseApply approveExpenseApply(Long applyId, Long approverId, Integer status, String comment);

/**
 * 撤回费用申请（带原因）
 */
ExpenseApply withdrawExpenseApply(Long applyId, String reason);

/**
 * 关联报销单
 */
ExpenseApply linkToReimbursement(Long applyId, Long reimburseId);

/**
 * 批量操作
 */
boolean batchOperation(Long[] applyIds, String operation);

/**
 * 获取审批历史
 */
Object getApprovalHistory(Long applyId);

/**
 * 调整费用金额
 */
ExpenseApply adjustExpenseAmount(Long applyId, Double newAmount, String reason);

/**
 * 导出费用申请
 */
Object exportExpenseApplies(java.util.Map<String, Object> exportParams);

/**
 * 简化版预算检查
 */
/**
 * 检查费用申请预算
 * 
 * @param applyId 申请ID
 * @return 预算检查结果
 */
Map<String, Object> checkBudgetAvailability(Long applyId);
}