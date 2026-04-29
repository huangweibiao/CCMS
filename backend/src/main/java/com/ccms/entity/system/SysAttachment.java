package com.ccms.entity.system;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 附件表实体类
 * 对应表名：ccms_sys_attachment
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "ccms_sys_attachment")
public class SysAttachment extends BaseEntity {

    /**
     * 业务类型：0-系统文件，1-用户头像，2-费用凭证，3-预算文档，4-审批附件
     */
    @Column(name = "business_type", nullable = false)
    private Integer businessType;
    
    /**
     * 关联业务ID
     */
    @Column(name = "business_id")
    private Long businessId;
    
    /**
     * 文件名称
     */
    @Column(name = "file_name", length = 256, nullable = false)
    private String fileName;
    
    /**
     * 文件路径
     */
    @Column(name = "file_path", length = 512, nullable = false)
    private String filePath;
    
    /**
     * 文件URL
     */
    @Column(name = "file_url", length = 512)
    private String fileUrl;
    
    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;
    
    /**
     * 文件类型
     */
    @Column(name = "file_type", length = 64)
    private String fileType;
    
    /**
     * 存储类型：0-本地，1-阿里云OSS，2-腾讯云COS，3-七牛云
     */
    @Column(name = "storage_type", nullable = false)
    private Integer storageType = 0;
    
    /**
     * 上传人ID
     */
    @Column(name = "upload_user_id", nullable = false)
    private Long uploadUserId;
    
    /**
     * 文件MD5值（用于去重）
     */
    @Column(name = "file_md5", length = 32)
    private String fileMd5;
    
    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    /**
     * 是否公开（0:私有, 1:公开）
     */
    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;
    
    /**
     * 下载次数
     */
    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;
    
    /**
     * 是否已删除（0:正常, 1:已删除）
     */
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    /**
     * 最后下载时间
     */
    @Column(name = "last_download_time")
    private LocalDateTime lastDownloadTime;
    
    /**
     * 删除时间
     */
    @Column(name = "delete_time")
    private LocalDateTime deleteTime;
    
    /**
     * 删除人
     */
    @Column(name = "deleted_by", length = 64)
    private String deletedBy;
    
    /**
     * 描述
     */
    @Column(name = "description", length = 512)
    private String description;

    // Getters and Setters
    public Integer getBusinessType() {
        return businessType;
    }

    public void setBusinessType(Integer businessType) {
        this.businessType = businessType;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Integer getStorageType() {
        return storageType;
    }

    public void setStorageType(Integer storageType) {
        this.storageType = storageType;
    }

    public Long getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getLastDownloadTime() {
        return lastDownloadTime;
    }

    public void setLastDownloadTime(LocalDateTime lastDownloadTime) {
        this.lastDownloadTime = lastDownloadTime;
    }

    public LocalDateTime getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(LocalDateTime deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    @Override
    public String toString() {
        return "SysAttachment{" +
                "id=" + getId() +
                ", businessType=" + businessType +
                ", businessId=" + businessId +
                ", fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", fileSize=" + fileSize +
                ", fileType='" + fileType + '\'' +
                ", fileMd5='" + fileMd5 + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", storageType=" + storageType +
                ", uploadUserId=" + uploadUserId +
                ", isPublic=" + isPublic +
                ", downloadCount=" + downloadCount +
                ", isDeleted=" + isDeleted +
                ", lastDownloadTime=" + lastDownloadTime +
                ", deleteTime=" + deleteTime +
                ", deletedBy='" + deletedBy + '\'' +
                ", description='" + description + '\'' +
                ", createTime=" + getCreateTime() +
                ", updateTime=" + getUpdateTime() +
                ", version=" + getVersion() +
                '}';
    }
}