package com.ccms.entity.invoice;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发票信息实体
 */
@Entity
@Table(name = "invoice_info")
public class InvoiceInfo extends BaseEntity {
    
    @Column(name = "invoice_no", nullable = false, unique = true, length = 50)
    private String invoiceNo;
    
    @Column(name = "invoice_code", length = 20)
    private String invoiceCode;
    
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    
    @Column(name = "invoice_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal invoiceAmount;
    
    @Column(name = "purchaser_name", length = 200)
    private String purchaserName;
    
    @Column(name = "purchaser_tax_no", length = 30)
    private String purchaserTaxNo;
    
    @Column(name = "seller_name", length = 200)
    private String sellerName;
    
    @Column(name = "seller_tax_no", length = 30)
    private String sellerTaxNo;
    
    @Column(name = "invoice_type", nullable = false)
    private Integer invoiceType; // 发票类型：1-增值税专用发票, 2-增值税普通发票
    
    @Column(name = "expense_type", length = 50)
    private String expenseType;
    
    @Column(name = "expense_item_id")
    private Long expenseItemId;
    
    @Column(name = "reimburse_id")
    private Long reimburseId;
    
    @Column(name = "verification_status", nullable = false)
    private Integer verificationStatus = 0; // 0-未验真, 1-验真通过, 2-验真失败
    
    @Column(name = "verification_time")
    private LocalDate verificationTime;
    
    @Column(name = "verification_remark", length = 500)
    private String verificationRemark;
    
    @Column(name = "attachment_id")
    private String attachmentId;
    
    @Column(name = "remark", length = 1000)
    private String remark;

    // Getters and Setters
    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(BigDecimal invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getPurchaserName() {
        return purchaserName;
    }

    public void setPurchaserName(String purchaserName) {
        this.purchaserName = purchaserName;
    }

    public String getPurchaserTaxNo() {
        return purchaserTaxNo;
    }

    public void setPurchaserTaxNo(String purchaserTaxNo) {
        this.purchaserTaxNo = purchaserTaxNo;
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

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public Long getExpenseItemId() {
        return expenseItemId;
    }

    public void setExpenseItemId(Long expenseItemId) {
        this.expenseItemId = expenseItemId;
    }

    public Long getReimburseId() {
        return reimburseId;
    }

    public void setReimburseId(Long reimburseId) {
        this.reimburseId = reimburseId;
    }

    public Integer getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(Integer verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public LocalDate getVerificationTime() {
        return verificationTime;
    }

    public void setVerificationTime(LocalDate verificationTime) {
        this.verificationTime = verificationTime;
    }

    public String getVerificationRemark() {
        return verificationRemark;
    }

    public void setVerificationRemark(String verificationRemark) {
        this.verificationRemark = verificationRemark;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    /**
     * 检查是否需要验真（金额超过1000元）
     */
    public boolean requiresVerification() {
        return invoiceAmount != null && invoiceAmount.compareTo(new BigDecimal("1000")) >= 0;
    }
    
    /**
     * 检查发票是否已验真
     */
    public boolean isVerified() {
        return verificationStatus != null && verificationStatus.equals(1);
    }
    
    /**
     * 检查发票类型与费用类型是否匹配
     */
    public boolean isTypeMatching(String requiredExpenseType) {
        return expenseType != null && expenseType.equals(requiredExpenseType);
    }
}