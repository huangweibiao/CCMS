package com.ccms.vo.statistics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 费用趋势分析视图对象
 */
public class ExpenseTrendAnalysis {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String dimension;
    private Map<String, BigDecimal> trendData;
    private BigDecimal averageAmount;
    private BigDecimal growthRate;
    
    // Getters and Setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    
    public String getDimension() { return dimension; }
    public void setDimension(String dimension) { this.dimension = dimension; }
    
    public Map<String, BigDecimal> getTrendData() { return trendData; }
    public void setTrendData(Map<String, BigDecimal> trendData) { this.trendData = trendData; }
    
    public BigDecimal getAverageAmount() { return averageAmount; }
    public void setAverageAmount(BigDecimal averageAmount) { this.averageAmount = averageAmount; }
    
    public BigDecimal getGrowthRate() { return growthRate; }
    public void setGrowthRate(BigDecimal growthRate) { this.growthRate = growthRate; }
}