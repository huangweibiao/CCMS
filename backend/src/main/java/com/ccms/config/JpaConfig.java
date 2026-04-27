package com.ccms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;

/**
 * JPA配置类
 * 
 * @author 系统生成
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.ccms.repository")
@EnableTransactionManagement
public class JpaConfig {

    /**
     * 审计功能配置 - 自动填充创建人、修改人等信息
     */
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            // 从SecurityContext中获取当前用户ID
            // 实际应用中需要从JWT token中解析用户ID
            // 这里先返回一个默认值，实际使用时需要实现具体的逻辑
            return Optional.of(1L);
        };
    }

    // 如果需要自定义JPA属性，可以在这里配置
    // 目前使用application.yml中的配置已经足够
}