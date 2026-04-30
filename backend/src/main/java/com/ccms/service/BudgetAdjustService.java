package com.ccms.service;

import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.vo.ResultVO;

import java.math.BigDecimal;

/**
 * 预算调整服务接口
 * 
 * @author 系统生成
 */
public interface BudgetAdjustService {
    
    /**
     * 创建预算调整申请
     * 
     * @param budgetId 预算ID
     * @param budgetDetailId 预算明细ID
     * @param adjustType 调整类型
     * @param adjustAmount 调整金额
     * @param reason 调整原因
     * @param applyUserId 申请人ID
     * @param applyUserName 申请人姓名
     * @return 调整申请结果
     */
    ResultVO<BudgetAdjust> createAdjustApply(Long budgetId, Long budgetDetailId, 
                                            Integer adjustType, BigDecimal adjustAmount,
                                            String reason, Long applyUserId, String applyUserName);
    
    /**
     * 提交调整申请到审批流程
     * 
     * @param adjustId 调整记录ID
     * @param userId 提交人ID
     * @return 提交结果
     */
    ResultVO<BudgetAdjust> submitToApproval(Long adjustId, Long userId);
    
    /**
     * 审批预算调整申请
     * 
     * @param adjustId 调整记录ID
     * @param approveResult 审批结果：true-通过 false-驳回
     * @param approvalComment 审批意见
     * @param approverId 审批人ID
     * @param approverName 审批人姓名
     * @return 审批结果
     */
    ResultVO<BudgetAdjust> approveAdjust(Long adjustId, Boolean approveResult, 
                                        String approvalComment, Long approverId, String approverName);
    
    /**
     * 执行预算调整
     * 
     * @param adjustId 调整记录ID
     * @return 执行结果
     */
    ResultVO<BudgetAdjust> executeAdjust(Long adjustId);
    
    /**
     * 撤销预算调整申请
     * 
     * @param adjustId 调整记录ID
     * @param userId 撤销人ID
     * @param reason 撤销原因
     * @return 撤销结果
     */
    ResultVO<BudgetAdjust> cancelAdjust(Long adjustId, Long userId, String reason);
    
    /**
     * 获取调整记录详情
     * 
     * @param adjustId 调整记录ID
     * @return 调整详情
     */
    BudgetAdjust getAdjustDetail(Long adjustId);
    
    /**
     * 验证预算调整的可行性
     * 
     * @param budgetId 预算ID
     * @param budgetDetailId 预算明细ID
     * @param adjustType 调整类型
     * @param adjustAmount 调整金额
     * @return 验证结果
     */
    ResultVO<Boolean> validateAdjustment(Long budgetId, Long budgetDetailId, 
                                       Integer adjustType, BigDecimal adjustAmount);
}