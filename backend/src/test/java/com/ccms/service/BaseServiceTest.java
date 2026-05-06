package com.ccms.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

/**
 * 服务层测试基类
 * 所有服务层测试类都应继承此类
 */
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public abstract class BaseServiceTest {
    
    /**
     * 创建测试用的分页请求
     */
    protected org.springframework.data.domain.PageRequest createPageRequest(int page, int size) {
        return org.springframework.data.domain.PageRequest.of(page, size);
    }
    
    /**
     * 创建测试用的分页请求（带排序）
     */
    protected org.springframework.data.domain.PageRequest createPageRequest(int page, int size, org.springframework.data.domain.Sort sort) {
        return org.springframework.data.domain.PageRequest.of(page, size, sort);
    }
}
