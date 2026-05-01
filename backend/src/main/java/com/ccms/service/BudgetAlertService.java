package com.ccms.service;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 预算报警服务接口
 * 提供预算使用情况监控、预警和报警功能
 * 
 * @author 系统生成
 */
@Service
public interface BudgetAlertService {

    /**
     * 检查预算明细是否达到预警阈值
     * 
     * @param budgetDetail 预算明细
     * @param warningThreshold 预警阈值（百分比，如80表示80%）
     * @return 是否达到预警
     */
    boolean checkBudgetWarning(BudgetDetail budgetDetail, int warningThreshold);

    /**
     * 检查预算明细是否超支
     * 
     * @param budgetDetail 预算明细
     * @return 是否超支
     */
    boolean checkBudgetOverrun(BudgetDetail budgetDetail);

    /**
     * 获取预算主表的预警信息
     * 
     * @param budgetMain 预算主表
     * @return 预警信息列表
     */
    List<BudgetAlertInfo> getBudgetMainAlerts(BudgetMain budgetMain);

    /**
     * 检查部门所有预算的预警状态
     * 
     * @param deptId 部门ID
     * @param budgetYear 预算年份
     * @return 部门预算预警统计
     */
    DeptBudgetAlertSummary checkDeptBudgetAlerts(Long deptId, Integer budgetYear);

    /**
     * 发送预算预警通知
     * 
     * @param alertInfo 预警信息
     * @return 是否发送成功
     */
    boolean sendBudgetAlertNotification(BudgetAlertInfo alertInfo);

    /**
     * 批量检查预算预警
     * 
     * @param budgetDetailIds 预算明细ID列表
     * @param warningThreshold 预警阈值
     * @return 预警结果
     */
    List<BudgetAlertResult> batchCheckBudgetAlerts(List<Long> budgetDetailIds, int warningThreshold);

    /**
     * 预算预警信息类
     */
    class BudgetAlertInfo {
        private final BudgetDetail budgetDetail;
        private final String alertType; // "WARNING", "OVERRUN"
        private final String message;
        private final BigDecimal currentPercentage;

        public BudgetAlertInfo(BudgetDetail budgetDetail, String alertType, String message, BigDecimal currentPercentage) {
            this.budgetDetail = budgetDetail;
            this.alertType = alertType;
            this.message = message;
            this.currentPercentage = currentPercentage;
        }

        public BudgetDetail getBudgetDetail() { return budgetDetail; }
        public String getAlertType() { return alertType; }
        public String getMessage() { return message; }
        public BigDecimal getCurrentPercentage() { return currentPercentage; }
    }

    /**
     * 部门预算预警统计类
     */
    class DeptBudgetAlertSummary {
        private final Long deptId;
        private final Integer budgetYear;
        private final int totalBudgets;
        private final int warningCount;
        private final int overrunCount;
        private final List<BudgetAlertInfo> alerts;

        public DeptBudgetAlertSummary(Long deptId, Integer budgetYear, int totalBudgets, 
                                      int warningCount, int overrunCount, List<BudgetAlertInfo> alerts) {
            this.deptId = deptId;
            this.budgetYear = budgetYear;
            this.totalBudgets = totalBudgets;
            this.warningCount = warningCount;
            this.overrunCount = overrunCount;
            this.alerts = alerts;
        }

        public Long getDeptId() { return deptId; }
        public Integer getBudgetYear() { return budgetYear; }
        public int getTotalBudgets() { return totalBudgets; }
        public int getWarningCount() { return warningCount; }
        public int getOverrunCount() { return overrunCount; }
        public List<BudgetAlertInfo> getAlerts() { return alerts; }
    }

    /**
     * 预算预警结果类
     */
    class BudgetAlertResult {
        private final Long budgetDetailId;
        private final boolean hasWarning;
        private final boolean hasOverrun;
        private final BigDecimal usedPercentage;

        public BudgetAlertResult(Long budgetDetailId, boolean hasWarning, boolean hasOverrun, BigDecimal usedPercentage) {
            this.budgetDetailId = budgetDetailId;
            this.hasWarning = hasWarning;
            this.hasOverrun = hasOverrun;
            this.usedPercentage = usedPercentage;
        }

        public Long getBudgetDetailId() { return budgetDetailId; }
        public boolean isHasWarning() { return hasWarning; }
        public boolean isHasOverrun() { return hasOverrun; }
        public BigDecimal getUsedPercentage() { return usedPercentage; }
    }
}