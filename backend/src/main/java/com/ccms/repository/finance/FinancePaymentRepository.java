package com.ccms.repository.finance;

import com.ccms.entity.finance.FinancePayment;
import com.ccms.entity.finance.FinancePaymentMethod;
import com.ccms.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 财务支付单据Repository接口
 * 对应表名：finance_payment
 */
public interface FinancePaymentRepository extends BaseRepository<FinancePayment, Long> {

    /**
     * 根据支付单据编号查询
     * @param paymentNo 支付单据编号
     * @return 支付单据信息
     */
    FinancePayment findByPaymentNo(String paymentNo);

    /**
     * 根据业务类型查询支付单据
     * @param businessType 业务类型
     * @param pageable 分页信息
     * @return 支付单据分页列表
     */
    Page<FinancePayment> findByBusinessType(String businessType, Pageable pageable);

    /**
     * 根据业务类型查询支付单据
     * @param businessType 业务类型
     * @return 支付单据列表
     */
    List<FinancePayment> findByBusinessType(String businessType);

    /**
     * 根据业务单据ID查询支付单据
     * @param businessId 业务单据ID
     * @return 支付单据信息
     */
    FinancePayment findByBusinessId(Long businessId);

    /**
     * 根据业务单据编号查询支付单据（分页）
     * @param businessNo 业务单据编号
     * @param pageable 分页信息
     * @return 支付单据分页信息
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.businessNo LIKE %:businessNo%")
    Page<FinancePayment> findByBusinessNoLike(@Param("businessNo") String businessNo, Pageable pageable);

    /**
     * 根据业务单据编号查询支付单据
     * @param businessNo 业务单据编号
     * @return 支付单据信息
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.businessNo LIKE %:businessNo%")
    FinancePayment findByBusinessNoLike(@Param("businessNo") String businessNo);

    /**
     * 根据支付状态查询支付单据（分页）
     * @param paymentStatus 支付状态
     * @param pageable 分页信息
     * @return 支付单据分页列表
     */
    Page<FinancePayment> findByPaymentStatus(Integer paymentStatus, Pageable pageable);

    /**
     * 根据支付状态查询支付单据
     * @param paymentStatus 支付状态
     * @return 支付单据列表
     */
    List<FinancePayment> findByPaymentStatus(Integer paymentStatus);

    /**
     * 根据支付方式查询支付单据
     * @param paymentMethod 支付方式
     * @return 支付单据列表
     */
    List<FinancePayment> findByPaymentMethod(FinancePaymentMethod paymentMethod);

    /**
     * 根据申请部门ID查询支付单据
     * @param applyDepartmentId 申请部门ID
     * @return 支付单据列表
     */
    List<FinancePayment> findByApplyDepartmentId(Long applyDepartmentId);

    /**
     * 根据申请人工号查询支付单据
     * @param applyEmployeeId 申请人工号
     * @return 支付单据列表
     */
    List<FinancePayment> findByApplyEmployeeId(Long applyEmployeeId);

    /**
     * 根据支付日期范围查询支付单据（分页）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageable 分页信息
     * @return 支付单据分页列表
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Page<FinancePayment> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);

    /**
     * 根据支付日期范围查询支付单据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付单据列表
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate ORDER BY p.paymentDate DESC")
    List<FinancePayment> findByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 统计指定日期范围内的支付单据数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付单据数量
     */
    @Query("SELECT COUNT(p) FROM FinancePayment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    Long countByPaymentDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 查询待审批的支付单据
     * @return 待审批支付单据列表
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.paymentStatus = 1 ORDER BY p.createTime DESC")
    List<FinancePayment> findPendingApprovalPayments();

    /**
     * 查询已审批待支付的支付单据
     * @return 已审批待支付单据列表
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.paymentStatus = 2 ORDER BY p.createTime DESC")
    List<FinancePayment> findApprovedPendingPayments();

    /**
     * 统计指定状态和日期范围内的支付总金额
     * @param paymentStatus 支付状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付总金额
     */
    @Query("SELECT SUM(p.amount) FROM FinancePayment p WHERE p.paymentStatus = :paymentStatus AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumAmountByPaymentStatusAndPaymentDateBetween(@Param("paymentStatus") Integer paymentStatus, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据凭证生成状态查询支付单据
     * @param voucherGenerationStatus 凭证生成状态
     * @return 支付单据列表
     */
    List<FinancePayment> findByVoucherGenerationStatus(Integer voucherGenerationStatus);

    /**
     * 查询支付金额最大的支付单据
     * @return 支付单据信息
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.paymentStatus = 3 ORDER BY p.amount DESC")
    FinancePayment findTopByAmountByPaymentStatusOrderByIdAsc();

    /**
     * 根据收款人姓名查询支付单据
     * @param payeeName 收款人姓名
     * @return 支付单据列表
     */
    @Query("SELECT p FROM FinancePayment p WHERE p.payeeName LIKE %:payeeName% ORDER BY p.createTime DESC")
    List<FinancePayment> findByPayeeNameLike(@Param("payeeName") String payeeName);
}