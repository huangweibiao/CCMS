package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import com.ccms.enums.ApprovalStatusEnum;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 审批实例表实体类
 * 对应表名：ccms_approval_instance
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_approval_instance")
public class ApprovalInstance extends BaseEntity {

    /**
     * 审批实例号
     */
    @Column(name = "instance_no", length = 64, nullable = false)
    private String instanceNo;
    
    /**
     * 审批流配置ID
     */
    @Column(name = "flow_id", nullable = false)
    private Long flowId;
    
    /**
     * 业务单据ID
     */
    @Column(name = "business_id", nullable = false)
    private Long businessId;
    
    /**
     * 业务类型
     */
    @Column(name = "business_type", length = 32, nullable = false)
    private String businessType;
    
    /**
     * 审批状态（使用枚举，存储为整数）
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 总节点数
     */
    @Column(name = "total_nodes")
    private Integer totalNodes;
    
    /**
     * 已处理节点数
     */
    @Column(name = "processed_nodes")
    private Integer processedNodes = 0;
    
    /**
     * 审批标题/摘要
     */
    @Column(name = "approval_title", length = 200)
    private String approvalTitle;
    
    /**
     * 紧急程度：1-普通 2-紧急 3-特急
     */
    @Column(name = "urgency_level")
    private Integer urgencyLevel = 1;
    
    /**
     * 当前节点
     */
    @Column(name = "current_node", length = 64)
    private String currentNode;
    
    /**
     * 创建人ID
     */
    @Column(name = "create_by", nullable = false)
    private String createBy;
    
    /**
     * 完成时间
     */
    @Column(name = "finish_time")
    private LocalDateTime finishTime;

    // Getters and Setters
    public String getInstanceNo() {
        return instanceNo;
    }

    public void setInstanceNo(String instanceNo) {
        this.instanceNo = instanceNo;
    }

    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(String currentNode) {
        this.currentNode = currentNode;
    }

    public String getApprovalCreateBy() {
        return createBy;
    }

    public void setApprovalCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    // 枚举状态相关方法
    public ApprovalStatusEnum getApprovalStatus() {
        return ApprovalStatusEnum.getByCode(status);
    }

    public void setApprovalStatus(ApprovalStatusEnum statusEnum) {
        this.status = statusEnum != null ? statusEnum.getCode() : null;
    }

    public Integer getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(Integer totalNodes) {
        this.totalNodes = totalNodes;
    }

    public Integer getProcessedNodes() {
        return processedNodes;
    }

    public void setProcessedNodes(Integer processedNodes) {
        this.processedNodes = processedNodes;
    }

    public String getApprovalTitle() {
        return approvalTitle;
    }

    public void setApprovalTitle(String approvalTitle) {
        this.approvalTitle = approvalTitle;
    }

    public Integer getUrgencyLevel() {
        return urgencyLevel;
    }

    public void setUrgencyLevel(Integer urgencyLevel) {
        this.urgencyLevel = urgencyLevel;
    }

    // 业务逻辑方法
    public boolean isFinalStatus() {
        ApprovalStatusEnum statusEnum = getApprovalStatus();
        return statusEnum != null && statusEnum.isFinalStatus();
    }

    public void markAsCompleted() {
        this.finishTime = LocalDateTime.now();
        if (totalNodes != null && processedNodes < totalNodes) {
            this.processedNodes = totalNodes;
        }
    }

    @Override
    public String toString() {
        return "ApprovalInstance{" +
                "id=" + getId() +
                ", instanceNo='" + instanceNo + '\'' +
                ", flowId=" + flowId +
                ", businessId=" + businessId +
                ", businessType='" + businessType + '\'' +
                ", status=" + status +
                ", currentNode='" + currentNode + '\'' +
                ", createBy=" + createBy +
                ", finishTime=" + finishTime +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}