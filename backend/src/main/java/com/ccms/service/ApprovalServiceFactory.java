package com.ccms.service;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.BusinessTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 审批服务工厂
 * 统一管理各个业务类型的审批服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceFactory {

    private final ApplicationContext applicationContext;
    private final ApprovalFlowService approvalFlowService;

    /**
     * 根据业务类型获取对应的业务服务
     */
    private Object getBusinessService(BusinessTypeEnum businessType) {
        switch (businessType) {
            case EXPENSE_REIMBURSE:
                return applicationContext.getBean(ExpenseReimburseService.class);
            case LOAN:
                return applicationContext.getBean(LoanService.class);
            case EXPENSE_APPLY:
                // ExpenseApplyService暂时没有实现类，返回null
                try {
                    return applicationContext.getBean("expenseApplyService", Object.class);
                } catch (Exception e) {
                    log.warn("ExpenseApplyService not available for approval: {}", e.getMessage());
                    return null;
                }
            default:
                log.warn("Unsupported business type for approval: {}", businessType);
                return null;
        }
    }

    /**
     * 统一提交业务审批
     */
    public ApprovalInstance submitBusinessApproval(BusinessTypeEnum businessType, 
                                                    String businessId, 
                                                    Long applicantId, 
                                                    String title,
                                                    Map<String, Object> context) {
        log.info("开始提交业务审批: 业务类型={}, 业务ID={}", businessType, businessId);
        
        // 验证业务类型
        if (businessType == null) {
            throw new IllegalArgumentException("业务类型不能为空");
        }
        
        // 构建审批请求
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessId(businessId);
        request.setBusinessType(businessType);
        request.setApplicantId(applicantId);
        request.setTitle(title);
        if (context != null) {
            request.setAmount((BigDecimal) context.get("amount"));
            request.setDepartId((Long) context.get("departId"));
        }
        
        // 根据业务类型调用不同的服务方法
        switch (businessType) {
            case EXPENSE_REIMBURSE:
                ExpenseReimburseService expenseReimburseService = 
                        applicationContext.getBean(ExpenseReimburseService.class);
                return expenseReimburseService.submitExpenseReimburseForApproval(
                        Long.parseLong(businessId), applicantId, title);
                
            case LOAN:
                LoanService loanService = applicationContext.getBean(LoanService.class);
                return loanService.submitForApproval(Long.parseLong(businessId), applicantId, title);
                
            case EXPENSE_APPLY:
                // 费用申请暂不支持
                throw new UnsupportedOperationException("费用申请审批暂未实现");
                
            default:
                throw new IllegalArgumentException("不支持的审批业务类型: " + businessType);
        }
    }

    /**
     * 统一处理审批结果
     */
    public void processApprovalResult(BusinessTypeEnum businessType, 
                                      String businessId, 
                                      ApprovalAction action, 
                                      String remarks) {
        log.info("处理审批结果: 业务类型={}, 业务ID={}, 操作={}", businessType, businessId, action);
        
        // 验证参数
        if (businessType == null || businessId == null || action == null) {
            throw new IllegalArgumentException("业务类型、业务ID和审批操作不能为空");
        }
        
        // 根据业务类型获取审批实例ID
        ApprovalInstance instance = approvalFlowService.getApprovalInstanceByBusinessId(
                businessId, businessType);
        if (instance == null) {
            throw new RuntimeException("找不到对应的审批实例: 业务类型=" + businessType + ", 业务ID=" + businessId);
        }
        
        // 执行业务具体的审批结果处理
        switch (businessType) {
            case EXPENSE_REIMBURSE:
                ExpenseReimburseService expenseReimburseService = 
                        applicationContext.getBean(ExpenseReimburseService.class);
                
                if (action == ApprovalAction.APPROVE || action == ApprovalAction.REJECT) {
                    expenseReimburseService.handleApprovalCallback(
                            instance.getId(), action, remarks);
                }
                break;
                
            case LOAN:
                LoanService loanService = applicationContext.getBean(LoanService.class);
                
                if (action == ApprovalAction.APPROVE || action == ApprovalAction.REJECT) {
                    loanService.handleApprovalCallback(
                            instance.getId(), action, remarks);
                }
                break;
                
            case EXPENSE_APPLY:
                throw new UnsupportedOperationException("费用申请审批回调暂未实现");
                
            default:
                throw new IllegalArgumentException("不支持的审批业务类型: " + businessType);
        }
    }

    /**
     * 统一检查业务审批状态
     */
    public Map<String, Object> checkBusinessApprovalStatus(BusinessTypeEnum businessType, String businessId) {
        log.debug("检查业务审批状态: 业务类型={}, 业务ID={}", businessType, businessId);
        
        try {
            ApprovalInstance instance = approvalFlowService.getApprovalInstanceByBusinessId(
                    businessId, businessType);
            
            if (instance == null) {
                return Map.of(
                        "success", true,
                        "approvalStatus", "NOT_STARTED",
                        "message", "未开始审批"
                );
            }
            
            return Map.of(
                    "success", true,
                    "approvalStatus", instance.getStatus().name(),
                    "instanceId", instance.getId(),
                    "currentNode", instance.getCurrentNode(),
                    "message", "获取审批状态成功"
            );
            
        } catch (Exception e) {
            log.error("检查业务审批状态失败: 业务类型={}, 业务ID={}, 错误={}", 
                    businessType, businessId, e.getMessage(), e);
            
            return Map.of(
                    "success", false,
                    "error", "检查审批状态失败: " + e.getMessage()
            );
        }
    }

    /**
     * 统一撤回业务审批
     */
    public Map<String, Object> withdrawBusinessApproval(BusinessTypeEnum businessType, String businessId, String remarks) {
        log.info("撤回业务审批: 业务类型={}, 业务ID={}", businessType, businessId);
        
        try {
            // 根据业务类型调用不同的撤回方法
            switch (businessType) {
                case EXPENSE_REIMBURSE:
                    ExpenseReimburseService expenseReimburseService = 
                            applicationContext.getBean(ExpenseReimburseService.class);
                    expenseReimburseService.withdrawApproval(Long.parseLong(businessId), remarks);
                    break;
                    
                case LOAN:
                    LoanService loanService = applicationContext.getBean(LoanService.class);
                    loanService.withdrawApproval(Long.parseLong(businessId), remarks);
                    break;
                    
                case EXPENSE_APPLY:
                    throw new UnsupportedOperationException("费用申请审批撤回暂未实现");
                    
                default:
                    throw new IllegalArgumentException("不支持的审批业务类型: " + businessType);
            }
            
            return Map.of(
                    "success", true,
                    "message", "审批撤回成功"
            );
            
        } catch (Exception e) {
            log.error("撤回业务审批失败: 业务类型={}, 业务ID={}, 错误={}", 
                    businessType, businessId, e.getMessage(), e);
            
            return Map.of(
                    "success", false,
                    "error", "撤回审批失败: " + e.getMessage()
            );
        }
    }

    /**
     * 统一获取业务审批记录
     */
    public Map<String, Object> getBusinessApprovalRecords(BusinessTypeEnum businessType, String businessId) {
        log.debug("获取业务审批记录: 业务类型={}, 业务ID={}", businessType, businessId);
        
        try {
            List<ApprovalRecord> records;
            
            // 根据业务类型获取审批记录
            switch (businessType) {
                case EXPENSE_REIMBURSE:
                    ExpenseReimburseService expenseReimburseService = 
                            applicationContext.getBean(ExpenseReimburseService.class);
                    records = expenseReimburseService.getApprovalRecords(Long.parseLong(businessId));
                    break;
                    
                case LOAN:
                    LoanService loanService = applicationContext.getBean(LoanService.class);
                    records = loanService.getApprovalRecords(Long.parseLong(businessId));
                    break;
                    
                case EXPENSE_APPLY:
                    throw new UnsupportedOperationException("费用申请审批记录获取暂未实现");
                    
                default:
                    throw new IllegalArgumentException("不支持的审批业务类型: " + businessType);
            }
            
            return Map.of(
                    "success", true,
                    "records", records,
                    "total", records != null ? records.size() : 0,
                    "message", "获取审批记录成功"
            );
            
        } catch (Exception e) {
            log.error("获取业务审批记录失败: 业务类型={}, 业务ID={}, 错误={}", 
                    businessType, businessId, e.getMessage(), e);
            
            return Map.of(
                    "success", false,
                    "error", "获取审批记录失败: " + e.getMessage()
            );
        }
    }

    /**
     * 检查是否支持特定业务类型的审批
     */
    public boolean isBusinessTypeSupported(BusinessTypeEnum businessType) {
        switch (businessType) {
            case EXPENSE_REIMBURSE:
            case LOAN:
                return true;
            case EXPENSE_APPLY:
            default:
                return false;
        }
    }

    /**
     * 获取所有支持的审批业务类型
     */
    public List<BusinessTypeEnum> getSupportedBusinessTypes() {
        return Arrays.asList(BusinessTypeEnum.EXPENSE_REIMBURSE, BusinessTypeEnum.LOAN);
    }
}