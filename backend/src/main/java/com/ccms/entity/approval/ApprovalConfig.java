package com.ccms.entity.approval;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 审批流程配置实体
 */
@Entity
@Table(name = "approval_config")
public class ApprovalConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplyType applyType;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column
    private Long departmentId;
    
    @Column(nullable = false)
    private BigDecimal minAmount;
    
    @Column(nullable = false)
    private BigDecimal maxAmount;
    
    @Column(nullable = false)
    private Integer approvalLevel;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "approval_node_config", joinColumns = @JoinColumn(name = "config_id"))
    private List<ApprovalNode> nodes = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();
    
    @Column
    private LocalDateTime updateTime;
    
    @Version
    private Long versionNumber;
    
    @Embeddable
    public static class ApprovalNode {
        @Column(nullable = false)
        private Integer nodeOrder;
        
        @Column(nullable = false, length = 100)
        private String nodeName;
        
        @Column(nullable = false)
        @Enumerated(EnumType.STRING)
        private ApproverType approverType;
        
        @Column
        private Long approverId;
        
        @Column(length = 100)
        private String approverRole;
        
        @Column
        private Long departmentId;
        
        @Column(nullable = false)
        private Boolean required = true;
        
        @Column
        private Integer timeoutHours;
        
        // Constructors
        public ApprovalNode() {}
        
        public ApprovalNode(Integer nodeOrder, String nodeName, ApproverType approverType) {
            this.nodeOrder = nodeOrder;
            this.nodeName = nodeName;
            this.approverType = approverType;
        }
        
        // Getters and Setters
        public Integer getNodeOrder() { return nodeOrder; }
        public void setNodeOrder(Integer nodeOrder) { this.nodeOrder = nodeOrder; }
        
        public String getNodeName() { return nodeName; }
        public void setNodeName(String nodeName) { this.nodeName = nodeName; }
        
        public ApproverType getApproverType() { return approverType; }
        public void setApproverType(ApproverType approverType) { this.approverType = approverType; }
        
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        
        public String getApproverRole() { return approverRole; }
        public void setApproverRole(String approverRole) { this.approverRole = approverRole; }
        
        public Long getDepartmentId() { return departmentId; }
        public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
        
        public Boolean getRequired() { return required; }
        public void setRequired(Boolean required) { this.required = required; }
        
        public Integer getTimeoutHours() { return timeoutHours; }
        public void setTimeoutHours(Integer timeoutHours) { this.timeoutHours = timeoutHours; }
    }
    
    public enum ApplyType {
        EXPENSE_APPLY,  // 费用报销
        LOAN_APPLY,     // 借款申请
        INVOICE_VERIFY  // 发票验真
    }
    
    public enum ApproverType {
        SPECIFIED_USER,     // 指定用户
        DEPT_MANAGER,       // 部门经理
        SUPERIOR,          // 直属上级
        ROLE_BASED,        // 角色匹配
        ANY_MANAGER        // 任意经理
    }
    
    // Constructors
    public ApprovalConfig() {}
    
    public ApprovalConfig(String name, ApplyType applyType) {
        this.name = name;
        this.applyType = applyType;
    }
    
    // Business methods
    public void addNode(ApprovalNode node) {
        if (nodes == null) {
            nodes = new ArrayList<>();
        }
        nodes.add(node);
    }
    
    public boolean isAmountInRange(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(minAmount) >= 0 && 
               amount.compareTo(maxAmount) <= 0;
    }
    
    public boolean matchesCondition(ApplyType type, Long deptId, BigDecimal amount) {
        if (!applyType.equals(type)) {
            return false;
        }
        if (departmentId != null && !departmentId.equals(deptId)) {
            return false;
        }
        return isAmountInRange(amount);
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ApplyType getApplyType() { return applyType; }
    public void setApplyType(ApplyType applyType) { this.applyType = applyType; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    
    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }
    
    public BigDecimal getMaxAmount() { return maxAmount; }
    public void setMaxAmount(BigDecimal maxAmount) { this.maxAmount = maxAmount; }
    
    public Integer getApprovalLevel() { return approvalLevel; }
    public void setApprovalLevel(Integer approvalLevel) { this.approvalLevel = approvalLevel; }
    
    public List<ApprovalNode> getNodes() { return nodes; }
    public void setNodes(List<ApprovalNode> nodes) { this.nodes = nodes; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    public Long getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Long versionNumber) { this.versionNumber = versionNumber; }
}