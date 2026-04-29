package com.ccms.service.approval;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 审批流程引擎
 * 实现多级审批节点分支流程和超时自动处理机制
 * 
 * @author 系统生成
 */
@Service
public class ApprovalFlowEngine {

    @Autowired
    private ApprovalRuleMatcher approvalRuleMatcher;
    
    @Autowired
    private ApprovalRecordService approvalRecordService;
    
    // 存储超时监控的任务
    private final ConcurrentHashMap<Long, ApprovalTimeoutTask> timeoutTasks = new ConcurrentHashMap<>();

    /**
     * 启动审批流程
     */
    public boolean startApprovalFlow(String businessType, Long businessId, 
                                     Long applyUserId, LocalDateTime applyTime) {
        
        // 1. 根据业务类型匹配审批流程配置
        ApprovalFlowConfig config = findApprovalConfig(businessType, businessId);
        if (config == null) {
            // 没有匹配的审批流程，自动审批通过
            return autoApprove(businessType, businessId, applyUserId);
        }
        
        // 2. 创建初始审批记录
        ApprovalRecord initialRecord = createInitialApprovalRecord(businessType, businessId, 
                                                                   config, applyUserId, applyTime);
        
        // 3. 启动超时监控
        startTimeoutMonitoring(initialRecord);
        
        // 4. 通知第一个审批人
        notifyNextApprover(initialRecord);
        
        return true;
    }

    /**
     * 处理审批动作
     */
    public boolean processApprovalAction(Long recordId, Long approverId, 
                                         String action, String comment) {
        
        ApprovalRecord record = approvalRecordService.findById(recordId);
        if (record == null) {
            return false;
        }
        
        // 检查审批人权限
        if (!isValidApprover(record, approverId)) {
            return false;
        }
        
        // 更新审批记录
        record.setApproverId(approverId);
        record.setApprovalTime(LocalDateTime.now());
        record.setApprovalComment(comment);
        record.setApprovalResult(action);
        
        // 根据审批动作处理
        if ("APPROVE".equals(action)) {
            return handleApproved(record);
        } else if ("REJECT".equals(action)) {
            return handleRejected(record);
        }
        
        return false;
    }

    /**
     * 处理审批通过
     */
    private boolean handleApproved(ApprovalRecord record) {
        ApprovalFlowConfig config = record.getApprovalConfig();
        int currentStep = record.getApprovalStep();
        
        // 检查是否还有下一级审批
        if (approvalRuleMatcher.isApprovalComplete(config, currentStep + 1)) {
            // 所有审批完成，流程结束
            record.setApprovalStatus(2); // 审批通过
            approvalRecordService.update(record);
            
            // 取消超时监控
            cancelTimeoutMonitoring(record.getId());
            
            // 通知申请人
            notifyApplicant(record, "审批通过");
            
            return true;
        } else {
            // 进入下一级审批
            return startNextApprovalStep(record);
        }
    }

    /**
     * 处理审批拒绝
     */
    private boolean handleRejected(ApprovalRecord record) {
        record.setApprovalStatus(3); // 审批拒绝
        approvalRecordService.update(record);
        
        // 取消超时监控
        cancelTimeoutMonitoring(record.getId());
        
        // 通知申请人
        notifyApplicant(record, "审批被拒绝");
        
        return true;
    }

    /**
     * 启动下一级审批
     */
    private boolean startNextApprovalStep(ApprovalRecord currentRecord) {
        ApprovalFlowConfig config = currentRecord.getApprovalConfig();
        int nextStep = currentRecord.getApprovalStep() + 1;
        
        // 创建新的审批记录
        ApprovalRecord nextRecord = new ApprovalRecord();
        nextRecord.setBusinessType(currentRecord.getBusinessType());
        nextRecord.setBusinessId(currentRecord.getBusinessId());
        nextRecord.setApprovalConfig(config);
        nextRecord.setApprovalStep(nextStep);
        nextRecord.setApplyUserId(currentRecord.getApplyUserId());
        nextRecord.setApplyTime(currentRecord.getApplyTime());
        nextRecord.setApprovalStatus(0); // 待审批
        
        approvalRecordService.save(nextRecord);
        
        // 启动超时监控
        startTimeoutMonitoring(nextRecord);
        
        // 通知下一级审批人
        notifyNextApprover(nextRecord);
        
        return true;
    }

    /**
     * 定时检查超时审批
     */
    @Scheduled(fixedRate = 300000) // 每5分钟检查一次
    public void checkTimeoutApprovals() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(24); // 24小时超时
        
        List<ApprovalRecord> timeoutRecords = approvalRecordService
                .findTimeoutRecords(timeoutThreshold);
        
        for (ApprovalRecord record : timeoutRecords) {
            handleApprovalTimeout(record);
        }
    }

    /**
     * 处理审批超时
     */
    private void handleApprovalTimeout(ApprovalRecord record) {
        ApprovalFlowConfig config = record.getApprovalConfig();
        
        // 根据配置决定超时处理方式
        if (shouldAutoSkipOnTimeout(config)) {
            // 自动跳过当前审批人
            autoSkipApproval(record);
        } else if (shouldEscalateOnTimeout(config)) {
            // 升级审批（通知上级领导）
            escalateApproval(record);
        } else {
            // 默认处理：保持现状，记录超时日志
            record.setApprovalComment("【系统】审批超时，等待人工处理");
            approvalRecordService.update(record);
        }
        
        // 更新超时监控状态
        timeoutTasks.remove(record.getId());
    }

    /**
     * 自动跳过审批
     */
    private void autoSkipApproval(ApprovalRecord record) {
        record.setApprovalResult("AUTO_SKIP");
        record.setApprovalComment("【系统】审批超时，自动跳过");
        record.setApprovalTime(LocalDateTime.now());
        
        // 继续下一级审批
        handleApproved(record);
    }

    /**
     * 升级审批
     */
    private void escalateApproval(ApprovalRecord record) {
        // 查找上级领导作为新的审批人
        String escalatedApprover = findEscalatedApprover(record);
        
        record.setApprovalComment("【系统】审批超时，已升级至：" + escalatedApprover);
        approvalRecordService.update(record);
        
        // 通知升级后的审批人
        notifyApprover(escalatedApprover, record);
    }

    /**
     * 启动超时监控
     */
    private void startTimeoutMonitoring(ApprovalRecord record) {
        ApprovalTimeoutTask timeoutTask = new ApprovalTimeoutTask(record.getId());
        timeoutTasks.put(record.getId(), timeoutTask);
    }

    /**
     * 取消超时监控
     */
    private void cancelTimeoutMonitoring(Long recordId) {
        timeoutTasks.remove(recordId);
    }

    /**
     * 查找升级审批人
     */
    private String findEscalatedApprover(ApprovalRecord record) {
        // TODO: 实现查找上级领导逻辑
        return "escalated_approver";
    }

    /**
     * 判断是否应该自动跳过超时审批
     */
    private boolean shouldAutoSkipOnTimeout(ApprovalFlowConfig config) {
        // TODO: 从配置中读取超时处理策略
        return true;
    }

    /**
     * 判断是否应该升级审批
     */
    private boolean shouldEscalateOnTimeout(ApprovalFlowConfig config) {
        // TODO: 从配置中读取升级策略
        return false;
    }

    /**
     * 检查审批人是否有效
     */
    private boolean isValidApprover(ApprovalRecord record, Long approverId) {
        // TODO: 实现审批人验证逻辑
        return true;
    }

    /**
     * 查找审批流程配置
     */
    private ApprovalFlowConfig findApprovalConfig(String businessType, Long businessId) {
        // TODO: 实现根据业务查找配置的逻辑
        return null;
    }

    /**
     * 创建初始审批记录
     */
    private ApprovalRecord createInitialApprovalRecord(String businessType, Long businessId,
                                                       ApprovalFlowConfig config, Long applyUserId, 
                                                       LocalDateTime applyTime) {
        // TODO: 实现创建审批记录逻辑
        return null;
    }

    /**
     * 自动审批通过
     */
    private boolean autoApprove(String businessType, Long businessId, Long applyUserId) {
        // TODO: 实现自动审批逻辑
        return true;
    }

    /**
     * 通知下一级审批人
     */
    private void notifyNextApprover(ApprovalRecord record) {
        // TODO: 实现通知逻辑
    }

    /**
     * 通知审批人
     */
    private void notifyApprover(String approver, ApprovalRecord record) {
        // TODO: 实现通知逻辑
    }

    /**
     * 通知申请人
     */
    private void notifyApplicant(ApprovalRecord record, String message) {
        // TODO: 实现通知逻辑
    }

    /**
     * 审批超时任务
     */
    private static class ApprovalTimeoutTask {
        private final Long recordId;
        
        public ApprovalTimeoutTask(Long recordId) {
            this.recordId = recordId;
        }
        
        public Long getRecordId() {
            return recordId;
        }
    }
}