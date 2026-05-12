package com.ccms.repository.approval;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.enums.BusinessTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 审批流配置表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface ApprovalFlowConfigRepository extends JpaRepository<ApprovalFlowConfig, Long> {

    /**
     * 根据流程编码查询配置
     * 
     * @param flowCode 流程编码
     * @return 流程配置
     */
    Optional<ApprovalFlowConfig> findByFlowCode(String flowCode);

    /**
     * 根据流程名称查询配置
     * 
     * @param flowName 流程名称
     * @return 流程配置
     */
    Optional<ApprovalFlowConfig> findByFlowName(String flowName);

    /**
     * 根据业务类型枚举查询配置
     * 
     * @param businessType 业务类型枚举
     * @return 流程配置列表
     */
    List<ApprovalFlowConfig> findByBusinessType(BusinessTypeEnum businessType);

    /**
     * 根据状态查询配置
     * 
     * @param status 状态：0-禁用，1-启用
     * @return 流程配置列表
     */
    List<ApprovalFlowConfig> findByStatus(Integer status);

    /**
     * 查询所有启用的流程配置
     * 
     * @return 启用配置列表
     */
    List<ApprovalFlowConfig> findByStatusOrderByCreateTimeDesc(Integer status);

    /**
     * 根据业务类型和状态查询配置
     * 
     * @param businessType 业务类型枚举
     * @param status 状态
     * @return 流程配置
     */
    Optional<ApprovalFlowConfig> findByBusinessTypeAndStatus(BusinessTypeEnum businessType, Integer status);

    /**
     * 检查流程编码是否存在
     * 
     * @param flowCode 流程编码
     * @return 是否存在
     */
    boolean existsByFlowCode(String flowCode);

    /**
     * 根据部门ID查询适用的流程配置
     * 
     * @param deptId 部门ID
     * @param businessType 业务类型枚举
     * @param status 状态
     * @return 流程配置列表
     */
    @Query("SELECT afc FROM ApprovalFlowConfig afc WHERE (afc.applicableDeptIds LIKE %:deptId% OR afc.applicableDeptIds = '' OR afc.applicableDeptIds IS NULL) AND afc.businessType = :businessType AND afc.status = :status")
    List<ApprovalFlowConfig> findApplicableFlowConfigs(@Param("deptId") Long deptId, @Param("businessType") BusinessTypeEnum businessType, @Param("status") Integer status);

    /**
     * 根据最小金额阈值查询适用于特定金额的流程配置
     * 
     * @param businessType 业务类型枚举
     * @param amount 金额
     * @param status 状态
     * @return 流程配置列表
     */
    @Query("SELECT afc FROM ApprovalFlowConfig afc WHERE afc.businessType = :businessType AND afc.minAmountThreshold <= :amount AND afc.status = :status ORDER BY afc.minAmountThreshold DESC")
    List<ApprovalFlowConfig> findFlowConfigsByAmountThreshold(@Param("businessType") BusinessTypeEnum businessType, @Param("amount") java.math.BigDecimal amount, @Param("status") Integer status);

    /**
     * 根据版本号查询流程配置（降序排列，获取最新版本）
     * 
     * @param flowCode 流程编码
     * @return 流程配置列表
     */
    List<ApprovalFlowConfig> findByFlowCodeOrderByVersionDesc(String flowCode);

    /**
     * 根据流程编码和版本号查询配置
     * 
     * @param flowCode 流程编码
     * @param version 版本号
     * @return 流程配置
     */
    Optional<ApprovalFlowConfig> findByFlowCodeAndVersion(String flowCode, Integer version);

    /**
     * 查询最新的流程配置版本
     * 
     * @param flowCode 流程编码
     * @return 最新版本的流程配置
     */
    @Query("SELECT afc FROM ApprovalFlowConfig afc WHERE afc.flowCode = :flowCode ORDER BY afc.version DESC LIMIT 1")
    Optional<ApprovalFlowConfig> findLatestVersionByFlowCode(@Param("flowCode") String flowCode);

    // 别名方法，与findLatestVersionByFlowCode作用相同
    default Optional<ApprovalFlowConfig> findLatestByFlowCode(String flowCode) {
        return findLatestVersionByFlowCode(flowCode);
    }
}