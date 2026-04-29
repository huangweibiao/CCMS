package com.ccms.service.invoice;

import com.ccms.entity.invoice.InvoiceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 发票服务接口
 */
public interface InvoiceService {
    
    /**
     * 创建发票信息
     */
    InvoiceInfo createInvoice(InvoiceInfo invoiceInfo);
    
    /**
     * 更新发票信息
     */
    InvoiceInfo updateInvoice(InvoiceInfo invoiceInfo);
    
    /**
     * 根据ID获取发票
     */
    InvoiceInfo getInvoiceById(Long id);
    
    /**
     * 根据发票号获取发票
     */
    InvoiceInfo getInvoiceByNo(String invoiceNo);
    
    /**
     * 删除发票
     */
    void deleteInvoice(Long id);
    
    /**
     * 查询发票列表
     */
    Page<InvoiceInfo> getInvoiceList(InvoiceQueryCondition condition, Pageable pageable);
    
    /**
     * 根据报销单ID查询发票
     */
    List<InvoiceInfo> getInvoicesByReimburseId(Long reimburseId);
    
    /**
     * 验证发票号是否唯一
     */
    boolean isInvoiceNoUnique(String invoiceNo, Long excludeId);
    
    /**
     * 批量验真发票（默认通过，简化实现）
     */
    boolean verifyInvoice(String invoiceNo, String invoiceCode, BigDecimal amount, LocalDate date);
    
    /**
     * 检查发票合规性
     */
    String checkInvoiceCompliance(InvoiceInfo invoiceInfo);
    
    /**
     * 关联发票与费用明细
     */
    void associateWithExpenseItem(Long invoiceId, Long expenseItemId);
    
    /**
     * 更新发票验真状态
     */
    void updateVerificationStatus(Long invoiceId, Integer status, String remark);
    
    class InvoiceQueryCondition {
        private String invoiceNo;
        private String purchaserName;
        private String sellerName;
        private Integer invoiceType;
        private Integer verificationStatus;
        private LocalDate startDate;
        private LocalDate endDate;
        
        // Getters and Setters
        public String getInvoiceNo() { return invoiceNo; }
        public void setInvoiceNo(String invoiceNo) { this.invoiceNo = invoiceNo; }
        
        public String getPurchaserName() { return purchaserName; }
        public void setPurchaserName(String purchaserName) { this.purchaserName = purchaserName; }
        
        public String getSellerName() { return sellerName; }
        public void setSellerName(String sellerName) { this.sellerName = sellerName; }
        
        public Integer getInvoiceType() { return invoiceType; }
        public void setInvoiceType(Integer invoiceType) { this.invoiceType = invoiceType; }
        
        public Integer getVerificationStatus() { return verificationStatus; }
        public void setVerificationStatus(Integer verificationStatus) { this.verificationStatus = verificationStatus; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    }
}