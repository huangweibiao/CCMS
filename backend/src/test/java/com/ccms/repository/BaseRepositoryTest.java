package com.ccms.repository;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 存储层测试基类
 * 所有Repository测试类都应继承此类
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public abstract class BaseRepositoryTest {
    
    /**
     * 创建测试用的分页请求
     */
    protected org.springframework.data.domain.PageRequest createPageRequest(int page, int size) {
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
}
