package com.ccms.controller;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审批控制器单元测试
 */
@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApprovalService approvalService;

    @Test
    void testSubmitApproval() throws Exception {
        // 准备测试数据
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("TEST_001");
        request.setApplicantId(1L);
        request.setTitle("测试审批提交");
        request.setAmount(new BigDecimal("1000.00"));

        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(1L);
        instance.setBusinessType("EXPENSE_REIMBURSE");
        instance.setBusinessId("TEST_001");
        instance.setStatus(ApprovalStatus.DRAFT);

        // 模拟服务调用
        when(approvalService.createApprovalInstance(
                any(BusinessTypeEnum.class), 
                anyString(), 
                anyLong(), 
                anyString(),
                any(BigDecimal.class),
                anyLong()
        )).thenReturn(instance);

        when(approvalService.submitApproval(anyLong())).thenReturn(instance);

        // 执行请求并验证
        mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L));
    }

    @Test
    void testGetApprovalInstance() throws Exception {
        // 准备测试数据
        ApprovalInstance instance = new ApprovalInstance();
        instance.setId(1L);
        instance.setBusinessType("EXPENSE_REIMBURSE");
        instance.setBusinessId("TEST_002");
        instance.setStatus(ApprovalStatus.APPROVING);

        // 模拟服务调用
        when(approvalService.findById(1L)).thenReturn(instance);

        // 执行请求并验证
        mockMvc.perform(get("/api/approval/instances/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("APPROVING"));
    }

    @Test
    void testGetApprovalInstance_NotFound() throws Exception {
        // 模拟服务返回空
        when(approvalService.findById(999L)).thenReturn(null);

        // 执行请求并验证
        mockMvc.perform(get("/api/approval/instances/999"))
                .andExpect(status().isOk()) // 注意：这里返回200，而不是404
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetApprovalHistory() throws Exception {
        // 准备测试数据
        List<Object> history = Arrays.asList(
                new Object(){
                    public Long getId() { return 1L; }
                    public String getAction() { return "SUBMIT"; }
                },
                new Object(){
                    public Long getId() { return 2L; }
                    public String getAction() { return "APPROVE"; }
                }
        );

        // 模拟服务调用
        when(approvalService.getApprovalHistory(1L)).thenReturn(history);

        // 执行请求并验证
        mockMvc.perform(get("/api/approval/instances/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void testGetFlowConfigs() throws Exception {
        // 准备测试数据
        ApprovalFlowConfig config = new ApprovalFlowConfig();
        config.setId(1L);
        config.setBusinessType("EXPENSE_REIMBURSE");
        config.setFlowName("费用报销流程");

        List<ApprovalFlowConfig> configs = Arrays.asList(config);

        // 模拟服务调用
        when(approvalService.getFlowConfigs(anyString())).thenReturn(configs);

        // 执行请求并验证
        mockMvc.perform(get("/api/approval/flow-configs")
                .param("businessType", "EXPENSE_REIMBURSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].flowName").value("费用报销流程"));
    }

    @Test
    void testApproveAction() throws Exception {
        // 准备请求体
        String requestBody = "{\"approverId\": 2, \"remarks\": \"同意审批\"}";

        ApprovalInstance updatedInstance = new ApprovalInstance();
        updatedInstance.setId(1L);
        updatedInstance.setStatus(ApprovalStatus.APPROVED);

        // 模拟服务调用
        when(approvalService.performApprovalAction(anyLong(), anyLong(), any(), anyString()))
                .thenReturn(null); // 正常情况下返回Record，这里简化处理
        when(approvalService.findById(1L)).thenReturn(updatedInstance);

        // 执行请求并验证
        mockMvc.perform(post("/api/approval/instances/1/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testCancelApproval() throws Exception {
        // 准备请求体
        String requestBody = "{\"operatorId\": 1, \"reason\": \"不想申请了\"}";

        ApprovalInstance cancelledInstance = new ApprovalInstance();
        cancelledInstance.setId(1L);
        cancelledInstance.setStatus(ApprovalStatus.CANCELED);

        // 模拟服务调用
        when(approvalService.cancelApproval(anyLong(), anyLong(), anyString()))
                .thenReturn(cancelledInstance);

        // 执行请求并验证
        mockMvc.perform(post("/api/approval/instances/1/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELED"));
    }

    @Test
    void testValidateApprovalRequest_MissingFields() throws Exception {
        // 准备无效的请求（缺失必填字段）
        ApprovalRequest invalidRequest = new ApprovalRequest();
        invalidRequest.setBusinessType(null); // 业务类型为空
        invalidRequest.setBusinessId("TEST");
        invalidRequest.setApplicantId(1L);

        // 执行请求并验证
        mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk()); // 控制器内部捕获异常返回统一响应
    }

    @Test
    void testHealthCheck() throws Exception {
        // 测试健康检查接口
        mockMvc.perform(get("/api/approval/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Approval service is healthy"));
    }
}