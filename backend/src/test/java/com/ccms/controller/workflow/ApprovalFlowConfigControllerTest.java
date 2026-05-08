package com.ccms.controller.workflow;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审批流配置控制器单元测试
 */
@WebMvcTest(ApprovalFlowConfigController.class)
class ApprovalFlowConfigControllerTest extends ControllerTestBase {

    @MockBean
    private ApprovalFlowConfigRepository approvalFlowConfigRepository;

    private ApprovalFlowConfig createTestConfig(Long id, String code, String name, String businessType) {
        ApprovalFlowConfig config = new ApprovalFlowConfig();
        config.setId(id);
        config.setFlowCode(code);
        config.setFlowName(name);
        config.setBusinessType(businessType);
        config.setMinAmount(new BigDecimal("0"));
        config.setMaxAmount(new BigDecimal("10000"));
        config.setStatus(1);
        config.setCreateTime(LocalDateTime.now());
        return config;
    }

    @Test
    void shouldReturnFlowConfigList() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findAll()).thenReturn(Collections.singletonList(config));

        performGet("/api/approval/flow-config")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].flowCode").value("EXPENSE_APPROVAL"));
    }

    @Test
    void shouldReturnFlowConfigByIdWhenExists() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findById(1L)).thenReturn(Optional.of(config));

        performGet("/api/approval/flow-config/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.flowCode").value("EXPENSE_APPROVAL"));
    }

    @Test
    void shouldReturnNotFoundWhenFlowConfigNotExists() throws Exception {
        when(approvalFlowConfigRepository.findById(999L)).thenReturn(Optional.empty());

        performGet("/api/approval/flow-config/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnFlowConfigByCodeWhenExists() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findByFlowCode("EXPENSE_APPROVAL")).thenReturn(Optional.of(config));

        performGet("/api/approval/flow-config/code/EXPENSE_APPROVAL")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flowCode").value("EXPENSE_APPROVAL"));
    }

    @Test
    void shouldReturnFlowConfigsByBusinessType() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findAll()).thenReturn(Collections.singletonList(config));

        performGet("/api/approval/flow-config/business-type/EXPENSE")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldCreateFlowConfigSuccess() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "NEW_APPROVAL", "新审批流", "EXPENSE");
        when(approvalFlowConfigRepository.existsByFlowCode("NEW_APPROVAL")).thenReturn(false);
        when(approvalFlowConfigRepository.save(any(ApprovalFlowConfig.class))).thenReturn(config);

        performPost("/api/approval/flow-config", config)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flowCode").value("NEW_APPROVAL"));
    }

    @Test
    void shouldReturnBadRequestWhenFlowCodeExists() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXISTING_CODE", "已存在", "EXPENSE");
        when(approvalFlowConfigRepository.existsByFlowCode("EXISTING_CODE")).thenReturn(true);

        performPost("/api/approval/flow-config", config)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("流程编码已存在"));
    }

    @Test
    void shouldUpdateFlowConfigSuccess() throws Exception {
        ApprovalFlowConfig existing = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        ApprovalFlowConfig updated = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批更新", "EXPENSE");
        when(approvalFlowConfigRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(approvalFlowConfigRepository.findByFlowCode("EXPENSE_APPROVAL")).thenReturn(Optional.of(existing));
        when(approvalFlowConfigRepository.save(any(ApprovalFlowConfig.class))).thenReturn(updated);

        performPut("/api/approval/flow-config/1", updated)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flowName").value("费用审批更新"));
    }

    @Test
    void shouldDeleteFlowConfigSuccess() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findById(1L)).thenReturn(Optional.of(config));

        performDelete("/api/approval/flow-config/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批流配置删除成功"));
    }

    @Test
    void shouldUpdateFlowConfigStatusSuccess() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findById(1L)).thenReturn(Optional.of(config));
        when(approvalFlowConfigRepository.save(any(ApprovalFlowConfig.class))).thenReturn(config);

        performPut("/api/approval/flow-config/1/status?status=0", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批流配置已禁用"));
    }

    @Test
    void shouldMatchFlowConfigSuccess() throws Exception {
        ApprovalFlowConfig config = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        when(approvalFlowConfigRepository.findByStatus(1)).thenReturn(Collections.singletonList(config));

        performPost("/api/approval/flow-config/match?businessType=EXPENSE&amount=5000&deptId=1&feeTypeId=1", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flowCode").value("EXPENSE_APPROVAL"));
    }

    @Test
    void shouldReturnNotFoundWhenNoMatchedFlowConfig() throws Exception {
        when(approvalFlowConfigRepository.findByStatus(1)).thenReturn(Collections.emptyList());

        performPost("/api/approval/flow-config/match?businessType=UNKNOWN&amount=5000", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("未找到适用的审批流配置"));
    }

    @Test
    void shouldCopyFlowConfigSuccess() throws Exception {
        ApprovalFlowConfig existing = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        ApprovalFlowConfig copied = createTestConfig(2L, "EXPENSE_APPROVAL_COPY", "费用审批副本", "EXPENSE");
        when(approvalFlowConfigRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(approvalFlowConfigRepository.existsByFlowCode("EXPENSE_APPROVAL_COPY")).thenReturn(false);
        when(approvalFlowConfigRepository.save(any(ApprovalFlowConfig.class))).thenReturn(copied);

        performPost("/api/approval/flow-config/1/copy?newFlowCode=EXPENSE_APPROVAL_COPY&newFlowName=费用审批副本", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.flowCode").value("EXPENSE_APPROVAL_COPY"));
    }

    @Test
    void shouldGetFlowConfigStatistics() throws Exception {
        ApprovalFlowConfig config1 = createTestConfig(1L, "EXPENSE_APPROVAL", "费用审批", "EXPENSE");
        ApprovalFlowConfig config2 = createTestConfig(2L, "BUDGET_APPROVAL", "预算审批", "BUDGET");
        config2.setStatus(0);
        when(approvalFlowConfigRepository.findAll()).thenReturn(java.util.Arrays.asList(config1, config2));

        performGet("/api/approval/flow-config/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.active").value(1))
                .andExpect(jsonPath("$.inactive").value(1));
    }
}
