package com.ccms.service.impl;

import com.ccms.entity.budget.BudgetMain;
import com.ccms.entity.budget.BudgetDetail;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.service.BudgetAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 预算报警服务实现类
 * 实现预算使用情况监控、预警和报警功能
 * 
 * @author 系统生成
 */
@Service
public class BudgetAlertServiceImpl implements BudgetAlertService {

    private static final Logger logger = LoggerFactory.getLogger(BudgetAlertServiceImpl.class);

    private final BudgetMainRepository budgetMainRepository;
    private final BudgetDetailRepository budgetDetailRepository;

    @Autowired
    public BudgetAlertServiceImpl(BudgetMainRepository budgetMainRepository,
                                BudgetDetailRepository budgetDetailRepository) {
        this.budgetMainRepository = budgetMainRepository;
        this.budgetDetailRepository = budgetDetailRepository;
    }

    @Override
    public boolean checkBudgetWarning(BudgetDetail budgetDetail, int warningThreshold) {
        if (budgetDetail == null || budgetDetail.getBudgetAmount() == null) {
            return false;
        }

        // 计算使用百分比
        BigDecimal usedPercentage = calculateUsedPercentage(budgetDetail);
        
        boolean isWarning = usedPercentage.compareTo(new BigDecimal(warningThreshold)) >= 0;
        
        if (isWarning) {
            logger.warn("预算预警：预算明细ID={}，当前使用率={}%，预警阈值={}%", 
                      budgetDetail.getId(), usedPercentage.setScale(2, RoundingMode.HALF_UP), warningThreshold);
        }
        
        return isWarning;
    }

    @Override
    public boolean checkBudgetOverrun(BudgetDetail budgetDetail) {
        if (budgetDetail == null || budgetDetail.getBudgetAmount() == null) {
            return false;
        }

        // 计算使用百分比
        BigDecimal usedPercentage = calculateUsedPercentage(budgetDetail);
        
        boolean isOverrun = usedPercentage.compareTo(new BigDecimal(100)) > 0;
        
        if (isOverrun) {
            logger.error("预算超支：预算明细ID={}，当前使用率={}%", 
                       budgetDetail.getId(), usedPercentage.setScale(2, RoundingMode.HALF_UP));
        }
        
        return isOverrun;
    }

    @Override
    public List<BudgetAlertInfo> getBudgetMainAlerts(BudgetMain budgetMain) {
        List<BudgetAlertInfo> alerts = new ArrayList<>();
        
        if (budgetMain == null) {
            return alerts;
        }

        // 获取预算主表的所有明细
        List<BudgetDetail> details = budgetDetailRepository.findByBudgetId(budgetMain.getId());
        
        for (BudgetDetail detail : details) {
            // 检查预警
            if (checkBudgetWarning(detail, 80)) {
                BigDecimal usedPercentage = calculateUsedPercentage(detail);
                String message = String.format("预算明细[%s]使用率已达%.2f%%，请关注预算情况",
                    detail.getExpenseTypeName() != null ? detail.getExpenseTypeName() : "未知类型",
                    usedPercentage);
                
                alerts.add(new BudgetAlertInfo(detail, "WARNING", message, usedPercentage));
            }
            
            // 检查超支
            if (checkBudgetOverrun(detail)) {
                BigDecimal usedPercentage = calculateUsedPercentage(detail);
                String message = String.format("预算明细[%s]已超支，使用率%.2f%%",
                    detail.getExpenseTypeName() != null ? detail.getExpenseTypeName() : "未知类型",
                    usedPercentage);
                
                alerts.add(new BudgetAlertInfo(detail, "OVERRUN", message, usedPercentage));
            }
        }
        
        logger.info("预算主表ID={}的预警检查完成，发现{}条预警信息", budgetMain.getId(), alerts.size());
        
        return alerts;
    }

    @Override
    public DeptBudgetAlertSummary checkDeptBudgetAlerts(Long deptId, Integer budgetYear) {
        List<BudgetAlertInfo> allAlerts = new ArrayList<>();
        int totalBudgets = 0;
        int warningCount = 0;
        int overrunCount = 0;

        if (deptId == null || budgetYear == null) {
            return new DeptBudgetAlertSummary(deptId, budgetYear, totalBudgets, warningCount, overrunCount, allAlerts);
        }

        // 查询部门在指定年份的所有预算主表
        List<BudgetMain> budgetMains = budgetMainRepository.findByDeptIdAndBudgetYear(deptId, budgetYear);
        totalBudgets = budgetMains.size();

        for (BudgetMain budgetMain : budgetMains) {
            List<BudgetAlertInfo> mainAlerts = getBudgetMainAlerts(budgetMain);
            allAlerts.addAll(mainAlerts);
            
            for (BudgetAlertInfo alert : mainAlerts) {
                if ("WARNING".equals(alert.getAlertType())) {
                    warningCount++;
                } else if ("OVERRUN".equals(alert.getAlertType())) {
                    overrunCount++;
                }
            }
        }

        logger.info("部门ID={}在{}年的预算预警检查完成，总预算数={}，预警数={}，超支数={}", 
                   deptId, budgetYear, totalBudgets, warningCount, overrunCount);

        return new DeptBudgetAlertSummary(deptId, budgetYear, totalBudgets, warningCount, overrunCount, allAlerts);
    }

    @Override
    public boolean sendBudgetAlertNotification(BudgetAlertInfo alertInfo) {
        if (alertInfo == null) {
            return false;
        }

        // 实际应用中这里应该集成邮件、短信、消息推送等服务
        // 这里简化实现，只记录日志
        
        logger.warn("发送预算预警通知：类型={}，消息={}，使用率={}%", 
                   alertInfo.getAlertType(),
                   alertInfo.getMessage(),
                   alertInfo.getCurrentPercentage().setScale(2, RoundingMode.HALF_UP));

        // 模拟发送成功
        boolean sentSuccessfully = true;
        
        if (sentSuccessfully) {
            logger.info("预算预警通知发送成功：预算明细ID={}", alertInfo.getBudgetDetail().getId());
        }
        
        return sentSuccessfully;
    }

    @Override
    public List<BudgetAlertResult> batchCheckBudgetAlerts(List<Long> budgetDetailIds, int warningThreshold) {
        List<BudgetAlertResult> results = new ArrayList<>();
        
        if (budgetDetailIds == null || budgetDetailIds.isEmpty()) {
            return results;
        }

        for (Long budgetDetailId : budgetDetailIds) {
            Optional<BudgetDetail> detailOpt = budgetDetailRepository.findById(budgetDetailId);
            if (detailOpt.isPresent()) {
                BudgetDetail detail = detailOpt.get();
                BigDecimal usedPercentage = calculateUsedPercentage(detail);
                boolean hasWarning = checkBudgetWarning(detail, warningThreshold);
                boolean hasOverrun = checkBudgetOverrun(detail);
                
                results.add(new BudgetAlertResult(budgetDetailId, hasWarning, hasOverrun, usedPercentage));
            }
        }

        logger.info("批量预算预警检查完成，检查{}个预算明细，返回{}个结果", budgetDetailIds.size(), results.size());
        
        return results;
    }

    /**
     * 计算预算明细的使用百分比
     * 
     * @param budgetDetail 预算明细
     * @return 使用百分比
     */
    private BigDecimal calculateUsedPercentage(BudgetDetail budgetDetail) {
        if (budgetDetail.getBudgetAmount() == null || budgetDetail.getBudgetAmount().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalUsed = budgetDetail.getUsedAmount() != null ? budgetDetail.getUsedAmount() : BigDecimal.ZERO;
        
        // 使用率 = (已用金额 / 预算金额) × 100
        BigDecimal percentage = totalUsed
            .multiply(new BigDecimal(100))
            .divide(budgetDetail.getBudgetAmount(), 2, RoundingMode.HALF_UP);
        
        return percentage;
    }

    /**
     * 定期预算监控任务（可配合Spring的定时任务使用）
     * 
     * @param warningThreshold 预警阈值
     */
    public void performScheduledBudgetMonitoring(int warningThreshold) {
        logger.info("开始执行定期预算监控任务，预警阈值={}%", warningThreshold);
        
        // 查询所有活动的预算主表（状态为已发布）
        List<BudgetMain> activeBudgets = budgetMainRepository.findByStatus(3); // 3表示已发布
        
        for (BudgetMain budgetMain : activeBudgets) {
            List<BudgetAlertInfo> alerts = getBudgetMainAlerts(budgetMain);
            
            for (BudgetAlertInfo alert : alerts) {
                // 发送通知
                sendBudgetAlertNotification(alert);
                
                // 在实际应用中，这里可以记录到数据库或发送给相关人员
                logger.info("检测到预算预警：预算主表ID={}，明细ID={}，类型={}", 
                           budgetMain.getId(), alert.getBudgetDetail().getId(), alert.getAlertType());
            }
        }
        
        logger.info("定期预算监控任务完成，检查了{}个预算主表", activeBudgets.size());
    }

    /**
     * 获取预算明细的详细信息（包含预警状态）
     * 
     * @param budgetDetail 预算明细
     * @param warningThreshold 预警阈值
     * @return 详细信息Map
     */
    public java.util.Map<String, Object> getBudgetDetailWithAlerts(BudgetDetail budgetDetail, int warningThreshold) {
        if (budgetDetail == null) {
            return java.util.Map.of("error", "预算明细为空");
        }

        BigDecimal usedPercentage = calculateUsedPercentage(budgetDetail);
        boolean hasWarning = checkBudgetWarning(budgetDetail, warningThreshold);
        boolean hasOverrun = checkBudgetOverrun(budgetDetail);
        BigDecimal availableAmount = budgetDetail.getBudgetAmount()
            .subtract(budgetDetail.getUsedAmount() != null ? budgetDetail.getUsedAmount() : BigDecimal.ZERO)
            .subtract(budgetDetail.getFrozenAmount() != null ? budgetDetail.getFrozenAmount() : BigDecimal.ZERO);

        return java.util.Map.of(
            "budgetDetail", budgetDetail,
            "usedPercentage", usedPercentage,
            "availableAmount", availableAmount,
            "hasWarning", hasWarning,
            "hasOverrun", hasOverrun,
            "warningThreshold", warningThreshold
        );
    }
}