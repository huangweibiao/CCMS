package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_apply")
public class ExpenseApply extends BaseEntity {
    
    @Column(name = "apply_no", nullable = false, unique = true, length = 50)
    private String applyNo;
    
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
    
    @Column(name = "apply_date", nullable = false)
    private LocalDate applyDate;
    
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
    
    @Column(name = "budget_available_amount", precision = 15, scale = 2)
    private BigDecimal budgetAvailableAmount;
    
    @Column(name = "budget_checked", columnDefinition = "tinyint(1) default 0")
    private Boolean budgetChecked = false;

    // Getters and Setters
    public String getApplyNo() {
        return applyNo;
    }

    public void setApplyNo(String applyNo) {
        this.applyNo = applyNo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getApplyUserName() {
        return applyUserName;
    }

    public void setApplyUserName(String applyUserName) {
        this.applyUserName = applyUserName;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public LocalDate getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(LocalDate applyDate) {
        this.applyDate = applyDate;
    }

    public LocalDateTime getApplyTime() {
        return this.getCreateTime(); // 使用基类的createTime作为applyTime
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public Long getCurrentApproverId() {
        return currentApproverId;
    }

    public void setCurrentApproverId(Long currentApproverId) {
        this.currentApproverId = currentApproverId;
    }

    public String getCurrentApproverName() {
        return currentApproverName;
    }

    public void setCurrentApproverName(String currentApproverName) {
        this.currentApproverName = currentApproverName;
    }

    public LocalDateTime getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime = submitTime;
    }

    public LocalDateTime getApproveTime() {
        return approveTime;
    }

    public void setApproveTime(LocalDateTime approveTime) {
        this.approveTime = approveTime;
    }

    public LocalDateTime getRejectTime() {
        return rejectTime;
    }

    public void setRejectTime(LocalDateTime rejectTime) {
        this.rejectTime = rejectTime;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public BigDecimal getBudgetAvailableAmount() {
        return budgetAvailableAmount;
    }

    public void setBudgetAvailableAmount(BigDecimal budgetAvailableAmount) {
        this.budgetAvailableAmount = budgetAvailableAmount;
    }

    public Boolean getBudgetChecked() {
        return budgetChecked;
    }

    public void setBudgetChecked(Boolean budgetChecked) {
        this.budgetChecked = budgetChecked;
    }

    // 为缺失的方法添加实现
    public String getApplyCode() {
        return this.applyNo; // applyCode对应applyNo
    }

    public void setApplyCode(String applyCode) {
        this.applyNo = applyCode;
    }

    public void setApplyTime(java.time.LocalDateTime applyTime) {
        // ApplyTime对应基类的createTime
        this.setCreateTime(applyTime);
    }

    // 添加缺失的字段和方法
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "expected_date")
    private java.time.LocalDate expectedDate;
    
    @Column(name = "approver_id")
    private Long approverId;
    
    @Column(name = "reimburse_id")
    private Long reimburseId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.time.LocalDate getExpectedDate() {
        return expectedDate;
    }

    public void setExpectedDate(java.time.LocalDate expectedDate) {
        this.expectedDate = expectedDate;
    }

    public String getApplyTitle() {
        return this.title; // applyTitle对应title
    }

    public void setApplyTitle(String applyTitle) {
        this.title = applyTitle;
    }

    // 添加缺失的approverId和reimburseId字段的getter/setter
    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public Long getReimburseId() {
        return reimburseId;
    }

    public void setReimburseId(Long reimburseId) {
        this.reimburseId = reimburseId;
    }
}