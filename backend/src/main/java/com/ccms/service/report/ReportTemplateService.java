package com.ccms.service.report;

import com.ccms.entity.report.ReportTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 报表模板服务接口
 */
public interface ReportTemplateService {

    /**
     * 创建报表模板
     */
    ReportTemplate createTemplate(ReportTemplate template);

    /**
     * 更新报表模板
     */
    ReportTemplate updateTemplate(ReportTemplate template);

    /**
     * 删除报表模板
     */
    void deleteTemplate(Long id);

    /**
     * 根据ID获取模板
     */
    ReportTemplate getTemplateById(Long id);

    /**
     * 分页查询模板列表
     */
    Page<ReportTemplate> getTemplateList(String templateName, String templateType, Integer status, Pageable pageable);

    /**
     * 根据类型获取模板列表
     */
    List<ReportTemplate> getTemplatesByType(String templateType);

    /**
     * 生成报表数据
     */
    Map<String, Object> generateReport(String templateCode, Map<String, Object> params);

    /**
     * 复制模板
     */
    ReportTemplate copyTemplate(Long sourceTemplateId, String newTemplateName);

    /**
     * 设置模板状态
     */
    void setTemplateStatus(Long templateId, Integer status);

    /**
     * 根据模板代码获取模板
     */
    ReportTemplate getTemplateByCode(String templateCode);

    /**
     * 获取可用的模板列表
     */
    List<ReportTemplate> getActiveTemplates();

    /**
     * 校验模板配置
     */
    boolean validateTemplate(ReportTemplate template);
}