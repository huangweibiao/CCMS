package com.ccms.dto;

import lombok.Data;

@Data
public class TemplateQuery {
    private String templateCode;
    private String templateName;
    private String templateType;
    private Integer status;
    private String creatorId;
}