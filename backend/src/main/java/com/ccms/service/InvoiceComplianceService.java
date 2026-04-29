package com.ccms.service;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ExpenseReimburseDetail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * 发票合规性检查服务接口
 * 
 * @author 系统生成
 */
public interface InvoiceComplianceService {
    
    /**
     * 检查单张发票的合规性
     * 
     * @param invoice 待检查的发票
     * @param reimburse 关联的报销单（可为空）
     * @return 合规性检查结果
     */
    ComplianceResult checkSingleInvoice(ExpenseInvoice invoice, ExpenseReimburse reimburse);
    
    /**
     * 批量检查发票合规性
     * 
     * @param invoices 发票列表
     * @param reimburse 关联的报销单
     * @return 批量检查结果
     */
    BatchComplianceResult checkBatchInvoices(List<ExpenseInvoice> invoices, ExpenseReimburse reimburse);
    
    /**
     * 检查报销单整体合规性
     * 
     * @param reimburse 报销单
     * @param details 报销明细
     * @param invoices 关联的发票
     * @return 整体合规性检查结果
     */
    ReimburseComplianceResult checkReimburseCompliance(
        ExpenseReimburse reimburse, 
        List<ExpenseReimburseDetail> details,
        List<ExpenseInvoice> invoices);
    
    /**
     * 获取合规性规则配置
     * 
     * @return 规则配置
     */
    Map<String, Object> getComplianceRules();
    
    /**
     * 单张发票合规性检查结果
     */
    class ComplianceResult {
        private final boolean compliant;           // 是否合规
        private final String invoiceNo;           // 发票号码
        private final int score;                  // 合规分数（0-100）
        private final List<RuleViolation> violations; // 违规项列表
        private final List<ComplianceTip> tips;   // 合规提示
        
        public ComplianceResult(boolean compliant, String invoiceNo, int score, 
                               List<RuleViolation> violations, List<ComplianceTip> tips) {
            this.compliant = compliant;
            this.invoiceNo = invoiceNo;
            this.score = score;
            this.violations = violations != null ? violations : java.util.Collections.emptyList();
            this.tips = tips != null ? tips : java.util.Collections.emptyList();
        }
        
        // Getter方法
        public boolean isCompliant() { return compliant; }
        public String getInvoiceNo() { return invoiceNo; }
        public int getScore() { return score; }
        public List<RuleViolation> getViolations() { return violations; }
        public List<ComplianceTip> getTips() { return tips; }
    }
    
    /**
     * 批量检查结果
     */
    class BatchComplianceResult {
        private final int totalCount;              // 总发票数
        private final int compliantCount;          // 合规发票数
        private final int nonCompliantCount;       // 不合规发票数
        private final List<ComplianceResult> results; // 单张检查结果
        private final BigDecimal totalAmount;      // 总金额
        private final BigDecimal compliantAmount;  // 合规金额
        
        public BatchComplianceResult(int totalCount, int compliantCount, int nonCompliantCount,
                                    List<ComplianceResult> results, BigDecimal totalAmount, 
                                    BigDecimal compliantAmount) {
            this.totalCount = totalCount;
            this.compliantCount = compliantCount;
            this.nonCompliantCount = nonCompliantCount;
            this.results = results != null ? results : java.util.Collections.emptyList();
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.compliantAmount = compliantAmount != null ? compliantAmount : BigDecimal.ZERO;
        }
        
        // Getter方法
        public int getTotalCount() { return totalCount; }
        public int getCompliantCount() { return compliantCount; }
        public int getNonCompliantCount() { return nonCompliantCount; }
        public List<ComplianceResult> getResults() { return results; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getCompliantAmount() { return compliantAmount; }
    }
    
    /**
     * 报销单整体合规性检查结果
     */
    class ReimburseComplianceResult {
        private final boolean compliant;           // 报销单是否合规
        private final String reimburseNo;         // 报销单号
        private final int score;                  // 整体合规分数
        private final List<ComplianceResult> invoiceResults; // 发票检查结果
        private final BigDecimal totalAmount;      // 总报销金额
        private final BigDecimal compliantAmount;  // 合规报销金额
        private final List<GlobalViolation> globalViolations; // 整体违规项
        
        public ReimburseComplianceResult(boolean compliant, String reimburseNo, int score,
                                        List<ComplianceResult> invoiceResults, BigDecimal totalAmount,
                                        BigDecimal compliantAmount, List<GlobalViolation> globalViolations) {
            this.compliant = compliant;
            this.reimburseNo = reimburseNo;
            this.score = score;
            this.invoiceResults = invoiceResults != null ? invoiceResults : java.util.Collections.emptyList();
            this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
            this.compliantAmount = compliantAmount != null ? compliantAmount : BigDecimal.ZERO;
            this.globalViolations = globalViolations != null ? globalViolations : java.util.Collections.emptyList();
        }
        
        // Getter方法
        public boolean isCompliant() { return compliant; }
        public String getReimburseNo() { return reimburseNo; }
        public int getScore() { return score; }
        public List<ComplianceResult> getInvoiceResults() { return invoiceResults; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getCompliantAmount() { return compliantAmount; }
        public List<GlobalViolation> getGlobalViolations() { return globalViolations; }
    }
    
    /**
     * 规则违规项
     */
    class RuleViolation {
        private final String ruleCode;             // 规则代码
        private final String ruleName;             // 规则名称
        private final String violationDesc;        // 违规描述
        private final String severity;             // 严重程度：HIGH/MEDIUM/LOW
        private final String suggestFix;           // 修复建议
        
        public RuleViolation(String ruleCode, String ruleName, String violationDesc, 
                            String severity, String suggestFix) {
            this.ruleCode = ruleCode;
            this.ruleName = ruleName;
            this.violationDesc = violationDesc;
            this.severity = severity;
            this.suggestFix = suggestFix;
        }
        
        // Getter方法
        public String getRuleCode() { return ruleCode; }
        public String getRuleName() { return ruleName; }
        public String getViolationDesc() { return violationDesc; }
        public String getSeverity() { return severity; }
        public String getSuggestFix() { return suggestFix; }
    }
    
    /**
     * 合规提示
     */
    class ComplianceTip {
        private final String tipCode;              // 提示代码
        private final String tipContent;           // 提示内容
        private final String tipType;              // 提示类型：WARNING/INFO/SUCCESS
        
        public ComplianceTip(String tipCode, String tipContent, String tipType) {
            this.tipCode = tipCode;
            this.tipContent = tipContent;
            this.tipType = tipType;
        }
        
        // Getter方法
        public String getTipCode() { return tipCode; }
        public String getTipContent() { return tipContent; }
        public String getTipType() { return tipType; }
    }
    
    /**
     * 整体违规项
     */
    class GlobalViolation {
        private final String violationCode;        // 违规代码
        private final String violationDesc;        // 违规描述
        private final BigDecimal violationAmount;  // 违规金额
        private final String fixAction;            // 修复行动
        
        public GlobalViolation(String violationCode, String violationDesc, 
                              BigDecimal violationAmount, String fixAction) {
            this.violationCode = violationCode;
            this.violationDesc = violationDesc;
            this.violationAmount = violationAmount != null ? violationAmount : BigDecimal.ZERO;
            this.fixAction = fixAction;
        }
        
        // Getter方法
        public String getViolationCode() { return violationCode; }
        public String getViolationDesc() { return violationDesc; }
        public BigDecimal getViolationAmount() { return violationAmount; }
        public String getFixAction() { return fixAction; }
    }
}