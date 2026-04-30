package com.ccms.service.archive;

import java.time.LocalDate;
import java.util.List;

/**
 * 数据归档服务接口
 * 负责历史数据的归档、查询和恢复功能
 */
public interface DataArchiveService {
    
    /**
     * 执行数据归档任务
     * 
     * @param archiveDate 归档截止日期（归档此日期之前的数据）
     * @return 归档结果统计
     */
    ArchiveResult executeArchive(LocalDate archiveDate);
    
    /**
     * 批量归档指定表的业务数据
     * 
     * @param tableName 表名
     * @param archiveDate 归档截止日期
     * @return 归档记录数
     */
    int archiveBusinessData(String tableName, LocalDate archiveDate);
    
    /**
     * 归档操作日志数据
     * 
     * @param archiveDate 归档截止日期
     * @return 归档记录数
     */
    int archiveOperationLogs(LocalDate archiveDate);
    
    /**
     * 归档系统日志数据
     * 
     * @param archiveDate 归档截止日期
     * @return 归档记录数
     */
    int archiveSystemLogs(LocalDate archiveDate);
    
    /**
     * 查询归档数据
     * 
     * @param archiveQuery 查询条件
     * @return 归档数据列表
     */
    List<ArchiveRecord> queryArchivedData(ArchiveQuery archiveQuery);
    
    /**
     * 恢复归档数据
     * 
     * @param archiveId 归档记录ID
     * @return 恢复结果
     */
    boolean restoreArchivedData(Long archiveId);
    
    /**
     * 获取归档策略配置
     * 
     * @return 归档策略列表
     */
    List<ArchivePolicy> getArchivePolicies();
    
    /**
     * 更新归档策略
     * 
     * @param policy 归档策略
     * @return 是否更新成功
     */
    boolean updateArchivePolicy(ArchivePolicy policy);
    
    /**
     * 归档结果统计
     */
    class ArchiveResult {
        private final int totalArchivedCount;
        private final int operationLogArchivedCount;
        private final int systemLogArchivedCount;
        private final int businessDataArchivedCount;
        private final LocalDate archiveDate;
        
        public ArchiveResult(int totalArchivedCount, int operationLogArchivedCount, 
                           int systemLogArchivedCount, int businessDataArchivedCount, 
                           LocalDate archiveDate) {
            this.totalArchivedCount = totalArchivedCount;
            this.operationLogArchivedCount = operationLogArchivedCount;
            this.systemLogArchivedCount = systemLogArchivedCount;
            this.businessDataArchivedCount = businessDataArchivedCount;
            this.archiveDate = archiveDate;
        }
        
        public int getTotalArchivedCount() { return totalArchivedCount; }
        public int getOperationLogArchivedCount() { return operationLogArchivedCount; }
        public int getSystemLogArchivedCount() { return systemLogArchivedCount; }
        public int getBusinessDataArchivedCount() { return businessDataArchivedCount; }
        public LocalDate getArchiveDate() { return archiveDate; }
    }
    
    /**
     * 归档查询条件
     */
    class ArchiveQuery {
        private String tableName;
        private LocalDate startDate;
        private LocalDate endDate;
        private String businessType;
        private int page;
        private int size;
        
        public ArchiveQuery(String tableName, LocalDate startDate, LocalDate endDate) {
            this.tableName = tableName;
            this.startDate = startDate;
            this.endDate = endDate;
            this.page = 0;
            this.size = 20;
        }
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDate getEndDate() { return endDate; }
        public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
        
        public String getBusinessType() { return businessType; }
        public void setBusinessType(String businessType) { this.businessType = businessType; }
        
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
    
    /**
     * 归档记录
     */
    class ArchiveRecord {
        private Long archiveId;
        private String tableName;
        private LocalDate archiveDate;
        private long recordCount;
        private String archiveFilePath;
        private LocalDate createTime;
        
        public ArchiveRecord() {}
        
        public ArchiveRecord(String tableName, LocalDate archiveDate, long recordCount) {
            this.tableName = tableName;
            this.archiveDate = archiveDate;
            this.recordCount = recordCount;
        }
        
        public Long getArchiveId() { return archiveId; }
        public void setArchiveId(Long archiveId) { this.archiveId = archiveId; }
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public LocalDate getArchiveDate() { return archiveDate; }
        public void setArchiveDate(LocalDate archiveDate) { this.archiveDate = archiveDate; }
        
        public long getRecordCount() { return recordCount; }
        public void setRecordCount(long recordCount) { this.recordCount = recordCount; }
        
        public String getArchiveFilePath() { return archiveFilePath; }
        public void setArchiveFilePath(String archiveFilePath) { this.archiveFilePath = archiveFilePath; }
        
        public LocalDate getCreateTime() { return createTime; }
        public void setCreateTime(LocalDate createTime) { this.createTime = createTime; }
    }
    
    /**
     * 归档策略
     */
    class ArchivePolicy {
        private String tableName;
        private int retentionYears;
        private boolean autoArchive;
        private String archiveSchedule;
        private String description;
        
        public ArchivePolicy(String tableName, int retentionYears) {
            this.tableName = tableName;
            this.retentionYears = retentionYears;
            this.autoArchive = true;
        }
        
        public String getTableName() { return tableName; }
        public void setTableName(String tableName) { this.tableName = tableName; }
        
        public int getRetentionYears() { return retentionYears; }
        public void setRetentionYears(int retentionYears) { this.retentionYears = retentionYears; }
        
        public boolean isAutoArchive() { return autoArchive; }
        public void setAutoArchive(boolean autoArchive) { this.autoArchive = autoArchive; }
        
        public String getArchiveSchedule() { return archiveSchedule; }
        public void setArchiveSchedule(String archiveSchedule) { this.archiveSchedule = archiveSchedule; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}