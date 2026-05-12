package com.ccms.vo;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.BusinessTypeEnum;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 审批实例视图对象
 */
public class ApprovalInstanceVO {
    
    /**
     * 实例ID
     */
    private Long id;
    
    /**
     * 流程配置ID
     */
    private Long flowConfigId;
    
    /**
     * 流程配置信息
     */
    private ApprovalFlowConfig flowConfig;
    
    /**
     * 业务类型
     */
    private BusinessTypeEnum businessType;
    
    /**
     * 业务ID
     */
    private String businessId;
    
    /**
     * 申请人ID
     */
    private Long applicantId;
    
    /**
     * 申请人姓名
     */
    private String applicantName;
    
    /**
     * 申请标题
     */
    private String title;
    
    /**
     * 申请内容
     */
    private String content;
    
    /**
     * 当前节点
     */
    private Integer currentNode;
    
    /**
     * 总节点数
     */
    private Integer totalNodes;
    
    /**
     * 已处理节点数
     */
    private Integer processedNodes;
    
    /**
     * 审批状态
     */
    private ApprovalStatusEnum status;
    
    /**
     * 当前审批人ID
     */
    private Long currentApproverId;
    
    /**
     * 当前审批人姓名
     */
    private String currentApproverName;
    
    /**
     * 当前审批节点信息
     */
    private ApprovalNode currentNodeDetail;
    
    /**
     * 审批节点列表
     */
    private List<ApprovalNode> approvalNodes;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;
    
    /**
     * 处理时长（小时）
     */
    private Double processingHours;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 是否已完成
     */
    public boolean isCompleted() {
        return status != null && status.isFinalStatus();
    }
    
    /**
     * 是否待审批
     */
    public boolean isPending() {
        return status == ApprovalStatusEnum.RUNNING || 
               status == ApprovalStatusEnum.WAITING ||
               status == ApprovalStatusEnum.PENDING;
    }
    
    /**
     * 获取进度百分比
     */
    public Integer getProgressPercentage() {
        if (totalNodes == null || totalNodes == 0) {
            return 0;
        }
        return Math.min(100, (processedNodes * 100) / totalNodes);
    }

    public ApprovalInstanceVO() {}

    public ApprovalInstanceVO(Long id, Long flowConfigId, ApprovalFlowConfig flowConfig, BusinessTypeEnum businessType, 
                             String businessId, Long applicantId, String applicantName, String title, String content,
                             Integer currentNode, Integer totalNodes, Integer processedNodes, ApprovalStatusEnum status,
                             Long currentApproverId, String currentApproverName, ApprovalNode currentNodeDetail,
                             List<ApprovalNode> approvalNodes, LocalDateTime createTime, LocalDateTime updateTime,
                             LocalDateTime finishTime, Double processingHours, String remarks) {
        this.id = id;
        this.flowConfigId = flowConfigId;
        this.flowConfig = flowConfig;
        this.businessType = businessType;
        this.businessId = businessId;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.title = title;
        this.content = content;
        this.currentNode = currentNode;
        this.totalNodes = totalNodes;
        this.processedNodes = processedNodes;
        this.status = status;
        this.currentApproverId = currentApproverId;
        this.currentApproverName = currentApproverName;
        this.currentNodeDetail = currentNodeDetail;
        this.approvalNodes = approvalNodes;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.finishTime = finishTime;
        this.processingHours = processingHours;
        this.remarks = remarks;
    }

    // Getter and Setter methods
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFlowConfigId() {
        return flowConfigId;
    }

    public void setFlowConfigId(Long flowConfigId) {
        this.flowConfigId = flowConfigId;
    }

    public ApprovalFlowConfig getFlowConfig() {
        return flowConfig;
    }

    public void setFlowConfig(ApprovalFlowConfig flowConfig) {
        this.flowConfig = flowConfig;
    }

    public BusinessTypeEnum getBusinessType() {
        return businessType;
    }

    public void setBusinessType(BusinessTypeEnum businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Integer currentNode) {
        this.currentNode = currentNode;
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

    public ApprovalStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatusEnum status) {
        this.status = status;
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

    public ApprovalNode getCurrentNodeDetail() {
        return currentNodeDetail;
    }

    public void setCurrentNodeDetail(ApprovalNode currentNodeDetail) {
        this.currentNodeDetail = currentNodeDetail;
    }

    public List<ApprovalNode> getApprovalNodes() {
        return approvalNodes;
    }

    public void setApprovalNodes(List<ApprovalNode> approvalNodes) {
        this.approvalNodes = approvalNodes;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public Double getProcessingHours() {
        return processingHours;
    }

    public void setProcessingHours(Double processingHours) {
        this.processingHours = processingHours;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalInstanceVO that = (ApprovalInstanceVO) o;
        return Objects.equals(id, that.id) && Objects.equals(flowConfigId, that.flowConfigId) 
                && Objects.equals(flowConfig, that.flowConfig) && Objects.equals(businessType, that.businessType) 
                && Objects.equals(businessId, that.businessId) && Objects.equals(applicantId, that.applicantId) 
                && Objects.equals(applicantName, that.applicantName) && Objects.equals(title, that.title) 
                && Objects.equals(content, that.content) && Objects.equals(currentNode, that.currentNode) 
                && Objects.equals(totalNodes, that.totalNodes) && Objects.equals(processedNodes, that.processedNodes) 
                && Objects.equals(status, that.status) && Objects.equals(currentApproverId, that.currentApproverId) 
                && Objects.equals(currentApproverName, that.currentApproverName) 
                && Objects.equals(currentNodeDetail, that.currentNodeDetail) && Objects.equals(approvalNodes, that.approvalNodes) 
                && Objects.equals(createTime, that.createTime) && Objects.equals(updateTime, that.updateTime) 
                && Objects.equals(finishTime, that.finishTime) && Objects.equals(processingHours, that.processingHours) 
                && Objects.equals(remarks, that.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, flowConfigId, flowConfig, businessType, businessId, applicantId, applicantName, 
                title, content, currentNode, totalNodes, processedNodes, status, currentApproverId, currentApproverName,
                currentNodeDetail, approvalNodes, createTime, updateTime, finishTime, processingHours, remarks);
    }

    @Override
    public String toString() {
        return "ApprovalInstanceVO{" +
                "id=" + id +
                ", flowConfigId=" + flowConfigId +
                ", flowConfig=" + flowConfig +
                ", businessType=" + businessType +
                ", businessId='" + businessId + '\'' +
                ", applicantId=" + applicantId +
                ", applicantName='" + applicantName + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", currentNode=" + currentNode +
                ", totalNodes=" + totalNodes +
                ", processedNodes=" + processedNodes +
                ", status=" + status +
                ", currentApproverId=" + currentApproverId +
                ", currentApproverName='" + currentApproverName + '\'' +
                ", currentNodeDetail=" + currentNodeDetail +
                ", approvalNodes=" + approvalNodes +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", finishTime=" + finishTime +
                ", processingHours=" + processingHours +
                ", remarks='" + remarks + '\'' +
                '}';
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long flowConfigId;
        private ApprovalFlowConfig flowConfig;
        private BusinessTypeEnum businessType;
        private String businessId;
        private Long applicantId;
        private String applicantName;
        private String title;
        private String content;
        private Integer currentNode;
        private Integer totalNodes;
        private Integer processedNodes;
        private ApprovalStatusEnum status;
        private Long currentApproverId;
        private String currentApproverName;
        private ApprovalNode currentNodeDetail;
        private List<ApprovalNode> approvalNodes;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
        private LocalDateTime finishTime;
        private Double processingHours;
        private String remarks;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder flowConfigId(Long flowConfigId) {
            this.flowConfigId = flowConfigId;
            return this;
        }

        public Builder flowConfig(ApprovalFlowConfig flowConfig) {
            this.flowConfig = flowConfig;
            return this;
        }

        public Builder businessType(BusinessTypeEnum businessType) {
            this.businessType = businessType;
            return this;
        }

        public Builder businessId(String businessId) {
            this.businessId = businessId;
            return this;
        }

        public Builder applicantId(Long applicantId) {
            this.applicantId = applicantId;
            return this;
        }

        public Builder applicantName(String applicantName) {
            this.applicantName = applicantName;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder currentNode(Integer currentNode) {
            this.currentNode = currentNode;
            return this;
        }

        public Builder totalNodes(Integer totalNodes) {
            this.totalNodes = totalNodes;
            return this;
        }

        public Builder processedNodes(Integer processedNodes) {
            this.processedNodes = processedNodes;
            return this;
        }

        public Builder status(ApprovalStatusEnum status) {
            this.status = status;
            return this;
        }

        public Builder currentApproverId(Long currentApproverId) {
            this.currentApproverId = currentApproverId;
            return this;
        }

        public Builder currentApproverName(String currentApproverName) {
            this.currentApproverName = currentApproverName;
            return this;
        }

        public Builder currentNodeDetail(ApprovalNode currentNodeDetail) {
            this.currentNodeDetail = currentNodeDetail;
            return this;
        }

        public Builder approvalNodes(List<ApprovalNode> approvalNodes) {
            this.approvalNodes = approvalNodes;
            return this;
        }

        public Builder createTime(LocalDateTime createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder finishTime(LocalDateTime finishTime) {
            this.finishTime = finishTime;
            return this;
        }

        public Builder processingHours(Double processingHours) {
            this.processingHours = processingHours;
            return this;
        }

        public Builder remarks(String remarks) {
            this.remarks = remarks;
            return this;
        }

        public ApprovalInstanceVO build() {
            return new ApprovalInstanceVO(id, flowConfigId, flowConfig, businessType, businessId, applicantId,
                    applicantName, title, content, currentNode, totalNodes, processedNodes, status, currentApproverId,
                    currentApproverName, currentNodeDetail, approvalNodes, createTime, updateTime, finishTime,
                    processingHours, remarks);
        }
    }
}