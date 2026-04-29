package com.ccms.entity;

import jakarta.persistence.*;

/**
 * 系统附件表实体类
 * 对应表名：sys_attachment
 * 
 * @author 系统生成
 */
@Entity
@Table(name = "sys_attachment")
public class SysAttachment extends BaseEntity {

    /**
     * 业务类型：1-报销单附件 2-发票图片 3-合同文件 4-其他
     */
    @Column(name = "biz_type", nullable = false)
    private Integer bizType;
    
    /**
     * 业务ID（对应业务表主键）
     */
    @Column(name = "biz_id")
    private Long bizId;
    
    /**
     * 原始文件名
     */
    @Column(name = "original_file_name", length = 255, nullable = false)
    private String originalFileName;
    
    /**
     * 存储文件名（UUID或其他唯一标识）
     */
    @Column(name = "stored_file_name", length = 255, nullable = false)
    private String storedFileName;
    
    /**
     * 文件路径
     */
    @Column(name = "file_path", length = 500, nullable = false)
    private String filePath;
    
    /**
     * 文件扩展名
     */
    @Column(name = "file_extension", length = 10)
    private String fileExtension;
    
    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;
    
    /**
     * MIME类型
     */
    @Column(name = "content_type", length = 100)
    private String contentType;
    
    /**
     * 存储类型：1-本地存储 2-云存储 3-数据库存储
     */
    @Column(name = "storage_type", nullable = false)
    private Integer storageType;
    
    /**
     * MD5校验码
     */
    @Column(name = "md5_checksum", length = 32)
    private String md5Checksum;
    
    /**
     * 上传用户ID
     */
    @Column(name = "upload_user_id", nullable = false)
    private Long uploadUserId;
    
    /**
     * 下载次数
     */
    @Column(name = "download_count")
    private Integer downloadCount = 0;
    
    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;
    
    /**
     * 状态：0-正常 1-已删除
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;
    
    // 业务类型常量
    public static final int BIZ_TYPE_REIMBURSE = 1; // 报销单附件
    public static final int BIZ_TYPE_INVOICE = 2;   // 发票图片
    public static final int BIZ_TYPE_CONTRACT = 3;  // 合同文件
    public static final int BIZ_TYPE_OTHER = 4;     // 其他
    
    // 存储类型常量
    public static final int STORAGE_LOCAL = 1;      // 本地存储
    public static final int STORAGE_CLOUD = 2;      // 云存储
    public static final int STORAGE_DB = 3;         // 数据库存储
    
    // 状态常量
    public static final int STATUS_NORMAL = 0;      // 正常
    public static final int STATUS_DELETED = 1;     // 已删除
    
    // Getter and Setter 方法
    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Long getBizId() {
        return bizId;
    }

    public void setBizId(Long bizId) {
        this.bizId = bizId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getStorageType() {
        return storageType;
    }

    public void setStorageType(Integer storageType) {
        this.storageType = storageType;
    }

    public String getMd5Checksum() {
        return md5Checksum;
    }

    public void setMd5Checksum(String md5Checksum) {
        this.md5Checksum = md5Checksum;
    }

    public Long getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}