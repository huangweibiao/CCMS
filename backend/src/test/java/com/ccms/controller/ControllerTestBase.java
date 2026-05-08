package com.ccms.controller;

import com.ccms.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Controller 测试基类
 * 提供通用的 MockMvc 操作方法和工具方法
 */
@AutoConfigureMockMvc(addFilters = false)
public abstract class ControllerTestBase extends BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

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
}
