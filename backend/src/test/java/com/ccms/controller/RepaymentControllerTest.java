package com.ccms.controller;

import com.ccms.dto.RepaymentRequest;
import com.ccms.dto.RepaymentResponse;
import com.ccms.service.RepaymentManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 还款管理控制器单元测试
 */
@WebMvcTest(RepaymentController.class)
class RepaymentControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RepaymentManagementService repaymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private RepaymentResponse testRepaymentResponse;
    private RepaymentRequest testRepaymentRequest;

    @BeforeEach
    void setUp() {
        testRepaymentResponse = createTestRepaymentResponse();
        testRepaymentRequest = createTestRepaymentRequest();
    }

    @Test
    void shouldCreateRepaymentSuccessfully() throws Exception {
        // Given
        when(repaymentService.createRepayment(any(RepaymentRequest.class)))
                .thenReturn(testRepaymentResponse);

        // When & Then
        mockMvc.perform(post("/api/repayments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRepaymentRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loanId").value(100))
                .andExpect(jsonPath("$.amount").value(5000.00));
    }

    @Test
    void shouldGetRepaymentByIdSuccessfully() throws Exception {
        // Given
        when(repaymentService.getRepaymentById(eq(1L)))
                .thenReturn(testRepaymentResponse);

        // When & Then
        mockMvc.perform(get("/api/repayments/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void shouldUpdateRepaymentSuccessfully() throws Exception {
        // Given
        RepaymentResponse updatedResponse = createTestRepaymentResponse();
        updatedResponse.setAmount(new BigDecimal("6000.00"));
        when(repaymentService.updateRepayment(any(RepaymentRequest.class)))
                .thenReturn(updatedResponse);

        RepaymentRequest updateRequest = createTestRepaymentRequest();
        updateRequest.setAmount(new BigDecimal("6000.00"));

        // When & Then
        mockMvc.perform(put("/api/repayments/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(6000.00));
    }

    @Test
    void shouldDeleteRepaymentSuccessfully() throws Exception {
        // Given
        doNothing().when(repaymentService).deleteRepayment(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/repayments/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetRepaymentsByLoanIdSuccessfully() throws Exception {
        // Given
        List List<RepaymentResponse> repayments = Arrays.asList(
                testRepaymentResponse,
                createTestRepaymentResponse(2L)
        );
        when(repaymentService.getRepaymentsByLoanId(eq(100L)))
                .thenReturn(repayments);

        // When & Then
        mockMvc.perform(get("/api/repayments/loan/{loanId}", 100L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldGetRepaymentsWithPaginationSuccessfully() throws Exception {
        // Given
        Page Page<RepaymentResponse> page = new PageImpl<>(
                Arrays.asList(testRepaymentResponse, createTestRepaymentResponse(2L)),
                PageRequest.of(0, 10),
                2
        );
        when(repaymentService.findRepayments(any(Pageable.class)))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/repayments")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldGetRepaymentsByStatusSuccessfully() throws Exception {
        // Given
        List List<RepaymentResponse> repayments = Collections.singletonList(testRepaymentResponse);
        when(repaymentService.findRepaymentsByStatus(eq(1)))
                .thenReturn(repayments);

        // When & Then
        mockMvc.perform(get("/api/repayments/status/{status}", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value(1));
    }

    @Test
    void shouldConfirmRepaymentSuccessfully() throws Exception {
        // Given
        RepaymentResponse confirmedResponse = createTestRepaymentResponse();
        confirmedResponse.setStatus(2); // 已确认状态
        when(repaymentService.confirmRepayment(eq(1L)))
                .thenReturn(confirmedResponse);

        // When & Then
        mockMvc.perform(post("/api/repayments/{id}/confirm", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(2));
    }

    @Test
    void shouldCancelRepaymentSuccessfully() throws Exception {
        // Given
        RepaymentResponse cancelledResponse = createTestRepaymentResponse();
        cancelledResponse.setStatus(0); // 已取消状态
        when(repaymentService.cancelRepayment(eq(1L)))
                .thenReturn(cancelledResponse);

        // When & Then
        mockMvc.perform(post("/api/repayments/{id}/cancel", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(0));
    }

    @Test
    void shouldBatchConfirmRepaymentsSuccessfully() throws Exception {
        // Given
        List<Long> repaymentIds = Arrays.asList(1L, 2L, 3L);
        doNothing().when(repaymentService).batchConfirmRepayments(eq(repaymentIds));

        // When & Then
        mockMvc.perform(post("/api/repayments/batch-confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repaymentIds)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetUserRepaymentsSuccessfully() throws Exception {
        // Given
        List List<RepaymentResponse> repayments = Arrays.asList(
                testRepaymentResponse,
                createTestRepaymentResponse(2L)
        );
        when(repaymentService.findRepaymentsByUserId(eq(1001L)))
                .thenReturn(repayments);

        // When & Then
        mockMvc.perform(get("/api/repayments/user/{userId}", 1001L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldAutoWriteOffSuccessfully() throws Exception {
        // Given
        RepaymentResponse writeOffResponse = createTestRepaymentResponse();
        writeOffResponse.setRemark("自动核销");
        when(repaymentService.autoWriteOff(eq(100L)))
                .thenReturn(writeOffResponse);

        // When & Then
        mockMvc.perform(post("/api/repayments/{loanId}/auto-write-off", 100L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("自动核销"));
    }

    @Test
    void shouldGetLoanRepaymentStatsSuccessfully() throws Exception {
        // Given
        Object stats = createTestStats();
        when(repaymentService.getLoanRepaymentStats(eq(100L)))
                .thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/repayments/stats/loan/{loanId}", 100L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.repaidAmount").exists());
    }

    @Test
    void shouldGetUserRepaymentStatsSuccessfully() throws Exception {
        // Given
        Object stats = createTestStats();
        when(repaymentService.getUserRepaymentStats(eq(1001L)))
                .thenReturn(stats);

        // When & Then
        mockMvc.perform(get("/api/repayments/stats/user/{userId}", 1001L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").exists())
                .andExpect(jsonPath("$.repaidAmount").exists());
    }

    private RepaymentResponse createTestRepaymentResponse() {
        return createTestRepaymentResponse(1L);
    }

    private RepaymentResponse createTestRepaymentResponse(Long id) {
        RepaymentResponse response = new RepaymentResponse();
        response.setId(id);
        response.setLoanId(100L);
        response.setUserId(1001L);
        response.setAmount(new BigDecimal("5000.00"));
        response.setRepaymentDate(LocalDateTime.now());
        response.setStatus(1);
        response.setRepaymentMethod("BANK_TRANSFER");
        response.setRemark("测试还款");
        return response;
    }

    private RepaymentRequest createTestRepaymentRequest() {
        RepaymentRequest request = new RepaymentRequest();
        request.setLoanId(100L);
        request.setUserId(1001L);
        request.setAmount(new BigDecimal("5000.00"));
        request.setRepaymentMethod("BANK_TRANSFER");
        request.setRemark("测试还款");
        return request;
    }

    private Object createTestStats() {
        return new Object() {
            public final BigDecimal totalAmount = new BigDecimal("10000.00");
            public final BigDecimal repaidAmount = new BigDecimal("5000.00");
            public final BigDecimal remainingAmount = new BigDecimal("5000.00");
            public final int repaymentCount = 2;
        };
    }
}