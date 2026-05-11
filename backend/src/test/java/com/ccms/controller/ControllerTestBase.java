package com.ccms.controller;

import com.ccms.BaseTest;
import com.ccms.config.TestSecurityConfig;
import com.ccms.service.audit.AuditLogService;
import com.ccms.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Controller 测试基类
 * 提供通用的 MockMvc 操作方法和工具方法
 */
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
public abstract class ControllerTestBase extends BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected RedisUtil redisUtil;

    @MockBean
    protected AuditLogService auditLogService;

    /**
     * 执行 GET 请求
     */
    protected ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", createTestToken())
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 GET 请求（带查询参数）
     */
    protected ResultActions performGet(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(get(url, uriVars)
                .header("Authorization", createTestToken())
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 POST 请求
     */
    protected ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * 执行 POST 请求（无请求体）
     */
    protected ResultActions performPost(String url) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 POST 请求（带路径变量，无请求体）
     */
    protected ResultActions performPost(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(post(url, uriVars)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 PUT 请求
     */
    protected ResultActions performPut(String url, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    /**
     * 执行 PUT 请求（无请求体）
     */
    protected ResultActions performPut(String url) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 PUT 请求（带路径变量，无请求体）
     */
    protected ResultActions performPut(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(put(url, uriVars)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 DELETE 请求
     */
    protected ResultActions performDelete(String url) throws Exception {
        return mockMvc.perform(delete(url)
                .header("Authorization", createTestToken())
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 执行 DELETE 请求（带路径变量）
     */
    protected ResultActions performDelete(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(delete(url, uriVars)
                .header("Authorization", createTestToken())
                .accept(MediaType.APPLICATION_JSON));
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    protected String asJsonString(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 创建测试用的令牌
     */
    protected String createTestToken() {
        return "Bearer test-token-" + System.currentTimeMillis();
    }

    /**
     * 创建带有自定义令牌的请求头
     */
    protected String createCustomToken(String username, String role) {
        return "Bearer custom-token-" + username + "-" + role;
    }

    /**
     * 生成随机测试数据
     */
    protected String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            result.append(chars.charAt(index));
        }
        return result.toString();
    }

    /**
     * 生成随机测试邮箱
     */
    protected String generateRandomEmail() {
        return "test-" + generateRandomString(8) + "@example.com";
    }

    /**
     * 生成随机测试数字
     */
    protected long generateRandomId() {
        return System.currentTimeMillis();
    }

    /**
     * 模拟分页参数
     */
    protected java.util.Map<String, Object> createPageParams(int page, int size) {
        java.util.Map<String, Object> params = new java.util.HashMap<>();
        params.put("page", page);
        params.put("size", size);
        return params;
    }

    /**
     * 执行 POST 请求（带查询参数，无请求体）
     */
    protected ResultActions performPostWithQuery(String url, java.util.Map<String, String> params) throws Exception {
        var requestBuilder = post(url)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        
        if (params != null) {
            params.forEach(requestBuilder::param);
        }
        
        return mockMvc.perform(requestBuilder);
    }

    /**
     * 执行 PUT 请求（带查询参数，无请求体）
     */
    protected ResultActions performPutWithQuery(String url, java.util.Map<String, String> params) throws Exception {
        var requestBuilder = put(url)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        
        if (params != null) {
            params.forEach(requestBuilder::param);
        }
        
        return mockMvc.perform(requestBuilder);
    }
}
