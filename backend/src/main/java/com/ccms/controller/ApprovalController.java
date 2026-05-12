package com.ccms.controller;

import com.ccms.dto.ApprovalRequest;
import com.ccms.dto.ApprovalResult;
import com.ccms.dto.ApprovalStatistics;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessType;
import com.ccms.service.ApprovalFlowService;
import com.ccms.vo.ApprovalInstanceVO;
import com.ccms.vo.ApprovalRecordVO;
import com.ccms.vo.ApprovalStatisticsVO;
import com.ccms.vo.PageVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 审批流程控制器
 */
@RestController
@RequestMapping("/api/approval")
@Validated
public class ApprovalController {

    private static final Logger log = LoggerFactory.getLogger(ApprovalController.class);
    
    private final ApprovalFlowService approvalFlowService;
    
    public ApprovalController(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }

    /**
     * 发起审批流程
     */
    @PostMapping("/start")
    public ResponseEntity<ApprovalResult> startApproval(@Valid @RequestBody ApprovalRequest request) {
        log.info("发起审批流程: 业务类型={}, 业务ID={}, 申请人ID={}", 
                request.getBusinessType(), request.getBusinessId(), request.getApplicantId());
        
        ApprovalInstance instance = approvalFlowService.startApprovalInstance(
                request.getBusinessType(),
                request.getBusinessId(),
                request.getApplicantId(),
                request.getTitle(),
                request.getContent()
        );
        
        ApprovalResult result = ApprovalResult.builder()
                .success(true)
                .message("审批流程发起成功")
                .instanceId(instance.getId())
                .flowConfigId(instance.getFlowConfigId())
                .currentNode(instance.getCurrentNode())
                .status(instance.getStatus())
                .build();
        
        log.info("审批流程发起成功: 实例ID={}", instance.getId());
        return ResponseEntity.ok(result);
    }

    /**
     * 审批同意
     */
    @PostMapping("/{instanceId}/approve")
    public ResponseEntity<ApprovalResult> approve(
            @PathVariable Long instanceId,
            @RequestParam(required = false) String remarks) {
        
        log.info("审批同意: 实例ID={}, 备注={}", instanceId, remarks);
        
        ApprovalInstance instance = approvalFlowService.approve(
                instanceId,
                getCurrentUserId(),
                remarks
        );
        
        ApprovalResult result = buildApprovalResult(instance, "审批成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 审批拒绝
     */
    @PostMapping("/{instanceId}/reject")
    public ResponseEntity<ApprovalResult> reject(
            @PathVariable Long instanceId,
            @RequestParam(required = false) String remarks) {
        
        log.info("审批拒绝: 实例ID={}, 备注={}", instanceId, remarks);
        
        ApprovalInstance instance = approvalFlowService.reject(
                instanceId,
                getCurrentUserId(),
                remarks
        );
        
        ApprovalResult result = buildApprovalResult(instance, "审批拒绝");
        return ResponseEntity.ok(result);
    }

    /**
     * 审批转审
     */
    @PostMapping("/{instanceId}/transfer")
    public ResponseEntity<ApprovalResult> transfer(
            @PathVariable Long instanceId,
            @RequestParam Long targetApproverId,
            @RequestParam(required = false) String remarks) {
        
        log.info("审批转审: 实例ID={}, 目标审批人ID={}", instanceId, targetApproverId);
        
        ApprovalInstance instance = approvalFlowService.transfer(
                instanceId,
                getCurrentUserId(),
                targetApproverId,
                remarks
        );
        
        ApprovalResult result = buildApprovalResult(instance, "转审成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 跳过当前节点
     */
    @PostMapping("/{instanceId}/skip")
    public ResponseEntity<ApprovalResult> skip(
            @PathVariable Long instanceId,
            @RequestParam(required = false) String remarks) {
        
        log.info("跳过当前节点: 实例ID={}", instanceId);
        
        ApprovalInstance instance = approvalFlowService.skip(
                instanceId,
                getCurrentUserId(),
                remarks
        );
        
        ApprovalResult result = buildApprovalResult(instance, "节点跳过成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 取消审批流程
     */
    @PostMapping("/{instanceId}/cancel")
    public ResponseEntity<ApprovalResult> cancel(
            @PathVariable Long instanceId,
            @RequestParam(required = false) String remarks) {
        
        log.info("取消审批流程: 实例ID={}", instanceId);
        
        ApprovalInstance instance = approvalFlowService.cancel(
                instanceId,
                getCurrentUserId(),
                remarks
        );
        
        ApprovalResult result = buildApprovalResult(instance, "审批流程已取消");
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批实例详情
     */
    @GetMapping("/instances/{instanceId}")
    public ResponseEntity<ApprovalInstanceVO> getApprovalInstance(@PathVariable Long instanceId) {
        log.info("获取审批实例详情: ID={}", instanceId);
        
        ApprovalInstance instance = approvalFlowService.getApprovalInstance(instanceId);
        ApprovalInstanceVO instanceVO = convertToInstanceVO(instance);
        
        return ResponseEntity.ok(instanceVO);
    }

    /**
     * 查询我的待审批列表
     */
    @GetMapping("/instances/pending")
    public ResponseEntity<PageVO<ApprovalInstanceVO>> getPendingInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询待审批列表: 用户ID={}, 页码={}, 大小={}", getCurrentUserId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ApprovalInstance> instances = approvalFlowService.getPendingInstances(getCurrentUserId(), pageable);
        
        PageVO<ApprovalInstanceVO> result = convertToPageVO(instances);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询我发起的审批列表
     */
    @GetMapping("/instances/initiated")
    public ResponseEntity<PageVO<ApprovalInstanceVO>> getInitiatedInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询我发起的审批列表: 用户ID={}, 页码={}, 大小={}", getCurrentUserId(), page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ApprovalInstance> instances = approvalFlowService.getMyInitiatedInstances(getCurrentUserId(), pageable);
        
        PageVO<ApprovalInstanceVO> result = convertToPageVO(instances);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询审批记录
     */
    @GetMapping("/records")
    public ResponseEntity<PageVO<ApprovalRecordVO>> getApprovalRecords(
            @RequestParam Long instanceId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询审批记录: 实例ID={}, 页码={}, 大小={}", instanceId, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ApprovalRecord> records = approvalFlowService.getApprovalRecords(instanceId, pageable);
        
        PageVO<ApprovalRecordVO> result = convertToRecordsPageVO(records);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApprovalStatisticsVO> getStatistics(
            @RequestParam(required = false) BusinessType businessType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("获取审批统计信息: 业务类型={}, 开始时间={}, 结束时间={}", businessType, startDate, endDate);
        
        ApprovalStatistics statistics = approvalFlowService.getApprovalStatistics(
                businessType, startDate, endDate);
        
        ApprovalStatisticsVO statisticsVO = convertToStatisticsVO(statistics);
        return ResponseEntity.ok(statisticsVO);
    }

    /**
     * 获取用户审批统计
     */
    @GetMapping("/statistics/user/{userId}")
    public ResponseEntity<ApprovalStatisticsVO> getUserStatistics(
            @PathVariable Long userId,
            @RequestParam(required = false) BusinessType businessType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("获取用户审批统计: 用户ID={}, 业务类型={}", userId, businessType);
        
        ApprovalStatistics statistics = approvalFlowService.getUserApprovalStatistics(
                userId, businessType, startDate, endDate);
        
        ApprovalStatisticsVO statisticsVO = convertToStatisticsVO(statistics);
        return ResponseEntity.ok(statisticsVO);
    }

    /**
     * 获取可用的流程配置列表
     */
    @GetMapping("/flow-configs")
    public ResponseEntity<List<ApprovalFlowConfig>> getAvailableFlowConfigs(
            @RequestParam(required = false) BusinessType businessType) {
        
        log.info("获取流程配置列表: 业务类型={}", businessType);
        
        List<ApprovalFlowConfig> configs = approvalFlowService.getAvailableFlowConfigs(businessType);
        return ResponseEntity.ok(configs);
    }

    /**
     * 审批撤回
     */
    @PostMapping("/{instanceId}/withdraw")
    public ResponseEntity<ApprovalResult> withdraw(
            @PathVariable Long instanceId,
            @RequestParam(required = false) String remarks) {
        
        log.info("审批撤回: 实例ID={}", instanceId);
        
        ApprovalInstance instance = approvalFlowService.withdraw(
                instanceId,
                getCurrentUserId(),
                remarks
        );
        
        ApprovalResult result = buildApprovalResult(instance, "审批撤回成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 查询所有审批实例（管理员使用）
     */
    @GetMapping("/instances/all")
    public ResponseEntity<PageVO<ApprovalInstanceVO>> getAllInstances(
            @RequestParam(required = false) BusinessType businessType,
            @RequestParam(required = false) ApprovalStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询所有审批实例: 业务类型={}, 状态={}, 关键词={}", businessType, status, keyword);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ApprovalInstance> instances = approvalFlowService.getAllInstances(
                businessType, status, keyword, pageable);
        
        PageVO<ApprovalInstanceVO> result = convertToPageVO(instances);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询我参与的审批列表（包括已审批的）
     */
    @GetMapping("/instances/involved")
    public ResponseEntity<PageVO<ApprovalInstanceVO>> getInvolvedInstances(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询我参与的审批列表: 用户ID={}", getCurrentUserId());
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ApprovalInstance> instances = approvalFlowService.getMyInvolvedInstances(getCurrentUserId(), pageable);
        
        PageVO<ApprovalInstanceVO> result = convertToPageVO(instances);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批流程图（节点状态）
     */
    @GetMapping("/{instanceId}/flow-chart")
    public ResponseEntity<List<ApprovalRecordVO>> getApprovalFlowChart(@PathVariable Long instanceId) {
        log.info("获取审批流程图: 实例ID={}", instanceId);
        
        List<ApprovalRecord> records = approvalFlowService.getApprovalFlowChart(instanceId);
        List<ApprovalRecordVO> result = records.stream()
                .map(this::convertToRecordVO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    /**
     * 批量审批操作
     */
    @PostMapping("/batch-approve")
    public ResponseEntity<ApprovalResult> batchApprove(@RequestBody List<Long> instanceIds) {
        log.info("批量审批: 实例数量={}", instanceIds.size());
        
        int successCount = approvalFlowService.batchApprove(instanceIds, getCurrentUserId());
        
        ApprovalResult result = ApprovalResult.builder()
                .success(true)
                .message(String.format("批量审批完成，成功处理 %d 个实例", successCount))
                .build();
        
        return ResponseEntity.ok(result);
    }

    // 辅助方法
    private ApprovalResult buildApprovalResult(ApprovalInstance instance, String message) {
        return ApprovalResult.builder()
                .success(true)
                .message(message)
                .instanceId(instance.getId())
                .flowConfigId(instance.getFlowConfigId())
                .currentNode(instance.getCurrentNode())
                .status(instance.getStatus())
                .completed(instance.getStatus().isFinalStatus())
                .build();
    }

    private PageVO<ApprovalInstanceVO> convertToPageVO(Page<ApprovalInstance> page) {
        List<ApprovalInstanceVO> content = page.getContent().stream()
                .map(this::convertToInstanceVO)
                .collect(Collectors.toList());
        
        return PageVO.<ApprovalInstanceVO>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .build();
    }

    private PageVO<ApprovalRecordVO> convertToRecordsPageVO(Page<ApprovalRecord> page) {
        List<ApprovalRecordVO> content = page.getContent().stream()
                .map(this::convertToRecordVO)
                .collect(Collectors.toList());
        
        return PageVO.<ApprovalRecordVO>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .build();
    }

    private ApprovalInstanceVO convertToInstanceVO(ApprovalInstance instance) {
        return ApprovalInstanceVO.builder()
                .id(instance.getId())
                .flowConfigId(instance.getFlowConfigId())
                .businessType(instance.getBusinessType())
                .businessId(instance.getBusinessId())
                .applicantId(instance.getApplicantId())
                .title(instance.getTitle())
                .content(instance.getContent())
                .currentNode(instance.getCurrentNode())
                .status(instance.getStatus())
                .createTime(instance.getCreateTime())
                .updateTime(instance.getUpdateTime())
                .build();
    }

    private ApprovalRecordVO convertToRecordVO(ApprovalRecord record) {
        return ApprovalRecordVO.builder()
                .id(record.getId())
                .instanceId(record.getInstanceId())
                .nodeId(record.getNodeId())
                .approverId(record.getApproverId())
                .action(record.getAction())
                .remarks(record.getRemarks())
                .createTime(record.getCreateTime())
                .build();
    }

    private ApprovalStatisticsVO convertToStatisticsVO(ApprovalStatistics statistics) {
        return ApprovalStatisticsVO.builder()
                .totalCount(statistics.getTotalCount())
                .pendingCount(statistics.getPendingCount())
                .approvedCount(statistics.getApprovedCount())
                .rejectedCount(statistics.getRejectedCount())
                .canceledCount(statistics.getCanceledCount())
                .averageApprovalDuration(statistics.getAverageApprovalDuration())
                .build();
    }

    /**
     * 获取当前用户ID（这里需要根据认证系统实现）
     */
    private Long getCurrentUserId() {
        // TODO: 根据认证系统获取当前用户ID
        // 这里返回一个模拟的用户ID
        return 1L;
    }
}