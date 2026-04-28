package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "approval")
public class Approval extends BaseEntity {
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "business_type", nullable = false, length = 50)
    private String businessType;
    
    @Column(name = "business_id", nullable = false)
    private Long businessId;
    
    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;
    
    @Column(name = "applicant_name", nullable = false, length = 100)
    private String applicantName;
    
    @Column(name = "dept_id")
    private Long deptId;
    
    @Column(name = "dept_name", length = 100)
    private String deptName;
    
    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;
    
    @Column(name = "status", nullable = false)
    private Integer status; // 0-草稿 1-已提交 2-审批中 3-已批准 4-已拒绝 5-已撤回
    
    @Column(name = "current_step")
    private Integer currentStep;
    
    @Column(name = "current_approver_id")
    private Long currentApproverId;
    
    @Column(name = "current_approver_name", length = 100)
    private String currentApproverName;
    
    @Column(name = "submit_time")
    private LocalDateTime submitTime;
    
    @Column(name = "approve_time")
    private LocalDateTime approveTime;
    
    @Column(name = "reject_time")
    private LocalDateTime rejectTime;
    
    @Column(name = "reject_reason", length = 500)
    private String rejectReason;
    
    @Column(name = "remark", length = 1000)
    private String remark;
}