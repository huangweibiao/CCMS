package com.ccms.config;

import com.ccms.service.impl.DefaultApprovalEngine;
import com.ccms.service.impl.ApprovalTimeoutProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class ApprovalEngineConfig {

    @Bean
    public DefaultApprovalEngine approvalEngine() {
        DefaultApprovalEngine engine = new DefaultApprovalEngine();
        
        // 配置引擎参数
        log.info("创建默认审批流程引擎");
        
        return engine;
    }
    
    @Bean
    public ApprovalTimeoutProcessor approvalTimeoutProcessor() {
        ApprovalTimeoutProcessor processor = new ApprovalTimeoutProcessor();
        
        // 配置超时处理参数
        processor.setDefaultTimeoutHours(24);  // 默认24小时超时
        processor.setMonitoringIntervalMinutes(30);  // 每30分钟检查一次
        
        return processor;
    }
    
    /**
     * 引擎生命周期管理
     */
    @Configuration
    public static class EngineLifecycleConfig {
        
        @Bean(initMethod = "initialize")
        public DefaultApprovalEngine initializedEngine() {
            return new DefaultApprovalEngine();
        }
    }
    
    /**
     * 超时处理器生命周期管理
     */
    @Configuration
    public static class TimeoutProcessorLifecycleConfig {
        
        @Bean(initMethod = "startTimeoutProcessing")
        public ApprovalTimeoutProcessor startedTimeoutProcessor() {
            return new ApprovalTimeoutProcessor();
        }
    }
}