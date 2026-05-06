package com.ccms.repository;

import com.ccms.entity.master.FeeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 费用类型数据访问接口
 */
@Repository
public interface FeeTypeRepository extends JpaRepository<FeeType, Long>, JpaSpecificationExecutor<FeeType> {

    /**
     * 根据状态查询费用类型
     */
    List<FeeType> findByStatus(Integer status);

    /**
     * 根据状态查询费用类型，按排序号排序
     */
    List<FeeType> findByStatusOrderBySortNoAsc(Integer status);

    /**
     * 根据费用类型编码查询
     */
    Optional<FeeType> findByTypeCode(String typeCode);

    /**
     * 根据预算控制标记查询
     */
    List<FeeType> findByBudgetControlFlag(Integer budgetControlFlag);

    /**
     * 根据发票需求标记查询
     */
    List<FeeType> findByInvoiceRequireFlag(Integer invoiceRequireFlag);

    /**
     * 根据类别和状态查询，按排序号排序
     */
    List<FeeType> findByCategoryAndStatusOrderBySortNo(Integer category, Integer status);

    /**
     * 获取所有费用类型并按parent_id和sort_no排序
     */
    @Query("SELECT ft FROM FeeType ft ORDER BY COALESCE(ft.parentId, 0), ft.sortNo")
    List<FeeType> findAllOrderByParentIdAndSortNo();

    /**
     * 批量更新预算控制标记
     */
    @Modifying
    @Query("UPDATE FeeType ft SET ft.budgetControlFlag = :budgetControlFlag, ft.updateTime = CURRENT_TIMESTAMP WHERE ft.id IN :ids")
    void batchUpdateBudgetControlFlag(@Param("ids") List<Long> ids, @Param("budgetControlFlag") Integer budgetControlFlag);

    /**
     * 批量更新发票需求标记
     */
    @Modifying
    @Query("UPDATE FeeType ft SET ft.invoiceRequireFlag = :invoiceRequireFlag, ft.updateTime = CURRENT_TIMESTAMP WHERE ft.id IN :ids")
    void batchUpdateInvoiceRequireFlag(@Param("ids") List<Long> ids, @Param("invoiceRequireFlag") Integer invoiceRequireFlag);

    /**
     * 根据父级ID查询子类型
     */
    List<FeeType> findByParentIdOrderBySortNo(Long parentId);

    /**
     * 查询根级费用类型（parent_id为null）
     */
    List<FeeType> findByParentIdIsNullOrderBySortNo();

    /**
     * 检查费用类型编码是否存在
     */
    boolean existsByTypeCode(String typeCode);

    /**
     * 查询启用的费用类型数量
     */
    long countByStatus(Integer status);

    /**
     * 根据费用类型名称模糊查询
     */
    @Query("SELECT ft FROM FeeType ft WHERE ft.typeName LIKE %:typeName%")
    List<FeeType> findByTypeNameContaining(String typeName);
}