package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "approval_process")
public class ApprovalProcess extends BaseEntity {
    
    @Column(name = "process_code", nullable = false, unique = true, length = 50)
    private String processCode;
    
    @Column(name = "process_name", nullable = false, length = 200)
    private String processName;
    
    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "is_active", columnDefinition = "tinyint(1) default 1")
    private Boolean active = true;
    
    @Column(name = "version", nullable = false)
    private Integer version = 1;
}