package com.ccms.service.impl;

import java.util.Objects;

public class TemplateQuery {
    private String templateCode;
    private String templateName;
    private String contentType;
    private Boolean enabled;

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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateQuery that = (TemplateQuery) o;
        return Objects.equals(templateCode, that.templateCode) && 
               Objects.equals(templateName, that.templateName) && 
               Objects.equals(contentType, that.contentType) && 
               Objects.equals(enabled, that.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateCode, templateName, contentType, enabled);
    }

    @Override
    public String toString() {
        return "TemplateQuery{" +
                "templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", enabled=" + enabled +
                '}';
    }
}