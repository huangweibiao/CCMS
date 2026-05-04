package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
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
     * 状态：0-运行中 1-已通过 2-已驳回 3-已撤销
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
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

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
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