package com.ccms.service;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetAdjust;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算管理服务接口
 * 
 * @author 系统生成
 */
public interface BudgetService {
    
    /**
     * 创建预算
     * 
     * @param budget 预算主信息
     * @return 创建的预算
     */
    BudgetMain createBudget(BudgetMain budget);
    
    /**
     * 更新预算信息
     * 
     * @param budget 预算信息
     * @return 更新后的预算
     */
    BudgetMain updateBudget(BudgetMain budget);
    
    /**
     * 提交预算审批
     * 
     * @param budgetId 预算ID
     */
    void submitBudgetForApproval(Long budgetId);
    
    /**
     * 获取预算详情
     * 
     * @param budgetId 预算ID
     * @return 预算详情
     */
    BudgetMain getBudgetById(Long budgetId);
    
    /**
     * 查询部门预算列表
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 预算列表
     */
    List<BudgetMain> getBudgetsByDeptAndYear(Long deptId, Integer year);
    
    /**
     * 添加预算明细
     * 
     * @param detail 预算明细
     * @return 添加的明细
     */
    BudgetDetail addBudgetDetail(BudgetDetail detail);
    
    /**
     * 更新预算明细
     * 
     * @param detail 预算明细
     * @return 更新后的明细
     */
    BudgetDetail updateBudgetDetail(BudgetDetail detail);
    
    /**
     * 获取预算明细列表
     * 
     * @param budgetId 预算ID
     * @return 明细列表
     */
    List<BudgetDetail> getBudgetDetails(Long budgetId);
    
    /**
     * 申请预算调整
     * 
     * @param adjust 调整申请
     * @return 调整记录
     */
    BudgetAdjust applyBudgetAdjust(BudgetAdjust adjust);
    
    /**
     * 审批预算调整
     * 
     * @param adjustId 调整ID
     * @param approved 是否批准
     * @param comment 审批意见
     */
    void approveBudgetAdjust(Long adjustId, boolean approved, String comment);
    
    /**
     * 获取预算调整记录
     * 
     * @param budgetId 预算ID
     * @return 调整记录列表
     */
    List<BudgetAdjust> getBudgetAdjustments(Long budgetId);
    
    /**
     * 统计部门预算使用情况
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 预算统计信息
     */
    BudgetStatistics getBudgetStatistics(Long deptId, Integer year);
    
    /**
     * 检查预算可用性
     * 
     * @param deptId 部门ID
     * @param expenseType 费用类型
     * @param amount 申请金额
     * @param year 年份
     * @return 是否可用
     */
    boolean checkBudgetAvailability(Long deptId, Integer expenseType, BigDecimal amount, Integer year);
    
    /**
     * 扣减预算
     * 
     * @param deptId 部门ID
     * @param expenseType 费用类型
     * @param amount 扣减金额
     * @param year 年份
     * @return 是否成功
     */
    boolean deductBudget(Long deptId, Integer expenseType, BigDecimal amount, Integer year);
    
    /**
     * 退回预算
     * 
     * @param deptId 部门ID
     * @param expenseType 费用类型
     * @param amount 退回金额
     * @param year 年份
     * @return 是否成功
     */
    boolean returnBudget(Long deptId, Integer expenseType, BigDecimal amount, Integer year);
    
    /**
     * 预算统计信息类
     */
    class BudgetStatistics {
        private final BigDecimal totalBudget;
        private final BigDecimal usedBudget;
        private final BigDecimal remainingBudget;
        private final BigDecimal usageRate;
        
        public BudgetStatistics(BigDecimal totalBudget, BigDecimal usedBudget, BigDecimal remainingBudget, BigDecimal usageRate) {
            this.totalBudget = totalBudget;
            this.usedBudget = usedBudget;
            this.remainingBudget = remainingBudget;
            this.usageRate = usageRate;
        }
        
        // Getters
        public BigDecimal getTotalBudget() { return totalBudget; }
        public BigDecimal getUsedBudget() { return usedBudget; }
        public BigDecimal getRemainingBudget() { return remainingBudget; }
        public BigDecimal getUsageRate() { return usageRate; }
    }
}