package com.ccms.service.approval;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.expense.ExpenseApply;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批规则匹配器
 * 根据设计文档要求实现动态审批规则匹配算法
 * 
 * @author 系统生成
 */
@Service
public class ApprovalRuleMatcher {

    /**
     * 根据费用申请匹配审批流程配置
     * 
     * @param apply 费用申请对象
     * @param allConfigs 所有可用的审批流程配置
     * @return 匹配的审批流程配置列表
     */
    public List<ApprovalFlowConfig> matchApprovalFlow(ExpenseApply apply, List<ApprovalFlowConfig> allConfigs) {
        return allConfigs.stream()
                .filter(config -> isConfigEnabled(config))
                .filter(config -> matchesBusinessType(config, apply))
                .filter(config -> matchesAmountRange(config, apply))
                .filter(config -> matchesDepartment(config, apply))
                .filter(config -> matchesCostType(config, apply))
                .sorted((c1, c2) -> comparePriority(c1, c2))
                .collect(Collectors.toList());
    }

    /**
     * 检查配置是否启用
     */
    private boolean isConfigEnabled(ApprovalFlowConfig config) {
        return config.getStatus() != null && config.getStatus() == 1;
    }

    /**
     * 匹配业务类型
     */
    private boolean matchesBusinessType(ApprovalFlowConfig config, ExpenseApply apply) {
        String businessType = config.getBusinessType();
        if (businessType == null || businessType.isEmpty()) {
            return true;
        }
        // 根据申请类型匹配业务类型
        // APPLY-费用申请, REIMBURSE-费用报销, LOAN-借款申请
        return "APPLY".equals(businessType);
    }

    /**
     * 匹配金额范围
     */
    private boolean matchesAmountRange(ApprovalFlowConfig config, ExpenseApply apply) {
        if (config.getMinAmount() == null && config.getMaxAmount() == null) {
            return true;
        }
        
        // 获取申请金额
        java.math.BigDecimal applyAmount = apply.getTotalAmount();
        if (applyAmount == null) {
            return false;
        }
        
        // 检查最小金额匹配
        if (config.getMinAmount() != null && applyAmount.compareTo(config.getMinAmount()) < 0) {
            return false;
        }
        
        // 检查最大金额匹配
        if (config.getMaxAmount() != null && applyAmount.compareTo(config.getMaxAmount()) > 0) {
            return false;
        }
        
        return true;
    }

    /**
     * 匹配部门
     */
    private boolean matchesDepartment(ApprovalFlowConfig config, ExpenseApply apply) {
        Long configDeptId = config.getDeptId();
        if (configDeptId == null) {
            return true;
        }
        
        // 获取申请部门的ID
        Long applyDeptId = apply.getDeptId();
        return configDeptId.equals(applyDeptId);
    }

    /**
     * 匹配费用类型
     */
    private boolean matchesCostType(ApprovalFlowConfig config, ExpenseApply apply) {
        Long configFeeTypeId = config.getFeeTypeId();
        if (configFeeTypeId == null) {
            return true;
        }
        
        // 获取申请的费用类型ID
        Long applyFeeTypeId = apply.getFeeTypeId();
        return configFeeTypeId.equals(applyFeeTypeId);
    }

    /**
     * 比较配置优先级
     * 优先级规则：适用范围越具体，优先级越高
     */
    private int comparePriority(ApprovalFlowConfig c1, ApprovalFlowConfig c2) {
        int specificity1 = calculateSpecificity(c1);
        int specificity2 = calculateSpecificity(c2);
        return Integer.compare(specificity2, specificity1);
    }

    /**
     * 计算配置的具体性分数
     * 每个限制条件（部门、费用类型、金额范围）增加分数
     */
    private int calculateSpecificity(ApprovalFlowConfig config) {
        int specificity = 0;
        
        // 部门限制加分
        if (config.getDeptId() != null) {
            specificity += 3;
        }
        
        // 费用类型限制加分
        if (config.getFeeTypeId() != null) {
            specificity += 2;
        }
        
        // 金额范围限制加分
        if (config.getMinAmount() != null || config.getMaxAmount() != null) {
            specificity += 1;
        }
        
        return specificity;
    }

    /**
     * 根据优先级选择最佳匹配的审批流程
     */
    public ApprovalFlowConfig selectBestMatch(List<ApprovalFlowConfig> matchedConfigs) {
        if (matchedConfigs == null || matchedConfigs.isEmpty()) {
            return null;
        }
        
        return matchedConfigs.get(0); // 已经按优先级排序，取第一个
    }

    /**
     * 获取审批流程的审批人列表
     * 根据流程配置JSON解析出审批节点和审批人
     */
    public List<String> extractApprovers(ApprovalFlowConfig config) {
        // TODO: 实现JSON解析逻辑，从flowJson字段解析审批节点和审批人
        // 这里返回示例数据
        return List.of("approver1", "approver2", "approver3");
    }

    /**
     * 获取下一级审批人
     */
    public String getNextApprover(ApprovalFlowConfig config, int currentStep) {
        List<String> approvers = extractApprovers(config);
        if (currentStep >= 0 && currentStep < approvers.size()) {
            return approvers.get(currentStep);
        }
        return null;
    }

    /**
     * 检查审批是否完成
     */
    public boolean isApprovalComplete(ApprovalFlowConfig config, int currentStep) {
        List<String> approvers = extractApprovers(config);
        return currentStep >= approvers.size();
    }
}