package com.ccms.entity.archive;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 归档操作日志实体
 * 记录每次归档操作的结果和统计信息
 */
@Entity
@Table(name = "sys_archive_log")
@Data
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
}