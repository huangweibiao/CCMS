package com.ccms.enums;

/**
 * 费用申请单状态枚举
 * 实现状态机：草稿→审批中→已通过/驳回→待支付→已支付
 */
public enum ApplyStatusEnum {
    
    /** 草稿状态 */
    DRAFT(0, "草稿"),
    
    /** 审批中状态 */
    APPROVING(1, "审批中"),
    
    /** 已通过状态 */
    APPROVED(2, "已通过"),
    
    /** 已驳回状态 */
    REJECTED(3, "已驳回"),
    
    /** 待支付状态 */
    TO_BE_PAID(4, "待支付"),
    
    /** 已支付状态 */
    PAID(5, "已支付"),
    
    /** 已作废状态 */
    CANCELLED(6, "已作废");
    
    private final Integer code;
    private final String description;
    
    ApplyStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据状态码获取枚举
     */
    public static ApplyStatusEnum getByCode(Integer code) {
        for (ApplyStatusEnum status : ApplyStatusEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断状态是否允许变更到目标状态
     */
    public static boolean isTransitionAllowed(Integer fromStatus, Integer toStatus) {
        if (fromStatus == null || toStatus == null) {
            return false;
        }
        
        // 状态机流转规则定义
        switch (fromStatus) {
            case 0: // 草稿可以变更为：审批中、作废
                return toStatus == 1 || toStatus == 6;
            case 1: // 审批中可以变更为：已通过、已驳回、作废
                return toStatus == 2 || toStatus == 3 || toStatus == 6;
            case 2: // 已通过可以变更为：待支付、作废
                return toStatus == 4 || toStatus == 6;
            case 3: // 已驳回可以变更为：草稿（重新提交）、作废
                return toStatus == 0 || toStatus == 6;
            case 4: // 待支付可以变更为：已支付、作废
                return toStatus == 5 || toStatus == 6;
            case 5: // 已支付状态为终态，不允许变更
                return false;
            case 6: // 已作废状态为终态，不允许变更
                return false;
            default:
                return false;
        }
    }
    
    /**
     * 获取状态变更的描述
     */
    public static String getTransitionDescription(Integer fromStatus, Integer toStatus) {
        if (!isTransitionAllowed(fromStatus, toStatus)) {
            return "状态变更不合法";
        }
        
        String fromDesc = getByCode(fromStatus) != null ? getByCode(fromStatus).getDescription() : fromStatus + "";
        String toDesc = getByCode(toStatus) != null ? getByCode(toStatus).getDescription() : toStatus + "";
        
        return "从" + fromDesc + "变更为" + toDesc;
    }
}