package com.ccms.service.impl;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalActionEnum;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.ApproverTypeEnum;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalNodeRepository;
import com.ccms.repository.approval.ApprovalRecordRepository;
import com.ccms.service.ApprovalFlowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalFlowServiceImpl implements ApprovalFlowService {

    private final ApprovalFlowConfigRepository flowConfigRepository;
    private final ApprovalInstanceRepository instanceRepository;
    private final ApprovalNodeRepository nodeRepository;
    private final ApprovalRecordRepository recordRepository;

    @Override
    @Transactional
    public ApprovalFlowConfig createFlowConfig(ApprovalFlowConfig flowConfig) {
        log.info("创建审批流程配置: {}", flowConfig.getFlowName());
        
        // 验证流程配置
        if (!validateFlowConfig(flowConfig)) {
            throw new IllegalArgumentException("流程配置验证失败");
        }
        
        // 如果存在相同编码的配置，获取最新版本号并递增
        Optional<ApprovalFlowConfig> latestConfig = flowConfigRepository.findLatestVersionByFlowCode(flowConfig.getFlowCode());
        if (latestConfig.isPresent()) {
            flowConfig.setVersion(latestConfig.get().getVersion() + 1);
        } else {
            flowConfig.setVersion(1);
        }
        
        // 设置默认值
        if (flowConfig.getPriority() == null) {
            flowConfig.setPriority(100);
        }
        if (flowConfig.getStatus() == null) {
            flowConfig.setStatus(1); // 默认启用
        }
        
        return flowConfigRepository.save(flowConfig);
    }

    @Override
    @Transactional
    public ApprovalFlowConfig updateFlowConfig(ApprovalFlowConfig flowConfig) {
        log.info("更新审批流程配置: {} (ID: {})", flowConfig.getFlowName(), flowConfig.getId());
        
        // 检查配置是否存在
        ApprovalFlowConfig existingConfig = flowConfigRepository.findById(flowConfig.getId())
                .orElseThrow(() -> new IllegalArgumentException("流程配置不存在: " + flowConfig.getId()));
        
        // 验证流程配置
        if (!validateFlowConfig(flowConfig)) {
            throw new IllegalArgumentException("流程配置验证失败");
        }
        
        // 保留原始创建时间和版本号
        flowConfig.setCreateTime(existingConfig.getCreateTime());
        flowConfig.setVersion(existingConfig.getVersion());
        
        return flowConfigRepository.save(flowConfig);
    }

    @Override
    public ApprovalFlowConfig getFlowConfigById(Long id) {
        return flowConfigRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("流程配置不存在: " + id));
    }

    @Override
    public ApprovalFlowConfig getLatestFlowConfigByCode(String flowCode) {
        return flowConfigRepository.findLatestVersionByFlowCode(flowCode)
                .orElseThrow(() -> new IllegalArgumentException("流程配置不存在: " + flowCode));
    }

    @Override
    @Transactional
    public ApprovalInstance startApprovalInstance(BusinessTypeEnum businessType, Long businessId, Long applicantId,
                                                 String businessTitle, String businessContent, BigDecimal amount) {
        log.info("启动审批实例 - 业务类型: {}, 业务ID: {}, 申请人: {}", businessType, businessId, applicantId);
        
        // 匹配适用的流程配置
        ApprovalFlowConfig flowConfig = matchApplicableFlowConfig(businessType, amount, null); // deptId暂时为空
        
        // 创建审批实例
        ApprovalInstance instance = new ApprovalInstance();
        instance.setFlowConfigId(flowConfig.getId());
        instance.setBusinessType(businessType);
        instance.setBusinessId(businessId);
        instance.setBusinessTitle(businessTitle);
        instance.setBusinessContent(businessContent);
        instance.setAmount(amount);
        instance.setApplicantId(applicantId);
        instance.setStatus(ApprovalStatusEnum.RUNNING);
        instance.setCurrentNode(0); // 开始节点
        instance.setTotalNodes(countNodesByFlowConfig(flowConfig.getId()));
        instance.setCreateTime(LocalDateTime.now());
        
        ApprovalInstance savedInstance = instanceRepository.save(instance);
        
        // 创建第一个审批节点记录
        createInitialApprovalNodes(savedInstance, flowConfig.getId());
        
        return savedInstance;
    }

    @Override
    public ApprovalInstance getApprovalInstanceById(Long instanceId) {
        return instanceRepository.findById(instanceId)
                .orElseThrow(() -> new IllegalArgumentException("审批实例不存在: " + instanceId));
    }

    @Override
    public ApprovalInstance getApprovalInstanceByBusiness(BusinessTypeEnum businessType, Long businessId) {
        return instanceRepository.findTopByBusinessTypeAndBusinessIdOrderByCreateTimeDesc(businessType, businessId)
                .orElseThrow(() -> new IllegalArgumentException("审批实例不存在: 业务类型=" + businessType + ", 业务ID=" + businessId));
    }

    @Override
    @Transactional
    public void withdrawApprovalInstance(Long instanceId, Long applicantId) {
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        
        // 验证申请人与实例申请人是否一致
        if (!instance.getApplicantId().equals(applicantId)) {
            throw new SecurityException("无权撤回此审批实例");
        }
        
        // 验证是否可以撤回（只有运行中或待审核状态可以撤回）
        if (!instance.getStatus().canWithdraw()) {
            throw new IllegalStateException("当前状态无法撤回审批");
        }
        
        // 更新实例状态
        instance.setStatus(ApprovalStatusEnum.CANCELED);
        instance.setFinishTime(LocalDateTime.now());
        instanceRepository.save(instance);
        
        log.info("审批实例已撤回: {}, 申请人: {}", instanceId, applicantId);
    }

    @Override
    @Transactional
    public ApprovalRecord approve(Long instanceId, Long approverId, String comment) {
        return processApprovalAction(instanceId, approverId, ApprovalActionEnum.APPROVE, comment);
    }

    @Override
    @Transactional
    public ApprovalRecord reject(Long instanceId, Long approverId, String comment) {
        return processApprovalAction(instanceId, approverId, ApprovalActionEnum.REJECT, comment);
    }

    @Override
    @Transactional
    public ApprovalRecord transfer(Long instanceId, Long currentApproverId, Long targetApproverId, String comment) {
        // 先验证当前审批人是否有权操作
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        ApprovalNode currentNode = getCurrentApprovalNode(instanceId);
        
        if (!currentNode.getApproverId().equals(currentApproverId)) {
            throw new SecurityException("无权转审此审批实例");
        }
        
        // 创建转审记录
        ApprovalRecord transferRecord = createApprovalRecord(instanceId, currentNode, ApprovalActionEnum.TRANSFER, comment);
        transferRecord.setTargetApproverId(targetApproverId);
        
        // 更新当前节点的审批人
        currentNode.setApproverId(targetApproverId);
        nodeRepository.save(currentNode);
        
        return transferRecord;
    }

    @Override
    @Transactional
    public ApprovalRecord skip(Long instanceId, Long approverId, String reason) {
        return processApprovalAction(instanceId, approverId, ApprovalActionEnum.SKIP, reason);
    }

    @Override
    public Page<ApprovalInstance> getPendingApprovals(Long approverId, Pageable pageable) {
        // 获取待审批节点列表
        List<ApprovalNode> pendingNodes = nodeRepository.findByApproverIdAndStatus(approverId, 0);
        List<Long> instanceIds = pendingNodes.stream()
                .map(ApprovalNode::getFlowConfigId) // 需要调整：应该关联到实例而不是配置
                .distinct()
                .collect(Collectors.toList());
        
        // 根据实例ID查询实例列表
        // TODO: 这里需要调整查询逻辑，需要添加相关查询方法
        return instanceRepository.findAll(pageable); // 简化实现
    }

    @Override
    public ApprovalNode getCurrentApprovalNode(Long instanceId) {
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        return nodeRepository.findByFlowConfigIdAndStepNumber(instance.getFlowConfigId(), instance.getCurrentNode())
                .orElseThrow(() -> new IllegalStateException("未找到当前审批节点"));
    }

    @Override
    public List<ApprovalNode> getApprovalNodePath(Long instanceId) {
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        return nodeRepository.findByFlowConfigIdOrderByStepNumberAsc(instance.getFlowConfigId());
    }

    @Override
    public ApprovalFlowConfig matchApplicableFlowConfig(BusinessTypeEnum businessType, BigDecimal amount, Long deptId) {
        // 先根据业务类型和状态查找启用的流程配置
        List<ApprovalFlowConfig> applicableConfigs = flowConfigRepository
                .findApplicableFlowConfigs(deptId, businessType, 1); // 状态1=启用
        
        if (applicableConfigs.isEmpty()) {
            // 如果没有部门特定的配置，查找通用的配置
            applicableConfigs = flowConfigRepository.findByBusinessTypeAndStatus(businessType, 1)
                    .map(List::of)
                    .orElse(List.of());
        }
        
        if (applicableConfigs.isEmpty()) {
            throw new IllegalArgumentException("未找到适用的审批流程配置: " + businessType);
        }
        
        // 根据金额阈值匹配最合适的配置
        return applicableConfigs.stream()
                .filter(config -> config.getMinAmountThreshold() == null || 
                                amount.compareTo(config.getMinAmountThreshold()) >= 0)
                .sorted((c1, c2) -> Integer.compare(c2.getPriority(), c1.getPriority())) // 优先级高的在前
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("未找到金额匹配的流程配置"));
    }

    @Override
    public List<ApprovalFlowConfig> getAllEnabledFlowConfigs() {
        return flowConfigRepository.findByStatus(1);
    }

    @Override
    public ApprovalStatistics getApprovalStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<ApprovalInstance> instances = instanceRepository.findByCreateTimeBetween(startTime, endTime);
        
        long total = instances.size();
        long pending = instances.stream().filter(i -> !i.getStatus().isFinalStatus()).count();
        long approved = instances.stream().filter(i -> i.getStatus() == ApprovalStatusEnum.APPROVED).count();
        long rejected = instances.stream().filter(i -> i.getStatus() == ApprovalStatusEnum.REJECTED).count();
        long canceled = instances.stream().filter(i -> i.getStatus() == ApprovalStatusEnum.CANCELED).count();
        
        Double avgProcessingHours = instanceRepository.findAverageApprovalDuration(startTime, endTime);
        if (avgProcessingHours != null) {
            avgProcessingHours = avgProcessingHours / 3600.0; // 秒转小时
        }
        
        return new ApprovalStatistics(total, pending, approved, rejected, canceled, avgProcessingHours);
    }

    @Override
    public boolean validateFlowConfig(ApprovalFlowConfig flowConfig) {
        if (flowConfig.getFlowCode() == null || flowConfig.getFlowCode().trim().isEmpty()) {
            return false;
        }
        if (flowConfig.getFlowName() == null || flowConfig.getFlowName().trim().isEmpty()) {
            return false;
        }
        if (flowConfig.getBusinessType() == null) {
            return false;
        }
        // 验证节点配置是否完整（简单验证）
        return true;
    }

    @Override
    public boolean checkVersionCompatibility(String flowCode, Integer fromVersion, Integer toVersion) {
        // 简化实现：只检查版本号是否递增
        return toVersion > fromVersion;
    }

    @Override
    public void cancelApprovalInstance(Long instanceId, String reason) {
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        instance.setStatus(ApprovalStatusEnum.CANCELED);
        instance.setFinishTime(LocalDateTime.now());
        instanceRepository.save(instance);
        
        log.info("审批实例已取消: {}, 原因: {}", instanceId, reason);
    }

    @Override
    public Page<ApprovalInstance> getProcessedApprovals(Long approverId, Pageable pageable) {
        // TODO: 实现已处理审批查询逻辑
        return instanceRepository.findAll(pageable);
    }

    @Override
    public Page<ApprovalInstance> getMyApplications(Long applicantId, Pageable pageable) {
        // TODO: 实现我发起的申请查询逻辑
        return instanceRepository.findAll(pageable);
    }

    @Override
    public List<ApprovalRecord> getApprovalRecords(Long instanceId) {
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        // TODO: 需要调整查询逻辑，目前是按配置ID查询
        return new ArrayList<>();
    }

    @Override
    public UserApprovalStatistics getUserApprovalStatistics(Long approverId, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 实现用户统计逻辑
        return new UserApprovalStatistics(0L, 0L, 0L, 0L, 0.0, Map.of());
    }

    @Override
    public BusinessApprovalStatistics getBusinessApprovalStatistics(BusinessTypeEnum businessType, LocalDateTime startTime, LocalDateTime endTime) {
        // TODO: 实现业务类型统计逻辑
        return new BusinessApprovalStatistics(0L, 0L, 0L, 0L, 0.0, 0.0);
    }

    @Override
    public List<ApprovalRecord> batchApprove(List<Long> instanceIds, Long approverId, String comment) {
        return instanceIds.stream()
                .map(instanceId -> approve(instanceId, approverId, comment))
                .collect(Collectors.toList());
    }

    @Override
    public void batchWithdraw(List<Long> instanceIds, Long applicantId, String reason) {
        instanceIds.forEach(instanceId -> withdrawApprovalInstance(instanceId, applicantId));
    }

    @Override
    public void remindApprovalTask(Long instanceId, Long approverId) {
        log.info("催办审批任务: 实例ID: {}, 审批人ID: {}", instanceId, approverId);
        // TODO: 实现催办通知逻辑
    }

    // 私有辅助方法
    private Long countNodesByFlowConfig(Long flowConfigId) {
        return nodeRepository.countByFlowConfigId(flowConfigId);
    }

    private void createInitialApprovalNodes(ApprovalInstance instance, Long flowConfigId) {
        // 根据流程配置创建初始审批节点
        List<ApprovalNode> nodes = nodeRepository.findByFlowConfigIdOrderByStepNumber(flowConfigId);
        
        if (nodes.isEmpty()) {
            throw new IllegalStateException("流程配置中未定义审批节点");
        }
        
        // 创建第一个审批记录
        ApprovalNode firstNode = nodes.get(0);
        createApprovalRecord(instance.getId(), firstNode, ApprovalActionEnum.CREATE, "创建审批实例");
    }

    private ApprovalRecord processApprovalAction(Long instanceId, Long approverId, ApprovalActionEnum action, String comment) {
        ApprovalInstance instance = getApprovalInstanceById(instanceId);
        ApprovalNode currentNode = getCurrentApprovalNode(instanceId);
        
        // 验证审批人权限
        if (!currentNode.getApproverId().equals(approverId)) {
            throw new SecurityException("无权审批此实例");
        }
        
        // 创建审批记录
        ApprovalRecord record = createApprovalRecord(instanceId, currentNode, action, comment);
        
        // 更新节点状态
        currentNode.setStatus(action.getStatusCode());
        currentNode.setProcessTime(LocalDateTime.now());
        nodeRepository.save(currentNode);
        
        // 处理审批结果
        handleApprovalResult(instance, action, currentNode);
        
        return record;
    }

    private ApprovalRecord createApprovalRecord(Long instanceId, ApprovalNode node, ApprovalActionEnum action, String comment) {
        ApprovalRecord record = new ApprovalRecord();
        record.setInstanceId(instanceId);
        record.setNodeId(node.getId());
        record.setApproverId(node.getApproverId());
        record.setApprovalAction(action);
        record.setApprovalComment(comment);
        record.setCreateTime(LocalDateTime.now());
        record.setApprovalStatus(getActionStatus(action));
        
        return recordRepository.save(record);
    }

    private ApprovalStatusEnum getActionStatus(ApprovalActionEnum action) {
        switch (action) {
            case APPROVE:
                return ApprovalStatusEnum.APPROVED;
            case REJECT:
                return ApprovalStatusEnum.REJECTED;
            case TRANSFER:
                return ApprovalStatusEnum.RUNNING;
            case SKIP:
                return ApprovalStatusEnum.RUNNING;
            case CANCEL:
                return ApprovalStatusEnum.CANCELED;
            default:
                return ApprovalStatusEnum.RUNNING;
        }
    }

    private void handleApprovalResult(ApprovalInstance instance, ApprovalActionEnum action, ApprovalNode currentNode) {
        switch (action) {
            case APPROVE:
                processApprovedCase(instance, currentNode);
                break;
            case REJECT:
                processRejectedCase(instance);
                break;
            case SKIP:
            case TRANSFER:
                // 继续流程，不做特殊处理
                break;
        }
    }

    private void processApprovedCase(ApprovalInstance instance, ApprovalNode currentNode) {
        // 更新已处理节点数
        instance.setProcessedNodes(instance.getProcessedNodes() + 1);
        
        // 检查是否是最后一个节点
        if (instance.getProcessedNodes() >= instance.getTotalNodes()) {
            instance.setStatus(ApprovalStatusEnum.APPROVED);
            instance.setFinishTime(LocalDateTime.now());
        } else {
            // 移动到下一个节点
            instance.setCurrentNode(currentNode.getStepNumber() + 1);
        }
        
        instanceRepository.save(instance);
    }

    private void processRejectedCase(ApprovalInstance instance) {
        instance.setStatus(ApprovalStatusEnum.REJECTED);
        instance.setFinishTime(LocalDateTime.now());
        instanceRepository.save(instance);
    }

    // 省略其他方法的实现...
}