package com.ccms.vo;

import com.ccms.enums.ApprovalActionEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 审批记录视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRecordVO {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 审批实例ID
     */
    private Long instanceId;
    
    /**
     * 审批节点ID
     */
    private Long nodeId;
    
    /**
     * 审批节点步骤编号
     */
    private Integer stepNumber;
    
    /**
     * 审批人ID
     */
    private Long approverId;
    
    /**
     * 审批人姓名
     */
    private String approverName;
    
    /**
     * 部门信息
     */
    private String department;
    
    /**
     * 职位信息
     */
    private String position;
    
    /**
     * 审批操作
     */
    private ApprovalActionEnum action;
    
    /**
     * 操作说明
     */
    private String actionDescription;
    
    /**
     * 审批意见
     */
    private String comments;
    
    /**
     * 处理时间
     */
    private LocalDateTime processTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 前处理时间（分钟）
     */
    private Long beforeProcessMinutes;
    
    /**
     * 是否超时处理
     */
    private Boolean timeoutFlag;
    
    /**
     * 附件数量
     */
    private Integer attachmentCount;
    
    /**
     * 自定义字段（JSON格式）
     */
    private String customData;
}