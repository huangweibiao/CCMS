package com.ccms.entity.approval;

import com.ccms.entity.BaseEntity;
import com.ccms.enums.ApprovalActionEnum;
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
     * 审批配置ID
     */
    @Column(name = "approval_config_id")
    private Long approvalConfigId;
    
    /**
     * 审批步骤
     */
    @Column(name = "approval_step")
    private Integer approvalStep;
    
    /**
     * 审核结果
     */
    @Column(name = "approval_result", length = 32)
    private String approvalResult;
    
    /**
     * 申请人ID
     */
    @Column(name = "apply_user_id")
    private Long applyUserId;
    
    /**
     * 申请时间
     */
    @Column(name = "apply_time")
    private LocalDateTime applyTime;
    
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
    
    /**
     * 审批状态
     */
    @Column(name = "approval_status")
    private Integer approvalStatus;
    
    /**
     * 节点排序
     */
    @Column(name = "node_order")
    private Integer nodeOrder;
    
    /**
     * 审批人名称
     */
    @Column(name = "approver_name", length = 100)
    private String approverName;
    
    /**
     * 处理时长（小时）
     */
    @Column(name = "processing_time")
    private Double processingTime;
    
    /**
     * 是否自动审批
     */
    @Column(name = "is_auto_approved")
    private Boolean autoApproved = false;
    
    /**
     * 流程ID
     */
    @Transient
    private Long processId;
    
    /**
     * 节点ID
     */
    @Transient
    private Long nodeId;
    
    /**
     * 是否已审批
     */
    @Transient
    private Boolean approved;
    
    /**
     * 审批评论
     */
    @Transient
    private String comment;
    
    /**
     * 是否跳过
     */
    @Transient
    private Boolean skipped;
    
    // 枚举相关方法
    public ApprovalActionEnum getApprovalActionEnum() {
        return ApprovalActionEnum.getByCode(approvalAction);
    }

    public void setApprovalActionEnum(ApprovalActionEnum actionEnum) {
        this.approvalAction = actionEnum != null ? actionEnum.getCode() : null;
    }

    // Getters and Setters
    public Long getApprovalConfigId() {
        return approvalConfigId;
    }
    
    public void setApprovalConfigId(Long approvalConfigId) {
        this.approvalConfigId = approvalConfigId;
    }
    
    public Integer getApprovalStep() {
        return approvalStep;
    }
    
    public void setApprovalStep(Integer approvalStep) {
        this.approvalStep = approvalStep;
    }
    
    public String getApprovalResult() {
        return approvalResult;
    }
    
    public void setApprovalResult(String approvalResult) {
        this.approvalResult = approvalResult;
    }
    
    public Long getApplyUserId() {
        return applyUserId;
    }
    
    public void setApplyUserId(Long applyUserId) {
        this.applyUserId = applyUserId;
    }
    
    public LocalDateTime getApplyTime() {
        return applyTime;
    }
    
    public void setApplyTime(LocalDateTime applyTime) {
        this.applyTime = applyTime;
    }
    
    public Integer getApprovalStatus() {
        return approvalStatus;
    }
    
    public void setApprovalStatus(Integer approvalStatus) {
        this.approvalStatus = approvalStatus;
    }
    
    public Integer getNodeOrder() {
        return nodeOrder;
    }
    
    public void setNodeOrder(Integer nodeOrder) {
        this.nodeOrder = nodeOrder;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public Double getProcessingTime() {
        return processingTime;
    }

    public void setProcessingTime(Double processingTime) {
        this.processingTime = processingTime;
    }

    public Boolean getAutoApproved() {
        return autoApproved;
    }

    public void setAutoApproved(Boolean autoApproved) {
        this.autoApproved = autoApproved;
    }

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

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getSkipped() {
        return skipped;
    }

    public void setSkipped(Boolean skipped) {
        this.skipped = skipped;
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