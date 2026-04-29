package com.ccms.service;

import com.ccms.entity.system.SysAttachment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 系统附件服务接口
 * 提供完整的文件上传、下载、预览、权限控制等功能
 */
public interface SysAttachmentService {

    /**
     * 上传单个文件
     */
    UploadResult uploadFile(MultipartFile file, AttachmentInfo attachmentInfo);

    /**
     * 批量上传文件
     */
    List<UploadResult> uploadFiles(List<MultipartFile> files, AttachmentInfo attachmentInfo);

    /**
     * 下载附件
     */
    DownloadResult downloadFile(Long attachmentId, String downloaderId);

    /**
     * 预览附件
     */
    PreviewResult previewFile(Long attachmentId, String viewerId);

    /**
     * 根据业务ID获取附件列表
     */
    List<SysAttachment> getAttachmentsByBiz(String bizType, Long bizId);

    /**
     * 分页查询附件
     */
    Page<SysAttachment> getAttachments(AttachmentQuery query, Pageable pageable);

    /**
     * 删除附件（逻辑删除）
     */
    void deleteAttachment(Long attachmentId, String operatorId);

    /**
     * 批量删除附件
     */
    void batchDeleteAttachments(List<Long> attachmentIds, String operatorId);

    /**
     * 恢复已删除的附件
     */
    void restoreAttachment(Long attachmentId, String operatorId);

    /**
     * 获取附件统计信息
     */
    AttachmentStatistics getStatistics();

    /**
     * 文件去重检查
     */
    DuplicateCheckResult checkDuplicate(MultipartFile file);

    /**
     * 清理过期附件
     */
    CleanupResult cleanupExpiredAttachments();

    // ========== DTO类定义 ==========

    /**
     * 附件基本信息
     */
    class AttachmentInfo {
        private String bizType;           // 业务类型
        private Long bizId;               // 业务ID
        private String description;       // 描述
        private Boolean isPublic = false; // 是否公开
        private Map<String, Object> metadata; // 元数据

        // getters and setters
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public Long getBizId() { return bizId; }
        public void setBizId(Long bizId) { this.bizId = bizId; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Boolean getIsPublic() { return isPublic; }
        public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    }

    /**
     * 上传结果
     */
    class UploadResult {
        private boolean success;          // 是否成功
        private Long attachmentId;        // 附件ID
        private String fileName;          // 文件名
        private String fileUrl;           // 文件URL
        private String filePath;          // 文件路径
        private Long fileSize;            // 文件大小
        private String message;           // 消息
        private Boolean isDuplicate;      // 是否重复
        private Long duplicateId;         // 重复文件ID

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public Long getAttachmentId() { return attachmentId; }
        public void setAttachmentId(Long attachmentId) { this.attachmentId = attachmentId; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFileUrl() { return fileUrl; }
        public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
        
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Boolean getIsDuplicate() { return isDuplicate; }
        public void setIsDuplicate(Boolean isDuplicate) { this.isDuplicate = isDuplicate; }
        
        public Long getDuplicateId() { return duplicateId; }
        public void setDuplicateId(Long duplicateId) { this.duplicateId = duplicateId; }
    }

    /**
     * 下载结果
     */
    class DownloadResult {
        private boolean success;          // 是否成功
        private byte[] fileContent;       // 文件内容
        private String fileName;          // 文件名
        private String mimeType;          // MIME类型
        private String message;           // 消息

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public byte[] getFileContent() { return fileContent; }
        public void setFileContent(byte[] fileContent) { this.fileContent = fileContent; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getMimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 预览结果
     */
    class PreviewResult {
        private boolean success;          // 是否成功
        private String previewUrl;        // 预览URL
        private String previewContent;    // 预览内容（HTML/JSON等）
        private boolean supportPreview;   // 是否支持预览
        private String message;           // 消息

        // getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getPreviewUrl() { return previewUrl; }
        public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }
        
        public String getPreviewContent() { return previewContent; }
        public void setPreviewContent(String previewContent) { this.previewContent = previewContent; }
        
        public boolean isSupportPreview() { return supportPreview; }
        public void setSupportPreview(boolean supportPreview) { this.supportPreview = supportPreview; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    /**
     * 附件查询条件
     */
    class AttachmentQuery {
        private String bizType;           // 业务类型
        private Long bizId;               // 业务ID
        private String fileName;          // 文件名
        private String fileType;          // 文件类型
        private Boolean isDeleted;        // 是否删除
        private String uploadUserId;      // 上传人ID

        // getters and setters
        public String getBizType() { return bizType; }
        public void setBizType(String bizType) { this.bizType = bizType; }
        
        public Long getBizId() { return bizId; }
        public void setBizId(Long bizId) { this.bizId = bizId; }
        
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        
        public Boolean getIsDeleted() { return isDeleted; }
        public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
        
        public String getUploadUserId() { return uploadUserId; }
        public void setUploadUserId(String uploadUserId) { this.uploadUserId = uploadUserId; }
    }

    /**
     * 附件统计信息
     */
    class AttachmentStatistics {
        private long totalCount;          // 总附件数
        private long totalSize;           // 总大小（字节）
        private long todayUploadCount;    // 今日上传数
        private long deletedCount;        // 已删除数量
        private Map<String, Long> bizTypeStats; // 按业务类型统计
        private Map<String, Long> fileTypeStats; // 按文件类型统计

        // getters and setters
        public long getTotalCount() { return totalCount; }
        public void setTotalCount(long totalCount) { this.totalCount = totalCount; }
        
        public long getTotalSize() { return totalSize; }
        public void setTotalSize(long totalSize) { this.totalSize = totalSize; }
        
        public long getTodayUploadCount() { return todayUploadCount; }
        public void setTodayUploadCount(long todayUploadCount) { this.todayUploadCount = todayUploadCount; }
        
        public long getDeletedCount() { return deletedCount; }
        public void setDeletedCount(long deletedCount) { this.deletedCount = deletedCount; }
        
        public Map<String, Long> getBizTypeStats() { return bizTypeStats; }
        public void setBizTypeStats(Map<String, Long> bizTypeStats) { this.bizTypeStats = bizTypeStats; }
        
        public Map<String, Long> getFileTypeStats() { return fileTypeStats; }
        public void setFileTypeStats(Map<String, Long> fileTypeStats) { this.fileTypeStats = fileTypeStats; }
    }

    /**
     * 去重检查结果
     */
    class DuplicateCheckResult {
        private boolean isDuplicate;      // 是否重复
        private Long duplicateId;         // 重复文件ID
        private String fileMd5;           // 文件MD5
        private Long existingFileSize;    // 已存在文件大小

        // getters and setters
        public boolean isDuplicate() { return isDuplicate; }
        public void setDuplicate(boolean duplicate) { isDuplicate = duplicate; }
        
        public Long getDuplicateId() { return duplicateId; }
        public void setDuplicateId(Long duplicateId) { this.duplicateId = duplicateId; }
        
        public String getFileMd5() { return fileMd5; }
        public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }
        
        public Long getExistingFileSize() { return existingFileSize; }
        public void setExistingFileSize(Long existingFileSize) { this.existingFileSize = existingFileSize; }
    }

    /**
     * 清理结果
     */
    class CleanupResult {
        private int deletedCount;         // 删除数量
        private long freedSize;           // 释放空间大小
        private int errorCount;           // 错误数量
        private List<String> errors;      // 错误列表

        // getters and setters
        public int getDeletedCount() { return deletedCount; }
        public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }
        
        public long getFreedSize() { return freedSize; }
        public void setFreedSize(long freedSize) { this.freedSize = freedSize; }
        
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}