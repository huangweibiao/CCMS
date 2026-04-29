package com.ccms.aspect;

import com.ccms.service.SysOperLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志AOP切面
 * 自动记录Controller方法的操作日志
 */
@Aspect
@Component
public class OperLogAspect {

    private final SysOperLogService operLogService;

    public OperLogAspect(SysOperLogService operLogService) {
        this.operLogService = operLogService;
    }

    /**
     * 环绕通知，自动记录操作日志
     */
    @Around("@annotation(operLogAnnotation) || @within(operLogAnnotation)")
    public Object around(ProceedingJoinPoint joinPoint, OperLog operLogAnnotation) throws Throwable {
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 确定实际的OperLog注解
        OperLog operLog = method.getAnnotation(OperLog.class);
        if (operLog == null) {
            // 如果没有方法级别的注解，使用类级别的注解
            operLog = method.getDeclaringClass().getAnnotation(OperLog.class);
        }
        
        if (operLog == null) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;
        
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            try {
                long costTime = System.currentTimeMillis() - startTime;
                // 记录操作日志
                recordOperLog(joinPoint, operLog, result, exception, costTime);
            } catch (Exception e) {
                // 记录操作日志出错不应该影响业务逻辑
                System.err.println("记录操作日志失败: " + e.getMessage());
            }
        }
    }

    /**
     * 记录操作日志
     */
    private void recordOperLog(ProceedingJoinPoint joinPoint, OperLog operLog, 
                              Object result, Exception exception, long costTime) {
        SysOperLogService.OperLogInfo logInfo = new SysOperLogService.OperLogInfo();
        
        // 设置基础信息
        logInfo.setTitle(operLog.title());
        logInfo.setBusinessType(operLog.businessType().getValue());
        
        // 设置操作人员信息（需要从SecurityContext中获取）
        setOperatorInfo(logInfo);
        
        // 设置请求信息
        setRequestInfo(logInfo, joinPoint);
        
        // 设置执行结果
        logInfo.setStatus(exception == null ? 0 : 1);
        logInfo.setErrorMsg(exception != null ? exception.getMessage() : null);
        logInfo.setCostTime(costTime);
        
        // 记录操作日志
        operLogService.logOperation(logInfo);
    }

    /**
     * 设置操作人员信息
     */
    private void setOperatorInfo(SysOperLogService.OperLogInfo logInfo) {
        // 从SecurityContext中获取当前用户信息
        // 这里需要根据实际的安全框架实现
        logInfo.setOperUserId("system"); // 默认值
        logInfo.setOperUserName("系统用户"); // 默认值
        logInfo.setOperatorType(0); // 后台用户
        
        // TODO: 需要集成SecurityContextHolder
        // Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // if (authentication != null && authentication.isAuthenticated()) {
        //     Object principal = authentication.getPrincipal();
        //     if (principal instanceof UserDetails) {
        //         // 设置用户信息
        //     }
        // }
    }

    /**
     * 设置请求信息
     */
    private void setRequestInfo(SysOperLogService.OperLogInfo logInfo, ProceedingJoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            
            // 设置请求信息
            logInfo.setOperUrl(request.getRequestURI());
            logInfo.setMethod(request.getMethod());
            logInfo.setOperIp(getClientIp(request));
            logInfo.setUserAgent(request.getHeader("User-Agent"));
            
            // 设置方法信息
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            logInfo.setMethod(signature.getMethod().getName());
            logInfo.setRequestMethod(request.getMethod());
            
            // TODO: 可以设置请求参数（需要处理敏感信息）
            // logInfo.setOperParam(getRequestParams(request, joinPoint));
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 对于多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}