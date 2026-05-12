package com.ccms.dto;

import com.ccms.service.MessageTemplateService;
import java.util.List;
import java.util.Objects;

public class TemplateRequest {
    private String templateCode;
    private String templateName;
    private String templateType;
    private String content;
    private String description;
    private List<MessageTemplateService.TemplateVariable> variables;

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

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MessageTemplateService.TemplateVariable> getVariables() {
        return variables;
    }

    public void setVariables(List<MessageTemplateService.TemplateVariable> variables) {
        this.variables = variables;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateRequest that = (TemplateRequest) o;
        return Objects.equals(templateCode, that.templateCode) && 
               Objects.equals(templateName, that.templateName) && 
               Objects.equals(templateType, that.templateType) && 
               Objects.equals(content, that.content) && 
               Objects.equals(description, that.description) && 
               Objects.equals(variables, that.variables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateCode, templateName, templateType, content, description, variables);
    }

    @Override
    public String toString() {
        return "TemplateRequest{" +
                "templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateType='" + templateType + '\'' +
                ", content='" + content + '\'' +
                ", description='" + description + '\'' +
                ", variables=" + variables +
                '}';
    }
}