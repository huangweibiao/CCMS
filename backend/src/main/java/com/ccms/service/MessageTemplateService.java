package com.ccms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 消息模板管理服务接口
 * 
 * @author 系统生成
 */
public interface MessageTemplateService {
    
    /**
     * 模板状态
     */
    enum TemplateStatus {
        ACTIVE("启用"),
        INACTIVE("停用"),
        DRAFT("草稿"),
        ARCHIVED("已归档");
        
        private final String description;
        
        TemplateStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 模板类型
     */
    enum TemplateType {
        EMAIL("邮件模板"),
        SMS("短信模板"),
        INNER_MESSAGE("站内信模板"),
        NOTIFICATION("通知模板"),
        TODO_REMINDER("待办提醒模板"),
        SYSTEM_ALERT("系统警报模板");
        
        private final String description;
        
        TemplateType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 模板变量类型
     */
    enum VarType {
        STRING("字符串"),
        NUMBER("数字"),
        DATE("日期"),
        TIME("时间"),
        DATETIME("日期时间"),
        BOOLEAN("布尔值");
        
        private final String description;
        
        VarType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 模板变量定义
     */
    class TemplateVariable {
        private String varName;
        private String displayName;
        private VarType varType;
        private boolean required;
        private String defaultValue;
        private String description;
        private String validationRegex;
        
        public TemplateVariable() {}
        
        public TemplateVariable(String varName, String displayName, VarType varType, boolean required) {
            this.varName = varName;
            this.displayName = displayName;
            this.varType = varType;
            this.required = required;
        }
        
        public String getVarName() { return varName; }
        public void setVarName(String varName) { this.varName = varName; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public VarType getVarType() { return varType; }
        public void setVarType(VarType varType) { this.varType = varType; }
        
        public boolean isRequired() { return required; }
        public void setRequired(boolean required) { this.required = required; }
        
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getValidationRegex() { return validationRegex; }
        public void setValidationRegex(String validationRegex) { this.validationRegex = validationRegex; }
    }
    
    /**
     * 消息模板
     */
    class MessageTemplate {
        private Long templateId;
        private String templateCode;
        private String templateName;
        private TemplateType templateType;
        private String subjectTemplate;
        private String contentTemplate;
        private String description;
        private TemplateStatus status;
        private String bizType;
        private String version;
        private List<TemplateVariable> variables;
        private Long createdBy;
        private String createdByName;
        private LocalDateTime createdTime;
        private Long updatedBy;
        private LocalDateTime updatedTime;
        private Integer usageCount;
        private LocalDateTime lastUsedTime;
        
        public MessageTemplate() {}
        
        public MessageTemplate(String templateCode, String templateName, TemplateType templateType) {
            this.templateCode = templateCode;
            this.templateName = templateName;
            this.templateType = templateType;
            this.status = TemplateStatus.DRAFT;
            this.createdTime = LocalDateTime.now();
            this.usageCount = 0;
        }
        
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public TemplateType getTemplateType() { return templateType; }
        public void setTemplateType(TemplateType templateType) { this.templateType = templateType; }
        
        public String getSubjectTemplate() { return subjectTemplate; }
        public void setSubjectTemplate(String subjectTemplate) { this.subjectTemplate = subjectTemplate; }
        
        public String getContentTemplate() { return contentTemplate; }
        public void setContentTemplate(String contentTemplate) { this.contentTemplate = contentTemplate; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public TemplateStatus getStatus() { return status; }
        public void setStatus(TemplateStatus status) { this.status = status; }
        
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        
        public List<TemplateVariable> getVariables() { return variables; }
        public void setVariables(List<TemplateVariable> variables) { this.variables = variables; }
        
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
        
        public String getCreatedByName() { return createdByName; }
        public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
        
        public LocalDateTime getCreatedTime() { return createdTime; }
        public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
        
        public Long getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
        
        public LocalDateTime getUpdatedTime() { return updatedTime; }
        public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }
        
        public Integer getUsageCount() { return usageCount; }
        public void setUsageCount(Integer usageCount) { this.usageCount = usageCount; }
        
        public LocalDateTime getLastUsedTime() { return lastUsedTime; }
        public void setLastUsedTime(LocalDateTime lastUsedTime) { this.lastUsedTime = lastUsedTime; }
        
        // 校验模板是否有效
        public boolean isValid() {
            return templateCode != null && !templateCode.trim().isEmpty()
                    && templateName != null && !templateName.trim().isEmpty()
                    && templateType != null
                    && status != null;
        }
        
        // 检查是否启用
        public boolean isActive() {
            return status == TemplateStatus.ACTIVE;
        }
        
        // 增加使用次数
        public void incrementUsageCount() {
            this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
            this.lastUsedTime = LocalDateTime.now();
        }
    }
    
    /**
     * 创建消息模板
     * 
     * @param template 模板信息
     * @return 创建结果
     */
    TemplateOperationResult createTemplate(MessageTemplate template);
    
    /**
     * 更新消息模板
     * 
     * @param template 模板信息
     * @return 更新结果
     */
    TemplateOperationResult updateTemplate(MessageTemplate template);
    
    /**
     * 根据模板代码获取模板
     * 
     * @param templateCode 模板代码
     * @return 模板信息
     */
    MessageTemplate getTemplateByCode(String templateCode);
    
    /**
     * 根据模板ID获取模板
     * 
     * @param templateId 模板ID
     * @return 模板信息
     */
    MessageTemplate getTemplateById(Long templateId);
    
    /**
     * 删除模板
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 删除结果
     */
    TemplateOperationResult deleteTemplate(Long templateId, Long userId);
    
    /**
     * 禁用模板
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 操作结果
     */
    TemplateOperationResult disableTemplate(Long templateId, Long userId);
    
    /**
     * 启用模板
     * 
     * @param templateId 模板ID
     * @param userId 用户ID
     * @return 操作结果
     */
    TemplateOperationResult enableTemplate(Long templateId, Long userId);
    
    /**
     * 获取模板列表
     * 
     * @param templateType 模板类型筛选
     * @param status 状态筛选
     * @param bizType 业务类型筛选
     * @return 模板列表
     */
    List<MessageTemplate> getTemplateList(TemplateType templateType, TemplateStatus status, String bizType);
    
    /**
     * 渲染模板
     * 
     * @param templateCode 模板代码
     * @param params 参数映射
     * @return 渲染结果
     */
    TemplateRenderResult renderTemplate(String templateCode, Map<String, Object> params);
    
    /**
     * 校验模板变量
     * 
     * @param templateCode 模板代码
     * @param params 参数映射
     * @return 校验结果
     */
    TemplateValidationResult validateParameters(String templateCode, Map<String, Object> params);
    
    /**
     * 导出模板数据
     * 
     * @param templateIds 模板ID列表
     * @return 导出数据
     */
    TemplateExportResult exportTemplates(List<Long> templateIds);
    
    /**
     * 导入模板数据
     * 
     * @param importData 导入数据
     * @param userId 用户ID
     * @return 导入结果
     */
    TemplateImportResult importTemplates(TemplateExportResult importData, Long userId);
    
    /**
     * 获取模板统计信息
     * 
     * @return 模板统计
     */
    TemplateStatistics getTemplateStatistics();
    
    /**
     * 模板操作结果
     */
    class TemplateOperationResult {
        private boolean success;
        private String message;
        private Long templateId;
        private MessageTemplate template;
        
        public TemplateOperationResult() {}
        
        public TemplateOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        
        public MessageTemplate getTemplate() { return template; }
        public void setTemplate(MessageTemplate template) { this.template = template; }
    }
    
    /**
     * 模板渲染结果
     */
    class TemplateRenderResult {
        private boolean success;
        private String message;
        private String renderedSubject;
        private String renderedContent;
        private List<String> missingVariables;
        private List<String> invalidVariables;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getRenderedSubject() { return renderedSubject; }
        public void setRenderedSubject(String renderedSubject) { this.renderedSubject = renderedSubject; }
        
        public String getRenderedContent() { return renderedContent; }
        public void setRenderedContent(String renderedContent) { this.renderedContent = renderedContent; }
        
        public List<String> getMissingVariables() { return missingVariables; }
        public void setMissingVariables(List<String> missingVariables) { this.missingVariables = missingVariables; }
        
        public List<String> getInvalidVariables() { return invalidVariables; }
        public void setInvalidVariables(List<String> invalidVariables) { this.invalidVariables = invalidVariables; }
    }
    
    /**
     * 模板校验结果
     */
    class TemplateValidationResult {
        private boolean valid;
        private String message;
        private List<String> missingVariables;
        private List<String> invalidVariables;
        private List<String> typeMismatchVariables;
        
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<String> getMissingVariables() { return missingVariables; }
        public void setMissingVariables(List<String> missingVariables) { this.missingVariables = missingVariables; }
        
        public List<String> getInvalidVariables() { return invalidVariables; }
        public void setInvalidVariables(List<String> invalidVariables) { this.invalidVariables = invalidVariables; }
        
        public List<String> getTypeMismatchVariables() { return typeMismatchVariables; }
        public void setTypeMismatchVariables(List<String> typeMismatchVariables) { this.typeMismatchVariables = typeMismatchVariables; }
    }
    
    /**
     * 模板导出结果
     */
    class TemplateExportResult {
        private String exportVersion;
        private LocalDateTime exportTime;
        private Long exportedBy;
        private List<MessageTemplate> templates;
        
        public String getExportVersion() { return exportVersion; }
        public void setExportVersion(String exportVersion) { this.exportVersion = exportVersion; }
        
        public LocalDateTime getExportTime() { return exportTime; }
        public void setExportTime(LocalDateTime exportTime) { this.exportTime = exportTime; }
        
        public Long getExportedBy() { return exportedBy; }
        public void setExportedBy(Long exportedBy) { this.exportedBy = exportedBy; }
        
        public List<MessageTemplate> getTemplates() { return templates; }
        public void setTemplates(List<MessageTemplate> templates) { this.templates = templates; }
    }
    
    /**
     * 模板导入结果
     */
    class TemplateImportResult {
        private boolean success;
        private String message;
        private int importedCount;
        private int skippedCount;
        private int errorCount;
        private List<String> importErrors;
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getImportedCount() { return importedCount; }
        public void setImportedCount(int importedCount) { this.importedCount = importedCount; }
        
        public int getSkippedCount() { return skippedCount; }
        public void setSkippedCount(int skippedCount) { this.skippedCount = skippedCount; }
        
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        
        public List<String> getImportErrors() { return importErrors; }
        public void setImportErrors(List<String> importErrors) { this.importErrors = importErrors; }
    }
    
    /**
     * 模板统计信息
     */
    class TemplateStatistics {
        private int totalTemplates;
        private int activeTemplates;
        private int emailTemplates;
        private int smsTemplates;
        private int innerMessageTemplates;
        private double averageUsageRate;
        private int mostUsedTemplateId;
        private String mostUsedTemplateName;
        
        public int getTotalTemplates() { return totalTemplates; }
        public void setTotalTemplates(int totalTemplates) { this.totalTemplates = totalTemplates; }
        
        public int getActiveTemplates() { return activeTemplates; }
        public void setActiveTemplates(int activeTemplates) { this.activeTemplates = activeTemplates; }
        
        public int getEmailTemplates() { return emailTemplates; }
        public void setEmailTemplates(int emailTemplates) { this.emailTemplates = emailTemplates; }
        
        public int getSmsTemplates() { return smsTemplates; }
        public void setSmsTemplates(int smsTemplates) { this.smsTemplates = smsTemplates; }
        
        public int getInnerMessageTemplates() { return innerMessageTemplates; }
        public void setInnerMessageTemplates(int innerMessageTemplates) { this.innerMessageTemplates = innerMessageTemplates; }
        
        public double getAverageUsageRate() { return averageUsageRate; }
        public void setAverageUsageRate(double averageUsageRate) { this.averageUsageRate = averageUsageRate; }
        
        public int getMostUsedTemplateId() { return mostUsedTemplateId; }
        public void setMostUsedTemplateId(int mostUsedTemplateId) { this.mostUsedTemplateId = mostUsedTemplateId; }
        
        public String getMostUsedTemplateName() { return mostUsedTemplateName; }
        public void setMostUsedTemplateName(String mostUsedTemplateName) { this.mostUsedTemplateName = mostUsedTemplateName; }
    }
}