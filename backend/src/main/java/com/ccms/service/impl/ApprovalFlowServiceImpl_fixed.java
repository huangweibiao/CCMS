package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalActionEnum;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.BusinessType;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalNodeRepository;
import com.ccms.repository.approval.ApprovalRecordRepository;
import com.ccms.service.ApprovalFlowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalFlowServiceImpl_fixed implements ApprovalFlowService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalFlowServiceImpl_fixed.class);
    
    private final ApprovalFlowConfigRepository flowConfigRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalNodeRepository nodeRepository;
    private final ApprovalRecordRepository recordRepository;

    public ApprovalFlowServiceImpl_fixed(ApprovalFlowConfigRepository flowConfigRepository, 
                                       ApprovalInstanceRepository instanceRepository,
                                       ApprovalNodeRepository nodeRepository,
                                       ApprovalRecordRepository recordRepository) {
        this.flowConfigRepository = flowConfigRepository;
        this.instanceRepository = instanceRepository;
        this.nodeRepository = nodeRepository;
        this.recordRepository = recordRepository;
    }

    // 流程配置方法
    @Override
    @Transactional
    public ApprovalFlowConfig createFlowConfig(ApprovalFlowConfig flowConfig) {
        log.info("创建审批流程配置: {}", flowConfig.getFlowName());
        return flowConfigRepository.save(flowConfig);
    }

    @Override
    @Transactional
    public ApprovalFlowConfig updateFlowConfig(ApprovalFlowConfig flowConfig) {
        log.info("更新审批流程配置: ID={}", flowConfig.getId());
        return flowConfigRepository.save(flowConfig);
    }

    @Override
    public ApprovalFlowConfig getFlowConfigById(Long id) {
        return flowConfigRepository.findById(id).orElse(null);
    }

    @Override
    public ApprovalFlowConfig getLatestFlowConfigByCode(String flowCode) {
        return flowConfigRepository.findLatestByFlowCode(flowCode);
    }

    @Override
    public ApprovalFlowConfig matchApplicableFlowConfig(BusinessType businessType, BigDecimal amount, Long deptId) {
        log.info("匹配适用流程配置: 业务类型={}, 金额={}", businessType, amount);
        return flowConfigRepository.findLatestByFlowCode("DEFAULT"); // 简化实现
    }

    @Override
    public List<ApprovalFlowConfig> getAllEnabledFlowConfigs() {
        return flowConfigRepository.findByStatus(1);
    }

    // 审批实例方法
    @Override
    @Transactional
    public ApprovalInstance startApprovalInstance(BusinessType businessType, String businessId, Long applicantId, 
                                                 String title, String content) {
        log.info("启动审批实例: 业务类型={}, 业务ID={}, 申请人={}", businessType, businessId, applicantId);
        
        ApprovalInstance instance = new ApprovalInstance();
        instance.setInstanceNo("APP-" + System.currentTimeMillis());
        instance.setFlowConfigId(1L); // 默认流程配置
        instance.setBusinessId(Long.valueOf(businessId));
        instance.setBusinessType(businessType.name());
        instance.setStatus(0); // 初始状态
        instance.setApprovalTitle(title);
        instance.setCreateBy(applicantId.toString());
        
        return instanceRepository.save(instance);
    }

    @Override
    public ApprovalInstance getApprovalInstanceById(Long instanceId) {
        return instanceRepository.findById(instanceId).orElse(null);
    }

    @Override
    public ApprovalInstance getApprovalInstance(Long instanceId) {
        return getApprovalInstanceById(instanceId);
    }

    @Override
    public ApprovalInstance getApprovalInstanceByBusiness(BusinessType businessType, String businessId) {
        return instanceRepository.findTop1ByBusinessTypeAndBusinessId(
            businessType.name(), Long.valueOf(businessId)
        );
    }

    @Override
    @Transactional
    public void withdrawApprovalInstance(Long instanceId, Long applicantId) {
        log.info("撤回审批实例: ID={}, 申请人={}", instanceId, applicantId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(4); // 取消状态
            instanceRepository.save(instance);
        }
    }

    @Override
    @Transactional
    public void cancelApprovalInstance(Long instanceId, String reason) {
        log.info("取消审批实例: ID={}, 原因={}", instanceId, reason);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(4); // 取消状态
            instance.setRemarks(reason);
            instanceRepository.save(instance);
        }
    }

    // 审批操作方法
    @Override
    @Transactional
    public ApprovalInstance approve(Long instanceId, Long approverId, String remarks) {
        log.info("审批同意: 实例ID={}, 审批人={}", instanceId, approverId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(2); // 已批准
            instance.setFinishTime(LocalDateTime.now());
            instanceRepository.save(instance);
        }
        return instance;
    }

    @Override
    @Transactional
    public ApprovalInstance reject(Long instanceId, Long approverId, String remarks) {
        log.info("审批拒绝: 实例ID={}, 审批人={}", instanceId, approverId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(3); // 已拒绝
            instance.setFinishTime(LocalDateTime.now());
            instance.setRemarks(remarks);
            instanceRepository.save(instance);
        }
        return instance;
    }

    @Override
    @Transactional
    public ApprovalInstance transfer(Long instanceId, Long currentApproverId, Long targetApproverId, String remarks) {
        log.info("转审: 实例ID={}, 目标审批人={}", instanceId, targetApproverId);
        return getApprovalInstanceById(instanceId);
    }

    @Override
    @Transactional
    public ApprovalInstance skip(Long instanceId, Long approverId, String remarks) {
        log.info("跳过节点: 实例ID={}, 审批人={}", instanceId, approverId);
        return getApprovalInstanceById(instanceId);
    }

    @Override
    @Transactional
    public ApprovalInstance withdraw(Long instanceId, Long applicantId, String remarks) {
        log.info("撤回审批: 实例ID={}, 申请人={}", instanceId, applicantId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null && instance.getCreateBy().equals(applicantId.toString())) {
            instance.setStatus(4); // 取消状态
            instance.setRemarks(remarks);
            instanceRepository.save(instance);
        }
        return instance;
    }

    @Override
    @Transactional
    public ApprovalInstance cancel(Long instanceId, Long applicantId, String remarks) {
        log.info("取消审批: 实例ID={}, 申请人={}", instanceId, applicantId);
        return withdraw(instanceId, applicantId, remarks);
    }

    // 查询方法
    @Override
    public Page<ApprovalInstance> getPendingApprovals(Long approverId, Pageable pageable) {
        return instanceRepository.findByStatusNotAndCreateByNot(4L, approverId.toString(), pageable);
    }

    @Override
    public Page<ApprovalInstance> getPendingInstances(Long approverId, Pageable pageable) {
        return getPendingApprovals(approverId, pageable);
    }

    @Override
    public Page<ApprovalInstance> getMyApplications(Long applicantId, Pageable pageable) {
        return instanceRepository.findByCreateBy(applicantId.toString(), pageable);
    }

    @Override
    public Page<ApprovalInstance> getMyInitiatedInstances(Long applicantId, Pageable pageable) {
        return getMyApplications(applicantId, pageable);
    }

    @Override
    public Page<ApprovalInstance> getAllInstances(BusinessType businessType, ApprovalStatus status, String keyword, Pageable pageable) {
        return instanceRepository.findAll(pageable);
    }

    @Override
    public Page<ApprovalInstance> getMyInvolvedInstances(Long userId, Pageable pageable) {
        return instanceRepository.findAll(pageable);
    }

    @Override
    public List<ApprovalRecord> getApprovalRecords(Long instanceId) {
        return recordRepository.findByInstanceId(instanceId);
    }

    @Override
    public Page<ApprovalRecord> getApprovalRecords(Long instanceId, Pageable pageable) {
        return recordRepository.findByInstanceId(instanceId, pageable);
    }

    @Override
    public List<ApprovalRecord> getApprovalFlowChart(Long instanceId) {
        return getApprovalRecords(instanceId);
    }

    @Override
    public ApprovalNode getCurrentApprovalNode(Long instanceId) {
        List<ApprovalNode> nodes = nodeRepository.findByInstanceIdOrderByStepNumber(instanceId);
        return nodes.isEmpty() ? null : nodes.get(0);
    }

    @Override
    public List<ApprovalNode> getApprovalNodePath(Long instanceId) {
        return nodeRepository.findByInstanceIdOrderByStepNumber(instanceId);
    }

    // 统计方法
    @Override
    public ApprovalStatistics getApprovalStatistics(BusinessType businessType, String startDate, String endDate) {
        log.info("获取审批统计信息: 业务类型={}, 开始时间={}, 结束时间={}", businessType, startDate, endDate);
        
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        
        long totalCount = instanceRepository.countByCreateTimeBetween(start, end);
        long pendingCount = instanceRepository.countByStatusAndCreateTimeBetween(0L, start, end);
        long approvedCount = instanceRepository.countByStatusAndCreateTimeBetween(2L, start, end);
        long rejectedCount = instanceRepository.countByStatusAndCreateTimeBetween(3L, start, end);
        long canceledCount = instanceRepository.countByStatusAndCreateTimeBetween(4L, start, end);
        
        // 简化计算平均处理时间
        double averageDuration = 24.0; // 默认24小时
        
        return new ApprovalStatistics(totalCount, pendingCount, approvedCount, rejectedCount, canceledCount, averageDuration);
    }

    @Override
    public ApprovalStatistics getUserApprovalStatistics(Long userId, BusinessType businessType, String startDate, String endDate) {
        log.info("获取用户审批统计: 用户ID={}, 业务类型={}", userId, businessType);
        
        // 简化实现：返回默认统计
        return new ApprovalStatistics(0L, 0L, 0L, 0L, 0L, 0.0);
    }

    @Override
    public List<ApprovalStatistics> getBusinessApprovalStatistics(BusinessType businessType, LocalDateTime startTime, LocalDateTime endTime) {
        // 简化实现
        ApprovalStatistics stats = new ApprovalStatistics(0L, 0L, 0L, 0L, 0L, 0.0);
        return List.of(stats);
    }

    @Override
    public List<ApprovalFlowConfig> getAvailableFlowConfigs(BusinessType businessType) {
        return getAllEnabledFlowConfigs();
    }

    // 验证方法
    @Override
    public boolean validateFlowConfig(ApprovalFlowConfig flowConfig) {
        return flowConfig != null && flowConfig.getFlowName() != null;
    }

    @Override
    public boolean checkVersionCompatibility(String flowCode, Integer fromVersion, Integer toVersion) {
        return toVersion != null && fromVersion != null && toVersion >= fromVersion;
    }

    // 批量操作方法
    @Override
    public int batchApprove(List<Long> instanceIds, Long approverId) {
        int successCount = 0;
        for (Long instanceId : instanceIds) {
            try {
                approve(instanceId, approverId, "批量审批");
                successCount++;
            } catch (Exception e) {
                log.warn("批量审批失败 - 实例ID: {}, 错误: {}", instanceId, e.getMessage());
            }
        }
        return successCount;
    }

    @Override
    public void batchWithdraw(List<Long> instanceIds, Long applicantId, String reason) {
        for (Long instanceId : instanceIds) {
            try {
                withdraw(instanceId, applicantId, reason);
            } catch (Exception e) {
                log.warn("批量撤回失败 - 实例ID: {}, 错误: {}", instanceId, e.getMessage());
            }
        }
    }

    @Override
    public void remindApprovalTask(Long instanceId, Long approverId) {
        log.info("催办审批任务: 实例ID={}, 审批人ID={}", instanceId, approverId);
    }

    // 私有辅助方法
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return LocalDateTime.now().minusMonths(1); // 默认一个月前
        }
        try {
            return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
        } catch (Exception e) {
            log.warn("日期解析失败: {}, 使用默认值", dateTimeStr);
            return LocalDateTime.now().minusMonths(1);
        }
    }
}