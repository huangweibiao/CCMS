package com.ccms.vo;

import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.PriorityTypeEnum;
import java.util.Objects;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流程配置视图对象
 */
public class ApprovalFlowConfigVO {
    public ApprovalFlowConfigVO() {
        // 默认构造函数
    }
    
    public ApprovalFlowConfigVO(Long id, String flowCode, String flowName, BusinessTypeEnum businessType, 
                               String businessTypeName, String description, PriorityTypeEnum priorityType, 
                               String priorityTypeName, Boolean enabled, Integer versionNumber, 
                               Boolean latestVersion, Long createdBy, String createdByName, 
                               LocalDateTime createTime, Long updatedBy, LocalDateTime updateTime, 
                               LocalDateTime effectiveTime, LocalDateTime expireTime, Double amountLimit, 
                               Double amountFloor, List<ApprovalNode> nodes, Integer nodeCount, 
                               Long usageCount, Double averageDuration, Double passRate, String remarks) {
        this.id = id;
        this.flowCode = flowCode;
        this.flowName = flowName;
        this.businessType = businessType;
        this.businessTypeName = businessTypeName;
        this.description = description;
        this.priorityType = priorityType;
        this.priorityTypeName = priorityTypeName;
        this.enabled = enabled;
        this.versionNumber = versionNumber;
        this.latestVersion = latestVersion;
        this.createdBy = createdBy;
        this.createdByName = createdByName;
        this.createTime = createTime;
        this.updatedBy = updatedBy;
        this.updateTime = updateTime;
        this.effectiveTime = effectiveTime;
        this.expireTime = expireTime;
        this.amountLimit = amountLimit;
        this.amountFloor = amountFloor;
        this.nodes = nodes;
        this.nodeCount = nodeCount;
        this.usageCount = usageCount;
        this.averageDuration = averageDuration;
        this.passRate = passRate;
        this.remarks = remarks;
    }
    
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 流程代码（唯一标识）
     */
    private String flowCode;
    
    /**
     * 流程名称
     */
    private String flowName;
    
    /**
     * 业务类型
     */
    private BusinessTypeEnum businessType;
    
    /**
     * 业务类型名称
     */
    private String businessTypeName;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 优先级类型
     */
    private PriorityTypeEnum priorityType;
    
    /**
     * 优先级类型名称
     */
    private String priorityTypeName;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 版本号
     */
    private Integer versionNumber;
    
    /**
     * 是否为最新版本
     */
    private Boolean latestVersion;
    
    /**
     * 创建人ID
     */
    private Long createdBy;
    
    /**
     * 创建人姓名
     */
    private String createdByName;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 修改人ID
     */
    private Long updatedBy;
    
    /**
     * 修改时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 生效时间
     */
    private LocalDateTime effectiveTime;
    
    /**
     * 失效时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 金额上限
     */
    private Double amountLimit;
    
    /**
     * 金额下限
     */
    private Double amountFloor;
    
    /**
     * 审批节点列表
     */
    private List<ApprovalNode> nodes;
    
    /**
     * 节点数量
     */
    private Integer nodeCount;
    
    /**
     * 使用次数统计
     */
    private Long usageCount;
    
    /**
     * 平均审批时长（小时）
     */
    private Double averageDuration;
    
    /**
     * 通过率（百分比）
     */
    private Double passRate;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 是否有效
     */
    public boolean isValid() {
        if (!Boolean.TRUE.equals(enabled)) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (effectiveTime != null && now.isBefore(effectiveTime)) {
            return false;
        }
        
        if (expireTime != null && now.isAfter(expireTime)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取状态描述
     */
    public String getStatusDescription() {
        if (!Boolean.TRUE.equals(enabled)) {
            return "已禁用";
        }
        
        if (expireTime != null && LocalDateTime.now().isAfter(expireTime)) {
            return "已过期";
        }
        
        if (effectiveTime != null && LocalDateTime.now().isBefore(effectiveTime)) {
            return "待生效";
        }
        
        return "运行中";
    }
    
    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlowCode() { return flowCode; }
    public void setFlowCode(String flowCode) { this.flowCode = flowCode; }
    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }
    public BusinessTypeEnum getBusinessType() { return businessType; }
    public void setBusinessType(BusinessTypeEnum businessType) { this.businessType = businessType; }
    public String getBusinessTypeName() { return businessTypeName; }
    public void setBusinessTypeName(String businessTypeName) { this.businessTypeName = businessTypeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public PriorityTypeEnum getPriorityType() { return priorityType; }
    public void setPriorityType(PriorityTypeEnum priorityType) { this.priorityType = priorityType; }
    public String getPriorityTypeName() { return priorityTypeName; }
    public void setPriorityTypeName(String priorityTypeName) { this.priorityTypeName = priorityTypeName; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Integer getVersionNumber() { return versionNumber; }
    public void setVersionNumber(Integer versionNumber) { this.versionNumber = versionNumber; }
    public Boolean getLatestVersion() { return latestVersion; }
    public void setLatestVersion(Boolean latestVersion) { this.latestVersion = latestVersion; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public LocalDateTime getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(LocalDateTime effectiveTime) { this.effectiveTime = effectiveTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public Double getAmountLimit() { return amountLimit; }
    public void setAmountLimit(Double amountLimit) { this.amountLimit = amountLimit; }
    public Double getAmountFloor() { return amountFloor; }
    public void setAmountFloor(Double amountFloor) { this.amountFloor = amountFloor; }
    public List<ApprovalNode> getNodes() { return nodes; }
    public void setNodes(List<ApprovalNode> nodes) { this.nodes = nodes; }
    public Integer getNodeCount() { return nodeCount; }
    public void setNodeCount(Integer nodeCount) { this.nodeCount = nodeCount; }
    public Long getUsageCount() { return usageCount; }
    public void setUsageCount(Long usageCount) { this.usageCount = usageCount; }
    public Double getAverageDuration() { return averageDuration; }
    public void setAverageDuration(Double averageDuration) { this.averageDuration = averageDuration; }
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApprovalFlowConfigVO)) return false;
        ApprovalFlowConfigVO that = (ApprovalFlowConfigVO) o;
        return Objects.equals(id, that.id) &&
               Objects.equals(flowCode, that.flowCode) &&
               Objects.equals(flowName, that.flowName) &&
               businessType == that.businessType &&
               Objects.equals(businessTypeName, that.businessTypeName) &&
               Objects.equals(description, that.description) &&
               priorityType == that.priorityType &&
               Objects.equals(priorityTypeName, that.priorityTypeName) &&
               Objects.equals(enabled, that.enabled) &&
               Objects.equals(versionNumber, that.versionNumber) &&
               Objects.equals(latestVersion, that.latestVersion) &&
               Objects.equals(createdBy, that.createdBy) &&
               Objects.equals(createdByName, that.createdByName) &&
               Objects.equals(createTime, that.createTime) &&
               Objects.equals(updatedBy, that.updatedBy) &&
               Objects.equals(updateTime, that.updateTime) &&
               Objects.equals(effectiveTime, that.effectiveTime) &&
               Objects.equals(expireTime, that.expireTime) &&
               Objects.equals(amountLimit, that.amountLimit) &&
               Objects.equals(amountFloor, that.amountFloor) &&
               Objects.equals(nodes, that.nodes) &&
               Objects.equals(nodeCount, that.nodeCount) &&
               Objects.equals(usageCount, that.usageCount) &&
               Objects.equals(averageDuration, that.averageDuration) &&
               Objects.equals(passRate, that.passRate) &&
               Objects.equals(remarks, that.remarks);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, flowCode, flowName, businessType, businessTypeName, description, 
                          priorityType, priorityTypeName, enabled, versionNumber, latestVersion, 
                          createdBy, createdByName, createTime, updatedBy, updateTime, 
                          effectiveTime, expireTime, amountLimit, amountFloor, nodes, 
                          nodeCount, usageCount, averageDuration, passRate, remarks);
    }
    
    @Override
    public String toString() {
        return "ApprovalFlowConfigVO{" +
               "id=" + id +
               ", flowCode='" + flowCode + '\'' +
               ", flowName='" + flowName + '\'' +
               ", businessType=" + businessType +
               ", businessTypeName='" + businessTypeName + '\'' +
               ", description='" + description + '\'' +
               ", priorityType=" + priorityType +
               ", priorityTypeName='" + priorityTypeName + '\'' +
               ", enabled=" + enabled +
               ", versionNumber=" + versionNumber +
               ", latestVersion=" + latestVersion +
               ", createdBy=" + createdBy +
               ", createdByName='" + createdByName + '\'' +
               ", createTime=" + createTime +
               ", updatedBy=" + updatedBy +
               ", updateTime=" + updateTime +
               ", effectiveTime=" + effectiveTime +
               ", expireTime=" + expireTime +
               ", amountLimit=" + amountLimit +
               ", amountFloor=" + amountFloor +
               ", nodes=" + nodes +
               ", nodeCount=" + nodeCount +
               ", usageCount=" + usageCount +
               ", averageDuration=" + averageDuration +
               ", passRate=" + passRate +
               ", remarks='" + remarks + '\'' +
               '}';
    }
}