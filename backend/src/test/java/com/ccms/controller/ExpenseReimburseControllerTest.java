package com.ccms.controller;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.service.ExpenseReimburseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用报销控制器单元测试
 */
@WebMvcTest(ExpenseReimburseController.class)
class ExpenseReimburseControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseReimburseService expenseReimburseService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseReimburse testExpenseReimburse;
    private Page Page<ExpenseReimburse> testPage;
    private static final String AUTH_TOKEN = "Bearer test-token";

    @BeforeEach
    void setUp() {
        testExpenseReimburse = createTestExpenseReimburse();
        testPage = new PageImpl<>(
                List.of(testExpenseReimburse),
                PageRequest.of(0, 20),
                1
        );
    }

    @Test
    void shouldReturnExpenseReimburseList_whenGetListSuccess() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:list"));
        when(expenseReimburseService.getExpenseReimburseList(eq(0), eq(20), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(testPage);

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses")
                        .header("Authorization", AUTH_TOKEN)
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].reimburseNo").value("RB20250001"));
    }

    @Test
    void shouldReturnFilteredList_whenQueryWithParameters() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:list"));
        when(expenseReimburseService.getExpenseReimburseList(eq(0), eq(20), eq(1001L), eq(1L), eq(1), eq(2025)))
                .thenReturn(testPage);

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses")
                        .header("Authorization", AUTH_TOKEN)
                        .param("page", "0")
                        .param("size", "20")
                        .param("applicantId", "1001")
                        .param("deptId", "1")
                        .param("status", "1")
                        .param("year", "2025"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").exists());
    }

    @Test
    void shouldReturnExpenseReimburseDetail_whenGetDetailSuccess() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:view"));
        when(expenseReimburseService.getExpenseReimburseById(eq(1L))).thenReturn(testExpenseReimburse);

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses/{reimburseId}", 1L)
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reimburseNo").value("RB20250001"))
                .andExpect(jsonPath("$.title").value("差旅费报销"));
    }

    @Test
    void shouldCreateExpenseReimburseSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:create"));
        doNothing().when(expenseReimburseService).createExpenseReimburse(any(ExpenseReimburse.class));

        ExpenseReimburse newReimburse = createTestExpenseReimburse();
        newReimburse.setId(null);

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReimburse)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用报销创建成功"));
    }

    @Test
    void shouldReturnBadRequest_whenCreateExpenseReimburseFails() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:create"));
        doThrow(new RuntimeException("报销单号已存在")).when(expenseReimburseService).createExpenseReimburse(any(ExpenseReimburse.class));

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testExpenseReimburse)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void shouldUpdateExpenseReimburseSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:update"));
        doNothing().when(expenseReimburseService).updateExpenseReimburse(any(ExpenseReimburse.class));

        ExpenseReimburse updateReimburse = createTestExpenseReimburse();
        updateReimburse.setTitle("更新后的报销标题");

        // When & Then
        mockMvc.perform(put("/api/expense-reimburses/{reimburseId}", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReimburse)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用报销更新成功"));
    }

    @Test
    void shouldDeleteExpenseReimburseSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:delete"));
        doNothing().when(expenseReimburseService).deleteExpenseReimburse(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/expense-reimburses/{reimburseId}", 1L)
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用报销删除成功"));
    }

    @Test
    void shouldSubmitExpenseReimburseForApproval() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:submit"));
        doNothing().when(expenseReimburseService).submitExpenseReimburseForApproval(eq(1L));

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/submit", 1L)
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用报销提交审批成功"));
    }

    @Test
    void shouldApproveExpenseReimburseSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:approve"));
        doNothing().when(expenseReimburseService).approveExpenseReimburse(eq(1L), eq(1002L), eq(1), eq("同意报销"));

        Map<String, Object> approval = Map.of(
                "result", 1,
                "comment", "同意报销",
                "approverId", 1002L
        );

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/approve", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approval)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用报销审批完成"));
    }

    @Test
    void shouldRejectExpenseReimburseSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:approve"));
        doNothing().when(expenseReimburseService).approveExpenseReimburse(eq(1L), eq(1002L), eq(0), eq("发票不合规"));

        Map<String, Object> approval = Map.of(
                "result", 0,
                "comment", "发票不合规",
                "approverId", 1002L
        );

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/approve", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(approval)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("费用报销审批完成"));
    }

    @Test
    void shouldLinkApplySuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:link-apply"));
        doNothing().when(expenseReimburseService).linkToExpenseApply(eq(1L), eq(100L));

        Map<String, Long> applyLink = Map.of("applyId", 100L);

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/link-apply", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyLink)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("申请单关联成功"));
    }

    @Test
    void shouldProcessPaymentSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:process-payment"));
        doNothing().when(expenseReimburseService).processPayment(eq(1L), eq(1003L), eq("银行转账"), eq("转账单号:TR20250001"));

        Map<String, Object> payment = Map.of(
                "paymentMethod", "银行转账",
                "paymentInfo", "转账单号:TR20250001",
                "processorId", 1003L
        );

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/process-payment", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("报销支付处理成功"));
    }

    @Test
    void shouldReturnStatistics_whenGetStatisticsSuccess() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:statistics"));
        Map<String, Object> statistics = Map.of(
                "totalCount", 100,
                "totalAmount", 500000.00,
                "approvedCount", 80,
                "pendingCount", 20
        );
        when(expenseReimburseService.getExpenseReimburseStatistics(isNull(), isNull(), eq(2025)))
                .thenReturn(statistics);

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses/statistics")
                        .header("Authorization", AUTH_TOKEN)
                        .param("year", "2025"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100))
                .andExpect(jsonPath("$.totalAmount").value(500000.00));
    }

    @Test
    void shouldUploadVoucherSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:upload-voucher"));
        doNothing().when(expenseReimburseService).uploadVoucher(eq(1L), eq("invoice"), eq("https://example.com/voucher.pdf"), eq("发票凭证.pdf"));

        Map<String, Object> voucher = Map.of(
                "voucherType", "invoice",
                "voucherUrl", "https://example.com/voucher.pdf",
                "fileName", "发票凭证.pdf"
        );

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/upload-voucher", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(voucher)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("报销凭证上传成功"));
    }

    @Test
    void shouldReturnDownloadUrl_whenDownloadVoucher() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:download-voucher"));
        when(expenseReimburseService.getVoucherDownloadUrl(eq(1L)))
                .thenReturn("https://example.com/download/voucher.pdf");

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses/{reimburseId}/download-voucher", 1L)
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadUrl").value("https://example.com/download/voucher.pdf"));
    }

    @Test
    void shouldProcessRefundSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:process-refund"));
        doNothing().when(expenseReimburseService).processRefund(eq(1L), eq(1004L), eq(500.00), eq("多报销退款"));

        Map<String, Object> refund = Map.of(
                "refundAmount", 500.00,
                "refundReason", "多报销退款",
                "processorId", 1004L
        );

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/process-refund", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refund)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("报销退款处理成功"));
    }

    @Test
    void shouldReturnStatusTracking_whenGetStatusTracking() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:view"));
        List<Map<String, Object>> trackingList = Collections.singletonList(
                Map.of("status", "已提交", "time", "2025-01-15 10:00:00", "operator", "张三")
        );
        when(expenseReimburseService.getStatusTracking(eq(1L))).thenReturn(trackingList);

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses/{reimburseId}/status-tracking", 1L)
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("已提交"));
    }

    @Test
    void shouldHandleUrgentProcessingSuccessfully() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:urgent-processing"));
        doNothing().when(expenseReimburseService).urgentProcessing(eq(1L), eq(1005L), eq("客户紧急需求，需要加急处理"));

        Map<String, Object> urgentInfo = Map.of(
                "urgentReason", "客户紧急需求，需要加急处理",
                "processorId", 1005L
        );

        // When & Then
        mockMvc.perform(post("/api/expense-reimburses/{reimburseId}/urgent-processing", 1L)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urgentInfo)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("紧急报销处理成功"));
    }

    @Test
    void shouldHandlePermissionDenied() throws Exception {
        // Given
        doThrow(new RuntimeException("权限不足")).when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:list"));

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses")
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleEmptyList() throws Exception {
        // Given
        doNothing().when(expenseReimburseService).checkPermission(anyString(), eq("expense-reimburse:list"));
        Page Page<ExpenseReimburse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
        when(expenseReimburseService.getExpenseReimburseList(anyInt(), anyInt(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/api/expense-reimburses")
                        .header("Authorization", AUTH_TOKEN))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    /**
     * 创建测试费用报销记录
     */
    private ExpenseReimburse createTestExpenseReimburse() {
        ExpenseReimburse reimburse = new ExpenseReimburse();
        reimburse.setId(1L);
        reimburse.setReimburseNo("RB20250001");
        reimburse.setTitle("差旅费报销");
        reimburse.setApplyUserId(1001L);
        reimburse.setApplyUserName("张三");
        reimburse.setDeptId(1L);
        reimburse.setDeptName("技术部");
        reimburse.setReimburseDate(LocalDate.now());
        reimburse.setTotalAmount(new BigDecimal("3500.00"));
        reimburse.setCurrency("CNY");
        reimburse.setStatus(1);
        reimburse.setApprovalStatus(0);
        reimburse.setBankAccount("6222021234567890123");
        reimburse.setBankName("中国工商银行");
        reimburse.setCreateTime(LocalDateTime.now());
        reimburse.setUpdateTime(LocalDateTime.now());
        return reimburse;
    }
}