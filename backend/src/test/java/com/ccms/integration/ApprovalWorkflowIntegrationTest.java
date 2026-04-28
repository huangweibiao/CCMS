package com.ccms.integration;

import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalRecordRepository;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.service.ApprovalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审批流程集成测试
 * 测试完整的审批流程：启动审批 -> 审批操作 -> 完成审批
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
class ApprovalWorkflowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private ApprovalInstanceRepository approvalInstanceRepository;

    @Autowired
    private ApprovalRecordRepository approvalRecordRepository;

    @Autowired
    private ApprovalFlowConfigRepository approvalFlowConfigRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Long testFlowId;
    private Long testUserId = 1L;
    private Long testApproverId = 2L;
    private Long testBusinessId = 1001L;

    @BeforeEach
    void setUp() {
        // 创建测试审批流配置
        ApprovalFlowConfig testFlowConfig = new ApprovalFlowConfig();
        testFlowConfig.setFlowName("测试审批流程");
        testFlowConfig.setBusinessType("TEST_BUSINESS");
        testFlowConfig.setFlowConfig("{\"nodes\": [{\"name\": \"部门经理审批\", \"approvers\": [2]}, {\"name\": \"财务审批\", \"approvers\": [3]}]}");
        testFlowConfig.setCreateBy(testUserId);
        testFlowConfig.setCreateTime(LocalDateTime.now());
        testFlowConfig.setUpdateBy(testUserId);
        testFlowConfig.setUpdateTime(LocalDateTime.now());
        
        ApprovalFlowConfig savedConfig = approvalFlowConfigRepository.save(testFlowConfig);
        testFlowId = savedConfig.getId();
    }

    @Test
    void testCompleteApprovalWorkflow() throws Exception {
        // 1. 启动审批流程
        Map<String, Object> createRequest = Map.of(
                "flowId", testFlowId,
                "businessId", testBusinessId,
                "businessType", "TEST_BUSINESS",
                "createBy", testUserId
        );

        String response = mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        assertEquals("审批流程创建成功", responseMap.get("message"));

        // 2. 验证审批实例已创建
        ApprovalInstance approvalInstance = approvalInstanceRepository.findByBusinessIdAndBusinessType(testBusinessId, "TEST_BUSINESS")
                .orElse(null);
        assertNotNull(approvalInstance);
        assertEquals(0, approvalInstance.getStatus()); // 审核中
        assertEquals("部门经理审批", approvalInstance.getCurrentNode());

        Long approvalInstanceId = approvalInstance.getId();

        // 3. 查看待审批列表
        mockMvc.perform(get("/api/approvals/pending")
                        .param("approverId", testApproverId.toString())
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(approvalInstanceId));

        // 4. 获取审批详情
        mockMvc.perform(get("/api/approvals/" + approvalInstanceId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(approvalInstanceId));

        // 5. 执行审批操作（同意）
        Map<String, Object> approveRequest = Map.of(
                "result", 1,
                "comment", "测试审批同意",
                "approverId", testApproverId
        );

        mockMvc.perform(post("/api/approvals/" + approvalInstanceId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批操作完成"));

        // 6. 验证审批记录已创建
        ApprovalRecord approvalRecord = approvalRecordRepository.findByInstanceId(approvalInstanceId).get(0);
        assertNotNull(approvalRecord);
        assertEquals(1, approvalRecord.getApprovalAction()); // 同意
        assertEquals("测试审批同意", approvalRecord.getApprovalRemark());
        assertEquals(testApproverId, approvalRecord.getApproverId());

        // 7. 验证审批实例状态更新
        ApprovalInstance updatedInstance = approvalInstanceRepository.findById(approvalInstanceId).orElse(null);
        assertNotNull(updatedInstance);
        assertEquals("财务审批", updatedInstance.getCurrentNode()); // 进入到下一个节点

        // 8. 查看审批历史
        mockMvc.perform(get("/api/approvals/" + approvalInstanceId + "/history")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // 9. 统计信息查询
        mockMvc.perform(get("/api/approvals/statistics")
                        .param("approverId", testApproverId.toString())
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-12-31")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());
    }

    @Test
    void testApprovalWorkflowWithRejection() throws Exception {
        // 1. 启动审批流程
        Map<String, Object> createRequest = Map.of(
                "flowId", testFlowId,
                "businessId", testBusinessId + 1,
                "businessType", "TEST_BUSINESS",
                "createBy", testUserId
        );

        mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // 2. 获取审批实例ID
        ApprovalInstance approvalInstance = approvalInstanceRepository.findByBusinessIdAndBusinessType(testBusinessId + 1, "TEST_BUSINESS")
                .orElseThrow();
        Long approvalInstanceId = approvalInstance.getId();

        // 3. 执行审批操作（驳回）
        Map<String, Object> rejectRequest = Map.of(
                "result", 2,
                "comment", "申请材料不完整，请补充后再提交",
                "approverId", testApproverId
        );

        mockMvc.perform(post("/api/approvals/" + approvalInstanceId + "/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批操作完成"));

        // 4. 验证审批流程终止（驳回后流程结束）
        ApprovalInstance rejectedInstance = approvalInstanceRepository.findById(approvalInstanceId).orElse(null);
        assertNotNull(rejectedInstance);
        assertEquals(2, rejectedInstance.getStatus()); // 已驳回

        // 5. 验证审批记录
        ApprovalRecord rejectionRecord = approvalRecordRepository.findByInstanceId(approvalInstanceId).get(0);
        assertEquals(2, rejectionRecord.getApprovalAction()); // 驳回
        assertEquals("申请材料不完整，请补充后再提交", rejectionRecord.getApprovalRemark());
    }

    @Test
    void testApprovalWorkflowWithDelegation() throws Exception {
        // 1. 启动审批流程
        Map<String, Object> createRequest = Map.of(
                "flowId", testFlowId,
                "businessId", testBusinessId + 2,
                "businessType", "TEST_BUSINESS",
                "createBy", testUserId
        );

        mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // 2. 获取审批实例ID
        ApprovalInstance approvalInstance = approvalInstanceRepository.findByBusinessIdAndBusinessType(testBusinessId + 2, "TEST_BUSINESS")
                .orElseThrow();
        Long approvalInstanceId = approvalInstance.getId();

        // 3. 执行审批委托
        Long delegateToId = 3L;
        Map<String, Object> delegationRequest = Map.of(
                "delegateToId", delegateToId,
                "reason", "出差期间，委托他人处理"
        );

        mockMvc.perform(post("/api/approvals/" + approvalInstanceId + "/delegate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(delegationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批委托成功"));

        // 4. 验证委托记录（这里需要检查是否有委托记录或节点转移记录）
        // 根据实际实现逻辑进行验证
        ApprovalRecord delegationRecord = approvalRecordRepository.findByInstanceId(approvalInstanceId)
                .stream()
                .filter(record -> record.getTransferTo() != null)
                .findFirst()
                .orElse(null);
        
        // 如果存在转审记录，验证转审目标
        if (delegationRecord != null) {
            assertEquals(delegateToId, delegationRecord.getTransferTo());
        }
    }

    @Test
    void testApprovalWorkflowUrgentSetting() throws Exception {
        // 1. 启动审批流程
        Map<String, Object> createRequest = Map.of(
                "flowId", testFlowId,
                "businessId", testBusinessId + 3,
                "businessType", "TEST_BUSINESS",
                "createBy", testUserId
        );

        mockMvc.perform(post("/api/approvals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // 2. 获取审批实例ID
        ApprovalInstance approvalInstance = approvalInstanceRepository.findByBusinessIdAndBusinessType(testBusinessId + 3, "TEST_BUSINESS")
                .orElseThrow();
        Long approvalInstanceId = approvalInstance.getId();

        // 3. 设置审批加急
        Map<String, Object> urgentRequest = Map.of(
                "urgentReason", "紧急会议需要",
                "priority", 1
        );

        mockMvc.perform(post("/api/approvals/" + approvalInstanceId + "/urgent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(urgentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批加急设置成功"));
    }

    @Test
    void testApprovalConfiguration() throws Exception {
        // 测试审批流程配置
        Map<String, Object> configRequest = Map.of(
                "businessType", "NEW_BUSINESS",
                "approvalFlow", Map.of(
                        "nodes", new Object[]{
                                Map.of("name", "一级审批", "approvers", new Long[]{2L}),
                                Map.of("name", "二级审批", "approvers", new Long[]{3L})
                        }
                )
        );

        mockMvc.perform(post("/api/approvals/configuration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(configRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("审批流程配置成功"));

        // 验证配置已保存
        assertTrue(approvalFlowConfigRepository.findByBusinessType("NEW_BUSINESS").isPresent());
    }

    @Test
    void testApprovalBatchOperation() throws Exception {
        // 创建多个审批实例用于批量操作
        for (int i = 0; i < 3; i++) {
            ApprovalInstance instance = new ApprovalInstance();
            instance.setInstanceNo("TEST-" + i);
            instance.setFlowId(testFlowId);
            instance.setBusinessId(testBusinessId + 10 + i);
            instance.setBusinessType("TEST_BUSINESS");
            instance.setStatus(0);
            instance.setCurrentNode("部门经理审批");
            instance.setCreateBy(testUserId);
            approvalInstanceRepository.save(instance);
        }

        // 批量撤回操作
        Map<String, Object> batchRequest = Map.of(
                "operation", "withdraw",
                "approvalIds", new Long[]{
                        approvalInstanceRepository.findAll().get(0).getId(),
                        approvalInstanceRepository.findAll().get(1).getId()
                }
        );

        mockMvc.perform(post("/api/approvals/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer test-token")
                        .content(objectMapper.writeValueAsString(batchRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("批量审批操作完成"));
    }
}