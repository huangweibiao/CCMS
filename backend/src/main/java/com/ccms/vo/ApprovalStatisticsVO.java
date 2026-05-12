package com.ccms.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审批统计视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatisticsVO {
    
    /**
     * 总审批数量
     */
    private Long totalCount;
    
    /**
     * 待审批数量
     */
    private Long pendingCount;
    
    /**
     * 已审批数量
     */
    private Long approvedCount;
    
    /**
     * 已拒绝数量
     */
    private Long rejectedCount;
    
    /**
     * 已取消数量
     */
    private Long canceledCount;
    
    /**
     * 超时数量
     */
    private Long timeoutCount;
    
    /**
     * 平均审批时长（小时）
     */
    private Double averageDuration;
    
    /**
     * 最长审批时长（小时）
     */
    private Double maxDuration;
    
    /**
     * 最短审批时长（小时）
     */
    private Double minDuration;
    
    /**
     * 审批通过率（百分比）
     */
    private Double approvalRate;
    
    /**
     * 审批拒绝率（百分比）
     */
    private Double rejectionRate;
    
    /**
     * 超时率（百分比）
     */
    private Double timeoutRate;
    
    /**
     * 业务类型分布
     */
    private List<BusinessTypeStatistic> businessTypeStats;
    
    /**
     * 时间趋势数据
     */
    private List<TimeTrendData> timeTrendData;
    
    /**
     * 审批人效率统计
     */
    private List<ApproverEfficiencyStat> approverEfficiencyStats;
    
    /**
     * 统计时间范围
     */
    private String timeRange;
    
    /**
     * 数据更新时间
     */
    private String lastUpdated;
}

/**
 * 业务类型统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BusinessTypeStatistic {
    private String businessType;
    private String businessTypeName;
    private Long count;
    private Double percentage;
    private Double avgDuration;
    private Double approvalRate;
}

/**
 * 时间趋势数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TimeTrendData {
    private String timePeriod; // 日: 2023-01-01, 月: 2023-01, 年: 2023
    private Long totalCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Double avgDuration;
}

/**
 * 审批人效率统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApproverEfficiencyStat {
    private Long approverId;
    private String approverName;
    private String department;
    private Long totalApprovals;
    private Double avgProcessingHours;
    private Double approvalRate;
    private Integer efficiencyRank;
}