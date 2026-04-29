package com.ccms.service;

import com.ccms.entity.approval.ApprovalProcess;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.Approval;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审批流程服务接口
 * 
 * @author 系统生成
 */
public interface ApprovalService {
    
    /**
     * 启动审批流程
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @param applicantId 申请人ID
     * @param approvers 审批人ID列表
     * @return 创建的审批流程
     */
    ApprovalProcess startApprovalProcess(String businessType, Long businessId, Long applicantId, List<Long> approvers);
    
    /**
     * 获取审批流程详情
     * 
     * @param processId 流程ID
     * @return 审批流程信息
     */
    ApprovalProcess getApprovalProcess(Long processId);
    
    /**
     * 获取业务的审批流程
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 审批流程信息
     */
    ApprovalProcess getApprovalProcessByBusiness(String businessType, Long businessId);
    
    /**
     * 处理审批操作
     * 
     * @param processId 流程ID
     * @param approverId 审批人ID
     * @param approved 是否通过
     * @param comment 审批意见
     */
    void processApproval(Long processId, Long approverId, boolean approved, String comment);
    
    /**
     * 撤回审批流程
     * 
     * @param processId 流程ID
     * @param applicantId 申请人ID
     */
    void withdrawApprovalProcess(Long processId, Long applicantId);
    
    /**
     * 获取待我审批的流程列表
     * 
     * @param approverId 审批人ID
     * @return 待审批流程列表
     */
    List<ApprovalProcess> getPendingApprovals(Long approverId);
    
    /**
     * 获取我已处理的审批列表
     * 
     * @param approverId 审批人ID
     * @return 已处理审批列表
     */
    List<ApprovalProcess> getProcessedApprovals(Long approverId);
    
    /**
     * 获取审批记录
     * 
     * @param processId 流程ID
     * @return 审批记录列表
     */
    List<ApprovalRecord> getApprovalRecords(Long processId);
    
    /**
     * 获取当前审批节点
     * 
     * @param processId 流程ID
     * @return 当前审批节点
     */
    ApprovalNode getCurrentApprovalNode(Long processId);
    
    /**
     * 获取下一级审批节点
     * 
     * @param processId 流程ID
     * @return 下一级审批节点
     */
    ApprovalNode getNextApprovalNode(Long processId);
    
    /**
     * 跳过当前审批节点
     * 
     * @param processId 流程ID
     * @param skippperId 跳过人ID
     * @param reason 跳过原因
     */
    void skipCurrentNode(Long processId, Long skippperId, String reason);
    
    /**
     * 重新分配审批节点
     * 
     * @param processId 流程ID
     * @param nodeId 节点ID
     * @param newApproverId 新的审批人ID
     */
    void reassignApprover(Long processId, Long nodeId, Long newApproverId);
    
    /**
     * 获取审批流程统计信息
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计信息
     */
    ApprovalStatistics getApprovalStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取用户的审批统计
     * 
     * @param approverId 审批人ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户审批统计
     */
    UserApprovalStatistics getUserApprovalStatistics(Long approverId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 认证相关方法
    boolean checkPermission(String token, String permission);
    
    // 基本的CRUD方法
    Page<Approval> getApprovalList(int page, int size, Long approverId, Long applicantId, Integer status, String businessType);
    
    Approval getApprovalById(Long approvalId);
    
    void createApproval(Approval approval);
    
    void updateApproval(Approval approval);
    
    void deleteApproval(Long approvalId);
    
    // 审批操作
    void approve(Long approvalId, Long approverId, Integer result, String comment);
    
    // 分页查询方法
    Page<Approval> getPendingApprovals(int page, int size, Long approverId, String businessType);
    
    // 委托和加急
    void delegateApproval(Long approvalId, Long delegateToId, String reason);
    
    void setApprovalUrgent(Long approvalId, String urgentReason, Integer priority);
    
    // 历史记录和统计
    Object getApprovalHistory(Long approvalId);
    
    Map<String, Object> getApprovalStatistics(Long approverId, Long deptId, String startDate, String endDate);
    
    // 配置和管理
    void configureApproval(String businessType, Object approvalFlow);
    
    void batchOperation(Long[] approvalIds, String operation);
    
    void setApprovalNotification(Long userId, String notifyType, Boolean enabled);
    
    Object exportApprovals(Map<String, Object> exportParams);
    
    Object analyzeApprovalEfficiency(Map<String, Object> analysisParams);
    
    void handleApprovalException(Long approvalId, String exceptionType, String action, String comment);
    
    Object monitorApprovalProcess(Map<String, Object> monitorParams);
    
    Object getOptimizationSuggestions(Map<String, Object> suggestionParams);
    
    /**
     * 催办审批
     * 
     * @param processId 流程ID
     */
    void remindApproval(Long processId);
    
    /**
     * 委派审批任务
     * 
     * @param processId 流程ID
     * @param approverId 原审批人ID
     * @param delegateToId 委托给的用户ID
     * @param reason 委托原因
     */
    void delegateApprovalTask(Long processId, Long approverId, Long delegateToId, String reason);
    
    /**
     * 审批统计信息
     */
    class ApprovalStatistics {
        private final Long totalProcesses;
        private final Long pendingProcesses;
        private final Long approvedProcesses;
        private final Long rejectedProcesses;
        private final Long expiredProcesses;
        
        public ApprovalStatistics(Long totalProcesses, Long pendingProcesses, 
                                Long approvedProcesses, Long rejectedProcesses, 
                                Long expiredProcesses) {
            this.totalProcesses = totalProcesses;
            this.pendingProcesses = pendingProcesses;
            this.approvedProcesses = approvedProcesses;
            this.rejectedProcesses = rejectedProcesses;
            this.expiredProcesses = expiredProcesses;
        }
        
        public Long getTotalProcesses() {
            return totalProcesses;
        }
        
        public Long getPendingProcesses() {
            return pendingProcesses;
        }
        
        public Long getApprovedProcesses() {
            return approvedProcesses;
        }
        
        public Long getRejectedProcesses() {
            return rejectedProcesses;
        }
        
        public Long getExpiredProcesses() {
            return expiredProcesses;
        }
    }
    
    /**
     * 用户审批统计信息
     */
    class UserApprovalStatistics {
        private final Long totalAssignments;
        private final Long pendingAssignments;
        private final Long approvedAssignments;
        private final Long rejectedAssignments;
        private final Double averageProcessingTime; // 平均处理时间（小时）
        
        public UserApprovalStatistics(Long totalAssignments, Long pendingAssignments, 
                                    Long approvedAssignments, Long rejectedAssignments, 
                                    Double averageProcessingTime) {
            this.totalAssignments = totalAssignments;
            this.pendingAssignments = pendingAssignments;
            this.approvedAssignments = approvedAssignments;
            this.rejectedAssignments = rejectedAssignments;
            this.averageProcessingTime = averageProcessingTime;
        }
        
        public Long getTotalAssignments() {
            return totalAssignments;
        }
        
        public Long getPendingAssignments() {
            return pendingAssignments;
        }
        
        public Long getApprovedAssignments() {
            return approvedAssignments;
        }
        
        public Long getRejectedAssignments() {
            return rejectedAssignments;
        }
        
        public Double getAverageProcessingTime() {
            return averageProcessingTime;
        }
    }
}