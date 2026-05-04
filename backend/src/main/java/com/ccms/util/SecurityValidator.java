package com.ccms.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * 安全检查工具类
 * 提供输入校验、SQL注入防护、XSS防护等安全检查功能
 */
@Component
public class SecurityValidator {
    private static final Logger log = LoggerFactory.getLogger(SecurityValidator.class);

    // SQL注入检测正则表达式
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(select|insert|update|delete|drop|create|alter|exec|union|and|or|xp_cmdshell)\\b)"
    );
    
    // XSS攻击检测正则表达式
    private static final Pattern XSS_PATTERN = Pattern.compile(
        "<script[^>]*(\\s|>)|" +
        "javascript:|" +
        "on(load|error|click|mouse|key)([^>=]|=\\s*[^>]*)" +
        "|\\b(expression|eval|alert|prompt|confirm)\\b"
    );
    
    // 路径遍历攻击检测
    private static final Pattern PATH_TRAVERSAL_PATTERN = Pattern.compile(
        "(\\.\\.[\\/\\]|\\/\\/|\\~\\~|file:|ftp:|http:|https:)"
    );

    /**
     * 检查SQL注入攻击
     */
    public boolean checkSqlInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * 检查XSS攻击
     */
    public boolean checkXssInjection(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return XSS_PATTERN.matcher(input).find();
    }

    /**
     * 检查路径遍历攻击
     */
    public boolean checkPathTraversal(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        return PATH_TRAVERSAL_PATTERN.matcher(input).find();
    }

    /**
     * 验证邮箱格式
     */
    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";
        return Pattern.matches(emailRegex, email);
    }

    /**
     * 验证手机号格式
     */
    public boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        String phoneRegex = "^1[3-9]\\d{9}$";
        return Pattern.matches(phoneRegex, phone);
    }

    /**
     * 验证身份证号格式
     */
    public boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        String idCardRegex = "(^\\d{15}$)|(^\\d{17}([0-9]|X|x)$)";
        return Pattern.matches(idCardRegex, idCard);
    }

    /**
     * 安全字符串转义
     */
    public String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * 校验输入长度限制
     */
    public boolean validateLength(String input, int minLength, int maxLength) {
        if (input == null) {
            return false;
        }
        int length = input.trim().length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 校验数值范围
     */
    public boolean validateNumberRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        return doubleValue >= minValue && doubleValue <= maxValue;
    }

    /**
     * 检查请求来源安全性
     */
    public boolean isSecureRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        
        // 检查请求头安全性
        String referer = request.getHeader("Referer");
        String origin = request.getHeader("Origin");
        
        // 验证Referer和Origin头
        if (referer != null && !referer.startsWith(request.getScheme() + "://" + request.getServerName())) {
            log.warn("可疑的Referer头: {}", referer);
            return false;
        }
        
        if (origin != null && !origin.startsWith(request.getScheme() + "://" + request.getServerName())) {
            log.warn("可疑的Origin头: {}", origin);
            return false;
        }
        
        return true;
    }

    /**
     * 生成安全令牌
     */
    public String generateSecureToken(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder token = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            token.append(chars.charAt(index));
        }
        
        return token.toString();
    }

    /**
     * 密码强度校验
     */
    public PasswordStrength checkPasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            return PasswordStrength.WEAK;
        }
        
        int score = 0;
        
        // 长度检查
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // 包含数字
        if (password.matches(".*\\d.*")) score++;
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) score++;
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) score++;
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        
        if (score >= 5) return PasswordStrength.STRONG;
        if (score >= 3) return PasswordStrength.MEDIUM;
        return PasswordStrength.WEAK;
    }

    /**
     * 密码强度枚举
     */
    public enum PasswordStrength {
        WEAK, MEDIUM, STRONG
    }

    /**
     * 记录安全事件
     */
    public void logSecurityEvent(String eventType, String description, HttpServletRequest request) {
        if (request != null) {
            String ip = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            log.warn("安全事件 - 类型: {}, 描述: {}, IP: {}, User-Agent: {}", 
                    eventType, description, ip, userAgent);
        } else {
            log.warn("安全事件 - 类型: {}, 描述: {}", eventType, description);
        }
    }

    /**
     * 获取客户端真实IP
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
}