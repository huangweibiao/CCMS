/**
 * 审批流程API文档
 * 
 * 本模块提供完整的审批流程管理和业务集成接口
 * 
 * ## 核心功能
 * • 审批流程配置管理
 * • 审批实例生命周期管理
 * • 多级多节点审批支持
 * • 审批历史记录查询
 * • 状态转换验证和异常处理
 * 
 * ## 业务集成示例
 * 
 * ### 1. 费用报销审批流程
 * ```java
 * // 用户提交报销申请
 * ApprovalRequest request = new ApprovalRequest();
 * request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
 * request.setBusinessId("EXPENSE_001");
 * request.setApplicantId(1001L);
 * request.setTitle("差旅费用报销");
 * request.setAmount(new BigDecimal("2580.50"));
 * 
 * // 调用审批接口
 * ApiResult<ApprovalInstance> result = approvalController.submitApproval(request);
 * if (result.isSuccess()) {
 *     // 申请提交成功，等待审批
 *     return Response.ok("报销申请已提交审批");
 * }
 * ```
 * 
 * ### 2. 借款申请审批流程
 * ```java
 * // 借款申请审批
 * ApprovalRequest request = new ApprovalRequest();
 * request.setBusinessType(BusinessTypeEnum.LOAN_APPLY);
 * request.setBusinessId("LOAN_20240115001");
 * request.setApplicantId(1002L);
 * request.setTitle("项目借款申请");
 * request.setAmount(new BigDecimal("5000.00"));
 * 
 * // 金额触发多级审批
 * ApiResult<ApprovalInstance> result = approvalController.submitApproval(request);
 * ```
 * 
 * ## API接口规范
 */
package com.ccms.controller;

/**
 * 审批流程API接口文档
 * 
 * ### 基本URL
 * - 基础路径: `/api/approval`
 * 
 * ### 统一响应格式
 * ```json
 * {
 *   "success": true,
 *   "data": {
 *     // 业务数据
 *   },
 *   "errorCode": "1000",
 *   "errorMessage": "操作成功",
 *   "timestamp": "2024-01-15T10:30:00"
 * }
 * ```
 * 
 * ### 错误码说明
 * | 错误码 | 说明 | 处理建议 |
 * |--------|------|----------|
 * | 1000   | 操作成功 | - |
 * | 1100   | 审批流程配置不存在 | 检查业务类型和金额范围 |
 * | 1101   | 当前状态下不允许此操作 | 检查审批实例状态 |
 * | 1102   | 审批人不匹配 | 确认当前审批人身份 |
 * | 1103   | 审批流程实例不存在 | 检查实例ID是否正确 |
 * | 1104   | 金额范围不匹配 | 调整申请金额或配置流程 |
 */
public class ApprovalApiDocument {
    
    /**
     * ## 1. 提交审批申请
     * 
     * ### 接口信息
     * - **URL**: `POST /api/approval/submit`
     * - **功能**: 提交新的审批申请并创建审批实例
     * - **前置条件**: 业务数据准备完成，用户已登录
     * 
     * ### 请求参数
     * ```json
     * {
     *   "businessType": "EXPENSE_REIMBURSE",
     *   "businessId": "REIMBURSE_20240115001",
     *   "applicantId": 1001,
     *   "title": "差旅费用报销",
     *   "amount": 1500.00
     * }
     * ```
     * 
     * ### 响应示例
     * ```json
     * {
     *   "success": true,
     *   "data": {
     *     "id": 2001,
     *     "businessType": "EXPENSE_REIMBURSE",
     *     "businessId": "REIMBURSE_20240115001",
     *     "status": "APPROVING",
     *     "currentApproverId": 1002,
     *     "createTime": "2024-01-15T10:30:00"
     *   }
     * }
     * ```
     * 
     * ### 业务规则
     * 1. 根据业务类型和金额自动匹配审批流程配置
     * 2. 创建审批实例并初始化第一个审批节点
     * 3. 状态自动设置为APPROVING（审批中）
     * 4. 生成操作审计日志
     */
    
    /**
     * ## 2. 审批通过
     * 
     * ### 接口信息
     * - **URL**: `POST /api/approval/instances/{instanceId}/approve`
     * - **功能**: 审批人审批通过当前节点
     * - **前置条件**: 用户具有当前节点审批权限
     * 
     * ### 请求参数
     * ```json
     * {
     *   "approverId": 1002,
     *   "remarks": "同意报销"
     * }
     * ```
     * 
     * ### 状态转换逻辑
     * - 单节点审批：APPROVING → APPROVED
     * - 多节点审批：APPROVING → APPROVING（下一个节点）
     * - 无后续节点：APPROVING → APPROVED
     */
    
    /**
     * ## 3. 审批驳回
     * 
     * ### 接口信息
     * - **URL**: `POST /api/approval/instances/{instanceId}/reject`
     * - **功能**: 审批人驳回当前申请
     * - **状态转换**: APPROVING → REJECTED
     * 
     * ### 请求参数
     * ```json
     * {
     *   "approverId": 1002,
     *   "remarks": "报销明细不清晰"
     * }
     * ```
     */
    
    /**
     * ## 4. 取消审批
     * 
     * ### 接口信息
     * - **URL**: `POST /api/approval/instances/{instanceId}/cancel`
     * - **功能**: 申请人取消正在审批中的申请
     * - **状态转换**: APPROVING → CANCELED
     * - **前置条件**: 仅申请人可操作
     */
    
    /**
     * ## 5. 重新提交
     * 
     * ### 接口信息
     * - **URL**: `POST /api/approval/instances/{instanceId}/resubmit`
     * - **功能**: 申请人重新提交被驳回的申请
     * - **状态转换**: REJECTED → APPROVING
     */
    
    /**
     * ## 6. 查询审批实例
     * 
     * ### 接口信息
     * - **URL**: `GET /api/approval/instances/{instanceId}`
     * - **功能**: 获取审批实例详细信息
     * 
     * ### 响应结构
     * ```json
     * {
     *   "id": 2001,
     *   "businessType": "EXPENSE_REIMBURSE",
     *   "businessId": "REIMBURSE_001",
     *   "applicantId": 1001,
     *   "title": "差旅费用报销",
     *   "amount": 1500.00,
     *   "status": "APPROVING",
     *   "currentApproverId": 1002,
     *   "currentNodeName": "部门审批",
     *   "createTime": "2024-01-15T10:30:00",
     *   "updateTime": "2024-01-15T10:32:00"
     * }
     * ```
     */
    
    /**
     * ## 7. 查询审批历史
     * 
     * ### 接口信息
     * - **URL**: `GET /api/approval/instances/{instanceId}/history`
     * - **功能**: 获取审批流程操作历史
     * 
     * ### 响应示例
     * ```json
     * [
     *   {
     *     "id": 5001,
     *     "instanceId": 2001,
     *     "approverId": 1002,
     *     "action": "APPROVE",
     *     "remarks": "同意报销",
     *     "createTime": "2024-01-15T10:35:00"
     *   }
     * ]
     * ```
     */
    
    /**
     * ## 8. 查询流程配置
     * 
     * ### 接口信息
     * - **URL**: `GET /api/approval/flow-configs`
     * - **参数**: `businessType` - 业务类型
     * - **功能**: 查询指定业务类型的流程配置
     * 
     * ### 响应结构
     * ```json
     * [
     *   {
     *     "id": 1,
     *     "businessType": "EXPENSE_REIMBURSE",
     *     "flowName": "费用报销流程",
     *     "minAmount": 0,
     *     "maxAmount": 5000,
     *     "nodes": [
     *       {
     *         "nodeName": "部门审批",
     *         "approverType": "ROLE",
     *         "approverId": 2
     *       }
     *     ]
     *   }
     * ]
     * ```
     */
    
    /**
     * ## 状态机说明
     * 
     * ### 审批状态流转图
     * ```
     * 草稿(DRAFT) → 提交 → 审批中(APPROVING)
     *                   ↓
     *                驳回 → 已驳回(REJECTED)
     *                   ↓
     *                审批通过 → 已批准(APPROVED)
     *                   ↓
     *                取消 → 已取消(CANCELED)
     *                   ↓
     *                重新提交 → 审批中(APPROVING)
     * ```
     * 
     * ### 审批操作集
     * - SUBMIT: 提交申请
     * - APPROVE: 审批通过
     * - REJECT: 审批驳回
     * - CANCEL: 取消申请
     * - RESUBMIT: 重新提交
     * - TERMINATE: 终止流程
     * - COMPLETE: 完成审批
     */
    
    /**
     * ## 集成指导
     * 
     * ### 1. 业务系统集成步骤
     * 
     * #### 步骤1: 环境配置
     * 1. 添加审批服务依赖
     * 2. 配置数据库连接
     * 3. 初始化流程配置数据
     * 
     * #### 步骤2: 前端集成
     * 1. 集成审批提交组件
     * 2. 实现审批进度展示
     * 3. 添加审批操作按钮
     * 
     * #### 步骤3: 后端调用
     * 1. 注入ApprovalService或使用RestTemplate
     * 2. 调用审批接口处理业务审批
     * 3. 监听审批状态变更
     * 
     * ### 2. 回调机制
     * 
     * 审批服务支持状态变更回调：
     * ```java
     * @EventListener
     * public void handleApprovalStatusChange(ApprovalStatusChangeEvent event) {
     *     // 更新业务状态
     *     businessService.updateStatus(event.getBusinessId(), event.getNewStatus());
     * }
     * ```
     * 
     * ### 3. 权限控制
     * 
     * - 申请人权限: 查看、取消、重新提交
     * - 审批人权限: 查看、审批、驳回
     * - 管理员权限: 配置管理、流程监控
     */
    
    /**
     * ## 性能优化
     * 
     * ### 1. 缓存策略
     * - 流程配置缓存: 减少数据库查询
     * - 状态信息缓存: 快速获取当前状态
     * - 历史记录分页: 支持大数据量查询
     * 
     * ### 2. 查询优化
     * - 复合索引: businessType + businessId
     * - 分页查询: 支持大数据量分页
     * - 延迟加载: 关联对象按需加载
     * 
     * ### 3. 并发处理
     * - 乐观锁: 防止同时操作冲突
     * - 事务控制: 确保数据一致性
     * - 异步处理: 非关键操作异步执行
     */
    
    /**
     * ## 监控和日志
     * 
     * ### 监控指标
     * - 请求成功率: 接口调用成功率
     * - 处理耗时: 关键操作处理时间
     * - 错误率: 各类错误占比
     * 
     * ### 日志记录
     * - 操作审计: 记录关键业务操作
     * - 异常追踪: 详细错误堆栈
     * - 性能日志: 慢操作记录
     */
}

/**
 * ## 附录: 完整的状态转换表
 * 
 * | 当前状态 | 允许操作 | 目标状态 | 前置条件 |
 * |----------|----------|----------|----------|
 * | DRAFT    | SUBMIT   | APPROVING | 无 |
 * | APPROVING | APPROVE | APPROVED | 当前审批人 |
 * | APPROVING | REJECT | REJECTED | 当前审批人 |
 * | APPROVING | CANCEL | CANCELED | 申请人本人 |
 * | REJECTED | RESUBMIT | APPROVING | 申请人本人 |
 * | REJECTED | TERMINATE | TERMINATED | 管理员 |
 * | APPROVED | COMPLETE | COMPLETED | 系统自动 |
 * 
 * ## 最佳实践
 * 
 * 1. **前端集成**: 审批操作需同步更新本地状态
 * 2. **异常处理**: 捕获接口异常并提供用户提示
 * 3. **数据校验**: 前端和后端均需进行数据验证
 * 4. **权限控制**: 严格限制操作权限边界
 * 5. **日志记录**: 关键操作必须记录审计日志
 */