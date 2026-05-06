package com.ccms.service.impl;

import com.ccms.entity.message.MessageTemplate;
import com.ccms.repository.message.MessageTemplateRepository;
import com.ccms.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageTemplateServiceImpl implements MessageTemplateService {
    
    private static final Logger log = LoggerFactory.getLogger(MessageTemplateServiceImpl.class);
    
    private final MessageTemplateRepository templateRepository;


    
    @Override
    public Optional<MessageTemplate> getTemplateByCode(String templateCode) {
        log.debug("Getting template by code: {}", templateCode);
        return templateRepository.findByTemplateCode(templateCode);
    }
    
    @Override
    @Transactional
    public TemplateOperationResult createTemplate(TemplateRequest request) {
        try {
            MessageTemplate template = new MessageTemplate();
            template.setTemplateCode(request.getTemplateCode());
            template.setTemplateName(request.getTemplateName());
            template.setTemplateContent(request.getContent());
            template.setTemplateType(request.getTemplateType());
            template.setDescription(request.getDescription());
            template.setCreatedTime(LocalDateTime.now());
            template.setCreatedBy(request.getCreatedBy());
            
            MessageTemplate savedTemplate = templateRepository.save(template);
            
            TemplateOperationResult result = new TemplateOperationResult();
            result.setSuccess(true);
            result.setMessage("模板创建成功");
            result.setTemplateId(savedTemplate.getId());
            result.setTemplate(savedTemplate);
            
            return result;
        } catch (Exception e) {
            log.error("创建模板失败: {}", e.getMessage(), e);
            TemplateOperationResult result = new TemplateOperationResult();
            result.setSuccess(false);
            result.setMessage("模板创建失败: " + e.getMessage());
            return result;
        }
    }
    
    @Override
    @Transactional
    public TemplateOperationResult updateTemplate(Long templateId, TemplateRequest request) {
        try {
            Optional<MessageTemplate> existingOpt = templateRepository.findById(templateId);
            if (!existingOpt.isPresent()) {
                TemplateOperationResult result = new TemplateOperationResult();
                result.setSuccess(false);
                result.setMessage("模板不存在");
                return result;
            }
            
            MessageTemplate template = existingOpt.get();
            template.setTemplateName(request.getTemplateName());
            template.setTemplateContent(request.getContent());
            template.setTemplateType(request.getTemplateType());
            template.setDescription(request.getDescription());
            template.setUpdatedTime(LocalDateTime.now());
            template.setUpdatedBy(request.getUpdatedBy());
            
            MessageTemplate updatedTemplate = templateRepository.save(template);
            
            TemplateOperationResult result = new TemplateOperationResult();
            result.setSuccess(true);
            result.setMessage("模板更新成功");
            result.setTemplateId(updatedTemplate.getId());
            result.setTemplate(updatedTemplate);
            
            return result;
        } catch (Exception e) {
            log.error("更新模板失败: {}", e.getMessage(), e);
            TemplateOperationResult result = new TemplateOperationResult();
            result.setSuccess(false);
            result.setMessage("模板更新失败: " + e.getMessage());
            return result;
        }
    }
    
    @Override
    @Transactional
    public boolean deleteTemplate(String templateCode) {
        try {
            Optional<MessageTemplate> templateOpt = templateRepository.findByTemplateCode(templateCode);
            if (templateOpt.isPresent()) {
                templateRepository.delete(templateOpt.get());
                log.info("删除模板成功: {}", templateCode);
                return true;
            }
            log.warn("删除模板失败，模板不存在: {}", templateCode);
            return false;
        } catch (Exception e) {
            log.error("删除模板失败: {}", e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public List<MessageTemplate> getAllTemplates() {
        log.debug("Getting all templates");
        return templateRepository.findAll();
    }
}