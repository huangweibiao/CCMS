package com.ccms.aspect;

import com.ccms.service.ApprovalAuditHelperService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 审批审计切面
 * 自动拦截审批相关操作并记录审计日志
 */
@Aspect
@Component
public class ApprovalAuditAspect {

    @Autowired
    private ApprovalAuditHelperService approvalAuditService;

    /**
     * 拦截审批流程配置相关操作
     */
    @Around("execution(* com.ccms.service.ApprovalFlowConfigurationService.*(..))")
    public Object auditFlowConfigurationOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditApprovalOperation(joinPoint, "ApprovalFlowConfig", "流程配置管理");
    }

    /**
     * 拦截审批实例相关操作
     */
    @Around("execution(* com.ccms.service.ApprovalInstanceService.*(..))")
    public Object auditInstanceOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditApprovalOperation(joinPoint, "ApprovalInstance", "审批实例管理");
    }

    /**
     * 拦截审批节点相关操作
     */
    @Around("execution(* com.ccms.service.ApprovalNodeService.*(..))")
    public Object auditNodeOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditApprovalOperation(joinPoint, "ApprovalNode", "审批节点管理");
    }

    /**
     * 拦截审批记录相关操作
     */
    @Around("execution(* com.ccms.service.ApprovalRecordService.*(..))")
    public Object auditRecordOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditApprovalOperation(joinPoint, "ApprovalRecord", "审批记录管理");
    }

    /**
     * 拦截费用报销服务相关操作
     */
    @Around("execution(* com.ccms.service.ExpenseReimburseService.*(..))")
    public Object auditExpenseReimburseOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditApprovalOperation(joinPoint, "ExpenseReimburse", "费用报销管理");
    }

    /**
     * 拦截借款服务相关操作
     */
    @Around("execution(* com.ccms.service.LoanService.*(..))")
    public Object auditLoanOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        return auditApprovalOperation(joinPoint, "Loan", "借款管理");
    }

    /**
     * 通用的审批操作审计逻辑
     */
    private Object auditApprovalOperation(ProceedingJoinPoint joinPoint, 
                                        String targetEntity, String businessType) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        
        // 获取操作类型
        String operationType = determineOperationType(methodName);
        
        // 获取目标ID
        Long targetId = extractTargetId(joinPoint.getArgs(), method);
        
        // 获取用户信息（从安全上下文或方法参数中获取）
        Map<String, Object> userInfo = getUserInfo();
        Long userId = (Long) userInfo.get("userId");
        String userName = (String) userInfo.get("userName");
        
        // 记录操作开始前的数据状态（如果有）
        Object beforeData = extractBeforeData(methodName, joinPoint.getArgs(), targetId);
        
        // 记录操作开始时间
        long startTime = System.currentTimeMillis();
        LocalDateTime operationStart = LocalDateTime.now();
        
        // 准备操作详情
        Map<String, Object> operationDetails = new HashMap<>();
        operationDetails.put("method", methodName);
        operationDetails.put("className", joinPoint.getTarget().getClass().getSimpleName());
        operationDetails.put("startTime", operationStart);
        
        try {
            // 执行原方法
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            
            // 记录操作成功
            operationDetails.put("endTime", LocalDateTime.now());
            operationDetails.put("executionTime", endTime - startTime);
            operationDetails.put("result", "SUCCESS");
            
            // 如果有返回值，提取相关信息
            Object afterData = extractAfterData(methodName, result);
            
            // 记录审计日志
            if (methodName.startsWith("update") || methodName.startsWith("create")) {
                approvalAuditService.logDataChangeOperation(
                        operationType, targetEntity, targetId, 
                        userId, userName, buildOperationDescription(methodName, operationDetails),
                        beforeData, afterData, operationDetails);
            } else {
                approvalAuditService.logApprovalOperation(
                        operationType, targetEntity, targetId,
                        userId, userName, buildOperationDescription(methodName, operationDetails),
                        operationDetails);
            }
            
            return result;
            
        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            
            // 记录操作失败
            operationDetails.put("endTime", LocalDateTime.now());
            operationDetails.put("executionTime", endTime - startTime);
            operationDetails.put("result", "FAILED");
            operationDetails.put("error", e.getMessage());
            
            // 记录失败审计日志
            approvalAuditService.logFailedOperation(
                    operationType, targetEntity, targetId,
                    userId, userName, buildOperationDescription(methodName, operationDetails),
                    e.getMessage(), operationDetails);
            
            throw e;
        }
    }

    /**
     * 根据方法名确定操作类型
     */
    private String determineOperationType(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("modify")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        } else if (methodName.startsWith("approve")) {
            return "APPROVE";
        } else if (methodName.startsWith("reject") || methodName.startsWith("deny")) {
            return "REJECT";
        } else if (methodName.startsWith("query") || methodName.startsWith("get") || 
                   methodName.startsWith("find") || methodName.startsWith("search")) {
            return "QUERY";
        } else if (methodName.startsWith("submit")) {
            return "SUBMIT";
        } else if (methodName.startsWith("cancel")) {
            return "CANCEL";
        } else {
            return "OTHER";
        }
    }

    /**
     * 从方法参数中提取目标ID
     */
    private Long extractTargetId(Object[] args, Method method) {
        // 查找包含ID的参数
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof Long) {
                return (Long) arg;
            } else if (arg != null && arg.getClass().getSimpleName().endsWith("DTO")) {
                // 尝试从DTO中获取ID
                try {
                    java.lang.reflect.Field idField = arg.getClass().getDeclaredField("id");
                    idField.setAccessible(true);
                    Object idValue = idField.get(arg);
                    if (idValue instanceof Long) {
                        return (Long) idValue;
                    }
                } catch (Exception e) {
                    // 忽略，尝试其他方式
                }
            }
        }
        
        // 从方法名中提取（例如：getById(123L)）
        String methodName = method.getName();
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        
        return null;
    }

    /**
     * 获取当前用户信息
     */
    private Map<String, Object> getUserInfo() {
        Map<String, Object> userInfo = new HashMap<>();
        
        try {
            // 实际实现中应该从SecurityContext中获取当前用户
            // 这里使用默认值
            userInfo.put("userId", 1L); // 默认用户ID
            userInfo.put("userName", "system"); // 默认用户名
            
            // 可以添加更多用户信息
            userInfo.put("department", "IT");
            userInfo.put("role", "ADMIN");
            
        } catch (Exception e) {
            // 如果无法获取用户信息，使用默认值
            userInfo.put("userId", -1L);
            userInfo.put("userName", "unknown");
        }
        
        return userInfo;
    }

    /**
     * 提取操作前的数据状态
     */
    private Object extractBeforeData(String methodName, Object[] args, Long targetId) {
        if (targetId == null || !(methodName.startsWith("update") || methodName.startsWith("delete"))) {
            return null;
        }
        
        // 实际实现中应该查询数据库获取当前数据状态
        // 这里返回null表示不记录数据变更详情
        return null;
    }

    /**
     * 提取操作后的数据状态
     */
    private Object extractAfterData(String methodName, Object result) {
        if (methodName.startsWith("create") || methodName.startsWith("update")) {
            return result; // 返回创建或更新后的对象
        }
        return null;
    }

    /**
     * 构建操作描述
     */
    private String buildOperationDescription(String methodName, Map<String, Object> details) {
        StringBuilder description = new StringBuilder();
        
        switch (determineOperationType(methodName)) {
            case "CREATE":
                description.append("创建审批");
                break;
            case "UPDATE":
                description.append("更新审批");
                break;
            case "DELETE":
                description.append("删除审批");
                break;
            case "APPROVE":
                description.append("审批通过");
                break;
            case "REJECT":
                description.append("审批拒绝");
                break;
            case "SUBMIT":
                description.append("提交审批");
                break;
            case "CANCEL":
                description.append("取消审批");
                break;
            case "QUERY":
                description.append("查询审批");
                break;
            default:
                description.append("执行操作: ").append(methodName);
        }
        
        Long executionTime = (Long) details.get("executionTime");
        if (executionTime != null) {
            description.append(" (耗时: ").append(executionTime).append("ms)");
        }
        
        return description.toString();
    }
}