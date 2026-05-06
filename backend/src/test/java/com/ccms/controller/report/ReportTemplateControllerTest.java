package com.ccms.controller.report;

import com.ccms.controller.BaseControllerTest;
import com.ccms.entity.report.ReportTemplate;
import com.ccms.service.report.ReportTemplateService;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报表模板控制器单元测试
 */
@WebMvcTest(ReportTemplateController.class)
class ReportTemplateControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldCreateTemplateSuccessfully() throws Exception {
        // Given
        when(reportTemplateService.createTemplate(any(ReportTemplate.class)))
                .thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(post("/api/report/template/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTemplate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT"));
    }

    @Test
    void shouldUpdateTemplateSuccessfully() throws Exception {
        // Given
        ReportTemplate updated = createTestTemplate();
        updated.setTemplateName("Updated Name");
        when(reportTemplateService.updateTemplate(any(ReportTemplate.class)))
                .thenReturn(updated);

        // When & Then
        mockMvc.perform(put("/api/report/template/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTemplate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldDeleteTemplateSuccessfully() throws Exception {
        // Given
        doNothing().when(reportTemplateService).deleteTemplate(eq(1L));

        // When & Then
        mockMvc.perform(delete("/api/report/template/delete/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetTemplateByIdSuccessfully() throws Exception {
        // Given
        when(reportTemplateService.getTemplateById(eq(1L))).thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(get("/api/report/template/detail/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void shouldGetTemplateListSuccessfully() throws Exception {
        // Given
        Page<ReportTemplate> page = new PageImpl<>(
                Collections.singletonList(testTemplate),
                PageRequest.of(0, 10),
                1
        );
        when(reportTemplateService.getTemplateList(any(), any(), any(), any()))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/report/template/list")
                        .param("page", "1")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGetTemplatesByTypeSuccessfully() throws Exception {
        // Given
        List<ReportTemplate> templates = Collections.singletonList(testTemplate);
        when(reportTemplateService.getTemplatesByType(eq("EXPENSE")))
                .thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/report/template/type/{templateType}", "EXPENSE"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGenerateReportSuccessfully() throws Exception {
        // Given
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("totalAmount", 10000);
        when(reportTemplateService.generateReport(eq("EXPENSE_REPORT"), anyMap()))
                .thenReturn(reportData);

        Map<String, Object> params = new HashMap<>();
        params.put("startDate", "2025-01-01");

        // When & Then
        mockMvc.perform(post("/api/report/template/generate/{templateCode}", "EXPENSE_REPORT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(params)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalAmount").value(10000));
    }

    @Test
    void shouldCopyTemplateSuccessfully() throws Exception {
        // Given
        ReportTemplate copied = createTestTemplate();
        copied.setId(2L);
        copied.setTemplateName("Copied Template");
        when(reportTemplateService.copyTemplate(eq(1L), eq("Copied Template")))
                .thenReturn(copied);

        // When & Then
        mockMvc.perform(post("/api/report/template/copy/{sourceTemplateId}", 1L)
                        .param("newTemplateName", "Copied Template"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldSetTemplateStatusSuccessfully() throws Exception {
        // Given
        doNothing().when(reportTemplateService).setTemplateStatus(eq(1L), eq(0));

        // When & Then
        mockMvc.perform(put("/api/report/template/status/{templateId}", 1L)
                        .param("status", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldGetActiveTemplatesSuccessfully() throws Exception {
        // Given
        List<ReportTemplate> templates = Collections.singletonList(testTemplate);
        when(reportTemplateService.getActiveTemplates()).thenReturn(templates);

        // When & Then
        mockMvc.perform(get("/api/report/template/active"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void shouldGetTemplateByCodeSuccessfully() throws Exception {
        // Given
        when(reportTemplateService.getTemplateByCode(eq("EXPENSE_REPORT")))
                .thenReturn(testTemplate);

        // When & Then
        mockMvc.perform(get("/api/report/template/code/{templateCode}", "EXPENSE_REPORT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT"));
    }

    @Test
    void shouldValidateTemplateSuccessfully() throws Exception {
        // Given
        when(reportTemplateService.validateTemplate(any(ReportTemplate.class)))
                .thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/report/template/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testTemplate)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
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
