package com.ccms.service;

import com.ccms.entity.fee.FeeType;
import com.ccms.repository.FeeTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 费用类型服务类
 * 实现费用类型相关的业务逻辑
 */
@Service
@Transactional
public class FeeTypeService {

    @Autowired
    private FeeTypeRepository feeTypeRepository;

    /**
     * 获取所有启用的费用类型，按排序号排序
     */
    public List<FeeType> getAllActiveFeeTypes() {
        return feeTypeRepository.findByStatusOrderBySortNoAsc(1);
    }

    /**
     * 获取所有需要预算控制的费用类型
     */
    public List<FeeType> getBudgetControlledFeeTypes() {
        return feeTypeRepository.findByBudgetControlFlag(1);
    }

    /**
     * 获取所有需要发票的费用类型
     */
    public List<FeeType> getInvoiceRequiredFeeTypes() {
        return feeTypeRepository.findByInvoiceRequireFlag(1);
    }

    /**
     * 根据费用类型编码查找费用类型
     */
    public Optional<FeeType> findByTypeCode(String typeCode) {
        return feeTypeRepository.findByTypeCode(typeCode);
    }

    /**
     * 验证费用类型是否存在且启用
     */
    public boolean isValidFeeType(String typeCode) {
        Optional<FeeType> feeType = findByTypeCode(typeCode);
        return feeType.isPresent() && feeType.get().isEnabled();
    }

    /**
     * 验证费用类型是否需要预算控制
     */
    public boolean isBudgetControlRequired(String typeCode) {
        Optional<FeeType> feeType = findByTypeCode(typeCode);
        return feeType.isPresent() && feeType.get().isBudgetControlRequired();
    }

    /**
     * 验证费用类型是否需要发票
     */
    public boolean isInvoiceRequired(String typeCode) {
        Optional<FeeType> feeType = findByTypeCode(typeCode);
        return feeType.isPresent() && feeType.get().isInvoiceRequired();
    }

    /**
     * 创建新的费用类型
     */
    public FeeType createFeeType(FeeType feeType) {
        // 验证类型编码是否已存在
        if (findByTypeCode(feeType.getTypeCode()).isPresent()) {
            throw new RuntimeException("费用类型编码已存在: " + feeType.getTypeCode());
        }
        
        return feeTypeRepository.save(feeType);
    }

    /**
     * 更新费用类型
     */
    public FeeType updateFeeType(FeeType feeType) {
        FeeType existing = feeTypeRepository.findById(feeType.getId())
                .orElseThrow(() -> new RuntimeException("费用类型不存在: " + feeType.getId()));
        
        // 验证类型编码是否冲突（如果是修改了类型编码）
        if (!existing.getTypeCode().equals(feeType.getTypeCode())) {
            if (findByTypeCode(feeType.getTypeCode()).isPresent()) {
                throw new RuntimeException("费用类型编码已存在: " + feeType.getTypeCode());
            }
        }
        
        existing.setTypeCode(feeType.getTypeCode());
        existing.setTypeName(feeType.getTypeName());
        existing.setTypeDesc(feeType.getTypeDesc());
        existing.setStatus(feeType.getStatus());
        existing.setCategory(feeType.getCategory());
        existing.setBudgetControlFlag(feeType.getBudgetControlFlag());
        existing.setInvoiceRequireFlag(feeType.getInvoiceRequireFlag());
        existing.setSortNo(feeType.getSortNo());
        existing.setParentId(feeType.getParentId());
        existing.setUpdateBy(feeType.getUpdateBy());
        
        return feeTypeRepository.save(existing);
    }

    /**
     * 删除费用类型（逻辑删除）
     */
    public void deleteFeeType(Long id) {
        FeeType feeType = feeTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用类型不存在: " + id));
        
        // 如果是系统预设类型，不允许删除
        if (feeType.isSystemPreset()) {
            throw new RuntimeException("系统预设费用类型不允许删除");
        }
        
        feeType.setStatus(0); // 设置为禁用状态
        feeTypeRepository.save(feeType);
    }

    /**
     * 获取费用类型的完整层级结构
     */
    public List<FeeType> getFeeTypeHierarchy() {
        return feeTypeRepository.findAllOrderByParentIdAndSortNo();
    }

    /**
     * 根据ID获取费用类型
     */
    public Optional<FeeType> getFeeTypeById(Long id) {
        return feeTypeRepository.findById(id);
    }

    /**
     * 根据费用类别获取费用类型列表
     */
    public List<FeeType> getFeeTypesByCategory(Integer category) {
        return feeTypeRepository.findByCategoryAndStatusOrderBySortNo(category, 1);
    }

    /**
     * 批量更新费用类型的预算控制标记
     */
    public void batchUpdateBudgetControlFlag(List<Long> ids, Integer budgetControlFlag) {
        feeTypeRepository.batchUpdateBudgetControlFlag(ids, budgetControlFlag);
    }

    /**
     * 批量更新发票需求标记
     */
    public void batchUpdateInvoiceRequireFlag(List<Long> ids, Integer invoiceRequireFlag) {
        feeTypeRepository.batchUpdateInvoiceRequireFlag(ids, invoiceRequireFlag);
    }

    /**
     * 搜索费用类型
     */
    public List<FeeType> searchFeeTypes(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllActiveFeeTypes();
        }
        return feeTypeRepository.findByTypeNameContaining(keyword.trim());
    }

    /**
     * 检查费用类型是否允许删除
     */
    public boolean canDeleteFeeType(Long id) {
        Optional<FeeType> feeTypeOptional = getFeeTypeById(id);
        if (feeTypeOptional.isEmpty()) {
            return false;
        }
        
        FeeType feeType = feeTypeOptional.get();
        // 系统预设类型不允许删除
        return !feeType.isSystemPreset();
    }

    /**
     * 获取费用类型的完整路径（层级显示）
     */
    public String getFeeTypeFullPath(Long id) {
        Optional<FeeType> feeTypeOptional = getFeeTypeById(id);
        if (feeTypeOptional.isEmpty()) {
            return "";
        }
        
        FeeType feeType = feeTypeOptional.get();
        StringBuilder path = new StringBuilder(feeType.getTypeName());
        
        // 递归构建上级路径
        while (feeType.getParentId() != null) {
            Optional<FeeType> parentOptional = getFeeTypeById(feeType.getParentId());
            if (parentOptional.isPresent()) {
                feeType = parentOptional.get();
                path.insert(0, feeType.getTypeName() + " > ");
            } else {
                break;
            }
        }
        
        return path.toString();
    }

    /**
     * 验证费用类型数据
     */
    public void validateFeeType(FeeType feeType) {
        if (feeType.getTypeCode() == null || feeType.getTypeCode().trim().isEmpty()) {
            throw new RuntimeException("费用类型编码不能为空");
        }
        if (feeType.getTypeName() == null || feeType.getTypeName().trim().isEmpty()) {
            throw new RuntimeException("费用类型名称不能为空");
        }
        if (feeType.getCategory() == null) {
            throw new RuntimeException("费用类别不能为空");
        }
        
        // 验证编码格式（字母、数字、下划线）
        if (!feeType.getTypeCode().matches("^[A-Za-z0-9_]+$")) {
            throw new RuntimeException("费用类型编码只能包含字母、数字和下划线");
        }
    }

    /**
     * 启用/禁用费用类型
     */
    public FeeType toggleFeeTypeStatus(Long id, Long operatorId) {
        FeeType feeType = feeTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("费用类型不存在: " + id));
        
        feeType.setStatus(feeType.getStatus() == 1 ? 0 : 1);
        feeType.setUpdateBy(operatorId);
        
        return feeTypeRepository.save(feeType);
    }
}