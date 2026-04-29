package com.ccms.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化配置（简化版）
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {
    
    @Override
    public void run(String... args) throws Exception {
        // 此处暂时不初始化数据，避免编译错误
        // 实际部署时可根据需要添加数据初始化逻辑
    }
}