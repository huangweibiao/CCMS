package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessType;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.PriorityTypeEnum;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalNodeRepository;
import com.ccms.repository.approval.ApprovalRecordRepository;
import com.ccms.dto.ApprovalStatistics;
import com.ccms.service.ApprovalFlowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApprovalFlowServiceImpl implements ApprovalFlowService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalFlowServiceImpl.class);
    
    private final ApprovalFlowConfigRepository flowConfigRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalNodeRepository nodeRepository;
    private final ApprovalRecordRepository recordRepository;
    
    public ApprovalFlowServiceImpl(ApprovalFlowConfigRepository flowConfigRepository,
                                  ApprovalInstanceRepository instanceRepository,
                                  ApprovalNodeRepository nodeRepository,
                                  ApprovalRecordRepository recordRepository) {
        this.flowConfigRepository = flowConfigRepository;
        this.instanceRepository = instanceRepository;
        this.nodeRepository = nodeRepository;
        this.recordRepository = recordRepository;
    }

    // 1. 流程配置管理方法
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
        return flowConfigRepository.findByFlowCodeOrderByVersionDesc(flowCode).stream().findFirst().orElse(null);
    }

    @Override
    public ApprovalFlowConfig matchApplicableFlowConfig(BusinessType businessType, BigDecimal amount, Long deptId) {
        // 简化实现：使用默认流程
        return flowConfigRepository.findByFlowCodeOrderByVersionDesc("DEFAULT").stream().findFirst().orElse(null);
    }

    @Override
    public List<ApprovalFlowConfig> getAllEnabledFlowConfigs() {
        // 简化实现：使用findAll
        return flowConfigRepository.findAll();
    }

    // 2. 审批实例管理方法
    @Override
    @Transactional
    public ApprovalInstance startApprovalInstance(BusinessType businessType, String businessId, Long applicantId, 
                                                 String title, String content) {
        log.info("启动审批实例: 业务类型={}, 业务ID={}, 申请人={}", businessType, businessId, applicantId);
        
        ApprovalInstance instance = new ApprovalInstance();
        instance.setInstanceNo(generateInstanceNo());
        instance.setFlowId(1L); // 默认流程ID
        
        // 安全转换业务ID
        try {
            instance.setBusinessId(Long.parseLong(businessId));
        } catch (NumberFormatException e) {
            log.warn("业务ID格式错误: {}, 设置为0", businessId);
            instance.setBusinessId(0L);
        }
        
        // 转换业务类型为String存储
        instance.setBusinessType(businessType != null ? businessType.name() : "DEFAULT");
        instance.setStatus(ApprovalStatus.DRAFT.ordinal()); // 初始状态
        instance.setApprovalTitle(title);
        instance.setCreateBy(applicantId != null ? applicantId.toString() : "0");
        instance.setCreateTime(LocalDateTime.now());
        
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
        // 手动实现查询逻辑
        Long businessIdLong;
        try {
            businessIdLong = Long.parseLong(businessId);
        } catch (NumberFormatException e) {
            log.warn("业务ID格式错误: {}", businessId);
            return null;
        }
        
        String businessTypeStr = businessType != null ? businessType.name() : null;
        
        // 使用手动筛选
        for (ApprovalInstance instance : instanceRepository.findAll()) {
            if (instance.getBusinessType() != null && 
                instance.getBusinessType().equals(businessTypeStr) &&
                instance.getBusinessId() != null && 
                instance.getBusinessId().equals(businessIdLong)) {
                return instance;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void withdrawApprovalInstance(Long instanceId, Long applicantId) {
        log.info("撤回审批实例: ID={}, 申请人={}", instanceId, applicantId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(ApprovalStatus.CANCELED.ordinal());
            instanceRepository.save(instance);
        }
    }

    @Override
    @Transactional
    public void cancelApprovalInstance(Long instanceId, String reason) {
        log.info("取消审批实例: ID={}, 原因={}", instanceId, reason);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(ApprovalStatus.CANCELED.ordinal());
            instance.setRemarks(reason);
            instanceRepository.save(instance);
        }
    }

    // 3. 审批操作方法
    @Override
    @Transactional
    public ApprovalInstance approve(Long instanceId, Long approverId, String remarks) {
        log.info("审批同意: 实例ID={}, 审批人={}", instanceId, approverId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(ApprovalStatus.APPROVED.ordinal());
            instance.setFinishTime(LocalDateTime.now());
            instanceRepository.save(instance);
            
            // 创建审批记录
            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instanceId);
            record.setApproverId(approverId);
            record.setApprovalRemark(remarks);
            record.setApprovalTime(LocalDateTime.now());
            record.setApprovalAction(1); // 同意
            recordRepository.save(record);
        }
        return instance;
    }

    @Override
    @Transactional
    public ApprovalInstance reject(Long instanceId, Long approverId, String remarks) {
        log.info("审批拒绝: 实例ID={}, 审批人={}", instanceId, approverId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null) {
            instance.setStatus(ApprovalStatus.REJECTED.ordinal());
            instance.setFinishTime(LocalDateTime.now());
            instance.setRemarks(remarks);
            instanceRepository.save(instance);
            
            // 创建审批记录
            ApprovalRecord record = new ApprovalRecord();
            record.setInstanceId(instanceId);
            record.setApproverId(approverId);
            record.setApprovalRemark(remarks);
            record.setApprovalTime(LocalDateTime.now());
            record.setApprovalAction(2); // 拒绝
            recordRepository.save(record);
        }
        return instance;
    }

    @Override
    @Transactional
    public ApprovalInstance transfer(Long instanceId, Long currentApproverId, Long targetApproverId, String remarks) {
        log.info("转审: 实例ID={}, 目标审批人={}", instanceId, targetApproverId);
        // 简化实现
        return getApprovalInstanceById(instanceId);
    }

    @Override
    @Transactional
    public ApprovalInstance skip(Long instanceId, Long approverId, String remarks) {
        log.info("跳过节点: 实例ID={}, 审批人={}", instanceId, approverId);
        // 简化实现
        return getApprovalInstanceById(instanceId);
    }

    @Override
    @Transactional
    public ApprovalInstance withdraw(Long instanceId, Long applicantId, String remarks) {
        log.info("撤回审批: 实例ID={}, 申请人={}", instanceId, applicantId);
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        if (instance != null && 
            instance.getCreateBy() != null && 
            instance.getCreateBy().equals(applicantId.toString())) {
            instance.setStatus(ApprovalStatus.WITHDRAWN.ordinal());
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

    // 4. 查询功能
    @Override
    public Page<ApprovalInstance> getPendingApprovals(Long approverId, Pageable pageable) {
        // 手动实现：筛选出审批中的实例
        List<ApprovalInstance> allInstances = instanceRepository.findAll();
        List<ApprovalInstance> pendingInstances = new ArrayList<>();
        
        for (ApprovalInstance instance : allInstances) {
            if (instance.getStatus() == ApprovalStatus.APPROVING.ordinal()) {
                pendingInstances.add(instance);
            }
        }
        
        return new PageImpl<>(pendingInstances, pageable, pendingInstances.size());
    }

    @Override
    public Page<ApprovalInstance> getPendingInstances(Long approverId, Pageable pageable) {
        return getPendingApprovals(approverId, pageable);
    }

    @Override
    public Page<ApprovalInstance> getMyApplications(Long applicantId, Pageable pageable) {
        // 手动筛选实现
        List<ApprovalInstance> allInstances = instanceRepository.findAll();
        List<ApprovalInstance> myApplications = new ArrayList<>();
        
        String applicantIdStr = applicantId != null ? applicantId.toString() : null;
        for (ApprovalInstance instance : allInstances) {
            if (instance.getCreateBy() != null && instance.getCreateBy().equals(applicantIdStr)) {
                myApplications.add(instance);
            }
        }
        
        return new PageImpl<>(myApplications, pageable, myApplications.size());
    }

    @Override
    public Page<ApprovalInstance> getMyInitiatedInstances(Long applicantId, Pageable pageable) {
        return getMyApplications(applicantId, pageable);
    }

    @Override
    public Page<ApprovalInstance> getAllInstances(BusinessType businessType, ApprovalStatus status, String keyword, Pageable pageable) {
        // 简化实现：返回所有实例
        List<ApprovalInstance> allInstances = instanceRepository.findAll();
        
        // 应用过滤器
        List<ApprovalInstance> filteredInstances = new ArrayList<>();
        String businessTypeStr = businessType != null ? businessType.name() : null;
        Integer statusInt = status != null ? status.ordinal() : null;
        
        for (ApprovalInstance instance : allInstances) {
            boolean matches = true;
            
            if (businessTypeStr != null && !businessTypeStr.equals(instance.getBusinessType())) {
                matches = false;
            }
            
            if (statusInt != null && !statusInt.equals(instance.getStatus())) {
                matches = false;
            }
            
            if (keyword != null && !keyword.isEmpty()) {
                // 关键词搜索
                boolean keywordMatch = false;
                if (instance.getApprovalTitle() != null && instance.getApprovalTitle().contains(keyword)) {
                    keywordMatch = true;
                }
                if (instance.getRemarks() != null && instance.getRemarks().contains(keyword)) {
                    keywordMatch = true;
                }
                matches = matches && keywordMatch;
            }
            
            if (matches) {
                filteredInstances.add(instance);
            }
        }
        
        return new PageImpl<>(filteredInstances, pageable, filteredInstances.size());
    }

    @Override
    public Page<ApprovalInstance> getMyInvolvedInstances(Long userId, Pageable pageable) {
        // 简化实现：返回所有实例
        List<ApprovalInstance> allInstances = instanceRepository.findAll();
        return new PageImpl<>(allInstances, pageable, allInstances.size());
    }

    @Override
    public List<ApprovalRecord> getApprovalRecords(Long instanceId) {
        // 简化实现：手动筛选
        List<ApprovalRecord> allRecords = recordRepository.findAll();
        List<ApprovalRecord> recordsForInstance = new ArrayList<>();
        
        for (ApprovalRecord record : allRecords) {
            if (record.getInstanceId() != null && record.getInstanceId().equals(instanceId)) {
                recordsForInstance.add(record);
            }
        }
        
        return recordsForInstance;
    }

    @Override
    public Page<ApprovalRecord> getApprovalRecords(Long instanceId, Pageable pageable) {
        List<ApprovalRecord> records = getApprovalRecords(instanceId);
        return new PageImpl<>(records, pageable, records.size());
    }

    @Override
    public List<ApprovalRecord> getApprovalFlowChart(Long instanceId) {
        return getApprovalRecords(instanceId);
    }

    @Override
    public ApprovalNode getCurrentApprovalNode(Long instanceId) {
        // 简化实现
        List<ApprovalNode> allNodes = nodeRepository.findAll();
        for (ApprovalNode node : allNodes) {
            if (node.getProcessId() != null && node.getProcessId().equals(instanceId)) {
                return node;
            }
        }
        return null;
    }

    @Override
    public List<ApprovalNode> getApprovalNodePath(Long instanceId) {
        // 简化实现
        List<ApprovalNode> allNodes = nodeRepository.findAll();
        List<ApprovalNode> nodesForInstance = new ArrayList<>();
        
        for (ApprovalNode node : allNodes) {
            if (node.getProcessId() != null && node.getProcessId().equals(instanceId)) {
                nodesForInstance.add(node);
            }
        }
        
        return nodesForInstance;
    }

    // 5. 统计和分析
    @Override
    public ApprovalStatistics getApprovalStatistics(BusinessType businessType, String startDate, String endDate) {
        log.info("获取审批统计信息: 业务类型={}, 开始时间={}, 结束时间={}", businessType, startDate, endDate);
        
        // 手动计算统计数据
        List<ApprovalInstance> allInstances = instanceRepository.findAll();
        
        long totalCount = allInstances.size();
        long pendingCount = 0;
        long approvedCount = 0;
        long rejectedCount = 0;
        long canceledCount = 0;
        double totalDuration = 0;
        int completedCount = 0;
        
        // 统计各状态数量
        for (ApprovalInstance instance : allInstances) {
            switch (ApprovalStatus.values()[instance.getStatus()]) {
                case APPROVING:
                    pendingCount++;
                    break;
                case APPROVED:
                    approvedCount++;
                    completedCount++;
                    break;
                case REJECTED:
                    rejectedCount++;
                    completedCount++;
                    break;
                case CANCELED:
                case WITHDRAWN:
                    canceledCount++;
                    completedCount++;
                    break;
                default:
                    break;
            }
            
            // 计算平均处理时间
            if (instance.getFinishTime() != null && instance.getCreateTime() != null) {
                long duration = java.time.Duration.between(instance.getCreateTime(), instance.getFinishTime()).toHours();
                totalDuration += duration;
            }
        }
        
        double averageDuration = completedCount > 0 ? totalDuration / completedCount : 0.0;
        
        return new ApprovalStatistics(totalCount, pendingCount, approvedCount, rejectedCount, canceledCount, averageDuration);
    }

    @Override
    public com.ccms.dto.ApprovalStatistics getUserApprovalStatistics(Long userId, BusinessType businessType, String startDate, String endDate) {
        log.info("获取用户审批统计: 用户ID={}, 业务类型={}", userId, businessType);
        
        // 简化实现 - 使用com.ccms.dto.ApprovalStatistics
        com.ccms.dto.ApprovalStatistics result = new com.ccms.dto.ApprovalStatistics();
        result.setTotalCount(0L);
        result.setPendingCount(0L);
        result.setApprovedCount(0L);
        result.setRejectedCount(0L);
        result.setCanceledCount(0L);
        result.setAverageDuration(0.0);
        return result;
    }

    @Override
    public List<ApprovalStatistics> getBusinessApprovalStatistics(BusinessType businessType, LocalDateTime startTime, LocalDateTime endTime) {
        // 简化实现
        return new ArrayList<>();
    }

    @Override
    public List<ApprovalFlowConfig> getAvailableFlowConfigs(BusinessType businessType) {
        return getAllEnabledFlowConfigs();
    }

    // 6. 流程配置验证
    @Override
    public boolean validateFlowConfig(ApprovalFlowConfig flowConfig) {
        return flowConfig != null && flowConfig.getFlowName() != null;
    }

    @Override
    public boolean checkVersionCompatibility(String flowCode, Integer fromVersion, Integer toVersion) {
        return toVersion != null && fromVersion != null && toVersion >= fromVersion;
    }

    // 7. 批量操作
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
                withdrawApprovalInstance(instanceId, applicantId);
            } catch (Exception e) {
                log.warn("批量撤回失败 - 实例ID: {}, 错误: {}", instanceId, e.getMessage());
            }
        }
    }

    @Override
    public void remindApprovalTask(Long instanceId, Long approverId) {
        log.info("催办审批任务: 实例ID={}, 审批人ID={}", instanceId, approverId);
    }

    // 添加缺失的接口方法实现
    @Override
    public ApprovalFlowConfig createApprovalFlowConfig(String flowName, String flowCode, BusinessTypeEnum businessType, String description, PriorityTypeEnum priority) {
        log.info("创建审批流程配置: 名称={}, 编码={}", flowName, flowCode);
        
        ApprovalFlowConfig config = new ApprovalFlowConfig();
        config.setFlowName(flowName);
        config.setFlowCode(flowCode);
        config.setBusinessType(businessType != null ? businessType.name() : BusinessTypeEnum.OTHER.name());
        config.setDescription(description);
        config.setPriority(priority != null ? getPriorityValue(priority) : 100);
        config.setStatus(1);
        config.setCreateTime(LocalDateTime.now());
        
        return flowConfigRepository.save(config);
    }

    @Override
    public ApprovalFlowConfig updateApprovalFlowConfig(Long id, String flowName, String description, PriorityTypeEnum priority, Boolean enabled) {
        log.info("更新审批流程配置: ID={}", id);
        
        ApprovalFlowConfig config = getFlowConfigById(id);
        if (config != null) {
            if (flowName != null) config.setFlowName(flowName);
            if (description != null) config.setDescription(description);
            if (priority != null) config.setPriority(getPriorityValue(priority));
            if (enabled != null) config.setStatus(enabled ? 1 : 0);
            config.setUpdateTime(LocalDateTime.now());
            return flowConfigRepository.save(config);
        }
        return null;
    }

    @Override
    public ApprovalFlowConfig getApprovalFlowConfig(Long id) {
        return getFlowConfigById(id);
    }

    @Override
    public void deleteApprovalFlowConfig(Long id) {
        log.info("删除审批流程配置: ID={}", id);
        flowConfigRepository.deleteById(id);
    }

    @Override
    public void toggleApprovalFlowConfig(Long id, boolean enabled) {
        log.info("切换审批流程配置状态: ID={}, 启用={}", id, enabled);
        
        ApprovalFlowConfig config = getFlowConfigById(id);
        if (config != null) {
            config.setStatus(enabled ? 1 : 0);
            config.setUpdateTime(LocalDateTime.now());
            flowConfigRepository.save(config);
        }
    }

    @Override
    public Page<ApprovalFlowConfig> getApprovalFlowConfigs(BusinessTypeEnum businessType, String keyword, Boolean enabled, Pageable pageable) {
        log.info("分页查询审批流程配置: 业务类型={}, 关键词={}, 启用状态={}", businessType, keyword, enabled);
        
        // 简化实现：获取所有配置并手动筛选
        List<ApprovalFlowConfig> allConfigs = flowConfigRepository.findAll();
        List<ApprovalFlowConfig> filteredConfigs = new ArrayList<>();
        
        String businessTypeStr = businessType != null ? businessType.name() : null;
        
        for (ApprovalFlowConfig config : allConfigs) {
            boolean matches = true;
            
            if (businessTypeStr != null && !businessTypeStr.equals(config.getBusinessType())) {
                matches = false;
            }
            
            if (enabled != null && enabled != (config.getStatus() == 1)) {
                matches = false;
            }
            
            if (keyword != null && !keyword.isEmpty()) {
                boolean keywordMatch = false;
                if (config.getFlowName() != null && config.getFlowName().contains(keyword)) {
                    keywordMatch = true;
                }
                if (config.getDescription() != null && config.getDescription().contains(keyword)) {
                    keywordMatch = true;
                }
                matches = matches && keywordMatch;
            }
            
            if (matches) {
                filteredConfigs.add(config);
            }
        }
        
        return new PageImpl<>(filteredConfigs, pageable, filteredConfigs.size());
    }

    @Override
    public List<ApprovalFlowConfig> getApprovalFlowConfigsByBusinessType(BusinessTypeEnum businessType) {
        log.info("根据业务类型获取流程配置: 类型={}", businessType);
        
        String businessTypeStr = businessType != null ? businessType.name() : null;
        List<ApprovalFlowConfig> allConfigs = flowConfigRepository.findAll();
        List<ApprovalFlowConfig> result = new ArrayList<>();
        
        for (ApprovalFlowConfig config : allConfigs) {
            if (businessTypeStr == null || businessTypeStr.equals(config.getBusinessType())) {
                result.add(config);
            }
        }
        
        return result;
    }

    @Override
    public List<ApprovalFlowConfig> getLatestFlowConfigs() {
        log.info("获取最新的流程配置");
        
        // 简化实现：获取所有启用的配置
        List<ApprovalFlowConfig> allConfigs = flowConfigRepository.findAll();
        List<ApprovalFlowConfig> enabledConfigs = new ArrayList<>();
        
        for (ApprovalFlowConfig config : allConfigs) {
            if (config.getStatus() != null && config.getStatus() == 1) {
                enabledConfigs.add(config);
            }
        }
        
        return enabledConfigs;
    }

    @Override
    public ApprovalFlowConfig createFlowConfigVersion(Long sourceConfigId, String versionName, String description, PriorityTypeEnum priority) {
        log.info("创建流程配置版本: 源配置ID={}, 版本名称={}", sourceConfigId, versionName);
        
        ApprovalFlowConfig sourceConfig = getFlowConfigById(sourceConfigId);
        if (sourceConfig == null) {
            return null;
        }
        
        // 创建新版本配置
        ApprovalFlowConfig newConfig = new ApprovalFlowConfig();
        newConfig.setFlowName(versionName);
        newConfig.setFlowCode(sourceConfig.getFlowCode());
        newConfig.setBusinessType(sourceConfig.getBusinessType());
        newConfig.setDescription(description != null ? description : sourceConfig.getDescription());
        newConfig.setPriority(priority != null ? getPriorityValue(priority) : sourceConfig.getPriority());
        newConfig.setStatus(1);
        newConfig.setCreateTime(LocalDateTime.now());
        
        return flowConfigRepository.save(newConfig);
    }

    @Override
    public ApprovalFlowConfig copyApprovalFlowConfig(Long sourceConfigId, String newFlowName, String newFlowCode) {
        log.info("复制流程配置: 源配置ID={}, 新配置名称={}", sourceConfigId, newFlowName);
        
        ApprovalFlowConfig sourceConfig = getFlowConfigById(sourceConfigId);
        if (sourceConfig == null) {
            return null;
        }
        
        // 创建复制配置
        ApprovalFlowConfig newConfig = new ApprovalFlowConfig();
        newConfig.setFlowName(newFlowName);
        newConfig.setFlowCode(newFlowCode);
        newConfig.setBusinessType(sourceConfig.getBusinessType());
        newConfig.setDescription(sourceConfig.getDescription());
        newConfig.setPriority(sourceConfig.getPriority());
        newConfig.setStatus(sourceConfig.getStatus());
        newConfig.setCreateTime(LocalDateTime.now());
        
        return flowConfigRepository.save(newConfig);
    }

    @Override
    public ApprovalFlowConfig importApprovalFlowConfig(String flowName, String flowCode, BusinessTypeEnum businessType, String description, PriorityTypeEnum priority) {
        return createApprovalFlowConfig(flowName, flowCode, businessType, description, priority);
    }

    @Override
    public String exportApprovalFlowConfig(Long id) {
        log.info("导出流程配置: ID={}", id);
        
        ApprovalFlowConfig config = getFlowConfigById(id);
        if (config != null) {
            return "流程配置导出: " + config.getFlowName() + " (" + config.getFlowCode() + ")";
        }
        return null;
    }

    @Override
    public List<ApprovalNode> getApprovalNodesByFlowConfig(Long configId) {
        log.info("获取审批节点列表: 配置ID={}", configId);
        
        // 简化实现：返回所有节点
        return nodeRepository.findAll();
    }

    @Override
    public boolean validateApprovalFlowConfig(Long configId) {
        ApprovalFlowConfig config = getFlowConfigById(configId);
        return config != null && config.getFlowName() != null && !config.getFlowName().isEmpty();
    }

    @Override
    public ApprovalFlowConfig matchApprovalFlowConfig(BusinessTypeEnum businessType, Double amount, PriorityTypeEnum priority) {
        log.info("匹配流程配置: 业务类型={}, 金额={}, 优先级={}", businessType, amount, priority);
        
        // 简化实现：使用默认配置
        List<ApprovalFlowConfig> configs = getLatestFlowConfigs();
        if (!configs.isEmpty()) {
            return configs.get(0);
        }
        return null;
    }

    @Override
    public ApprovalFlowConfig getDefaultFlowConfig(BusinessTypeEnum businessType) {
        log.info("获取默认流程配置: 业务类型={}", businessType);
        
        // 简化实现：返回第一个启用的配置
        List<ApprovalFlowConfig> enabledConfigs = getLatestFlowConfigs();
        if (!enabledConfigs.isEmpty()) {
            return enabledConfigs.get(0);
        }
        return null;
    }

    // 私有辅助方法
    private String generateInstanceNo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        return "APP" + LocalDateTime.now().format(formatter);
    }
    
    /**
     * 根据优先级枚举获取对应的数值
     */
    private Integer getPriorityValue(PriorityTypeEnum priority) {
        switch (priority) {
            case HIGH:
                return 50;
            case MEDIUM:
                return 100;
            case LOW:
                return 150;
            case URGENT:
                return 0;
            default:
                return 100;
        }
    }
}