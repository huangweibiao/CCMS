package com.ccms.controller.budget;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.service.BudgetAdjustService;
import com.ccms.vo.ResultVO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 预算调整控制器单元测试
 */
@WebMvcTest(BudgetAdjustController.class)
class BudgetAdjustControllerTest extends ControllerTestBase {

    @MockBean
    private BudgetAdjustService budgetAdjustService;

    private Map<String, Object> createApplyRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("budgetId", 1L);
        request.put("budgetDetailId", 1L);
        request.put("adjustType", 1);
        request.put("adjustAmount", "1000.00");
        request.put("reason", "预算调整原因");
        request.put("applyUserId", 1L);
        request.put("applyUserName", "张三");
        return request;
    }

    @Test
    void shouldCreateAdjustApplySuccess() throws Exception {
        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        ResultVO<BudgetAdjust> result = ResultVO.success(adjust);
        when(budgetAdjustService.createAdjustApply(anyLong(), anyLong(), anyInt(), any(BigDecimal.class), anyString(), anyLong(), anyString()))
                .thenReturn(result);

        performPost("/api/budget/adjust/apply", createApplyRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturnBadRequestWhenCreateAdjustApplyFails() throws Exception {
        ResultVO<BudgetAdjust> result = ResultVO.error("预算不足");
        when(budgetAdjustService.createAdjustApply(anyLong(), anyLong(), anyInt(), any(BigDecimal.class), anyString(), anyLong(), anyString()))
                .thenReturn(result);

        performPost("/api/budget/adjust/apply", createApplyRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldSubmitToApprovalSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("adjustId", 1L);
        request.put("userId", 1L);

        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        when(budgetAdjustService.submitToApproval(anyLong(), anyLong()))
                .thenReturn(ResultVO.success(adjust));

        performPost("/api/budget/adjust/submit", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldApproveAdjustSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("adjustId", 1L);
        request.put("approveResult", true);
        request.put("approvalComment", "同意调整");
        request.put("approverId", 2L);
        request.put("approverName", "李四");

        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        when(budgetAdjustService.approveAdjust(anyLong(), anyBoolean(), anyString(), anyLong(), anyString()))
                .thenReturn(ResultVO.success(adjust));

        performPost("/api/budget/adjust/approve", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldExecuteAdjustSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("adjustId", 1L);

        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        when(budgetAdjustService.executeAdjust(anyLong()))
                .thenReturn(ResultVO.success(adjust));

        performPost("/api/budget/adjust/execute", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldCancelAdjustSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("adjustId", 1L);
        request.put("userId", 1L);
        request.put("reason", "撤销原因");

        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        when(budgetAdjustService.cancelAdjust(anyLong(), anyLong(), anyString()))
                .thenReturn(ResultVO.success(adjust));

        performPost("/api/budget/adjust/cancel", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldReturnAdjustDetailWhenExists() throws Exception {
        BudgetAdjust adjust = new BudgetAdjust();
        adjust.setId(1L);
        when(budgetAdjustService.getAdjustDetail(1L)).thenReturn(adjust);

        performGet("/api/budget/adjust/detail/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenAdjustDetailNotExists() throws Exception {
        when(budgetAdjustService.getAdjustDetail(999L)).thenReturn(null);

        performGet("/api/budget/adjust/detail/999")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldValidateAdjustmentSuccess() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("budgetId", 1L);
        request.put("budgetDetailId", 1L);
        request.put("adjustType", 1);
        request.put("adjustAmount", "1000.00");

        when(budgetAdjustService.validateAdjustment(anyLong(), anyLong(), anyInt(), any(BigDecimal.class)))
                .thenReturn(ResultVO.success(true));

        performPost("/api/budget/adjust/validate", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));
    }
}
