package com.ccms.service;

import com.ccms.entity.budget.BudgetDetail;
import com.ccms.entity.budget.BudgetMain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 预算查询服务接口
 * 提供预算余额查询、超标预警等高级查询功能
 * 
 * @author 系统生成
 */
public interface BudgetQueryService {
    
    /**
     * 查询部门预算余额
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 按费用类型分组的预算余额信息
     */
    Map<String, Object> getDepartmentBudgetBalance(Long deptId, Integer year);
    
    /**
     * 按费用类型查询可用预算
     * 
     * @param deptId 部门ID
     * @param feeTypeId 费用类型ID
     * @param year 年份
     * @return 可用预算金额及相关信息
     */
    Map<String, Object> getAvailableBudgetByFeeType(Long deptId, Long feeTypeId, Integer year);
    
    /**
     * 预算超标检查及预警
     * 
     * @param deptId 部门ID
     * @param feeTypeId 费用类型ID
     * @param applyAmount 申请金额
     * @param year 年份
     * @return 检查结果和预警信息
     */
    Map<String, Object> checkBudgetExceed(Long deptId, Long feeTypeId, BigDecimal applyAmount, Integer year);
    
    /**
     * 获取部门预算执行统计
     * 
     * @param deptId 部门ID
     * @param year 年份
     * @return 预算执行情况统计
     */
    Map<String, Object> getBudgetExecutionStatistics(Long deptId, Integer year);
    
    /**
     * 多维度预算可用性校验
     * 
     * @param deptId 部门ID
     * @param feeTypeId 费用类型ID
     * @param amount 申请金额
     * @param year 年份
     * @param period 预算周期
     * @return 校验结果详细信息
     */
    Map<String, Object> validateBudgetAvailability(Long deptId, Long feeTypeId, BigDecimal amount, Integer year, String period);
}