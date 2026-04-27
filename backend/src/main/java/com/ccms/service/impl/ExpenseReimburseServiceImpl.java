package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ReimburseItem;
import com.ccms.entity.expense.ReimburseAttachment;
import com.ccms.repository.expense.ExpenseReimburseRepository;
import com.ccms.repository.expense.ReimburseItemRepository;
import com.ccms.repository.expense.ReimburseAttachmentRepository;
import com.ccms.service.ExpenseReimburseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 费用报销服务实现类
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class ExpenseReimburseServiceImpl implements ExpenseReimburseService {

    private final ExpenseReimburseRepository expenseReimburseRepository;
    private final ReimburseItemRepository reimburseItemRepository;
    private final ReimburseAttachmentRepository reimburseAttachmentRepository;

    @Autowired
    public ExpenseReimburseServiceImpl(ExpenseReimburseRepository expenseReimburseRepository,
                                     ReimburseItemRepository reimburseItemRepository,
                                     ReimburseAttachmentRepository reimburseAttachmentRepository) {
        this.expenseReimburseRepository = expenseReimburseRepository;
        this.reimburseItemRepository = reimburseItemRepository;
        this.reimburseAttachmentRepository = reimburseAttachmentRepository;
    }

    @Override
    public ExpenseReimburse createExpenseReimburse(ExpenseReimburse reimburse) {
        // 设置默认状态
        if (reimburse.getStatus() == null) {
            reimburse.setStatus(0); // 草稿状态
        }
        if (reimburse.getApprovalStatus() == null) {
            reimburse.setApprovalStatus(0); // 待提交
        }
        if (reimburse.getPaymentStatus() == null) {
            reimburse.setPaymentStatus(0); // 未支付
        }
        
        // 设置申请时间
        if (reimburse.getApplyTime() == null) {
            reimburse.setApplyTime(LocalDateTime.now());
        }
        
        // 生成报销编号
        if (reimburse.getReimburseCode() == null || reimburse.getReimburseCode().isEmpty()) {
            reimburse.setReimburseCode(generateReimburseCode());
        }
        
        return expenseReimburseRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburse updateExpenseReimburse(ExpenseReimburse reimburse) {
        Optional<ExpenseReimburse> existingOpt = expenseReimburseRepository.findById(reimburse.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse existing = existingOpt.get();
        // 检查是否允许修改（草稿状态才允许修改）
        if (existing.getStatus() != 0 && existing.getApprovalStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许修改");
        }
        
        // 更新可修改的字段
        existing.setReimburseTitle(reimburse.getReimburseTitle());
        existing.setDescription(reimburse.getDescription());
        existing.setExpectedDate(reimburse.getExpectedDate());
        
        return expenseReimburseRepository.save(existing);
    }

    @Override
    public void submitExpenseReimburseForApproval(Long reimburseId) {
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        // 只有草稿状态才能提交审批
        if (reimburse.getStatus() != 0 || reimburse.getApprovalStatus() != 0) {
            throw new RuntimeException("费用报销申请状态不允许提交审批");
        }
        
        // 检查报销明细是否完整
        List<ReimburseItem> items = reimburseItemRepository.findByExpenseReimburseId(reimburseId);
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("报销明细不能为空");
        }
        
        // 计算总金额
        BigDecimal totalAmount = calculateTotalAmount(reimburseId);
        reimburse.setTotalAmount(totalAmount);
        
        // 更新状态为待审批
        reimburse.setStatus(1); // 审批中
        reimburse.setApprovalStatus(1); // 审批中
        reimburse.setSubmitTime(LocalDateTime.now());
        
        expenseReimburseRepository.save(reimburse);
    }

    @Override
    public void withdrawExpenseReimburse(Long reimburseId) {
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        // 只有审批中的申请才能撤回
        if (reimburse.getStatus() != 1) {
            throw new RuntimeException("只有审批中的申请才能撤回");
        }
        
        // 撤回申请，恢复为草稿状态
        reimburse.setStatus(0);
        reimburse.setApprovalStatus(0);
        
        expenseReimburseRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburse getExpenseReimburseById(Long reimburseId) {
        return expenseReimburseRepository.findById(reimburseId).orElse(null);
    }

    @Override
    public List<ExpenseReimburse> getExpenseReimbursesByUser(Long userId) {
        return expenseReimburseRepository.findByUserId(userId);
    }

    @Override
    public List<ExpenseReimburse> getExpenseReimbursesByDept(Long deptId) {
        return expenseReimburseRepository.findByDeptId(deptId);
    }

    @Override
    public ReimburseItem addReimburseItem(ReimburseItem item) {
        // 验证费用报销申请存在
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(item.getExpenseReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        // 只有在草稿状态才能添加明细
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许添加明细");
        }
        
        // 设置默认值
        if (item.getExpenseDate() == null) {
            item.setExpenseDate(LocalDate.now());
        }
        if (item.getQuantity() == null) {
            item.setQuantity(1);
        }
        if (item.getUnitPrice() != null && item.getQuantity() != null) {
            item.setAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        return reimburseItemRepository.save(item);
    }

    @Override
    public ReimburseItem updateReimburseItem(ReimburseItem item) {
        Optional<ReimburseItem> existingOpt = reimburseItemRepository.findById(item.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 验证费用报销申请状态
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(item.getExpenseReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许修改明细");
        }
        
        ReimburseItem existing = existingOpt.get();
        // 更新可修改字段
        existing.setExpenseType(item.getExpenseType());
        existing.setDescription(item.getDescription());
        existing.setExpenseDate(item.getExpenseDate());
        existing.setQuantity(item.getQuantity());
        existing.setUnitPrice(item.getUnitPrice());
        
        // 重新计算金额
        if (existing.getUnitPrice() != null && existing.getQuantity() != null) {
            existing.setAmount(existing.getUnitPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
        }
        
        return reimburseItemRepository.save(existing);
    }

    @Override
    public void deleteReimburseItem(Long itemId) {
        Optional<ReimburseItem> itemOpt = reimburseItemRepository.findById(itemId);
        if (itemOpt.isEmpty()) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 验证费用报销申请状态
        ReimburseItem item = itemOpt.get();
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(item.getExpenseReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许删除明细");
        }
        
        reimburseItemRepository.deleteById(itemId);
    }

    @Override
    public List<ReimburseItem> getReimburseItems(Long reimburseId) {
        return reimburseItemRepository.findByExpenseReimburseId(reimburseId);
    }

    @Override
    public ReimburseAttachment addReimburseAttachment(ReimburseAttachment attachment) {
        // 验证费用报销申请存在
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(attachment.getExpenseReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        // 设置上传时间
        if (attachment.getUploadTime() == null) {
            attachment.setUploadTime(LocalDateTime.now());
        }
        
        return reimburseAttachmentRepository.save(attachment);
    }

    @Override
    public void deleteReimburseAttachment(Long attachmentId) {
        Optional<ReimburseAttachment> attachmentOpt = reimburseAttachmentRepository.findById(attachmentId);
        if (attachmentOpt.isEmpty()) {
            throw new RuntimeException("附件不存在");
        }
        
        reimburseAttachmentRepository.deleteById(attachmentId);
    }

    @Override
    public List<ReimburseAttachment> getReimburseAttachments(Long reimburseId) {
        return reimburseAttachmentRepository.findByExpenseReimburseId(reimburseId);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long reimburseId) {
        List<ReimburseItem> items = reimburseItemRepository.findByExpenseReimburseId(reimburseId);
        
        return items.stream()
                .map(ReimburseItem::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public long getPendingExpenseReimburseCount(Long userId) {
        // 这里简化实现，实际应根据审批流程查询
        return expenseReimburseRepository.countByStatusAndApproverId(1, userId);
    }

    @Override
    public List<ExpenseReimburse> getExpenseReimbursesByStatus(Integer status, LocalDate startDate, LocalDate endDate) {
        return expenseReimburseRepository.findByStatusAndDateRange(status, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
    }

    @Override
    public void linkExpenseApply(Long reimburseId, Long applyId) {
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        // 只有在草稿状态才能关联申请
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许关联申请");
        }
        
        reimburse.setExpenseApplyId(applyId);
        expenseReimburseRepository.save(reimburse);
    }

    @Override
    public Long getLinkedExpenseApply(Long reimburseId) {
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        return reimburseOpt.get().getExpenseApplyId();
    }

    @Override
    public void processReimbursePayment(Long reimburseId, Integer paymentMethod, String paymentDocNumber) {
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        // 只有审批通过才能支付
        if (reimburse.getApprovalStatus() != 2) {
            throw new RuntimeException("只有审批通过的报销申请才能进行支付");
        }
        
        reimburse.setPaymentStatus(1); // 已支付
        reimburse.setPaymentMethod(paymentMethod);
        reimburse.setPaymentDocNumber(paymentDocNumber);
        reimburse.setPaymentTime(LocalDateTime.now());
        
        expenseReimburseRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburseStatistics getExpenseReimburseStatistics(Long deptId, LocalDate startDate, LocalDate endDate) {
        Long totalCount = expenseReimburseRepository.countByDeptIdAndDateRange(deptId, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        BigDecimal totalAmount = expenseReimburseRepository.sumAmountByDeptIdAndDateRange(deptId, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        Long pendingCount = expenseReimburseRepository.countByDeptIdAndStatusAndDateRange(deptId, 1, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        Long approvedCount = expenseReimburseRepository.countByDeptIdAndStatusAndDateRange(deptId, 2, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        Long paidCount = expenseReimburseRepository.countByDeptIdAndPaymentStatusAndDateRange(deptId, 1, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        BigDecimal totalPaidAmount = expenseReimburseRepository.sumPaidAmountByDeptIdAndDateRange(deptId, 
                startDate.atStartOfDay(), 
                endDate.atTime(23, 59, 59));
        
        return new ExpenseReimburseStatistics(totalCount, totalAmount, pendingCount, 
                approvedCount, paidCount, totalPaidAmount);
    }
    
    /**
     * 生成费用报销编号
     */
    private String generateReimburseCode() {
        return "REIMBURSE_" + System.currentTimeMillis();
    }
}