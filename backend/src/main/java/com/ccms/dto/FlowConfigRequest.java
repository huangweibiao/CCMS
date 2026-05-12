package com.ccms.dto;

import com.ccms.enums.BusinessTypeEnum;
import com.ccms.enums.PriorityTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 流程配置请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowConfigRequest {
    
    /**
     * 流程代码（唯一标识）
     */
    @NotBlank(message = "流程代码不能为空")
    private String flowCode;
    
    /**
     * 流程名称
     */
    @NotBlank(message = "流程名称不能为空")
    private String flowName;
    
    /**
     * 业务类型
     */
    @NotNull(message = "业务类型不能为空")
    private BusinessTypeEnum businessType;
    
    /**
     * 流程描述
     */
    private String description;
    
    /**
     * 优先级类型
     */
    private PriorityTypeEnum priorityType;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 金额上限（可选）
     */
    private Double amountLimit;
    
    /**
     * 金额下限（可选）
     */
    private Double amountFloor;
}