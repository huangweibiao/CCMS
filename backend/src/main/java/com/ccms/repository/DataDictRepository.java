package com.ccms.repository;

import com.ccms.entity.system.DataDict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 数据字典数据访问接口
 */
@Repository
public interface DataDictRepository extends JpaRepository<DataDict, Long> {

    /**
     * 根据字典类型和字典码查找
     */
    Optional<DataDict> findByDictTypeAndDictCode(String dictType, String dictCode);

    /**
     * 根据字典类型查找启用的字典项
     */
    List<DataDict> findByDictTypeAndStatusOrderBySortOrderAsc(String dictType);

    /**
     * 根据字典类型查找所有字典项
     */
    List<DataDict> findByDictTypeOrderBySortOrderAsc(String dictType);

    /**
     * 查找所有启用的字典项
     */
    List<DataDict> findByStatusOrderByDictTypeAscSortOrderAsc(Integer status);

    /**
     * 检查字典类型和代码组合是否已存在
     */
    boolean existsByDictTypeAndDictCode(String dictType, String dictCode);

    /**
     * 查找指定父级的字典项
     */
    List<DataDict> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * 查找顶层字典项（无父级）
     */
    List<DataDict> findByParentIsNullOrderByDictTypeAsc();

    /**
     * 根据字典项ID查找其所有子项
     */
    @Query("SELECT d FROM DataDict d WHERE d.parent.id = ?1 ORDER BY d.sortOrder ASC")
    List<DataDict> findChildrenByParentId(Long parentId);

    /**
     * 获取所有不同的字典类型
     */
    @Query("SELECT DISTINCT d.dictType FROM DataDict d WHERE d.status = 1")
    List<String> findAllDistinctDictTypes();

    /**
     * 统计指定字典类型的字典项数量
     */
    long countByDictType(String dictType);

    /**
     * 查找内置字典项
     */
    List<DataDict> findByBuiltInTrueOrderByDictTypeAscSortOrderAsc();
}