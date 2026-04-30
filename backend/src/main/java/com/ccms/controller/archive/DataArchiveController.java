package com.ccms.controller.archive;

import com.ccms.common.response.ApiResponse;
import com.ccms.service.archive.DataArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据归档管理控制器
 * 提供数据归档、查询和恢复的REST API接口
 */
@RestController
@RequestMapping("/api/archive")
public class DataArchiveController {

    @Autowired
    private DataArchiveService dataArchiveService;

    /**
     * 执行数据归档任务
     */
    @PostMapping("/execute")
    public ApiResponse<DataArchiveService.ArchiveResult> executeArchive(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate archiveDate) {
        
        DataArchiveService.ArchiveResult result = dataArchiveService.executeArchive(archiveDate);
        return ApiResponse.success(result);
    }

    /**
     * 查询归档数据
     */
    @PostMapping("/query")
    public ApiResponse<List<DataArchiveService.ArchiveRecord>> queryArchivedData(
            @RequestBody DataArchiveService.ArchiveQuery archiveQuery) {
        
        List<DataArchiveService.ArchiveRecord> records = dataArchiveService.queryArchivedData(archiveQuery);
        return ApiResponse.success(records);
    }

    /**
     * 恢复归档数据
     */
    @PostMapping("/restore/{archiveId}")
    public ApiResponse<Boolean> restoreArchivedData(@PathVariable Long archiveId) {
        
        boolean success = dataArchiveService.restoreArchivedData(archiveId);
        return ApiResponse.success(success);
    }

    /**
     * 获取归档策略配置
     */
    @GetMapping("/policies")
    public ApiResponse<List<DataArchiveService.ArchivePolicy>> getArchivePolicies() {
        
        List<DataArchiveService.ArchivePolicy> policies = dataArchiveService.getArchivePolicies();
        return ApiResponse.success(policies);
    }

    /**
     * 更新归档策略
     */
    @PutMapping("/policy")
    public ApiResponse<Boolean> updateArchivePolicy(@RequestBody DataArchiveService.ArchivePolicy policy) {
        
        boolean success = dataArchiveService.updateArchivePolicy(policy);
        return ApiResponse.success(success);
    }

    /**
     * 归档指定业务表数据
     */
    @PostMapping("/business")
    public ApiResponse<Integer> archiveBusinessData(
            @RequestParam String tableName,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate archiveDate) {
        
        int archivedCount = dataArchiveService.archiveBusinessData(tableName, archiveDate);
        return ApiResponse.success(archivedCount);
    }

    /**
     * 归档操作日志数据
     */
    @PostMapping("/operation-logs")
    public ApiResponse<Integer> archiveOperationLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate archiveDate) {
        
        int archivedCount = dataArchiveService.archiveOperationLogs(archiveDate);
        return ApiResponse.success(archivedCount);
    }

    /**
     * 归档系统日志数据
     */
    @PostMapping("/system-logs")
    public ApiResponse<Integer> archiveSystemLogs(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate archiveDate) {
        
        int archivedCount = dataArchiveService.archiveSystemLogs(archiveDate);
        return ApiResponse.success(archivedCount);
    }

    /**
     * 获取归档统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<ArchiveStatistics> getArchiveStatistics() {
        
        // 模拟统计信息
        ArchiveStatistics statistics = new ArchiveStatistics();
        statistics.setTotalArchivedRecords(25000L);
        statistics.setLastArchiveDate(LocalDate.now().minusDays(1));
        statistics.setArchiveFileSize("2.5GB");
        statistics.setSuccessArchiveOperations(45);
        
        return ApiResponse.success(statistics);
    }

    /**
     * 归档统计信息类
     */
    public static class ArchiveStatistics {
        private Long totalArchivedRecords;
        private LocalDate lastArchiveDate;
        private String archiveFileSize;
        private Integer successArchiveOperations;

        public Long getTotalArchivedRecords() { return totalArchivedRecords; }
        public void setTotalArchivedRecords(Long totalArchivedRecords) { this.totalArchivedRecords = totalArchivedRecords; }

        public LocalDate getLastArchiveDate() { return lastArchiveDate; }
        public void setLastArchiveDate(LocalDate lastArchiveDate) { this.lastArchiveDate = lastArchiveDate; }

        public String getArchiveFileSize() { return archiveFileSize; }
        public void setArchiveFileSize(String archiveFileSize) { this.archiveFileSize = archiveFileSize; }

        public Integer getSuccessArchiveOperations() { return successArchiveOperations; }
        public void setSuccessArchiveOperations(Integer successArchiveOperations) { this.successArchiveOperations = successArchiveOperations; }
    }
}