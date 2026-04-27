package com.ccms;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;

/**
 * 基础测试类
 * 提供测试环境通用配置和工具方法
 */
@ActiveProfiles("test")
public abstract class BaseTest {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    @BeforeEach
    void setUp() {
        // 初始化Mockito注解
        MockitoAnnotations.openMocks(this);
        logger.info("{} 测试开始执行", getClass().getSimpleName());
    }
    
    /**
     * 创建测试用户数据
     */
    protected com.ccms.entity.User createTestUser() {
        com.ccms.entity.User user = new com.ccms.entity.User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTV4U4W"); // encoded "password"
        user.setRole("user");
        user.setEmail("test@example.com");
        user.setDepartment("测试部门");
        user.setPosition("测试职位");
        user.setStatus(1);
        return user;
    }
    
    /**
     * 创建测试费用申请数据
     */
    protected com.ccms.entity.ExpenseApply createTestExpenseApply() {
        com.ccms.entity.ExpenseApply apply = new com.ccms.entity.ExpenseApply();
        apply.setId(1L);
        apply.setUserId(1L);
        apply.setAmount(1000.0);
        apply.setReason("测试费用申请");
        apply.setStatus(1);
        return apply;
    }
    
    /**
     * 创建测试预算数据
     */
    protected com.ccms.entity.Budget createTestBudget() {
        com.ccms.entity.Budget budget = new com.ccms.entity.Budget();
        budget.setId(1L);
        budget.setDepartment("测试部门");
        budget.setYear(2024);
        budget.setMonth(4);
        budget.setTotalAmount(50000.0);
        budget.setUsedAmount(0.0);
        return budget;
    }
    
    /**
     * 创建默认的JWT token
     */
    protected String createTestToken() {
        return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MjQyNjIyfQ.test_signature";
    }
}