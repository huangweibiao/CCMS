package com.ccms.controller;

import com.ccms.entity.expense.LoanRepayment;
import com.ccms.service.LoanSettlementService;
import com.ccms.vo.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 借款核销控制器单元测试
 */
@WebMvcTest(LoanSettlementController.class)
class LoanSettlementControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanSettlementService loanSettlementService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoanSettlementService.SettlementResult testSettlementResult;
    private LoanRepayment testRepayment;

    @BeforeEach
    void setUp() {
        testSettlementResult = createTestSettlementResult();
        testRepayment = createTestRepayment();
    }

    @Test
    void shouldAutoSettleLoanSuccessfully() throws Exception {
        // Given
        when(loanSettlementService.autoSettleLoan(eq(1L), eq(1001L)))
                .thenReturn(testSettlementResult);

        // When & Then
        mockMvc.perform(post("/api/loan-settlement/auto-settle")
                        .param("reimburseId", "1")
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("核销成功"))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void shouldReturnError_whenAutoSettleFails() throws Exception {
        // Given
        LoanSettlementService.SettlementResult failResult = new LoanSettlementService.SettlementResult();
        failResult.setSuccess(false);
        failResult.setMessage("无可核销借款");
        
        when(loanSettlementService.autoSettleLoan(eq(1L), eq(1001L)))
                .thenReturn(failResult);

        // When & Then
        mockMvc.perform(post("/api/loan-settlement/auto-settle")
                        .param("reimburseId", "1")
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("无可核销借款"));
    }

    @Test
    void shouldManualSettleLoanSuccessfully() throws Exception {
        // Given
        when(loanSettlementService.manualSettleLoan(eq(1L), anyList(), anyMap(), eq(1001L)))
                .thenReturn(testSettlementResult);

        Map<String, Object> request = Map.of(
                "reimburseId", 1L,
                "loanIds", Arrays.asList(1L, 2L),
                "settleAmounts", Map.of("1", 500.00, "2", 300.00),
                "userId", 1001L
        );

        // When & Then
        mockMvc.perform(post("/api/loan-settlement/manual-settle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    void shouldGetAvailableSettlementSuccessfully() throws Exception {
        // Given
        LoanSettlementService.AvailableSettlement available = new LoanSettlementService.AvailableSettlement();
        available.setAvailableAmount(new BigDecimal("2000.00"));
        available.setMaxSettleAmount(new BigDecimal("1500.00"));
        available.setLoanCount(3);
        
        when(loanSettlementService.calculateAvailableSettlement(eq(1001L), eq(new BigDecimal("3000.00"))))
                .thenReturn(available);

        // When & Then
        mockMvc.perform(get("/api/loan-settlement/available-settlement")
                        .param("userId", "1001")
                        .param("reimburseAmount", "3000.00"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.availableAmount").value(2000.00))
                .andExpect(jsonPath("$.data.loanCount").value(3));
    }

    @Test
    void shouldGetSettlementHistorySuccessfully() throws Exception {
        // Given
        List List<LoanRepayment> history = Arrays.asList(testRepayment, createTestRepayment(2L));
        when(loanSettlementService.getSettlementHistory(eq(1L))).thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/loan-settlement/settlement-history/{loanId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void shouldCheckLoanOverdueSuccessfully() throws Exception {
        // Given
        LoanSettlementService.OverdueStatus status = new LoanSettlementService.OverdueStatus();
        status.setOverdue(true);
        status.setOverdueDays(15);
        status.setOverdueAmount(new BigDecimal("500.00"));
        
        when(loanSettlementService.checkLoanOverdue(eq(1L))).thenReturn(status);

        // When & Then
        mockMvc.perform(get("/api/loan-settlement/overdue-status/{loanId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.overdue").value(true))
                .andExpect(jsonPath("$.data.overdueDays").value(15));
    }

    @Test
    void shouldSendLoanRemindersSuccessfully() throws Exception {
        // Given
        LoanSettlementService.ReminderResult reminderResult = new LoanSettlementService.ReminderResult();
        reminderResult.setSuccess(true);
        reminderResult.setMessage("提醒发送成功");
        reminderResult.setSentCount(3);
        
        when(loanSettlementService.sendLoanReminders(anyList())).thenReturn(reminderResult);

        List<Long> loanIds = Arrays.asList(1L, 2L, 3L);

        // When & Then
        mockMvc.perform(post("/api/loan-settlement/send-reminders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.sentCount").value(3));
    }

    @Test
    void shouldBatchCheckLoanOverdueSuccessfully() throws Exception {
        // Given
        LoanSettlementService.OverdueStatus status1 = new LoanSettlementService.OverdueStatus();
        status1.setOverdue(false);
        LoanSettlementService.OverdueStatus status2 = new LoanSettlementService.OverdueStatus();
        status2.setOverdue(true);
        status2.setOverdueDays(10);
        
        when(loanSettlementService.checkLoanOverdue(eq(1L))).thenReturn(status1);
        when(loanSettlementService.checkLoanOverdue(eq(2L))).thenReturn(status2);

        List<Long> loanIds = Arrays.asList(1L, 2L);

        // When & Then
        mockMvc.perform(post("/api/loan-settlement/batch-overdue-status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loanIds)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.1.overdue").value(false))
                .andExpect(jsonPath("$.data.2.overdue").value(true));
    }

    @Test
    void shouldGetRepaymentStatisticsSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/loan-settlement/statistics")
                        .param("year", "2025")
                        .param("month", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalRepaymentCount").value(125))
                .andExpect(jsonPath("$.data.totalRepaymentAmount").value(85000.00))
                .andExpect(jsonPath("$.data.settleRatio").value(0.85));
    }

    @Test
    void shouldHandleServerError_whenServiceException() throws Exception {
        // Given
        when(loanSettlementService.autoSettleLoan(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("数据库连接失败"));

        // When & Then
        mockMvc.perform(post("/api/loan-settlement/auto-settle")
                        .param("reimburseId", "1")
                        .param("userId", "1001"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.message").value("自动核销失败: 数据库连接失败"));
    }

    private LoanSettlementService.SettlementResult createTestSettlementResult() {
        LoanSettlementService.SettlementResult result = new LoanSettlementService.SettlementResult();
        result.setSuccess(true);
        result.setMessage("核销成功");
        result.setSettledAmount(new BigDecimal("800.00"));
        result.setSettledLoanIds(Arrays.asList(1L, 2L));
        return result;
    }

    private LoanRepayment createTestRepayment() {
        return createTestRepayment(1L);
    }

    private LoanRepayment createTestRepayment(Long id) {
        LoanRepayment repayment = new LoanRepayment();
        repayment.setId(id);
        repayment.setLoanId(1L);
        repayment.setRepaymentAmount(new BigDecimal("500.00"));
        repayment.setRepaymentTime(LocalDateTime.now());
        repayment.setRepaymentMethod(1);
        repayment.setStatus(1);
        return repayment;
    }
}