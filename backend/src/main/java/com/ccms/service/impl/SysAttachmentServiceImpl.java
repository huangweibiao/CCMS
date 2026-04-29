package com.ccms.service.impl;

import com.ccms.entity.system.SysAttachment;
import com.ccms.repository.SysAttachmentRepository;
import com.ccms.service.SysAttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.criteria.Predicate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统附件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAttachmentServiceImpl implements SysAttachmentService {
    
    private final SysAttachmentRepository attachmentRepository;
    
    @Value("${file.upload.path:/uploads}")
    private String uploadPath;
    
    @Value("${file.max-size:10485760}") // 10MB default
    private long maxFileSize;
    
    @Value("${file.allowed-types:jpg,jpeg,png,pdf,doc,docx,xls,xlsx,txt}")
    private String allowedFileTypes;
    
    @Override
    @Transactional
    public UploadResult uploadFile(MultipartFile file, AttachmentInfo attachmentInfo) {
        UploadResult result = new UploadResult();
        
        try {
            // 验证文件
            validateFile(file);
            
            // 检查文件是否重复
            DuplicateCheckResult duplicateCheck = checkDuplicate(file);
            if (duplicateCheck.isDuplicate()) {
                result.setSuccess(true);
                result.setIsDuplicate(true);
                result.setDuplicateId(duplicateCheck.getDuplicateId());
                result.setMessage("文件已存在，已关联到现有文件");
                return result;
            }
            
            // 生成存储路径
            String relativePath = generateFilePath(file.getOriginalFilename());
            Path targetPath = Paths.get(uploadPath, relativePath);
            
            // 确保目录存在
            Files.createDirectories(targetPath.getParent());
            
            // 保存文件
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 计算MD5
            String fileMd5 = calculateFileMd5(targetPath.toFile());
            
            // 创建附件记录
            SysAttachment attachment = new SysAttachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFilePath(targetPath.toString());
            attachment.setFileUrl("/api/attachments/download/" + fileMd5); // 临时URL
            attachment.setFileSize(file.getSize());
            attachment.setFileType(getFileExtension(file.getOriginalFilename()));
            attachment.setBusinessType(convertBizType(attachmentInfo.getBizType()));
            attachment.setBusinessId(attachmentInfo.getBizId());
            attachment.setUploadUserId(Long.parseLong(attachmentInfo.getBizType())); // 临时使用bizType作为userId
            attachment.setDescription(attachmentInfo.getDescription());
            
            // 设置额外字段（需要先扩展实体类）
            setExtendedFields(attachment, file, fileMd5, attachmentInfo);
            
            attachment = attachmentRepository.save(attachment);
            
            result.setSuccess(true);
            result.setAttachmentId(attachment.getId());
            result.setFileName(attachment.getFileName());
            result.setFileUrl(attachment.getFileUrl());
            result.setFilePath(attachment.getFilePath());
            result.setFileSize(attachment.getFileSize());
            result.setMessage("文件上传成功");
            
            log.info("文件上传成功: {}, 大小: {}, ID: {}", 
                    file.getOriginalFilename(), file.getSize(), attachment.getId());
            
        } catch (Exception e) {
            log.error("文件上传失败: {}", file.getOriginalFilename(), e);
            result.setSuccess(false);
            result.setMessage("文件上传失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public List<UploadResult> uploadFiles(List<MultipartFile> files, AttachmentInfo attachmentInfo) {
        return files.stream()
                .map(file -> uploadFile(file, attachmentInfo))
                .collect(Collectors.toList());
    }
    
    @Override
    public DownloadResult downloadFile(Long attachmentId, String downloaderId) {
        DownloadResult result = new DownloadResult();
        
        try {
            SysAttachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("附件不存在: " + attachmentId));
            
            if (attachment.getIsDeleted()) {
                throw new RuntimeException("附件已被删除");
            }
            
            // 检查权限（简化版本，实际项目需要更复杂的权限控制）
            if (!attachment.getIsPublic() && !attachment.getUploadUserId().toString().equals(downloaderId)) {
                throw new RuntimeException("无权下载该附件");
            }
            
            // 读取文件内容
            Path filePath = Paths.get(attachment.getFilePath());
            if (!Files.exists(filePath)) {
                throw new RuntimeException("文件不存在: " + attachment.getFilePath());
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            // 更新下载统计
            attachmentRepository.incrementDownloadCount(attachmentId, LocalDateTime.now());
            
            result.setSuccess(true);
            result.setFileContent(fileContent);
            result.setFileName(attachment.getFileName());
            result.setMimeType(getMimeType(attachment.getFileType()));
            result.setMessage("下载成功");
            
        } catch (Exception e) {
            log.error("文件下载失败: {}", attachmentId, e);
            result.setSuccess(false);
            result.setMessage("文件下载失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public PreviewResult previewFile(Long attachmentId, String viewerId) {
        PreviewResult result = new PreviewResult();
        
        try {
            SysAttachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("附件不存在: " + attachmentId));
            
            if (attachment.getIsDeleted()) {
                throw new RuntimeException("附件已被删除");
            }
            
            // 检查预览支持
            if (!isPreviewSupported(attachment)) {
                result.setSuccess(false);
                result.setSupportPreview(false);
                result.setMessage("该文件类型不支持预览");
                return result;
            }
            
            // 生成预览URL或内容
            String previewUrl = generatePreviewUrl(attachment);
            
            result.setSuccess(true);
            result.setPreviewUrl(previewUrl);
            result.setSupportPreview(true);
            result.setMessage("预览链接生成成功");
            
        } catch (Exception e) {
            log.error("文件预览失败: {}", attachmentId, e);
            result.setSuccess(false);
            result.setMessage("文件预览失败: " + e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public List<SysAttachment> getAttachmentsByBiz(String bizType, Long bizId) {
        return attachmentRepository.findByBusinessTypeAndBusinessIdAndIsDeletedFalse(
                convertBizType(bizType), bizId);
    }
    
    @Override
    public Page<SysAttachment> getAttachments(AttachmentQuery query, Pageable pageable) {
        Specification<SysAttachment> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 业务类型过滤
            if (StringUtils.hasText(query.getBizType())) {
                predicates.add(criteriaBuilder.equal(root.get("businessType"), 
                        convertBizType(query.getBizType())));
            }
            
            // 业务ID过滤
            if (query.getBizId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("businessId"), query.getBizId()));
            }
            
            // 文件名过滤
            if (StringUtils.hasText(query.getFileName())) {
                predicates.add(criteriaBuilder.like(root.get("fileName"), 
                        "%" + query.getFileName() + "%"));
            }
            
            // 文件类型过滤
            if (StringUtils.hasText(query.getFileType())) {
                predicates.add(criteriaBuilder.equal(root.get("fileType"), query.getFileType()));
            }
            
            // 删除状态过滤
            if (query.getIsDeleted() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), query.getIsDeleted()));
            } else {
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return attachmentRepository.findAll(spec, pageable);
    }
    
    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, String operatorId) {
        SysAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("附件不存在: " + attachmentId));
        
        attachment.setIsDeleted(true);
        attachment.setDeleteTime(LocalDateTime.now());
        attachment.setDeletedBy(operatorId);
        
        attachmentRepository.save(attachment);
        log.info("附件删除成功: {}, 操作人: {}", attachmentId, operatorId);
    }
    
    @Override
    @Transactional
    public void batchDeleteAttachments(List<Long> attachmentIds, String operatorId) {
        attachmentRepository.batchSoftDelete(attachmentIds, LocalDateTime.now(), operatorId);
        log.info("批量删除附件完成: {}, 操作人: {}", attachmentIds.size(), operatorId);
    }
    
    @Override
    @Transactional
    public void restoreAttachment(Long attachmentId, String operatorId) {
        attachmentRepository.restoreAttachment(attachmentId);
        log.info("附件恢复成功: {}, 操作人: {}", attachmentId, operatorId);
    }
    
    @Override
    public AttachmentStatistics getStatistics() {
        AttachmentStatistics stats = new AttachmentStatistics();
        
        // 总数量和大小
        stats.setTotalCount(attachmentRepository.countByIsDeletedFalse());
        Long totalSize = attachmentRepository.getTotalFileSize();
        stats.setTotalSize(totalSize != null ? totalSize : 0);
        
        // 今日上传数量
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        stats.setTodayUploadCount(attachmentRepository.countTodayUploads(todayStart, tomorrowStart));
        
        // 已删除数量
        stats.setDeletedCount(attachmentRepository.countByIsDeletedTrue());
        
        // 按业务类型统计
        List<Object[]> bizStats = attachmentRepository.getStatisticsByBusinessType();
        Map<String, Long> bizTypeStats = bizStats.stream()
                .collect(Collectors.toMap(
                        stat -> stat[0].toString(),
                        stat -> (Long) stat[1]
                ));
        stats.setBizTypeStats(bizTypeStats);
        
        // 按文件类型统计
        List<Object[]> fileStats = attachmentRepository.getStatisticsByFileType();
        Map<String, Long> fileTypeStats = fileStats.stream()
                .collect(Collectors.toMap(
                        stat -> stat[0].toString(),
                        stat -> (Long) stat[1]
                ));
        stats.setFileTypeStats(fileTypeStats);
        
        return stats;
    }
    
    @Override
    public DuplicateCheckResult checkDuplicate(MultipartFile file) {
        DuplicateCheckResult result = new DuplicateCheckResult();
        
        try {
            // 计算文件MD5
            String fileMd5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            
            // 查找相同MD5的文件
            Optional<SysAttachment> existingAttachment = attachmentRepository.findByFileMd5(fileMd5);
            
            if (existingAttachment.isPresent()) {
                SysAttachment attachment = existingAttachment.get();
                result.setDuplicate(true);
                result.setDuplicateId(attachment.getId());
                result.setFileMd5(fileMd5);
                result.setExistingFileSize(attachment.getFileSize());
            } else {
                result.setDuplicate(false);
                result.setFileMd5(fileMd5);
            }
            
        } catch (IOException e) {
            log.error("文件重复性检查失败", e);
            result.setDuplicate(false);
        }
        
        return result;
    }
    
    @Override
    @Transactional
    public CleanupResult cleanupExpiredAttachments() {
        CleanupResult result = new CleanupResult();
        List<String> errors = new ArrayList<>();
        
        try {
            // 查找过期附件（创建时间超过30天）
            LocalDateTime expiryDate = LocalDateTime.now().minusDays(30);
            List<SysAttachment> expiredAttachments = attachmentRepository.findExpiredAttachments(expiryDate);
            
            for (SysAttachment attachment : expiredAttachments) {
                try {
                    // 删除物理文件
                    Path filePath = Paths.get(attachment.getFilePath());
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        result.setFreedSize(result.getFreedSize() + (attachment.getFileSize() != null ? attachment.getFileSize() : 0));
                    }
                    
                    // 删除数据库记录
                    attachmentRepository.delete(attachment);
                    result.setDeletedCount(result.getDeletedCount() + 1);
                    
                } catch (Exception e) {
                    errors.add("删除附件失败: " + attachment.getId() + " - " + e.getMessage());
                    result.setErrorCount(result.getErrorCount() + 1);
                }
            }
            
            result.setErrors(errors);
            log.info("清理过期附件完成: 删除 {} 个文件，释放 {} 字节空间", 
                    result.getDeletedCount(), result.getFreedSize());
            
        } catch (Exception e) {
            log.error("清理过期附件失败", e);
            errors.add("清理任务执行失败: " + e.getMessage());
            result.setErrors(errors);
        }
        
        return result;
    }
    
    // ========== 私有方法 ==========
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("文件大小超过限制: " + file.getSize() + " > " + maxFileSize);
        }
        
        String fileExtension = getFileExtension(file.getOriginalFilename());
        if (!isFileTypeAllowed(fileExtension)) {
            throw new RuntimeException("文件类型不被允许: " + fileExtension);
        }
    }
    
    private boolean isFileTypeAllowed(String fileExtension) {
        if (!StringUtils.hasText(fileExtension)) {
            return false;
        }
        
        String[] allowedTypes = allowedFileTypes.split(",");
        return Arrays.asList(allowedTypes).contains(fileExtension.toLowerCase());
    }
    
    private String generateFilePath(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = UUID.randomUUID().toString().substring(0, 8);
        
        // 按日期组织文件目录
        LocalDateTime now = LocalDateTime.now();
        String datePath = String.format("%d/%02d/%02d", 
                now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        
        return Paths.get(datePath, timestamp + "_" + random + "." + extension).toString();
    }
    
    private String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    private String calculateFileMd5(File file) throws IOException {
        return DigestUtils.md5DigestAsHex(Files.readAllBytes(file.toPath()));
    }
    
    private void setExtendedFields(SysAttachment attachment, MultipartFile file, String fileMd5, AttachmentInfo attachmentInfo) {
        // 这里设置额外字段，需要先扩展SysAttachment实体类
        // 暂时使用反射或直接设置字段（如果实体类已扩展）
        try {
            // 使用反射设置MD5字段
            attachment.getClass().getMethod("setFileMd5", String.class).invoke(attachment, fileMd5);
            
            // 设置MIME类型
            String mimeType = getMimeType(attachment.getFileType());
            attachment.getClass().getMethod("setMimeType", String.class).invoke(attachment, mimeType);
            
            // 设置是否公开
            Boolean isPublic = attachmentInfo.getIsPublic() != null ? attachmentInfo.getIsPublic() : false;
            attachment.getClass().getMethod("setIsPublic", Boolean.class).invoke(attachment, isPublic);
            
        } catch (Exception e) {
            log.warn("设置扩展字段失败，实体类可能需要更新", e);
        }
    }
    
    private String getMimeType(String fileExtension) {
        Map<String, String> mimeTypes = new HashMap<>();
        mimeTypes.put("jpg", "image/jpeg");
        mimeTypes.put("jpeg", "image/jpeg");
        mimeTypes.put("png", "image/png");
        mimeTypes.put("pdf", "application/pdf");
        mimeTypes.put("doc", "application/msword");
        mimeTypes.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        mimeTypes.put("xls", "application/vnd.ms-excel");
        mimeTypes.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        mimeTypes.put("txt", "text/plain");
        
        return mimeTypes.getOrDefault(fileExtension.toLowerCase(), "application/octet-stream");
    }
    
    private boolean isPreviewSupported(SysAttachment attachment) {
        String fileType = attachment.getFileType();
        String[] previewableTypes = {"jpg", "jpeg", "png", "pdf", "txt"};
        return Arrays.asList(previewableTypes).contains(fileType.toLowerCase());
    }
    
    private String generatePreviewUrl(SysAttachment attachment) {
        return "/api/attachments/preview/" + attachment.getId();
    }
    
    private Integer convertBizType(String bizType) {
        // 将字符串业务类型转换为数字编码
        Map<String, Integer> bizTypeMap = new HashMap<>();
        bizTypeMap.put("INVOICE", 1);
        bizTypeMap.put("VOUCHER", 2);
        bizTypeMap.put("CONTRACT", 3);
        bizTypeMap.put("OTHER", 4);
        
        return bizTypeMap.getOrDefault(bizType, 4); // 默认为其他类型
    }
}