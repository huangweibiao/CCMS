package com.ccms.entity.attachment;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 附件管理实体
 */
@Entity
@Table(name = "attachment")
public class Attachment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long uploadUserId;
    
    @Column(nullable = false, length = 200)
    private String fileName;
    
    @Column(nullable = false, length = 100)
    private String fileType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false, length = 500)
    private String filePath;
    
    @Column(length = 100)
    private String mimeType;
    
    @Column(nullable = false, length = 100)
    private String attachmentType;
    
    @Column
    private Long relatedId;
    
    @Column(length = 100)
    private String relatedType;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Integer downloadCount = 0;
    
    @Column(nullable = false)
    private Boolean deleted = false;
    
    @Column(nullable = false)
    private LocalDateTime uploadTime = LocalDateTime.now();
    
    @Column
    private LocalDateTime deleteTime;
    
    @Version
    private Long version;
    
    // Constructors
    public Attachment() {}
    
    public Attachment(Long uploadUserId, String fileName, String fileType, Long fileSize, String filePath) {
        this.uploadUserId = uploadUserId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.filePath = filePath;
    }
    
    // Business methods
    public void incrementDownloadCount() {
        if (downloadCount == null) {
            downloadCount = 0;
        }
        downloadCount++;
    }
    
    public void markAsDeleted() {
        this.deleted = true;
        this.deleteTime = LocalDateTime.now();
    }
    
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    public boolean isPdf() {
        return mimeType != null && mimeType.equals("application/pdf");
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUploadUserId() { return uploadUserId; }
    public void setUploadUserId(Long uploadUserId) { this.uploadUserId = uploadUserId; }
    
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    
    public String getAttachmentType() { return attachmentType; }
    public void setAttachmentType(String attachmentType) { this.attachmentType = attachmentType; }
    
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    
    public String getRelatedType() { return relatedType; }
    public void setRelatedType(String relatedType) { this.relatedType = relatedType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }
    
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
    
    public LocalDateTime getUploadTime() { return uploadTime; }
    public void setUploadTime(LocalDateTime uploadTime) { this.uploadTime = uploadTime; }
    
    public LocalDateTime getDeleteTime() { return deleteTime; }
    public void setDeleteTime(LocalDateTime deleteTime) { this.deleteTime = deleteTime; }
    
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}