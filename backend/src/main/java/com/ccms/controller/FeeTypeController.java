package com.ccms.controller;

import com.ccms.entity.fee.FeeType;
import com.ccms.service.FeeTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 费用类型管理控制器
 */
@RestController
@RequestMapping("/api/fee-types")
public class FeeTypeController {

    @Autowired
    private FeeTypeService feeTypeService;

    /**
     * 获取所有启用的费用类型
     */
    @GetMapping
    public ResponseEntity<List<FeeType>> getAllActiveFeeTypes() {
        List<FeeType> feeTypes = feeTypeService.getAllActiveFeeTypes();
        return ResponseEntity.ok(feeTypes);
    }

    /**
     * 根据ID获取费用类型详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<FeeType> getFeeTypeById(@PathVariable Long id) {
        Optional<FeeType> feeTypeOptional = feeTypeService.getFeeTypeById(id);
        return feeTypeOptional.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 创建新的费用类型
     */
    @PostMapping
    public ResponseEntity<FeeType> createFeeType(@RequestBody FeeType feeType) {
        try {
            FeeType created = feeTypeService.createFeeType(feeType);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新费用类型
     */
    @PutMapping("/{id}")
    public ResponseEntity<FeeType> updateFeeType(@PathVariable Long id, @RequestBody FeeType feeType) {
        try {
            feeType.setId(id);
            FeeType updated = feeTypeService.updateFeeType(feeType);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除费用类型（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFeeType(@PathVariable Long id) {
        try {
            feeTypeService.deleteFeeType(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 获取需要预算控制的费用类型
     */
    @GetMapping("/budget-controlled")
    public ResponseEntity<List<FeeType>> getBudgetControlledFeeTypes() {
        List<FeeType> feeTypes = feeTypeService.getBudgetControlledFeeTypes();
        return ResponseEntity.ok(feeTypes);
    }

    /**
     * 获取需要发票的费用类型
     */
    @GetMapping("/invoice-required")
    public ResponseEntity<List<FeeType>> getInvoiceRequiredFeeTypes() {
        List<FeeType> feeTypes = feeTypeService.getInvoiceRequiredFeeTypes();
        return ResponseEntity.ok(feeTypes);
    }

    /**
     * 验证费用类型是否存在且启用
     */
    @GetMapping("/validate/{typeCode}")
    public ResponseEntity<Boolean> validateFeeType(@PathVariable String typeCode) {
        boolean isValid = feeTypeService.isValidFeeType(typeCode);
        return ResponseEntity.ok(isValid);
    }

    /**
     * 验证费用类型是否需要预算控制
     */
    @GetMapping("/budget-control/{typeCode}")
    public ResponseEntity<Boolean> checkBudgetControl(@PathVariable String typeCode) {
        boolean requiresBudgetControl = feeTypeService.isBudgetControlRequired(typeCode);
        return ResponseEntity.ok(requiresBudgetControl);
    }

    /**
     * 验证费用类型是否需要发票
     */
    @GetMapping("/invoice-require/{typeCode}")
    public ResponseEntity<Boolean> checkInvoiceRequire(@PathVariable String typeCode) {
        boolean requiresInvoice = feeTypeService.isInvoiceRequired(typeCode);
        return ResponseEntity.ok(requiresInvoice);
    }

    /**
     * 获取费用类型层级结构
     */
    @GetMapping("/hierarchy")
    public ResponseEntity<List<FeeType>> getFeeTypeHierarchy() {
        List<FeeType> hierarchy = feeTypeService.getFeeTypeHierarchy();
        return ResponseEntity.ok(hierarchy);
    }

    /**
     * 根据费用类别获取费用类型
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<FeeType>> getFeeTypesByCategory(@PathVariable Integer category) {
        List<FeeType> feeTypes = feeTypeService.getFeeTypesByCategory(category);
        return ResponseEntity.ok(feeTypes);
    }

    /**
     * 批量更新预算控制标记
     */
    @PutMapping("/batch-update/budget-control")
    public ResponseEntity<Void> batchUpdateBudgetControlFlag(@RequestBody List<Long> ids, 
                                                            @RequestParam Integer budgetControlFlag) {
        try {
            feeTypeService.batchUpdateBudgetControlFlag(ids, budgetControlFlag);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 批量更新发票需求标记
     */
    @PutMapping("/batch-update/invoice-require")
    public ResponseEntity<Void> batchUpdateInvoiceRequireFlag(@RequestBody List<Long> ids, 
                                                             @RequestParam Integer invoiceRequireFlag) {
        try {
            feeTypeService.batchUpdateInvoiceRequireFlag(ids, invoiceRequireFlag);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}