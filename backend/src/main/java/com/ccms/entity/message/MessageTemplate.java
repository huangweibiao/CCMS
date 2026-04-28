package com.ccms.entity.message;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
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
}