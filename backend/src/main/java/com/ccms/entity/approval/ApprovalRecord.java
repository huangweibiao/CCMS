package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 审批记录表实体类
 * 对应表名：ccms_approval_record
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_approval_record")
public class ApprovalRecord extends BaseEntity {

    /**
     * 审批实例ID
     */
    @Column(name = "instance_id", nullable = false)
    private Long instanceId;
    
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
     * 审批节点
     */
    @Column(name = "approval_node", length = 64, nullable = false)
    private String approvalNode;
    
    /**
     * 审批人ID
     */
    @Column(name = "approver_id", nullable = false)
    private Long approverId;
    
    /**
     * 审批动作：1-同意 2-驳回 3-转审 4-撤销
     */
    @Column(name = "approval_action", nullable = false)
    private Integer approvalAction;
    
    /**
     * 审批意见
     */
    @Column(name = "approval_remark", length = 512)
    private String approvalRemark;
    
    /**
     * 审批时间
     */
    @Column(name = "approval_time", nullable = false)
    private LocalDateTime approvalTime;
    
    /**
     * 转审目标人ID
     */
    @Column(name = "transfer_to")
    private Long transferTo;

    // Getters and Setters
    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long instanceId) {
        this.instanceId = instanceId;
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

    public String getApprovalNode() {
        return approvalNode;
    }

    public void setApprovalNode(String approvalNode) {
        this.approvalNode = approvalNode;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public Integer getApprovalAction() {
        return approvalAction;
    }

    public void setApprovalAction(Integer approvalAction) {
        this.approvalAction = approvalAction;
    }

    public String getApprovalRemark() {
        return approvalRemark;
    }

    public void setApprovalRemark(String approvalRemark) {
        this.approvalRemark = approvalRemark;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public Long getTransferTo() {
        return transferTo;
    }

    public void setTransferTo(Long transferTo) {
        this.transferTo = transferTo;
    }

    @Override
    public String toString() {
        return "ApprovalRecord{" +
                "id=" + getId() +
                ", instanceId=" + instanceId +
                ", businessId=" + businessId +
                ", businessType='" + businessType + '\'' +
                ", approvalNode='" + approvalNode + '\'' +
                ", approverId=" + approverId +
                ", approvalAction=" + approvalAction +
                ", approvalRemark='" + approvalRemark + '\'' +
                ", approvalTime=" + approvalTime +
                ", transferTo=" + transferTo +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}