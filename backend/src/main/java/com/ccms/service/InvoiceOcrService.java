package com.ccms.service;

import com.ccms.entity.expense.ExpenseInvoice;

/**
 * 发票OCR识别服务接口
 * 
 * @author 系统生成
 */
public interface InvoiceOcrService {
    
    /**
     * 识别发票图片并提取信息
     * 
     * @param imageData 发票图片数据（Base64编码或文件路径）
     * @return 解析后的发票信息
     */
    ExpenseInvoice recognizeInvoice(String imageData);
    
    /**
     * 批量识别多张发票图片
     * 
     * @param imagesData 多张发票图片数据
     * @return 解析后的发票信息列表
     */
    java.util.List<ExpenseInvoice> recognizeInvoicesBatch(java.util.List<String> imagesData);
    
    /**
     * 验证OCR识别的发票信息
     * 
     * @param invoice OCR识别结果
     * @return 验证结果
     */
    boolean validateOcrResult(ExpenseInvoice invoice);
    
    /**
     * 获取OCR服务状态
     * 
     * @return 服务状态信息
     */
    OcrServiceStatus getServiceStatus();
    
    /**
     * OCR服务状态类
     */
    class OcrServiceStatus {
        private final boolean available;
        private final String serviceProvider;
        private final int remainingQuota;
        private final Long lastUsedTime;
        
        public OcrServiceStatus(boolean available, String serviceProvider, 
                               int remainingQuota, Long lastUsedTime) {
            this.available = available;
            this.serviceProvider = serviceProvider;
            this.remainingQuota = remainingQuota;
            this.lastUsedTime = lastUsedTime;
        }
        
        public boolean isAvailable() {
            return available;
        }
        
        public String getServiceProvider() {
            return serviceProvider;
        }
        
        public int getRemainingQuota() {
            return remainingQuota;
        }
        
        public Long getLastUsedTime() {
            return lastUsedTime;
        }
    }
}