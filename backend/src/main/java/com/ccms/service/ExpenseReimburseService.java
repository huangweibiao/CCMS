package com.ccms.service;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ReimburseItem;
import com.ccms.entity.expense.ReimburseAttachment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 费用报销服务接口
 * 
 * @author 系统生成
 */
public interface ExpenseReimburseService {
    
    /**
     * 创建费用报销申请
     * 
     * @param reimburse 费用报销信息
     * @return 创建的费用报销申请
     */
    ExpenseReimburse createExpenseReimburse(ExpenseReimburse reimburse);
    
    /**
     * 更新费用报销申请
     * 
     * @param reimburse 费用报销信息
     * @return 更新后的费用报销申请
     */
    ExpenseReimburse updateExpenseReimburse(ExpenseReimburse reimburse);
    
    /**
     * 提交费用报销审批
     * 
     * @param reimburseId 费用报销ID
     */
    void submitExpenseReimburseForApproval(Long reimburseId);
    
    /**
     * 撤回费用报销申请
     * 
     * @param reimburseId 费用报销ID
     */
    void withdrawExpenseReimburse(Long reimburseId);
    
    /**
     * 根据ID获取费用报销申请
     * 
     * @param reimburseId 费用报销ID
     * @return 费用报销信息
     */
    ExpenseReimburse getExpenseReimburseById(Long reimburseId);
    
    /**
     * 根据用户ID获取费用报销列表
     * 
     * @param userId 用户ID
     * @return 费用报销列表
     */
    List<ExpenseReimburse> getExpenseReimbursesByUser(Long userId);
    
    /**
     * 根据部门获取费用报销列表
     * 
     * @param deptId 部门ID
     * @return 费用报销列表
     */
    List<ExpenseReimburse> getExpenseReimbursesByDept(Long deptId);
    
    /**
     * 添加报销明细项
     * 
     * @param item 报销明细项
     * @return 创建的报销明细项
     */
    ReimburseItem addReimburseItem(ReimburseItem item);
    
    /**
     * 更新报销明细项
     * 
     * @param item 报销明细项
     * @return 更新后的报销明细项
     */
    ReimburseItem updateReimburseItem(ReimburseItem item);
    
    /**
     * 删除报销明细项
     * 
     * @param itemId 报销明细项ID
     */
    void deleteReimburseItem(Long itemId);
    
    /**
     * 获取费用报销的明细项列表
     * 
     * @param reimburseId 费用报销ID
     * @return 报销明细项列表
     */
    List<ReimburseItem> getReimburseItems(Long reimburseId);
    
    /**
     * 添加报销附件
     * 
     * @param attachment 附件信息
     * @return 创建的附件信息
     */
    ReimburseAttachment addReimburseAttachment(ReimburseAttachment attachment);
    
    /**
     * 删除报销附件
     * 
     * @param attachmentId 附件ID
     */
    void deleteReimburseAttachment(Long attachmentId);
    
    /**
     * 获取费用报销的附件列表
     * 
     * @param reimburseId 费用报销ID
     * @return 附件列表
     */
    List<ReimburseAttachment> getReimburseAttachments(Long reimburseId);
    
    /**
     * 计算费用报销的总金额
     * 
     * @param reimburseId 费用报销ID
     * @return 总金额
     */
    BigDecimal calculateTotalAmount(Long reimburseId);
    
    /**
     * 获取待处理的费用报销数量
     * 
     * @param userId 用户ID（审批人）
     * @return 待处理数量
     */
    long getPendingExpenseReimburseCount(Long userId);
    
    /**
     * 根据状态筛选费用报销
     * 
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 费用报销列表
     */
    List<ExpenseReimburse> getExpenseReimbursesByStatus(Integer status, LocalDate startDate, LocalDate endDate);
    
    /**
     * 关联费用申请
     * 
     * @param reimburseId 报销ID
     * @param applyId 申请ID
     */
    void linkExpenseApply(Long reimburseId, Long applyId);
    
    /**
     * 获取关联的费用申请
     * 
     * @param reimburseId 报销ID
     * @return 关联的费用申请ID
     */
    Long getLinkedExpenseApply(Long reimburseId);
    
    /**
     * 处理报销支付
     * 
     * @param reimburseId 报销ID
     * @param paymentMethod 支付方式
     * @param paymentDocNumber 支付凭证号
     */
    void processReimbursePayment(Long reimburseId, Integer paymentMethod, String paymentDocNumber);
    
    /**
     * 费用报销统计信息
     */
    class ExpenseReimburseStatistics {
        private final Long totalCount;
        private final BigDecimal totalAmount;
        private final Long pendingCount;
        private final Long approvedCount;
        private final Long paidCount;
        private final BigDecimal totalPaidAmount;
        
        public ExpenseReimburseStatistics(Long totalCount, BigDecimal totalAmount, 
                                         Long pendingCount, Long approvedCount, 
                                         Long paidCount, BigDecimal totalPaidAmount) {
            this.totalCount = totalCount;
            this.totalAmount = totalAmount;
            this.pendingCount = pendingCount;
            this.approvedCount = approvedCount;
            this.paidCount = paidCount;
            this.totalPaidAmount = totalPaidAmount;
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
        
        public Long getPaidCount() {
            return paidCount;
        }
        
        public BigDecimal getTotalPaidAmount() {
            return totalPaidAmount;
        }
    }
    
    /**
     * 获取费用报销统计信息
     * 
     * @param deptId 部门ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    ExpenseReimburseStatistics getExpenseReimburseStatistics(Long deptId, LocalDate startDate, LocalDate endDate);
}