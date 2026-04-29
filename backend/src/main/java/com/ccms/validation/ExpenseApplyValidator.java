package com.ccms.validation;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseItem;
import com.ccms.repository.expense.ExpenseItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 费用申请校验器
 */
@Component
public class ExpenseApplyValidator implements Validator<ExpenseApply> {
    
    private static final BigDecimal MAX_SINGLE_APPLY_AMOUNT = new BigDecimal("1000000"); // 单次申请最高100万
    private static final BigDecimal MAX_SINGLE_ITEM_AMOUNT = new BigDecimal("200000"); // 单条明细最高20万
    
    @Autowired
    private ExpenseItemRepository expenseItemRepository;
    
    @Override
    public ValidationResult validate(ExpenseApply apply, ValidationContext context) {
        ValidationResult result = new ValidationResult();
        
        // 基础信息校验
        result.combine(validateBasicInfo(apply));
        
        // 金额校验
        result.combine(validateAmounts(apply));
        
        // 业务逻辑校验
        result.combine(validateBusinessRules(apply));
        
        // 状态流转校验（如果提供了当前状态）
        if (context != null && context.hasContextData("currentStatus")) {
            Integer currentStatus = (Integer) context.getContextData("currentStatus");
            String operation = (String) context.getContextData("operation");
            result.combine(validateStatusTransition(apply, currentStatus, operation));
        }
        
        return result;
    }
    
    private ValidationResult validateBasicInfo(ExpenseApply apply) {
        ValidationResult result = new ValidationResult();
        BasicValidator basicValidator = new BasicValidator();
        
        // 申请标题不能为空
        if (apply.getTitle() == null || apply.getTitle().trim().isEmpty()) {
            result.addError(ValidationRuleType.NOT_NULL, "title", "申请标题不能为空");
        }
        
        // 申请人ID不能为空
        if (apply.getApplyUserId() == null) {
            result.addError(ValidationRuleType.NOT_NULL, "applyUserId", "申请人ID不能为空");
        }
        
        // 部门ID不能为空
        if (apply.getDeptId() == null) {
            result.addError(ValidationRuleType.NOT_NULL, "deptId", "部门ID不能为空");
        }
        
        // 申请时间必须在合理范围内
        LocalDate applyDate = apply.getApplyDate() != null ? apply.getApplyDate() : null;
        if (applyDate != null) {
            LocalDate minDate = LocalDate.now().minusYears(1); // 一年前的日期
            LocalDate maxDate = LocalDate.now().plusDays(30); // 30天后的日期
            result.combine(basicValidator.validateDateRange(applyDate, minDate, maxDate, "申请时间"));
        }
        
        return result;
    }
    
    private ValidationResult validateAmounts(ExpenseApply apply) {
        ValidationResult result = new ValidationResult();
        BasicValidator basicValidator = new BasicValidator();
        
        // 总金额校验
        if (apply.getTotalAmount() != null) {
            result.combine(basicValidator.validateAmountPositive(apply.getTotalAmount(), "总金额"));
            result.combine(basicValidator.validateAmountRange(apply.getTotalAmount(), 
                BigDecimal.ZERO, MAX_SINGLE_APPLY_AMOUNT, "总金额"));
        }
        
        // 校验费用明细
        if (apply.getId() != null) {
            List<ExpenseItem> items = expenseItemRepository.findByExpenseApplyId(apply.getId());
            if (items != null && !items.isEmpty()) {
                // 检查明细金额
                for (ExpenseItem item : items) {
                    if (item.getAmount() != null) {
                        result.combine(basicValidator.validateAmountRange(item.getAmount(), 
                            BigDecimal.ZERO, MAX_SINGLE_ITEM_AMOUNT, "明细金额"));
                    }
                }
                
                // 检查明细总金额与申请总金额是否一致
                BigDecimal calculatedTotal = items.stream()
                    .map(ExpenseItem::getAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                if (apply.getTotalAmount() != null && 
                    calculatedTotal.compareTo(apply.getTotalAmount()) != 0) {
                    result.addError(ValidationRuleType.AMOUNT_MAX_LIMIT, "totalAmount", 
                        "明细总金额" + calculatedTotal + "与申请总金额" + apply.getTotalAmount() + "不一致");
                }
            }
        }
        
        return result;
    }
    
    private ValidationResult validateBusinessRules(ExpenseApply apply) {
        ValidationResult result = new ValidationResult();
        
        // 检查是否有费用明细（对于提交审批的情况）
        if (apply.getId() != null && apply.getStatus() != null && apply.getStatus() == 1) {
            List<ExpenseItem> items = expenseItemRepository.findByExpenseApplyId(apply.getId());
            if (items == null || items.isEmpty()) {
                result.addError(ValidationRuleType.RELATED_ENTITY_EXISTS, "items", 
                    "提交审批前必须添加费用明细");
            }
        }
        
        // 检查预算是否充足（简化实现）
        if (apply.getDeptId() != null && apply.getTotalAmount() != null && apply.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            // 实际应该调用预算服务检查
            BigDecimal budgetLimit = new BigDecimal("50000"); // 简化：假设5万预算
            if (apply.getTotalAmount().compareTo(budgetLimit) > 0) {
                result.addError(ValidationRuleType.BUDGET_AVAILABLE, "totalAmount", 
                    "申请金额" + apply.getTotalAmount() + "超过预算限额" + budgetLimit);
            }
        }
        
        // 检查申请日期是否在预算年度内
        if (apply.getApplyTime() != null) {
            int applyYear = apply.getApplyTime().getYear();
            int currentYear = LocalDate.now().getYear();
            if (applyYear < currentYear - 1 || applyYear > currentYear + 1) {
                result.addError(ValidationRuleType.EXPENSE_DATE_VALID, "applyTime", 
                    "申请日期" + applyYear + "超出预算年度范围");
            }
        }
        
        return result;
    }
    
    private ValidationResult validateStatusTransition(ExpenseApply apply, Integer currentStatus, String operation) {
        ValidationResult result = new ValidationResult();
        
        if (currentStatus == null || operation == null) {
            return result;
        }
        
        // 状态流转校验
        switch (operation) {
            case "submit":
                if (currentStatus != 0) { // 草稿状态才能提交
                    result.addError(ValidationRuleType.STATUS_TRANSITION_VALID, "status", 
                        "只有草稿状态的申请才能提交审批");
                }
                break;
                
            case "approve":
                if (currentStatus != 1) { // 审批中状态才能审批
                    result.addError(ValidationRuleType.STATUS_TRANSITION_VALID, "status", 
                        "只有审批中的申请才能进行审批操作");
                }
                break;
                
            case "withdraw":
                if (currentStatus != 1) { // 审批中状态才能撤回
                    result.addError(ValidationRuleType.STATUS_TRANSITION_VALID, "status", 
                        "只有审批中的申请才能撤回");
                }
                break;
                
            case "delete":
                if (currentStatus != 0) { // 草稿状态才能删除
                    result.addError(ValidationRuleType.STATUS_TRANSITION_VALID, "status", 
                        "只有草稿状态的申请才能删除");
                }
                break;
        }
        
        return result;
    }
    
    @Override
    public boolean supports(Class<?> clazz) {
        return ExpenseApply.class.isAssignableFrom(clazz);
    }
}