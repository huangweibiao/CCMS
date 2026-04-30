package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * 发票表实体类
 * 对应表名：ccms_expense_invoice
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_expense_invoice")
public class ExpenseInvoice extends BaseEntity {

    /**
     * 报销明细ID
     */
    @Column(name = "reimburse_detail_id", nullable = false)
    private Long reimburseDetailId;
    
    /**
     * 发票类型：1-增值税专票 2-普票 3-电子票
     */
    @Column(name = "invoice_type", nullable = false)
    private Integer invoiceType;
    
    /**
     * 发票代码
     */
    @Column(name = "invoice_code", length = 32, nullable = false)
    private String invoiceCode;
    
    /**
     * 发票号码
     */
    @Column(name = "invoice_no", length = 32, nullable = false)
    private String invoiceNo;
    
    /**
     * 发票金额
     */
    @Column(name = "invoice_amount", precision = 18, scale = 2, nullable = false)
    private BigDecimal invoiceAmount;
    
    /**
     * 税额
     */
    @Column(name = "tax_amount", precision = 18, scale = 2)
    private BigDecimal taxAmount;
    
    /**
     * 开票日期
     */
    @Column(name = "invoice_date", nullable = false)
    private Date invoiceDate;
    
    /**
     * 销方名称
     */
    @Column(name = "seller_name", length = 128)
    private String sellerName;
    
    /**
     * 销方税号
     */
    @Column(name = "seller_tax_no", length = 32)
    private String sellerTaxNo;
    
    /**
     * 发票状态：0-未验真 1-已验真通过 2-验真失败
     */
    @Column(name = "verify_status", nullable = false)
    private Integer verifyStatus;
    
    /**
     * 验真结果
     */
    @Column(name = "verify_result", length = 256)
    private String verifyResult;
    
    /**
     * 验真时间
     */
    @Column(name = "verify_time")
    private java.time.LocalDateTime verifyTime;
    
    /**
     * 发票文件路径
     */
    @Column(name = "file_path", length = 256)
    private String filePath;

    /**
     * 校验码
     */
    @Column(name = "check_code", length = 32)
    private String checkCode;

    /**
     * 验真备注
     */
    @Column(name = "verify_comment", length = 256)
    private String verifyComment;

    /**
     * 验真数据
     */
    @Column(name = "verify_data", length = 500)
    private String verifyData;

    /**
     * 验真错误码
     */
    @Column(name = "verify_error_code", length = 32)
    private String verifyErrorCode;

    // Getters and Setters
    public Long getReimburseDetailId() {
        return reimburseDetailId;
    }

    public void setReimburseDetailId(Long reimburseDetailId) {
        this.reimburseDetailId = reimburseDetailId;
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
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

    public BigDecimal getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerTaxNo() {
        return sellerTaxNo;
    }

    public void setSellerTaxNo(String sellerTaxNo) {
        this.sellerTaxNo = sellerTaxNo;
    }

    public Integer getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(Integer verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getVerifyResult() {
        return verifyResult;
    }

    public void setVerifyResult(String verifyResult) {
        this.verifyResult = verifyResult;
    }

    public java.time.LocalDateTime getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(java.time.LocalDateTime verifyTime) {
        this.verifyTime = verifyTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCheckCode() {
        return checkCode;
    }

    public void setCheckCode(String checkCode) {
        this.checkCode = checkCode;
    }

    public String getVerifyComment() {
        return verifyComment;
    }

    public void setVerifyComment(String verifyComment) {
        this.verifyComment = verifyComment;
    }

    public String getVerifyData() {
        return verifyData;
    }

    public void setVerifyData(String verifyData) {
        this.verifyData = verifyData;
    }

    public String getVerifyErrorCode() {
        return verifyErrorCode;
    }

    public void setVerifyErrorCode(String verifyErrorCode) {
        this.verifyErrorCode = verifyErrorCode;
    }

    @Override
    public String toString() {
        return "ExpenseInvoice{" +
                "id=" + getId() +
                ", reimburseDetailId=" + reimburseDetailId +
                ", invoiceType=" + invoiceType +
                ", invoiceCode='" + invoiceCode + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", invoiceAmount=" + invoiceAmount +
                ", taxAmount=" + taxAmount +
                ", invoiceDate=" + invoiceDate +
                ", sellerName='" + sellerName + '\'' +
                ", sellerTaxNo='" + sellerTaxNo + '\'' +
                ", verifyStatus=" + verifyStatus +
                ", verifyResult='" + verifyResult + '\'' +
                ", verifyTime=" + verifyTime +
                ", filePath=" + filePath + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}