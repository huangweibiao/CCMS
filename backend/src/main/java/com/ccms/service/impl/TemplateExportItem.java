package com.ccms.service.impl;

import java.util.Objects;

public class TemplateExportItem {
    private String templateCode;
    private String templateName;
    private String content;
    private String contentType;
    
    public TemplateExportItem() {
        this.templateCode = "";
        this.templateName = "";
        this.content = "";
        this.contentType = "";
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateExportItem that = (TemplateExportItem) o;
        return Objects.equals(templateCode, that.templateCode) && 
               Objects.equals(templateName, that.templateName) && 
               Objects.equals(content, that.content) && 
               Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateCode, templateName, content, contentType);
    }

    @Override
    public String toString() {
        return "TemplateExportItem{" +
                "templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", content='" + content + '\'' +
                ", contentType='" + contentType + '\'' +
                '}';
    }
}