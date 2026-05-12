package com.ccms.config;

/**
 * 审批流程API文档配置类
 * 提供详细的API接口规范和使用说明
 */
public class ApprovalAPIDocumentation {

    /**
     * 审批流程API规范
     * 
     * 概述：
     * 企业级费控管理系统的审批流程模块，提供完整的流程配置、实例管理、
     * 审批操作和状态查询功能。支持多种业务类型（报销、借款、请假等）
     * 和灵活的审批流程配置。
     * 
     * 基础路径：/api/approval
     * 认证方式：Bearer Token（JWT）
     * 响应格式：统一JSON响应格式
     * 
     * 统一响应格式：
     * {
     *   "code": "1000",
     *   "message": "操作成功",
     *   "data": {...},
     *   "timestamp": "2024-01-01T00:00:00"
     * }
     * 
     * 错误码说明：
     * 1000: 操作成功
     * 1001: 系统内部错误
     * 1100-1199: 审批流程相关错误
     */

    /**
     * API端点详细说明
     */
    public static class APIEndpoints {
        
        /**
         * 审批流程配置管理接口
         */
        public static final class FlowConfig {
            /**
             * GET /api/approval/configs
             * 查询审批流程配置列表
             * 
             * 请求参数：
             * - businessType (可选): 业务类型筛选
             * - category (可选): 分类筛选
             * - page (可选): 页码，默认1
             * - size (可选): 页大小，默认10
             * 
             * 响应数据：
             * {
             *   "total": 100,
             *   "pages": 10,
             *   "data": [
             *     {
             *       "id": 1,
             *       "flowName": "小额报销审批流程",
             *       "businessType": "EXPENSE_APPROVAL",
             *       "approvalType": "SEQUENTIAL",
             *       "category": "报销",
             *       "minAmount": 0.00,
             *       "maxAmount": 500.00,
             *       "active": true
             *     }
             *   ]
             * }
             */
            public static final String GET_CONFIGS = "查询审批流程配置列表";
            
            /**
             * GET /api/approval/configs/{id}
             * 根据ID查询审批流程配置详情
             * 
             * 路径参数：
             * - id: 配置ID
             * 
             * 响应数据：
             * {
             *   "id": 1,
             *   "flowName": "小额报销审批流程",
             *   "businessType": "EXPENSE_APPROVAL",
             *   "approvalType": "SEQUENTIAL",
             *   "category": "报销",
             *   "minAmount": 0.00,
             *   "maxAmount": 500.00,
             *   "nodes": [...],
             *   "active": true
             * }
             */
            public static final String GET_CONFIG_BY_ID = "查询审批流程配置详情";
            
            /**
             * POST /api/approval/configs
             * 创建审批流程配置
             * 
             * 请求体：
             * {
             *   "flowName": "测试流程",
             *   "businessType": "EXPENSE_APPROVAL",
             *   "approvalType": "SEQUENTIAL",
             *   "category": "报销",
             *   "minAmount": 0.00,
             *   "maxAmount": 1000.00,
             *   "description": "测试流程描述"
             * }
             * 
             * 响应数据：创建后的配置详情
             */
            public static final String CREATE_CONFIG = "创建审批流程配置";
            
            /**
             * PUT /api/approval/configs/{id}
             * 更新审批流程配置
             * 
             * 路径参数：
             * - id: 配置ID
             * 
             * 请求体：同创建接口
             */
            public static final String UPDATE_CONFIG = "更新审批流程配置";
            
            /**
             * DELETE /api/approval/configs/{id}
             * 删除审批流程配置（软删除）
             */
            public static final String DELETE_CONFIG = "删除审批流程配置";
            
            /**
             * POST /api/approval/configs/{configId}/nodes
             * 为审批流程添加审批节点
             * 
             * 路径参数：
             * - configId: 配置ID
             * 
             * 请求体：
             * {
             *   "nodeName": "主管审批",
             *   "nodeType": "APPROVAL",
             *   "approverIds": [1001, 1002],
             *   "approvalStrategy": "OR",
             *   "nodeOrder": 1,
             *   "conditions": "amount >= 100 && amount < 5000"
             * }
             */
            public static final String ADD_NODE = "添加审批节点";
        }

        /**
         * 审批实例管理接口
         */
        public static final class ApprovalInstance {
            
            /**
             * POST /api/approval/instances
             * 创建审批实例
             * 
             * 请求体（报销业务）：
             * {
             *   "businessType": "EXPENSE_APPROVAL",
             *   "businessTitle": "交通费报销申请",
             *   "applicantId": 1001,
             *   "departmentId": 101,
             *   "amount": 150.50,
             *   "description": "本月交通费用报销",
             *   "expenseDate": "2024-01-01T00:00:00"
             * }
             */
            public static final String CREATE_INSTANCE = "创建审批实例";
            
            /**
             * GET /api/approval/instances/{id}
             * 查询审批实例详情
             * 
             * 响应数据：
             * {
             *   "id": 1,
             *   "businessType": "EXPENSE_APPROVAL",
             *   "businessTitle": "交通费报销申请",
             *   "status": "DRAFT",
             *   "currentStatus": "草稿",
             *   "amount": 150.50,
             *   "applicant": {...},
             *   "records": [...],
             *   "currentNode": {...}
             * }
             */
            public static final String GET_INSTANCE = "查询审批实例详情";
            
            /**
             * GET /api/approval/instances
             * 查询审批实例列表
             * 
             * 查询参数：
             * - applicantId: 申请人ID
             * - businessType: 业务类型
             * - status: 状态筛选
             * - startDate/endDate: 申请时间范围
             * - page/size: 分页参数
             */
            public static final String GET_INSTANCES = "查询审批实例列表";
            
            /**
             * GET /api/approval/instances/my-requests
             * 查询我的申请列表
             * 
             * 参数：applicantId (必填)
             */
            public static final String GET_MY_REQUESTS = "查询我的申请列表";
            
            /**
             * GET /api/approval/instances/pending
             * 查询待审批列表
             * 
             * 参数：approverId (必填)
             */
            public static final String GET_PENDING_REQUESTS = "查询待审批列表";
        }

        /**
         * 审批操作接口
         */
        public static final class ApprovalAction {
            
            /**
             * POST /api/approval/instances/{instanceId}/submit
             * 提交审批申请
             * 
             * 路径参数：instanceId
             * 
             * 响应状态变化：DRAFT → APPROVING
             */
            public static final String SUBMIT = "提交审批申请";
            
            /**
             * POST /api/approval/instances/{instanceId}/approve
             * 进行审批操作
             * 
             * 请求体：
             * {
             *   "actionType": "APPROVE", // 或 REJECT, TRANSFER, WITHDRAW 等
             *   "comment": "审核意见",
             *   "approverId": 1002,
             *   "nextApproverId": 1003 // 转审时使用
             * }
             * 
             * 响应：更新后的实例状态和审批记录
             */
            public static final String APPROVE = "进行审批操作";
            
            /**
             * POST /api/approval/instances/{instanceId}/withdraw
             * 撤销审批申请
             * 
             * 响应状态变化：APPROVING → WITHDRAWN
             */
            public static final String WITHDRAW = "撤销审批申请";
            
            /**
             * POST /api/approval/instances/{instanceId}/resubmit
             * 重新提交被驳回的申请
             * 
             * 响应状态变化：REJECTED → APPROVING
             */
            public static final String RESUBMIT = "重新提交申请";
            
            /**
             * POST /api/approval/instances/{instanceId}/transfer
             * 转审给其他审批人
             * 
             * 请求体：包含nextApproverId
             */
            public static final String TRANSFER = "转审操作";
            
            /**
             * POST /api/approval/instances/{instanceId}/cancel
             * 取消审批申请
             * 
             * 响应状态变化：DRAFT/APPROVING → CANCELLED
             */
            public static final String CANCEL = "取消审批申请";
        }

        /**
         * 审批记录查询接口
         */
        public static final class ApprovalRecord {
            
            /**
             * GET /api/approval/instances/{instanceId}/records
             * 查询审批记录历史
             * 
             * 响应：审批记录列表，按时间倒序
             */
            public static final String GET_RECORDS = "查询审批记录";
            
            /**
             * GET /api/approval/records/{recordId}
             * 查询单个审批记录详情
             */
            public static final String GET_RECORD = "查询审批记录详情";
        }

        /**
         * 统计和报表接口
         */
        public static final class Statistics {
            
            /**
             * GET /api/approval/statistics/department/{departmentId}
             * 查询部门审批统计
             * 
             * 响应：按状态统计的数量
             */
            public static final String DEPARTMENT_STATS = "部门审批统计";
            
            /**
             * GET /api/approval/statistics/user/{userId}
             * 查询用户审批统计
             */
            public static final String USER_STATS = "用户审批统计";
            
            /**
             * GET /api/approval/statistics/approval-rate
             * 查询审批通过率统计
             */
            public static final String APPROVAL_RATE = "审批通过率统计";
        }
    }

    /**
     * 业务类型和审批流程匹配规则
     */
    public static class BusinessTypeRules {
        
        /**
         * 报销业务 (EXPENSE_APPROVAL)
         * - 金额范围匹配：
         *   0-500元: 直接主管审批
         *   500-2000元: 部门经理审批
         *   2000-5000元: 主管+财务审批
         *   5000元以上: 主管+财务+总经理审批
         * - 特殊业务：差旅费、招待费、培训费等
         */
        public static final String EXPENSE_RULES = "报销业务审批规则";
        
        /**
         * 借款业务 (LOAN_APPROVAL)
         * - 金额范围匹配：
         *   0-1000元: 直接主管审批
         *   1000-5000元: 主管+财务审批
         *   5000元以上: 主管+财务+总经理审批
         * - 期限要求：短期借款、长期借款
         */
        public static final String LOAN_RULES = "借款业务审批规则";
        
        /**
         * 请假业务 (LEAVE_APPROVAL)
         * - 时长匹配：
         *   1-3天: 直接主管审批
         *   3-7天: 部门经理审批
         *   7天以上: 主管+人事审批
         * - 请假类型：年假、病假、事假、调休等
         */
        public static final String LEAVE_RULES = "请假业务审批规则";
        
        /**
         * 采购业务 (PURCHASE_APPROVAL)
         * - 金额范围匹配：
         *   0-1000元: 部门负责人审批
         *   1000-50000元: 采购部+财务审批
         *   50000元以上: 多部门会签+总经理审批
         * - 采购类型：固定资产、办公用品、服务采购等
         */
        public static final String PURCHASE_RULES = "采购业务审批规则";
    }

    /**
     * 错误码详细说明
     */
    public static class ErrorCodes {
        
        /**
         * 系统通用错误码
         */
        public static final class System {
            public static final String SUCCESS = "1000";
            public static final String SYSTEM_ERROR = "1001";
            public static final String PARAM_ERROR = "1002";
            public static final String UNAUTHORIZED = "1003";
            public static final String FORBIDDEN = "1004";
        }
        
        /**
         * 审批流程相关错误码
         */
        public static final class Approval {
            public static final String FLOW_CONFIG_NOT_FOUND = "1100";
            public static final String INSTANCE_NOT_FOUND = "1101";
            public static final String NODE_NOT_FOUND = "1102";
            public static final String RECORD_NOT_FOUND = "1103";
            public static final String INVALID_STATUS_TRANSITION = "1104";
            public static final String PERMISSION_DENIED = "1105";
            public static final String BUSINESS_TYPE_NOT_SUPPORTED = "1106";
            public static final String AMOUNT_OUT_OF_RANGE = "1107";
            public static final String APPROVER_NOT_FOUND = "1108";
            public static final String FLOW_CONFIG_INACTIVE = "1109";
            public static final String NODE_CONDITION_NOT_MATCH = "1110";
            public static final String APPROVAL_STRATEGY_NOT_SUPPORTED = "1111";
        }
        
        /**
         * 业务集成错误码
         */
        public static final class Business {
            public static final String BUSINESS_DATA_ERROR = "1200";
            public static final String CALLBACK_FAILED = "1201";
            public static final String DATA_SYNC_ERROR = "1202";
        }
    }

    /**
     * 状态流转规则
     */
    public static class StatusTransitionRules {
        
        /**
         * 审批状态流转图：
         * DRAFT → [SUBMIT] → APPROVING → [APPROVE] → COMPLETED
         *                              → [REJECT] → REJECTED → [RESUBMIT] → APPROVING
         *                              → [WITHDRAW] → WITHDRAWN
         *                              → [CANCEL] → CANCELLED
         *                              → [TRANSFER] → APPROVING (新审批人)
         *                              → [TERMINATE] → TERMINATED
         * 
         * 允许的操作：
         * - 草稿状态(DRAFT): SUBMIT, CANCEL
         * - 审批中(APPROVING): APPROVE, REJECT, WITHDRAW, TRANSFER, TERMINATE
         * - 已驳回(REJECTED): RESUBMIT
         * - 其他状态：只允许查询操作
         */
        public static final String TRANSITION_DIAGRAM = "审批状态流转图";
    }

    /**
     * 性能优化指导
     */
    public static class PerformanceGuidelines {
        
        /**
         * 高频查询接口优化：
         * - 审批配置查询：使用Redis缓存
         * - 待审批列表：分页查询 + 索引优化
         * - 审批记录查询：时间范围限制 + 分页
         */
        public static final String QUERY_OPTIMIZATION = "查询性能优化";
        
        /**
         * 写操作优化：
         * - 审批操作：异步处理 + 事务控制
         * - 批量操作：分批处理 + 批量提交
         * - 审计日志：异步写入 + 批量提交
         */
        public static final String WRITE_OPTIMIZATION = "写入性能优化";
        
        /**
         * 内存使用优化：
         * - 大对象缓存：使用软引用
         * - 连接池配置：合理设置最大连接数
         * - 查询结果集：使用流式处理
         */
        public static final String MEMORY_OPTIMIZATION = "内存使用优化";
    }

    /**
     * 安全规范
     */
    public static class SecuritySpecifications {
        
        /**
         * 权限控制：
         * - 申请人只能操作自己的审批实例
         * - 审批人只能审批分配给自己的节点
         * - 管理员可以查看所有审批流程
         */
        public static final String ACCESS_CONTROL = "访问权限控制";
        
        /**
         * 数据安全：
         * - 敏感数据脱敏处理
         * - 操作日志完整记录
         * - 修改操作需要二次确认
         */
        public static final String DATA_SECURITY = "数据安全规范";
        
        /**
         * 审计要求：
         * - 所有操作记录审计日志
         * - 关键操作需要多级审批
         * - 异常操作自动告警
         */
        public static final String AUDIT_REQUIREMENTS = "审计合规要求";
    }
}