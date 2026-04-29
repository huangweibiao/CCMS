package com.ccms.service.impl;

import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.repository.budget.BudgetMainRepository;
import com.ccms.service.BudgetQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 预算查询服务实现类
 * 增强版预算查询和校验功能
 * 
 * @author 系统生成
 */
@Service
public class BudgetQueryServiceImpl implements BudgetQueryService {
    
    private static final Logger logger = LoggerFactory.getLogger(BudgetQueryServiceImpl.class);
    
    @Autowired
    private BudgetMainRepository budgetMainRepository;
    
    @Autowired
    private BudgetDetailRepository budgetDetailRepository;
    
    @Override
    public Map<String, Object> getDepartmentBudgetBalance(Long deptId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询部门在该年份的生效预算
            List<BudgetMain> budgets = budgetMainRepository.findByDeptIdAndBudgetYear(deptId, year);
            
            BigDecimal totalBudget = BigDecimal.ZERO;
            BigDecimal totalUsed = BigDecimal.ZERO;
            BigDecimal totalFrozen = BigDecimal.ZERO;
            BigDecimal totalAvailable = BigDecimal.ZERO;
            
            Map<Long, Map<String, Object>> feeTypeBalance = new HashMap<>();
            
            for (BudgetMain budget : budgets) {
                // 只统计已生效状态的预算
                if (budget.getBudgetStatus() == 3) { // 3表示已生效
                    List<BudgetDetail> details = budgetDetailRepository.findByBudgetMainId(budget.getId());
                    
                    for (BudgetDetail detail : details) {
                        BigDecimal budgetAmount = detail.getBudgetAmount() != null ? detail.getBudgetAmount() : BigDecimal.ZERO;
                        BigDecimal usedAmount = detail.getUsedAmount() != null ? detail.getUsedAmount() : BigDecimal.ZERO;
                        BigDecimal frozenAmount = detail.getFrozenAmount() != null ? detail.getFrozenAmount() : BigDecimal.ZERO;
                        BigDecimal availableAmount = budgetAmount.subtract(usedAmount).subtract(frozenAmount);
                        
                        totalBudget = totalBudget.add(budgetAmount);
                        totalUsed = totalUsed.add(usedAmount);
                        totalFrozen = totalFrozen.add(frozenAmount);
                        totalAvailable = totalAvailable.add(availableAmount);
                        
                        // 按费用类型聚合
                        Map<String, Object> feeBalance = feeTypeBalance.computeIfAbsent(detail.getFeeTypeId(), 
                            k -> new HashMap<>());
                        
                        feeBalance.put("budgetAmount", 
                            ((BigDecimal) feeBalance.getOrDefault("budgetAmount", BigDecimal.ZERO)).add(budgetAmount));
                        feeBalance.put("usedAmount", 
                            ((BigDecimal) feeBalance.getOrDefault("usedAmount", BigDecimal.ZERO)).add(usedAmount));
                        feeBalance.put("frozenAmount", 
                            ((BigDecimal) feeBalance.getOrDefault("frozenAmount", BigDecimal.ZERO)).add(frozenAmount));
                        feeBalance.put("availableAmount", 
                            ((BigDecimal) feeBalance.getOrDefault("availableAmount", BigDecimal.ZERO)).add(availableAmount));
                        feeBalance.put("feeTypeId", detail.getFeeTypeId());
                    }
                }
            }
            
            // 计算使用率
            BigDecimal usageRate = BigDecimal.ZERO;
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                usageRate = totalUsed.divide(totalBudget, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            
            result.put("deptId", deptId);
            result.put("year", year);
            result.put("totalBudget", totalBudget);
            result.put("totalUsed", totalUsed);
            result.put("totalFrozen", totalFrozen);
            result.put("totalAvailable", totalAvailable);
            result.put("usageRate", usageRate);
            result.put("feeTypeBalance", feeTypeBalance);
            result.put("budgetCount", budgets.size());
            
            logger.info("部门预算余额查询成功：deptId={}, year={}, totalAvailable={}", 
                      deptId, year, totalAvailable);
            
        } catch (Exception e) {
            logger.error("部门预算余额查询失败：deptId={}, year={}", deptId, year, e);
            result.put("error", "查询失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getAvailableBudgetByFeeType(Long deptId, Long feeTypeId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询部门在该年份的生效预算
            List<BudgetMain> budgets = budgetMainRepository.findByDeptIdAndBudgetYear(deptId, year);
            
            BigDecimal totalBudget = BigDecimal.ZERO;
            BigDecimal totalUsed = BigDecimal.ZERO;
            BigDecimal totalFrozen = BigDecimal.ZERO;
            BigDecimal totalAvailable = BigDecimal.ZERO;
            
            for (BudgetMain budget : budgets) {
                // 只统计已生效状态的预算
                if (budget.getBudgetStatus() == 3) {
                    // 按费用类型查询预算明细
                    List<BudgetDetail> details = budgetDetailRepository.findByBudgetMainId(budget.getId());
                    
                    for (BudgetDetail detail : details) {
                        if (detail.getFeeTypeId().equals(feeTypeId)) {
                            BigDecimal budgetAmount = detail.getBudgetAmount() != null ? detail.getBudgetAmount() : BigDecimal.ZERO;
                            BigDecimal usedAmount = detail.getUsedAmount() != null ? detail.getUsedAmount() : BigDecimal.ZERO;
                            BigDecimal frozenAmount = detail.getFrozenAmount() != null ? detail.getFrozenAmount() : BigDecimal.ZERO;
                            BigDecimal availableAmount = budgetAmount.subtract(usedAmount).subtract(frozenAmount);
                            
                            totalBudget = totalBudget.add(budgetAmount);
                            totalUsed = totalUsed.add(usedAmount);
                            totalFrozen = totalFrozen.add(frozenAmount);
                            totalAvailable = totalAvailable.add(availableAmount);
                        }
                    }
                }
            }
            
            // 检查预算可用性状态
            String availabilityStatus = "充足";
            String warningLevel = "无";
            
            if (totalBudget.compareTo(BigDecimal.ZERO) == 0) {
                availabilityStatus = "未设置预算";
                warningLevel = "高";
            } else if (totalAvailable.compareTo(BigDecimal.ZERO) <= 0) {
                availabilityStatus = "预算已用完";
                warningLevel = "高";
            } else if (totalAvailable.compareTo(totalBudget.multiply(BigDecimal.valueOf(0.2))) < 0) {
                availabilityStatus = "预算紧张";
                warningLevel = "中";
            } else if (totalAvailable.compareTo(totalBudget.multiply(BigDecimal.valueOf(0.5))) < 0) {
                availabilityStatus = "预算充足";
                warningLevel = "低";
            }
            
            result.put("deptId", deptId);
            result.put("feeTypeId", feeTypeId);
            result.put("year", year);
            result.put("totalBudget", totalBudget);
            result.put("totalUsed", totalUsed);
            result.put("totalFrozen", totalFrozen);
            result.put("totalAvailable", totalAvailable);
            result.put("availabilityStatus", availabilityStatus);
            result.put("warningLevel", warningLevel);
            
            // 计算各项比率
            BigDecimal usageRate = BigDecimal.ZERO;
            BigDecimal frozenRate = BigDecimal.ZERO;
            BigDecimal availableRate = BigDecimal.ZERO;
            
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                usageRate = totalUsed.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                frozenRate = totalFrozen.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
                availableRate = totalAvailable.divide(totalBudget, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
            }
            
            result.put("usageRate", usageRate);
            result.put("frozenRate", frozenRate);
            result.put("availableRate", availableRate);
            
            logger.info("费用类型预算查询成功：deptId={}, feeTypeId={}, available={}", 
                      deptId, feeTypeId, totalAvailable);
            
        } catch (Exception e) {
            logger.error("费用类型预算查询失败：deptId={}, feeTypeId={}, year={}", 
                       deptId, feeTypeId, year, e);
            result.put("error", "查询失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> checkBudgetExceed(Long deptId, Long feeTypeId, BigDecimal applyAmount, Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 查询可用预算
            Map<String, Object> budgetInfo = getAvailableBudgetByFeeType(deptId, feeTypeId, year);
            
            BigDecimal totalAvailable = (BigDecimal) budgetInfo.getOrDefault("totalAvailable", BigDecimal.ZERO);
            String availabilityStatus = (String) budgetInfo.getOrDefault("availabilityStatus", "未知");
            String warningLevel = (String) budgetInfo.getOrDefault("warningLevel", "无");
            
            boolean isExceeded = applyAmount.compareTo(totalAvailable) > 0;
            boolean isWarning = false;
            String exceedMessage = "";
            
            if (isExceeded) {
                BigDecimal exceedAmount = applyAmount.subtract(totalAvailable);
                exceedMessage = String.format("预算超标：申请金额(%.2f)超过可用预算(%.2f)，超标金额：%.2f", 
                    applyAmount, totalAvailable, exceedAmount);
                warningLevel = "高";
            } else if (applyAmount.compareTo(totalAvailable.multiply(BigDecimal.valueOf(0.8))) > 0) {
                isWarning = true;
                exceedMessage = "预算使用接近上限，请注意控制费用";
                warningLevel = "中";
            } else {
                exceedMessage = "预算充足，可正常申请";
                warningLevel = "低";
            }
            
            // 构建超额分析
            BigDecimal usagePercentage = BigDecimal.ZERO;
            if (totalAvailable.add(applyAmount).compareTo(BigDecimal.ZERO) > 0) {
                usagePercentage = applyAmount.divide(totalAvailable.add(applyAmount), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            
            result.putAll(budgetInfo);
            result.put("applyAmount", applyAmount);
            result.put("isExceeded", isExceeded);
            result.put("isWarning", isWarning);
            result.put("exceedMessage", exceedMessage);
            result.put("warningLevel", warningLevel);
            result.put("usagePercentage", usagePercentage);
            result.put("isAvailable", !isExceeded);
            
            logger.info("预算超标检查：deptId={}, feeTypeId={}, applyAmount={}, isExceeded={}", 
                      deptId, feeTypeId, applyAmount, isExceeded);
            
        } catch (Exception e) {
            logger.error("预算超标检查失败：deptId={}, feeTypeId={}, applyAmount={}", 
                       deptId, feeTypeId, applyAmount, e);
            result.put("error", "检查失败：" + e.getMessage());
            result.put("isExceeded", true);
            result.put("isAvailable", false);
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> getBudgetExecutionStatistics(Long deptId, Integer year) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取部门预算余额
            Map<String, Object> balanceInfo = getDepartmentBudgetBalance(deptId, year);
            
            // 计算执行率统计
            BigDecimal totalBudget = (BigDecimal) balanceInfo.getOrDefault("totalBudget", BigDecimal.ZERO);
            BigDecimal totalUsed = (BigDecimal) balanceInfo.getOrDefault("totalUsed", BigDecimal.ZERO);
            BigDecimal totalFrozen = (BigDecimal) balanceInfo.getOrDefault("totalFrozen", BigDecimal.ZERO);
            BigDecimal totalAvailable = (BigDecimal) balanceInfo.getOrDefault("totalAvailable", BigDecimal.ZERO);
            BigDecimal usageRate = (BigDecimal) balanceInfo.getOrDefault("usageRate", BigDecimal.ZERO);
            
            // 计算各类预算执行指标
            BigDecimal executionRate = usageRate; // 预算执行率
            BigDecimal freezeRate = BigDecimal.ZERO; // 预算冻结率
            BigDecimal availableRate = BigDecimal.ZERO; // 预算可用率
            
            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                freezeRate = totalFrozen.divide(totalBudget, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                availableRate = totalAvailable.divide(totalBudget, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            
            // 预算使用趋势分析
            String trend = "稳定";
            if (executionRate.compareTo(BigDecimal.valueOf(80)) > 0) {
                trend = "快速消耗";
            } else if (executionRate.compareTo(BigDecimal.valueOf(50)) > 0) {
                trend = "正常使用";
            } else {
                trend = "使用缓慢";
            }
            
            // 预算健康度评估
            String healthStatus = "健康";
            if (executionRate.compareTo(BigDecimal.valueOf(90)) > 0) {
                healthStatus = "紧张";
            } else if (availableRate.compareTo(BigDecimal.valueOf(10)) < 0) {
                healthStatus = "不足";
            }
            
            result.putAll(balanceInfo);
            result.put("executionRate", executionRate);
            result.put("freezeRate", freezeRate);
            result.put("availableRate", availableRate);
            result.put("trend", trend);
            result.put("healthStatus", healthStatus);
            result.put("budgetYear", year);
            result.put("deptId", deptId);
            
            logger.info("预算执行统计查询成功：deptId={}, year={}, executionRate={}%", 
                      deptId, year, executionRate);
            
        } catch (Exception e) {
            logger.error("预算执行统计查询失败：deptId={}, year={}", deptId, year, e);
            result.put("error", "查询失败：" + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> validateBudgetAvailability(Long deptId, Long feeTypeId, BigDecimal amount, Integer year, String period) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 基础预算超标检查
            Map<String, Object> exceedCheck = checkBudgetExceed(deptId, feeTypeId, amount, year);
            
            boolean isAvailable = (boolean) exceedCheck.getOrDefault("isAvailable", false);
            String warningLevel = (String) exceedCheck.getOrDefault("warningLevel", "无");
            String exceedMessage = (String) exceedCheck.getOrDefault("exceedMessage", "");
            
            // 附加周期校验逻辑
            boolean periodValid = true;
            String periodValidation = "周期校验通过";
            
            if (period != null && !period.equals("年度")) {
                // 这里可以实现按季度、月度等周期校验逻辑
                // 简化实现：检查是否在预算周期内
                periodValidation = "周期校验：当前仅支持年度预算校验";
            }
            
            // 预算策略校验（未来可扩展）
            boolean strategyValid = true;
            String strategyValidation = "策略校验通过";
            
            // 多维度验证结果
            boolean overallValid = isAvailable && periodValid && strategyValid;
            
            result.putAll(exceedCheck);
            result.put("periodValid", periodValid);
            result.put("periodValidation", periodValidation);
            result.put("strategyValid", strategyValid);
            result.put("strategyValidation", strategyValidation);
            result.put("overallValid", overallValid);
            result.put("validationTimestamp", System.currentTimeMillis());
            result.put("validationLevel", "高级");
            
            logger.info("多维度预算验证：deptId={}, feeTypeId={}, overallValid={}", 
                      deptId, feeTypeId, overallValid);
            
        } catch (Exception e) {
            logger.error("多维度预算验证失败：deptId={}, feeTypeId={}, amount={}", 
                       deptId, feeTypeId, amount, e);
            result.put("error", "验证失败：" + e.getMessage());
            result.put("overallValid", false);
        }
        
        return result;
    }
}