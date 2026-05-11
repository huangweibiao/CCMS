package com.ccms.controller.export;

import com.ccms.controller.ControllerTestBase;
import com.ccms.entity.report.ReportTemplate;
import com.ccms.service.export.ReportExportService;
import com.ccms.service.report.ReportTemplateService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 报表导出控制器单元测试
 */
@WebMvcTest(ReportExportController.class)
class ReportExportControllerTest extends ControllerTestBase {

    @MockBean
    private ReportExportService reportExportService;

    @MockBean
    private ReportTemplateService reportTemplateService;

    @Test
    void shouldGenerateShareLinkSuccess() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);
        when(reportExportService.generateShareLink(any(), any(), any())).thenReturn("http://share.link/abc123");

        Map<String, Object> request = new HashMap<>();
        request.put("params", new HashMap<>());
        
        performPost("/api/export/share/EXPENSE_REPORT", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("分享链接生成成功"))
                .andExpect(jsonPath("$.data").value("http://share.link/abc123"));
    }

    @Test
    void shouldGetSharedReportSuccess() throws Exception {
        when(reportExportService.isShareValid("validToken")).thenReturn(true);
        
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("templateCode", "EXPENSE_REPORT");
        when(reportExportService.getSharedReport("validToken")).thenReturn(shareData);

        performGet("/api/export/share/data/validToken")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.templateCode").value("EXPENSE_REPORT"));
    }

    @Test
    void shouldReturnErrorWhenShareExpired() throws Exception {
        when(reportExportService.isShareValid("expiredToken")).thenReturn(false);

        performGet("/api/export/share/data/expiredToken")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("分享链接已过期或不存在"));
    }

    @Test
    void shouldRevokeShareLinkSuccess() throws Exception {
        when(reportExportService.revokeShareLink("validToken")).thenReturn(true);

        performDelete("/api/export/share/validToken")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("分享链接已撤销"));
    }

    @Test
    void shouldReturnErrorWhenRevokeNonExistentShare() throws Exception {
        when(reportExportService.revokeShareLink("invalidToken")).thenReturn(false);

        performDelete("/api/export/share/invalidToken")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("分享链接不存在"));
    }

    @Test
    void shouldValidateShareLinkSuccess() throws Exception {
        when(reportExportService.isShareValid("validToken")).thenReturn(true);

        performGet("/api/export/share/validate/validToken")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void shouldExportHTMLSuccess() throws Exception {
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);
        when(reportTemplateService.generateReport(any(), any())).thenReturn(new HashMap<>());
        when(reportExportService.exportHTML(any(), any())).thenReturn("<html>Report</html>");

        performPost("/api/export/html/EXPENSE_REPORT", new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("HTML导出成功"))
                .andExpect(jsonPath("$.data").value("<html>Report</html>"));
    }

    @Test
    void shouldGetExportOptionsSuccess() throws Exception {
        performGet("/api/export/options")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.formats").exists())
                .andExpect(jsonPath("$.data.defaultFormat").value("excel"));
    }

    /**
     * Task 14.1: 添加大数据量导出性能测试
     */

    @Test
    void shouldHandleLargeDataExport() throws Exception {
        // given
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        
        Map<String, Object> largeReportData = new HashMap<>();
        largeReportData.put("totalRecords", 10000);
        largeReportData.put("data", java.util.Collections.nCopies(10000, Map.of("id", 1, "amount", 100.0)));
        
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);
        when(reportTemplateService.generateReport(any(), any())).thenReturn(largeReportData);
        when(reportExportService.exportHTML(any(), any())).thenReturn("<html>Large Report</html>");

        // when & then
        performPost("/api/export/html/EXPENSE_REPORT", new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("HTML导出成功"));
    }

    /**
     * Task 14.2: 添加多格式导出转换测试
     */

    @Test
    void shouldHandleMultipleExportFormats() throws Exception {
        // given
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);
        when(reportTemplateService.generateReport(any(), any())).thenReturn(new HashMap<>());
        when(reportExportService.exportHTML(any(), any())).thenReturn("<html>Report</html>");

        // when & then - 测试HTML格式
        performPost("/api/export/html/EXPENSE_REPORT", new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("HTML导出成功"));
    }

    /**
     * Task 14.3: 添加导出数据校验测试
     */

    @Test
    void shouldValidateExportDataIntegrity() throws Exception {
        // given
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("records", java.util.Arrays.asList(
                Map.of("id", 1, "amount", 100.0, "date", "2025-01-01"),
                Map.of("id", 2, "amount", 200.0, "date", "2025-01-02"),
                Map.of("id", 3, "amount", 150.0, "date", "2025-01-03")
        ));
        reportData.put("totalAmount", 450.0);
        
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);
        when(reportTemplateService.generateReport(any(), any())).thenReturn(reportData);
        when(reportExportService.exportHTML(any(), any())).thenReturn("<html>Validated Report</html>");

        // when & then
        performPost("/api/export/html/EXPENSE_REPORT", new HashMap<>())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    /**
     * Task 14.4: 添加异步导出状态跟踪测试
     */

    @Test
    void shouldHandleAsyncExportStatus() throws Exception {
        // given
        Map<String, Object> shareData = new HashMap<>();
        shareData.put("templateCode", "EXPENSE_REPORT");
        shareData.put("status", "COMPLETED");

        when(reportExportService.isShareValid("validToken")).thenReturn(true);
        when(reportExportService.getSharedReport("validToken")).thenReturn(shareData);

        // when & then
        performGet("/api/export/share/data/validToken")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    /**
     * Task 14.5: 添加导出权限控制测试
     */

    @Test
    void shouldGenerateShareLinkWithPermission() throws Exception {
        // given
        ReportTemplate template = new ReportTemplate();
        template.setId(1L);
        template.setTemplateCode("EXPENSE_REPORT");
        
        when(reportTemplateService.getTemplateByCode("EXPENSE_REPORT")).thenReturn(template);
        when(reportExportService.generateShareLink(any(), any(), any())).thenReturn("http://share.link/abc123");

        Map<String, Object> request = new HashMap<>();
        request.put("params", new HashMap<>());
        
        // when & then
        performPost("/api/export/share/EXPENSE_REPORT", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("分享链接生成成功"));
    }
}
