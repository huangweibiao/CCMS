package com.ccms.controller;

import com.ccms.dto.FlowConfigRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.PriorityTypeEnum;
import com.ccms.service.ApprovalFlowService;
import com.ccms.vo.ApprovalFlowConfigVO;
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
 * 审批流程配置控制器
 */
@RestController
@RequestMapping("/api/approval/flow-configs")
@Validated
public class ApprovalFlowConfigController {

    private static final Logger log = LoggerFactory.getLogger(ApprovalFlowConfigController.class);
    
    private final ApprovalFlowService approvalFlowService;
    
    public ApprovalFlowConfigController(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }

    /**
     * 创建审批流程配置
     */
    @PostMapping
    public ResponseEntity<ApprovalFlowConfig> createFlowConfig(@Valid @RequestBody FlowConfigRequest request) {
        log.info("创建审批流程配置: 流程代码={}, 流程名称={}, 业务类型={}", 
                request.getFlowCode(), request.getFlowName(), request.getBusinessType());
        
        ApprovalFlowConfig config = approvalFlowService.createApprovalFlowConfig(
                request.getFlowCode(),
                request.getFlowName(),
                request.getBusinessType(),
                request.getDescription(),
                request.getPriorityType()
        );
        
        log.info("审批流程配置创建成功: ID={}", config.getId());
        return ResponseEntity.ok(config);
    }

    /**
     * 更新审批流程配置
     */
    @PutMapping("/{configId}")
    public ResponseEntity<ApprovalFlowConfig> updateFlowConfig(
            @PathVariable Long configId,
            @Valid @RequestBody FlowConfigRequest request) {
        
        log.info("更新审批流程配置: ID={}", configId);
        
        ApprovalFlowConfig config = approvalFlowService.updateApprovalFlowConfig(
                configId,
                request.getFlowName(),
                request.getDescription(),
                request.getPriorityType(),
                request.getEnabled()
        );
        
        log.info("审批流程配置更新成功: ID={}", configId);
        return ResponseEntity.ok(config);
    }

    /**
     * 获取流程配置详情
     */
    @GetMapping("/{configId}")
    public ResponseEntity<ApprovalFlowConfigVO> getFlowConfig(@PathVariable Long configId) {
        log.info("获取流程配置详情: ID={}", configId);
        
        ApprovalFlowConfig config = approvalFlowService.getApprovalFlowConfig(configId);
        ApprovalFlowConfigVO configVO = convertToConfigVO(config);
        
        return ResponseEntity.ok(configVO);
    }

    /**
     * 删除流程配置
     */
    @DeleteMapping("/{configId}")
    public ResponseEntity<Void> deleteFlowConfig(@PathVariable Long configId) {
        log.info("删除流程配置: ID={}", configId);
        
        approvalFlowService.deleteApprovalFlowConfig(configId);
        
        log.info("流程配置删除成功: ID={}", configId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 启用/禁用流程配置
     */
    @PostMapping("/{configId}/toggle")
    public ResponseEntity<ApprovalFlowConfig> toggleFlowConfig(
            @PathVariable Long configId,
            @RequestParam boolean enabled) {
        
        log.info("{}流程配置: ID={}", enabled ? "启用" : "禁用", configId);
        
        approvalFlowService.toggleApprovalFlowConfig(configId, enabled);
        
        ApprovalFlowConfig config = approvalFlowService.getApprovalFlowConfig(configId);
        log.info("流程配置{}成功: ID={}", enabled ? "启用" : "禁用", configId);
        return ResponseEntity.ok(config);
    }

    /**
     * 查询流程配置列表
     */
    @GetMapping
    public ResponseEntity<PageVO<ApprovalFlowConfigVO>> getFlowConfigs(
            @RequestParam(required = false) BusinessTypeEnum businessType,
            @RequestParam(required = false) String flowCode,
            @RequestParam(defaultValue = "true") Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        log.info("查询流程配置列表: 业务类型={}, 流程代码={}, 页码={}, 大小={}", 
                businessType, flowCode, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "versionNumber"));
        Page<ApprovalFlowConfig> configs = approvalFlowService.getApprovalFlowConfigs(
                businessType, flowCode, enabled, pageable);
        
        PageVO<ApprovalFlowConfigVO> result = convertToPageVO(configs);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取所有业务类型对应的流程配置
     */
    @GetMapping("/by-business-type")
    public ResponseEntity<List<ApprovalFlowConfigVO>> getFlowConfigsByBusinessType(
            @RequestParam BusinessTypeEnum businessType) {
        
        log.info("根据业务类型查询流程配置: {}", businessType);
        
        List<ApprovalFlowConfig> configs = approvalFlowService.getApprovalFlowConfigsByBusinessType(businessType);
        List<ApprovalFlowConfigVO> configVOs = configs.stream()
                .map(this::convertToConfigVO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(configVOs);
    }

    /**
     * 获取最新版本的流程配置
     */
    @GetMapping("/latest")
    public ResponseEntity<List<ApprovalFlowConfigVO>> getLatestFlowConfigs() {
        log.info("获取最新版本的流程配置");
        
        List<ApprovalFlowConfig> configs = approvalFlowService.getLatestFlowConfigs();
        List<ApprovalFlowConfigVO> configVOs = configs.stream()
                .map(this::convertToConfigVO)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(configVOs);
    }

    /**
     * 创建流程配置版本
     */
    @PostMapping("/{configId}/versions")
    public ResponseEntity<ApprovalFlowConfig> createFlowConfigVersion(
            @PathVariable Long configId,
            @Valid @RequestBody FlowConfigRequest request) {
        
        log.info("创建流程配置新版本: 源配置ID={}, 版本描述={}", configId, request.getDescription());
        
        ApprovalFlowConfig newVersion = approvalFlowService.createFlowConfigVersion(
                configId,
                request.getFlowName(),
                request.getDescription(),
                request.getPriorityType()
        );
        
        log.info("流程配置新版本创建成功: 新配置ID={}", newVersion.getId());
        return ResponseEntity.ok(newVersion);
    }

    /**
     * 复制流程配置
     */
    @PostMapping("/{configId}/copy")
    public ResponseEntity<ApprovalFlowConfig> copyFlowConfig(
            @PathVariable Long configId,
            @RequestParam String newFlowCode,
            @RequestParam String newFlowName) {
        
        log.info("复制流程配置: 源配置ID={}, 新流程代码={}", configId, newFlowCode);
        
        ApprovalFlowConfig copiedConfig = approvalFlowService.copyApprovalFlowConfig(
                configId, newFlowCode, newFlowName);
        
        log.info("流程配置复制成功: 新配置ID={}", copiedConfig.getId());
        return ResponseEntity.ok(copiedConfig);
    }

    /**
     * 导入流程配置
     */
    @PostMapping("/import")
    public ResponseEntity<ApprovalFlowConfig> importFlowConfig(@Valid @RequestBody FlowConfigRequest request) {
        log.info("导入流程配置: 流程代码={}", request.getFlowCode());
        
        ApprovalFlowConfig config = approvalFlowService.importApprovalFlowConfig(
                request.getFlowCode(),
                request.getFlowName(),
                request.getBusinessType(),
                request.getDescription(),
                request.getPriorityType()
        );
        
        log.info("流程配置导入成功: ID={}", config.getId());
        return ResponseEntity.ok(config);
    }

    /**
     * 导出流程配置
     */
    @GetMapping("/{configId}/export")
    public ResponseEntity<ApprovalFlowConfig> exportFlowConfig(@PathVariable Long configId) {
        log.info("导出流程配置: ID={}", configId);
        
        ApprovalFlowConfig config = approvalFlowService.exportApprovalFlowConfig(configId);
        
        log.info("流程配置导出成功: ID={}", configId);
        return ResponseEntity.ok(config);
    }

    /**
     * 获取流程配置的节点列表
     */
    @GetMapping("/{configId}/nodes")
    public ResponseEntity<List<ApprovalNode>> getFlowConfigNodes(@PathVariable Long configId) {
        log.info("获取流程配置节点列表: 配置ID={}", configId);
        
        List<ApprovalNode> nodes = approvalFlowService.getApprovalNodesByFlowConfig(configId);
        
        return ResponseEntity.ok(nodes);
    }

    /**
     * 验证流程配置是否有效
     */
    @GetMapping("/{configId}/validate")
    public ResponseEntity<Boolean> validateFlowConfig(@PathVariable Long configId) {
        log.info("验证流程配置: ID={}", configId);
        
        boolean isValid = approvalFlowService.validateApprovalFlowConfig(configId);
        
        log.info("流程配置验证结果: ID={}, 有效={}", configId, isValid);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 根据业务类型和金额范围匹配流程配置
     */
    @GetMapping("/match")
    public ResponseEntity<ApprovalFlowConfig> matchFlowConfig(
            @RequestParam BusinessTypeEnum businessType,
            @RequestParam(required = false) Double amount,
            @RequestParam(required = false) PriorityTypeEnum priority) {
        
        log.info("匹配流程配置: 业务类型={}, 金额={}, 优先级={}", businessType, amount, priority);
        
        ApprovalFlowConfig config = approvalFlowService.matchApprovalFlowConfig(
                businessType, amount, priority);
        
        if (config == null) {
            log.warn("未找到匹配的流程配置");
            return ResponseEntity.notFound().build();
        }
        
        log.info("找到匹配的流程配置: ID={}, 流程代码={}", config.getId(), config.getFlowCode());
        return ResponseEntity.ok(config);
    }

    // 辅助方法
    private PageVO<ApprovalFlowConfigVO> convertToPageVO(Page<ApprovalFlowConfig> page) {
        List<ApprovalFlowConfigVO> content = page.getContent().stream()
                .map(this::convertToConfigVO)
                .collect(Collectors.toList());
        
        return PageVO.<ApprovalFlowConfigVO>builder()
                .content(content)
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .size(page.getSize())
                .number(page.getNumber())
                .build();
    }

    private ApprovalFlowConfigVO convertToConfigVO(ApprovalFlowConfig config) {
        ApprovalFlowConfigVO vo = new ApprovalFlowConfigVO();
        
        // 基础字段映射
        vo.setId(config.getId());
        vo.setFlowCode(config.getFlowCode());
        vo.setFlowName(config.getFlowName());
        
        // 业务类型转换
        if (config.getBusinessType() != null) {
            try {
                vo.setBusinessType(config.getBusinessTypeEnum());
                vo.setBusinessTypeName(config.getBusinessTypeEnum() != null ? config.getBusinessTypeEnum().getDescription() : null);
            } catch (Exception e) {
                // 如果枚举转换失败，设置默认值
                vo.setBusinessType(null);
                vo.setBusinessTypeName(config.getBusinessType());
            }
        }
        
        vo.setDescription(config.getDescription());
        
        // 状态转换（status 0-禁用 1-启用 -> Boolean enabled）
        if (config.getStatus() != null) {
            vo.setEnabled(config.getStatus() == 1);
        }
        
        // 优先级（数字越小优先级越高 -> PriorityTypeEnum）
        if (config.getPriority() != null) {
            // 简单的优先级转换逻辑
            vo.setPriorityType(config.getPriority() <= 50 ? PriorityTypeEnum.HIGH : 
                              config.getPriority() <= 100 ? PriorityTypeEnum.MEDIUM : PriorityTypeEnum.LOW);
            vo.setPriorityTypeName(vo.getPriorityType() != null ? vo.getPriorityType().getDescription() : null);
        }
        
        vo.setVersionNumber(config.getVersion());
        vo.setLatestVersion(true); // 简化处理，默认最新
        vo.setCreatedBy(config.getCreateBy());
        vo.setCreateTime(config.getCreateTime());
        vo.setUpdatedBy(config.getUpdateBy());
        vo.setUpdateTime(config.getUpdateTime());
        
        // 金额限制转换
        if (config.getMinAmount() != null) {
            vo.setAmountFloor(config.getMinAmount().doubleValue());
        }
        if (config.getMaxAmount() != null) {
            vo.setAmountLimit(config.getMaxAmount().doubleValue());
        }
        
        // 节点相关字段
        vo.setNodeCount(0); // 简化处理
        vo.setUsageCount(0L); // 简化处理
        vo.setAverageDuration(0.0); // 简化处理
        vo.setPassRate(100.0); // 简化处理
        
        return vo;
    }
}