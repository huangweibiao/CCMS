package com.ccms.controller.export;

import com.ccms.entity.report.ReportTemplate;
import com.ccms.service.export.ReportExportService;
import com.ccms.service.report.ReportTemplateService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 报表导出控制器
 */
@RestController
@RequestMapping("/api/export")
public class ReportExportController {

    @Autowired
    private ReportExportService reportExportService;

    @Autowired
    private ReportTemplateService reportTemplateService;

    /**
     * 导出Excel报表
     */
    @PostMapping("/excel/{templateCode}")
    public void exportExcel(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) {
        
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> reportData = reportTemplateService.generateReport(templateCode, params);
            reportExportService.exportExcel(template, reportData, response);
        } catch (Exception e) {
            throw new RuntimeException("Excel导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出PDF报表
     */
    @PostMapping("/pdf/{templateCode}")
    public void exportPDF(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) {
        
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> reportData = reportTemplateService.generateReport(templateCode, params);
            reportExportService.exportPDF(template, reportData, response);
        } catch (Exception e) {
            throw new RuntimeException("PDF导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出Word报表
     */
    @PostMapping("/word/{templateCode}")
    public void exportWord(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) {
        
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> reportData = reportTemplateService.generateReport(templateCode, params);
            reportExportService.exportWord(template, reportData, response);
        } catch (Exception e) {
            throw new RuntimeException("Word导出失败：" + e.getMessage());
        }
    }

    /**
     * 导出CSV报表
     */
    @PostMapping("/csv/{templateCode}")
    public void exportCSV(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> params,
            HttpServletResponse response) {
        
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> reportData = reportTemplateService.generateReport(templateCode, params);
            reportExportService.exportCSV(template, reportData, response);
        } catch (Exception e) {
            throw new RuntimeException("CSV导出失败：" + e.getMessage());
        }
    }

    /**
     * 批量导出报表
     */
    @PostMapping("/batch")
    public void batchExport(
            @RequestParam String[] templateCodes,
            @RequestBody Map<String, Object> params,
            @RequestParam(defaultValue = "excel") String format,
            HttpServletResponse response) {
        
        try {
            reportExportService.batchExport(templateCodes, params, format, response);
        } catch (Exception e) {
            throw new RuntimeException("批量导出失败：" + e.getMessage());
        }
    }

    /**
     * 生成报表分享链接
     */
    @PostMapping("/share/{templateCode}")
    public ResponseEntity<Map<String, Object>> generateShareLink(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> request) {
        
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", new HashMap<>());
            ReportExportService.ExportConfig config = createExportConfig(request);
            
            String shareLink = reportExportService.generateShareLink(template, params, config);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "分享链接生成成功");
            result.put("data", shareLink);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "分享链接生成失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取分享的报表数据
     */
    @GetMapping("/share/data/{shareToken}")
    public ResponseEntity<Map<String, Object>> getSharedReport(@PathVariable String shareToken) {
        try {
            if (!reportExportService.isShareValid(shareToken)) {
                Map<String, Object> result = new HashMap<>();
                result.put("code", 404);
                result.put("message", "分享链接已过期或不存在");
                return ResponseEntity.ok(result);
            }
            
            Map<String, Object> shareData = reportExportService.getSharedReport(shareToken);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取分享数据成功");
            result.put("data", shareData);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取分享数据失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 撤销分享链接
     */
    @DeleteMapping("/share/{shareToken}")
    public ResponseEntity<Map<String, Object>> revokeShareLink(@PathVariable String shareToken) {
        try {
            boolean success = reportExportService.revokeShareLink(shareToken);
            
            Map<String, Object> result = new HashMap<>();
            if (success) {
                result.put("code", 200);
                result.put("message", "分享链接已撤销");
            } else {
                result.put("code", 404);
                result.put("message", "分享链接不存在");
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "撤销分享链接失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 检查分享链接有效性
     */
    @GetMapping("/share/validate/{shareToken}")
    public ResponseEntity<Map<String, Object>> validateShareLink(@PathVariable String shareToken) {
        try {
            boolean isValid = reportExportService.isShareValid(shareToken);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "分享链接验证完成");
            result.put("data", isValid);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "分享链接验证失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 导出HTML预览
     */
    @PostMapping("/html/{templateCode}")
    public ResponseEntity<Map<String, Object>> exportHTML(
            @PathVariable String templateCode,
            @RequestBody Map<String, Object> params) {
        
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> reportData = reportTemplateService.generateReport(templateCode, params);
            String htmlContent = reportExportService.exportHTML(template, reportData);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "HTML导出成功");
            result.put("data", htmlContent);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "HTML导出失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 导出选项配置
     */
    @GetMapping("/options")
    public ResponseEntity<Map<String, Object>> getExportOptions() {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("formats", new String[]{"excel", "pdf", "word", "csv", "html"});
            options.put("defaultFormat", "excel");
            options.put("fileExtensions", Map.of(
                "excel", ".xlsx",
                "pdf", ".pdf", 
                "word", ".docx",
                "csv", ".csv",
                "html", ".html"
            ));
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取导出选项成功");
            result.put("data", options);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取导出选项失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    // ========== 私有辅助方法 ==========

    private ReportExportService.ExportConfig createExportConfig(Map<String, Object> request) {
        ReportExportService.ExportConfig config = new ReportExportService.ExportConfig();
        
        Object expireHoursObj = request.get("expireHours");
        if (expireHoursObj != null) {
            config.setExpireHours(Integer.parseInt(expireHoursObj.toString()));
        }
        
        Object allowDownloadObj = request.get("allowDownload");
        if (allowDownloadObj != null) {
            config.setAllowDownload(Boolean.parseBoolean(allowDownloadObj.toString()));
        }
        
        Object secureShareObj = request.get("secureShare");
        if (secureShareObj != null) {
            config.setSecureShare(Boolean.parseBoolean(secureShareObj.toString()));
        }
        
        Object passwordObj = request.get("password");
        if (passwordObj != null) {
            config.setPassword(passwordObj.toString());
        }
        
        return config;
    }
}