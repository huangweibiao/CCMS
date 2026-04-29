package com.ccms.service.approval;

import java.math.BigDecimal;

/**
 * 审批人匹配器接口
 */
public interface ApprovalMatcher {
    
    /**
     * 按部门匹配审批人
     */
    Long matchByDepartment(Long deptId, Integer approvalLevel);
    
    /**
     * 按金额级别匹配审批人
     */
    Long matchByAmountLevel(BigDecimal amount, Integer approvalLevel);
    
    /**
     * 匹配直属上级
     */
    Long matchDirectSupervisor(Long applicantId);
    
    /**
     * 确定多级审批人
     */
    ApprovalChain determineMultiLevelApprovers(Long applicantId, Long deptId, BigDecimal amount);
    
    class ApprovalChain {
        private final Long[] approvers; // 审批人链，从低到高
        private final String[] levels;  // 审批级别描述
        
        public ApprovalChain(Long[] approvers, String[] levels) {
            this.approvers = approvers;
            this.levels = levels;
        }
        
        public Long[] getApprovers() { return approvers; }
        public String[] getLevels() { return levels; }
        public int getLevelCount() { return approvers != null ? approvers.length : 0; }
    }
}