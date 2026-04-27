package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseApply;
import com.ccms.entity.expense.ExpenseItem;
import com.ccms.entity.expense.ExpenseAttachment;
import com.ccms.repository.expense.ExpenseApplyRepository;
import com.ccms.repository.expense.ExpenseItemRepository;
import com.ccms.repository.expense.ExpenseAttachmentRepository;
import com.ccms.service.ExpenseApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 费用申请服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class ExpenseApplyServiceImpl implements ExpenseApplyService {

    private final ExpenseApplyRepository expenseApplyRepository;
    private final ExpenseItemRepository expenseItemRepository;
    private final ExpenseAttachmentRepository expenseAttachmentRepository;

    @Autowired
    public ExpenseApplyServiceImpl(ExpenseApplyRepository expenseApplyRepository,
                                 ExpenseItemRepository expenseItemRepository,
                                 ExpenseAttachmentRepository expenseAttachmentRepository) {
        this.expenseApplyRepository = expenseApplyRepository;
        this.expenseItemRepository = expenseItemRepository;
        this.expenseAttachmentRepository = expenseAttachmentRepository;
    }

    @Override
    public ExpenseApply createExpenseApply(ExpenseApply apply) {
        // 设置默认状态
        if (apply.getStatus() == null) {
            apply.setStatus(0); // 草稿状态
        }
        if (apply.getApprovalStatus() == null) {
            apply.setApprovalStatus(0); // 待提交
        }
        
        // 设置申请时间
        if (apply.getApplyTime() == null) {
            apply.setApplyTime(LocalDateTime.now());
        }
        
        // 生成申请编号
        if (apply.getApplyCode() == null || apply.getApplyCode().isEmpty()) {
            apply.setApplyCode(generateApplyCode());
        }
        
        return expenseApplyRepository.save(apply);
    }

    @Override
    public ExpenseApply updateExpenseApply(ExpenseApply apply) {
        Optional<ExpenseApply> existingOpt = expenseApplyRepository.findById(apply.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        ExpenseApply existing = existingOpt.get();
        // 检查是否允许修改（草稿状态才允许修改）
        if (existing.getStatus() != 0 && existing.getApprovalStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用申请不允许修改");
        }
        
        // 更新可修改的字段
        existing.setApplyTitle(apply.getApplyTitle());
        existing.setDescription(apply.getDescription());
        existing.setExpectedDate(apply.getExpectedDate());
        
        return expenseApplyRepository.save(existing);
    }

    @Override
    public void submitExpenseApplyForApproval(Long applyId) {
        Optional<ExpenseApply> applyOpt = expenseApplyRepository.findById(applyId);
        if (applyOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        ExpenseApply apply = applyOpt.get();
        // 只有草稿状态才能提交审批
        if (apply.getStatus() != 0 || apply.getApprovalStatus() != 0) {
            throw new RuntimeException("费用申请状态不允许提交审批");
        }
        
        // 检查费用明细是否完整
        List<ExpenseItem> items = expenseItemRepository.findByExpenseApplyId(applyId);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("费用明细不能为空");
        }
        
        // 计算总金额
        BigDecimal totalAmount = calculateTotalAmount(applyId);
        apply.setTotalAmount(totalAmount);
        
        // 更新状态为待审批
        apply.setStatus(1); // 审批中
        apply.setApprovalStatus(1); // 审批中
        apply.setSubmitTime(LocalDateTime.now());
        
        expenseApplyRepository.save(apply);
    }

    @Override
    public void withdrawExpenseApply(Long applyId) {
        Optional<ExpenseApply> applyOpt = expenseApplyRepository.findById(applyId);
        if (applyOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        ExpenseApply apply = applyOpt.get();
        // 只有审批中的申请才能撤回
        if (apply.getStatus() != 1) {
            throw new RuntimeException("只有审批中的申请才能撤回");
        }
        
        // 撤回申请，恢复为草稿状态
        apply.setStatus(0);
        apply.setApprovalStatus(0);
        
        expenseApplyRepository.save(apply);
    }

    @Override
    public ExpenseApply getExpenseApplyById(Long applyId) {
        return expenseApplyRepository.findById(applyId).orElse(null);
    }

    @Override
    public List<ExpenseApply> getExpenseAppliesByUser(Long userId) {
        return expenseApplyRepository.findByUserId(userId);
    }

    @Override
    public List<ExpenseApply> getExpenseAppliesByDept(Long deptId) {
        return expenseApplyRepository.findByDeptId(deptId);
    }

    @Override
    public ExpenseItem addExpenseItem(ExpenseItem item) {
        // 验证费用申请存在
        Optional<ExpenseApply> applyOpt = expenseApplyRepository.findById(item.getExpenseApplyId());
        if (applyOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        ExpenseApply apply = applyOpt.get();
        // 只有在草稿状态才能添加明细
        if (apply.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用申请不允许添加明细");
        }
        
        // 设置默认值
        if (item.getQuantity() == null) {
            item.setQuantity(1);
        }
        if (item.getUnitPrice() != null && item.getQuantity() != null) {
            item.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        return expenseItemRepository.save(item);
    }

    @Override
    public ExpenseItem updateExpenseItem(ExpenseItem item) {
        Optional<ExpenseItem> existingOpt = expenseItemRepository.findById(item.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("费用明细不存在");
        }
        
        // 验证费用申请状态
        Optional<ExpenseApply> applyOpt = expenseApplyRepository.findById(item.getExpenseApplyId());
        if (applyOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        ExpenseApply apply = applyOpt.get();
        if (apply.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用申请不允许修改明细");
        }
        
        ExpenseItem existing = existingOpt.get();
        // 更新可修改字段
        existing.setExpenseType(item.getExpenseType());
        existing.setDescription(item.getDescription());
        existing.setQuantity(item.getQuantity());
        existing.setUnitPrice(item.getUnitPrice());
        
        // 重新计算金额
        if (existing.getUnitPrice() != null && existing.getQuantity() != null) {
            existing.setAmount(existing.getUnitPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
        }
        
        return expenseItemRepository.save(existing);
    }

    @Override
    public void deleteExpenseItem(Long itemId) {
        Optional<ExpenseItem> itemOpt = expenseItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("费用明细不存在");
        }
        
        // 验证费用申请状态
        ExpenseItem item = itemOpt.get();
        Optional<ExpenseApply> applyOpt = expenseApplyRepository.findById(item.getExpenseApplyId());
        if (applyOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        ExpenseApply apply = applyOpt.get();
        if (apply.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用申请不允许删除明细");
        }
        
        expenseItemRepository.deleteById(itemId);
    }

    @Override
    public List<ExpenseItem> getExpenseItems(Long applyId) {
        return expenseItemRepository.findByExpenseApplyId(applyId);
    }

    @Override
    public ExpenseAttachment addExpenseAttachment(ExpenseAttachment attachment) {
        // 验证费用申请存在
        Optional<ExpenseApply> applyOpt = expenseApplyRepository.findById(attachment.getExpenseApplyId());
        if (applyOpt.isEmpty()) {
            throw new RuntimeException("费用申请不存在");
        }
        
        // 设置上传时间
        if (attachment.getUploadTime() == null) {
            attachment.setUploadTime(LocalDateTime.now());
        }
        
        return expenseAttachmentRepository.save(attachment);
    }

    @Override
    public void deleteExpenseAttachment(Long attachmentId) {
        Optional<ExpenseAttachment> attachmentOpt = expenseAttachmentRepository.findById(attachmentId);
        if (attachmentOpt.isEmpty()) {
            throw new RuntimeException("附件不存在");
        }
        
        expenseAttachmentRepository.deleteById(attachmentId);
    }

    @Override
    public List<ExpenseAttachment> getExpenseAttachments(Long applyId) {
        return expenseAttachmentRepository.findByExpenseApplyId(applyId);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long applyId) {
        List<ExpenseItem> items = expenseItemRepository.findByExpenseApplyId(applyId);
        
        return items.stream()
                .map(ExpenseItem::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public boolean checkBudgetAvailability(Long deptId, Integer expenseType, BigDecimal amount, LocalDate applyDate) {
        // 调用预算服务检查预算是否充足
        // 这里简化实现，实际应调用BudgetService
        return true; // 简化返回true
    }

    @Override
    public long getPendingExpenseApplyCount(Long userId) {
        // 这里简化实现，实际应根据审批流程查询
        return expenseApplyRepository.countByStatusAndApproverId(1, userId);
    }

    @Override
    public List<ExpenseApply> getExpenseAppliesByStatus(Integer status, LocalDate startDate, LocalDate endDate) {
        return expenseApplyRepository.findByStatusAndDateRange(status, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
    }

    @Override
    public ExpenseApplyStatistics getExpenseApplyStatistics(Long deptId, LocalDate startDate, LocalDate endDate) {
        Long totalCount = expenseApplyRepository.countByDeptIdAndDateRange(deptId, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        BigDecimal totalAmount = expenseApplyRepository.sumAmountByDeptIdAndDateRange(deptId, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        Long pendingCount = expenseApplyRepository.countByDeptIdAndStatusAndDateRange(deptId, 1, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        Long approvedCount = expenseApplyRepository.countByDeptIdAndStatusAndDateRange(deptId, 2, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        Long rejectedCount = expenseApplyRepository.countByDeptIdAndStatusAndDateRange(deptId, 3, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        return new ExpenseApplyStatistics(totalCount, totalAmount, pendingCount, approvedCount, rejectedCount);
    }
    
    /**
     * 生成费用申请编号
     */
    private String generateApplyCode() {
        return "EXPENSE_" + System.currentTimeMillis();
    }
}