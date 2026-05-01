package com.ccms.vo.statistics;

import java.math.BigDecimal;

/**
 * 预算执行分析视图对象
 */
public class BudgetExecutionAnalysis {
    
    private int year;
    private int month;
    private BigDecimal totalBudget;
    private BigDecimal usedBudget;
    private BigDecimal remainingBudget;
    private BigDecimal usageRate;
    private BigDecimal monthOverMonthChange;
    private BigDecimal yearOverYearChange;
    
    // Getters and Setters
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    
    public BigDecimal getTotalBudget() { return totalBudget; }
    public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
    
    public BigDecimal getUsedBudget() { return usedBudget; }
    public void setUsedBudget(BigDecimal usedBudget) { this.usedBudget = usedBudget; }
    
    public BigDecimal getRemainingBudget() { return remainingBudget; }
    public void setRemainingBudget(BigDecimal remainingBudget) { this.remainingBudget = remainingBudget; }
    
    public BigDecimal getUsageRate() { return usageRate; }
    public void setUsageRate(BigDecimal usageRate) { this.usageRate = usageRate; }
    
    public BigDecimal getMonthOverMonthChange() { return monthOverMonthChange; }
    public void setMonthOverMonthChange(BigDecimal monthOverMonthChange) { this.monthOverMonthChange = monthOverMonthChange; }
    
    public BigDecimal getYearOverYearChange() { return yearOverYearChange; }
    public void setYearOverYearChange(BigDecimal yearOverYearChange) { this.yearOverYearChange = yearOverYearChange; }
}