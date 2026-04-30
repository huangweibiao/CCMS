package com.ccms.service;

import com.ccms.entity.message.MessageTemplate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
     * 模板导入/导出查询请求
     */
    class TemplateQuery {
        private String templateCode;
        private String templateName;
        private String templateType;
        private Integer status;
        private String creatorId;
        private String channel;
        private Boolean isActive;
        private LocalDateTime createdTimeBegin;
        private LocalDateTime createdTimeEnd;
        
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
        
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        
        public String getCreatorId() { return creatorId; }
        public void setCreatorId(String creatorId) { this.creatorId = creatorId; }
        
        public String getChannel() { return channel; }
        public void setChannel(String channel) { this.channel = channel; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public LocalDateTime getCreatedTimeBegin() { return createdTimeBegin; }
        public void setCreatedTimeBegin(LocalDateTime createdTimeBegin) { this.createdTimeBegin = createdTimeBegin; }
        
        public LocalDateTime getCreatedTimeEnd() { return createdTimeEnd; }
        public void setCreatedTimeEnd(LocalDateTime createdTimeEnd) { this.createdTimeEnd = createdTimeEnd; }
    }
    
    /**
     * 模板创建/更新请求
     */
    class TemplateRequest {
        private String templateCode;
        private String templateName;
        private String templateType;
        private String content;
        private String description;
        private List<TemplateVariable> variables;
        private String channels;
        private Boolean isActive = true;
        private Long createdBy;
        private Long updatedBy;
        
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<TemplateVariable> getVariables() { return variables; }
        public void setVariables(List<TemplateVariable> variables) { this.variables = variables; }
        
        public String getChannels() { return channels; }
        public void setChannels(String channels) { this.channels = channels; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public Long getCreatedBy() { return createdBy; }
        public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
        
        public Long getUpdatedBy() { return updatedBy; }
        public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    }
    
    /**
     * 批量导入结果
     */
    class BatchImportResult {
        private int totalCount;
        private int successCount;
        private int failCount;
        private List<String> errorMessages;
        
        public BatchImportResult() {}
        
        public BatchImportResult(int totalCount, int successCount, int failCount) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failCount = failCount;
        }
        
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailCount() { return failCount; }
        public void setFailCount(int failCount) { this.failCount = failCount; }
        
        public List<String> getErrorMessages() { return errorMessages; }
        public void setErrorMessages(List<String> errorMessages) { this.errorMessages = errorMessages; }
    }
    
    /**
     * 模板导出项
     */
    class TemplateExportItem {
        private String templateCode;
        private String templateName;
        private String templateType;
        private String content;
        private String description;
        private List<TemplateVariable> variables;
        
        public String getTemplateCode() { return templateCode; }
        public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public String getTemplateType() { return templateType; }
        public void setTemplateType(String templateType) { this.templateType = templateType; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<TemplateVariable> getVariables() { return variables; }
        public void setVariables(List<TemplateVariable> variables) { this.variables = variables; }
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
        private String renderedContent;
        private Map<String, Object> usedParams;
        private List<String> warnings;
        
        public TemplateRenderResult() {}
        
        public TemplateRenderResult(String renderedContent, Map<String, Object> usedParams) {
            this.renderedContent = renderedContent;
            this.usedParams = usedParams;
        }
        
        public String getRenderedContent() { return renderedContent; }
        public void setRenderedContent(String renderedContent) { this.renderedContent = renderedContent; }
        
        public Map<String, Object> getUsedParams() { return usedParams; }
        public void setUsedParams(Map<String, Object> usedParams) { this.usedParams = usedParams; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }
    
    /**
     * 模板统计数据
     */
    class TemplateStatistics {
        private Long templateId;
        private String templateName;
        private Integer totalSendCount;
        private Integer successCount;
        private Double successRate;
        private LocalDateTime lastSendTime;
        
        public TemplateStatistics() {}
        
        public Long getTemplateId() { return templateId; }
        public void setTemplateId(Long templateId) { this.templateId = templateId; }
        
        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }
        
        public Integer getTotalSendCount() { return totalSendCount; }
        public void setTotalSendCount(Integer totalSendCount) { this.totalSendCount = totalSendCount; }
        
        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
        
        public Double getSuccessRate() { return successRate; }
        public void setSuccessRate(Double successRate) { this.successRate = successRate; }
        
        public LocalDateTime getLastSendTime() { return lastSendTime; }
        public void setLastSendTime(LocalDateTime lastSendTime) { this.lastSendTime = lastSendTime; }
    }
    
    // 基本模板管理方法
    Optional<MessageTemplate> getTemplateByCode(String templateCode);
    
    TemplateOperationResult createTemplate(TemplateRequest request);
    
    TemplateOperationResult updateTemplate(Long templateId, TemplateRequest request);
    
    boolean deleteTemplate(String templateCode);
    
    List<MessageTemplate> getAllTemplates();
}