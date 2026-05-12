package com.ccms.enums;

/**
 * 审批错误码定义
 * 统一管理所有审批相关的错误码和错误消息
 */
public enum ApprovalErrorCode {

    // 通用错误 (1000-1099)
    SUCCESS("1000", "操作成功"),
    SYSTEM_ERROR("1001", "系统内部错误"),
    PARAM_ERROR("1002", "参数错误"),
    DATA_NOT_FOUND("1003", "数据不存在"),
    DATA_ALREADY_EXISTS("1004", "数据已存在"),
    UNAUTHORIZED_OPERATION("1005", "无操作权限"),
    INVALID_OPERATION("1006", "非法操作"),
    CONFIG_ERROR("1007", "配置错误"),
    REQUEST_TIMEOUT("1008", "请求超时"),
    SERVICE_UNAVAILABLE("1009", "服务不可用"),

    // 审批流程配置错误 (1100-1199)
    FLOW_CONFIG_NOT_FOUND("1100", "审批流程配置不存在"),
    FLOW_CONFIG_INVALID("1101", "审批流程配置无效"),
    FLOW_NODE_CONFIG_ERROR("1102", "审批节点配置错误"),
    FLOW_CYCLE_DETECTED("1103", "检测到审批流程循环"),
    FLOW_APPROVER_REQUIRED("1104", "审批人必须设置"),
    FLOW_CONDITION_INVALID("1105", "流程条件配置无效"),
    FLOW_VERSION_CONFLICT("1106", "流程版本冲突"),

    // 审批实例错误 (1200-1299)
    INSTANCE_NOT_FOUND("1200", "审批实例不存在"),
    INSTANCE_STATUS_ERROR("1201", "审批实例状态错误"),
    INSTANCE_ALREADY_STARTED("1202", "审批实例已启动"),
    INSTANCE_CANNOT_WITHDRAW("1203", "申请已提交，无法撤回"),
    INSTANCE_EXPIRED("1204", "审批实例已过期"),
    INSTANCE_CONDITION_NOT_MET("1205", "实例条件不满足"),
    INSTANCE_CONCURRENT_MODIFICATION("1206", "审批实例并发修改"),

    // 审批记录错误 (1300-1399)
    RECORD_NOT_FOUND("1300", "审批记录不存在"),
    RECORD_STATUS_ERROR("1301", "审批记录状态错误"),
    RECORD_OPERATOR_MISMATCH("1302", "操作人与记录不匹配"),
    RECORD_TIMEOUT("1303", "审批记录已超时"),
    RECORD_APPROVAL_REQUIRED("1304", "必须填写审批意见"),
    RECORD_CANNOT_SKIP("1305", "不能跳过此审批环节"),
    RECORD_ALREADY_PROCESSED("1306", "审批记录已处理"),

    // 业务集成错误 (1400-1499)
    BUSINESS_TYPE_NOT_SUPPORTED("1400", "不支持的业务类型"),
    BUSINESS_DATA_INVALID("1401", "业务数据无效"),
    BUSINESS_STATUS_ERROR("1402", "业务状态错误"),
    BUSINESS_INTEGRATION_ERROR("1403", "业务集成错误"),
    BUSINESS_CALLBACK_FAILED("1404", "业务回调失败"),
    BUSINESS_LI MIT_EXCEEDED("1405", "业务限额超限"),

    // 验证错误 (1500-1599)
    VALIDATION_FAILED("1500", "数据验证失败"),
    REQUIRED_FIELD_MISSING("1501", "必填字段缺失"),
    FIELD_FORMAT_ERROR("1502", "字段格式错误"),
    AMOUNT_RANGE_ERROR("1503", "金额范围错误"),
    DATE_VALIDATION_ERROR("1504", "日期验证错误"),
    PERMISSION_VALIDATION_ERROR("1505", "权限验证失败"),
    WORKFLOW_VALIDATION_ERROR("1506", "工作流验证失败"),

    // 审计和日志错误 (1600-1699)
    AUDIT_LOG_FAILED("1600", "审计日志记录失败"),
    AUDIT_DATA_INCOMPLETE("1601", "审计数据不完整"),
    LOG_CONFIG_ERROR("1602", "日志配置错误"),
    
    // 查询和分析错误 (1700-1799)
    QUERY_PARAM_INVALID("1700", "查询参数无效"),
    ANALYSIS_DATA_INSUFFICIENT("1701", "分析数据不足"),
    STATISTICS_ERROR("1702", "统计计算错误"),

    // 外部服务错误 (1800-1899)
    EXTERNAL_SERVICE_ERROR("1800", "外部服务错误"),
    DATABASE_CONNECTION_ERROR("1801", "数据库连接错误"),
    CACHE_SERVICE_ERROR("1802", "缓存服务错误"),
    MESSAGE_SERVICE_ERROR("1803", "消息服务错误");

    private final String code;
    private final String message;

    ApprovalErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取枚举值
     */
    public static ApprovalErrorCode getByCode(String code) {
        for (ApprovalErrorCode errorCode : ApprovalErrorCode.values()) {
            if (errorCode.getCode().equals(code)) {
                return errorCode;
            }
        }
        return null;
    }

    /**
     * 验证错误码是否存在
     */
    public static boolean isValidCode(String code) {
        return getByCode(code) != null;
    }

    /**
     * 获取完整的错误信息
     */
    public String getFullMessage() {
        return String.format("[%s] %s", code, message);
    }

    /**
     * 获取带业务ID的错误信息
     */
    public String getMessageWithBusinessId(String businessId) {
        return String.format("[%s] %s (业务ID: %s)", code, message, businessId);
    }

    /**
     * 判断是否为成功状态
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }

    /**
     * 判断是否为系统错误
     */
    public boolean isSystemError() {
        return this.code.startsWith("100");
    }

    /**
     * 判断是否为业务错误
     */
    public boolean isBusinessError() {
        return this.code.startsWith("1") && !isSystemError();
    }
}