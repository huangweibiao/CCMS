package com.ccms.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchImportResult {
    private int totalRecords;
    private int successCount;
    private int failureCount;
    
    public BatchImportResult() {
        this.totalRecords = 0;
        this.successCount = 0;
        this.failureCount = 0;
    }
}