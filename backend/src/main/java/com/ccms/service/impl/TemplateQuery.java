package com.ccms.service.impl;

import lombok.Data;

@Data
public class TemplateQuery {
    private String templateCode;
    private String templateName;
    private String contentType;
    private Boolean enabled;
}