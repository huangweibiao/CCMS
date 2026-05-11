package com.ccms.entity.finance;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务报表实体类
 * 对应表名：finance_report
 */
@Entity
@Table(name = "finance_report")
public class FinanceReport extends BaseEntity {

    /**
     * 报表ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    /**
     * 报表名称
     */
    @Column(name = "report_name", length = 64, nullable = false)
    private String reportName;

    /**
     * 报表类型
     * EXPENSE_REPORT-费用报表
     * PAYMENT_REPORT-支付报表
     * DEPARTMENT_REPORT-部门报表
     * ACCOUNT_REPORT-科目报表
     * CASH_FLOW_REPORT-现金流报表
     * PROFIT_REPORT-利润报表
     * BALANCE_SHEET_REPORT-资产负债报表
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type", length = 32, nullable = false)
    private FinanceReportType reportType;

    /**
     * 报表周期
     * DAILY-日
     * WEEKLY-周
     * MONTHLY-月
     * QUARTERLY-季度
     * YEARLY-年度
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "report_period", length = 16, nullable = false)
    private FinanceReportPeriod reportPeriod;

    /**
     * 报表日期
     */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    /**
     * 报表开始日期
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * 报表结束日期
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Column(name = "department_name", length = 64)
    private String departmentName;

    /**
     * 申请人ID
     */
    @Column(name = "applicant_id")
    private Long applicantId;

    /**
     * 申请人姓名
     */
    @Column(name = "applicant_name", length = 32)
    private String applicantName;

    /**
     * 审核状态
     * 0-待审核 1-已审核 2-已拒绝
     */
    @Column(name = "approval_status", nullable = false)
    private Integer approvalStatus;

    /**
     * 审核人ID
     */
    @Column(name = "approval_user_id")
    private Long approvalUserId;

    /**
     * 审核人姓名
     */
    @Column(name = "approval_user_name", length = 32)
    private String approvalUserName;

    /**
     * 审核时间
     */
    @Column(name = "approval_time")
    private LocalDateTime approvalTime;

    /**
     * 审核意见
     */
    @Column(name = "approval_comment", length = 512, columnDefinition = "TEXT")
    private String approvalComment;

    /**
     * 报表数据（JSON格式）
     */
    @Column(name = "report_data", columnDefinition = "TEXT", nullable = false)
    private String reportData;

    /**
     * 报表文件路径
     */
    @Column(name = "report_file_path", length = 256)
    private String reportFilePath;

    /**
     * 生成状态
     * 0-待生成 1-已生成 2-生成失败
     */
    @Column(name = "generation_status", nullable = false)
    private Integer generationStatus;

    /**
     * 下载次数
     */
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount;

    /**
     * 是否已删除
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    /**
     * 备注
     */
    @Column(name = "remark", length = 1024, columnDefinition = "TEXT")
    private String remark;

    /**
     * 创建时间
     */
    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @Column(name = "create_by", nullable = false)
    private Long createBy;

    /**
     * 更新人
     */
    @Column(name = "update_by", nullable = false)
    private Long updateBy;
}