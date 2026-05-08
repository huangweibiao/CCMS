package com.ccms.controller.invoice;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.invoice.InvoiceInfo;
import com.ccms.repository.invoice.InvoiceInfoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 发票管理控制器单元测试
 */
@WebMvcTest(InvoiceManagementController.class)
class InvoiceManagementControllerTest extends ControllerTestBase {

    @MockBean
    private InvoiceInfoRepository invoiceInfoRepository;

    private InvoiceInfo createTestInvoice(Long id, String invoiceNo, Integer status) {
        InvoiceInfo invoice = new InvoiceInfo();
        invoice.setId(id);
        invoice.setInvoiceNo(invoiceNo);
        invoice.setInvoiceCode("CODE" + id);
        invoice.setInvoiceDate(LocalDate.now());
        invoice.setInvoiceAmount(new BigDecimal("1000.00"));
        invoice.setInvoiceType(2);
        invoice.setVerificationStatus(status);
        return invoice;
    }

    @Test
    void shouldOcrInvoiceSuccess() throws Exception {
        InvoiceInfo invoice = createTestInvoice(1L, "OCR123456", 0);
        when(invoiceInfoRepository.save(any(InvoiceInfo.class))).thenReturn(invoice);

        MockMultipartFile file = new MockMultipartFile(
                "file", "invoice.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/invoice/ocr")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("OCR识别成功"));
    }

    @Test
    void shouldOcrInvoiceFail() throws Exception {
        when(invoiceInfoRepository.save(any(InvoiceInfo.class)))
                .thenThrow(new RuntimeException("保存失败"));

        MockMultipartFile file = new MockMultipartFile(
                "file", "invoice.jpg", "image/jpeg", "image content".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/invoice/ocr")
                        .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldBatchOcrInvoiceSuccess() throws Exception {
        InvoiceInfo invoice = createTestInvoice(1L, "OCR123456", 0);
        when(invoiceInfoRepository.save(any(InvoiceInfo.class))).thenReturn(invoice);

        MockMultipartFile file1 = new MockMultipartFile(
                "files", "invoice1.jpg", "image/jpeg", "image content 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "invoice2.jpg", "image/jpeg", "image content 2".getBytes());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .multipart("/api/invoice/ocr/batch")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    void shouldVerifyInvoiceSuccess() throws Exception {
        InvoiceInfo invoice = createTestInvoice(1L, "INV001", 0);
        when(invoiceInfoRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(invoiceInfoRepository.save(any(InvoiceInfo.class))).thenReturn(invoice);

        performPost("/api/invoice/verify/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.verificationStatus").value(1))
                .andExpect(jsonPath("$.message").value("发票验真通过"));
    }

    @Test
    void shouldReturnBadRequestWhenVerifyNonExistentInvoice() throws Exception {
        when(invoiceInfoRepository.findById(999L)).thenReturn(Optional.empty());

        performPost("/api/invoice/verify/999")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票不存在"));
    }

    @Test
    void shouldBatchVerifyInvoiceSuccess() throws Exception {
        InvoiceInfo invoice1 = createTestInvoice(1L, "INV001", 0);
        InvoiceInfo invoice2 = createTestInvoice(2L, "INV002", 0);
        when(invoiceInfoRepository.findById(1L)).thenReturn(Optional.of(invoice1));
        when(invoiceInfoRepository.findById(2L)).thenReturn(Optional.of(invoice2));
        when(invoiceInfoRepository.save(any(InvoiceInfo.class))).thenReturn(invoice1, invoice2);

        performPost("/api/invoice/verify/batch", Arrays.asList(1L, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.totalCount").value(2));
    }

    @Test
    void shouldReturnUnverifiedInvoices() throws Exception {
        InvoiceInfo invoice = createTestInvoice(1L, "INV001", 0);
        when(invoiceInfoRepository.findUnverifiedInvoices())
                .thenReturn(Collections.singletonList(invoice));

        performGet("/api/invoice/unverified")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void shouldCheckDuplicateWhenExists() throws Exception {
        InvoiceInfo invoice = createTestInvoice(1L, "INV001", 0);
        when(invoiceInfoRepository.findByInvoiceNo("INV001")).thenReturn(Optional.of(invoice));

        performGet("/api/invoice/check-duplicate?invoiceNo=INV001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true))
                .andExpect(jsonPath("$.existingInvoice.invoiceNo").value("INV001"));
    }

    @Test
    void shouldCheckDuplicateWhenNotExists() throws Exception {
        when(invoiceInfoRepository.findByInvoiceNo("INV999")).thenReturn(Optional.empty());

        performGet("/api/invoice/check-duplicate?invoiceNo=INV999")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(false));
    }

    @Test
    void shouldCheckDuplicateWithCode() throws Exception {
        InvoiceInfo invoice = createTestInvoice(1L, "INV001", 0);
        invoice.setInvoiceCode("CODE001");
        when(invoiceInfoRepository.findByInvoiceNoAndCode("INV001", "CODE001"))
                .thenReturn(Optional.of(invoice));

        performGet("/api/invoice/check-duplicate?invoiceNo=INV001&invoiceCode=CODE001")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duplicate").value(true));
    }

    @Test
    void shouldReturnInvoiceStatistics() throws Exception {
        when(invoiceInfoRepository.count()).thenReturn(100L);
        when(invoiceInfoRepository.countByVerificationStatus(0)).thenReturn(30L);
        when(invoiceInfoRepository.countByVerificationStatus(1)).thenReturn(60L);
        when(invoiceInfoRepository.countByVerificationStatus(2)).thenReturn(10L);

        performGet("/api/invoice/statistics")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(100))
                .andExpect(jsonPath("$.unverified").value(30))
                .andExpect(jsonPath("$.verified").value(60))
                .andExpect(jsonPath("$.verifyFailed").value(10));
    }
}
