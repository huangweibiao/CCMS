package com.ccms.service.impl;

import lombok.Data;

@Data
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
}