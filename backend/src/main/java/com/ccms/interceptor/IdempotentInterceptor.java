package com.ccms.interceptor;

import com.ccms.annotation.Idempotent;
import com.ccms.service.audit.AuditLogService;
import com.ccms.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 防重复提交拦截器
 * 基于Redis实现幂等性控制，防止重复请求
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotentInterceptor implements HandlerInterceptor {

    private final RedisUtil redisUtil;
    private final AuditLogService auditLogService;

    /**
     * 令牌前缀
     */
    private static final String IDEMPOTENT_PREFIX = "idempotent:";
    
    /**
     * 默认令牌过期时间（5分钟）
     */
    private static final long DEFAULT_EXPIRE_TIME = 5 * 60;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只拦截方法级别的请求
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // 检查方法是否有幂等性注解
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        if (idempotent == null) {
            return true;
        }

        // 获取幂等性令牌
        String token = request.getHeader("Idempotent-Token");
        if (token == null || token.trim().isEmpty()) {
            return handleError(response, "幂等性令牌不能为空");
        }

        // 构建Redis键
        String redisKey = IDEMPOTENT_PREFIX + token;
        
        // 检查令牌是否已使用
        Boolean result = redisUtil.setIfAbsent(redisKey, "1", 
                idempotent.expireTime() > 0 ? idempotent.expireTime() : DEFAULT_EXPIRE_TIME, 
                TimeUnit.SECONDS);
        
        if (result == null || !result) {
            // 令牌已使用，记录安全事件
            auditLogService.logOperation("Security", "REPEAT_SUBMIT", 
                    "检测到重复提交请求: " + request.getRequestURI(),
                    getCurrentUserId(), getCurrentUsername(), getClientIp(request), false);
            
            return handleError(response, "请勿重复提交");
        }

        return true;
    }

    /**
     * 处理错误响应
     */
    private boolean handleError(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                String.format("{\"code\":400,\"message\":\"%s\",\"success\":false}", message)
        );
        return false;
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
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
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        // TODO: 从SecurityContext获取当前用户ID
        return null;
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        // TODO: 从SecurityContext获取当前用户名
        return "anonymous";
    }
}

}