package com.ccms.controller;

import com.ccms.entity.system.SysAttachment;
import com.ccms.service.SysAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 系统附件管理控制器
 * 提供附件上传、下载、预览、管理等REST API接口
 */
@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class SysAttachmentController {

    private final SysAttachmentService attachmentService;

    /**
     * 上传单个文件
     */
    @PostMapping("/upload")
    public ResponseEntity<SysAttachmentService.UploadResult> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        
        SysAttachmentService.AttachmentInfo attachmentInfo = new SysAttachmentService.AttachmentInfo();
        attachmentInfo.setBizType(bizType);
        attachmentInfo.setBizId(bizId);
        attachmentInfo.setDescription(description);
        attachmentInfo.setIsPublic(isPublic);
        
        SysAttachmentService.UploadResult result = attachmentService.uploadFile(file, attachmentInfo);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 批量上传文件
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<List<SysAttachmentService.UploadResult>> uploadFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("bizType") String bizType,
            @RequestParam("bizId") Long bizId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String userName) {
        
        SysAttachmentService.AttachmentInfo attachmentInfo = new SysAttachmentService.AttachmentInfo();
        attachmentInfo.setBizType(bizType);
        attachmentInfo.setBizId(bizId);
        attachmentInfo.setDescription(description);
        attachmentInfo.setIsPublic(isPublic);
        
        List<SysAttachmentService.UploadResult> results = attachmentService.uploadFiles(files, attachmentInfo);
        
        return ResponseEntity.ok(results);
    }

    /**
     * 下载附件
     */
    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") String userId) {
        
        SysAttachmentService.DownloadResult result = attachmentService.downloadFile(attachmentId, userId);
        
        if (result.isSuccess()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(result.getMimeType()));
            headers.setContentDispositionFormData("attachment", result.getFileName());
            headers.setContentLength(result.getFileContent().length);
            
            return new ResponseEntity<>(result.getFileContent(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 预览附件
     */
    @GetMapping("/preview/{attachmentId}")
    public ResponseEntity<SysAttachmentService.PreviewResult> previewFile(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") String userId) {
        
        SysAttachmentService.PreviewResult result = attachmentService.previewFile(attachmentId, userId);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据业务ID获取附件列表
     */
    @GetMapping("/biz/{bizType}/{bizId}")
    public ResponseEntity<List<SysAttachment>> getAttachmentsByBiz(
            @PathVariable String bizType,
            @PathVariable Long bizId) {
        
        List<SysAttachment> attachments = attachmentService.getAttachmentsByBiz(bizType, bizId);
        return ResponseEntity.ok(attachments);
    }

    /**
     * 分页查询附件
     */
    @GetMapping("/list")
    public ResponseEntity<Page<SysAttachment>> getAttachments(
            @RequestParam(value = "bizType", required = false) String bizType,
            @RequestParam(value = "bizId", required = false) Long bizId,
            @RequestParam(value = "fileName", required = false) String fileName,
            @RequestParam(value = "fileType", required = false) String fileType,
            @RequestParam(value = "isDeleted", required = false) Boolean isDeleted,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createTime") String sort,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        
        Sort sortObj = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        SysAttachmentService.AttachmentQuery query = new SysAttachmentService.AttachmentQuery();
        query.setBizType(bizType);
        query.setBizId(bizId);
        query.setFileName(fileName);
        query.setFileType(fileType);
        query.setIsDeleted(isDeleted);
        
        Page<SysAttachment> attachments = attachmentService.getAttachments(query, pageable);
        return ResponseEntity.ok(attachments);
    }

    /**
     * 删除附件（逻辑删除）
     */
    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") String userId) {
        
        attachmentService.deleteAttachment(attachmentId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 批量删除附件
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Void> batchDeleteAttachments(
            @RequestBody List<Long> attachmentIds,
            @RequestHeader("X-User-Id") String userId) {
        
        attachmentService.batchDeleteAttachments(attachmentIds, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 恢复已删除的附件
     */
    @PutMapping("/{attachmentId}/restore")
    public ResponseEntity<Void> restoreAttachment(
            @PathVariable Long attachmentId,
            @RequestHeader("X-User-Id") String userId) {
        
        attachmentService.restoreAttachment(attachmentId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取附件统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<SysAttachmentService.AttachmentStatistics> getStatistics() {
        
        SysAttachmentService.AttachmentStatistics statistics = attachmentService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * 检查文件重复性
     */
    @PostMapping("/check-duplicate")
    public ResponseEntity<SysAttachmentService.DuplicateCheckResult> checkDuplicate(
            @RequestParam("file") MultipartFile file) {
        
        SysAttachmentService.DuplicateCheckResult result = attachmentService.checkDuplicate(file);
        return ResponseEntity.ok(result);
    }

    /**
     * 清理过期附件（管理员接口）
     */
    @PostMapping("/cleanup")
    public ResponseEntity<SysAttachmentService.CleanupResult> cleanupExpiredAttachments(
            @RequestHeader("X-User-Id") String userId) {
        
        SysAttachmentService.CleanupResult result = attachmentService.cleanupExpiredAttachments();
        return ResponseEntity.ok(result);
    }

    /**
     * 文件去重建议
     */
    @GetMapping("/duplicate-suggestions")
    public ResponseEntity<List<SysAttachment>> getDuplicateSuggestions(
            @RequestParam("fileMd5") String fileMd5) {
        
        // 实现去重建议逻辑（可根据MD5查找相似文件）
        return ResponseEntity.ok(List.of());
    }
}