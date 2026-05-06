package com.ccms.controller;

import com.ccms.entity.budget.BudgetAdjust;
import com.ccms.service.BudgetAdjustService;
import com.ccms.vo.ResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 预算调整控制器单元测试
 */
@WebMvcTest(BudgetAdjustController.class)
class BudgetAdjustControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetAdjustService budgetAdjustService;

    @Autowired
    private ObjectMapper objectMapper;

    private BudgetAdjust testBudgetAdjust;
    private ResultVO<BudgetAdjust> successResult;
    private ResultVO<BudgetAdjust> failureResult;

    @BeforeEach
    void setUp() {
        testBudgetAdjust = createTestBudgetAdjust();
        
        successResult = ResultVO.<BudgetAdjust>builder()
                .success(true)
                .code("200")
                .msg("操作成功")
                .data(testBudgetAdjust)
                .build();
                
        failureResult = ResultVO.<BudgetAdjust>builder()
                .success(false)
                .code("400")
                .msg("操作失败")
                .build();
    }

    @Test
    void shouldReturnSuccessResponse_whenCreateAdjustApplySuccess() throws Exception {
        // Given
        Map<String, Object> applyRequest = Map.of(
                "budgetId", 1L,
                "budgetDetailId", 2L,
                "adjustType", 1,
                "adjustAmount", 5000.00,
                "reason", "项目预算不足需要追加",
                "applyUserId", 1001L,
                "applyUserName", "张三"
        );
        
        when(budgetAdjustService.createAdjustApply(
                eq(1L), eq(2L), eq(1), eq(new BigDecimal("5000.00")), 
                eq("项目预算不足需要追加"), eq(1001L), eq("张三"))).thenReturn(successResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("操作成功"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnBadRequest_whenCreateAdjustApplyFails() throws Exception {
        // Given
        Map<String, Object> applyRequest = Map.of(
                "budgetId", 1L,
                "budgetDetailId", 2L,
                "adjustType", 1,
                "adjustAmount", 5000.00,
                "reason", "项目预算不足需要追加",
                "applyUserId", 1001L,
                "applyUserName", "张三"
        );
        
        when(budgetAdjustService.createAdjustApply(
                eq(1L), eq(2L), eq(1), eq(new BigDecimal("5000.00")), 
                eq("项目预算不足需要追加"), eq(1001L), eq("张三"))).thenReturn(failureResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("操作失败"));
    }

    @Test
    void shouldReturnServerError_whenCreateAdjustApplyThrowsException() throws Exception {
        // Given
        Map<String, Object> applyRequest = new HashMap<>();
        applyRequest.put("budgetId", "invalid"); // 无效的预算ID

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldReturnSuccessResponse_whenSubmitToApprovalSuccess() throws Exception {
        // Given
        Map<String, Object> submitRequest = Map.of(
                "adjustId", 1L,
                "userId", 1001L
        );
        
        when(budgetAdjustService.submitToApproval(eq(1L), eq(1001L))).thenReturn(successResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("操作成功"));
    }

    @Test
    void shouldReturnBadRequest_whenSubmitToApprovalFails() throws Exception {
        // Given
        Map<String, Object> submitRequest = Map.of(
                "adjustId", 1L,
                "userId", 1001L
        );
        
        when(budgetAdjustService.submitToApproval(eq(1L), eq(1001L))).thenReturn(failureResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldHandleApprovalSuccess_whenApproveAdjust() throws Exception {
        // Given
        Map<String, Object> approveRequest = Map.of(
                "adjustId", 1L,
                "approveResult", true,
                "approvalComment", "同意调整",
                "approverId", 1002L,
                "approverName", "李四"
        );
        
        // 模拟审批通过
        ResultVO<BudgetAdjust> approveSuccessResult = ResultVO.<BudgetAdjust>builder()
                .success(true)
                .code("200")
                .msg("审批通过")
                .data(testBudgetAdjust)
                .build();
        
        when(budgetAdjustService.approveAdjust(
                eq(1L), eq(true), eq("同意调整"), eq(1002L), eq("李四"))).thenReturn(approveSuccessResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("审批通过"));
    }

    @Test
    void shouldHandleApprovalRejection_whenApproveAdjust() throws Exception {
        // Given
        Map<String, Object> approveRequest = Map.of(
                "adjustId", 1L,
                "approveResult", false,
                "approvalComment", "预算不足，不予通过",
                "approverId", 1002L,
                "approverName", "李四"
        );
        
        // 模拟审批驳回
        ResultVO<BudgetAdjust> rejectResult = ResultVO.<BudgetAdjust>builder()
                .success(false)
                .code("400")
                .msg("审批驳回")
                .build();
        
        when(budgetAdjustService.approveAdjust(
                eq(1L), eq(false), eq("预算不足，不予通过"), eq(1002L), eq("李四"))).thenReturn(rejectResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/approve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approveRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("审批驳回"));
    }

    @Test
    void shouldExecuteAdjustSuccessfully() throws Exception {
        // Given
        Map<String, Object> executeRequest = Map.of("adjustId", 1L);
        
        // 模拟执行成功
        ResultVO<BudgetAdjust> executeResult = ResultVO.<BudgetAdjust>builder()
                .success(true)
                .code("200")
                .msg("预算调整执行成功")
                .data(testBudgetAdjust)
                .build();
        
        when(budgetAdjustService.executeAdjust(eq(1L))).thenReturn(executeResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算调整执行成功"));
    }

    @Test
    void shouldHandleExecuteFailure() throws Exception {
        // Given
        Map<String, Object> executeRequest = Map.of("adjustId", 1L);
        
        when(budgetAdjustService.executeAdjust(eq(1L))).thenReturn(failureResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(executeRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldCancelAdjustSuccessfully() throws Exception {
        // Given
        Map<String, Object> cancelRequest = Map.of(
                "adjustId", 1L,
                "userId", 1001L,
                "reason", "申请错误，需要撤销"
        );
        
        // 模拟撤销成功
        ResultVO<BudgetAdjust> cancelResult = ResultVO.<BudgetAdjust>builder()
                .success(true)
                .code("200")
                .msg("撤销成功")
                .data(testBudgetAdjust)
                .build();
        
        when(budgetAdjustService.cancelAdjust(eq(1L), eq(1001L), eq("申请错误，需要撤销"))).thenReturn(cancelResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cancelRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("撤销成功"));
    }

    @Test
    void shouldRetrieveAdjustDetailSuccessfully() throws Exception {
        // Given
        when(budgetAdjustService.getAdjustDetail(eq(1L))).thenReturn(testBudgetAdjust);

        // When & Then
        mockMvc.perform(get("/api/budget/adjust/detail/{adjustId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取调整记录详情成功"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldHandleAdjustDetailNotFound() throws Exception {
        // Given
        when(budgetAdjustService.getAdjustDetail(eq(999L))).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/budget/adjust/detail/{adjustId}", 999L))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("调整记录不存在"));
    }

    @Test
    void shouldValidateAdjustmentSuccessfully() throws Exception {
        // Given
        Map<String, Object> validateRequest = Map.of(
                "budgetId", 1L,
                "budgetDetailId", 2L,
                "adjustType", 1,
                "adjustAmount", 3000.00
        );
        
        // 模拟验证通过
        ResultVO<Boolean> validateResult = ResultVO.<Boolean>builder()
                .success(true)
                .code("200")
                .msg("预算调整验证通过")
                .data(true)
                .build();
        
        when(budgetAdjustService.validateAdjustment(
                eq(1L), eq(2L), eq(1), eq(new BigDecimal("3000.00")))).thenReturn(validateResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("预算调整验证通过"))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldHandleAdjustmentValidationFailure() throws Exception {
        // Given
        Map<String, Object> validateRequest = Map.of(
                "budgetId", 1L,
                "budgetDetailId", 2L,
                "adjustType", 1,
                "adjustAmount", 50000.00
        );
        
        // 模拟验证失败
        ResultVO<Boolean> validateFailureResult = ResultVO.<Boolean>builder()
                .success(false)
                .code("400")
                .msg("调整金额超出预算范围")
                .data(false)
                .build();
        
        when(budgetAdjustService.validateAdjustment(
                eq(1L), eq(2L), eq(1), eq(new BigDecimal("50000.00")))).thenReturn(validateFailureResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validateRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("调整金额超出预算范围"));
    }

    @Test
    void shouldHandleMalformedAdjustOperation() throws Exception {
        // Given
        Map<String, Object> malformedRequest = Map.of(
                "adjustId", "invalid_id", // 无效的调整ID
                "userId", "not_a_number"  // 无效的用户ID
        );
        
        // When & Then
        mockMvc.perform(post("/api/budget/adjust/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(malformedRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldHandleZeroAdjustAmount() throws Exception {
        // Given
        Map<String, Object> zeroAmountRequest = Map.of(
                "budgetId", 1L,
                "budgetDetailId", 2L,
                "adjustType", 1,
                "adjustAmount", 0.00
        );
        
        // 模拟零金额验证结果
        ResultVO<Boolean> zeroAmountResult = ResultVO.<Boolean>builder()
                .success(false)
                .code("400")
                .msg("调整金额不能为零")
                .data(false)
                .build();
        
        when(budgetAdjustService.validateAdjustment(
                eq(1L), eq(2L), eq(1), eq(new BigDecimal("0.00")))).thenReturn(zeroAmountResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(zeroAmountRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("调整金额不能为零"));
    }

    @Test
    void shouldHandleLargeAdjustAmount() throws Exception {
        // Given
        Map<String, Object> largeAmountRequest = Map.of(
                "budgetId", 1L,
                "budgetDetailId", 2L,
                "adjustType", 1,
                "adjustAmount", 99999999.99
        );
        
        ResultVO<Boolean> largeAmountResult = ResultVO.<Boolean>builder()
                .success(true)
                .code("200")
                .msg("验证通过")
                .data(true)
                .build();
        
        when(budgetAdjustService.validateAdjustment(
                eq(1L), eq(2L), eq(1), eq(new BigDecimal("99999999.99")))).thenReturn(largeAmountResult);

        // When & Then
        mockMvc.perform(post("/api/budget/adjust/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(largeAmountRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 创建测试预算调整记录
     */
    private BudgetAdjust createTestBudgetAdjust() {
        BudgetAdjust budgetAdjust = new BudgetAdjust();
        budgetAdjust.setId(1L);
        budgetAdjust.setBudgetId(1L);
        budgetAdjust.setBudgetDetailId(2L);
        budgetAdjust.setAdjustNo("BA20250001");
        budgetAdjust.setAdjustType(1); // 1-追加预算
        budgetAdjust.setAdjustAmount(new BigDecimal("5000.00"));
        budgetAdjust.setReason("项目预算不足需要追加预算");
        budgetAdjust.setAdjustBy(1001L);
        budgetAdjust.setApprovalStatus(0); // 0-待提交
        budgetAdjust.setExecuteStatus(0); // 0-未执行
        budgetAdjust.setCreateTime(LocalDateTime.now());
        return budgetAdjust;
    }
}