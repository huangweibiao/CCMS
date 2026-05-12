package com.ccms.vo;

import com.ccms.entity.approval.ApprovalNode;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.PriorityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审批流程配置视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalFlowConfigVO {
    
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
}