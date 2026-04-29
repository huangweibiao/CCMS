package com.ccms.service.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 费用统计报表服务
 */
public interface ExpenseReportService {
    
    /**
     * 部门费用统计
     */
    List<DepartmentExpenseStat> getDepartmentExpenseStats(LocalDate startDate, LocalDate endDate);
    
    /**
     * 费用类型统计
     */
    List<ExpenseTypeStat> getExpenseTypeStats(LocalDate startDate, LocalDate endDate, Long deptId);
    
    /**
     * 月度费用趋势分析
     */
    List<MonthlyExpenseTrend> getMonthlyExpenseTrend(int year, Long deptId);
    
    /**
     * 费用支出排行榜
     */
    List<ExpenseRankItem> getExpenseRanking(LocalDate startDate, LocalDate endDate, int limit);
    
    /**
     * 预算执行分析
     */
    List<BudgetExecution> getBudgetExecutionStats(LocalDate startDate, LocalDate endDate);
    
    class DepartmentExpenseStat {
        private Long deptId;
        private String deptName;
        private BigDecimal totalAmount;
        private Integer applyCount;
        private BigDecimal budgetUsed;
        
        // Constructors, Getters, Setters
        public DepartmentExpenseStat(Long deptId, String deptName, BigDecimal totalAmount) {
            this.deptId = deptId;
            this.deptName = deptName;
            this.totalAmount = totalAmount;
        }
        
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public Integer getApplyCount() { return applyCount; }
        public void setApplyCount(Integer applyCount) { this.applyCount = applyCount; }
        
        public BigDecimal getBudgetUsed() { return budgetUsed; }
        public void setBudgetUsed(BigDecimal budgetUsed) { this.budgetUsed = budgetUsed; }
    }
    
    class ExpenseTypeStat {
        private String expenseType;
        private BigDecimal amount;
        private Integer count;
        private BigDecimal percentage;
        
        public ExpenseTypeStat(String expenseType, BigDecimal amount) {
            this.expenseType = expenseType;
            this.amount = amount;
        }
        
        public String getExpenseType() { return expenseType; }
        public void setExpenseType(String expenseType) { this.expenseType = expenseType; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        
        public BigDecimal getPercentage() { return percentage; }
        public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
    }
    
    class MonthlyExpenseTrend {
        private String month;
        private BigDecimal amount;
        private Integer count;
        
        public MonthlyExpenseTrend(String month, BigDecimal amount) {
            this.month = month;
            this.amount = amount;
        }
        
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
    
    class ExpenseRankItem {
        private String name;
        private BigDecimal amount;
        private Integer count;
        
        public ExpenseRankItem(String name, BigDecimal amount) {
            this.name = name;
            this.amount = amount;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
    }
    
    class BudgetExecution {
        private Long budgetId;
        private String budgetName;
        private BigDecimal totalBudget;
        private BigDecimal usedAmount;
        private BigDecimal executionRate;
        
        public BudgetExecution(String budgetName, BigDecimal totalBudget, BigDecimal usedAmount) {
            this.budgetName = budgetName;
            this.totalBudget = totalBudget;
            this.usedAmount = usedAmount;
        }
        
        public Long getBudgetId() { return budgetId; }
        public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
        
        public String getBudgetName() { return budgetName; }
        public void setBudgetName(String budgetName) { this.budgetName = budgetName; }
        
        public BigDecimal getTotalBudget() { return totalBudget; }
        public void setTotalBudget(BigDecimal totalBudget) { this.totalBudget = totalBudget; }
        
        public BigDecimal getUsedAmount() { return usedAmount; }
        public void setUsedAmount(BigDecimal usedAmount) { this.usedAmount = usedAmount; }
        
        public BigDecimal getExecutionRate() { return executionRate; }
        public void setExecutionRate(BigDecimal executionRate) { this.executionRate = executionRate; }
    }
}