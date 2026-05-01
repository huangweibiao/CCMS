package com.ccms.controller;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.service.ExpenseReimburseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 费用报销控制器集成测试
 */
class ExpenseReimburseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExpenseReimburseService expenseReimburseService;

    @InjectMocks
    private ExpenseReimburseController expenseReimburseController;

    private ObjectMapper objectMapper;

    private ExpenseReimburse testReimburse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(expenseReimburseController).build();
        objectMapper = new ObjectMapper();

        testReimburse = new ExpenseReimburse();
        testReimburse.setId(1L);
        testReimburse.setReimburseNo("REIMBURSE_001");
        testReimburse.setTitle("测试报销");
        testReimburse.setApplyUserId(1001L);
        testReimburse.setApplyUserName("测试用户");
        testReimburse.setDeptId(1L);
        testReimburse.setDeptName("测试部门");
        testReimburse.setReimburseDate(LocalDate.now());
        testReimburse.setTotalAmount(new BigDecimal("1000.00"));
        testReimburse.setStatus(0);
        testReimburse.setApprovalStatus(0);
    }

    @Test
    void testGetExpenseReimburseList_Success() throws Exception {
        Page<ExpenseReimburse> page = new PageImpl<>(Collections.singletonList(testReimburse), PageRequest.of(0, 20), 1);
        
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.getExpenseReimburseList(anyInt(), anyInt(), any(), any(), any(), any()
        )).thenAnswer(invocation -> {
            int pageNum = invocation.getArgument(0);
            int pageSize = invocation.getArgument(1);
            return new PageImpl<>(Collections.singletonList(testReimburse), PageRequest.of(pageNum, pageSize), 1);
        });
        
        mockMvc.perform(get("/api/expense-reimburses")
                .header("Authorization", "Bearer token123")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].reimburseNo", is("REIMBURSE_001")));
    }

    @Test
    void testGetExpenseReimburseDetail_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.getExpenseReimburseById(1L)).thenReturn(testReimburse);
        
        mockMvc.perform(get("/api/expense-reimburses/1")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.reimburseNo", is("REIMBURSE_001")))
                .andExpect(jsonPath("$.title", is("测试报销")));
    }

    @Test
    void testGetExpenseReimburseDetail_NotFound() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.getExpenseReimburseById(999L)).thenReturn(null);
        
        mockMvc.perform(get("/api/expense-reimburses/999")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateExpenseReimburse_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        doNothing().when(expenseReimburseService).createExpenseReimburse(any(ExpenseReimburse.class));
        
        mockMvc.perform(post("/api/expense-reimburses")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReimburse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("费用报销创建成功")));
    }

    @Test
    void testCreateExpenseReimburse_Failure() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        doThrow(new RuntimeException("创建失败")).when(expenseReimburseService).createExpenseReimburse(any(ExpenseReimburse.class));
        
        mockMvc.perform(post("/api/expense-reimburses")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReimburse)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("费用报销创建失败")));
    }

    @Test
    void testUpdateExpenseReimburse_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        doNothing().when(expenseReimburseService).updateExpenseReimburse(any(ExpenseReimburse.class));
        
        mockMvc.perform(put("/api/expense-reimburses/1")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testReimburse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("费用报销更新成功")));
    }

    @Test
    void testDeleteExpenseReimburse_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.deleteExpenseReimburse(1L)).thenReturn(true);
        
        mockMvc.perform(delete("/api/expense-reimburses/1")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("费用报销删除成功")));
    }

    @Test
    void testDeleteExpenseReimburse_Failure() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.deleteExpenseReimburse(1L)).thenReturn(false);
        
        mockMvc.perform(delete("/api/expense-reimburses/1")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("费用报销删除失败")));
    }

    @Test
    void testSubmitExpenseReimburse_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        doNothing().when(expenseReimburseService).submitExpenseReimburseForApproval(1L);
        
        mockMvc.perform(post("/api/expense-reimburses/1/submit")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("费用报销提交审批成功")));
    }

    @Test
    void testApproveExpenseReimburse_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.approveExpenseReimburse(anyLong(), anyLong(), anyInt(), anyString())).thenReturn(true);
        
        Map<String, Object> approval = new HashMap<>();
        approval.put("result", 1);
        approval.put("comment", "审批通过");
        approval.put("approverId", 1001L);
        
        mockMvc.perform(post("/api/expense-reimburses/1/approve")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(approval)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("费用报销审批完成")));
    }

    @Test
    void testLinkApply_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.linkToExpenseApply(1L, 1001L)).thenReturn(true);
        
        Map<String, Long> apply = new HashMap<>();
        apply.put("applyId", 1001L);
        
        mockMvc.perform(post("/api/expense-reimburses/1/link-apply")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apply)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("申请单关联成功")));
    }

    @Test
    void testProcessPayment_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        doNothing().when(expenseReimburseService).processPayment(anyLong(), anyLong(), anyString(), anyString());
        
        Map<String, Object> payment = new HashMap<>();
        payment.put("paymentMethod", "现金");
        payment.put("paymentInfo", "支付凭证001");
        payment.put("processorId", 1001L);
        
        mockMvc.perform(post("/api/expense-reimburses/1/process-payment")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("报销支付处理成功")));
    }

    @Test
    void testGetExpenseReimburseStatistics_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", 10);
        stats.put("totalAmount", 50000.0);
        stats.put("approvedCount", 8);
        stats.put("year", 2024);
        
        when(expenseReimburseService.getExpenseReimburseStatistics(anyLong(), anyLong(), anyInt())).thenReturn(stats);
        
        mockMvc.perform(get("/api/expense-reimburses/statistics")
                .header("Authorization", "Bearer token123")
                .param("year", "2024"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount", is(10)))
                .andExpect(jsonPath("$.year", is(2024)));
    }

    @Test
    void testUploadVoucher_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.uploadVoucher(anyLong(), anyString(), anyString(), anyString())).thenReturn(true);
        
        Map<String, Object> voucher = new HashMap<>();
        voucher.put("voucherType", "发票");
        voucher.put("voucherUrl", "/uploads/voucher001.jpg");
        voucher.put("fileName", "voucher001.jpg");
        
        mockMvc.perform(post("/api/expense-reimburses/1/upload-voucher")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(voucher)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("报销凭证上传成功")));
    }

    @Test
    void testProcessRefund_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        when(expenseReimburseService.processRefund(anyLong(), anyLong(), anyDouble(), anyString())).thenReturn(true);
        
        Map<String, Object> refund = new HashMap<>();
        refund.put("refundAmount", 100.0);
        refund.put("refundReason", "多余报销款");
        refund.put("processorId", 1001L);
        
        mockMvc.perform(post("/api/expense-reimburses/1/process-refund")
                .header("Authorization", "Bearer token123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refund)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("报销退款处理成功")));
    }

    @Test
    void testGetStatusTracking_Success() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(true);
        
        List<Map<String, Object>> tracking = List.of(
            Map.of("status", "created", "time", "2024-01-01T10:00:00", "operator", "system")
        );
        
        when(expenseReimburseService.getStatusTracking(1L)).thenReturn(tracking);
        
        mockMvc.perform(get("/api/expense-reimburses/1/status-tracking")
                .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status", is("created")));
    }

    @Test
    void testAuthorization_Unauthorized() throws Exception {
        when(expenseReimburseService.checkPermission(anyString(), anyString())).thenReturn(false);
        
        mockMvc.perform(get("/api/expense-reimburses")
                .header("Authorization", "Bearer invalidtoken"))
                .andExpect(status().isBadRequest());
    }
}