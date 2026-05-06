package com.ccms.controller.export;

import com.ccms.controller.BaseControllerTest;
import com.ccms.entity.report.ReportTemplate;
import com.ccms.service.export.ReportExportService;
import com.ccms.service.report.ReportTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报表导出控制器单元测试
 */
@WebMvcTest(ReportExportController.class)
class ReportExportControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportExportService reportExportService;

    @MockBean
    private ReportTemplateService reportTemplateService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReportTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = createTestTemplate();
    }

    @Test
    void shouldGenerateShareLinkSuccessfully() throws Exception {
        // Given
        when(reportTemplateService.getTemplateByCode(eq("EXPENSE_REPORT"))).thenReturn(testTemplate);
        when(reportExportService.generateShareLink(any(), anyMap(), any())).thenReturn("https://example.com/share/abc123");

        Map<String, Object> request = new HashMap<>();
        request.put("params", new HashMap<>());
        request.put("expireHours", 24);

        // When & Then
        mockMvc.perform(post("/api/export/share/{templateCode}", "EXPENSE_REPORT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("https://example.com/share/abc123"));
    }

    @Test
    void shouldGetSharedReportSuccessfully() throws Exception {
        // Given
        when(reportExportService.isShareValid(eq("abc123"))).thenReturn(true);
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("reportName", "费用报表");
        when(reportExportService.getSharedReport(eq("abc123"))).thenReturn(shareData);

        // When & Then
        mockMvc.perform(get("/api/export/share/data/{shareToken}", "abc123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.reportName").value("费用报表"));
    }

    @Test
    void shouldReturnNotFound_whenShareExpired() throws Exception {
        // Given
        when(reportExportService.isShareValid(eq("expired123"))).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/export/share/data/{shareToken}", "expired123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldRevokeShareLinkSuccessfully() throws Exception {
        // Given
        when(reportExportService.revokeShareLink(eq("abc123"))).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/export/share/{shareToken}", "abc123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturnNotFound_whenRevokeNonExistentShare() throws Exception {
        // Given
        when(reportExportService.revokeShareLink(eq("nonexistent"))).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/export/share/{shareToken}", "nonexistent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void shouldValidateShareLinkSuccessfully() throws Exception {
        // Given
        when(reportExportService.isShareValid(eq("abc123"))).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/export/share/validate/{shareToken}", "abc123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldExportHTMLSuccessfully() throws Exception {
        // Given
        when(reportTemplateService.getTemplateByCode(eq("EXPENSE_REPORT"))).thenReturn(testTemplate);
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("total", 10000);
        when(reportTemplateService.generateReport(eq("EXPENSE_REPORT"), anyMap())).thenReturn(reportData);
        when(reportExportService.exportHTML(any(), anyMap())).thenReturn("("<html><body>Report</body></html>");

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2025-01-01");

        // When & Then
        mockMvc.perform(post("/api/export/html/{templateCode}", "EXPENSE_REPORT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value("("<html><body>Report</body></html>"));
    }

    @Test
    void shouldGetExportOptionsSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/export/options"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.formats").isArray())
                .andExpect(jsonPath("$.data.defaultFormat").value("excel"));
    }

    private ReportTemplate createTestTemplate() {
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        template.setTemplateName("费用报表");
        template.setTemplateType("EXPENSE");
        template.setStatus(1);
        return template;
    }
}
