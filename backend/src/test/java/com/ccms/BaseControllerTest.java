package com.ccms;

import com.ccms.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 控制器测试基类
 * 提供Web层测试的通用方法和配置
 */
@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTest extends BaseTest {
    
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
    
    /**
     * 执行GET请求并验证成功响应
     */
    protected ResultActions performGetRequest(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(get(url, uriVars)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    /**
     * 执行GET请求并验证具体响应内容
     */
    protected <T> T performGetRequestAndReturn(String url, Class<T> responseType, Object... uriVars) throws Exception {
        MvcResult result = performGetRequest(url, uriVars).andReturn();
        String content = result.getResponse().getContentAsString();
        return objectMapper.readValue(content, responseType);
    }
    
    /**
     * 执行POST请求并验证成功响应
     */
    protected ResultActions performPostRequest(String url, Object requestBody, Object... uriVars) throws Exception {
        return mockMvc.perform(post(url, uriVars)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    /**
     * 执行POST请求并解析响应
     */
    protected <T> T performPostRequestAndReturn(String url, Object requestBody, Class<T> responseType, Object... uriVars) throws Exception {
        MvcResult result = performPostRequest(url, requestBody, uriVars).andReturn();
        String content = result.getResponse().getContentAsString();
        return objectMapper.readValue(content, responseType);
    }
    
    /**
     * 执行PUT请求并验证成功响应
     */
    protected ResultActions performPutRequest(String url, Object requestBody, Object... uriVars) throws Exception {
        return mockMvc.perform(put(url, uriVars)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    
    /**
     * 执行DELETE请求
     */
    protected ResultActions performDeleteRequest(String url, Object... uriVars) throws Exception {
        return mockMvc.perform(delete(url, uriVars)
                .header("Authorization", createTestToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
    
    /**
     * 验证API响应成功
     */
    protected void verifyApiResponseSuccess(ApiResponse<?> response) {
        assert response.getCode() == 200 : "响应状态码应为200";
        assert response.getSuccess() : "响应success应为true";
    }
    
    /**
     * 验证API响应失败
     */
    protected void verifyApiResponseFailure(ApiResponse<?> response, int expectedCode) {
        assert response.getCode() == expectedCode : "响应状态码应为" + expectedCode;
        assert !response.getSuccess() : "响应success应为false";
    }
}