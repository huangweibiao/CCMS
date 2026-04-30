package com.ccms.repository;

import com.ccms.entity.budget.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 费用类型Repository
 */
@Repository
public interface FeeTypeRepository extends JpaRepository<FeeType, Long>, JpaSpecificationExecutor<FeeType> {
    
    /**
     * 根据费用类型编码查询
     */
    Optional<FeeType> findByFeeTypeCode(String feeTypeCode);
    
    /**
     * 根据上级费用类型ID查询子费用类型
     */
    List<FeeType> findByParentId(Long parentId);
    
    /**
     * 查询启用状态下的费用类型
     */
    List<FeeType> findByStatus(Integer status);
    
    /**
     * 根据费用类型名称模糊查询
     */
    @Query("SELECT ft FROM FeeType ft WHERE ft.feeTypeName LIKE %:feeTypeName%")
    List<FeeType> findByFeeTypeNameContaining(String feeTypeName);
    
    /**
     * 查询某个业务类型下的费用类型
     */
    @Query("SELECT ft FROM FeeType ft WHERE ft.businessTypes LIKE %:businessType% AND ft.status = 1")
    List<FeeType> findByBusinessType(String businessType);
}