package com.ccms.service;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.BusinessType;
import com.ccms.enums.BusinessTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 借款服务接口
 */
public interface LoanService {

    /**
     * 申请借款
     */
    LoanMain applyLoan(LoanMain loan);

    /**
     * 更新借款单信息
     */
    LoanMain updateLoan(LoanMain loan);

    /**
     * 根据ID获取借款单
     */
    LoanMain getLoanById(Long id);

    /**
     * 根据借款单号获取借款单
     */
    LoanMain getLoanByLoanNo(String loanNo);

    /**
     * 根据用户ID获取借款列表
     */
    List<LoanMain> getLoansByUserId(Long userId);

    /**
     * 根据用户ID获取借款列表（分页）
     */
    Page<LoanMain> getLoansByUserId(Long userId, Pageable pageable);

    /**
     * 根据状态获取借款列表
     */
    List<LoanMain> getLoansByStatus(Integer status);

    /**
     * 获取逾期借款列表
     */
    List<LoanMain> getOverdueLoans();

    /**
     * 获取用户未还清的借款
     */
    List<LoanMain> getUnpaidLoansByUserId(Long userId);

    /**
     * 计算用户借款总额
     */
    BigDecimal getUserTotalLoanAmount(Long userId);

    /**
     * 计算用户未还余额
     */
    BigDecimal getUserUnpaidBalance(Long userId);

    /**
     * 审批借款申请
     */
    boolean approveLoan(Long loanId, String remark);

    /**
     * 驳回借款申请
     */
    boolean rejectLoan(Long loanId, String remark);

    /**
     * 放款操作
     */
    boolean disburseLoan(Long loanId);

    /**
     * 更新借款状态
     */
    boolean updateLoanStatus(Long loanId, Integer status);

    /**
     * 检查用户是否可以借款
     */
    boolean canUserBorrow(Long userId, BigDecimal amount);

    /**
     * 生成借款单号
     */
    String generateLoanNo();

    /**
     * 统计指定日期范围内的借款金额
     */
    BigDecimal getLoanAmountBetweenDates(LocalDate startDate, LocalDate endDate);
    
    // ========== 审批流程集成方法 ==========
    
    /**
     * 提交借款申请审批
     */
    ApprovalInstance submitForApproval(Long loanId, Long applicantId, String title);
    
    /**
     * 获取借款申请的审批状态
     */
    ApprovalStatus getApprovalStatus(Long loanId);
    
    /**
     * 获取借款申请的审批记录
     */
    List<Map<String, Object>> getApprovalRecords(Long loanId);
    
    /**
     * 审批借款申请（集成审批流程）
     */
    boolean approveLoan(Long loanId, ApprovalAction action, String comment);
    
    /**
     * 检查借款申请是否在审批中
     */
    boolean isUnderApproval(Long loanId);
    
    /**
     * 处理审批完成回调
     */
    void onApprovalCompleted(Long loanId, ApprovalStatus finalStatus);
    
    /**
     * 借款业务审批实现
     */
    class LoanApprovalService extends BaseApprovalService {
        private final LoanService loanService;
        
        public LoanApprovalService(ApprovalFlowService approvalFlowService, LoanService loanService) {
            super(approvalFlowService);
            this.loanService = loanService;
        }
        
        @Override
        public BusinessType getBusinessType() {
            return BusinessType.LOAN;
        }
        
        @Override
        protected boolean validateBeforeCreate(ApprovalRequest request, Map<String, Object> context) {
            LoanMain loan = loanService.getLoanById(Long.parseLong(request.getBusinessId()));
            return loan != null && !loanService.isUnderApproval(Long.parseLong(request.getBusinessId()));
        }
        
        @Override
        protected void handleApprovalCompleted(ApprovalInstance instance, ApprovalStatus finalStatus) {
            loanService.onApprovalCompleted(instance.getBusinessId(), finalStatus);
        }
        
        @Override
        protected void handleApprovalProgress(ApprovalInstance instance, Map<String, Object> context) {
            // 审批进度处理，可发送通知等
        }
        
        @Override
        protected Map<String, Object> buildApprovalContext(ApprovalRequest request) {
            LoanMain loan = loanService.getLoanById(Long.parseLong(request.getBusinessId()));
            return Map.of(
                "amount", loan.getLoanAmount(),
                "userId", loan.getApplicantId(),
                "loanType", loan.getLoanType(),
                "loanPeriod", loan.getLoanPeriod()
            );
        }
        
        @Override
        protected boolean preMatchFlowConfig(ApprovalRequest request, Map<String, Object> context) {
            return true;
        }
        
        @Override
        protected ApprovalFlowConfig getDefaultFlowConfig() {
            // 返回借款业务默认流程配置
            return approvalFlowService.getDefaultFlowConfig(BusinessTypeEnum.fromBusinessType(BusinessType.LOAN));
        }
        
        @Override
        protected Long getCurrentUserId() {
            // 从认证上下文中获取当前用户ID
            // 这里需要根据实际认证系统实现
            return 1L; // 临时返回固定值
        }
    }
}