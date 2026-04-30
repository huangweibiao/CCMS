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

}