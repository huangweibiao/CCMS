package com.ccms.service.report.impl;

import com.ccms.entity.report.ReportTemplate;
import com.ccms.repository.report.ReportTemplateRepository;
import com.ccms.service.report.ReportTemplateService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 报表模板服务实现类
 */
@Service
@Transactional
public class ReportTemplateServiceImpl implements ReportTemplateService {

    @Autowired
    private ReportTemplateRepository reportTemplateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ReportTemplate createTemplate(ReportTemplate template) {
        // 验证模板代码唯一性
        if (reportTemplateRepository.existsByTemplateCode(template.getTemplateCode())) {
            throw new RuntimeException("模板代码已存在：" + template.getTemplateCode());
        }
        
        // 设置默认值
        template.setCreateTime(LocalDateTime.now());
        template.setStatus(1); // 默认启用
        
        // 如果未设置排序号，自动生成
        if (template.getSortOrder() == null) {
            Integer maxSortOrder = reportTemplateRepository.findMaxSortOrderByType(template.getTemplateType());
            template.setSortOrder(maxSortOrder != null ? maxSortOrder + 1 : 0);
        }
        
        // 验证模板配置
        if (!validateTemplate(template)) {
            throw new RuntimeException("模板配置验证失败");
        }
        
        return reportTemplateRepository.save(template);
    }

    @Override
    public ReportTemplate updateTemplate(ReportTemplate template) {
        ReportTemplate existingTemplate = reportTemplateRepository.findById(template.getId())
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        // 验证模板代码唯一性（排除自身）
        if (!existingTemplate.getTemplateCode().equals(template.getTemplateCode())) {
            if (reportTemplateRepository.existsByTemplateCode(template.getTemplateCode())) {
                throw new RuntimeException("模板代码已存在：" + template.getTemplateCode());
            }
        }
        
        // 更新字段
        existingTemplate.setTemplateCode(template.getTemplateCode());
        existingTemplate.setTemplateName(template.getTemplateName());
        existingTemplate.setTemplateType(template.getTemplateType());
        existingTemplate.setTemplateDesc(template.getTemplateDesc());
        existingTemplate.setTemplateConfig(template.getTemplateConfig());
        existingTemplate.setReportQuery(template.getReportQuery());
        existingTemplate.setParamConfig(template.getParamConfig());
        existingTemplate.setColumnConfig(template.getColumnConfig());
        existingTemplate.setChartConfig(template.getChartConfig());
        existingTemplate.setSortOrder(template.getSortOrder());
        existingTemplate.setRemark(template.getRemark());
        existingTemplate.setUpdateTime(LocalDateTime.now());
        
        // 验证模板配置
        if (!validateTemplate(existingTemplate)) {
            throw new RuntimeException("模板配置验证失败");
        }
        
        return reportTemplateRepository.save(existingTemplate);
    }

    @Override
    public void deleteTemplate(Long id) {
        ReportTemplate template = reportTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
        
        // 系统模板不可删除
        if (template.getIsSystem()) {
            throw new RuntimeException("系统模板不可删除");
        }
        
        reportTemplateRepository.delete(template);
    }

    @Override
    public ReportTemplate getTemplateById(Long id) {
        return reportTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("模板不存在"));
    }

    @Override
    public Page<ReportTemplate> getTemplateList(String templateName, String templateType, Integer status, Pageable pageable) {
        if (templateName != null && templateType != null && status != null) {
            return reportTemplateRepository.findByTemplateNameContainingAndTemplateTypeAndStatus(
                    templateName, templateType, status, pageable);
        } else if (templateName != null && templateType != null) {
            return reportTemplateRepository.findByTemplateNameContainingAndTemplateTypeAndStatus(
                    templateName, templateType, 1, pageable);
        } else if (status != null && templateType != null) {
            return reportTemplateRepository.findByStatusAndTemplateType(status, templateType, pageable);
        } else if (status != null) {
            return reportTemplateRepository.findByStatus(status, pageable);
        } else {
            return reportTemplateRepository.findAll(pageable);
        }
    }

    @Override
    public List<ReportTemplate> getTemplatesByType(String templateType) {
        return reportTemplateRepository.findByTemplateTypeAndStatus(templateType, 1);
    }

    @Override
    public Map<String, Object> generateReport(String templateCode, Map<String, Object> params) {
        ReportTemplate template = getTemplateByCode(templateCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("template", template);
        result.put("params", params);
        
        // 这里应该根据具体的报表查询配置和参数来生成报表数据
        // 实际实现需要根据业务需求进行扩展
        try {
            // 模拟报表数据生成
            Map<String, Object> reportData = executeReportQuery(template, params);
            result.put("data", reportData);
            result.put("generatedTime", LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("报表生成失败：" + e.getMessage());
        }
        
        return result;
    }

    @Override
    public ReportTemplate copyTemplate(Long sourceTemplateId, String newTemplateName) {
        ReportTemplate sourceTemplate = getTemplateById(sourceTemplateId);
        
        ReportTemplate newTemplate = new ReportTemplate();
        newTemplate.setTemplateCode(generateTemplateCode());
        newTemplate.setTemplateName(newTemplateName);
        newTemplate.setTemplateType(sourceTemplate.getTemplateType());
        newTemplate.setTemplateDesc("复制的模板：" + sourceTemplate.getTemplateName());
        newTemplate.setTemplateConfig(sourceTemplate.getTemplateConfig());
        newTemplate.setReportQuery(sourceTemplate.getReportQuery());
        newTemplate.setParamConfig(sourceTemplate.getParamConfig());
        newTemplate.setColumnConfig(sourceTemplate.getColumnConfig());
        newTemplate.setChartConfig(sourceTemplate.getChartConfig());
        newTemplate.setCreateBy(sourceTemplate.getCreateBy());
        newTemplate.setIsSystem(false); // 复制的模板为非系统模板
        
        return createTemplate(newTemplate);
    }

    @Override
    public void setTemplateStatus(Long templateId, Integer status) {
        ReportTemplate template = getTemplateById(templateId);
        template.setStatus(status);
        template.setUpdateTime(LocalDateTime.now());
        reportTemplateRepository.save(template);
    }

    @Override
    public ReportTemplate getTemplateByCode(String templateCode) {
        return reportTemplateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("模板不存在：" + templateCode));
    }

    @Override
    public List<ReportTemplate> getActiveTemplates() {
        return reportTemplateRepository.findActiveTemplates();
    }

    @Override
    public boolean validateTemplate(ReportTemplate template) {
        // 基本验证
        if (template.getTemplateCode() == null || template.getTemplateCode().trim().isEmpty()) {
            return false;
        }
        
        if (template.getTemplateName() == null || template.getTemplateName().trim().isEmpty()) {
            return false;
        }
        
        if (template.getTemplateType() == null) {
            return false;
        }
        
        // JSON配置验证
        try {
            if (template.getTemplateConfig() != null && !template.getTemplateConfig().isEmpty()) {
                objectMapper.readValue(template.getTemplateConfig(), new TypeReference<Map<String, Object>>() {});
            }
            if (template.getParamConfig() != null && !template.getParamConfig().isEmpty()) {
                objectMapper.readValue(template.getParamConfig(), new TypeReference<Map<String, Object>>() {});
            }
            if (template.getColumnConfig() != null && !template.getColumnConfig().isEmpty()) {
                objectMapper.readValue(template.getColumnConfig(), new TypeReference<List<Map<String, Object>>>() {});
            }
            if (template.getChartConfig() != null && !template.getChartConfig().isEmpty()) {
                objectMapper.readValue(template.getChartConfig(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            return false;
        }
        
        return true;
    }

    /**
     * 生成唯一的模板代码
     */
    private String generateTemplateCode() {
        String baseCode = "TMP_" + System.currentTimeMillis();
        int counter = 1;
        String newCode = baseCode;
        
        while (reportTemplateRepository.existsByTemplateCode(newCode)) {
            newCode = baseCode + "_" + counter;
            counter++;
        }
        
        return newCode;
    }

    /**
     * 执行报表查询
     */
    private Map<String, Object> executeReportQuery(ReportTemplate template, Map<String, Object> params) {
        // 这里应该根据template.getReportQuery()来执行实际的查询
        // 实际实现需要根据具体的数据库查询逻辑来编写
        
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalRecords", 100);
        reportData.put("summary", "报表数据摘要");
        
        // 模拟数据
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("type", "bar");
        chartData.put("title", template.getTemplateName());
        reportData.put("chart", chartData);
        
        return reportData;
    }
}