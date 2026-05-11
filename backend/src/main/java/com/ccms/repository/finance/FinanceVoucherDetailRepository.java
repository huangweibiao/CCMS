package com.ccms.repository.finance;

import com.ccms.entity.finance.FinanceVoucherDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 财务凭证明细Repository接口
 * 对应表名：finance_voucher_detail
 */
public interface FinanceVoucherDetailRepository extends JpaRepository<FinanceVoucherDetail, Long> {

    /**
     * 根据凭证ID查询明细
     * @param voucherId 凭证ID
     * @return 凭证明细列表
     */
    List<FinanceVoucherDetail> findByVoucherId(Long voucherId);

    /**
     * 根据凭证ID查询明细数量
     * @param voucherId 凭证ID
     * @return 凭证明细数量
     */
    @Query("SELECT COUNT(d) FROM FinanceVoucherDetail d WHERE d.voucherId = :voucherId")
    Long countByVoucherId(@Param("voucherId") Long voucherId);

    /**
     * 根据科目编码查询
     * @param accountCode 科目编码
     * @return 凭证明细列表
     */
    @Query("SELECT d FROM FinanceVoucherDetail d WHERE d.accountCode = :accountCode ORDER BY d.createTime DESC")
    List<FinanceVoucherDetail> findByAccountCode(@Param("accountCode") String accountCode);

    /**
     * 根据成本中心查询
     * @param costCenter 成本中心
     * @return 凭证明细列表
     */
    @Query("SELECT d FROM FinanceVoucherDetail d WHERE d.costCenter = :costCenter ORDER BY d.createTime DESC")
    List<FinanceVoucherDetail> findByCostCenter(@Param("costCenter") String costCenter);

    /**
     * 根据部门查询
     * @param department 部门
     * @return 凭证明细列表
     */
    @Query("SELECT d FROM FinanceVoucherDetail d WHERE d.department = :department ORDER BY d.createTime DESC")
    List<FinanceVoucherDetail> findByDepartment(@Param("department") String department);
}