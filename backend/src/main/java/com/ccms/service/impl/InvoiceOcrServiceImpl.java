package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.service.InvoiceOcrService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 发票OCR识别服务实现类
 * 
 * @author 系统生成
 */
@Service
public class InvoiceOcrServiceImpl implements InvoiceOcrService {
    
    private final Random random = new Random();
    
    @Override
    public ExpenseInvoice recognizeInvoice(String imageData) {
        // 模拟OCR识别过程
        // 实际实现会调用第三方OCR服务（如百度OCR、阿里云OCR等）
        
        ExpenseInvoice invoice = new ExpenseInvoice();
        
        // 识别基本信息
        invoice.setInvoiceType(detectInvoiceType(imageData));
        invoice.setInvoiceCode(generateInvoiceCode());
        invoice.setInvoiceNo(generateInvoiceNo());
        
        // 识别金额信息
        invoice.setInvoiceAmount(extractAmount(imageData));
        invoice.setTaxAmount(calculateTaxAmount(invoice.getInvoiceAmount()));
        
        // 识别开票信息
        invoice.setInvoiceDate(Date.valueOf(LocalDate.now().minusDays(random.nextInt(30))));
        invoice.setSellerName(extractSellerName(imageData));
        invoice.setSellerTaxNo(generateTaxNo());
        
        // 设置默认状态
        invoice.setVerifyStatus(0); // 未验真
        
        // 记录OCR识别时间
        invoice.setCreateTime(LocalDateTime.now());
        
        return invoice;
    }
    
    @Override
    public List<ExpenseInvoice> recognizeInvoicesBatch(List<String> imagesData) {
        List<ExpenseInvoice> invoices = new ArrayList<>();
        
        for (String imageData : imagesData) {
            try {
                ExpenseInvoice invoice = recognizeInvoice(imageData);
                if (validateOcrResult(invoice)) {
                    invoices.add(invoice);
                }
            } catch (Exception e) {
                // 单个发票识别失败不影响其他发票
                System.err.println("OCR识别失败: " + e.getMessage());
            }
        }
        
        return invoices;
    }
    
    @Override
    public boolean validateOcrResult(ExpenseInvoice invoice) {
        if (invoice == null) {
            return false;
        }
        
        // 验证必填字段
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().trim().isEmpty()) {
            return false;
        }
        
        if (invoice.getInvoiceAmount() == null || invoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // 验证发票号码格式（简化验证）
        if (!isValidInvoiceNo(invoice.getInvoiceNo())) {
            return false;
        }
        
        // 验证开票日期合理性
        if (invoice.getInvoiceDate() != null) {
            LocalDate invoiceDate = invoice.getInvoiceDate().toLocalDate();
            LocalDate today = LocalDate.now();
            if (invoiceDate.isAfter(today) || invoiceDate.isBefore(today.minusYears(1))) {
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public OcrServiceStatus getServiceStatus() {
        // 模拟OCR服务状态
        return new OcrServiceStatus(
            true,                    // 服务可用
            "模拟OCR服务",           // 服务提供商
            1000,                    // 剩余配额
            System.currentTimeMillis() // 最后使用时间
        );
    }
    
    /**
     * 检测发票类型
     */
    private Integer detectInvoiceType(String imageData) {
        // 实际实现会根据图片特征识别发票类型
        // 这里随机返回一种类型进行模拟
        return random.nextInt(3) + 1; // 1-3之间
    }
    
    /**
     * 生成发票代码
     */
    private String generateInvoiceCode() {
        // 模拟生成发票代码（实际应根据发票类型等规则生成）
        return String.format("1440%08d", random.nextInt(100000000));
    }
    
    /**
     * 生成发票号码
     */
    private String generateInvoiceNo() {
        // 模拟生成发票号码
        return String.format("%08d", random.nextInt(100000000));
    }
    
    /**
     * 从图片中提取金额
     */
    private BigDecimal extractAmount(String imageData) {
        // 实际实现会解析图片中的金额信息
        // 这里随机生成金额进行模拟
        return BigDecimal.valueOf(random.nextDouble() * 1000 + 1).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 计算税额
     */
    private BigDecimal calculateTaxAmount(BigDecimal invoiceAmount) {
        // 模拟计算税额（假设税率13%）
        return invoiceAmount.multiply(new BigDecimal("0.13")).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 识别销方名称
     */
    private String extractSellerName(String imageData) {
        // 模拟识别销方名称
        String[] sellers = {
            "北京市某科技有限公司", 
            "上海某服务有限公司", 
            "深圳某贸易有限公司",
            "广州某实业有限公司"
        };
        return sellers[random.nextInt(sellers.length)];
    }
    
    /**
     * 生成纳税人识别号
     */
    private String generateTaxNo() {
        // 模拟生成纳税人识别号
        StringBuilder taxNo = new StringBuilder();
        for (int i = 0; i < 18; i++) {
            taxNo.append(random.nextInt(10));
        }
        return taxNo.toString();
    }
    
    /**
     * 验证发票号码格式
     */
    private boolean isValidInvoiceNo(String invoiceNo) {
        // 简化验证：8位或10位数字
        return Pattern.matches("^\\d{8,10}$", invoiceNo);
    }
}