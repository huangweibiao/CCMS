package com.ccms.entity.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 报表模板实体类
 */
@Entity
@Table(name = "sys_report_template")
public class ReportTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板代码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String templateCode;
    
    /**
     * 模板名称
     */
    @Column(nullable = false, length = 100)
    private String templateName;
    
    /**
     * 模板类型：EXPENSE-费用，BUDGET-预算，PROJECT-项目
     */
    @Column(nullable = false, length = 20)
    private String templateType;
    
    /**
     * 模板描述
     */
    @Column(length = 500)
    private String templateDesc;
    
    /**
     * 模板配置（JSON格式）
     */
    @Column(columnDefinition = "TEXT")
    private String templateConfig;
    
    /**
     * 报表SQL/查询语句
     */
    @Column(columnDefinition = "TEXT")
    private String reportQuery;
    
    /**
     * 报表参数配置
     */
    @Column(columnDefinition = "TEXT")
    private String paramConfig;
    
    /**
     * 报表列配置
     */
    @Column(columnDefinition = "TEXT")
    private String columnConfig;
    
    /**
     * 图表配置
     */
    @Column(columnDefinition = "TEXT")
    private String chartConfig;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @Column(nullable = false)
    private Integer status = 1;
    
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
     * 更新人
     */
    @Column(length = 50)
    private String updateBy;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
    /**
     * 排序号
     */
    private Integer sortOrder = 0;
    
    /**
     * 是否系统模板
     */
    @Column(nullable = false)
    private Boolean isSystem = false;
    
    // Getters and Setters
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getTemplateDesc() {
        return templateDesc;
    }

    public void setTemplateDesc(String templateDesc) {
        this.templateDesc = templateDesc;
    }

    public String getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(String templateConfig) {
        this.templateConfig = templateConfig;
    }

    public String getReportQuery() {
        return reportQuery;
    }

    public void setReportQuery(String reportQuery) {
        this.reportQuery = reportQuery;
    }

    public String getParamConfig() {
        return paramConfig;
    }

    public void setParamConfig(String paramConfig) {
        this.paramConfig = paramConfig;
    }

    public String getColumnConfig() {
        return columnConfig;
    }

    public void setColumnConfig(String columnConfig) {
        this.columnConfig = columnConfig;
    }

    public String getChartConfig() {
        return chartConfig;
    }

    public void setChartConfig(String chartConfig) {
        this.chartConfig = chartConfig;
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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(Boolean isSystem) {
        this.isSystem = isSystem;
    }

    @Override
    public String toString() {
        return "ReportTemplate{" +
                "id=" + id +
                ", templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateType='" + templateType + '\'' +
                ", templateDesc='" + templateDesc + '\'' +
                ", status=" + status +
                ", createBy='" + createBy + '\'' +
                ", createTime=" + createTime +
                ", isSystem=" + isSystem +
                '}';
    }
}