package com.ccms.entity.report;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * 报表数据实体类
 * 用于存储预计算的报表数据，提高查询性能
 */
@Entity
@Table(name = "ccms_report_data")
public class ReportData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 报表类型
     */
    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType;
    
    /**
     * 报表日期
     */
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;
    
    /**
     * 维度1
     */
    @Column(name = "dimension1", length = 100)
    private String dimension1;
    
    /**
     * 维度2
     */
    @Column(name = "dimension2", length = 100)
    private String dimension2;
    
    /**
     * 指标值1
     */
    @Column(name = "metric1", precision = 15, scale = 2)
    private BigDecimal metric1;
    
    /**
     * 指标值2
     */
    @Column(name = "metric2", precision = 15, scale = 2)
    private BigDecimal metric2;
    
    /**
     * 指标值3
     */
    @Column(name = "metric3")
    private Integer metric3;
    
    /**
     * 指标值4
     */
    @Column(name = "metric4")
    private Double metric4;
    
    /**
     * 数据版本
     */
    @Version
    private Integer version;

    public ReportData() {
    }

    public ReportData(Long id, String reportType, LocalDate reportDate, String dimension1, String dimension2, BigDecimal metric1, BigDecimal metric2, Integer metric3, Double metric4, Integer version) {
        this.id = id;
        this.reportType = reportType;
        this.reportDate = reportDate;
        this.dimension1 = dimension1;
        this.dimension2 = dimension2;
        this.metric1 = metric1;
        this.metric2 = metric2;
        this.metric3 = metric3;
        this.metric4 = metric4;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public String getDimension1() {
        return dimension1;
    }

    public void setDimension1(String dimension1) {
        this.dimension1 = dimension1;
    }

    public String getDimension2() {
        return dimension2;
    }

    public void setDimension2(String dimension2) {
        this.dimension2 = dimension2;
    }

    public BigDecimal getMetric1() {
        return metric1;
    }

    public void setMetric1(BigDecimal metric1) {
        this.metric1 = metric1;
    }

    public BigDecimal getMetric2() {
        return metric2;
    }

    public void setMetric2(BigDecimal metric2) {
        this.metric2 = metric2;
    }

    public Integer getMetric3() {
        return metric3;
    }

    public void setMetric3(Integer metric3) {
        this.metric3 = metric3;
    }

    public Double getMetric4() {
        return metric4;
    }

    public void setMetric4(Double metric4) {
        this.metric4 = metric4;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportData that = (ReportData) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(reportType, that.reportType) && 
               Objects.equals(reportDate, that.reportDate) && 
               Objects.equals(dimension1, that.dimension1) && 
               Objects.equals(dimension2, that.dimension2) && 
               Objects.equals(metric1, that.metric1) && 
               Objects.equals(metric2, that.metric2) && 
               Objects.equals(metric3, that.metric3) && 
               Objects.equals(metric4, that.metric4) && 
               Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reportType, reportDate, dimension1, dimension2, metric1, metric2, metric3, metric4, version);
    }

    @Override
    public String toString() {
        return "ReportData{" +
                "id=" + id +
                ", reportType='" + reportType + '\'' +
                ", reportDate=" + reportDate +
                ", dimension1='" + dimension1 + '\'' +
                ", dimension2='" + dimension2 + '\'' +
                ", metric1=" + metric1 +
                ", metric2=" + metric2 +
                ", metric3=" + metric3 +
                ", metric4=" + metric4 +
                ", version=" + version +
                '}';
    }
}