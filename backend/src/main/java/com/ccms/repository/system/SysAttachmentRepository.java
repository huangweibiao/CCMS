package com.ccms.repository.system;

import com.ccms.entity.system.SysAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 附件表Repository接口
 * 
 * @author 系统生成
 */
@Repository
public interface SysAttachmentRepository extends JpaRepository<SysAttachment, Long>, JpaSpecificationExecutor<SysAttachment> {

    /**
     * 根据业务类型查询附件
     * 
     * @param businessType 业务类型：0-系统文件，1-用户头像，2-费用凭证，3-预算文档，4-审批附件
     * @return 附件列表
     */
    List<SysAttachment> findByBusinessType(Integer businessType);

    /**
     * 根据业务ID查询附件
     * 
     * @param businessId 业务ID
     * @return 附件列表
     */
    List<SysAttachment> findByBusinessId(Long businessId);

    /**
     * 根据上传人ID查询附件
     * 
     * @param uploadUserId 上传人ID
     * @return 附件列表
     */
    List<SysAttachment> findByUploadUserId(Long uploadUserId);

    /**
     * 根据存储类型查询附件
     * 
     * @param storageType 存储类型：0-本地，1-阿里云OSS，2-腾讯云COS，3-七牛云
     * @return 附件列表
     */
    List<SysAttachment> findByStorageType(Integer storageType);

    /**
     * 根据业务类型和业务ID查询附件
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 附件列表
     */
    List<SysAttachment> findByBusinessTypeAndBusinessId(Integer businessType, Long businessId);

    /**
     * 查询指定文件类型的附件
     * 
     * @param fileType 文件类型
     * @return 附件列表
     */
    List<SysAttachment> findByFileType(String fileType);

    /**
     * 统计用户的附件总大小
     * 
     * @param uploadUserId 上传人ID
     * @return 总大小（字节）
     */
    @Query("SELECT COALESCE(SUM(sa.fileSize), 0) FROM SysAttachment sa WHERE sa.uploadUserId = :uploadUserId")
    Long calculateTotalFileSizeByUser(@Param("uploadUserId") Long uploadUserId);

    /**
     * 统计业务关联的附件总数量
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 附件数量
     */
    Long countByBusinessTypeAndBusinessId(Integer businessType, Long businessId);

    /**
     * 更新附件业务关联信息
     * 
     * @param id 附件ID
     * @param businessType 业务类型
     * @param businessId 业务ID
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysAttachment sa SET sa.businessType = :businessType, sa.businessId = :businessId WHERE sa.id = :id")
    void updateBusinessInfo(@Param("id") Long id, @Param("businessType") Integer businessType, @Param("businessId") Long businessId);

    /**
     * 删除业务关联的附件
     * 
     * @param businessType 业务类型
     * @param businessId 业务ID
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM SysAttachment sa WHERE sa.businessType = :businessType AND sa.businessId = :businessId")
    void deleteByBusinessInfo(@Param("businessType") Integer businessType, @Param("businessId") Long businessId);

    /**
     * 查询文件大小超过阈值的附件
     * 
     * @param sizeThreshold 大小阈值（字节）
     * @return 附件列表
     */
    @Query("SELECT sa FROM SysAttachment sa WHERE sa.fileSize > :sizeThreshold")
    List<SysAttachment> findLargeFiles(@Param("sizeThreshold") Long sizeThreshold);

    /**
     * 统计不同业务类型的附件数量
     * 
     * @return Object[] 包含 businessType 和 count
     */
    @Query("SELECT sa.businessType, COUNT(sa) FROM SysAttachment sa GROUP BY sa.businessType")
    List<Object[]> countAttachmentsByBusinessType();

    /**
     * 增加附件下载次数
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysAttachment sa SET sa.downloadCount = sa.downloadCount + 1, sa.lastDownloadTime = :lastDownloadTime WHERE sa.id = :id")
    void incrementDownloadCount(@Param("id") Long id, @Param("lastDownloadTime") java.time.LocalDateTime lastDownloadTime);

    /**
     * 根据业务类型和业务ID查询未删除的附件
     */
    List<SysAttachment> findByBusinessTypeAndBusinessIdAndIsDeletedFalse(Integer businessType, Long businessId);

    /**
     * 批量软删除
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysAttachment sa SET sa.isDeleted = 1, sa.deletedTime = :deletedTime, sa.deletedBy = :deletedBy WHERE sa.id IN :ids")
    void batchSoftDelete(@Param("ids") java.util.List<Long> ids, @Param("deletedTime") java.time.LocalDateTime deletedTime, @Param("deletedBy") String deletedBy);

    /**
     * 恢复已删除的附件
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE SysAttachment sa SET sa.isDeleted = 0, sa.deletedTime = null, sa.deletedBy = null WHERE sa.id = :id")
    void restoreAttachment(@Param("id") Long id);

    /**
     * 统计未删除的附件数量
     */
    Long countByIsDeletedFalse();

    /**
     * 获取总文件大小
     */
    @Query("SELECT COALESCE(SUM(sa.fileSize), 0) FROM SysAttachment sa WHERE sa.isDeleted = false")
    Long getTotalFileSize();

    /**
     * 统计今天上传的附件数量
     */
    @Query("SELECT COUNT(sa) FROM SysAttachment sa WHERE sa.createdTime BETWEEN :startTime AND :endTime AND sa.isDeleted = false")
    Long countTodayUploads(@Param("startTime") java.time.LocalDateTime startTime, @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 统计已删除的附件数量
     */
    Long countByIsDeletedTrue();

    /**
     * 按业务类型统计
     */
    @Query("SELECT sa.businessType, COUNT(sa) FROM SysAttachment sa WHERE sa.isDeleted = false GROUP BY sa.businessType")
    List<Object[]> getStatisticsByBusinessType();

    /**
     * 按文件类型统计
     */
    @Query("SELECT sa.fileType, COUNT(sa) FROM SysAttachment sa WHERE sa.isDeleted = false GROUP BY sa.fileType")
    List<Object[]> getStatisticsByFileType();

    /**
     * 根据文件MD5查找重复附件
     */
    List<SysAttachment> findByFileMd5(String fileMd5);

    /**
     * 查找过期的附件
     */
    @Query("SELECT sa FROM SysAttachment sa WHERE sa.expireTime < :currentTime AND sa.isDeleted = false")
    List<SysAttachment> findExpiredAttachments(@Param("currentTime") java.time.LocalDateTime currentTime);
}