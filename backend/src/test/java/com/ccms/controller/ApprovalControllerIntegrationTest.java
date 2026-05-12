package com.ccms.controller;

import com.ccms.dto.ApprovalRequest;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalNode;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApproverTypeEnum;
import com.ccms.enums.BusinessTypeEnum;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalNodeRepository;
import com.ccms.repository.approval.ApprovalRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审批控制器集成测试
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ApprovalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApprovalInstanceRepository instanceRepository;

    @Autowired
    private ApprovalFlowConfigRepository configRepository;

    @Autowired
    private ApprovalNodeRepository nodeRepository;

    @Autowired
    private ApprovalRecordRepository recordRepository;

    private ApprovalFlowConfig testConfig;

    @BeforeEach
    void setUp() {
        // 清理测试数据
        recordRepository.deleteAll();
        instanceRepository.deleteAll();
        nodeRepository.deleteAll();
        configRepository.deleteAll();

        // 创建测试流程配置
        testConfig = new ApprovalFlowConfig();
        testConfig.setBusinessType("EXPENSE_REIMBURSE");
        testConfig.setFlowName("集成测试流程");
        testConfig.setDescription("用于集成测试的审批流程");
        testConfig.setMinAmount(new BigDecimal("0"));
        testConfig.setMaxAmount(new BigDecimal("10000"));
        testConfig.setEnableCondition(false);
        testConfig.setIsActive(true);
        testConfig.setVersion(1);
        testConfig = configRepository.save(testConfig);

        // 创建测试节点
        ApprovalNode node1 = new ApprovalNode();
        node1.setConfigId(testConfig.getId());
        node1.setNodeName("一级审批");
        node1.setNodeOrder(1);
        node1.setApproverType(ApproverTypeEnum.ROLE);
        node1.setApproverId(2L);
        node1.setIsRequired(true);
        node1 = nodeRepository.save(node1);

        ApprovalNode node2 = new ApprovalNode();
        node2.setConfigId(testConfig.getId());
        node2.setNodeName("二级审批");
        node2.setNodeOrder(2);
        node2.setApproverType(ApproverTypeEnum.ROLE);
        node2.setApproverId(3L);
        node2.setIsRequired(false);
        nodeRepository.save(node2);
    }

    @Test
    void testFullApprovalWorkflow() throws Exception {
        // 1. 提交审批
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("INTEGRATION_TEST_001");
        request.setApplicantId(1L);
        request.setTitle("集成测试审批流程");
        request.setAmount(new BigDecimal("1500.00"));

        MvcResult submitResult = mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVING"))
                .andReturn();

        String responseContent = submitResult.getResponse().getContentAsString();
        var responseMap = objectMapper.readValue(responseContent, java.util.Map.class);
        var dataMap = (java.util.Map<String, Object>) responseMap.get("data");
        Long instanceId = Long.valueOf(dataMap.get("id").toString());

        // 2. 查询审批实例
        mockMvc.perform(get("/api/approval/instances/" + instanceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(instanceId))
                .andExpect(jsonPath("$.data.status").value("APPROVING"));

        // 3. 一级审批人审批通过
        String approveBody = "{\"approverId\": 2, \"remarks\": \"一级审批通过\"}";
        
        mockMvc.perform(post("/api/approval/instances/" + instanceId + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .content(approveBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 4. 验证审批记录
        mockMvc.perform(get("/api/approval/instances/" + instanceId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2)); // 提交 + 审批

        // 5. 验证业务状态
        ApprovalInstance instance = instanceRepository.findById(instanceId).orElseThrow();
        assertEquals(ApprovalStatus.APPROVED, instance.getStatus());
    }

    @Test
    void testApprovalWithRejection() throws Exception {
        // 提交审批
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("INTEGRATION_TEST_002");
        request.setApplicantId(1L);
        request.setTitle("集成测试审批驳回");
        request.setAmount(new BigDecimal("800.00"));

        MvcResult submitResult = mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        var responseMap = objectMapper.readValue(submitResult.getResponse().getContentAsString(), java.util.Map.class);
        var dataMap = (java.util.Map<String, Object>) responseMap.get("data");
        Long instanceId = Long.valueOf(dataMap.get("id").toString());

        // 审批驳回
        String rejectBody = "{\"approverId\": 2, \"remarks\": \"申请理由不充分\"}";
        
        mockMvc.perform(post("/api/approval/instances/" + instanceId + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .content(rejectBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证状态
        ApprovalInstance instance = instanceRepository.findById(instanceId).orElseThrow();
        assertEquals(ApprovalStatus.REJECTED, instance.getStatus());
    }

    @Test
    void testCancellationWorkflow() throws Exception {
        // 创建草稿状态实例
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setBusinessId("INTEGRATION_TEST_003");
        request.setApplicantId(1L);
        request.setTitle("集成测试取消流程");
        request.setAmount(new BigDecimal("600.00"));

        MvcResult submitResult = mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        var responseMap = objectMapper.readValue(submitResult.getResponse().getContentAsString(), java.util.Map.class);
        var dataMap = (java.util.Map<String, Object>) responseMap.get("data");
        Long instanceId = Long.valueOf(dataMap.get("id").toString());

        // 取消审批
        String cancelBody = "{\"operatorId\": 1, \"reason\": \"需要修改申请内容\"}";
        
        mockMvc.perform(post("/api/approval/instances/" + instanceId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cancelBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 验证状态
        ApprovalInstance instance = instanceRepository.findById(instanceId).orElseThrow();
        assertEquals(ApprovalStatus.CANCELED, instance.getStatus());
    }

    @Test
    void testFlowConfigManagement() throws Exception {
        // 查询流程配置
        mockMvc.perform(get("/api/approval/flow-configs")
                .param("businessType", "EXPENSE_REIMBURSE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].flowName").value("集成测试流程"));
    }

    @Test
    void testValidationErrors() throws Exception {
        // 测试验证错误
        ApprovalRequest invalidRequest = new ApprovalRequest();
        invalidRequest.setBusinessType(null); // 必填字段为空
        invalidRequest.setBusinessId(""); // 空字符串
        invalidRequest.setApplicantId(null);
        invalidRequest.setTitle("");

        mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk()) // 统一错误处理返回200
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").exists());
    }

    @Test
    void testBusinessTypeValidation() throws Exception {
        // 测试不支持的验证错误
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessType(BusinessTypeEnum.valueOf("EXPENSE_APPLY")); // 不支持的枚举
        request.setBusinessId("TEST_004");
        request.setApplicantId(1L);
        request.setTitle("测试不支持的业务类型");

        mockMvc.perform(post("/api/approval/submit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testApprovalHistory() throws Exception {
        // 创建测试实例并添加审批记录
        ApprovalInstance instance = new ApprovalInstance();
        instance.setBusinessType("EXPENSE_REIMBURSE");
        instance.setBusinessId("HISTORY_TEST_001");
        instance.setApplicantId(1L);
        instance.setTitle("历史记录测试");
        instance.setStatus(ApprovalStatus.APPROVING);
        instance.setCurrentApproverId(2L);
        instance = instanceRepository.save(instance);

        // 添加审批记录
        ApprovalRecord record1 = new ApprovalRecord();
        record1.setInstanceId(instance.getId());
        record1.setApproverId(2L);
        record1.setAction(ApprovalAction.APPROVE);
        record1.setRemarks("测试审批记录");
        record1.setCreateTime(LocalDateTime.now());
        recordRepository.save(record1);

        // 查询审批历史
        mockMvc.perform(get("/api/approval/instances/" + instance.getId() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].action").value("APPROVE"))
                .andExpect(jsonPath("$.data[0].remarks").value("测试审批记录"));
    }

    @Test
    void testHealthCheck() throws Exception {
        // 测试健康检查接口
        mockMvc.perform(get("/api/approval/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Approval service is healthy"));
    }

    @Test
    void testInvalidInstanceId() throws Exception {
        // 测试不存在的实例ID
        mockMvc.perform(get("/api/approval/instances/999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
    }
}