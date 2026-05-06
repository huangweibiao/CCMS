package com.ccms.service;

import com.ccms.entity.system.SysAttachment;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 附件管理服务接口
 * 
 * @author 系统生成
 */
public interface AttachmentService {
    
    /**
     * 上传单个文件
     * 
     * @param file 文件对象
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param uploadUserId 上传用户ID
     * @return 上传结果
     * @throws IOException 文件操作异常
     */
    UploadResult uploadFile(MultipartFile file, Integer bizType, Long bizId, Long uploadUserId) throws IOException;
    
    /**
     * 批量上传文件
     * 
     * @param files 文件列表
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @param uploadUserId 上传用户ID
     * @return 批量上传结果
     * @throws IOException 文件操作异常
     */
    BatchUploadResult uploadFiles(List<MultipartFile> files, Integer bizType, Long bizId, Long uploadUserId) throws IOException;
    
    /**
     * 获取业务附件列表
     * 
     * @param bizType 业务类型
     * @param bizId 业务ID
     * @return 附件列表
     */
    List<SysAttachment> getAttachmentsByBiz(Integer bizType, Long bizId);
    
    /**
     * 获取附件详情
     * 
     * @param attachmentId 附件ID
     * @return 附件详情
     */
    SysAttachment getAttachmentById(Long attachmentId);
    
    /**
     * 删除附件（逻辑删除）
     * 
     * @param attachmentId 附件ID
     * @return 删除是否成功
     */
    boolean deleteAttachment(Long attachmentId);
    
    /**
     * 下载附件
     * 
     * @param attachmentId 附件ID
     * @param downloadToken 下载令牌（可选，用于权限校验）
     * @return 文件下载信息
     */
    DownloadResult downloadFile(Long attachmentId, String downloadToken);
    
    /**
     * 获取附件预览地址
     * 
     * @param attachmentId 附件ID
     * @return 预览地址信息
     */
    PreviewResult getPreviewUrl(Long attachmentId);
    
    /**
     * 验证文件合法性
     * 
     * @param file 文件对象
     * @return 验证结果
     */
    FileValidationResult validateFile(MultipartFile file);
    
    /**
     * 获取存储统计信息
     * 
     * @return 存储统计
     */
    StorageStatistics getStorageStatistics();
    
    /**
     * 单个文件上传结果
     */
    class UploadResult {
        private final boolean success;              // 是否成功
        private final String message;               // 结果消息
        private final SysAttachment attachment;     // 上传后的附件信息
        private final String fileUrl;               // 文件访问URL
        
        public UploadResult(boolean success, String message, SysAttachment attachment, String fileUrl) {
            this.success = success;
            this.message = message;
            this.attachment = attachment;
            this.fileUrl = fileUrl;
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public SysAttachment getAttachment() { return attachment; }
        public String getFileUrl() { return fileUrl; }
    }
    
    /**
     * 批量上传结果
     */
    class BatchUploadResult {
        private final int totalCount;              // 总文件数
        private final int successCount;            // 成功数
        private final int failedCount;             // 失败数
        private final List<UploadResult> results;  // 详细结果
        
        public BatchUploadResult(int totalCount, int successCount, int failedCount, List<UploadResult> results) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.results = results != null ? results : java.util.Collections.emptyList();
        }
        
        // Getter方法
        public int getTotalCount() { return totalCount; }
        public int getSuccessCount() { return successCount; }
        public int getFailedCount() { return failedCount; }
        public List<UploadResult> getResults() { return results; }
    }
    
    /**
     * 文件下载结果
     */
    class DownloadResult {
        private final boolean success;              // 是否成功
        private final String message;               // 结果消息
        private final String fileName;              // 文件名
        private final String filePath;              // 文件路径
        private final String contentType;           // 内容类型
        private final Long fileSize;                // 文件大小
        private final byte[] fileData;              // 文件数据（仅限小文件直接返回）
        
        public DownloadResult(boolean success, String message, String fileName, String filePath, 
                             String contentType, Long fileSize, byte[] fileData) {
            this.success = success;
            this.message = message;
            this.fileName = fileName;
            this.filePath = filePath;
            this.contentType = contentType;
            this.fileSize = fileSize;
            this.fileData = fileData;
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getFileName() { return fileName; }
        public String getFilePath() { return filePath; }
        public String getContentType() { return contentType; }
        public Long getFileSize() { return fileSize; }
        public byte[] getFileData() { return fileData; }
    }
    
    /**
     * 预览结果
     */
    class PreviewResult {
        private final boolean success;              // 是否成功
        private final String message;               // 结果消息
        private final String previewUrl;            // 预览URL
        private final String thumbnailUrl;          // 缩略图URL
        private final boolean supportPreview;       // 是否支持预览
        
        public PreviewResult(boolean success, String message, String previewUrl, String thumbnailUrl, boolean supportPreview) {
            this.success = success;
            this.message = message;
            this.previewUrl = previewUrl;
            this.thumbnailUrl = thumbnailUrl;
            this.supportPreview = supportPreview;
        }
        
        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getPreviewUrl() { return previewUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public boolean isSupportPreview() { return supportPreview; }
    }
    
    /**
     * 文件验证结果
     */
    class FileValidationResult {
        private final boolean valid;                // 是否有效
        private final String message;               // 验证消息
        private final String errorType;             // 错误类型
        private final List<String> allowedTypes;    // 允许的文件类型
        private final long maxFileSize;             // 最大文件大小
        
        public FileValidationResult(boolean valid, String message, String errorType, 
                                   List<String> allowedTypes, long maxFileSize) {
            this.valid = valid;
            this.message = message;
            this.errorType = errorType;
            this.allowedTypes = allowedTypes != null ? allowedTypes : java.util.Collections.emptyList();
            this.maxFileSize = maxFileSize;
        }
        
        // Getter方法
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getErrorType() { return errorType; }
        public List<String> getAllowedTypes() { return allowedTypes; }
        public long getMaxFileSize() { return maxFileSize; }
    }
    
    /**
     * 存储统计信息
     */
    class StorageStatistics {
        private final long totalFiles;              // 总文件数
        private final long totalSize;               // 总存储大小（字节）
        private final Map<String, Long> sizeByType; // 按类型统计大小
        private final Map<String, Long> countByType; // 按类型统计数量
        
        public StorageStatistics(long totalFiles, long totalSize, Map<String, Long> sizeByType, Map<String, Long> countByType) {
            this.totalFiles = totalFiles;
            this.totalSize = totalSize;
            this.sizeByType = sizeByType != null ? sizeByType : new java.util.HashMap<>();
            this.countByType = countByType != null ? countByType : new java.util.HashMap<>();
        }
        
        // Getter方法
        public long getTotalFiles() { return totalFiles; }
        public long getTotalSize() { return totalSize; }
        public Map<String, Long> getSizeByType() { return sizeByType; }
        public Map<String, Long> getCountByType() { return countByType; }
    }
}