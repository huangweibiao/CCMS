package com.ccms.repository.system;

import com.ccms.entity.system.SysAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
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
public interface SysAttachmentRepository extends JpaRepository<SysAttachment, Long> {

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
}