package com.ccms.entity.archive;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 归档操作日志实体
 * 记录每次归档操作的结果和统计信息
 */
@Entity
@Table(name = "sys_archive_log")
public class ArchiveLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "archive_date")
    private LocalDateTime archiveDate;
    
    @Column(name = "archive_type")
    private String archiveType; // BUSINESS_DATA, OPERATION_LOGS, SYSTEM_LOGS, FULL_ARCHIVE
    
    @Column(name = "table_name")
    private String tableName;
    
    @Column(name = "records_processed")
    private Integer recordsProcessed;
    
    @Column(name = "records_archived")
    private Integer recordsArchived;
    
    @Column(name = "archive_file_path")
    private String archiveFilePath;
    
    @Column(name = "archive_file_size")
    private Long archiveFileSize;
    
    @Column(name = "status")
    private String status; // SUCCESS, FAILED, PARTIAL_SUCCESS
    
    @Column(name = "error_message")
    private String errorMessage;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration_seconds")
    private Long durationSeconds;
    
    @Column(name = "archive_batch_id")
    private String archiveBatchId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getArchiveDate() {
        return archiveDate;
    }

    public void setArchiveDate(LocalDateTime archiveDate) {
        this.archiveDate = archiveDate;
    }

    public String getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(String archiveType) {
        this.archiveType = archiveType;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getRecordsArchived() {
        return recordsArchived;
    }

    public void setRecordsArchived(Integer recordsArchived) {
        this.recordsArchived = recordsArchived;
    }

    public String getArchiveFilePath() {
        return archiveFilePath;
    }

    public void setArchiveFilePath(String archiveFilePath) {
        this.archiveFilePath = archiveFilePath;
    }

    public Long getArchiveFileSize() {
        return archiveFileSize;
    }

    public void setArchiveFileSize(Long archiveFileSize) {
        this.archiveFileSize = archiveFileSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(Long durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getArchiveBatchId() {
        return archiveBatchId;
    }

    public void setArchiveBatchId(String archiveBatchId) {
        this.archiveBatchId = archiveBatchId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArchiveLog that = (ArchiveLog) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(archiveDate, that.archiveDate) && 
               Objects.equals(archiveType, that.archiveType) && 
               Objects.equals(tableName, that.tableName) && 
               Objects.equals(recordsProcessed, that.recordsProcessed) && 
               Objects.equals(recordsArchived, that.recordsArchived) && 
               Objects.equals(archiveFilePath, that.archiveFilePath) && 
               Objects.equals(archiveFileSize, that.archiveFileSize) && 
               Objects.equals(status, that.status) && 
               Objects.equals(errorMessage, that.errorMessage) && 
               Objects.equals(startTime, that.startTime) && 
               Objects.equals(endTime, that.endTime) && 
               Objects.equals(durationSeconds, that.durationSeconds) && 
               Objects.equals(archiveBatchId, that.archiveBatchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, archiveDate, archiveType, tableName, recordsProcessed, recordsArchived, archiveFilePath, archiveFileSize, status, errorMessage, startTime, endTime, durationSeconds, archiveBatchId);
    }

    @Override
    public String toString() {
        return "ArchiveLog{" +
                "id=" + id +
                ", archiveDate=" + archiveDate +
                ", archiveType='" + archiveType + '\'' +
                ", tableName='" + tableName + '\'' +
                ", recordsProcessed=" + recordsProcessed +
                ", recordsArchived=" + recordsArchived +
                ", archiveFilePath='" + archiveFilePath + '\'' +
                ", archiveFileSize=" + archiveFileSize +
                ", status='" + status + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", durationSeconds=" + durationSeconds +
                ", archiveBatchId='" + archiveBatchId + '\'' +
                '}';
    }
}