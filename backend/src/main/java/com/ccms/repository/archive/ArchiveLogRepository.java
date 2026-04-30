package com.ccms.repository.archive;

import com.ccms.entity.archive.ArchiveLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 归档日志仓库接口
 */
@Repository
public interface ArchiveLogRepository extends JpaRepository<ArchiveLog, Long> {
    
    /**
     * 根据归档日期范围查询归档日志
     */
    List<ArchiveLog> findByArchiveDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 根据归档类型查询归档日志
     */
    List<ArchiveLog> findByArchiveType(String archiveType);
    
    /**
     * 根据状态查询归档日志
     */
    List<ArchiveLog> findByStatus(String status);
    
    /**
     * 按归档类型和日期范围查询
     */
    List<ArchiveLog> findByArchiveTypeAndArchiveDateBetween(String archiveType, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取最近的成功归档记录
     */
    ArchiveLog findFirstByStatusOrderByArchiveDateDesc(String status);
    
    /**
     * 统计归档操作记录数
     */
    @Query("SELECT COUNT(al) FROM ArchiveLog al WHERE al.archiveDate >= :startDate")
    Long countArchiveOperationsSince(@Param("startDate") LocalDateTime startDate);
    
    /**
     * 获取归档统计信息
     */
    @Query("SELECT al.archiveType, SUM(al.recordsArchived), SUM(al.archiveFileSize) " +
           "FROM ArchiveLog al WHERE al.status = 'SUCCESS' AND al.archiveDate >= :startDate " +
           "GROUP BY al.archiveType")
    List<Object[]> getArchiveStatisticsByType(@Param("startDate") LocalDateTime startDate);
}