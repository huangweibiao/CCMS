package com.ccms.controller.report;

import com.ccms.entity.report.ReportTemplate;
import com.ccms.service.report.ReportTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表模板控制器
 */
@RestController
@RequestMapping("/api/report/template")
public class ReportTemplateController {

    @Autowired
    private ReportTemplateService reportTemplateService;

    /**
     * 创建报表模板
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createTemplate(@RequestBody ReportTemplate template) {
        try {
            ReportTemplate createdTemplate = reportTemplateService.createTemplate(template);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "模板创建成功");
            result.put("data", createdTemplate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "模板创建失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 更新报表模板
     */
    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateTemplate(@RequestBody ReportTemplate template) {
        try {
            ReportTemplate updatedTemplate = reportTemplateService.updateTemplate(template);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "模板更新成功");
            result.put("data", updatedTemplate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "模板更新失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteTemplate(@PathVariable Long id) {
        try {
            reportTemplateService.deleteTemplate(id);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "模板删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "模板删除失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据ID获取模板
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> getTemplateById(@PathVariable Long id) {
        try {
            ReportTemplate template = reportTemplateService.getTemplateById(id);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取模板成功");
            result.put("data", template);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取模板失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 分页查询模板列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getTemplateList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String templateType,
            @RequestParam(required = false) Integer status) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size);
            Page<ReportTemplate> templatePage = reportTemplateService.getTemplateList(
                    templateName, templateType, status, pageable);
            
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取模板列表成功");
            result.put("data", templatePage.getContent());
            result.put("total", templatePage.getTotalElements());
            result.put("page", page);
            result.put("size", size);
            result.put("totalPages", templatePage.getTotalPages());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取模板列表失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据类型获取模板列表
     */
    @GetMapping("/type/{templateType}")
    public ResponseEntity<Map<String, Object>> getTemplatesByType(@PathVariable String templateType) {
        try {
            List<ReportTemplate> templates = reportTemplateService.getTemplatesByType(templateType);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取模板列表成功");
            result.put("data", templates);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取模板列表失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 生成报表数据
     */
    @PostMapping("/generate/{templateCode}")
    public ResponseEntity<Map<String, Object>> generateReport(
            @PathVariable String templateCode, 
            @RequestBody Map<String, Object> params) {
        
        try {
            Map<String, Object> reportData = reportTemplateService.generateReport(templateCode, params);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "报表生成成功");
            result.put("data", reportData);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "报表生成失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 复制模板
     */
    @PostMapping("/copy/{sourceTemplateId}")
    public ResponseEntity<Map<String, Object>> copyTemplate(
            @PathVariable Long sourceTemplateId, 
            @RequestParam String newTemplateName) {
        
        try {
            ReportTemplate newTemplate = reportTemplateService.copyTemplate(sourceTemplateId, newTemplateName);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "模板复制成功");
            result.put("data", newTemplate);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "模板复制失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 设置模板状态
     */
    @PutMapping("/status/{templateId}")
    public ResponseEntity<Map<String, Object>> setTemplateStatus(
            @PathVariable Long templateId, 
            @RequestParam Integer status) {
        
        try {
            reportTemplateService.setTemplateStatus(templateId, status);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "模板状态更新成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "模板状态更新失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 获取可用的模板列表
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveTemplates() {
        try {
            List<ReportTemplate> templates = reportTemplateService.getActiveTemplates();
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取可用模板列表成功");
            result.put("data", templates);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取可用模板列表失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据模板代码获取模板
     */
    @GetMapping("/code/{templateCode}")
    public ResponseEntity<Map<String, Object>> getTemplateByCode(@PathVariable String templateCode) {
        try {
            ReportTemplate template = reportTemplateService.getTemplateByCode(templateCode);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "获取模板成功");
            result.put("data", template);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "获取模板失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 验证模板配置
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTemplate(@RequestBody ReportTemplate template) {
        try {
            boolean isValid = reportTemplateService.validateTemplate(template);
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "模板验证" + (isValid ? "成功" : "失败"));
            result.put("data", isValid);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 500);
            result.put("message", "模板验证失败：" + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}