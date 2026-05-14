package com.ccms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证入口点 - 处理认证失败
 * 
 * @author 系统生成
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        
        String requestUri = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        
        // 检查是否为API请求（请求路径以/api开头或者请求头包含application/json）
        boolean isApiRequest = requestUri.startsWith("/api/") || 
                              (acceptHeader != null && acceptHeader.contains("application/json"));
        
        // 如果是API请求，返回JSON格式错误信息
        if (isApiRequest) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> result = new HashMap<>();
            result.put("code", 401);
            result.put("message", "认证失败，请重新登录");
            result.put("path", request.getServletPath());

            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(response.getOutputStream(), result);
        } else {
            // 对于页面请求，重定向到登录页面
            // 检查当前请求是否为根路径或需要重定向的页面
            if (requestUri.equals("/") || 
                requestUri.equals("/index.html") || 
                requestUri.startsWith("/auth/")) {
                // 如果是SPA相关页面，返回首页让前端路由处理
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/html;charset=UTF-8");
                response.sendRedirect("/auth/login");
            } else {
                // 对于其他页面请求，重定向到登录页面
                response.setStatus(HttpServletResponse.SC_FOUND);
                response.sendRedirect("/auth/login");
            }
        }
    }
}