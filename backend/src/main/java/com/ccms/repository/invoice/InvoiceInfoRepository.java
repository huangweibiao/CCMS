package com.ccms.repository.invoice;

import com.ccms.entity.invoice.InvoiceInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 发票信息数据访问接口
 */
@Repository
public interface InvoiceInfoRepository extends JpaRepository<InvoiceInfo, Long> {

    Optional<InvoiceInfo> findByInvoiceNo(String invoiceNo);

    List<InvoiceInfo> findByInvoiceType(Integer invoiceType);

    List<InvoiceInfo> findByVerificationStatus(Integer verificationStatus);

    Page<InvoiceInfo> findByVerificationStatus(Integer verificationStatus, Pageable pageable);

    List<InvoiceInfo> findByReimburseId(Long reimburseId);

    @Query("SELECT ii FROM InvoiceInfo ii WHERE ii.verificationStatus = 0")
    List<InvoiceInfo> findUnverifiedInvoices();

    @Query("SELECT COUNT(ii) FROM InvoiceInfo ii WHERE ii.verificationStatus = :status")
    Long countByVerificationStatus(@Param("status") Integer status);

    @Query("SELECT ii FROM InvoiceInfo ii WHERE ii.invoiceNo = :invoiceNo AND ii.invoiceCode = :invoiceCode")
    Optional<InvoiceInfo> findByInvoiceNoAndCode(@Param("invoiceNo") String invoiceNo, @Param("invoiceCode") String invoiceCode);
}
