package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.service.InvoiceComplianceService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 发票合规性检查服务实现类
 * 
 * @author 系统生成
 */
@Service
public class InvoiceComplianceServiceImpl implements InvoiceComplianceService {
    
    // 合规性规则配置
    private final Map<String, Object> complianceRules = new HashMap<>();
    
    public InvoiceComplianceServiceImpl() {
        initializeComplianceRules();
    }
    
    @Override
    public ComplianceResult checkSingleInvoice(ExpenseInvoice invoice, ExpenseReimburse reimburse) {
        if (invoice == null) {
            return new ComplianceResult(false, null, 0, 
                Collections.singletonList(new RuleViolation("INVOICE_NULL", "发票为空", "发票对象为空", "HIGH", "请提供有效的发票信息")),
                Collections.emptyList());
        }
        
        List<RuleViolation> violations = new ArrayList<>();
        List<ComplianceTip> tips = new ArrayList<>();
        
        // 检查发票基本信息
        checkBasicInfo(invoice, violations, tips);
        
        // 检查发票日期有效性
        checkInvoiceDate(invoice, violations, tips);
        
        // 检查金额合理性
        checkAmount(invoice, violations, tips);
        
        // 检查验真状态
        checkVerifyStatus(invoice, violations, tips);
        
        // 检查与报销单的关联性
        checkReimburseRelation(invoice, reimburse, violations, tips);
        
        // 计算合规分数
        int score = calculateComplianceScore(violations);
        boolean compliant = violations.stream().noneMatch(v -> "HIGH".equals(v.getSeverity()));
        
        return new ComplianceResult(compliant, invoice.getInvoiceNo(), score, violations, tips);
    }
    
    @Override
    public BatchComplianceResult checkBatchInvoices(List<ExpenseInvoice> invoices, ExpenseReimburse reimburse) {
        if (invoices == null || invoices.isEmpty()) {
            return new BatchComplianceResult(0, 0, 0, Collections.emptyList(), BigDecimal.ZERO, BigDecimal.ZERO);
        }
        
        List<ComplianceResult> results = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal compliantAmount = BigDecimal.ZERO;
        int compliantCount = 0;
        
        for (ExpenseInvoice invoice : invoices) {
            ComplianceResult result = checkSingleInvoice(invoice, reimburse);
            results.add(result);
            
            if (invoice.getInvoiceAmount() != null) {
                totalAmount = totalAmount.add(invoice.getInvoiceAmount());
                
                if (result.isCompliant()) {
                    compliantAmount = compliantAmount.add(invoice.getInvoiceAmount());
                    compliantCount++;
                }
            }
        }
        
        int nonCompliantCount = invoices.size() - compliantCount;
        
        return new BatchComplianceResult(invoices.size(), compliantCount, nonCompliantCount, 
            results, totalAmount, compliantAmount);
    }
    
    @Override
    public ReimburseComplianceResult checkReimburseCompliance(ExpenseReimburse reimburse, 
                                                             List<ExpenseReimburseDetail> details,
                                                             List<ExpenseInvoice> invoices) {
        if (reimburse == null) {
            return new ReimburseComplianceResult(false, null, 0, 
                Collections.emptyList(), BigDecimal.ZERO, BigDecimal.ZERO, 
                Collections.emptyList());
        }
        
        // 检查发票合规性
        BatchComplianceResult batchResult = checkBatchInvoices(invoices, reimburse);
        
        // 检查报销单整体违规项
        List<GlobalViolation> globalViolations = checkGlobalCompliance(reimburse, details, invoices);
        
        // 计算整体合规分数
        int totalScore = calculateReimburseTotalScore(batchResult, globalViolations);
        boolean compliant = isReimburseCompliant(batchResult, globalViolations);
        
        return new ReimburseComplianceResult(compliant, reimburse.getReimburseNo(), totalScore,
            batchResult.getResults(), batchResult.getTotalAmount(), batchResult.getCompliantAmount(),
            globalViolations);
    }
    
    @Override
    public Map<String, Object> getComplianceRules() {
        return new HashMap<>(complianceRules);
    }
    
    /**
     * 初始化合规性规则
     */
    private void initializeComplianceRules() {
        complianceRules.put("INVOICE_MAX_AGE_DAYS", 180); // 发票最大有效天数
        complianceRules.put("MIN_INVOICE_AMOUNT", 1);    // 最小发票金额
        complianceRules.put("MAX_INVOICE_AMOUNT", 100000); // 最大发票金额
        complianceRules.put("REQUIRE_VERIFY", true);     // 是否需要验真
        complianceRules.put("ALLOW_FUTURE_DATE", false); // 是否允许未来日期发票
    }
    
    /**
     * 检查基本信息
     */
    private void checkBasicInfo(ExpenseInvoice invoice, List<RuleViolation> violations, List<ComplianceTip> tips) {
        // 检查发票号码和代码
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().trim().isEmpty()) {
            violations.add(new RuleViolation("INVOICE_NO_EMPTY", "发票号码为空", 
                "发票号码不能为空", "HIGH", "请填写发票号码"));
        }
        
        if (invoice.getInvoiceCode() == null || invoice.getInvoiceCode().trim().isEmpty()) {
            violations.add(new RuleViolation("INVOICE_CODE_EMPTY", "发票代码为空", 
                "发票代码不能为空", "HIGH", "请填写发票代码"));
        }
        
        // 检查销方信息
        if (invoice.getSellerName() == null || invoice.getSellerName().trim().isEmpty()) {
            violations.add(new RuleViolation("SELLER_NAME_EMPTY", "销方名称为空", 
                "销方名称不能为空", "MEDIUM", "请填写销方名称"));
        }
    }
    
    /**
     * 检查发票日期有效性
     */
    private void checkInvoiceDate(ExpenseInvoice invoice, List<RuleViolation> violations, List<ComplianceTip> tips) {
        if (invoice.getInvoiceDate() == null) {
            violations.add(new RuleViolation("INVOICE_DATE_NULL", "开票日期为空", 
                "开票日期不能为空", "HIGH", "请填写开票日期"));
            return;
        }
        
        LocalDate invoiceDate = invoice.getInvoiceDate().toLocalDate();
        LocalDate today = LocalDate.now();
        
        // 检查是否未来日期
        if (invoiceDate.isAfter(today)) {
            violations.add(new RuleViolation("FUTURE_INVOICE_DATE", "未来日期发票", 
                "开票日期不能是未来日期", "HIGH", "请检查开票日期准确性"));
        }
        
        // 检查是否超过最大有效天数
        int maxAgeDays = (Integer) complianceRules.get("INVOICE_MAX_AGE_DAYS");
        long daysDiff = ChronoUnit.DAYS.between(invoiceDate, today);
        
        if (daysDiff > maxAgeDays) {
            violations.add(new RuleViolation("INVOICE_EXPIRED", "发票已过期", 
                "发票有效期已超过 " + maxAgeDays + " 天", "HIGH", "请更换有效发票"));
        } else if (daysDiff > maxAgeDays - 30) {
            tips.add(new ComplianceTip("INVOICE_SOON_EXPIRE", "发票即将过期", "WARNING"));
        }
    }
    
    /**
     * 检查金额合理性
     */
    private void checkAmount(ExpenseInvoice invoice, List<RuleViolation> violations, List<ComplianceTip> tips) {
        if (invoice.getInvoiceAmount() == null) {
            violations.add(new RuleViolation("AMOUNT_NULL", "发票金额为空", 
                "发票金额不能为空", "HIGH", "请填写发票金额"));
            return;
        }
        
        BigDecimal amount = invoice.getInvoiceAmount();
        int minAmount = (Integer) complianceRules.get("MIN_INVOICE_AMOUNT");
        int maxAmount = (Integer) complianceRules.get("MAX_INVOICE_AMOUNT");
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            violations.add(new RuleViolation("AMOUNT_INVALID", "金额无效", 
                "发票金额必须大于零", "HIGH", "请填写有效金额"));
        } else if (amount.compareTo(new BigDecimal(minAmount)) < 0) {
            violations.add(new RuleViolation("AMOUNT_TOO_SMALL", "金额过小", 
                "发票金额不能小于 " + minAmount + " 元", "MEDIUM", "请确认金额准确性"));
        } else if (amount.compareTo(new BigDecimal(maxAmount)) > 0) {
            violations.add(new RuleViolation("AMOUNT_TOO_LARGE", "金额过大", 
                "单张发票金额不能超过 " + maxAmount + " 元", "HIGH", "请拆分大额发票"));
        }
        
        // 检查税额合理性（简单检查，实际业务逻辑更复杂）
        if (invoice.getTaxAmount() != null && invoice.getTaxAmount().compareTo(BigDecimal.ZERO) < 0) {
            violations.add(new RuleViolation("TAX_AMOUNT_INVALID", "税额不合法", 
                "税额不能为负数", "MEDIUM", "请检查税额准确性"));
        }
    }
    
    /**
     * 检查验真状态
     */
    private void checkVerifyStatus(ExpenseInvoice invoice, List<RuleViolation> violations, List<ComplianceTip> tips) {
        boolean requireVerify = (Boolean) complianceRules.get("REQUIRE_VERIFY");
        
        if (requireVerify) {
            if (invoice.getVerifyStatus() == null || invoice.getVerifyStatus() == 0) {
                violations.add(new RuleViolation("NOT_VERIFIED", "发票未验真", 
                    "发票需要经过验真才能使用", "HIGH", "请先进行发票验真"));
            } else if (invoice.getVerifyStatus() == 2) {
                violations.add(new RuleViolation("VERIFY_FAILED", "发票验真失败", 
                    "发票验真未通过", "HIGH", "请检查发票真实性"));
            } else if (invoice.getVerifyStatus() == 1) {
                tips.add(new ComplianceTip("VERIFIED_SUCCESS", "发票已验真通过", "SUCCESS"));
            }
        }
    }
    
    /**
     * 检查与报销单的关联性
     */
    private void checkReimburseRelation(ExpenseInvoice invoice, ExpenseReimburse reimburse, 
                                       List<RuleViolation> violations, List<ComplianceTip> tips) {
        if (reimburse == null) {
            return;
        }
        
        // 检查发票日期是否晚于报销单日期
        if (invoice.getInvoiceDate() != null && reimburse.getCreateTime() != null) {
            LocalDate invoiceDate = invoice.getInvoiceDate().toLocalDate();
            LocalDate reimburseDate = reimburse.getCreateTime().toLocalDate();
            
            if (invoiceDate.isAfter(reimburseDate)) {
                violations.add(new RuleViolation("INVOICE_DATE_LATER", "发票日期晚于报销日期", 
                    "发票开票日期不能晚于报销单创建日期", "MEDIUM", "请调整发票或报销单日期"));
            }
        }
    }
    
    /**
     * 检查报销单整体违规项
     */
    private List<GlobalViolation> checkGlobalCompliance(ExpenseReimburse reimburse, 
                                                       List<ExpenseReimburseDetail> details,
                                                       List<ExpenseInvoice> invoices) {
        List<GlobalViolation> violations = new ArrayList<>();
        
        // 检查发票总额与报销金额是否匹配
        BigDecimal invoicesTotal = invoices.stream()
            .map(ExpenseInvoice::getInvoiceAmount)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (reimburse.getReimburseAmount() != null && 
            !reimburse.getReimburseAmount().equals(invoicesTotal)) {
            violations.add(new GlobalViolation("AMOUNT_MISMATCH", "发票总额与报销金额不匹配", 
                reimburse.getReimburseAmount().subtract(invoicesTotal).abs(), 
                "请调整发票或报销金额"));
        }
        
        // 检查是否存在重复报销的发票
        Set<String> invoiceSet = new HashSet<>();
        for (ExpenseInvoice invoice : invoices) {
            if (invoice.getInvoiceNo() != null) {
                if (!invoiceSet.add(invoice.getInvoiceNo())) {
                    violations.add(new GlobalViolation("DUPLICATE_INVOICE", "存在重复发票", 
                        invoice.getInvoiceAmount(), "请移除重复发票"));
                }
            }
        }
        
        return violations;
    }
    
    /**
     * 计算合规分数
     */
    private int calculateComplianceScore(List<RuleViolation> violations) {
        if (violations.isEmpty()) {
            return 100;
        }
        
        int highViolations = (int) violations.stream()
            .filter(v -> "HIGH".equals(v.getSeverity()))
            .count();
        
        int mediumViolations = (int) violations.stream()
            .filter(v -> "MEDIUM".equals(v.getSeverity()))
            .count();
        
        int baseScore = 100;
        baseScore -= highViolations * 50;   // 每个严重违规扣50分
        baseScore -= mediumViolations * 20; // 每个中等违规扣20分
        
        return Math.max(baseScore, 0);
    }
    
    /**
     * 计算报销单整体合规分数
     */
    private int calculateReimburseTotalScore(BatchComplianceResult batchResult, 
                                            List<GlobalViolation> globalViolations) {
        if (batchResult.getTotalCount() == 0) {
            return 0;
        }
        
        // 计算发票平均合规分数
        double averageInvoiceScore = batchResult.getResults().stream()
            .mapToInt(ComplianceResult::getScore)
            .average()
            .orElse(0);
        
        // 整体违规项扣分
        int globalDeduction = globalViolations.size() * 10;
        
        return Math.max((int) averageInvoiceScore - globalDeduction, 0);
    }
    
    /**
     * 判断报销单是否合规
     */
    private boolean isReimburseCompliant(BatchComplianceResult batchResult, 
                                        List<GlobalViolation> globalViolations) {
        // 所有发票都要合规
        boolean allInvoicesCompliant = batchResult.getResults().stream()
            .allMatch(ComplianceResult::isCompliant);
        
        // 不能有整体违规项
        boolean noGlobalViolations = globalViolations.isEmpty();
        
        return allInvoicesCompliant && noGlobalViolations;
    }
}