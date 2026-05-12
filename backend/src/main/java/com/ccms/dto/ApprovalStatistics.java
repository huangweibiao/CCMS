package com.ccms.dto;

/**
 * 审批统计DTO
 */
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
     * 平均审批时长（小时）
     */
    private Double averageDuration;
    
    /**
     * 超时数量
     */
    private Long timeoutCount = 0L;
    
    /**
     * 最长审批时长（小时）
     */
    private Double maxDuration = 0.0;
    
    /**
     * 最短审批时长（小时）
     */
    private Double minDuration = 0.0;
    
    /**
     * 审批通过率
     */
    private Double approvalRate = 0.0;
    
    /**
     * 审批拒绝率
     */
    private Double rejectionRate = 0.0;
    
    /**
     * 超时率
     */
    private Double timeoutRate = 0.0;
    
    // Constructors
    public ApprovalStatistics() {}
    
    // Simplified constructor for common use case
    public ApprovalStatistics(Long totalCount, Long pendingCount, Long approvedCount, Long rejectedCount, 
                             Long canceledCount, Double averageDuration) {
        this.totalCount = totalCount;
        this.pendingCount = pendingCount;
        this.approvedCount = approvedCount;
        this.rejectedCount = rejectedCount;
        this.canceledCount = canceledCount;
        this.averageDuration = averageDuration;
    }
    
    // Full constructor
    public ApprovalStatistics(Long totalCount, Long pendingCount, Long approvedCount, Long rejectedCount, 
                             Long canceledCount, Long timeoutCount, Double averageDuration, Double maxDuration, 
                             Double minDuration, Double approvalRate, Double rejectionRate, Double timeoutRate) {
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
    }
    
    // Builder static method
    public static ApprovalStatisticsBuilder builder() {
        return new ApprovalStatisticsBuilder();
    }
    
    // Getters and Setters
    public Long getTotalCount() { return totalCount; }
    public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
    
    public Long getPendingCount() { return pendingCount; }
    public void setPendingCount(Long pendingCount) { this.pendingCount = pendingCount; }
    
    public Long getApprovedCount() { return approvedCount; }
    public void setApprovedCount(Long approvedCount) { this.approvedCount = approvedCount; }
    
    public Long getRejectedCount() { return rejectedCount; }
    public void setRejectedCount(Long rejectedCount) { this.rejectedCount = rejectedCount; }
    
    public Long getCanceledCount() { return canceledCount; }
    public void setCanceledCount(Long canceledCount) { this.canceledCount = canceledCount; }
    
    public Long getTimeoutCount() { return timeoutCount; }
    public void setTimeoutCount(Long timeoutCount) { this.timeoutCount = timeoutCount; }
    
    public Double getAverageDuration() { return averageDuration; }
    public void setAverageDuration(Double averageDuration) { this.averageDuration = averageDuration; }
    
    public Double getMaxDuration() { return maxDuration; }
    public void setMaxDuration(Double maxDuration) { this.maxDuration = maxDuration; }
    
    public Double getMinDuration() { return minDuration; }
    public void setMinDuration(Double minDuration) { this.minDuration = minDuration; }
    
    public Double getApprovalRate() { return approvalRate; }
    public void setApprovalRate(Double approvalRate) { this.approvalRate = approvalRate; }
    
    public Double getRejectionRate() { return rejectionRate; }
    public void setRejectionRate(Double rejectionRate) { this.rejectionRate = rejectionRate; }
    
    public Double getTimeoutRate() { return timeoutRate; }
    public void setTimeoutRate(Double timeoutRate) { this.timeoutRate = timeoutRate; }
    
    // equals, hashCode, and toString methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        ApprovalStatistics that = (ApprovalStatistics) o;
        
        if (totalCount != null ? !totalCount.equals(that.totalCount) : that.totalCount != null) return false;
        if (pendingCount != null ? !pendingCount.equals(that.pendingCount) : that.pendingCount != null) return false;
        if (approvedCount != null ? !approvedCount.equals(that.approvedCount) : that.approvedCount != null) return false;
        if (rejectedCount != null ? !rejectedCount.equals(that.rejectedCount) : that.rejectedCount != null) return false;
        if (canceledCount != null ? !canceledCount.equals(that.canceledCount) : that.canceledCount != null) return false;
        if (timeoutCount != null ? !timeoutCount.equals(that.timeoutCount) : that.timeoutCount != null) return false;
        if (averageDuration != null ? !averageDuration.equals(that.averageDuration) : that.averageDuration != null) return false;
        if (maxDuration != null ? !maxDuration.equals(that.maxDuration) : that.maxDuration != null) return false;
        if (minDuration != null ? !minDuration.equals(that.minDuration) : that.minDuration != null) return false;
        if (approvalRate != null ? !approvalRate.equals(that.approvalRate) : that.approvalRate != null) return false;
        if (rejectionRate != null ? !rejectionRate.equals(that.rejectionRate) : that.rejectionRate != null) return false;
        return timeoutRate != null ? timeoutRate.equals(that.timeoutRate) : that.timeoutRate == null;
    }
    
    @Override
    public int hashCode() {
        int result = totalCount != null ? totalCount.hashCode() : 0;
        result = 31 * result + (pendingCount != null ? pendingCount.hashCode() : 0);
        result = 31 * result + (approvedCount != null ? approvedCount.hashCode() : 0);
        result = 31 * result + (rejectedCount != null ? rejectedCount.hashCode() : 0);
        result = 31 * result + (canceledCount != null ? canceledCount.hashCode() : 0);
        result = 31 * result + (timeoutCount != null ? timeoutCount.hashCode() : 0);
        result = 31 * result + (averageDuration != null ? averageDuration.hashCode() : 0);
        result = 31 * result + (maxDuration != null ? maxDuration.hashCode() : 0);
        result = 31 * result + (minDuration != null ? minDuration.hashCode() : 0);
        result = 31 * result + (approvalRate != null ? approvalRate.hashCode() : 0);
        result = 31 * result + (rejectionRate != null ? rejectionRate.hashCode() : 0);
        result = 31 * result + (timeoutRate != null ? timeoutRate.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "ApprovalStatistics{" +
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
                '}';
    }
    
    // Builder class
    public static class ApprovalStatisticsBuilder {
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
        
        public ApprovalStatisticsBuilder totalCount(Long totalCount) {
            this.totalCount = totalCount;
            return this;
        }
        
        public ApprovalStatisticsBuilder pendingCount(Long pendingCount) {
            this.pendingCount = pendingCount;
            return this;
        }
        
        public ApprovalStatisticsBuilder approvedCount(Long approvedCount) {
            this.approvedCount = approvedCount;
            return this;
        }
        
        public ApprovalStatisticsBuilder rejectedCount(Long rejectedCount) {
            this.rejectedCount = rejectedCount;
            return this;
        }
        
        public ApprovalStatisticsBuilder canceledCount(Long canceledCount) {
            this.canceledCount = canceledCount;
            return this;
        }
        
        public ApprovalStatisticsBuilder timeoutCount(Long timeoutCount) {
            this.timeoutCount = timeoutCount;
            return this;
        }
        
        public ApprovalStatisticsBuilder averageDuration(Double averageDuration) {
            this.averageDuration = averageDuration;
            return this;
        }
        
        public ApprovalStatisticsBuilder maxDuration(Double maxDuration) {
            this.maxDuration = maxDuration;
            return this;
        }
        
        public ApprovalStatisticsBuilder minDuration(Double minDuration) {
            this.minDuration = minDuration;
            return this;
        }
        
        public ApprovalStatisticsBuilder approvalRate(Double approvalRate) {
            this.approvalRate = approvalRate;
            return this;
        }
        
        public ApprovalStatisticsBuilder rejectionRate(Double rejectionRate) {
            this.rejectionRate = rejectionRate;
            return this;
        }
        
        public ApprovalStatisticsBuilder timeoutRate(Double timeoutRate) {
            this.timeoutRate = timeoutRate;
            return this;
        }
        
        public ApprovalStatistics build() {
            return new ApprovalStatistics(totalCount, pendingCount, approvedCount, rejectedCount,
                canceledCount, timeoutCount, averageDuration, maxDuration, minDuration, approvalRate, rejectionRate, timeoutRate);
        }
    }
}