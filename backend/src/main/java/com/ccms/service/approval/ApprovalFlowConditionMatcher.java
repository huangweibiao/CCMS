package com.ccms.service.approval;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.master.FeeType;
import com.ccms.entity.system.dept.SysDept;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.repository.system.dept.SysDeptRepository;
import com.ccms.repository.master.FeeTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 审批流程条件匹配引擎
 * 实现多维度条件匹配算法（金额区间 + 部门 + 费用类型）
 */
@Service
public class ApprovalFlowConditionMatcher {

    @Autowired
    private ApprovalFlowConfigRepository approvalFlowConfigRepository;

    @Autowired
    private SysDeptRepository sysDeptRepository;

    @Autowired
    private FeeTypeRepository feeTypeRepository;

    /**
     * 查找匹配的审批流配置
     */
    public ApprovalFlowConfig findMatchingFlow(String businessType, BigDecimal amount, 
                                             Long deptId, Long feeTypeId) {
        
        // 获取所有生效的审批流配置
        var flows = approvalFlowConfigRepository.findByBusinessType(Integer.valueOf(businessType));
        
        ApprovalFlowConfig bestMatch = null;
        int highestScore = -1;
        
        for (var flow : flows) {
            int score = calculateMatchScore(flow, amount, deptId, feeTypeId);
            
            if (score > highestScore) {
                highestScore = score;
                bestMatch = flow;
            }
        }
        
        return bestMatch;
    }

    /**
     * 计算匹配分数（0-100）
     */
    private int calculateMatchScore(ApprovalFlowConfig flow, BigDecimal amount, 
                                  Long deptId, Long feeTypeId) {
        int score = 0;
        
        // 金额匹配检查（权重最高）
        score += calculateAmountMatchScore(flow, amount) * 50;
        
        // 部门匹配检查
        score += calculateDeptMatchScore(flow, deptId) * 30;
        
        // 费用类型匹配检查
        score += calculateFeeTypeMatchScore(flow, feeTypeId) * 20;
        
        return score;
    }

    /**
     * 金额匹配分数（0-100）
     */
    private int calculateAmountMatchScore(ApprovalFlowConfig flow, BigDecimal amount) {
        BigDecimal minAmount = flow.getMinAmount();
        BigDecimal maxAmount = flow.getMaxAmount();
        
        // 如果没有设置金额限制，返回中等分数
        if (minAmount == null && maxAmount == null) {
            return 50;
        }
        
        // 检查金额是否在区间内
        boolean inRange = true;
        if (minAmount != null && amount.compareTo(minAmount) < 0) {
            inRange = false;
        }
        if (maxAmount != null && amount.compareTo(maxAmount) > 0) {
            inRange = false;
        }
        
        return inRange ? 100 : 0;
    }

    /**
     * 部门匹配分数（0-100）
     */
    private int calculateDeptMatchScore(ApprovalFlowConfig flow, Long deptId) {
        Long configDeptId = flow.getDeptId();
        
        // 如果没有设置部门限制（适用全部门），返回高分
        if (configDeptId == null) {
            return 70;
        }
        
        // 检查是否匹配特定部门
        if (configDeptId.equals(deptId)) {
            return 100;
        }
        
        // 检查是否匹配下级部门
        if (isSubDepartment(deptId, configDeptId)) {
            return 80;
        }
        
        return 0;
    }

    /**
     * 费用类型匹配分数（0-100）
     */
    private int calculateFeeTypeMatchScore(ApprovalFlowConfig flow, Long feeTypeId) {
        Long configFeeTypeId = flow.getFeeTypeId();
        
        // 如果没有设置费用类型限制，返回高分
        if (configFeeTypeId == null) {
            return 70;
        }
        
        // 检查是否匹配特定费用类型
        if (configFeeTypeId.equals(feeTypeId)) {
            return 100;
        }
        
        return 0;
    }

    /**
     * 检查是否下级部门
     */
    private boolean isSubDepartment(Long checkDeptId, Long parentDeptId) {
        var dept = sysDeptRepository.findById(checkDeptId);
        if (dept.isPresent()) {
            // 递归检查上级部门
            return checkParentHierarchy(dept.get(), parentDeptId);
        }
        return false;
    }

    private boolean checkParentHierarchy(SysDept currentDept, Long targetParentId) {
        if (currentDept.getParentId() == null) {
            return false;
        }
        
        if (currentDept.getParentId().equals(targetParentId)) {
            return true;
        }
        
        var parentDept = sysDeptRepository.findById(currentDept.getParentId());
        if (parentDept.isPresent()) {
            return checkParentHierarchy(parentDept.get(), targetParentId);
        }
        
        return false;
    }
    
    /**
     * 验证审批流配置的有效性
     */
    public boolean validateFlowConfig(ApprovalFlowConfig config) {
        // 检查必填字段
        if (config.getFlowCode() == null || config.getFlowCode().trim().isEmpty()) {
            return false;
        }
        
        if (config.getFlowName() == null || config.getFlowName().trim().isEmpty()) {
            return false;
        }
        
        if (config.getBusinessType() == null || config.getBusinessType().trim().isEmpty()) {
            return false;
        }
        
        // 检查金额区间合理性
        if (config.getMinAmount() != null && config.getMaxAmount() != null) {
            if (config.getMinAmount().compareTo(config.getMaxAmount()) > 0) {
                return false;
            }
        }
        
        // 检查流程定义JSON格式
        if (!validateFlowJson(config.getFlowJson())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 验证流程定义JSON格式
     */
    private boolean validateFlowJson(String flowJson) {
        try {
            // 这里应该有实际的JSON格式验证逻辑
            // 简单检查是否为有效JSON
            if (flowJson == null || flowJson.trim().isEmpty()) {
                return false;
            }
            
            // TODO: 添加更严格的JSON格式验证
            return flowJson.contains("nodes") && flowJson.contains("approvers");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取默认审批流配置（当没有匹配配置时使用）
     */
    public ApprovalFlowConfig getDefaultFlow(String businessType) {
        return approvalFlowConfigRepository
            .findByBusinessTypeAndStatus(Integer.valueOf(businessType), 1)
            .stream()
            .filter(flow -> flow.getMinAmount() == null && 
                           flow.getMaxAmount() == null && 
                           flow.getDeptId() == null && 
                           flow.getFeeTypeId() == null)
            .findFirst()
            .orElse(null);
    }
}