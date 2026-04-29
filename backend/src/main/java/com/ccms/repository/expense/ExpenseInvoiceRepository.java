package com.ccms.repository.expense;

import com.ccms.entity.expense.ExpenseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 发票表数据访问接口
 * 
 * @author 系统生成
 */
@Repository
public interface ExpenseInvoiceRepository extends JpaRepository<ExpenseInvoice, Long> {
    
    /**
     * 根据报销明细ID查询发票列表
     * 
     * @param reimburseDetailId 报销明细ID
     * @return 发票列表
     */
    List<ExpenseInvoice> findByReimburseDetailId(Long reimburseDetailId);
    
    /**
     * 根据发票号码查询发票
     * 
     * @param invoiceNo 发票号码
     * @return 发票列表
     */
    List<ExpenseInvoice> findByInvoiceNo(String invoiceNo);
    
    /**
     * 根据发票状态查询发票
     * 
     * @param verifyStatus 验真状态
     * @return 发票列表
     */
    List<ExpenseInvoice> findByVerifyStatus(Integer verifyStatus);
    
    /**
     * 根据报销单ID查询所有相关发票
     * 
     * @param reimburseId 报销单ID
     * @return 发票列表
     */
    @Query("SELECT i FROM ExpenseInvoice i JOIN ExpenseReimburseDetail d ON i.reimburseDetailId = d.id WHERE d.reimburseId = :reimburseId")
    List<ExpenseInvoice> findByReimburseId(@Param("reimburseId") Long reimburseId);
    
    /**
     * 统计报销单的总发票金额
     * 
     * @param reimburseId 报销单ID
     * @return 总发票金额
     */
    @Query("SELECT SUM(i.invoiceAmount) FROM ExpenseInvoice i JOIN ExpenseReimburseDetail d ON i.reimburseDetailId = d.id WHERE d.reimburseId = :reimburseId")
    Double calculateTotalInvoiceAmountByReimburse(@Param("reimburseId") Long reimburseId);
    
    /**
     * 根据发票类型统计数量
     * 
     * @param invoiceType 发票类型
     * @return 数量
     */
    Long countByInvoiceType(Integer invoiceType);
}
