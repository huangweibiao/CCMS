package com.ccms.repository;

import com.ccms.entity.system.SysAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 系统附件数据访问层
 */
@Repository
public interface SysAttachmentRepository extends JpaRepository<SysAttachment, Long>, JpaSpecificationExecutor<SysAttachment> {

    /**
     * 根据业务类型和业务ID查找附件列表
     */
    List<SysAttachment> findByBusinessTypeAndBusinessIdAndIsDeletedFalse(Integer businessType, Long businessId);

    /**
     * 根据文件MD5查找已存在的文件（用于去重检查）
     */
    @Query("SELECT a FROM SysAttachment a WHERE a.fileMd5 = :fileMd5 AND a.isDeleted = false")
    Optional<SysAttachment> findByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据业务类型和业务ID删除附件（逻辑删除）
     */
    @Modifying
    @Query("UPDATE SysAttachment a SET a.isDeleted = true, a.deleteTime = :deleteTime, a.deletedBy = :deletedBy WHERE a.businessType = :businessType AND a.businessId = :businessId AND a.isDeleted = false")
    int deleteByBiz(@Param("businessType") Integer businessType, 
                    @Param("businessId") Long businessId, 
                    @Param("deleteTime") LocalDateTime deleteTime, 
                    @Param("deletedBy") String deletedBy);

    /**
     * 根据上传用户ID查找附件
     */
    Page<SysAttachment> findByUploadUserIdAndIsDeletedFalse(Long uploadUserId, Pageable pageable);

    /**
     * 统计今日上传的附件数量
     */
    @Query("SELECT COUNT(a) FROM SysAttachment a WHERE a.createTime >= :startDate AND a.createTime < :endDate AND a.isDeleted = false")
    long countTodayUploads(@Param("startDate") LocalDateTime startDate, 
                          @Param("endDate") LocalDateTime endDate);

    /**
     * 获取按业务类型分组的统计信息
     */
    @Query("SELECT a.businessType, COUNT(a), SUM(a.fileSize) FROM SysAttachment a WHERE a.isDeleted = false GROUP BY a.businessType")
    List<Object[]> getStatisticsByBusinessType();

    /**
     * 获取按文件类型分组的统计信息
     */
    @Query("SELECT a.fileType, COUNT(a), SUM(a.fileSize) FROM SysAttachment a WHERE a.isDeleted = false GROUP BY a.fileType")
    List<Object[]> getStatisticsByFileType();

    /**
     * 查找过期的附件（创建时间超过指定天数且未删除）
     */
    @Query("SELECT a FROM SysAttachment a WHERE a.createTime < :expiryDate AND a.isDeleted = false")
    List<SysAttachment> findExpiredAttachments(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * 批量软删除附件
     */
    @Modifying
    @Query("UPDATE SysAttachment a SET a.isDeleted = true, a.deleteTime = :deleteTime, a.deletedBy = :deletedBy WHERE a.id IN :ids AND a.isDeleted = false")
    int batchSoftDelete(@Param("ids") List<Long> ids, 
                       @Param("deleteTime") LocalDateTime deleteTime, 
                       @Param("deletedBy") String deletedBy);

    /**
     * 恢复已删除的附件
     */
    @Modifying
    @Query("UPDATE SysAttachment a SET a.isDeleted = false, a.deleteTime = NULL, a.deletedBy = NULL WHERE a.id = :id AND a.isDeleted = true")
    int restoreAttachment(@Param("id") Long id);

    /**
     * 更新附件下载次数
     */
    @Modifying
    @Query("UPDATE SysAttachment a SET a.downloadCount = a.downloadCount + 1, a.lastDownloadTime = :downloadTime WHERE a.id = :id")
    int incrementDownloadCount(@Param("id") Long id, 
                              @Param("downloadTime") LocalDateTime downloadTime);

    /**
     * 根据文件名模糊查询
     */
    Page<SysAttachment> findByFileNameContainingAndIsDeletedFalse(String fileName, Pageable pageable);

    /**
     * 根据文件类型查询
     */
    Page<SysAttachment> findByFileTypeAndIsDeletedFalse(String fileType, Pageable pageable);

    /**
     * 获取所有已删除的附件
     */
    Page<SysAttachment> findByIsDeletedTrue(Pageable pageable);

    /**
     * 统计总文件大小
     */
    @Query("SELECT SUM(a.fileSize) FROM SysAttachment a WHERE a.isDeleted = false AND a.fileSize IS NOT NULL")
    Long getTotalFileSize();

    /**
     * 统计已删除的附件数量
     */
    long countByIsDeletedTrue();

    /**
     * 批量物理删除已标记为删除的附件（仅用于清理任务）
     */
    @Modifying
    @Query("DELETE FROM SysAttachment a WHERE a.isDeleted = true AND a.deleteTime < :cleanupDate")
    int permanentlyDeleteExpired(@Param("cleanupDate") LocalDateTime cleanupDate);

    /**
     * 根据多个业务场景查询附件
     */
    @Query("SELECT a FROM SysAttachment a WHERE a.businessType IN :businessTypes AND a.businessId IN :businessIds AND a.isDeleted = false")
    List<SysAttachment> findAttachmentsByMultipleBiz(
            @Param("businessTypes") List<Integer> businessTypes,
            @Param("businessIds") List<Long> businessIds);

    /**
     * 检查某个业务是否有附件
     */
    boolean existsByBusinessTypeAndBusinessIdAndIsDeletedFalse(Integer businessType, Long businessId);

    /**
     * 获取用户的附件数量限制检查
     */
    @Query("SELECT COUNT(a) FROM SysAttachment a WHERE a.uploadUserId = :userId AND a.isDeleted = false AND a.createTime >= :startDate AND a.createTime < :endDate")
    long countUserUploadsInPeriod(@Param("userId") Long userId,
                                 @Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
}