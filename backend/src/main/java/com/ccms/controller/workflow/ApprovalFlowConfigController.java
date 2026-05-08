package com.ccms.controller.workflow;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 审批流配置管理控制器
 * 对应设计文档：4.7.1 审批流配置表
 */
@RestController
@RequestMapping("/api/approval/flow-config")
public class ApprovalFlowConfigController {

    private final ApprovalFlowConfigRepository approvalFlowConfigRepository;

    @Autowired
    public ApprovalFlowConfigController(ApprovalFlowConfigRepository approvalFlowConfigRepository) {
        this.approvalFlowConfigRepository = approvalFlowConfigRepository;
    }

    /**
     * 获取审批流配置列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<ApprovalFlowConfig>> getFlowConfigList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String flowName,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer status) {
        
        List<ApprovalFlowConfig> allConfigs = approvalFlowConfigRepository.findAll();
        
        // 根据条件过滤
        List<ApprovalFlowConfig> filtered = allConfigs.stream()
                .filter(c -> flowName == null || c.getFlowName().contains(flowName))
                .filter(c -> businessType == null || businessType.equals(c.getBusinessType()))
                .filter(c -> status == null || status.equals(c.getStatus()))
                .sorted(Comparator.comparing(ApprovalFlowConfig::getCreateTime).reversed())
                .toList();
        
        // 手动分页
        int start = Math.min(page * size, filtered.size());
        int end = Math.min(start + size, filtered.size());
        Page<ApprovalFlowConfig> configPage = new PageImpl<>(
                filtered.subList(start, end),
                PageRequest.of(page, size),
                filtered.size()
        );
        
        return ResponseEntity.ok(configPage);
    }

    /**
     * 根据ID获取审批流配置
     */
    @GetMapping("/{configId}")
    public ResponseEntity<ApprovalFlowConfig> getFlowConfigById(@PathVariable Long configId) {
        return approvalFlowConfigRepository.findById(configId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据流程编码获取配置
     */
    @GetMapping("/code/{flowCode}")
    public ResponseEntity<ApprovalFlowConfig> getFlowConfigByCode(@PathVariable String flowCode) {
        return approvalFlowConfigRepository.findByFlowCode(flowCode)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据业务类型获取配置列表
     */
    @GetMapping("/business-type/{businessType}")
    public ResponseEntity<List<ApprovalFlowConfig>> getFlowConfigsByBusinessType(@PathVariable String businessType) {
        List<ApprovalFlowConfig> configs = approvalFlowConfigRepository.findAll().stream()
                .filter(c -> businessType.equals(c.getBusinessType()))
                .toList();
        return ResponseEntity.ok(configs);
    }

    /**
     * 创建审批流配置
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFlowConfig(@RequestBody ApprovalFlowConfig config) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查编码是否已存在
        if (approvalFlowConfigRepository.existsByFlowCode(config.getFlowCode())) {
            result.put("success", false);
            result.put("message", "流程编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 设置默认值
        if (config.getStatus() == null) {
            config.setStatus(1);
        }
        
        ApprovalFlowConfig saved = approvalFlowConfigRepository.save(config);
        result.put("success", true);
        result.put("message", "审批流配置创建成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    /**
     * 更新审批流配置
     */
    @PutMapping("/{configId}")
    public ResponseEntity<Map<String, Object>> updateFlowConfig(
            @PathVariable Long configId,
            @RequestBody ApprovalFlowConfig config) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<ApprovalFlowConfig> existingOpt = approvalFlowConfigRepository.findById(configId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "审批流配置不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 检查编码是否与其他配置冲突
        Optional<ApprovalFlowConfig> configWithCode = approvalFlowConfigRepository.findByFlowCode(config.getFlowCode());
        if (configWithCode.isPresent() && !configWithCode.get().getId().equals(configId)) {
            result.put("success", false);
            result.put("message", "流程编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        config.setId(configId);
        ApprovalFlowConfig updated = approvalFlowConfigRepository.save(config);
        result.put("success", true);
        result.put("message", "审批流配置更新成功");
        result.put("data", updated);
        return ResponseEntity.ok(result);
    }

    /**
     * 删除审批流配置
     */
    @DeleteMapping("/{configId}")
    public ResponseEntity<Map<String, Object>> deleteFlowConfig(@PathVariable Long configId) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<ApprovalFlowConfig> existingOpt = approvalFlowConfigRepository.findById(configId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "审批流配置不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        approvalFlowConfigRepository.deleteById(configId);
        result.put("success", true);
        result.put("message", "审批流配置删除成功");
        return ResponseEntity.ok(result);
    }

    /**
     * 启用/禁用审批流配置
     */
    @PutMapping("/{configId}/status")
    public ResponseEntity<Map<String, Object>> updateFlowConfigStatus(
            @PathVariable Long configId,
            @RequestParam Integer status) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<ApprovalFlowConfig> configOpt = approvalFlowConfigRepository.findById(configId);
        if (configOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "审批流配置不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        ApprovalFlowConfig config = configOpt.get();
        config.setStatus(status);
        approvalFlowConfigRepository.save(config);
        
        result.put("success", true);
        result.put("message", status == 1 ? "审批流配置已启用" : "审批流配置已禁用");
        return ResponseEntity.ok(result);
    }

    /**
     * 匹配适用的审批流
     * 根据业务类型、金额、部门、费用类型匹配最优审批流
     */
    @PostMapping("/match")
    public ResponseEntity<Map<String, Object>> matchFlowConfig(
            @RequestParam String businessType,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long feeTypeId) {
        Map<String, Object> result = new HashMap<>();
        
        // 查询所有启用的配置
        List<ApprovalFlowConfig> activeConfigs = approvalFlowConfigRepository.findByStatus(1);
        
        // 按条件筛选
        Optional<ApprovalFlowConfig> matched = activeConfigs.stream()
                .filter(c -> businessType.equals(c.getBusinessType()))
                .filter(c -> {
                    // 金额范围匹配
                    BigDecimal minAmount = c.getMinAmount();
                    BigDecimal maxAmount = c.getMaxAmount();
                    boolean minMatch = minAmount == null || amount.compareTo(minAmount) >= 0;
                    boolean maxMatch = maxAmount == null || amount.compareTo(maxAmount) <= 0;
                    return minMatch && maxMatch;
                })
                .filter(c -> {
                    // 部门匹配（未指定部门或匹配指定部门）
                    Long configDeptId = c.getDeptId();
                    return configDeptId == null || configDeptId.equals(deptId);
                })
                .filter(c -> {
                    // 费用类型匹配（未指定类型或匹配指定类型）
                    Long configFeeTypeId = c.getFeeTypeId();
                    return configFeeTypeId == null || configFeeTypeId.equals(feeTypeId);
                })
                .findFirst();
        
        if (matched.isPresent()) {
            result.put("success", true);
            result.put("message", "匹配到适用的审批流");
            result.put("data", matched.get());
        } else {
            result.put("success", false);
            result.put("message", "未找到适用的审批流配置");
        }
        
        return ResponseEntity.ok(result);
    }

    /**
     * 复制审批流配置
     */
    @PostMapping("/{configId}/copy")
    public ResponseEntity<Map<String, Object>> copyFlowConfig(
            @PathVariable Long configId,
            @RequestParam String newFlowCode,
            @RequestParam String newFlowName) {
        Map<String, Object> result = new HashMap<>();
        
        Optional<ApprovalFlowConfig> existingOpt = approvalFlowConfigRepository.findById(configId);
        if (existingOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "原审批流配置不存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        // 检查新编码是否已存在
        if (approvalFlowConfigRepository.existsByFlowCode(newFlowCode)) {
            result.put("success", false);
            result.put("message", "新流程编码已存在");
            return ResponseEntity.badRequest().body(result);
        }
        
        ApprovalFlowConfig existing = existingOpt.get();
        ApprovalFlowConfig newConfig = new ApprovalFlowConfig();
        newConfig.setFlowCode(newFlowCode);
        newConfig.setFlowName(newFlowName);
        newConfig.setBusinessType(existing.getBusinessType());
        newConfig.setMinAmount(existing.getMinAmount());
        newConfig.setMaxAmount(existing.getMaxAmount());
        newConfig.setDeptId(existing.getDeptId());
        newConfig.setFeeTypeId(existing.getFeeTypeId());
        newConfig.setFlowJson(existing.getFlowJson());
        newConfig.setStatus(1);
        
        ApprovalFlowConfig saved = approvalFlowConfigRepository.save(newConfig);
        result.put("success", true);
        result.put("message", "审批流配置复制成功");
        result.put("data", saved);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取审批流统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFlowConfigStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        List<ApprovalFlowConfig> allConfigs = approvalFlowConfigRepository.findAll();
        
        // 总数量
        stats.put("total", allConfigs.size());
        
        // 按状态统计
        long activeCount = allConfigs.stream().filter(c -> c.getStatus() != null && c.getStatus() == 1).count();
        long inactiveCount = allConfigs.stream().filter(c -> c.getStatus() == null || c.getStatus() == 0).count();
        stats.put("active", activeCount);
        stats.put("inactive", inactiveCount);
        
        // 按业务类型统计
        Map<String, Long> businessTypeCount = allConfigs.stream()
                .filter(c -> c.getBusinessType() != null)
                .collect(java.util.stream.Collectors.groupingBy(
                        ApprovalFlowConfig::getBusinessType,
                        java.util.stream.Collectors.counting()
                ));
        stats.put("byBusinessType", businessTypeCount);
        
        return ResponseEntity.ok(stats);
    }
}
