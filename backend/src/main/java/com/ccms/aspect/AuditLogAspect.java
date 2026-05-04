package com.ccms.aspect;

import com.ccms.service.audit.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 审计日志切面
 * 自动记录Spring MVC控制器方法的调用和执行情况
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private final AuditLogService auditLogService;

    public AuditLogAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /**
     * 记录Controller层方法调用的审计日志
     */
    @Around("execution(* com.ccms.controller..*.*(..)) && " +
           "@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
           "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
           "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
           "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
           "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
           "@annotation(org.springframework.web.bind.annotation.PatchMapping)")
    public Object auditControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录成功日志
            logAudit(joinPoint, startTime, executionTime, true, null);
            
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录失败日志
            logAudit(joinPoint, startTime, executionTime, false, errorMessage);
            
            throw e;
        }
    }

    /**
     * 记录关键业务操作的审计日志
     */
    @Around("execution(* com.ccms.service..*.*(..)) && " +
           "@annotation(com.ccms.annotation.AuditLog)")
    public Object auditServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 记录业务操作日志
            logBusinessAudit(joinPoint, startTime, executionTime, true, null);
            
            return result;
        } catch (Exception e) {
            success = false;
            errorMessage = e.getMessage();
            long executionTime = System.currentTimeMillis() - startTime;
            
            logBusinessAudit(joinPoint, startTime, executionTime, false, errorMessage);
            
            throw e;
        }
    }

    /**
     * 记录Controller层审计日志
     */
    private void logAudit(ProceedingJoinPoint joinPoint, long startTime, 
                         long executionTime, boolean success, String errorMessage) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            // 获取请求信息
            ServletRequestAttributes attributes = (ServletRequestAttributes) 
                RequestContextHolder.getRequestAttributes();
            String requestMethod = "";
            String requestUrl = "";
            String userIp = "";
            
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                requestMethod = request.getMethod();
                requestUrl = request.getRequestURI();
                userIp = getClientIpAddress(request);
            }
            
            // 获取用户信息
            String username = "anonymous";
            Long userId = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                username = authentication.getName();
                // 这里可以根据实际的用户信息获取userId
                // userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            }
            
            // 构建请求参数
            String requestParams = Arrays.stream(joinPoint.getArgs())
                    .map(arg -> arg != null ? arg.toString() : "null")
                    .collect(Collectors.joining(", ", "[", "]"));
            
            // 记录审计日志
            auditLogService.logOperation(
                    "Controller",
                    className + "." + methodName,
                    buildOperationDescription(method, success, errorMessage),
                    userId,
                    username,
                    userIp,
                    success,
                    requestMethod,
                    requestUrl,
                    requestParams
            );
            
        } catch (Exception e) {
            log.error("记录审计日志失败", e);
        }
    }

    /**
     * 记录业务操作审计日志
     */
    private void logBusinessAudit(ProceedingJoinPoint joinPoint, long startTime,
                                long executionTime, boolean success, String errorMessage) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            String methodName = method.getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            
            // 获取用户信息
            String username = "system";
            Long userId = null;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getPrincipal())) {
                username = authentication.getName();
            }
            
            // 获取AOP注解信息
            com.ccms.annotation.AuditLog auditAnnotation = method.getAnnotation(
                com.ccms.annotation.AuditLog.class);
            String module = auditAnnotation != null ? auditAnnotation.module() : "Service";
            String operation = auditAnnotation != null ? auditAnnotation.operation() : methodName;
            String description = auditAnnotation != null ? auditAnnotation.description() : 
                buildServiceOperationDescription(method, success, errorMessage);
            
            // 记录业务操作日志
            auditLogService.logOperation(
                    module,
                    operation,
                    description,
                    userId,
                    username,
                    "127.0.0.1", // 内部服务调用IP
                    success
            );
            
        } catch (Exception e) {
            log.error("记录业务操作审计日志失败", e);
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP", 
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        
        return request.getRemoteAddr();
    }

    /**
     * 构建操作描述
     */
    private String buildOperationDescription(Method method, boolean success, String errorMessage) {
        String baseDescription = method.getName() + "操作";
        if (success) {
            return baseDescription + "成功";
        } else {
            return baseDescription + "失败: " + errorMessage;
        }
    }

    /**
     * 构建服务操作描述
     */
    private String buildServiceOperationDescription(Method method, boolean success, String errorMessage) {
        String baseDescription = method.getName() + "业务操作";
        if (success) {
            return baseDescription + "执行成功";
        } else {
            return baseDescription + "执行失败: " + errorMessage;
        }
    }
}