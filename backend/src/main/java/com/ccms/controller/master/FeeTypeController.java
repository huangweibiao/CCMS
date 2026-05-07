package com.ccms.controller.master;

import com.ccms.entity.expense.ExpenseType;
import com.ccms.repository.expense.ExpenseTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 费用类型控制器
 * 对应设计文档：4.3节 费用类型表
 */
@RestController
@RequestMapping("/api/fee-types")
public class FeeTypeController {

    private final ExpenseTypeRepository expenseTypeRepository;

    @Autowired
    public FeeTypeController(ExpenseTypeRepository expenseTypeRepository) {
        this.expenseTypeRepository = expenseTypeRepository;
    }

    /**
     * 获取费用类型列表（分页）
     */
    @GetMapping
    public ResponseEntity<Page<ExpenseType>> getFeeTypeList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean enabled) {
        Page<ExpenseType> typePage;
        if (enabled != null) {
            if (enabled) {
                List<ExpenseType> enabledList = expenseTypeRepository.findByEnabledTrue();
                // 将List转换为Page
                int start = Math.min((int) PageRequest.of(page, size).getOffset(), enabledList.size());
                int end = Math.min((start + size), enabledList.size());
                typePage = new PageImpl<>(
                        enabledList.subList(start, end),
                        PageRequest.of(page, size),
                        enabledList.size()
                );
            } else {
                typePage = expenseTypeRepository.findAll(PageRequest.of(page, size));
            }
        } else {
            typePage = expenseTypeRepository.findAll(PageRequest.of(page, size));
        }
        return ResponseEntity.ok(typePage);
    }

    /**
     * 根据ID获取费用类型
     */
    @GetMapping("/{typeId}")
    public ResponseEntity<ExpenseType> getFeeTypeById(@PathVariable Long typeId) {
        return expenseTypeRepository.findById(typeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据编码获取费用类型
     */
    @GetMapping("/code/{typeCode}")
    public ResponseEntity<ExpenseType> getFeeTypeByCode(@PathVariable String typeCode) {
        ExpenseType type = expenseTypeRepository.findByTypeCode(typeCode);
        if (type != null) {
            return ResponseEntity.ok(type);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 创建费用类型
     */
    @PostMapping
    public ResponseEntity<ExpenseType> createFeeType(@RequestBody ExpenseType expenseType) {
        // 检查编码是否已存在
        ExpenseType existingType = expenseTypeRepository.findByTypeCode(expenseType.getTypeCode());
        if (existingType != null) {
            return ResponseEntity.badRequest().build();
        }
        ExpenseType saved = expenseTypeRepository.save(expenseType);
        return ResponseEntity.ok(saved);
    }

    /**
     * 更新费用类型
     */
    @PutMapping("/{typeId}")
    public ResponseEntity<ExpenseType> updateFeeType(@PathVariable Long typeId, @RequestBody ExpenseType expenseType) {
        Optional<ExpenseType> existingTypeOpt = expenseTypeRepository.findById(typeId);
        if (!existingTypeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        ExpenseType existingType = existingTypeOpt.get();
        
        // 如果修改了编码，检查新编码是否已存在
        if (!existingType.getTypeCode().equals(expenseType.getTypeCode())) {
            ExpenseType typeWithSameCode = expenseTypeRepository.findByTypeCode(expenseType.getTypeCode());
            if (typeWithSameCode != null && !typeWithSameCode.getId().equals(typeId)) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        // 保留原有ID和创建信息
        expenseType.setId(typeId);
        expenseType.setCreateTime(existingType.getCreateTime());
        expenseType.setCreateBy(existingType.getCreateBy());
        
        ExpenseType updated = expenseTypeRepository.save(expenseType);
        return ResponseEntity.ok(updated);
    }

    /**
     * 删除费用类型
     */
    @DeleteMapping("/{typeId}")
    public ResponseEntity<Void> deleteFeeType(@PathVariable Long typeId) {
        Optional<ExpenseType> typeOpt = expenseTypeRepository.findById(typeId);
        if (!typeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        // 检查是否有子类型
        Long childCount = expenseTypeRepository.countByParentId(typeId);
        if (childCount != null && childCount > 0) {
            return ResponseEntity.badRequest().build();
        }
        
        // 检查是否为系统内置类型
        ExpenseType type = typeOpt.get();
        if (Boolean.TRUE.equals(type.getSystem())) {
            return ResponseEntity.badRequest().build();
        }
        
        expenseTypeRepository.deleteById(typeId);
        return ResponseEntity.ok().build();
    }

    /**
     * 更新费用类型状态
     */
    @PutMapping("/{typeId}/status")
    public ResponseEntity<Map<String, Object>> updateFeeTypeStatus(
            @PathVariable Long typeId,
            @RequestParam Boolean enabled) {
        Optional<ExpenseType> typeOpt = expenseTypeRepository.findById(typeId);
        if (!typeOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        ExpenseType type = typeOpt.get();
        type.setEnabled(enabled);
        expenseTypeRepository.save(type);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "状态更新成功");
        result.put("typeId", typeId);
        result.put("enabled", enabled);
        return ResponseEntity.ok(result);
    }

    /**
     * 获取费用类型树形结构
     */
    @GetMapping("/tree")
    public ResponseEntity<List<Map<String, Object>>> getFeeTypeTree() {
        List<ExpenseType> allTypes = expenseTypeRepository.findAll();
        List<Map<String, Object>> tree = buildTree(allTypes, null);
        return ResponseEntity.ok(tree);
    }

    /**
     * 构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<ExpenseType> allTypes, Long parentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ExpenseType type : allTypes) {
            Long typeParentId = type.getParentId();
            boolean isMatch = (parentId == null && typeParentId == null) ||
                    (parentId != null && parentId.equals(typeParentId));
            
            if (isMatch) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", type.getId());
                node.put("typeCode", type.getTypeCode());
                node.put("typeName", type.getTypeName());
                node.put("parentId", type.getParentId());
                node.put("typeLevel", type.getTypeLevel());
                node.put("sortOrder", type.getSortOrder());
                node.put("enabled", type.getEnabled());
                node.put("description", type.getDescription());
                node.put("needApproval", type.getNeedApproval());
                node.put("approvalThreshold", type.getApprovalThreshold());
                node.put("budgetCategoryId", type.getBudgetCategoryId());
                node.put("budgetCategoryName", type.getBudgetCategoryName());
                node.put("system", type.getSystem());
                
                // 递归获取子节点
                List<Map<String, Object>> children = buildTree(allTypes, type.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                
                result.add(node);
            }
        }
        
        // 按排序号排序
        result.sort(Comparator.comparingInt(n -> {
            Object sortOrder = n.get("sortOrder");
            return sortOrder != null ? (Integer) sortOrder : 0;
        }));
        
        return result;
    }
}