package com.ccms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批统计DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalStatistics {
    
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
     * 审批通过率
     */
    private Double approvalRate;
    
    /**
     * 审批拒绝率
     */
    private Double rejectionRate;
    
    /**
     * 超时率
     */
    private Double timeoutRate;
    
    /**
     * 用户统计相关数据（可选）
     */
    private UserApprovalStatistics userStats;
    
    /**
     * 业务统计相关数据（可选）
     */
    private BusinessApprovalStatistics businessStats;
    
    /**
     * 时间范围统计数据（可选）
     */
    private TimeRangeStatistics timeRangeStats;
}

/**
 * 用户审批统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class UserApprovalStatistics {
    
    /**
     * 审批用户ID
     */
    private Long userId;
    
    /**
     * 用户姓名
     */
    private String userName;
    
    /**
     * 审批总数量
     */
    private Long totalApprovals;
    
    /**
     * 平均审批时长（小时）
     */
    private Double avgApprovalDuration;
    
    /**
     * 审批通过率
     */
    private Double approvalRate;
    
    /**
     * 待审批数量
     */
    private Long pendingCount;
    
    /**
     * 审批效率排名
     */
    private Integer efficiencyRank;
}

/**
 * 业务审批统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class BusinessApprovalStatistics {
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 业务类型名称
     */
    private String businessTypeName;
    
    /**
     * 总审批数量
     */
    private Long totalCount;
    
    /**
     * 平均审批时长（小时）
     */
    private Double avgDuration;
    
    /**
     * 通过率
     */
    private Double passRate;
    
    /**
     * 平均审批金额
     */
    private Double avgAmount;
    
    /**
     * 总审批金额
     */
    private Double totalAmount;
}

/**
 * 时间范围统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TimeRangeStatistics {
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;
    
    /**
     * 每日统计
     */
    private DailyStatistics dailyStats;
    
    /**
     * 每周统计
     */
    private WeeklyStatistics weeklyStats;
    
    /**
     * 每月统计
     */
    private MonthlyStatistics monthlyStats;
}

/**
 * 每日统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class DailyStatistics {
    private String date;
    private Long count;
    private Double avgDuration;
}

/**
 * 每周统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class WeeklyStatistics {
    private String week;
    private Long count;
    private Double avgDuration;
}

/**
 * 每月统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class MonthlyStatistics {
    private String month;
    private Long count;
    private Double avgDuration;
}