package com.ccms.controller;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.service.InvoiceOcrService;
import com.ccms.service.InvoiceVerifyService;
import com.ccms.service.VerifyResult;
import com.ccms.service.VerifyServiceStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

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
 * 发票管理控制器单元测试
 */
@WebMvcTest(InvoiceManagementController.class)
class InvoiceManagementControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InvoiceOcrService invoiceOcrService;

    @MockBean
    private InvoiceVerifyService invoiceVerifyService;

    @Autowired
    private ObjectMapper objectMapper;

    private ExpenseInvoice testInvoice;

    @BeforeEach
    void setUp() {
        testInvoice = createTestInvoice();
    }

    @Test
    void shouldRecognizeSingleInvoiceSuccessfully() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        when(invoiceOcrService.recognizeInvoice(anyString())).thenReturn(testInvoice);
        when(invoiceOcrService.validateOcrResult(any(ExpenseInvoice.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("发票识别成功"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnBadRequest_whenInvalidFileType() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不支持的文件格式，请上传图片文件"));
    }

    @Test
    void shouldReturnBadRequest_whenEmptyFile() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "",
                "image/jpeg",
                new byte[0]
        );

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void shouldRecognizeBatchInvoicesSuccessfully() throws Exception {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "invoice1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "invoice2.png",
                "image/png",
                "test image content 2".getBytes()
        );
        
        List<ExpenseInvoice> recognizedInvoices = Arrays.asList(testInvoice, createTestInvoice());
        when(invoiceOcrService.recognizeInvoicesBatch(anyList())).thenReturn(recognizedInvoices);

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/batch")
                        .file(file1)
                        .file(file2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.successCount").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldReturnBadRequest_whenBatchOcrWithNoFiles() throws Exception {
        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/batch"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请上传至少一张发票图片"));
    }

    @Test
    void shouldReturnBadRequest_whenBatchOcrExceedsLimit() throws Exception {
        // Given - 创建11个文件超过限制
        MockMultipartFile[] files = new MockMultipartFile[11];
        for (int i = 0; i < 11; i++) {
            files[i] = new MockMultipartFile(
                    "files",
                    "invoice" + i + ".jpg",
                    "image/jpeg",
                    ("content" + i).getBytes()
            );
        }

        // When & Then
        var requestBuilder = multipart("/api/invoice/ocr/batch");
        for (MockMultipartFile file : files) {
            requestBuilder.file(file);
        }
        
        mockMvc.perform(requestBuilder)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("单次最多支持10张发票识别"));
    }

    @Test
    void shouldVerifySingleInvoiceSuccessfully() throws Exception {
        // Given
        VerifyResult verifyResult = VerifyResult.success("验真成功");
        when(invoiceVerifyService.verifyInvoice(
                eq("14400123456"),
                eq("0012341"),
                eq("2025-01-15"),
                eq("123456"),
                eq("100.00")
        )).thenReturn(verifyResult);

        Map<String, String> verifyRequest = Map.of(
                "invoiceCode", "14400123456",
                "invoiceNo", "0012341",
                "invoiceDate", "2025-01-15",
                "checkCode", "123456",
                "invoiceAmount", "100.00"
        );

        // When & Then
        mockMvc.perform(post("/api/invoice/verify/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("验真成功"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    void shouldReturnFailure_whenVerifyInvalidInvoice() throws Exception {
        // Given
        VerifyResult verifyResult = VerifyResult.failure("发票信息有误");
        when(invoiceVerifyService.verifyInvoice(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(verifyResult);

        Map<String, String> verifyRequest = Map.of(
                "invoiceCode", "INVALID",
                "invoiceNo", "000000",
                "invoiceDate", "2025-01-15",
                "checkCode", "000000",
                "invoiceAmount", "0.00"
        );

        // When & Then
        mockMvc.perform(post("/api/invoice/verify/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票信息有误"));
    }

    @Test
    void shouldReturnServiceStatusSuccessfully() throws Exception {
        // Given
        InvoiceOcrService.OcrServiceStatus ocrStatus = new InvoiceOcrService.OcrServiceStatus();
        ocrStatus.setAvailable(true);
        ocrStatus.setMessage("OCR服务正常");
        
        VerifyServiceStatus verifyStatus = new VerifyServiceStatus();
        verifyStatus.setAvailable(true);
        verifyStatus.setMessage("验真服务正常");
        
        when(invoiceOcrService.getServiceStatus()).thenReturn(ocrStatus);
        when(invoiceVerifyService.getServiceStatus()).thenReturn(verifyStatus);

        // When & Then
        mockMvc.perform(get("/api/invoice/service/status"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.ocrService").exists())
                .andExpect(jsonPath("$.data.verifyService").exists())
                .andExpect(jsonPath("$.data.ocrService.available").value(true))
                .andExpect(jsonPath("$.data.verifyService.available").value(true));
    }

    @Test
    void shouldHandleOcrValidationFailure() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        ExpenseInvoice invalidInvoice = createTestInvoice();
        when(invoiceOcrService.recognizeInvoice(anyString())).thenReturn(invalidInvoice);
        when(invoiceOcrService.validateOcrResult(any(ExpenseInvoice.class))).thenReturn(false);

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票识别结果验证失败"));
    }

    @Test
    void shouldHandleOcrServiceException() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );
        
        when(invoiceOcrService.recognizeInvoice(anyString()))
                .thenThrow(new RuntimeException("OCR服务异常"));

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票识别失败: OCR服务异常"));
    }

    @Test
    void shouldHandleVerifyServiceException() throws Exception {
        // Given
        when(invoiceVerifyService.verifyInvoice(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("验真服务异常"));

        Map<String, String> verifyRequest = Map.of(
                "invoiceCode", "14400123456",
                "invoiceNo", "0012341",
                "invoiceDate", "2025-01-15",
                "checkCode", "123456",
                "invoiceAmount", "100.00"
        );

        // When & Then
        mockMvc.perform(post("/api/invoice/verify/single")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("发票验真失败: 验真服务异常"));
    }

    @Test
    void shouldFilterInvalidFilesInBatchOcr() throws Exception {
        // Given - 混合有效和无效文件
        MockMultipartFile validFile = new MockMultipartFile(
                "files",
                "invoice.jpg",
                "image/jpeg",
                "valid image content".getBytes()
        );
        MockMultipartFile invalidFile = new MockMultipartFile(
                "files",
                "document.pdf",
                "application/pdf",
                "pdf content".getBytes()
        );
        
        when(invoiceOcrService.recognizeInvoicesBatch(anyList()))
                .thenReturn(Collections.singletonList(testInvoice));

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/batch")
                        .file(validFile)
                        .file(invalidFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.successCount").value(1));
    }

    @Test
    void shouldSupportPngFormat() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.png",
                "image/png",
                "png image content".getBytes()
        );
        
        when(invoiceOcrService.recognizeInvoice(anyString())).thenReturn(testInvoice);
        when(invoiceOcrService.validateOcrResult(any(ExpenseInvoice.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void shouldSupportGifFormat() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "invoice.gif",
                "image/gif",
                "gif image content".getBytes()
        );
        
        when(invoiceOcrService.recognizeInvoice(anyString())).thenReturn(testInvoice);
        when(invoiceOcrService.validateOcrResult(any(ExpenseInvoice.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(multipart("/api/invoice/ocr/single")
                        .file(file))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    /**
     * 创建测试发票
     */
    private ExpenseInvoice createTestInvoice() {
        ExpenseInvoice invoice = new ExpenseInvoice();
        invoice.setId(1L);
        invoice.setInvoiceCode("14400123456");
        invoice.setInvoiceNo("0012341");
        invoice.setInvoiceType(1);
        invoice.setSellerName("北京市某科技有限公司");
        invoice.setSellerTaxNo("91110108MA0112345");
        invoice.setInvoiceAmount(new java.math.BigDecimal("100.00"));
        invoice.setTaxAmount(new java.math.BigDecimal("13.00"));
        return invoice;
    }
}
