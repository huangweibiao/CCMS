package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "expense_reimburse")
public class ExpenseReimburse extends BaseEntity {
    
    @Column(name = "reimburse_no", nullable = false, unique = true, length = 50)
    private String reimburseNo;
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "apply_user_id", nullable = false)
    private Long applyUserId;
    
    @Column(name = "apply_user_name", nullable = false, length = 100)
    private String applyUserName;
    
    @Column(name = "dept_id")
    private Long deptId;
    
    @Column(name = "dept_name", length = 100)
    private String deptName;
    
    @Column(name = "reimburse_date", nullable = false)
    private LocalDate reimburseDate;
    
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "currency", length = 10)
    private String currency = "CNY";
    
    @Column(name = "status", nullable = false)
    private Integer status; // 0-草稿 1-已提交 2-审批中 3-已批准 4-已拒绝 5-已撤回
    
    @Column(name = "approval_status", nullable = false)
    private Integer approvalStatus; // 0-待提交 1-审批中 2-已通过 3-已拒绝
    
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
    
    @Column(name = "bank_account", length = 100)
    private String bankAccount;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
}