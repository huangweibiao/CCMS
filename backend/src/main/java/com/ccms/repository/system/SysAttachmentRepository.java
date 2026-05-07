package com.ccms.repository.system;

import com.ccms.entity.system.SysAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * 根据业务类型和业务ID查询附件
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 附件列表
     */
    List<SysAttachment> findByBusinessTypeAndBusinessId(Integer businessType, Long businessId);

    /**
     * 根据业务类型查询附件
     *
     * @param businessType 业务类型
     * @return 附件列表
     */
    List<SysAttachment> findByBusinessType(Integer businessType);

    /**
     * 根据上传人ID查询附件
     *
     * @param uploadUserId 上传人ID
     * @return 附件列表
     */
    List<SysAttachment> findByUploadUserId(Long uploadUserId);

    /**
     * 根据文件MD5查询附件
     *
     * @param fileMd5 文件MD5
     * @return 附件列表
     */
    List<SysAttachment> findByFileMd5(String fileMd5);

    /**
     * 查询未删除的附件
     *
     * @param isDeleted 是否已删除
     * @return 附件列表
     */
    List<SysAttachment> findByIsDeleted(Boolean isDeleted);
}
