package com.ccms.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * 性能监控配置
 */
@Configuration
public class PerformanceConfig {

    /**
     * 请求日志过滤器
     */
    @Bean
    public FilterRegistrationBean<CommonsRequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<CommonsRequestLoggingFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(true);
        
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }

    /**
     * 性能监控过滤器
     */
    @Bean
    public FilterRegistrationBean<PerformanceMonitorFilter> performanceMonitorFilter() {
        FilterRegistrationBean<PerformanceMonitorFilter> registrationBean = 
            new FilterRegistrationBean<>();
        
        PerformanceMonitorFilter filter = new PerformanceMonitorFilter();
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        
        return registrationBean;
    }
}

/**
 * 性能监控过滤器
 */
class PerformanceMonitorFilter implements jakarta.servlet.Filter {
    
    private static final org.slf4j.Logger logger = 
        org.slf4j.LoggerFactory.getLogger(PerformanceMonitorFilter.class);
    
    @Override
    public void doFilter(jakarta.servlet.ServletRequest request, 
                        jakarta.servlet.ServletResponse response, 
                        jakarta.servlet.FilterChain chain) 
            throws java.io.IOException, jakarta.servlet.ServletException {
        
        long startTime = System.currentTimeMillis();
        
        try {
            chain.doFilter(request, response);
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 记录慢查询
            if (duration > 1000) { // 超过1秒视为慢查询
                String requestUrl = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                logger.warn("慢请求检测 - URL: {}, 耗时: {}ms", requestUrl, duration);
            }
            
            // 记录性能数据（可以发送到监控系统）
            if (logger.isDebugEnabled()) {
                String requestUrl = ((jakarta.servlet.http.HttpServletRequest) request).getRequestURI();
                logger.debug("请求处理完成 - URL: {}, 耗时: {}ms", requestUrl, duration);
            }
        }
    }
}