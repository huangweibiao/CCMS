package com.ccms.service;

import com.ccms.entity.expense.ExpenseInvoice;

import java.util.List;
import java.util.Map;

/**
 * 发票验真服务接口
 * 
 * @author 系统生成
 */
public interface InvoiceVerifyService {
    
    /**
     * 单张发票验真
     * 
     * @param invoiceCode  发票代码
     * @param invoiceNo    发票号码
     * @param invoiceDate  开票日期
     * @param checkCode    校验码（可选）
     * @param invoiceAmount 发票金额（可选）
     * @return 验真结果
     */
    VerifyResult verifyInvoice(String invoiceCode, String invoiceNo, 
                              String invoiceDate, String checkCode, 
                              String invoiceAmount);
    
    /**
     * 多张发票批量验真
     * 
     * @param invoices 发票列表，每个map包含invoiceCode, invoiceNo, invoiceDate等字段
     * @return 验真结果列表
     */
    List<VerifyResult> verifyInvoicesBatch(List<Map<String, String>> invoices);
    
    /**
     * 同步验真发票实体状态
     * 
     * @param invoice 待验真的发票实体
     * @return 更新后的发票实体
     */
    ExpenseInvoice syncVerifyInvoice(ExpenseInvoice invoice);
    
    /**
     * 获取验真服务状态
     * 
     * @return 服务状态信息
     */
    VerifyServiceStatus getServiceStatus();
    
    /**
     * 发票验真结果封装类
     */
    class VerifyResult {
        private final boolean success;          // 验真是否成功
        private final String invoiceCode;      // 发票代码
        private final String invoiceNo;        // 发票号码
        private final String message;          // 结果消息
        private final int verifyStatus;        // 验真状态：0-未验证，1-验证通过，2-验证失败
        private final String verifyData;       // 验真返回数据
        private final String errorCode;        // 错误代码
        
        public VerifyResult(boolean success, String invoiceCode, String invoiceNo, 
                           String message, int verifyStatus, String verifyData, String errorCode) {
            this.success = success;
            this.invoiceCode = invoiceCode;
            this.invoiceNo = invoiceNo;
            this.message = message;
            this.verifyStatus = verifyStatus;
            this.verifyData = verifyData;
            this.errorCode = errorCode;
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getInvoiceCode() { return invoiceCode; }
        public String getInvoiceNo() { return invoiceNo; }
        public String getMessage() { return message; }
        public int getVerifyStatus() { return verifyStatus; }
        public String getVerifyData() { return verifyData; }
        public String getErrorCode() { return errorCode; }
    }
    
    /**
     * 验真服务状态封装类
     */
    class VerifyServiceStatus {
        private final boolean available;           // 服务是否可用
        private final String provider;            // 服务提供商
        private final int remainingQuota;         // 剩余调用配额
        private final long lastUsageTime;         // 最后使用时间
        
        public VerifyServiceStatus(boolean available, String provider, 
                                  int remainingQuota, long lastUsageTime) {
            this.available = available;
            this.provider = provider;
            this.remainingQuota = remainingQuota;
            this.lastUsageTime = lastUsageTime;
        }
        
        // Getter方法
        public boolean isAvailable() { return available; }
        public String getProvider() { return provider; }
        public int getRemainingQuota() { return remainingQuota; }
        public long getLastUsageTime() { return lastUsageTime; }
    }
}