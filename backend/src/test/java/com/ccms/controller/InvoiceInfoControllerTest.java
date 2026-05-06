package com.ccms.controller;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.service.InvoiceComplianceService;
import com.ccms.service.InvoiceVerifyService;
import com.ccms.service.VerifyResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
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
 * 发票信息控制器单元测试
 */
@WebMvcTest(InvoiceInfoController.class)
class InvoiceInfoControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceComplianceService invoiceComplianceService;

    @MockBean
    private InvoiceVerifyService invoiceVerifyService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseInvoice testInvoice;

    @BeforeEach
    void setUp() {
        testInvoice = createTestInvoice(1L);
    }

    @Test
    void shouldReturnInvoiceList_whenGetListSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/list")
                        .param("page", "1")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.totalPages").exists())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    void shouldReturnFilteredList_whenQueryWithParameters() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/list")
                        .param("page", "1")
                        .param("size", "20")
                        .param("invoiceNo", "001234")
                        .param("sellerName", "北京")
                        .param("verifyStatus", "1")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnInvoiceDetail_whenGetDetailSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/detail/{invoiceId}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnNotFound_whenInvoiceNotExists() throws Exception {
        // When & Then - 使用999L会触发模拟数据中的null返回
        mockMvc.perform(get("/api/invoice-info/detail/{invoiceId}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCheckComplianceSuccessfully() throws Exception {
        // Given
        InvoiceComplianceService.ComplianceResult complianceResult = 
                new InvoiceComplianceService.ComplianceResult();
        complianceResult.setCompliant(true);
        complianceResult.setMessage("发票合规");
        
        when(invoiceComplianceService.checkSingleInvoice(any(ExpenseInvoice.class), isNull()))
                .thenReturn(complianceResult);

        Map<String, Object> request = Map.of("invoiceId", 1L);

        // When & Then
        mockMvc.perform(post("/api/invoice-info/compliance/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnBadRequest_whenComplianceCheckWithInvalidInvoice() throws Exception {
        // Given
        Map<String, Object> request = Map.of("invoiceId", 999L);

        // When & Then
        mockMvc.perform(post("/api/invoice-info/compliance/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票不存在"));
    }

    @Test
    void shouldReturnStatistics_whenGetStatisticsSuccess() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/statistics"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalInvoices").value(1500))
                .andExpect(jsonPath("$.data.verifiedInvoices").value(1200))
                .andExpect(jsonPath("$.data.complianceRate").value(85.7))
                .andExpect(jsonPath("$.data.typeStatistics").exists())
                .andExpect(jsonPath("$.data.sellerStatistics").exists());
    }

    @Test
    void shouldBatchVerifyInvoicesSuccessfully() throws Exception {
        // Given
        List<Map<String, String>> invoices = Arrays.asList(
                Map.of("invoiceCode", "14400123456", "invoiceNo", "0012341"),
                Map.of("invoiceCode", "14400123457", "invoiceNo", "0012342")
        );
        
        List<VerifyResult> verifyResults = Arrays.asList(
                VerifyResult.success("验真成功"),
                VerifyResult.success("验真成功")
        );
        
        when(invoiceVerifyService.verifyInvoicesBatch(anyList())).thenReturn(verifyResults);

        Map<String, Object> request = Map.of("invoices", invoices);

        // When & Then
        mockMvc.perform(post("/api/invoice-info/batch/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.failedCount").value(0))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenBatchVerifyWithEmptyList() throws Exception {
        // Given
        Map<String, Object> request = Map.of("invoices", Collections.emptyList());

        // When & Then
        mockMvc.perform(post("/api/invoice-info/batch/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请提供需要验真的发票列表"));
    }

    @Test
    void shouldExportInvoiceDataSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/export")
                        .param("format", "excel")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.format").value("excel"))
                .andExpect(jsonPath("$.data.fileName").exists())
                .andExpect(jsonPath("$.data.downloadUrl").exists());
    }

    @Test
    void shouldExportWithDefaultFormat_whenFormatNotSpecified() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/export"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.format").value("excel"));
    }

    @Test
    void shouldExportWithCsvFormat_whenSpecified() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/invoice-info/export")
                        .param("format", "csv"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.format").value("csv"));
    }

    @Test
    void shouldUpdateRemarkSuccessfully() throws Exception {
        // Given
        Map<String, String> request = Map.of("remark", "测试备注更新");

        // When & Then
        mockMvc.perform(put("/api/invoice-info/remark/{invoiceId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("备注更新成功"));
    }

    @Test
    void shouldReturnNotFound_whenUpdateRemarkForNonExistentInvoice() throws Exception {
        // Given
        Map<String, String> request = Map.of("remark", "测试备注");

        // When & Then
        mockMvc.perform(put("/api/invoice-info/remark/{invoiceId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票不存在"));
    }

    @Test
    void shouldHandleServerError_whenUnexpectedException() throws Exception {
        // Given - 触发异常的情况
        when(invoiceVerifyService.verifyInvoicesBatch(anyList()))
                .thenThrow(new RuntimeException("服务异常"));

        List<Map<String, String>> invoices = Collections.singletonList(
                Map.of("invoiceCode", "14400123456", "invoiceNo", "0012341")
        );
        Map<String, Object> request = Map.of("invoices", invoices);

        // When & Then
        mockMvc.perform(post("/api/invoice-info/batch/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }

    /**
     * 创建测试发票
     */
    private ExpenseInvoice createTestInvoice(Long id) {
        ExpenseInvoice invoice = new ExpenseInvoice();
        invoice.setId(id);
        invoice.setInvoiceCode("14400123456" + id);
        invoice.setInvoiceNo("001234" + id);
        invoice.setInvoiceType(1);
        invoice.setSellerName("北京市某科技有限公司");
        invoice.setSellerTaxNo("91110108MA01" + id);
        invoice.setInvoiceAmount(new BigDecimal("100.00"));
        invoice.setTaxAmount(new BigDecimal("13.00"));
        invoice.setInvoiceDate(Date.valueOf(LocalDate.now()));
        invoice.setVerifyStatus(1);
        return invoice;
    }
}
