package com.ccms.service;

/**
 * 发票验真结果
 * 
 * @author 系统生成
 */
public class VerifyResult {
    
    private boolean success;
    private String invoiceCode;
    private String invoiceNo;
    private String message;
    private Integer verifyStatus; // 1-成功 2-失败
    private String verifyData;
    private String errorCode;
    
    // 构造函数
    public VerifyResult() {
    }
    
    public VerifyResult(boolean success, String invoiceCode, String invoiceNo, 
                       String message, Integer verifyStatus, String verifyData, String errorCode) {
        this.success = success;
        this.invoiceCode = invoiceCode;
        this.invoiceNo = invoiceNo;
        this.message = message;
        this.verifyStatus = verifyStatus;
        this.verifyData = verifyData;
        this.errorCode = errorCode;
    }
    
    // Getter和Setter方法
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getInvoiceCode() {
        return invoiceCode;
    }
    
    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }
    
    public String getInvoiceNo() {
        return invoiceNo;
    }
    
    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Integer getVerifyStatus() {
        return verifyStatus;
    }
    
    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }
    
    public String getVerifyData() {
        return verifyData;
    }
    
    public void setVerifyData(String verifyData) {
        this.verifyData = verifyData;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}