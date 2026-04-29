package com.ccms.entity.export;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 报表分享实体类
 */
@Entity
@Table(name = "sys_report_share")
public class ReportShare {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 分享令牌
     */
    @Column(nullable = false, unique = true, length = 64)
    private String shareToken;
    
    /**
     * 模板ID
     */
    @Column(nullable = false)
    private Long templateId;
    
    /**
     * 模板名称
     */
    @Column(nullable = false, length = 100)
    private String templateName;
    
    /**
     * 报表数据（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String reportData;
    
    /**
     * 导出参数（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String exportParams;
    
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
    private LocalDateTime createTime = LocalDateTime.now();
    
    /**
     * 过期时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime expireTime;
    
    /**
     * 访问次数
     */
    @Column(nullable = false)
    private Integer accessCount = 0;
    
    /**
     * 最大访问次数（-1表示无限制）
     */
    @Column(nullable = false)
    private Integer maxAccessCount = -1;
    
    /**
     * 是否加密分享
     */
    @Column(nullable = false)
    private Boolean secureShare = false;
    
    /**
     * 分享密码
     */
    @Column(length = 64)
    private String sharePassword;
    
    /**
     * 状态：0-无效，1-有效
     */
    @Column(nullable = false)
    private Integer status = 1;
    
    /**
     * 是否允许下载
     */
    @Column(nullable = false)
    private Boolean allowDownload = true;
    
    /**
     * 下载次数
     */
    @Column(nullable = false)
    private Integer downloadCount = 0;
    
    /**
     * 最后访问时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;
    
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

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }

    public String getExportParams() {
        return exportParams;
    }

    public void setExportParams(String exportParams) {
        this.exportParams = exportParams;
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

    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getAccessCount() {
        return accessCount;
    }

    public void setAccessCount(Integer accessCount) {
        this.accessCount = accessCount;
    }

    public Integer getMaxAccessCount() {
        return maxAccessCount;
    }

    public void setMaxAccessCount(Integer maxAccessCount) {
        this.maxAccessCount = maxAccessCount;
    }

    public Boolean getSecureShare() {
        return secureShare;
    }

    public void setSecureShare(Boolean secureShare) {
        this.secureShare = secureShare;
    }

    public String getSharePassword() {
        return sharePassword;
    }

    public void setSharePassword(String sharePassword) {
        this.sharePassword = sharePassword;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Boolean getAllowDownload() {
        return allowDownload;
    }

    public void setAllowDownload(Boolean allowDownload) {
        this.allowDownload = allowDownload;
    }

    public Integer getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Integer downloadCount) {
        this.downloadCount = downloadCount;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(LocalDateTime lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public String toString() {
        return "ReportShare{" +
                "id=" + id +
                ", shareToken='" + shareToken + '\'' +
                ", templateId=" + templateId +
                ", templateName='" + templateName + '\'' +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", expireTime=" + expireTime +
                ", accessCount=" + accessCount +
                ", status=" + status +
                '}';
    }
}