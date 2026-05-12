package com.ccms.config;

import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalNodeRepository;
import com.ccms.service.impl.DefaultApprovalEngine;
import com.ccms.service.impl.ApprovalTimeoutProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApprovalEngineConfig {

    @Autowired
    private ApprovalInstanceRepository approvalInstanceRepository;
    
    @Autowired
    private ApprovalNodeRepository approvalNodeRepository;

    private static final Logger log = LoggerFactory.getLogger(ApprovalEngineConfig.class);

    @Bean
    public DefaultApprovalEngine approvalEngine() {
        DefaultApprovalEngine engine = new DefaultApprovalEngine(approvalInstanceRepository, approvalNodeRepository);
        
        // 配置引擎参数
        log.info("创建默认审批流程引擎");
        
        return engine;
    }
    
    @Bean
    public ApprovalTimeoutProcessor approvalTimeoutProcessor() {
        ApprovalTimeoutProcessor processor = new ApprovalTimeoutProcessor(approvalInstanceRepository);
        
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
        
        @Autowired
        private ApprovalInstanceRepository approvalInstanceRepository;
        
        @Autowired
        private ApprovalNodeRepository approvalNodeRepository;
        
        @Bean(initMethod = "initialize")
        public DefaultApprovalEngine initializedEngine() {
            return new DefaultApprovalEngine(approvalInstanceRepository, approvalNodeRepository);
        }
    }
    
    /**
     * 超时处理器生命周期管理
     */
    @Configuration
    public static class TimeoutProcessorLifecycleConfig {
        
        @Autowired
        private ApprovalInstanceRepository approvalInstanceRepository;
        
        @Bean(initMethod = "startTimeoutProcessing")
        public ApprovalTimeoutProcessor startedTimeoutProcessor() {
            return new ApprovalTimeoutProcessor(approvalInstanceRepository);
        }
    }
}