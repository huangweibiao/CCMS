package com.ccms.service;

import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;

import java.math.BigDecimal;

/**
 * 预算控制服务接口
 * 提供预算扣减、释放、冻结、解冻等核心控制功能
 * 
 * @author 系统生成
 */
public interface BudgetControlService {
    
    /**
     * 检查预算可用性
     * 
     * @param budgetMain 预算主信息
     * @param budgetDetail 预算明细
     * @param amount 申请金额
     * @return 是否可用
     */
    boolean checkBudgetAvailability(BudgetMain budgetMain, BudgetDetail budgetDetail, BigDecimal amount);
    
    /**
     * 冻结预算金额（预扣减）
     * 
     * @param budgetDetail 预算明细
     * @param amount 冻结金额
     * @return 是否成功
     */
    boolean freezeBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount);
    
    /**
     * 解冻预算金额
     * 
     * @param budgetDetail 预算明细
     * @param amount 解冻金额
     * @return 是否成功
     */
    boolean unfreezeBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount);
    
    /**
     * 实际扣减预算金额
     * 将冻结金额转为已用金额
     * 
     * @param budgetDetail 预算明细
     * @param amount 扣减金额
     * @return 是否成功
     */
    boolean deductBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount);
    
    /**
     * 释放已用预算金额（退回）
     * 
     * @param budgetDetail 预算明细
     * @param amount 释放金额
     * @return 是否成功
     */
    boolean releaseBudgetAmount(BudgetDetail budgetDetail, BigDecimal amount);
    
    /**
     * 预算余额自动调整（用于月末结转等场景）
     * 
     * @param budgetDetail 预算明细
     * @param carryOverAmount 结转金额
     * @return 是否成功
     */
    boolean adjustBudgetBalance(BudgetDetail budgetDetail, BigDecimal carryOverAmount);
}