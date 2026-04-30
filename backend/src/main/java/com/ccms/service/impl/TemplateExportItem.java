package com.ccms.service.impl;

import lombok.Data;

@Data
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
}