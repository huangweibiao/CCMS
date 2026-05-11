package com.ccms.repository.finance;

import com.ccms.entity.finance.FinanceVoucher;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 财务凭证Repository接口
 * 对应表名：finance_voucher
 */
@Repository
public interface FinanceVoucherRepository extends BaseRepository<FinanceVoucher, Long> {

    /**
     * 根据凭证编号查询
     * @param voucherNo 凭证编号
     * @return 凭证信息
     */
    Optional<FinanceVoucher> findByVoucherNo(String voucherNo);

    /**
     * 根据业务类型查询
     * @param businessType 业务类型
     * @return 凭证列表
     */
    List<FinanceVoucher> findByBusinessType(String businessType);

    /**
     * 根据业务单ID和业务类型查询
     * @param businessId 业务单ID
     * @param businessType 业务类型
     * @return 凭证信息
     */
    Optional<FinanceVoucher> findByBusinessIdAndBusinessType(Long businessId, String businessType);

    /**
     * 根据凭证状态查询
     * @param status 凭证状态
     * @return 凭证列表
     */
    List<FinanceVoucher> findByStatus(Integer status);

    /**
     * 根据记账日期范围查询
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 凭证列表
     */
    @Query("SELECT v FROM FinanceVoucher v WHERE v.accountingDate BETWEEN :startDate AND :endDate ORDER BY v.accountingDate DESC")
    List<FinanceVoucher> findByAccountingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据创建人ID查询
     * @param createBy 创建人ID
     * @return 凭证列表
     */
    List<FinanceVoucher> findByCreateBy(Long createBy);

    /**
     * 根据模板ID查询
     * @param templateId 模板ID
     * @return 凭证列表
     */
    List<FinanceVoucher> findByTemplateId(Long templateId);

    /**
     * 统计指定日期范围内的凭证数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 凭证数量
     */
    @Query("SELECT COUNT(v) FROM FinanceVoucher v WHERE v.accountingDate BETWEEN :startDate AND :endDate")
    Long countByAccountingDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据业务单编号模糊查询
     * @param businessNo 业务单编号
     * @return 凭证列表
     */
    @Query("SELECT v FROM FinanceVoucher v WHERE v.businessNo LIKE %:businessNo% ORDER BY v.voucherDate DESC")
    List<FinanceVoucher> findByBusinessNoLike(@Param("businessNo") String businessNo);

    /**
     * 查询待审核的凭证
     * @return 待审核凭证列表
     */
    @Query("SELECT v FROM FinanceVoucher v WHERE v.status = 2 ORDER BY v.createTime DESC")
    List<FinanceVoucher> findPendingApprovalVouchers();

    /**
     * 查询已记账的凭证
     * @param limit 限制数量
     * @return 已记账凭证列表
     */
    @Query("SELECT v FROM FinanceVoucher v WHERE v.status = 3 ORDER BY v.accountingDate DESC LIMIT :limit")
    List<FinanceVoucher> findPostedVouchers(@Param("limit") Integer limit);
}