package com.ccms.service;

import com.ccms.dto.ApprovalStatistics;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审批流程服务接口
 * 提供审批流程的全生命周期管理
 */
public interface ApprovalFlowService {

    // 审批流程配置管理
    /**
     * 创建审批流程配置
     */
    ApprovalFlowConfig createFlowConfig(ApprovalFlowConfig flowConfig);

    /**
     * 更新审批流程配置
     */
    ApprovalFlowConfig updateFlowConfig(ApprovalFlowConfig flowConfig);

    /**
     * 根据ID获取流程配置
     */
    ApprovalFlowConfig getFlowConfigById(Long id);

    /**
     * 根据流程编码获取最新版本的流程配置
     */
    ApprovalFlowConfig getLatestFlowConfigByCode(String flowCode);

    /**
     * 根据业务类型和金额匹配适用的流程配置
     */
    ApprovalFlowConfig matchApplicableFlowConfig(BusinessType businessType, BigDecimal amount, Long deptId);

    /**
     * 获取所有启用的流程配置
     */
    List<ApprovalFlowConfig> getAllEnabledFlowConfigs();

    // 审批实例管理
    /**
     * 启动审批实例
     */
    ApprovalInstance startApprovalInstance(BusinessType businessType, String businessId, Long applicantId, 
                                          String title, String content);

    /**
     * 获取审批实例详情
     */
    ApprovalInstance getApprovalInstanceById(Long instanceId);

    /**
     * 获取审批实例详情（简写）
     */
    ApprovalInstance getApprovalInstance(Long instanceId);

    /**
     * 根据业务信息获取审批实例
     */
    ApprovalInstance getApprovalInstanceByBusiness(BusinessType businessType, String businessId);

    /**
     * 撤回审批实例
     */
    void withdrawApprovalInstance(Long instanceId, Long applicantId);

    /**
     * 取消审批实例
     */
    void cancelApprovalInstance(Long instanceId, String reason);

    // 审批操作处理
    /**
     * 审批同意
     */
    ApprovalInstance approve(Long instanceId, Long approverId, String remarks);

    /**
     * 审批拒绝
     */
    ApprovalInstance reject(Long instanceId, Long approverId, String remarks);

    /**
     * 转审到其他审批人
     */
    ApprovalInstance transfer(Long instanceId, Long currentApproverId, Long targetApproverId, String remarks);

    /**
     * 跳过当前节点
     */
    ApprovalInstance skip(Long instanceId, Long approverId, String remarks);

    /**
     * 审批撤回
     */
    ApprovalInstance withdraw(Long instanceId, Long applicantId, String remarks);

    /**
     * 取消审批流程
     */
    ApprovalInstance cancel(Long instanceId, Long applicantId, String remarks);

    // 查询功能
    /**
     * 获取待我审批的实例列表
     */
    Page<ApprovalInstance> getPendingApprovals(Long approverId, Pageable pageable);

    /**
     * 获取待审批实例列表
     */
    Page<ApprovalInstance> getPendingInstances(Long approverId, Pageable pageable);

    /**
     * 获取我发起的审批实例列表
     */
    Page<ApprovalInstance> getMyApplications(Long applicantId, Pageable pageable);

    /**
     * 获取我发起的审批列表
     */
    Page<ApprovalInstance> getMyInitiatedInstances(Long applicantId, Pageable pageable);

    /**
     * 获取所有审批实例（管理员使用）
     */
    Page<ApprovalInstance> getAllInstances(BusinessType businessType, ApprovalStatus status, String keyword, Pageable pageable);

    /**
     * 获取我参与的审批列表（包括已审批的）
     */
    Page<ApprovalInstance> getMyInvolvedInstances(Long userId, Pageable pageable);

    /**
     * 获取审批记录历史
     */
    List<ApprovalRecord> getApprovalRecords(Long instanceId);

    /**
     * 获取审批记录（分页）
     */
    Page<ApprovalRecord> getApprovalRecords(Long instanceId, Pageable pageable);

    /**
     * 获取审批流程图（节点状态）
     */
    List<ApprovalRecord> getApprovalFlowChart(Long instanceId);

    /**
     * 获取当前审批环节
     */
    ApprovalNode getCurrentApprovalNode(Long instanceId);

    /**
     * 获取审批实例的完整节点路径
     */
    List<ApprovalNode> getApprovalNodePath(Long instanceId);

    // 统计和分析
    /**
     * 获取审批统计信息
     */
    ApprovalStatistics getApprovalStatistics(BusinessType businessType, String startDate, String endDate);

    /**
     * 获取用户审批统计
     */
    ApprovalStatistics getUserApprovalStatistics(Long userId, BusinessType businessType, String startDate, String endDate);

    /**
     * 获取业务类型审批统计
     */
    List<ApprovalStatistics> getBusinessApprovalStatistics(BusinessType businessType, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取可用的流程配置列表
     */
    List<ApprovalFlowConfig> getAvailableFlowConfigs(BusinessType businessType);

    // 流程配置验证
    /**
     * 验证流程配置的完整性
     */
    boolean validateFlowConfig(ApprovalFlowConfig flowConfig);

    /**
     * 检查流程配置版本兼容性
     */
    boolean checkVersionCompatibility(String flowCode, Integer fromVersion, Integer toVersion);

    // 批量操作
    /**
     * 批量审批操作
     */
    int batchApprove(List<Long> instanceIds, Long approverId);

    /**
     * 批量撤回审批
     */
    void batchWithdraw(List<Long> instanceIds, Long applicantId, String reason);

    /**
     * 催办审批任务
     */
    void remindApprovalTask(Long instanceId, Long approverId);

    // 统计数据结构定义
    class ApprovalStatistics {
        private final Long totalInstances;
        private final Long pendingInstances;
        private final Long approvedInstances;
        private final Long rejectedInstances;
        private final Long canceledInstances;
        private final Double averageProcessingHours;

        public ApprovalStatistics(Long totalInstances, Long pendingInstances, Long approvedInstances, 
                                Long rejectedInstances, Long canceledInstances, Double averageProcessingHours) {
            this.totalInstances = totalInstances;
            this.pendingInstances = pendingInstances;
            this.approvedInstances = approvedInstances;
            this.rejectedInstances = rejectedInstances;
            this.canceledInstances = canceledInstances;
            this.averageProcessingHours = averageProcessingHours;
        }

        // Getters
        public Long getTotalInstances() { return totalInstances; }
        public Long getPendingInstances() { return pendingInstances; }
        public Long getApprovedInstances() { return approvedInstances; }
        public Long getRejectedInstances() { return rejectedInstances; }
        public Long getCanceledInstances() { return canceledInstances; }
        public Double getAverageProcessingHours() { return averageProcessingHours; }
    }

    class UserApprovalStatistics {
        private final Long totalAssignments;
        private final Long pendingAssignments;
        private final Long approvedAssignments;
        private final Long rejectedAssignments;
        private final Double averageProcessingHours;
        private final Map<BusinessTypeEnum, Long> businessTypeCounts;

        public UserApprovalStatistics(Long totalAssignments, Long pendingAssignments, Long approvedAssignments,
                                    Long rejectedAssignments, Double averageProcessingHours, 
                                    Map<BusinessTypeEnum, Long> businessTypeCounts) {
            this.totalAssignments = totalAssignments;
            this.pendingAssignments = pendingAssignments;
            this.approvedAssignments = approvedAssignments;
            this.rejectedAssignments = rejectedAssignments;
            this.averageProcessingHours = averageProcessingHours;
            this.businessTypeCounts = businessTypeCounts;
        }

        // Getters
        public Long getTotalAssignments() { return totalAssignments; }
        public Long getPendingAssignments() { return pendingAssignments; }
        public Long getApprovedAssignments() { return approvedAssignments; }
        public Long getRejectedAssignments() { return rejectedAssignments; }
        public Double getAverageProcessingHours() { return averageProcessingHours; }
        public Map<BusinessTypeEnum, Long> getBusinessTypeCounts() { return businessTypeCounts; }
    }

    class BusinessApprovalStatistics {
        private final Long totalInstances;
        private final Long pendingInstances;
        private final Long approvedInstances;
        private final Long rejectedInstances;
        private final Double approvalRate;
        private final Double averageAmount;

        public BusinessApprovalStatistics(Long totalInstances, Long pendingInstances, Long approvedInstances,
                                        Long rejectedInstances, Double approvalRate, Double averageAmount) {
            this.totalInstances = totalInstances;
            this.pendingInstances = pendingInstances;
            this.approvedInstances = approvedInstances;
            this.rejectedInstances = rejectedInstances;
            this.approvalRate = approvalRate;
            this.averageAmount = averageAmount;
        }

        // Getters
        public Long getTotalInstances() { return totalInstances; }
        public Long getPendingInstances() { return pendingInstances; }
        public Long getApprovedInstances() { return approvedInstances; }
        public Long getRejectedInstances() { return rejectedInstances; }
        public Double getApprovalRate() { return approvalRate; }
        public Double getAverageAmount() { return averageAmount; }
    }
}