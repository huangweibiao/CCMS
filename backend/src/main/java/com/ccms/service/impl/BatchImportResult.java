package com.ccms.service.impl;

import java.util.Objects;

public class BatchImportResult {
    private int totalRecords;
    private int successCount;
    private int failureCount;
    
    public BatchImportResult() {
        this.totalRecords = 0;
        this.successCount = 0;
        this.failureCount = 0;
    }

    public BatchImportResult(int totalRecords, int successCount, int failureCount) {
        this.totalRecords = totalRecords;
        this.successCount = successCount;
        this.failureCount = failureCount;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchImportResult that = (BatchImportResult) o;
        return totalRecords == that.totalRecords && 
               successCount == that.successCount && 
               failureCount == that.failureCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalRecords, successCount, failureCount);
    }

    @Override
    public String toString() {
        return "BatchImportResult{" +
                "totalRecords=" + totalRecords +
                ", successCount=" + successCount +
                ", failureCount=" + failureCount +
                '}';
    }
}