package com.ccms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 企业级费控管理系统启动类
 * 
 * @author CCMS Team
 * @version 1.0.0
 * @since 2025-01-01
 */
@SpringBootApplication
public class CcmsApplication {

    /**
     * 应用启动入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CcmsApplication.class, args);
    }
}