package com.ccms.controller;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.service.InvoiceOcrService;
import com.ccms.service.InvoiceVerifyService;
import com.ccms.service.VerifyResult;
import com.ccms.service.VerifyServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发票管理控制器
 * 
 * @author 系统生成
 */
@RestController
@RequestMapping("/api/invoice")
@CrossOrigin(origins = "*")
public class InvoiceManagementController {
    
    @Autowired
    private InvoiceOcrService invoiceOcrService;
    
    @Autowired
    private InvoiceVerifyService invoiceVerifyService;
    
    /**
     * 单张发票OCR识别
     */
    @PostMapping("/ocr/single")
    public ResponseEntity<Map<String, Object>> ocrSingleInvoice(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件类型
            if (!isValidImageFile(file)) {
                response.put("success", false);
                response.put("message", "不支持的文件格式，请上传图片文件");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 转换文件为Base64
            String imageData = convertToBase64(file);
            
            // 执行OCR识别
            ExpenseInvoice invoice = invoiceOcrService.recognizeInvoice(imageData);
            
            if (invoiceOcrService.validateOcrResult(invoice)) {
                response.put("success", true);
                response.put("message", "发票识别成功");
                response.put("data", invoice);
            } else {
                response.put("success", false);
                response.put("message", "发票识别结果验证失败");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发票识别失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 多张发票批量OCR识别
     */
    @PostMapping("/ocr/batch")
    public ResponseEntity<Map<String, Object>> ocrBatchInvoices(
            @RequestParam("files") MultipartFile[] files) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 验证文件数量
            if (files.length == 0) {
                response.put("success", false);
                response.put("message", "请上传至少一张发票图片");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (files.length > 10) {
                response.put("success", false);
                response.put("message", "单次最多支持10张发票识别");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 转换文件为Base64
            List<String> imagesData = files.length > 0 ? 
                java.util.Arrays.stream(files)
                    .filter(this::isValidImageFile)
                    .map(this::convertToBase64)
                    .collect(java.util.stream.Collectors.toList()) : 
                java.util.Collections.emptyList();
            
            // 批量OCR识别
            List<ExpenseInvoice> invoices = invoiceOcrService.recognizeInvoicesBatch(imagesData);
            
            response.put("success", true);
            response.put("message", "批量发票识别完成，成功识别 " + invoices.size() + " 张发票");
            response.put("data", invoices);
            response.put("total", files.length);
            response.put("successCount", invoices.size());
            response.put("failedCount", files.length - invoices.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "批量发票识别失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 单张发票验真
     */
    @PostMapping("/verify/single")
    public ResponseEntity<Map<String, Object>> verifySingleInvoice(
            @RequestBody Map<String, String> verifyRequest) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String invoiceCode = verifyRequest.get("invoiceCode");
            String invoiceNo = verifyRequest.get("invoiceNo");
            String invoiceDate = verifyRequest.get("invoiceDate");
            String checkCode = verifyRequest.get("checkCode");
            String invoiceAmount = verifyRequest.get("invoiceAmount");
            
            // 执行验真
            VerifyResult result = invoiceVerifyService.verifyInvoice(
                invoiceCode, invoiceNo, invoiceDate, checkCode, invoiceAmount);
            
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("data", result);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "发票验真失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 获取OCR服务状态
     */
    @GetMapping("/service/status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // OCR服务状态
            InvoiceOcrService.OcrServiceStatus ocrStatus = invoiceOcrService.getServiceStatus();
            // 验真服务状态
            VerifyServiceStatus verifyStatus = invoiceVerifyService.getServiceStatus();
            
            Map<String, Object> statusData = new HashMap<>();
            statusData.put("ocrService", ocrStatus);
            statusData.put("verifyService", verifyStatus);
            
            response.put("success", true);
            response.put("message", "服务状态获取成功");
            response.put("data", statusData);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务状态获取失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 验证是否为支持的图片文件
     */
    private boolean isValidImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        
        // 支持常见的图片格式
        return contentType != null && (
            contentType.startsWith("image/jpeg") ||
            contentType.startsWith("image/png") ||
            contentType.startsWith("image/gif") ||
            contentType.startsWith("image/bmp")
        ) && (
            originalFilename != null && (
                originalFilename.toLowerCase().endsWith(".jpg") ||
                originalFilename.toLowerCase().endsWith(".jpeg") ||
                originalFilename.toLowerCase().endsWith(".png") ||
                originalFilename.toLowerCase().endsWith(".gif") ||
                originalFilename.toLowerCase().endsWith(".bmp")
            )
        );
    }
    
    /**
     * 将图片文件转换为Base64编码
     */
    private String convertToBase64(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (Exception e) {
            throw new RuntimeException("文件转换失败: " + e.getMessage(), e);
        }
    }
}