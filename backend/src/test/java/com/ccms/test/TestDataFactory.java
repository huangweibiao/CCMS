package com.ccms.test;

import com.ccms.entity.budget.BudgetAdjust;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 测试数据工厂类
 * 用于创建各种测试数据
 */
public class TestDataFactory {

    /**
     * 创建预算调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createBudgetAdjust() {
        BudgetAdjust budgetAdjust = new BudgetAdjust();
        budgetAdjust.setId(1L);
        budgetAdjust.setBudgetId(1L);
        budgetAdjust.setBudgetDetailId(2L);
        budgetAdjust.setAdjustNo("BA20250001");
        budgetAdjust.setAdjustType(1); // 1-追加预算
        budgetAdjust.setAdjustAmount(new BigDecimal("5000.00"));
        budgetAdjust.setReason("项目预算不足需要追加预算");
        budgetAdjust.setAdjustBy(1001L);
        budgetAdjust.setApprovalStatus(0); // 0-待提交
        budgetAdjust.setExecuteStatus(0); // 0-未执行
        budgetAdjust.setCreateTime(LocalDateTime.now());
        budgetAdjust.setUpdateTime(LocalDateTime.now());
        
        return budgetAdjust;
    }

    /**
     * 创建已审批的预算调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createApprovedBudgetAdjust() {
        BudgetAdjust budgetAdjust = createBudgetAdjust();
        budgetAdjust.setApprovalStatus(2); // 2-已通过
        budgetAdjust.setApprovalTime(LocalDateTime.now());
        budgetAdjust.setCurrentApproverId(1002L);
        budgetAdjust.setCurrentApproverName("审批人张三");
        return budgetAdjust;
    }

    /**
     * 创建执行中的预算调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createExecutingBudgetAdjust() {
        BudgetAdjust budgetAdjust = createApprovedBudgetAdjust();
        budgetAdjust.setExecuteStatus(1); // 1-执行中
        return budgetAdjust;
    }

    /**
     * 创建执行成功的预算调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createExecutedBudgetAdjust() {
        BudgetAdjust budgetAdjust = createApprovedBudgetAdjust();
        budgetAdjust.setExecuteStatus(2); // 2-执行成功
        budgetAdjust.setExecuteTime(LocalDateTime.now());
        budgetAdjust.setExecuteMsg("预算调整执行成功");
        budgetAdjust.setOriAmount(new BigDecimal("10000.00"));
        budgetAdjust.setAfterAmount(new BigDecimal("15000.00"));
        return budgetAdjust;
    }

    /**
     * 创建拒绝的预算调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createRejectedBudgetAdjust() {
        BudgetAdjust budgetAdjust = createBudgetAdjust();
        budgetAdjust.setApprovalStatus(3); // 3-已驳回
        budgetAdjust.setApprovalTime(LocalDateTime.now());
        budgetAdjust.setCurrentApproverId(1002L);
        budgetAdjust.setCurrentApproverName("审批人李四");
        return budgetAdjust;
    }

    /**
     * 创建预算转移类型的调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createBudgetTransferAdjust() {
        BudgetAdjust budgetAdjust = createBudgetAdjust();
        budgetAdjust.setAdjustType(3); // 3-转移
        budgetAdjust.setSourceBudgetDetailId(2L);
        budgetAdjust.setTargetBudgetDetailId(3L);
        budgetAdjust.setReason("预算转移 - 从A项目转移到B项目");
        return budgetAdjust;
    }

    /**
     * 创建预算调减类型的调整记录
     * 
     * @return BudgetAdjust对象
     */
    public static BudgetAdjust createBudgetReductionAdjust() {
        BudgetAdjust budgetAdjust = createBudgetAdjust();
        budgetAdjust.setAdjustType(2); // 2-调减
        budgetAdjust.setAdjustAmount(new BigDecimal("-2000.00"));
        budgetAdjust.setReason("预算调减 - 项目支出减少");
        return budgetAdjust;
    }
}