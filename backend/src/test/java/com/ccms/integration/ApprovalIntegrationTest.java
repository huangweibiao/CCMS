package com.ccms.integration;

import com.ccms.controller.ApprovalController;
import com.ccms.dto.approval.*;
import com.ccms.entity.approval.*;
import com.ccms.entity.user.User;
import com.ccms.enums.approval.ApprovalAction;
import com.ccms.enums.approval.ApprovalStatus;
import com.ccms.enums.approval.ApprovalType;
import com.ccms.repository.approval.*;
import com.ccms.service.ApprovalService;
import com.ccms.service.ApprovalServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审批流程集成测试
 * 验证完整的审批流程和接口功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ApprovalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApprovalFlowConfigRepository flowConfigRepository;

    @Autowired
    private ApprovalInstanceRepository instanceRepository;

    @Autowired
    private ApprovalNodeRepository nodeRepository;

    @Autowired
    private ApprovalRecordRepository recordRepository;

    @Autowired
    private ApprovalAuditLogRepository auditLogRepository;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ApprovalServiceFactory serviceFactory;

    private User testUser;
    private User approver1;
    private User approver2;
    private User financeUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1001L);
        testUser.setUsername("test_user");
        testUser.setRealName("测试用户");

        approver1 = new User();
        approver1.setId(1002L);
        approver1.setUsername("approver1");
        approver1.setRealName("审批人一");

        approver2 = new User();
        approver2.setId(1003L);
        approver2.setUsername("approver2");
        approver2.setRealName("审批人二");

        financeUser = new User();
        financeUser.setId(1004L);
        financeUser.setUsername("finance_user");
        financeUser.setRealName("财务审批");
    }

    /**
     * 测试完整的报销审批流程
     */
    @Test
    void testCompleteExpenseApprovalFlow() throws Exception {
        // 1. 创建审批流程配置
        ApprovalFlowConfigRequest configRequest = new ApprovalFlowConfigRequest();
        configRequest.setFlowName("测试报销审批流程");
        configRequest.setBusinessType("EXPENSE_APPROVAL");
        configRequest.setApprovalType(ApprovalType.SEQUENTIAL);
        configRequest.setCategory("报销");
        configRequest.setMinAmount(BigDecimal.ZERO);
        configRequest.setMaxAmount(new BigDecimal("100000"));
        configRequest.setDescription("测试报销审批流程");

        // 提交配置创建请求
        mockMvc.perform(post("/api/approval/configs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(configRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.flowName").value("测试报销审批流程"));

        // 验证配置已保存
        List<ApprovalFlowConfig> configs = flowConfigRepository.findByBusinessType("EXPENSE_APPROVAL");
        assertThat(configs).hasSize(1);
        ApprovalFlowConfig savedConfig = configs.get(0);

        // 2. 创建审批节点配置
        ApprovalNodeRequest nodeRequest1 = ApprovalNodeRequest.builder()
                .flowConfigId(savedConfig.getId())
                .nodeName("主管审批")
                .nodeType("APPROVAL")
                .approverIds(Arrays.asList(approver1.getId()))
                .approvalStrategy("OR")
                .nodeOrder(1)
                .conditions("amount >= 100 && amount < 5000")
                .build();

        mockMvc.perform(post("/api/approval/configs/{configId}/nodes", savedConfig.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nodeRequest1)))
                .andExpect(status().isCreated());

        ApprovalNodeRequest nodeRequest2 = ApprovalNodeRequest.builder()
                .flowConfigId(savedConfig.getId())
                .nodeName("财务审批")
                .nodeType("APPROVAL")
                .approverIds(Arrays.asList(financeUser.getId()))
                .approvalStrategy("AND")
                .nodeOrder(2)
                .conditions("amount >= 5000")
                .build();

        mockMvc.perform(post("/api/approval/configs/{configId}/nodes", savedConfig.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nodeRequest2)))
                .andExpect(status().isCreated());

        // 3. 创建审批实例
        ExpenseApprovalDto expenseDto = ExpenseApprovalDto.builder()
                .businessType("EXPENSE_APPROVAL")
                .businessTitle("测试报销申请")
                .applicantId(testUser.getId())
                .departmentId(101L)
                .amount(new BigDecimal("2000.00"))
                .description("测试报销申请集成测试")
                .expenseDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/approval/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.currentStatus").value("DRAFT"));

        // 验证实例已创建
        List<ApprovalInstance> instances = instanceRepository.findByApplicantId(testUser.getId());
        assertThat(instances).hasSize(1);
        ApprovalInstance savedInstance = instances.get(0);

        // 4. 提交审批申请
        mockMvc.perform(post("/api/approval/instances/{instanceId}/submit", savedInstance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.currentStatus").value("APPROVING"));

        // 5. 获取待审批列表
        mockMvc.perform(get("/api/approval/instances/pending")
                .param("approverId", approver1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.total").value(1));

        // 6. 进行主管审批
        ApprovalActionDto approveAction = ApprovalActionDto.builder()
                .actionType(ApprovalAction.APPROVE)
                .comment("同意，金额合理")
                .approverId(approver1.getId())
                .build();

        mockMvc.perform(post("/api/approval/instances/{instanceId}/approve", savedInstance.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveAction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.currentStatus").value("COMPLETED"));

        // 验证最终状态
        ApprovalInstance finalInstance = instanceRepository.findById(savedInstance.getId()).get();
        assertThat(finalInstance.getStatus()).isEqualTo(ApprovalStatus.COMPLETED);

        // 验证记录数量
        List<ApprovalRecord> records = recordRepository.findByInstanceId(savedInstance.getId());
        assertThat(records).hasSize(1);

        // 验证审计日志
        List<ApprovalAuditLog> logs = auditLogRepository.findByInstanceId(savedInstance.getId());
        assertThat(logs).hasSizeGreaterThan(2); // 至少包含创建、提交、审批记录
    }

    /**
     * 测试需要多级审批的大额报销流程
     */
    @Test
    void testMultiLevelApprovalFlow() throws Exception {
        // 创建审批配置
        ApprovalFlowConfig config = createMultiLevelConfig();

        // 创建大额报销实例
        ExpenseApprovalDto expenseDto = ExpenseApprovalDto.builder()
                .businessType("EXPENSE_APPROVAL")
                .businessTitle("大额设备采购报销")
                .applicantId(testUser.getId())
                .departmentId(101L)
                .amount(new BigDecimal("8000.00"))
                .description("购买办公设备")
                .expenseDate(LocalDateTime.now())
                .build();

        // 创建并提交实例
        mockMvc.perform(post("/api/approval/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated());

        List<ApprovalInstance> instances = instanceRepository.findByApplicantId(testUser.getId());
        ApprovalInstance instance = instances.get(0);

        // 提交审批
        mockMvc.perform(post("/api/approval/instances/{instanceId}/submit", instance.getId()))
                .andExpect(status().isOk());

        // 主管审批
        ApprovalActionDto approveAction1 = ApprovalActionDto.builder()
                .actionType(ApprovalAction.APPROVE)
                .comment("同意")
                .approverId(approver1.getId())
                .build();

        mockMvc.perform(post("/api/approval/instances/{instanceId}/approve", instance.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveAction1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("APPROVING")); // 应进入下一节点

        // 财务审批
        ApprovalActionDto approveAction2 = ApprovalActionDto.builder()
                .actionType(ApprovalAction.APPROVE)
                .comment("财务审核通过")
                .approverId(financeUser.getId())
                .build();

        mockMvc.perform(post("/api/approval/instances/{instanceId}/approve", instance.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approveAction2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("COMPLETED"));

        // 验证记录和日志
        List<ApprovalRecord> records = recordRepository.findByInstanceId(instance.getId());
        assertThat(records).hasSize(2);

        List<ApprovalAuditLog> logs = auditLogRepository.findByInstanceId(instance.getId());
        assertThat(logs).hasSizeGreaterThan(3);
    }

    /**
     * 测试申请撤回功能
     */
    @Test
    void testWithdrawApproval() throws Exception {
        // 创建配置和实例
        ApprovalFlowConfig config = createSimpleConfig();
        ExpenseApprovalDto expenseDto = createTestExpenseDto();

        // 创建实例
        mockMvc.perform(post("/api/approval/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated());

        ApprovalInstance instance = instanceRepository.findByApplicantId(testUser.getId()).get(0);

        // 提交之前撤回应该失败
        mockMvc.perform(post("/api/approval/instances/{instanceId}/withdraw", instance.getId()))
                .andExpect(status().isBadRequest());

        // 提交
        mockMvc.perform(post("/api/approval/instances/{instanceId}/submit", instance.getId()))
                .andExpect(status().isOk());

        // 撤回申请
        mockMvc.perform(post("/api/approval/instances/{instanceId}/withdraw", instance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("WITHDRAWN"));

        // 状态验证
        ApprovalInstance updated = instanceRepository.findById(instance.getId()).get();
        assertThat(updated.getStatus()).isEqualTo(ApprovalStatus.WITHDRAWN);
    }

    /**
     * 测试驳回和重新提交
     */
    @Test
    void testRejectAndResubmit() throws Exception {
        ApprovalFlowConfig config = createSimpleConfig();
        ExpenseApprovalDto expenseDto = createTestExpenseDto();

        // 创建并提交实例
        mockMvc.perform(post("/api/approval/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated());

        ApprovalInstance instance = instanceRepository.findByApplicantId(testUser.getId()).get(0);
        
        mockMvc.perform(post("/api/approval/instances/{instanceId}/submit", instance.getId()))
                .andExpect(status().isOk());

        // 驳回申请
        ApprovalActionDto rejectAction = ApprovalActionDto.builder()
                .actionType(ApprovalAction.REJECT)
                .comment("单据填写不规范")
                .approverId(approver1.getId())
                .build();

        mockMvc.perform(post("/api/approval/instances/{instanceId}/approve", instance.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectAction)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("REJECTED"));

        // 重新提交
        mockMvc.perform(post("/api/approval/instances/{instanceId}/resubmit", instance.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("APPROVING"));
    }

    /**
     * 测试查询接口
     */
    @Test
    void testQueryInterfaces() throws Exception {
        // 创建测试数据
        createTestData();

        // 测试查询我的申请
        mockMvc.perform(get("/api/approval/instances/my-requests")
                .param("applicantId", testUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.total").value(2));

        // 测试查询配置列表
        mockMvc.perform(get("/api/approval/configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1000"))
                .andExpect(jsonPath("$.data.total").valueGreaterThan(0));

        // 测试根据业务类型查询实例
        mockMvc.perform(get("/api/approval/instances")
                .param("businessType", "EXPENSE_APPROVAL"))
                .andExpect(status().isOk());
    }

    /**
     * 测试异常场景
     */
    @Test
    void testErrorScenarios() throws Exception {
        // 测试不存在的实例ID
        mockMvc.perform(get("/api/approval/instances/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("1101"));

        // 测试无效的申请数据
        ExpenseApprovalDto invalidDto = ExpenseApprovalDto.builder()
                .businessType("")
                .applicantId(null)
                .amount(null)
                .build();

        mockMvc.perform(post("/api/approval/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        // 测试非法状态转换
        ApprovalFlowConfig config = createSimpleConfig();
        ExpenseApprovalDto expenseDto = createTestExpenseDto();

        mockMvc.perform(post("/api/approval/instances")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated());

        ApprovalInstance instance = instanceRepository.findByApplicantId(testUser.getId()).get(0);

        // 直接审批草稿状态应该失败
        ApprovalActionDto action = ApprovalActionDto.builder()
                .actionType(ApprovalAction.APPROVE)
                .approverId(approver1.getId())
                .build();

        mockMvc.perform(post("/api/approval/instances/{instanceId}/approve", instance.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(action)))
                .andExpect(status().isBadRequest());
    }

    // 辅助方法
    private ApprovalFlowConfig createSimpleConfig() {
        ApprovalFlowConfig config = new ApprovalFlowConfig();
        config.setFlowName("简单审批流程");
        config.setBusinessType("EXPENSE_APPROVAL");
        config.setApprovalType(ApprovalType.SEQUENTIAL);
        config.setCategory("报销");
        config.setMinAmount(BigDecimal.ZERO);
        config.setMaxAmount(new BigDecimal("100000"));
        config.setActive(true);
        return flowConfigRepository.save(config);
    }

    private ApprovalFlowConfig createMultiLevelConfig() {
        ApprovalFlowConfig config = createSimpleConfig();
        
        // 添加节点
        ApprovalNode node1 = new ApprovalNode();
        node1.setFlowConfigId(config.getId());
        node1.setNodeName("主管审批");
        node1.setNodeType("APPROVAL");
        node1.setApproverIds("1002");
        node1.setApprovalStrategy("OR");
        node1.setNodeOrder(1);
        node1.setConditions("amount >= 1000");
        nodeRepository.save(node1);

        ApprovalNode node2 = new ApprovalNode();
        node2.setFlowConfigId(config.getId());
        node2.setNodeName("财务审批");
        node2.setNodeType("APPROVAL");
        node2.setApproverIds("1004");
        node2.setApprovalStrategy("AND");
        node2.setNodeOrder(2);
        node2.setConditions("amount >= 5000");
        nodeRepository.save(node2);

        return config;
    }

    private ExpenseApprovalDto createTestExpenseDto() {
        return ExpenseApprovalDto.builder()
                .businessType("EXPENSE_APPROVAL")
                .businessTitle("测试报销")
                .applicantId(testUser.getId())
                .departmentId(101L)
                .amount(new BigDecimal("1500.00"))
                .description("集成测试")
                .expenseDate(LocalDateTime.now())
                .build();
    }

    private void createTestData() {
        // 创建配置
        ApprovalFlowConfig config = createSimpleConfig();

        // 创建多个测试实例
        ExpenseApprovalDto dto1 = createTestExpenseDto();
        ExpenseApprovalDto dto2 = createTestExpenseDto();
        dto2.setAmount(new BigDecimal("3000.00"));

        // 保存实例（模拟创建过程）
        ApprovalInstance instance1 = new ApprovalInstance();
        instance1.setBusinessType(dto1.getBusinessType());
        instance1.setBusinessTitle(dto1.getBusinessTitle());
        instance1.setApplicantId(dto1.getApplicantId());
        instance1.setDepartmentId(dto1.getDepartmentId());
        instance1.setStatus(ApprovalStatus.DRAFT);
        instance1.setFlowConfigId(config.getId());
        instanceRepository.save(instance1);

        ApprovalInstance instance2 = new ApprovalInstance();
        instance2.setBusinessType(dto2.getBusinessType());
        instance2.setBusinessTitle(dto2.getBusinessTitle());
        instance2.setApplicantId(dto2.getApplicantId());
        instance2.setDepartmentId(dto2.getDepartmentId());
        instance2.setStatus(ApprovalStatus.APPROVING);
        instance2.setFlowConfigId(config.getId());
        instanceRepository.save(instance2);
    }
}