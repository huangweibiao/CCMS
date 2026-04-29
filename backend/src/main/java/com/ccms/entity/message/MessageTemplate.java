package com.ccms.entity.message;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;

@Entity
@Table(name = "sys_message_template")
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
}