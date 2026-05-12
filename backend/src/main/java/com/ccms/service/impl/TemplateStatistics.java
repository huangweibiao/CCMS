package com.ccms.service.impl;

import java.util.Objects;

public class TemplateStatistics {
    private long totalUsage;
    private long successCount;
    private long failureCount;
    private double successRate;
    
    public TemplateStatistics() {
        this.totalUsage = 0;
        this.successCount = 0;
        this.failureCount = 0;
        this.successRate = 0.0;
    }

    public long getTotalUsage() {
        return totalUsage;
    }

    public void setTotalUsage(long totalUsage) {
        this.totalUsage = totalUsage;
    }

    public long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(long successCount) {
        this.successCount = successCount;
    }

    public long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(long failureCount) {
        this.failureCount = failureCount;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TemplateStatistics that = (TemplateStatistics) o;
        return totalUsage == that.totalUsage && successCount == that.successCount 
                && failureCount == that.failureCount && Double.compare(successRate, that.successRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalUsage, successCount, failureCount, successRate);
    }

    @Override
    public String toString() {
        return "TemplateStatistics{" +
                "totalUsage=" + totalUsage +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                ", successRate=" + successRate +
                '}';
    }
}