package com.ccms.service.impl;

import com.ccms.entity.system.MessageTemplate;
import com.ccms.repository.MessageTemplateRepository;
import com.ccms.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 消息模板服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageTemplateServiceImpl implements MessageTemplateService {
    
    private final MessageTemplateRepository templateRepository;
    
    // 模板变量匹配正则表达式
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    @Override
    @Transactional
    public MessageTemplate createTemplate(TemplateRequest request) {
        // 验证模板代码唯一性
        if (templateRepository.existsByTemplateCode(request.getTemplateCode())) {
            throw new RuntimeException("模板代码已存在: " + request.getTemplateCode());
        }
        
        // 验证模板变量
        validateTemplateVariables(request.getContent(), request.getVariables());
        
        MessageTemplate template = new MessageTemplate();
        template.setTemplateCode(request.getTemplateCode());
        template.setTemplateName(request.getTemplateName());
        template.setTemplateType(request.getTemplateType());
        template.setContent(request.getContent());
        template.setChannels(request.getChannels());
        template.setVariables(request.getVariables());
        template.setDescription(request.getDescription());
        template.setIsActive(true);
        template.setCreatedBy(request.getCreatedBy());
        template.setCreatedTime(LocalDateTime.now());
        template.setUpdatedBy(request.getCreatedBy());
        template.setUpdatedTime(LocalDateTime.now());
        
        // 设置默认值
        template.setSendCount(0);
        template.setSuccessCount(0);
        template.setVersion(1);
        
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public MessageTemplate updateTemplate(String templateId, TemplateRequest request) {
        MessageTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        // 如果模板代码变更，验证唯一性
        if (!template.getTemplateCode().equals(request.getTemplateCode()) && 
            templateRepository.existsByTemplateCodeAndIdNot(request.getTemplateCode(), templateId)) {
            throw new RuntimeException("模板代码已存在: " + request.getTemplateCode());
        }
        
        // 验证模板变量
        validateTemplateVariables(request.getContent(), request.getVariables());
        
        template.setTemplateCode(request.getTemplateCode());
        template.setTemplateName(request.getTemplateName());
        template.setTemplateType(request.getTemplateType());
        template.setContent(request.getContent());
        template.setChannels(request.getChannels());
        template.setVariables(request.getVariables());
        template.setDescription(request.getDescription());
        template.setIsActive(request.getIsActive());
        template.setUpdatedBy(request.getUpdatedBy());
        template.setUpdatedTime(LocalDateTime.now());
        template.setVersion(template.getVersion() + 1);
        
        return templateRepository.save(template);
    }
    
    @Override
    @Transactional
    public void deleteTemplate(String templateId) {
        MessageTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
        
        if (template.getSendCount() > 0) {
            template.setIsActive(false);
            template.setUpdatedTime(LocalDateTime.now());
            templateRepository.save(template);
        } else {
            templateRepository.delete(template);
        }
    }
    
    @Override
    public MessageTemplate getTemplateById(String templateId) {
        return templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("模板不存在: " + templateId));
    }
    
    @Override
    public MessageTemplate getTemplateByCode(String templateCode) {
        return templateRepository.findByTemplateCode(templateCode)
            .orElseThrow(() -> new RuntimeException("模板不存在: " + templateCode));
    }
    
    @Override
    public Page<MessageTemplate> getTemplates(TemplateQuery query, Pageable pageable) {
        Specification<MessageTemplate> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(query.getTemplateCode())) {
                predicates.add(criteriaBuilder.like(root.get("templateCode"), "%" + query.getTemplateCode() + "%"));
            }
            
            if (StringUtils.hasText(query.getTemplateName())) {
                predicates.add(criteriaBuilder.like(root.get("templateName"), "%" + query.getTemplateName() + "%"));
            }
            
            if (query.getTemplateType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("templateType"), query.getTemplateType()));
            }
            
            if (query.getChannel() != null) {
                predicates.add(criteriaBuilder.isMember(query.getChannel(), root.get("channels")));
            }
            
            if (query.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isActive"), query.getIsActive()));
            }
            
            if (query.getCreatedTimeBegin() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdTime"), query.getCreatedTimeBegin()));
            }
            
            if (query.getCreatedTimeEnd() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdTime"), query.getCreatedTimeEnd()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return templateRepository.findAll(spec, pageable);
    }
    
    @Override
    public TemplateRenderResult renderTemplate(String templateCode, Map<String, Object> params) {
        MessageTemplate template = getTemplateByCode(templateCode);
        
        if (!template.getIsActive()) {
            throw new RuntimeException("模板已禁用: " + templateCode);
        }
        
        // 验证参数
        validateTemplateParams(template.getVariables(), params);
        
        // 渲染模板内容
        String renderedContent = renderTemplateContent(template.getContent(), params);
        
        // 构建结果
        TemplateRenderResult result = new TemplateRenderResult();
        result.setTemplateId(template.getId());
        result.setTemplateCode(template.getTemplateCode());
        result.setTemplateName(template.getTemplateName());
        result.setTemplateType(template.getTemplateType());
        result.setOriginalContent(template.getContent());
        result.setRenderedContent(renderedContent);
        result.setChannels(template.getChannels());
        result.setUsedVariables(getUsedVariables(renderedContent));
        result.setRenderTime(LocalDateTime.now());
        
        // 更新发送统计
        updateTemplateStatistics(template);
        
        return result;
    }
    
    @Override
    public TemplateStatistics getTemplateStatistics(String templateId) {
        MessageTemplate template = getTemplateById(templateId);
        
        TemplateStatistics statistics = new TemplateStatistics();
        statistics.setTemplateId(template.getId());
        statistics.setTemplateCode(template.getTemplateCode());
        statistics.setTemplateName(template.getTemplateName());
        statistics.setTotalSendCount(template.getSendCount());
        statistics.setSuccessSendCount(template.getSuccessCount());
        statistics.setFailSendCount(template.getSendCount() - template.getSuccessCount());
        
        // 计算成功率
        if (template.getSendCount() > 0) {
            statistics.setSuccessRate((double) template.getSuccessCount() / template.getSendCount() * 100);
        } else {
            statistics.setSuccessRate(0.0);
        }
        
        // 获取最近7天的发送统计
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> weeklyStats = templateRepository.findSendStatisticsByTemplateIdAndPeriod(
            templateId, weekAgo, LocalDateTime.now());
        
        Map<String, Integer> weeklyStatsMap = new HashMap<>();
        for (Object[] stat : weeklyStats) {
            String date = stat[0].toString();
            Integer count = ((Number) stat[1]).intValue();
            weeklyStatsMap.put(date, count);
        }
        
        statistics.setWeeklySendStats(weeklyStatsMap);
        
        return statistics;
    }
    
    @Override
    @Transactional
    public BatchImportResult importTemplates(List<TemplateRequest> requests, String operator) {
        BatchImportResult result = new BatchImportResult();
        
        for (TemplateRequest request : requests) {
            try {
                // 设置操作者
                if (request.getCreatedBy() == null) {
                    request.setCreatedBy(operator);
                }
                
                if (templateRepository.existsByTemplateCode(request.getTemplateCode())) {
                    MessageTemplate existing = templateRepository.findByTemplateCode(request.getTemplateCode()).get();
                    request.setUpdatedBy(operator);
                    updateTemplate(existing.getId(), request);
                    result.incrementUpdated();
                } else {
                    createTemplate(request);
                    result.incrementCreated();
                }
                
            } catch (Exception e) {
                log.error("导入模板失败: {}", request.getTemplateCode(), e);
                result.addFailedItem(request.getTemplateCode(), e.getMessage());
            }
        }
        
        return result;
    }
    
    @Override
    public List<TemplateExportItem> exportTemplates(List<String> templateIds) {
        List<MessageTemplate> templates;
        if (templateIds == null || templateIds.isEmpty()) {
            templates = templateRepository.findAll();
        } else {
            templates = templateRepository.findAllById(templateIds);
        }
        
        return templates.stream()
            .map(this::convertToExportItem)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> validateTemplateVariables(String content, List<String> definedVariables) {
        Set<String> usedVariables = extractVariables(content);
        Set<String> definedSet = definedVariables != null ? 
            new HashSet<>(definedVariables) : new HashSet<>();
        
        // 检查未定义的变量
        List<String> undefinedVariables = usedVariables.stream()
            .filter(var -> !definedSet.contains(var))
            .collect(Collectors.toList());
        
        // 检查未使用的定义变量
        List<String> unusedVariables = definedSet.stream()
            .filter(var -> !usedVariables.contains(var))
            .collect(Collectors.toList());
        
        List<String> warnings = new ArrayList<>();
        if (!undefinedVariables.isEmpty()) {
            warnings.add("未定义的变量: " + String.join(", ", undefinedVariables));
        }
        if (!unusedVariables.isEmpty()) {
            warnings.add("未使用的定义变量: " + String.join(", ", unusedVariables));
        }
        
        return warnings;
    }
    
    // ========== 私有方法 ==========
    
    private void validateTemplateVariables(String content, List<String> variables) {
        List<String> warnings = validateTemplateVariables(content, variables);
        if (!warnings.isEmpty()) {
            log.warn("模板变量验证警告: {}", String.join("; ", warnings));
        }
    }
    
    private void validateTemplateParams(List<String> requiredVariables, Map<String, Object> providedParams) {
        if (requiredVariables == null || requiredVariables.isEmpty()) {
            return;
        }
        
        List<String> missingParams = requiredVariables.stream()
            .filter(var -> !providedParams.containsKey(var) || providedParams.get(var) == null)
            .collect(Collectors.toList());
        
        if (!missingParams.isEmpty()) {
            throw new RuntimeException("缺少必要的模板参数: " + String.join(", ", missingParams));
        }
    }
    
    private String renderTemplateContent(String content, Map<String, Object> params) {
        if (content == null) {
            return "";
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = params.get(variableName);
            String replacement = value != null ? value.toString() : "";
            
            // 特殊处理null值，保持原始变量表达式
            if (value == null) {
                replacement = "${ " + variableName + " }";
            }
            
            matcher.appendReplacement(result, replacement.replace("\\", "\\\\").replace("$", "\\$"));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    private Set<String> extractVariables(String content) {
        Set<String> variables = new HashSet<>();
        if (content == null) {
            return variables;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
    
    private List<String> getUsedVariables(String renderedContent) {
        // 在渲染后的内容中查找实际使用的变量
        return new ArrayList<>(extractVariables(renderedContent));
    }
    
    @Transactional
    private void updateTemplateStatistics(MessageTemplate template) {
        template.setSendCount(template.getSendCount() + 1);
        template.setUpdatedTime(LocalDateTime.now());
        templateRepository.save(template);
    }
    
    private TemplateExportItem convertToExportItem(MessageTemplate template) {
        TemplateExportItem item = new TemplateExportItem();
        item.setTemplateCode(template.getTemplateCode());
        item.setTemplateName(template.getTemplateName());
        item.setTemplateType(template.getTemplateType());
        item.setContent(template.getContent());
        item.setChannels(template.getChannels());
        item.setVariables(template.getVariables());
        item.setDescription(template.getDescription());
        item.setIsActive(template.getIsActive());
        item.setSendCount(template.getSendCount());
        item.setSuccessCount(template.getSuccessCount());
        item.setVersion(template.getVersion());
        item.setCreatedBy(template.getCreatedBy());
        item.setCreatedTime(template.getCreatedTime());
        item.setUpdatedBy(template.getUpdatedBy());
        item.setUpdatedTime(template.getUpdatedTime());
        
        return item;
    }
}