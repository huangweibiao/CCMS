package com.ccms.repository.approval;

import com.ccms.cache.BasicApprovalCacheConfig;
import com.ccms.cache.ApprovalCacheService;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.enums.BusinessTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 审批流配置Repository实现类
 * 提供缓存增强的数据访问功能
 */
@Repository
public class ApprovalFlowConfigRepositoryImpl {

    @Autowired
    private ApprovalFlowConfigRepository approvalFlowConfigRepository;
    
    @Autowired
    private ApprovalCacheService approvalCacheService;

    /**
     * 根据ID查询流程配置（带缓存）
     */
    @Cacheable(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigKey(#id)")
    public Optional<ApprovalFlowConfig> findByIdWithCache(Long id) {
        return approvalFlowConfigRepository.findById(id);
    }

    /**
     * 保存流程配置（更新缓存）
     */
    @CachePut(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
              key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigKey(#config.id)")
    public ApprovalFlowConfig saveWithCache(ApprovalFlowConfig config) {
        // 保存前清除相关的列表缓存
        evictRelatedListCaches(config.getBusinessType(), config.getCategory());
        return approvalFlowConfigRepository.save(config);
    }

    /**
     * 删除流程配置（清除缓存）
     */
    @CacheEvict(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
                key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigKey(#id)")
    public void deleteByIdWithCache(Long id) {
        Optional<ApprovalFlowConfig> config = approvalFlowConfigRepository.findById(id);
        config.ifPresent(c -> evictRelatedListCaches(c.getBusinessType(), c.getCategory()));
        approvalFlowConfigRepository.deleteById(id);
    }

    /**
     * 根据业务类型查询启用的流程配置（带缓存）
     */
    @Cacheable(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigListKey(#businessType.name(), 'all')")
    public List<ApprovalFlowConfig> findEnabledByBusinessType(BusinessTypeEnum businessType) {
        return approvalFlowConfigRepository.findByBusinessTypeAndStatus(businessType, 1)
                .map(List::of)
                .orElse(List.of());
    }

    /**
     * 根据业务类型、类别和状态查询流程配置（带缓存）
     */
    @Cacheable(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigListKey(#businessType.name(), #category)")
    public List<ApprovalFlowConfig> findByBusinessTypeAndCategoryAndStatus(BusinessTypeEnum businessType, 
                                                                        String category, Integer status) {
        return approvalFlowConfigRepository.findByBusinessType(businessType).stream()
                .filter(config -> config.getCategory().equals(category))
                .filter(config -> config.getStatus().equals(status))
                .sorted((c1, c2) -> c2.getVersion().compareTo(c1.getVersion()))
                .toList();
    }

    /**
     * 根据金额阈值查询适用的流程配置（带缓存）
     */
    @Cacheable(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigListKey(#businessType.name(), 'amount_' + #amount)")
    public List<ApprovalFlowConfig> findFlowConfigsByAmountThreshold(BusinessTypeEnum businessType, 
                                                                BigDecimal amount, Integer status) {
        return approvalFlowConfigRepository.findFlowConfigsByAmountThreshold(businessType, amount, status);
    }

    /**
     * 根据部门查询适用的流程配置（带缓存）
     */
    @Cacheable(value = BasicApprovalCacheConfig.CacheNames.FLOW_CONFIG, 
               key = "T(com.ccms.cache.BasicApprovalCacheConfig$CacheKeyGenerator).generateFlowConfigListKey(#businessType.name(), 'dept_' + #deptId)")
    public List<ApprovalFlowConfig> findApplicableFlowConfigs(Long deptId, BusinessTypeEnum businessType, Integer status) {
        return approvalFlowConfigRepository.findApplicableFlowConfigs(deptId, businessType, status);
    }

    /**
     * 清除所有流程配置缓存
     */
    public void clearAllFlowConfigCache() {
        approvalCacheService.clearAllApprovalCache();
    }

    /**
     * 重新加载特定流程配置到缓存
     */
    public void reloadFlowConfigToCache(Long id) {
        approvalFlowConfigRepository.findById(id).ifPresent(approvalCacheService::updateFlowConfigCache);
    }

    /**
     * 批量重新加载流程配置到缓存
     */
    public void reloadFlowConfigsToCache(List<Long> ids) {
        List<ApprovalFlowConfig> configs = approvalFlowConfigRepository.findAllById(ids);
        configs.forEach(approvalCacheService::updateFlowConfigCache);
    }

    /**
     * 获取缓存统计信息
     */
    public Object getCacheStats() {
        return approvalCacheService.getCacheStats();
    }

    /**
     * 清除相关的列表缓存
     */
    private void evictRelatedListCaches(String businessType, String category) {
        // 清除与业务类型相关的所有列表缓存
        approvalCacheService.evictFlowConfigCache(null); // 这里会清除所有列表缓存
    }
}