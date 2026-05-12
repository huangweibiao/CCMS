package com.ccms.vo;

import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.ApprovalStatusEnum;
import com.ccms.enums.BusinessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批实例视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
}