package com.ccms.entity.message;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;

@Entity
@Table(name = "ccms_sys_message_template")
public class MessageTemplate extends BaseEntity {
    
    @Column(name = "template_code", nullable = false, unique = true, length = 100)
    private String templateCode;
    
    @Column(name = "template_name", nullable = false, length = 200)
    private String templateName;
    
    @Column(name = "template_content", nullable = false, columnDefinition = "text")
    private String templateContent;
    
    @Column(name = "template_type", length = 50)
    private String templateType;
    
    @Column(name = "business_type", length = 50)
    private String businessType;
    
    @Column(name = "subject_template", length = 200)
    private String subjectTemplate;
    
    @Column(name = "is_active", columnDefinition = "tinyint(1) default 1")
    private Boolean active = true;
    
    @Column(name = "channels", length = 500)
    private String channels;
    
    @Column(name = "variables", columnDefinition = "text")
    private String variables;
    
    @Column(name = "send_count", columnDefinition = "int default 0")
    private Integer sendCount = 0;
    
    @Column(name = "success_count", columnDefinition = "int default 0")
    private Integer successCount = 0;
    
    @Column(name = "remark", length = 500)
    private String remark;

    // Manual getters and setters
    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // Added missing getters and setters
    public String getChannels() {
        return channels;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public Integer getSendCount() {
        return sendCount;
    }

    public void setSendCount(Integer sendCount) {
        this.sendCount = sendCount;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    // Alias for templateContent to match service expectations
    public String getContent() {
        return templateContent;
    }

    public void setContent(String content) {
        this.templateContent = content;
    }

    // Alias for active to match service expectations  
    public Boolean getIsActive() {
        return active;
    }

    public void setIsActive(Boolean isActive) {
        this.active = isActive;
    }

    // Alias for channels to match service expectations
    public String getChannel() {
        return channels;
    }
    
    public void setChannel(String channel) {
        this.channels = channel;
    }
    
    public void setDescription(String description) {
        // 将description映射到remark字段
        this.remark = description;
    }
    
    public void setCreatedTime(java.time.LocalDateTime createdTime) {
        // 调用基类的setCreateTime方法
        setCreateTime(createdTime);
    }
    
    public void setCreatedBy(Long createdBy) {
        // 设置创建人ID
        setCreateBy(createdBy);
    }
    
    public void setUpdatedBy(Long updatedBy) {
        // 设置更新人ID
        setUpdateBy(updatedBy);
    }
    
    public void setUpdatedTime(java.time.LocalDateTime updatedTime) {
        // 调用基类的setUpdateTime方法
        setUpdateTime(updatedTime);
    }
}