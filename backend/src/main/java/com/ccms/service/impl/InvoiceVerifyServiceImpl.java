package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.service.InvoiceVerifyService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 发票验真服务实现类
 * 
 * @author 系统生成
 */
@Service
public class InvoiceVerifyServiceImpl implements InvoiceVerifyService {
    
    private final Random random = new Random();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    @Override
    public VerifyResult verifyInvoice(String invoiceCode, String invoiceNo, 
                                     String invoiceDate, String checkCode, 
                                     String invoiceAmount) {
        
        // 验证必填参数
        if (invoiceCode == null || invoiceNo == null || invoiceDate == null) {
            return new VerifyResult(false, invoiceCode, invoiceNo, 
                "必填参数缺失", 2, null, "MISSING_PARAMS");
        }
        
        // 验证发票日期格式
        try {
            LocalDate.parse(invoiceDate, dateFormatter);
        } catch (Exception e) {
            return new VerifyResult(false, invoiceCode, invoiceNo, 
                "发票日期格式错误", 2, null, "INVALID_DATE");
        }
        
        // 模拟第三方验真API调用
        boolean verificationSuccess = simulateVerifyApiCall(invoiceCode, invoiceNo, invoiceDate);
        
        if (verificationSuccess) {
            String verifyData = generateSimulatedVerifyData(invoiceCode, invoiceNo, invoiceDate);
            return new VerifyResult(true, invoiceCode, invoiceNo, 
                "验真通过", 1, verifyData, null);
        } else {
            return new VerifyResult(false, invoiceCode, invoiceNo, 
                "发票验真失败，请检查发票信息是否正确", 2, null, "VERIFY_FAILED");
        }
    }
    
    @Override
    public List<VerifyResult> verifyInvoicesBatch(List<Map<String, String>> invoices) {
        List<VerifyResult> results = new ArrayList<>();
        
        for (Map<String, String> invoice : invoices) {
            String invoiceCode = invoice.get("invoiceCode");
            String invoiceNo = invoice.get("invoiceNo");
            String invoiceDate = invoice.get("invoiceDate");
            String checkCode = invoice.get("checkCode");
            String invoiceAmount = invoice.get("invoiceAmount");
            
            VerifyResult result = verifyInvoice(invoiceCode, invoiceNo, invoiceDate, checkCode, invoiceAmount);
            results.add(result);
        }
        
        return results;
    }
    
    @Override
    public ExpenseInvoice syncVerifyInvoice(ExpenseInvoice invoice) {
        if (invoice == null) {
            return null;
        }
        
        // 执行验真
        VerifyResult result = verifyInvoice(
            invoice.getInvoiceCode(), 
            invoice.getInvoiceNo(),
            invoice.getInvoiceDate() != null ? invoice.getInvoiceDate().toString() : null,
            invoice.getCheckCode(),
            invoice.getInvoiceAmount() != null ? invoice.getInvoiceAmount().toString() : null
        );
        
        // 更新发票状态
        if (result.isSuccess()) {
            invoice.setVerifyStatus(1); // 验真成功
            invoice.setVerifyTime(java.time.LocalDateTime.now());
            invoice.setVerifyComment(result.getMessage());
            invoice.setVerifyData(result.getVerifyData());
        } else {
            invoice.setVerifyStatus(2); // 验真失败
            invoice.setVerifyTime(java.time.LocalDateTime.now());
            invoice.setVerifyComment(result.getMessage());
            invoice.setVerifyErrorCode(result.getErrorCode());
        }
        
        return invoice;
    }
    
    @Override
    public VerifyServiceStatus getServiceStatus() {
        // 模拟实际服务状态检查
        return new VerifyServiceStatus(
            true,                    // 服务可用
            "国家税务总局发票查验平台", // 服务提供商
            500,                     // 剩余配额
            System.currentTimeMillis() // 最后使用时间
        );
    }
    
    /**
     * 模拟第三方发票验真API调用
     */
    private boolean simulateVerifyApiCall(String invoiceCode, String invoiceNo, String invoiceDate) {
        // 模拟API调用成功率（80%成功，20%失败）
        // 实际实现会调用真实的第三方验真API
        
        // 模拟网络延迟
        try {
            Thread.sleep(200 + random.nextInt(300));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 基于发票号最后一位决定验证结果（简化模拟）
        if (invoiceNo == null || invoiceNo.trim().isEmpty()) {
            return false;
        }
        
        char lastDigit = invoiceNo.charAt(invoiceNo.length() - 1);
        int digitValue = Character.getNumericValue(lastDigit);
        
        // 尾号为0-6的视为验证通过，7-9的视为验证失败
        return digitValue < 7;
    }
    
    /**
     * 生成模拟的验真返回数据
     */
    private String generateSimulatedVerifyData(String invoiceCode, String invoiceNo, String invoiceDate) {
        return String.format("{\"invoiceCode\":\"%s\",\"invoiceNo\":\"%s\",\"invoiceDate\":\"%s\"," +
                           "\"sellerName\":\"模拟企业名称\",\"totalAmount\":\"%d.00\"," +
                           "\"taxAmount\":\"%.2f\",\"verifyTime\":\"%s\"}",
            invoiceCode, invoiceNo, invoiceDate,
            random.nextInt(1000) + 1,
            random.nextDouble() * 100,
            java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }
}