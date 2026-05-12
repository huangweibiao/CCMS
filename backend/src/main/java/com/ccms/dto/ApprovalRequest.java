package com.ccms.dto;

import com.ccms.enums.BusinessTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 审批请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {
    
    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private BusinessTypeEnum businessType;
    
    /**
     * 业务ID
     */
    @NotBlank(message = "业务ID不能为空")
    private String businessId;
    
    /**
     * 申请人ID
     */
    @NotNull(message = "申请人ID不能为空")
    private Long applicantId;
    
    /**
     * 审批标题
     */
    @NotBlank(message = "审批标题不能为空")
    private String title;
    
    /**
     * 审批内容
     */
    private String content;
    
    /**
     * 金额（可选）
     */
    private BigDecimal amount;
    
    /**
     * 部门ID（可选，用于流程配置匹配）
     */
    private Long departId;
    
    /**
     * 紧急程度
     */
    private String urgencyLevel;
    
    /**
     * 自定义数据（JSON格式）
     */
    private String customData;
}