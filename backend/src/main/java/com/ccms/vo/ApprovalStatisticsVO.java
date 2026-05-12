package com.ccms.vo;

import java.util.List;
import java.util.Objects;

/**
 * 审批统计视图对象
 */
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

    public ApprovalStatisticsVO() {}

    public ApprovalStatisticsVO(Long totalCount, Long pendingCount, Long approvedCount, Long rejectedCount, 
                               Long canceledCount, Long timeoutCount, Double averageDuration, Double maxDuration, 
                               Double minDuration, Double approvalRate, Double rejectionRate, Double timeoutRate,
                               List<BusinessTypeStatistic> businessTypeStats, List<TimeTrendData> timeTrendData,
                               List<ApproverEfficiencyStat> approverEfficiencyStats, String timeRange, String lastUpdated) {
        this.totalCount = totalCount;
        this.pendingCount = pendingCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.canceledCount = canceledCount;
        this.timeoutCount = timeoutCount;
        this.averageDuration = averageDuration;
        this.maxDuration = maxDuration;
        this.minDuration = minDuration;
        this.approvalRate = approvalRate;
        this.rejectionRate = rejectionRate;
        this.timeoutRate = timeoutRate;
        this.businessTypeStats = businessTypeStats;
        this.timeTrendData = timeTrendData;
        this.approverEfficiencyStats = approverEfficiencyStats;
        this.timeRange = timeRange;
        this.lastUpdated = lastUpdated;
    }

    // Getter and Setter methods
    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getPendingCount() {
        return pendingCount;
    }

    public void setPendingCount(Long pendingCount) {
        this.pendingCount = pendingCount;
    }

    public Long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(Long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public Long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(Long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public Long getCanceledCount() {
        return canceledCount;
    }

    public void setCanceledCount(Long canceledCount) {
        this.canceledCount = canceledCount;
    }

    public Long getTimeoutCount() {
        return timeoutCount;
    }

    public void setTimeoutCount(Long timeoutCount) {
        this.timeoutCount = timeoutCount;
    }

    public Double getAverageDuration() {
        return averageDuration;
    }

    public void setAverageDuration(Double averageDuration) {
        this.averageDuration = averageDuration;
    }

    public Double getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(Double maxDuration) {
        this.maxDuration = maxDuration;
    }

    public Double getMinDuration() {
        return minDuration;
    }

    public void setMinDuration(Double minDuration) {
        this.minDuration = minDuration;
    }

    public Double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(Double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public Double getRejectionRate() {
        return rejectionRate;
    }

    public void setRejectionRate(Double rejectionRate) {
        this.rejectionRate = rejectionRate;
    }

    public Double getTimeoutRate() {
        return timeoutRate;
    }

    public void setTimeoutRate(Double timeoutRate) {
        this.timeoutRate = timeoutRate;
    }

    public List<BusinessTypeStatistic> getBusinessTypeStats() {
        return businessTypeStats;
    }

    public void setBusinessTypeStats(List<BusinessTypeStatistic> businessTypeStats) {
        this.businessTypeStats = businessTypeStats;
    }

    public List<TimeTrendData> getTimeTrendData() {
        return timeTrendData;
    }

    public void setTimeTrendData(List<TimeTrendData> timeTrendData) {
        this.timeTrendData = timeTrendData;
    }

    public List<ApproverEfficiencyStat> getApproverEfficiencyStats() {
        return approverEfficiencyStats;
    }

    public void setApproverEfficiencyStats(List<ApproverEfficiencyStat> approverEfficiencyStats) {
        this.approverEfficiencyStats = approverEfficiencyStats;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public void setTimeRange(String timeRange) {
        this.timeRange = timeRange;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApprovalStatisticsVO that = (ApprovalStatisticsVO) o;
        return Objects.equals(totalCount, that.totalCount) && Objects.equals(pendingCount, that.pendingCount) 
                && Objects.equals(approvedCount, that.approvedCount) && Objects.equals(rejectedCount, that.rejectedCount) 
                && Objects.equals(canceledCount, that.canceledCount) && Objects.equals(timeoutCount, that.timeoutCount) 
                && Objects.equals(averageDuration, that.averageDuration) && Objects.equals(maxDuration, that.maxDuration) 
                && Objects.equals(minDuration, that.minDuration) && Objects.equals(approvalRate, that.approvalRate) 
                && Objects.equals(rejectionRate, that.rejectionRate) && Objects.equals(timeoutRate, that.timeoutRate) 
                && Objects.equals(businessTypeStats, that.businessTypeStats) && Objects.equals(timeTrendData, that.timeTrendData) 
                && Objects.equals(approverEfficiencyStats, that.approverEfficiencyStats) && Objects.equals(timeRange, that.timeRange) 
                && Objects.equals(lastUpdated, that.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCount, pendingCount, approvedCount, rejectedCount, canceledCount, timeoutCount, 
                averageDuration, maxDuration, minDuration, approvalRate, rejectionRate, timeoutRate,
                businessTypeStats, timeTrendData, approverEfficiencyStats, timeRange, lastUpdated);
    }

    @Override
    public String toString() {
        return "ApprovalStatisticsVO{" +
                "totalCount=" + totalCount +
                ", pendingCount=" + pendingCount +
                ", approvedCount=" + approvedCount +
                ", rejectedCount=" + rejectedCount +
                ", canceledCount=" + canceledCount +
                ", timeoutCount=" + timeoutCount +
                ", averageDuration=" + averageDuration +
                ", maxDuration=" + maxDuration +
                ", minDuration=" + minDuration +
                ", approvalRate=" + approvalRate +
                ", rejectionRate=" + rejectionRate +
                ", timeoutRate=" + timeoutRate +
                ", businessTypeStats=" + businessTypeStats +
                ", timeTrendData=" + timeTrendData +
                ", approverEfficiencyStats=" + approverEfficiencyStats +
                ", timeRange='" + timeRange + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                '}';
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long totalCount;
        private Long pendingCount;
        private Long approvedCount;
        private Long rejectedCount;
        private Long canceledCount;
        private Long timeoutCount;
        private Double averageDuration;
        private Double maxDuration;
        private Double minDuration;
        private Double approvalRate;
        private Double rejectionRate;
        private Double timeoutRate;
        private List<BusinessTypeStatistic> businessTypeStats;
        private List<TimeTrendData> timeTrendData;
        private List<ApproverEfficiencyStat> approverEfficiencyStats;
        private String timeRange;
        private String lastUpdated;

        public Builder totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder pendingCount(Long pendingCount) {
            this.pendingCount = pendingCount;
            return this;
        }

        public Builder approvedCount(Long approvedCount) {
            this.approvedCount = approvedCount;
            return this;
        }

        public Builder rejectedCount(Long rejectedCount) {
            this.rejectedCount = rejectedCount;
            return this;
        }

        public Builder canceledCount(Long canceledCount) {
            this.canceledCount = canceledCount;
            return this;
        }

        public Builder timeoutCount(Long timeoutCount) {
            this.timeoutCount = timeoutCount;
            return this;
        }

        public Builder averageDuration(Double averageDuration) {
            this.averageDuration = averageDuration;
            return this;
        }

        public Builder maxDuration(Double maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }

        public Builder minDuration(Double minDuration) {
            this.minDuration = minDuration;
            return this;
        }

        public Builder approvalRate(Double approvalRate) {
            this.approvalRate = approvalRate;
            return this;
        }

        public Builder rejectionRate(Double rejectionRate) {
            this.rejectionRate = rejectionRate;
            return this;
        }

        public Builder timeoutRate(Double timeoutRate) {
            this.timeoutRate = timeoutRate;
            return this;
        }

        public Builder businessTypeStats(List<BusinessTypeStatistic> businessTypeStats) {
            this.businessTypeStats = businessTypeStats;
            return this;
        }

        public Builder timeTrendData(List<TimeTrendData> timeTrendData) {
            this.timeTrendData = timeTrendData;
            return this;
        }

        public Builder approverEfficiencyStats(List<ApproverEfficiencyStat> approverEfficiencyStats) {
            this.approverEfficiencyStats = approverEfficiencyStats;
            return this;
        }

        public Builder timeRange(String timeRange) {
            this.timeRange = timeRange;
            return this;
        }

        public Builder lastUpdated(String lastUpdated) {
            this.lastUpdated = lastUpdated;
            return this;
        }

        public ApprovalStatisticsVO build() {
            return new ApprovalStatisticsVO(totalCount, pendingCount, approvedCount, rejectedCount, 
                    canceledCount, timeoutCount, averageDuration, maxDuration, minDuration, 
                    approvalRate, rejectionRate, timeoutRate, businessTypeStats, timeTrendData,
                    approverEfficiencyStats, timeRange, lastUpdated);
        }
    }
}

/**
 * 业务类型统计
 */
class BusinessTypeStatistic {
    private String businessType;
    private String businessTypeName;
    private Long count;
    private Double percentage;
    private Double avgDuration;
    private Double approvalRate;

    public BusinessTypeStatistic() {}

    public BusinessTypeStatistic(String businessType, String businessTypeName, Long count, 
                                Double percentage, Double avgDuration, Double approvalRate) {
        this.businessType = businessType;
        this.businessTypeName = businessTypeName;
        this.count = count;
        this.percentage = percentage;
        this.avgDuration = avgDuration;
        this.approvalRate = approvalRate;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessTypeName() {
        return businessTypeName;
    }

    public void setBusinessTypeName(String businessTypeName) {
        this.businessTypeName = businessTypeName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }

    public Double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(Double approvalRate) {
        this.approvalRate = approvalRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusinessTypeStatistic that = (BusinessTypeStatistic) o;
        return Objects.equals(businessType, that.businessType) && Objects.equals(businessTypeName, that.businessTypeName) 
                && Objects.equals(count, that.count) && Objects.equals(percentage, that.percentage) 
                && Objects.equals(avgDuration, that.avgDuration) && Objects.equals(approvalRate, that.approvalRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(businessType, businessTypeName, count, percentage, avgDuration, approvalRate);
    }

    @Override
    public String toString() {
        return "BusinessTypeStatistic{" +
                "businessType='" + businessType + '\'' +
                ", businessTypeName='" + businessTypeName + '\'' +
                ", count=" + count +
                ", percentage=" + percentage +
                ", avgDuration=" + avgDuration +
                ", approvalRate=" + approvalRate +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String businessType;
        private String businessTypeName;
        private Long count;
        private Double percentage;
        private Double avgDuration;
        private Double approvalRate;

        public Builder businessType(String businessType) {
            this.businessType = businessType;
            return this;
        }

        public Builder businessTypeName(String businessTypeName) {
            this.businessTypeName = businessTypeName;
            return this;
        }

        public Builder count(Long count) {
            this.count = count;
            return this;
        }

        public Builder percentage(Double percentage) {
            this.percentage = percentage;
            return this;
        }

        public Builder avgDuration(Double avgDuration) {
            this.avgDuration = avgDuration;
            return this;
        }

        public Builder approvalRate(Double approvalRate) {
            this.approvalRate = approvalRate;
            return this;
        }

        public BusinessTypeStatistic build() {
            return new BusinessTypeStatistic(businessType, businessTypeName, count, percentage, avgDuration, approvalRate);
        }
    }
}

/**
 * 时间趋势数据
 */
class TimeTrendData {
    private String timePeriod; // 日: 2023-01-01, 月: 2023-01, 年: 2023
    private Long totalCount;
    private Long approvedCount;
    private Long rejectedCount;
    private Double avgDuration;

    public TimeTrendData() {}

    public TimeTrendData(String timePeriod, Long totalCount, Long approvedCount, Long rejectedCount, Double avgDuration) {
        this.timePeriod = timePeriod;
        this.totalCount = totalCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.avgDuration = avgDuration;
    }

    public String getTimePeriod() {
        return timePeriod;
    }

    public void setTimePeriod(String timePeriod) {
        this.timePeriod = timePeriod;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getApprovedCount() {
        return approvedCount;
    }

    public void setApprovedCount(Long approvedCount) {
        this.approvedCount = approvedCount;
    }

    public Long getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(Long rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public Double getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeTrendData that = (TimeTrendData) o;
        return Objects.equals(timePeriod, that.timePeriod) && Objects.equals(totalCount, that.totalCount) 
                && Objects.equals(approvedCount, that.approvedCount) && Objects.equals(rejectedCount, that.rejectedCount) 
                && Objects.equals(avgDuration, that.avgDuration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timePeriod, totalCount, approvedCount, rejectedCount, avgDuration);
    }

    @Override
    public String toString() {
        return "TimeTrendData{" +
                "timePeriod='" + timePeriod + '\'' +
                ", totalCount=" + totalCount +
                ", approvedCount=" + approvedCount +
                ", rejectedCount=" + rejectedCount +
                ", avgDuration=" + avgDuration +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String timePeriod;
        private Long totalCount;
        private Long approvedCount;
        private Long rejectedCount;
        private Double avgDuration;

        public Builder timePeriod(String timePeriod) {
            this.timePeriod = timePeriod;
            return this;
        }

        public Builder totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }

        public Builder approvedCount(Long approvedCount) {
            this.approvedCount = approvedCount;
            return this;
        }

        public Builder rejectedCount(Long rejectedCount) {
            this.rejectedCount = rejectedCount;
            return this;
        }

        public Builder avgDuration(Double avgDuration) {
            this.avgDuration = avgDuration;
            return this;
        }

        public TimeTrendData build() {
            return new TimeTrendData(timePeriod, totalCount, approvedCount, rejectedCount, avgDuration);
        }
    }
}

/**
 * 审批人效率统计
 */
class ApproverEfficiencyStat {
    private Long approverId;
    private String approverName;
    private String department;
    private Long totalApprovals;
    private Double avgProcessingHours;
    private Double approvalRate;
    private Integer efficiencyRank;

    public ApproverEfficiencyStat() {}

    public ApproverEfficiencyStat(Long approverId, String approverName, String department, Long totalApprovals, 
                                 Double avgProcessingHours, Double approvalRate, Integer efficiencyRank) {
        this.approverId = approverId;
        this.approverName = approverName;
        this.department = department;
        this.totalApprovals = totalApprovals;
        this.avgProcessingHours = avgProcessingHours;
        this.approvalRate = approvalRate;
        this.efficiencyRank = efficiencyRank;
    }

    public Long getApproverId() {
        return approverId;
    }

    public void setApproverId(Long approverId) {
        this.approverId = approverId;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Long getTotalApprovals() {
        return totalApprovals;
    }

    public void setTotalApprovals(Long totalApprovals) {
        this.totalApprovals = totalApprovals;
    }

    public Double getAvgProcessingHours() {
        return avgProcessingHours;
    }

    public void setAvgProcessingHours(Double avgProcessingHours) {
        this.avgProcessingHours = avgProcessingHours;
    }

    public Double getApprovalRate() {
        return approvalRate;
    }

    public void setApprovalRate(Double approvalRate) {
        this.approvalRate = approvalRate;
    }

    public Integer getEfficiencyRank() {
        return efficiencyRank;
    }

    public void setEfficiencyRank(Integer efficiencyRank) {
        this.efficiencyRank = efficiencyRank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApproverEfficiencyStat that = (ApproverEfficiencyStat) o;
        return Objects.equals(approverId, that.approverId) && Objects.equals(approverName, that.approverName) 
                && Objects.equals(department, that.department) && Objects.equals(totalApprovals, that.totalApprovals) 
                && Objects.equals(avgProcessingHours, that.avgProcessingHours) && Objects.equals(approvalRate, that.approvalRate) 
                && Objects.equals(efficiencyRank, that.efficiencyRank);
    }

    @Override
    public int hashCode() {
        return Objects.hash(approverId, approverName, department, totalApprovals, avgProcessingHours, approvalRate, efficiencyRank);
    }

    @Override
    public String toString() {
        return "ApproverEfficiencyStat{" +
                "approverId=" + approverId +
                ", approverName='" + approverName + '\'' +
                ", department='" + department + '\'' +
                ", totalApprovals=" + totalApprovals +
                ", avgProcessingHours=" + avgProcessingHours +
                ", approvalRate=" + approvalRate +
                ", efficiencyRank=" + efficiencyRank +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long approverId;
        private String approverName;
        private String department;
        private Long totalApprovals;
        private Double avgProcessingHours;
        private Double approvalRate;
        private Integer efficiencyRank;

        public Builder approverId(Long approverId) {
            this.approverId = approverId;
            return this;
        }

        public Builder approverName(String approverName) {
            this.approverName = approverName;
            return this;
        }

        public Builder department(String department) {
            this.department = department;
            return this;
        }

        public Builder totalApprovals(Long totalApprovals) {
            this.totalApprovals = totalApprovals;
            return this;
        }

        public Builder avgProcessingHours(Double avgProcessingHours) {
            this.avgProcessingHours = avgProcessingHours;
            return this;
        }

        public Builder approvalRate(Double approvalRate) {
            this.approvalRate = approvalRate;
            return this;
        }

        public Builder efficiencyRank(Integer efficiencyRank) {
            this.efficiencyRank = efficiencyRank;
            return this;
        }

        public ApproverEfficiencyStat build() {
            return new ApproverEfficiencyStat(approverId, approverName, department, totalApprovals, 
                    avgProcessingHours, approvalRate, efficiencyRank);
        }
    }
}