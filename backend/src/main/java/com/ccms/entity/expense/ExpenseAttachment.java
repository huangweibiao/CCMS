package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;


import jakarta.persistence.*;

@Entity
@Table(name = "ccms_expense_attachment")
public class ExpenseAttachment extends BaseEntity {
    
    @Column(name = "expense_apply_id", nullable = false)
    private Long expenseApplyId;
    
    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "file_type", length = 100)
    private String fileType;
    
    @Column(name = "upload_time", nullable = false)
    private java.time.LocalDateTime uploadTime;
    
    @Column(name = "upload_user_id", nullable = false)
    private Long uploadUserId;
    
    @Column(name = "upload_user_name", nullable = false, length = 100)
    private String uploadUserName;
    
    @Column(name = "is_deleted", columnDefinition = "tinyint(1) default 0")
    private Boolean deleted = false;
    
    @Column(name = "remark", length = 500)
    private String remark;

    // Getters and Setters
    public Long getExpenseApplyId() {
        return expenseApplyId;
    }

    public void setExpenseApplyId(Long expenseApplyId) {
        this.expenseApplyId = expenseApplyId;
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

    public java.time.LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(java.time.LocalDateTime uploadTime) {
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
}