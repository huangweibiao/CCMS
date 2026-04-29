package com.ccms.entity.export;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 导出任务实体类
 */
@Entity
@Table(name = "sys_export_task")
public class ExportTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 任务ID（UUID）
     */
    @Column(nullable = false, unique = true, length = 36)
    private String taskId;
    
    /**
     * 模板代码
     */
    @Column(nullable = false, length = 50)
    private String templateCode;
    
    /**
     * 导出格式（excel/pdf/word/csv）
     */
    @Column(nullable = false, length = 10)
    private String exportFormat;
    
    /**
     * 导出参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String exportParams;
    
    /**
     * 任务状态：0-待处理，1-处理中，2-已完成，3-失败，4-已取消
     */
    @Column(nullable = false)
    private Integer status;
    
    /**
     * 创建人
     */
    @Column(nullable = false, length = 50)
    private String createBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    /**
     * 开始处理时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 结果文件路径
     */
    @Column(length = 500)
    private String resultPath;
    
    /**
     * 错误信息
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 优先级别：1-低，2-中，3-高
     */
    @Column(nullable = false)
    private Integer priority = 2;
    
    /**
     * 预计处理时间（分钟）
     */
    private Integer estimatedMinutes;
    
    /**
     * 实际处理时长（秒）
     */
    private Integer actualSeconds;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public String getExportParams() {
        return exportParams;
    }

    public void setExportParams(String exportParams) {
        this.exportParams = exportParams;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public Integer getActualSeconds() {
        return actualSeconds;
    }

    public void setActualSeconds(Integer actualSeconds) {
        this.actualSeconds = actualSeconds;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ExportTask{" +
                "id=" + id +
                ", taskId='" + taskId + '\'' +
                ", templateCode='" + templateCode + '\'' +
                ", exportFormat='" + exportFormat + '\'' +
                ", status=" + status +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", resultPath='" + resultPath + '\'' +
                '}';
    }
}