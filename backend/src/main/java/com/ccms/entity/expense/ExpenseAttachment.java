package com.ccms.entity.expense;

import com.ccms.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "expense_attachment")
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
}