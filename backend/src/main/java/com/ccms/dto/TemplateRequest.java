package com.ccms.dto;

import com.ccms.service.MessageTemplateService;
import lombok.Data;
import java.util.List;

@Data
public class TemplateRequest {
    private String templateCode;
    private String templateName;
    private String templateType;
    private String content;
    private String description;
    private List<MessageTemplateService.TemplateVariable> variables;
}