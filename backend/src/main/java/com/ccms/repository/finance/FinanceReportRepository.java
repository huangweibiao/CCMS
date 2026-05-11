package com.ccms.repository.finance;

import com.ccms.entity.finance.FinanceReport;
import com.ccms.entity.finance.FinanceReportType;
import com.ccms.entity.finance.FinanceReportPeriod;
import com.ccms.repository.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 财务报表Repository接口
 * 对应表名：finance_report
 */
public interface FinanceReportRepository extends BaseRepository<FinanceReport, Long> {

    /**
     * 根据报表类型查询报表
     * @param reportType 报表类型
     * @return 报表列表
     */
    List<FinanceReport> findByReportType(FinanceReportType reportType);

    /**
     * 根据报表类型查询最新的报表
     * @param reportType 报表类型
     * @return 报表列表
     */
    @Query("SELECT r FROM FinanceReport r WHERE r.reportType = :reportType AND r.isDeleted = false ORDER BY r.createTime DESC")
    List<FinanceReport> findActiveReportsByType(@Param("reportType") FinanceReportType reportType);

    /**
     * 根据报表周期查询报表
     * @param reportPeriod 报表周期
     * @return 报表列表
     */
    @Query("SELECT r FROM FinanceReport r WHERE r.reportPeriod = :reportPeriod AND r.isDeleted = false ORDER BY r.createTime DESC")
    List<FinanceReport> findReportsByPeriod(@Param("reportPeriod") FinanceReportPeriod reportPeriod);

    /**
     * 根据日期范围查询报表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表列表
     */
    @Query("SELECT r FROM FinanceReport r WHERE r.reportDate BETWEEN :startDate AND :endDate AND r.isDeleted = false ORDER BY r.reportDate DESC")
    List<FinanceReport> findReportsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 某计日期范围内的报表数量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表数量
     */
    @Query("SELECT COUNT(r) FROM FinanceReport r WHERE r.reportDate BETWEEN :startDate AND :endDate AND r.isDeleted = false")
    Long countReportsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * 根据部门ID查询报表
     * @param departmentId 部门ID
     * @return 报表列表
     */
    List<FinanceReport> findByDepartmentId(Long departmentId);

    /**
     * 根据申请人ID查询报表
     * @param applicantId 申请人ID
     * @return 报表列表
     */
    List<FinanceReport> findByApplicantId(Long applicantId);

    /**
     * 根据审批状态查询报表
     * @param approvalStatus 审批状态
     * @return 报表列表
     */
    List<FinanceReport> findByApprovalStatus(Integer approvalStatus);

    /**
     * 根据报表名称查询报表
     * @param reportName 报表名称
     * @return 报表列表
     */
    @Query("SELECT r FROM FinanceReport r WHERE r.reportName LIKE %:reportName% AND r.isDeleted = false ORDER BY r.createTime DESC")
    List<FinanceReport> findByReportNameLike(@Param("reportName") String reportName);

    /**
     * 查询待审核的报表
     * @return 待审核报表列表
     */
    @Query("SELECT r FROM FinanceReport r WHERE r.approvalStatus = 0 AND r.isDeleted = false ORDER BY r.createTime DESC")
    List<FinanceReport> findPendingApprovalReports();

    /**
     * 查询已生成的报表
     * @param generationStatus 生成状态
     * @return 报表列表
     */
    @Query("SELECT r FROM FinanceReport r WHERE r.generationStatus = 1 AND r.isDeleted = false ORDER BY r.createTime DESC")
    List<FinanceReport> findGeneratedReports();

    /**
     * 统计报表数量
     * @param reportType 报表类型
     * @return 报表数量
     */
    @Query("SELECT COUNT(r) FROM FinanceReport r WHERE r.reportType = :reportType AND r.isDeleted = false")
    Long countByReportType(@Param("reportType") FinanceReportType reportType);

    /**
     * 统计报表数据总和
     * @param reportType 报表类型
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 报表数据总和
     */
    @Query("SELECT r.reportData FROM FinanceReport r WHERE r.reportType = :reportType AND r.reportDate BETWEEN :startDate AND :endDate")
    List<String> findReportDataByTypeAndDateRange(@Param("reportType") FinanceReportType reportType, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}