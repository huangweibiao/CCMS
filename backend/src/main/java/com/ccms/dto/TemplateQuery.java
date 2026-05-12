package com.ccms.dto;

import java.util.Objects;

public class TemplateQuery {
    private String templateCode;
    private String templateName;
    private String templateType;
    private Integer status;
    private String creatorId;

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateQuery that = (TemplateQuery) o;
        return Objects.equals(templateCode, that.templateCode) && 
               Objects.equals(templateName, that.templateName) && 
               Objects.equals(templateType, that.templateType) && 
               Objects.equals(status, that.status) && 
               Objects.equals(creatorId, that.creatorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateCode, templateName, templateType, status, creatorId);
    }

    @Override
    public String toString() {
        return "TemplateQuery{" +
                "templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateType='" + templateType + '\'' +
                ", status=" + status +
                ", creatorId='" + creatorId + '\'' +
                '}';
    }
}