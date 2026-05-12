package com.ccms.dto;

import com.ccms.enums.ApprovalStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResult {
    
    /**
     * 操作是否成功
     */
    private boolean success;
    
    /**
     * 结果消息
     */
    private String message;
    
    /**
     * 审批实例ID
     */
    private Long instanceId;
    
    /**
     * 流程配置ID
     */
    private Long flowConfigId;
    
    /**
     * 当前节点
     */
    private Integer currentNode;
    
    /**
     * 审批状态
     */
    private ApprovalStatusEnum status;
    
    /**
     * 是否完成
     */
    private Boolean completed;
    
    /**
     * 完成时间
     */
    private Long finishTime;
    
    /**
     * 错误码（失败时使用）
     */
    private String errorCode;
    
    /**
     * 错误详情（失败时使用）
     */
    private String errorDetails;
}