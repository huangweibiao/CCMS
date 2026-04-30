package com.ccms.repository.report;

import com.ccms.entity.report.ReportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 报表模板仓库接口
 */
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long>, JpaSpecificationExecutor<ReportTemplate> {

    /**
     * 根据模板代码查询
     */
    Optional<ReportTemplate> findByTemplateCode(String templateCode);

    /**
     * 根据状态查询模板列表
     */
    List<ReportTemplate> findByStatus(Integer status);

    /**
     * 根据状态分页查询模板
     */
    Page<ReportTemplate> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据模板类型查询
     */
    List<ReportTemplate> findByTemplateTypeAndStatus(String templateType, Integer status);

    /**
     * 根据模板类型查询（不包含状态限制）
     */
    List<ReportTemplate> findByTemplateType(String templateType);

    /**
     * 根据名称模糊查询
     */
    List<ReportTemplate> findByTemplateNameContainingAndStatus(String templateName, Integer status);

    /**
     * 根据名称和类型查询
     */
    Page<ReportTemplate> findByTemplateNameContainingAndTemplateTypeAndStatus(
            String templateName, String templateType, Integer status, Pageable pageable);

    /**
     * 根据状态和类型分页查询
     */
    Page<ReportTemplate> findByStatusAndTemplateType(Integer status, String templateType, Pageable pageable);

    /**
     * 查询所有启用的模板
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.status = 1 ORDER BY t.sortOrder ASC, t.createTime DESC")
    List<ReportTemplate> findActiveTemplates();

    /**
     * 根据模板代码检查是否存在
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * 统计各类模板数量
     */
    @Query("SELECT t.templateType, COUNT(t) FROM ReportTemplate t WHERE t.status = 1 GROUP BY t.templateType")
    List<Object[]> countTemplatesByType();

    /**
     * 根据系统标记查询
     */
    List<ReportTemplate> findByIsSystem(Boolean isSystem);

    /**
     * 查找最大排序号
     */
    @Query("SELECT MAX(t.sortOrder) FROM ReportTemplate t WHERE t.templateType = :templateType")
    Integer findMaxSortOrderByType(@Param("templateType") String templateType);

    /**
     * 根据排序号查询下一个模板
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.sortOrder > :sortOrder AND t.templateType = :templateType " +
           "ORDER BY t.sortOrder ASC LIMIT 1")
    Optional<ReportTemplate> findNextBySortOrder(@Param("sortOrder") Integer sortOrder, 
                                                @Param("templateType") String templateType);

    /**
     * 根据排序号查询上一个模板
     */
    @Query("SELECT t FROM ReportTemplate t WHERE t.sortOrder < :sortOrder AND t.templateType = :templateType " +
           "ORDER BY t.sortOrder DESC LIMIT 1")
    Optional<ReportTemplate> findPreviousBySortOrder(@Param("sortOrder") Integer sortOrder, 
                                                   @Param("templateType") String templateType);
}