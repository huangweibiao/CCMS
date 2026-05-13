package com.ccms.config;

import com.ccms.service.monitor.PerformanceMonitor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 性能监控切面
 */
@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    @Autowired
    private PerformanceMonitor performanceMonitor;

    /**
     * 监控Service层方法（排除性能监控服务自身）
     */
    @Pointcut("execution(* com.ccms.service..*.*(..)) && !execution(* com.ccms.service.monitor.PerformanceMonitor.*(..))")
    public void serviceMethods() {}

    /**
     * 监控Controller层方法
     */
    @Pointcut("execution(* com.ccms.controller..*.*(..))")
    public void controllerMethods() {}

    /**
     * 监控Repository层方法
     */
    @Pointcut("execution(* com.ccms.repository..*.*(..))")
    public void repositoryMethods() {}

    /**
     * 统一切面处理
     */
    @Around("serviceMethods() || controllerMethods() || repositoryMethods()")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = getMethodName(joinPoint);
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String fullMethodName = className + "." + methodName;

        // 记录开始时间
        performanceMonitor.startMethod(fullMethodName);
        long startTime = System.currentTimeMillis();

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();
            return result;
        } finally {
            // 记录结束时间
            long duration = System.currentTimeMillis() - startTime;
            performanceMonitor.endMethod(fullMethodName);

            // 如果执行时间较长，记录警告日志
            if (duration > 1000) { // 超过1秒
                logger.warn("方法执行较慢: {} - {}ms", fullMethodName, duration);
            } else if (duration > 5000) { // 超过5秒
                logger.error("方法执行非常慢: {} - {}ms", fullMethodName, duration);
            }

            // 记录数据库查询性能（如果是Repository方法）
            if (isRepositoryMethod(joinPoint)) {
                performanceMonitor.recordQuery(fullMethodName, duration);
            }
        }
    }

    /**
     * 获取方法名
     */
    private String getMethodName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod().getName();
    }

    /**
     * 判断是否是Repository方法
     */
    private boolean isRepositoryMethod(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        return className.contains("repository") || className.contains("Repository");
    }
}