package com.ccms.service.impl;

import com.ccms.entity.SysAttachment;
import com.ccms.service.AttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 附件管理服务实现类
 * 
 * @author 系统生成
 */
@Service
public class AttachmentServiceImpl implements AttachmentService {
    
    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadBasePath;
    
    @Value("${file.max-size:10485760}") // 10MB默认
    private long maxFileSize;
    
    // 允许的文件类型
    private final Set<String> allowedImageTypes = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
    );
    
    private final Set<String> allowedDocumentTypes = Set.of(
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", 
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    
    @Override
    public UploadResult uploadFile(MultipartFile file, Integer bizType, Long bizId, Long uploadUserId) throws IOException {
        // 验证文件
        FileValidationResult validation = validateFile(file);
        if (!validation.isValid()) {
            return new UploadResult(false, validation.getMessage(), null, null);
        }
        
        try {
            // 生成存储文件名
            String storedFileName = generateStoredFileName(file.getOriginalFilename());
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String relativePath = generateRelativePath(bizType, bizId);
            String fullPath = uploadBasePath + File.separator + relativePath;
            
            // 创建目录
            createDirectoryIfNotExists(fullPath);
            
            // 保存文件
            Path targetPath = Paths.get(fullPath, storedFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 计算MD5
            String md5Checksum = calculateMD5(targetPath);
            
            // 创建附件记录
            SysAttachment attachment = new SysAttachment();
            attachment.setBizType(bizType);
            attachment.setBizId(bizId);
            attachment.setOriginalFileName(file.getOriginalFilename());
            attachment.setStoredFileName(storedFileName);
            attachment.setFilePath(relativePath);
            attachment.setFileExtension(fileExtension);
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setStorageType(SysAttachment.STORAGE_LOCAL);
            attachment.setMd5Checksum(md5Checksum);
            attachment.setUploadUserId(uploadUserId);
            attachment.setCreateTime(LocalDateTime.now());
            attachment.setStatus(SysAttachment.STATUS_NORMAL);
            
            // 这里需要插入数据库，暂时模拟成功
            // attachmentRepository.save(attachment);
            
            String fileUrl = generateFileUrl(attachment);
            
            return new UploadResult(true, "文件上传成功", attachment, fileUrl);
            
        } catch (Exception e) {
            return new UploadResult(false, "文件上传失败: " + e.getMessage(), null, null);
        }
    }
    
    @Override
    public BatchUploadResult uploadFiles(List<MultipartFile> files, Integer bizType, Long bizId, Long uploadUserId) throws IOException {
        List<UploadResult> results = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                UploadResult result = uploadFile(file, bizType, bizId, uploadUserId);
                results.add(result);
            } catch (Exception e) {
                results.add(new UploadResult(false, "文件上传失败: " + e.getMessage(), null, null));
            }
        }
        
        int successCount = (int) results.stream().filter(UploadResult::isSuccess).count();
        int failedCount = files.size() - successCount;
        
        return new BatchUploadResult(files.size(), successCount, failedCount, results);
    }
    
    @Override
    public List<SysAttachment> getAttachmentsByBiz(Integer bizType, Long bizId) {
        // 模拟从数据库查询
        List<SysAttachment> attachments = new ArrayList<>();
        
        // 这里需要从数据库查询
        // return attachmentRepository.findByBizTypeAndBizIdAndStatus(bizType, bizId, SysAttachment.STATUS_NORMAL);
        
        return attachments;
    }
    
    @Override
    public SysAttachment getAttachmentById(Long attachmentId) {
        // 模拟从数据库查询
        // return attachmentRepository.findByIdAndStatus(attachmentId, SysAttachment.STATUS_NORMAL);
        return null; // 实际实现需要查询数据库
    }
    
    @Override
    public boolean deleteAttachment(Long attachmentId) {
        try {
            // 逻辑删除，更新状态为已删除
            // attachmentRepository.updateStatusById(attachmentId, SysAttachment.STATUS_DELETED);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public DownloadResult downloadFile(Long attachmentId, String downloadToken) {
        try {
            // 获取附件信息
            SysAttachment attachment = getAttachmentById(attachmentId);
            if (attachment == null) {
                return new DownloadResult(false, "附件不存在", null, null, null, null, null);
            }
            
            // 检查权限和下载令牌（简化实现）
            if (!validateDownloadPermission(attachment, downloadToken)) {
                return new DownloadResult(false, "无下载权限", null, null, null, null, null);
            }
            
            // 构建文件路径
            String filePath = uploadBasePath + File.separator + attachment.getFilePath() + 
                             File.separator + attachment.getStoredFileName();
            
            File file = new File(filePath);
            if (!file.exists()) {
                return new DownloadResult(false, "文件不存在", null, null, null, null, null);
            }
            
            // 读取文件数据（适用于小文件）
            byte[] fileData = Files.readAllBytes(file.toPath());
            
            // 更新下载次数
            // attachmentRepository.incrementDownloadCount(attachmentId);
            
            return new DownloadResult(true, "下载成功", attachment.getOriginalFileName(), 
                filePath, attachment.getContentType(), attachment.getFileSize(), fileData);
            
        } catch (Exception e) {
            return new DownloadResult(false, "下载失败: " + e.getMessage(), null, null, null, null, null);
        }
    }
    
    @Override
    public PreviewResult getPreviewUrl(Long attachmentId) {
        try {
            SysAttachment attachment = getAttachmentById(attachmentId);
            if (attachment == null) {
                return new PreviewResult(false, "附件不存在", null, null, false);
            }
            
            boolean supportPreview = isPreviewSupported(attachment.getContentType());
            String previewUrl = supportPreview ? generatePreviewUrl(attachment) : null;
            String thumbnailUrl = generateThumbnailUrl(attachment);
            
            return new PreviewResult(true, "获取成功", previewUrl, thumbnailUrl, supportPreview);
            
        } catch (Exception e) {
            return new PreviewResult(false, "获取预览地址失败: " + e.getMessage(), null, null, false);
        }
    }
    
    @Override
    public FileValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new FileValidationResult(false, "文件为空", "EMPTY_FILE", 
                getAllAllowedTypes(), maxFileSize);
        }
        
        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            return new FileValidationResult(false, "文件大小超过限制", "FILE_TOO_LARGE", 
                getAllAllowedTypes(), maxFileSize);
        }
        
        // 检查文件类型
        String contentType = file.getContentType();
        if (!isFileTypeAllowed(contentType)) {
            return new FileValidationResult(false, "不支持的文件类型", "UNSUPPORTED_TYPE", 
                getAllAllowedTypes(), maxFileSize);
        }
        
        // 检查文件名安全性
        if (!isFileNameSafe(file.getOriginalFilename())) {
            return new FileValidationResult(false, "文件名不合法", "INVALID_FILENAME", 
                getAllAllowedTypes(), maxFileSize);
        }
        
        return new FileValidationResult(true, "文件验证通过", null, 
            getAllAllowedTypes(), maxFileSize);
    }
    
    @Override
    public StorageStatistics getStorageStatistics() {
        // 模拟存储统计
        Map<String, Long> sizeByType = new HashMap<>();
        Map<String, Long> countByType = new HashMap<>();
        
        // 这里需要查询数据库和文件系统获取真实数据
        // long totalSize = attachmentRepository.sumFileSize();
        // long totalFiles = attachmentRepository.countByStatus(SysAttachment.STATUS_NORMAL);
        
        return new StorageStatistics(1000, 1024 * 1024 * 100L, sizeByType, countByType);
    }
    
    /**
     * 生成存储文件名
     */
    private String generateStoredFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return null;
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * 生成相对路径
     */
    private String generateRelativePath(Integer bizType, Long bizId) {
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        return bizType + File.separator + datePart + File.separator + bizId;
    }
    
    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String path) throws IOException {
        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("创建目录失败: " + path);
            }
        }
    }
    
    /**
     * 计算文件MD5
     */
    private String calculateMD5(Path filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] fileData = Files.readAllBytes(filePath);
        byte[] digest = md.digest(fileData);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    /**
     * 生成文件URL
     */
    private String generateFileUrl(SysAttachment attachment) {
        return "/api/attachment/download/" + attachment.getId();
    }
    
    /**
     * 验证下载权限
     */
    private boolean validateDownloadPermission(SysAttachment attachment, String downloadToken) {
        // 简化实现，实际需要复杂的权限校验
        return true;
    }
    
    /**
     * 检查是否支持预览
     */
    private boolean isPreviewSupported(String contentType) {
        return allowedImageTypes.contains(contentType) || 
               contentType != null && contentType.startsWith("application/pdf");
    }
    
    /**
     * 生成预览URL
     */
    private String generatePreviewUrl(SysAttachment attachment) {
        return "/api/attachment/preview/" + attachment.getId();
    }
    
    /**
     * 生成缩略图URL
     */
    private String generateThumbnailUrl(SysAttachment attachment) {
        if (allowedImageTypes.contains(attachment.getContentType())) {
            return "/api/attachment/thumbnail/" + attachment.getId();
        }
        return null;
    }
    
    /**
     * 获取所有允许的文件类型
     */
    private List<String> getAllAllowedTypes() {
        List<String> allTypes = new ArrayList<>();
        allTypes.addAll(allowedImageTypes);
        allTypes.addAll(allowedDocumentTypes);
        return allTypes;
    }
    
    /**
     * 检查是否允许的文件类型
     */
    private boolean isFileTypeAllowed(String contentType) {
        return allowedImageTypes.contains(contentType) || allowedDocumentTypes.contains(contentType);
    }
    
    /**
     * 检查文件名安全性
     */
    private boolean isFileNameSafe(String filename) {
        if (filename == null) return false;
        // 检查是否包含特殊字符或路径遍历
        Pattern pattern = Pattern.compile("[^a-zA-Z0-9._-]");
        return !pattern.matcher(filename).find() && 
               !filename.contains("..") && 
               !filename.startsWith("/") && 
               !filename.startsWith("\\");
    }
}