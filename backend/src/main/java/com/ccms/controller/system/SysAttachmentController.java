package com.ccms.controller.system;

import com.ccms.entity.system.SysAttachment;
import com.ccms.repository.system.SysAttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 附件管理控制器
 * 对应设计文档：4.9.2 附件表 (sys_attachment)
 */
@RestController
@RequestMapping("/api/system/attachments")
public class SysAttachmentController {

    private final SysAttachmentRepository attachmentRepository;
    private final String uploadDir = "uploads/";

    @Autowired
    public SysAttachmentController(SysAttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    /**
     * 获取附件列表
     */
    @GetMapping
    public ResponseEntity<List<SysAttachment>> getAttachmentList(
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Long businessId) {
        List<SysAttachment> attachments;
        if (businessType != null && businessId != null) {
            attachments = attachmentRepository.findByBusinessTypeAndBusinessId(businessType, businessId);
        } else if (businessType != null) {
            attachments = attachmentRepository.findByBusinessType(businessType);
        } else {
            attachments = attachmentRepository.findByDeleted(false);
        }
        return ResponseEntity.ok(attachments);
    }

    /**
     * 根据ID获取附件信息
     */
    @GetMapping("/{attachmentId}")
    public ResponseEntity<SysAttachment> getAttachmentById(@PathVariable Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 上传附件
     */
    @PostMapping("/upload")
    public ResponseEntity<SysAttachment> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer businessType,
            @RequestParam(required = false) Long businessId,
            @RequestParam(required = false) String description) {
        try {
            // 创建上传目录
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 保存文件
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // 保存附件信息
            SysAttachment attachment = new SysAttachment();
            attachment.setFileName(originalFilename);
            attachment.setFilePath(filePath.toString());
            attachment.setFileUrl("/uploads/" + newFilename);
            attachment.setFileSize(file.getSize());
            attachment.setFileType(fileExtension);
            attachment.setMimeType(file.getContentType());
            attachment.setBusinessType(businessType);
            attachment.setBusinessId(businessId);
            attachment.setDescription(description);
            attachment.setStorageType(0); // 本地存储
            attachment.setUploadUserId(1L); // TODO: 从当前用户获取
            attachment.setIsPublic(false);
            attachment.setDownloadCount(0);
            attachment.setDeleted(false);

            SysAttachment saved = attachmentRepository.save(attachment);
            return ResponseEntity.ok(saved);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除附件（逻辑删除）
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .map(attachment -> {
                    attachment.setDeleted(true);
                    attachment.setDeleteTime(LocalDateTime.now());
                    attachmentRepository.save(attachment);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新附件信息
     */
    @PutMapping("/{attachmentId}")
    public ResponseEntity<SysAttachment> updateAttachment(
            @PathVariable Long attachmentId,
            @RequestBody SysAttachment attachment) {
        return attachmentRepository.findById(attachmentId)
                .map(existing -> {
                    existing.setDescription(attachment.getDescription());
                    existing.setIsPublic(attachment.getIsPublic());
                    existing.setBusinessType(attachment.getBusinessType());
                    existing.setBusinessId(attachment.getBusinessId());
                    return ResponseEntity.ok(attachmentRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据业务类型和业务ID获取附件列表
     */
    @GetMapping("/business/{businessType}/{businessId}")
    public ResponseEntity<List<SysAttachment>> getAttachmentsByBusiness(
            @PathVariable Integer businessType,
            @PathVariable Long businessId) {
        List<SysAttachment> attachments = attachmentRepository
                .findByBusinessTypeAndBusinessId(businessType, businessId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * 根据MD5查询附件（用于秒传）
     */
    @GetMapping("/md5/{fileMd5}")
    public ResponseEntity<List<SysAttachment>> getAttachmentsByMd5(@PathVariable String fileMd5) {
        List<SysAttachment> attachments = attachmentRepository.findByFileMd5(fileMd5);
        return ResponseEntity.ok(attachments);
    }
}
