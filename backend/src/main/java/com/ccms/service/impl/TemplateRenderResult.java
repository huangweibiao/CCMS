package com.ccms.service.impl;

import lombok.Data;

@Data
public class TemplateRenderResult {
    private String content;
    private boolean success;
    private String error;
    
    public TemplateRenderResult() {
        this.content = "";
        this.success = true;
        this.error = "";
    }
}