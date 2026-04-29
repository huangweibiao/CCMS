package com.ccms.enums;

/**
 * 操作类型枚举
 * 定义系统中的各种操作类型
 */
public enum OperateTypeEnum {
    
    // 费用申请相关操作
    APPLY_CREATE(1001, "创建费用申请", "创建新的费用申请单"),
    APPLY_UPDATE(1002, "更新费用申请", "修改费用申请单信息"),
    APPLY_SUBMIT(1003, "提交费用申请", "提交费用申请审批"),
    APPLY_APPROVE(1004, "审批费用申请", "审批费用申请通过"),
    APPLY_REJECT(1005, "驳回报销申请", "驳回费用申请"),
    APPLY_CANCEL(1006, "作废费用申请", "作废费用申请单"),
    
    // 报销单相关操作
    REIMBURSE_CREATE(2001, "创建报销单", "创建新的报销单"),
    REIMBURSE_UPDATE(2002, "更新报销单", "修改报销单信息"),
    REIMBURSE_SUBMIT(2003, "提交报销单", "提交报销单审批"),
    REIMBURSE_APPROVE(2004, "审批报销单", "审批报销单通过"),
    REIMBURSE_REJECT(2005, "驳回报销单", "驳回报销单"),
    REIMBURSE_PAYMENT_PENDING(2006, "标记待支付", "标记报销单为待支付状态"),
    REIMBURSE_PAYMENT_COMPLETE(2007, "完成支付", "完成报销单支付操作"),
    REIMBURSE_CANCEL(2008, "作废报销单", "作废报销单"),
    
    // 预算相关操作
    BUDGET_CREATE(3001, "创建预算", "创建新的预算方案"),
    BUDGET_UPDATE(3002, "更新预算", "修改预算信息"),
    BUDGET_FREEZE(3003, "冻结预算", "冻结预算金额"),
    BUDGET_DEDUCT(3004, "扣减预算", "扣减预算金额"),
    BUDGET_RELEASE(3005, "解冻预算", "解冻预算金额"),
    
    // 审批流相关操作
    APPROVAL_CONFIG(4001, "配置审批流", "配置审批流程"),
    APPROVAL_INSTANCE_CREATE(4002, "创建审批实例", "创建审批流程实例"),
    APPROVAL_PROCESS(4003, "处理审批", "处理审批任务"),
    APPROVAL_TRANSFER(4004, "转审", "转审审批任务"),
    
    // 用户相关操作
    USER_LOGIN(5001, "用户登录", "用户登录系统"),
    USER_LOGOUT(5002, "用户登出", "用户登出系统"),
    USER_PASSWORD_CHANGE(5003, "修改密码", "修改用户密码"),
    
    // 系统管理操作
    SYSTEM_CONFIG(6001, "系统配置", "修改系统配置参数"),
    DATA_EXPORT(6002, "数据导出", "导出系统数据"),
    DATA_IMPORT(6003, "数据导入", "导入数据到系统");
    
    private final Integer code;
    private final String name;
    private final String description;
    
    OperateTypeEnum(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }
    
    public Integer getCode() {
        return code;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据操作类型代码获取枚举
     */
    public static OperateTypeEnum getByCode(Integer code) {
        for (OperateTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}