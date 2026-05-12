package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ccms_reimburse_attachment")
public class ReimburseAttachment extends BaseEntity {
    
    @Column(name = "reimburse_main_id", nullable = false)
    private Long reimburseMainId;
    
    @Column(name = "expense_reimburse_id", nullable = false)
    private Long expenseReimburseId = 0L;
    
    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_type", length = 100)
    private String fileType;
    
    @Column(name = "upload_time", nullable = false)
    private LocalDateTime uploadTime;
    
    @Column(name = "upload_user_id", nullable = false)
    private Long uploadUserId;
    
    @Column(name = "upload_user_name", nullable = false, length = 100)
    private String uploadUserName;
    
    @Column(name = "is_deleted", columnDefinition = "tinyint(1) default 0")
    private Boolean deleted = false;
    
    @Column(name = "remark", length = 500)
    private String remark;

    // Getter and Setter methods
    public Long getReimburseMainId() {
        return reimburseMainId;
    }

    public void setReimburseMainId(Long reimburseMainId) {
        this.reimburseMainId = reimburseMainId;
    }

    public Long getExpenseReimburseId() {
        return expenseReimburseId;
    }

    public void setExpenseReimburseId(Long expenseReimburseId) {
        this.expenseReimburseId = expenseReimburseId;
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

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Long getUploadUserId() {
        return uploadUserId;
    }

    public void setUploadUserId(Long uploadUserId) {
        this.uploadUserId = uploadUserId;
    }

    public String getUploadUserName() {
        return uploadUserName;
    }

    public void setUploadUserName(String uploadUserName) {
        this.uploadUserName = uploadUserName;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    
    // Compatible methods for service layer calls
    public void setCreateTime(LocalDateTime createTime) {
        // This might be setting uploadTime as createTime
        if (uploadTime == null) {
            uploadTime = createTime;
        }
    }
    
    public void setAttachmentName(String attachmentName) {
        // Alias for fileName
        this.fileName = attachmentName;
    }
    
    public void setAttachmentUrl(String attachmentUrl) {
        // Alias for filePath
        this.filePath = attachmentUrl;
    }
    
    public void setAttachmentType(String attachmentType) {
        // Alias for fileType
        this.fileType = attachmentType;
    }
}