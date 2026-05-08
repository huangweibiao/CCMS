package com.ccms.controller.workflow;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.approval.Approval;
import com.ccms.service.ApprovalService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 审批管理控制器单元测试
 */
@WebMvcTest(ApprovalController.class)
class ApprovalControllerTest extends ControllerTestBase {

    @MockBean
    private ApprovalService approvalService;

    private Approval createTestApproval(Long id, String businessType, Integer status) {
        Approval approval = new Approval();
        approval.setId(id);
        approval.setBusinessType(businessType);
        approval.setBusinessId(1L);
        approval.setApplicantId(1L);
        approval.setStatus(status);
        approval.setTitle("测试审批");
        return approval;
    }

    @Test
    void shouldReturnApprovalListWhenQuerySuccess() throws Exception {
        Approval approval = createTestApproval(1L, "EXPENSE", 0);
        Page<Approval> page = new PageImpl<>(Collections.singletonList(approval));
        when(approvalService.getApprovalList(anyInt(), anyInt(), any(), any(), any(), any())).thenReturn(page);

        performGet("/api/approval")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void shouldReturnApprovalWhenGetByIdSuccess() throws Exception {
        Approval approval = createTestApproval(1L, "EXPENSE", 0);
        when(approvalService.getApprovalById(1L)).thenReturn(approval);

        performGet("/api/approval/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturnNotFoundWhenGetByIdNotExist() throws Exception {
        when(approvalService.getApprovalById(999L)).thenReturn(null);

        performGet("/api/approval/999")
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateApprovalSuccess() throws Exception {
        Approval approval = createTestApproval(1L, "EXPENSE", 0);
        doNothing().when(approvalService).createApproval(any(Approval.class));

        performPost("/api/approval", approval)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批创建成功"));
    }

    @Test
    void shouldUpdateApprovalSuccess() throws Exception {
        Approval approval = createTestApproval(1L, "EXPENSE", 0);
        doNothing().when(approvalService).updateApproval(any(Approval.class));

        performPut("/api/approval/1", approval)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldDeleteApprovalSuccess() throws Exception {
        doNothing().when(approvalService).deleteApproval(1L);

        performDelete("/api/approval/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldApproveSuccess() throws Exception {
        doNothing().when(approvalService).approve(anyLong(), anyLong(), anyInt(), any());

        performPost("/api/approval/1/approve?approverId=2&comment=同意")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批已通过"));
    }

    @Test
    void shouldRejectSuccess() throws Exception {
        doNothing().when(approvalService).approve(anyLong(), anyLong(), anyInt(), any());

        performPost("/api/approval/1/reject?approverId=2&comment=驳回")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批已驳回"));
    }

    @Test
    void shouldReturnPendingApprovals() throws Exception {
        when(approvalService.getPendingApprovals(1L)).thenReturn(Collections.emptyList());

        performGet("/api/approval/pending/1")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnProcessedApprovals() throws Exception {
        when(approvalService.getProcessedApprovals(1L)).thenReturn(Collections.emptyList());

        performGet("/api/approval/processed/1")
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnApprovalStatistics() throws Exception {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", 100);
        stats.put("pending", 20);
        when(approvalService.getApprovalStatistics(any(), any(), any(), any())).thenReturn(stats);

        performGet("/api/approval/statistics?approverId=1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100));
    }
}

