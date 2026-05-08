package com.ccms.controller.report;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.report.ReportTemplate;
import com.ccms.service.report.ReportTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报表模板控制器单元测试
 */
@WebMvcTest(ReportTemplateController.class)
class ReportTemplateControllerTest extends ControllerTestBase {

    @MockBean
    private ReportTemplateService reportTemplateService;

    private ReportTemplate createTestTemplate(Long id, String code, String name) {
        ReportTemplate template = new ReportTemplate();
        template.setId(id);
        template.setTemplateCode(code);
        template.setTemplateName(name);
        template.setTemplateType("EXCEL");
        template.setStatus(1);

        return template;
    }

    @Test
    void shouldCreateTemplateSuccess() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        when(reportTemplateService.createTemplate(any(ReportTemplate.class))).thenReturn(template);

        performPost("/api/report/template/create", template)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT"));
    }

    @Test
    void shouldUpdateTemplateSuccess() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表更新");
        when(reportTemplateService.updateTemplate(any(ReportTemplate.class))).thenReturn(template);

        performPut("/api/report/template/update", template)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateName").value("费用报表更新"));
    }

    @Test
    void shouldDeleteTemplateSuccess() throws Exception {
        performDelete("/api/report/template/delete/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("模板删除成功"));

        verify(reportTemplateService, times(1)).deleteTemplate(1L);
    }

    @Test
    void shouldGetTemplateByIdSuccess() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        when(reportTemplateService.getTemplateById(1L)).thenReturn(template);

        performGet("/api/report/template/detail/1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT"));
    }

    @Test
    void shouldGetTemplateList() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        Page<ReportTemplate> page = new PageImpl<>(
                Collections.singletonList(template),
                PageRequest.of(0, 10),
                1
        );
        when(reportTemplateService.getTemplateList(any(), any(), any(), any(Pageable.class))).thenReturn(page);

        performGet("/api/report/template/list?page=1&size=10")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void shouldGetTemplatesByType() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        when(reportTemplateService.getTemplatesByType("EXCEL"))
                .thenReturn(Collections.singletonList(template));

        performGet("/api/report/template/type/EXCEL")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void shouldGenerateReportSuccess() throws Exception {
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("rows", new Object[]{});
        reportData.put("total", 100);
        when(reportTemplateService.generateReport(anyString(), anyMap())).thenReturn(reportData);

        performPost("/api/report/template/generate/EXPENSE_REPORT", new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.total").value(100));
    }

    @Test
    void shouldCopyTemplateSuccess() throws Exception {
        ReportTemplate newTemplate = createTestTemplate(2L, "EXPENSE_REPORT_COPY", "费用报表副本");
        when(reportTemplateService.copyTemplate(1L, "费用报表副本")).thenReturn(newTemplate);

        performPost("/api/report/template/copy/1?newTemplateName=费用报表副本", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT_COPY"));
    }

    @Test
    void shouldSetTemplateStatusSuccess() throws Exception {
        performPut("/api/report/template/status/1?status=0", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("模板状态更新成功"));

        verify(reportTemplateService, times(1)).setTemplateStatus(1L, 0);
    }

    @Test
    void shouldGetActiveTemplates() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        when(reportTemplateService.getActiveTemplates())
                .thenReturn(Collections.singletonList(template));

        performGet("/api/report/template/active")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void shouldGetTemplateByCode() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);

        performGet("/api/report/template/code/EXPENSE_REPORT")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT"));
    }

    @Test
    void shouldValidateTemplate() throws Exception {
        ReportTemplate template = createTestTemplate(1L, "EXPENSE_REPORT", "费用报表");
        when(reportTemplateService.validateTemplate(any(ReportTemplate.class))).thenReturn(true);

        performPost("/api/report/template/validate", template)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }
}
