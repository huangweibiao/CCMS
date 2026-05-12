package com.ccms.service;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ReimburseItem;
import com.ccms.entity.expense.ReimburseAttachment;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.BusinessType;
import org.springframework.data.domain.Page;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
     * @param applicantId 申请人ID
     * @param title 审批标题
     * @return 审批实例
     */
    ApprovalInstance submitExpenseReimburseForApproval(Long reimburseId, Long applicantId, String title);
    
    /**
     * 撤回费用报销审批
     * 
     * @param reimburseId 费用报销ID
     * @param remarks 撤回原因
     */
    void withdrawExpenseReimburseApproval(Long reimburseId, String remarks);
    
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
     * 创建费用报销明细
     * 
     * @param detail 费用报销明细
     * @return 创建的明细
     */
    ExpenseReimburseDetail createExpenseReimburseDetail(ExpenseReimburseDetail detail);
    
    /**
     * 更新费用报销明细
     * 
     * @param detail 费用报销明细
     * @return 更新后的明细
     */
    ExpenseReimburseDetail updateExpenseReimburseDetail(ExpenseReimburseDetail detail);
    
    /**
     * 删除费用报销明细
     * 
     * @param detailId 明细ID
     */
    void deleteExpenseReimburseDetail(Long detailId);
    
    /**
     * 获取费用报销的所有明细
     * 
     * @param reimburseId 费用报销ID
     * @return 明细列表
     */
    List<ExpenseReimburseDetail> getExpenseReimburseDetails(Long reimburseId);
    
    /**
     * 创建发票记录
     * 
     * @param invoice 发票信息
     * @return 创建的发票
     */
    ExpenseInvoice createExpenseInvoice(ExpenseInvoice invoice);
    
    /**
     * 更新发票记录
     * 
     * @param invoice 发票信息
     * @return 更新后的发票
     */
    ExpenseInvoice updateExpenseInvoice(ExpenseInvoice invoice);
    
    /**
     * 根据发票号码查询发票
     * 
     * @param invoiceNo 发票号码
     * @return 发票列表
     */
    List<ExpenseInvoice> getExpenseInvoicesByNo(String invoiceNo);
    
    /**
     * 获取费用报销的所有发票
     * 
     * @param reimburseId 费用报销ID
     * @return 发票列表
     */
    List<ExpenseInvoice> getExpenseInvoicesByReimburse(Long reimburseId);
    
    /**
     * 验证发票真伪
     * 
     * @param invoiceId 发票ID
     * @return 验证结果
     */
    boolean verifyExpenseInvoice(Long invoiceId);
    
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
    
    // ========== Controller需要的额外方法 ==========
    
    /**
     * 检查权限
     */
    boolean checkPermission(String token, String permission);
    
    /**
     * 获取费用报销列表
     */
    Page<ExpenseReimburse> getExpenseReimburseList(int pageNum, int pageSize, Long userId, Long deptId, Integer status, Integer paymentStatus);
    
    /**
     * 删除费用报销
     */
    boolean deleteExpenseReimburse(Long reimburseId);
    
    /**
     * 审批费用报销
     */
    boolean approveExpenseReimburse(Long reimburseId, Long approverId, Integer action, String comment);
    
    /**
     * 关联费用申请
     */
    boolean linkToExpenseApply(Long reimburseId, Long applyId);
    
    /**
     * 处理付款
     */
    boolean processPayment(Long reimburseId, Long payerId, String paymentMethod, String voucherNumber);
    
    /**
     * 获取统计信息
     */
    Map<String, Object> getExpenseReimburseStatistics(Long userId, Long deptId, Integer year);
    
    /**
     * 上传凭证
     */
    boolean uploadVoucher(Long reimburseId, String voucherType, String fileName, String fileUrl);
    
    /**
     * 获取凭证下载URL
     */
    String getVoucherDownloadUrl(Long attachmentId);
    
    /**
     * 处理退款
     */
    boolean processRefund(Long reimburseId, Long operatorId, Double refundAmount, String reason);
    
    /**
     * 获取状态跟踪
     */
    List<Map<String, Object>> getStatusTracking(Long reimburseId);
    
    // ========== 审批流程集成方法 ==========
    
    /**
     * 获取费用报销的审批状态
     */
    ApprovalStatus getApprovalStatus(Long reimburseId);
    
    /**
     * 获取费用报销的审批记录
     */
    List<Map<String, Object>> getApprovalRecords(Long reimburseId);
    
    /**
     * 检查费用报销是否在审批中
     */
    boolean isUnderApproval(Long reimburseId);
    
    /**
     * 处理审批完成回调
     */
    void onApprovalCompleted(Long reimburseId, ApprovalStatus finalStatus);
    
    /**
     * 费用报销业务审批实现
     */
    class ExpenseReimburseApprovalService extends BaseApprovalService {
        private final ExpenseReimburseService expenseReimburseService;
        
        public ExpenseReimburseApprovalService(ApprovalFlowService approvalFlowService, ExpenseReimburseService expenseReimburseService) {
            super();
            this.expenseReimburseService = expenseReimburseService;
        }
        
        @Override
        public BusinessType getBusinessType() {
            return BusinessType.EXPENSE_REIMBURSE;
        }
        
        @Override
        protected boolean validateBeforeCreate(ApprovalRequest request, Map<String, Object> context) {
            ExpenseReimburse reimburse = expenseReimburseService.getExpenseReimburseById(Long.parseLong(request.getBusinessId()));
            return reimburse != null && !expenseReimburseService.isUnderApproval(Long.parseLong(request.getBusinessId()));
        }
        
        @Override
        protected void handleApprovalCompleted(ApprovalInstance instance, ApprovalStatus finalStatus) {
            expenseReimburseService.onApprovalCompleted(Long.parseLong(instance.getBusinessId()), finalStatus);
        }
        
        @Override
        protected void handleApprovalProgress(ApprovalInstance instance, Map<String, Object> context) {
            // 审批进度处理，可发送通知等
        }
        
        @Override
        protected Map<String, Object> buildApprovalContext(ApprovalRequest request) {
            ExpenseReimburse reimburse = expenseReimburseService.getExpenseReimburseById(Long.parseLong(request.getBusinessId()));
            return Map.of(
                "amount", expenseReimburseService.calculateTotalAmount(Long.parseLong(request.getBusinessId())),
                "userId", reimburse.getApplicantId(),
                "deptId", reimburse.getDeptId(),
                "reimburseType", reimburse.getReimburseType()
            );
        }
        
        @Override
        protected boolean preMatchFlowConfig(ApprovalRequest request, Map<String, Object> context) {
            return true;
        }
        
        @Override
        protected ApprovalFlowConfig getDefaultFlowConfig() {
            // 返回费用报销默认流程配置
            return approvalFlowService.getDefaultFlowConfig(BusinessType.EXPENSE_REIMBURSE);
        }
        
        @Override
        protected Long getCurrentUserId() {
            // 从认证上下文中获取当前用户ID
            // 这里需要根据实际认证系统实现
            return 1L; // 临时返回固定值
        }
    }
    
    /**
     * 导出费用报销
     */
    byte[] exportExpenseReimburses(Map<String, Object> exportParams);
    
    /**
     * 分析费用报销
     */
    Object analyzeExpenseReimburses(Map<String, Object> analyzeParams);
    
    /**
     * 加急处理
     */
    boolean urgentProcessing(Long reimburseId, Long operatorId, String reason);
}