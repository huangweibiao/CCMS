package com.ccms.entity.report;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报表数据实体类
 * 用于存储预计算的报表数据，提高查询性能
 */
@Entity
@Table(name = "ccms_report_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}