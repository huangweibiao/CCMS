package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseReimburse;
import com.ccms.entity.expense.ReimburseItem;
import com.ccms.entity.expense.ReimburseAttachment;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.entity.expense.ExpenseInvoice;
import com.ccms.entity.approval.ApprovalInstance;
import com.ccms.entity.approval.ApprovalFlowConfig;
import com.ccms.entity.approval.ApprovalRecord;
import com.ccms.repository.expense.ExpenseReimburseRepository;
import com.ccms.repository.expense.ReimburseItemRepository;
import com.ccms.repository.expense.ReimburseAttachmentRepository;
import com.ccms.repository.expense.ExpenseReimburseDetailRepository;
import com.ccms.repository.expense.ExpenseInvoiceRepository;
import com.ccms.repository.approval.ApprovalInstanceRepository;
import com.ccms.repository.approval.ApprovalFlowConfigRepository;
import com.ccms.repository.approval.ApprovalRecordRepository;
import com.ccms.service.ExpenseReimburseService;
import com.ccms.service.ApprovalFlowService;
import com.ccms.dto.ApprovalRequest;
import com.ccms.enums.ApprovalStatus;
import com.ccms.enums.ApprovalAction;
import com.ccms.enums.BusinessTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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
    private final ExpenseReimburseDetailRepository expenseReimburseDetailRepository;
    private final ExpenseInvoiceRepository expenseInvoiceRepository;
    private final ApprovalInstanceRepository approvalInstanceRepository;
    private final ApprovalFlowConfigRepository approvalFlowConfigRepository;
    private final ApprovalRecordRepository approvalRecordRepository;
    private final ApprovalFlowService approvalFlowService;

    @Autowired
    public ExpenseReimburseServiceImpl(ExpenseReimburseRepository expenseReimburseRepository,
                                     ReimburseItemRepository reimburseItemRepository,
                                     ReimburseAttachmentRepository reimburseAttachmentRepository,
                                     ExpenseReimburseDetailRepository expenseReimburseDetailRepository,
                                     ExpenseInvoiceRepository expenseInvoiceRepository,
                                     ApprovalInstanceRepository approvalInstanceRepository,
                                     ApprovalFlowConfigRepository approvalFlowConfigRepository,
                                     ApprovalRecordRepository approvalRecordRepository,
                                     ApprovalFlowService approvalFlowService) {
        this.expenseReimburseRepository = expenseReimburseRepository;
        this.reimburseItemRepository = reimburseItemRepository;
        this.reimburseAttachmentRepository = reimburseAttachmentRepository;
        this.expenseReimburseDetailRepository = expenseReimburseDetailRepository;
        this.expenseInvoiceRepository = expenseInvoiceRepository;
        this.approvalInstanceRepository = approvalInstanceRepository;
        this.approvalFlowConfigRepository = approvalFlowConfigRepository;
        this.approvalRecordRepository = approvalRecordRepository;
        this.approvalFlowService = approvalFlowService;
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
        
        // 生成报销编号
        if (reimburse.getReimburseNo() == null || reimburse.getReimburseNo().isEmpty()) {
            reimburse.setReimburseNo(generateReimburseCode());
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
        existing.setTitle(reimburse.getTitle());
        existing.setRemark(reimburse.getRemark());
        
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
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburseId);
        
        if ((items == null || items.isEmpty()) && (details == null || details.isEmpty())) {
            throw new RuntimeException("报销明细和费用明细不能同时为空");
        }
        
        // 检查发票验真状态（有发票的情况下）
        List<ExpenseInvoice> invoices = expenseInvoiceRepository.findByReimburseId(reimburseId);
        for (ExpenseInvoice invoice : invoices) {
            if (invoice.getVerifyStatus() != null && invoice.getVerifyStatus() != 1) {
                throw new RuntimeException("存在未通过验真的发票，请先完成发票验真");
            }
        }
        
        // 计算并验证总金额
        BigDecimal totalAmount = calculateTotalAmount(reimburseId);
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("报销总金额必须大于0");
        }
        
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
        // 只有审批通过且未支付的才能支付
        if (reimburse.getApprovalStatus() != 2) {
            throw new RuntimeException("只有审批通过的报销申请才能进行支付");
        }
        
        if (reimburse.getPaymentStatus() == 1) {
            throw new RuntimeException("报销申请已支付，不能重复支付");
        }
        
        // 处理借款抵扣逻辑
        handleLoanDeduction(reimburse);
        
        // 记录实际支付金额（考虑借款抵扣后）
        BigDecimal actualPaymentAmount = calculateActualPaymentAmount(reimburse);
        
        reimburse.setPaymentStatus(1); // 已支付
        reimburse.setPaymentMethod(paymentMethod);
        reimburse.setPaymentDocNumber(paymentDocNumber);
        reimburse.setPaymentTime(LocalDateTime.now());
        reimburse.setActualPaymentAmount(actualPaymentAmount);
        
        // 通知财务系统（简化实现）
        notifyFinanceSystem(reimburse);
        
        expenseReimburseRepository.save(reimburse);
    }
    
    /**
     * 处理借款抵扣逻辑
     */
    private void handleLoanDeduction(ExpenseReimburse reimburse) {
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburse.getId());
        
        BigDecimal totalLoanDeduct = details.stream()
                .map(ExpenseReimburseDetail::getLoanDeductAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 如果存在借款抵扣，记录抵扣金额
        if (totalLoanDeduct.compareTo(BigDecimal.ZERO) > 0) {
            reimburse.setLoanDeductionAmount(totalLoanDeduct);
        }
    }
    
    /**
     * 计算实际支付金额
     */
    private BigDecimal calculateActualPaymentAmount(ExpenseReimburse reimburse) {
        BigDecimal totalAmount = reimburse.getTotalAmount();
        BigDecimal loanDeduction = reimburse.getLoanDeductionAmount();
        
        if (loanDeduction == null) {
            return totalAmount;
        }
        
        // 实际支付金额 = 总金额 - 借款抵扣金额
        return totalAmount.subtract(loanDeduction);
    }
    
    /**
     * 通知财务系统（简化实现）
     */
    private void notifyFinanceSystem(ExpenseReimburse reimburse) {
        // 这里应该调用财务系统的API
        // 暂时记录日志
        System.out.println("通知财务系统：报销单 " + reimburse.getReimburseNo() + " 已完成支付，金额：" + reimburse.getActualPaymentAmount());
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

    @Override
    public ExpenseReimburseDetail createExpenseReimburseDetail(ExpenseReimburseDetail detail) {
        // 验证报销单是否存在
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(detail.getReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        // 检查报销单状态
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("只有在草稿状态才能添加明细");
        }
        
        // 自动计算金额，如果数量和单价都提供的话
        if (detail.getQuantity() != null && detail.getUnitPrice() != null) {
            detail.setAmount(detail.getUnitPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));
        }
        
        return expenseReimburseDetailRepository.save(detail);
    }

    @Override
    public ExpenseReimburseDetail updateExpenseReimburseDetail(ExpenseReimburseDetail detail) {
        Optional<ExpenseReimburseDetail> existingOpt = expenseReimburseDetailRepository.findById(detail.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 验证报销单状态
        ExpenseReimburseDetail existing = existingOpt.get();
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(existing.getReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许修改明细");
        }
        
        // 更新字段
        existing.setFeeTypeId(detail.getFeeTypeId());
        existing.setExpenseDate(detail.getExpenseDate());
        existing.setQuantity(detail.getQuantity());
        existing.setUnitPrice(detail.getUnitPrice());
        existing.setDescription(detail.getDescription());
        existing.setInvoiceNo(detail.getInvoiceNo());
        existing.setBudgetId(detail.getBudgetId());
        existing.setLoanDeductAmount(detail.getLoanDeductAmount());
        
        // 重新计算金额
        if (existing.getQuantity() != null && existing.getUnitPrice() != null) {
            existing.setAmount(existing.getUnitPrice().multiply(BigDecimal.valueOf(existing.getQuantity())));
        } else if (detail.getAmount() != null) {
            existing.setAmount(detail.getAmount());
        }
        
        return expenseReimburseDetailRepository.save(existing);
    }

    @Override
    public void deleteExpenseReimburseDetail(Long detailId) {
        Optional<ExpenseReimburseDetail> detailOpt = expenseReimburseDetailRepository.findById(detailId);
        if (detailOpt.isEmpty()) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 验证报销单状态
        ExpenseReimburseDetail detail = detailOpt.get();
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(detail.getReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许删除明细");
        }
        
        expenseReimburseDetailRepository.deleteById(detailId);
    }

    @Override
    public List<ExpenseReimburseDetail> getExpenseReimburseDetails(Long reimburseId) {
        return expenseReimburseDetailRepository.findByReimburseId(reimburseId);
    }

    @Override
    public ExpenseInvoice createExpenseInvoice(ExpenseInvoice invoice) {
        // 验证报销明细存在
        Optional<ExpenseReimburseDetail> detailOpt = expenseReimburseDetailRepository.findById(invoice.getReimburseDetailId());
        if (detailOpt.isEmpty()) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 验证发票号码唯一性（同一报销明细）
        List<ExpenseInvoice> existingInvoices = expenseInvoiceRepository.findByReimburseDetailId(invoice.getReimburseDetailId());
        if (existingInvoices.stream().anyMatch(i -> i.getInvoiceNo().equals(invoice.getInvoiceNo()))) {
            throw new RuntimeException("同一报销明细下发票号码不能重复");
        }
        
        // 设置默认状态
        if (invoice.getVerifyStatus() == null) {
            invoice.setVerifyStatus(0); // 未验真
        }
        
        return expenseInvoiceRepository.save(invoice);
    }

    @Override
    public ExpenseInvoice updateExpenseInvoice(ExpenseInvoice invoice) {
        Optional<ExpenseInvoice> existingOpt = expenseInvoiceRepository.findById(invoice.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("发票记录不存在");
        }
        
        ExpenseInvoice existing = existingOpt.get();
        
        // 验证报销明细存在
        Optional<ExpenseReimburseDetail> detailOpt = expenseReimburseDetailRepository.findById(existing.getReimburseDetailId());
        if (detailOpt.isEmpty()) {
            throw new RuntimeException("报销明细不存在");
        }
        
        // 验证报销单状态
        ExpenseReimburseDetail detail = detailOpt.get();
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(detail.getReimburseId());
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        ExpenseReimburse reimburse = reimburseOpt.get();
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的费用报销申请不允许修改发票信息");
        }
        
        // 更新字段
        existing.setInvoiceType(invoice.getInvoiceType());
        existing.setInvoiceCode(invoice.getInvoiceCode());
        existing.setInvoiceAmount(invoice.getInvoiceAmount());
        existing.setTaxAmount(invoice.getTaxAmount());
        existing.setInvoiceDate(invoice.getInvoiceDate());
        existing.setSellerName(invoice.getSellerName());
        existing.setSellerTaxNo(invoice.getSellerTaxNo());
        existing.setFilePath(invoice.getFilePath());
        
        // 如果修改了发票号码，需要重置验真状态
        if (!existing.getInvoiceNo().equals(invoice.getInvoiceNo())) {
            existing.setInvoiceNo(invoice.getInvoiceNo());
            existing.setVerifyStatus(0); // 重置为未验真
            existing.setVerifyResult(null);
            existing.setVerifyTime(null);
        }
        
        return expenseInvoiceRepository.save(existing);
    }

    @Override
    public List<ExpenseInvoice> getExpenseInvoicesByNo(String invoiceNo) {
        return expenseInvoiceRepository.findByInvoiceNo(invoiceNo);
    }

    @Override
    public List<ExpenseInvoice> getExpenseInvoicesByReimburse(Long reimburseId) {
        return expenseInvoiceRepository.findByReimburseId(reimburseId);
    }

    @Override
    public boolean verifyExpenseInvoice(Long invoiceId) {
        Optional<ExpenseInvoice> invoiceOpt = expenseInvoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            throw new RuntimeException("发票记录不存在");
        }
        
        ExpenseInvoice invoice = invoiceOpt.get();
        
        // 模拟发票验真（实际应调用外部API）
        // 这里简化实现，检查发票号码格式等
        if (invoice.getInvoiceNo() == null || invoice.getInvoiceNo().length() < 8) {
            invoice.setVerifyStatus(2); // 验真失败
            invoice.setVerifyResult("发票号码格式不正确");
            invoice.setVerifyTime(LocalDateTime.now());
            expenseInvoiceRepository.save(invoice);
            return false;
        }
        
        // 模拟成功验真
        invoice.setVerifyStatus(1); // 已验真通过
        invoice.setVerifyResult("验真通过");
        invoice.setVerifyTime(LocalDateTime.now());
        expenseInvoiceRepository.save(invoice);
        
        return true;
    }
    
    /**
     * 生成费用报销编号
     */
    private String generateReimburseCode() {
        return "REIMBURSE_" + System.currentTimeMillis();
    }
    
    // ========== 审批流程集成方法 ==========
    
    @Override
    public ApprovalInstance submitExpenseReimburseForApproval(Long reimburseId, Long applicantId, String title) {
        ExpenseReimburse reimburse = getExpenseReimburseById(reimburseId);
        if (reimburse == null) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        // 检查是否在审批中
        if (isUnderApproval(reimburseId)) {
            throw new RuntimeException("费用报销申请已在审批中");
        }
        
        // 创建审批请求
        ApprovalRequest request = new ApprovalRequest();
        request.setBusinessId(reimburseId.toString());
        request.setBusinessType(BusinessTypeEnum.EXPENSE_REIMBURSE);
        request.setApplicantId(applicantId);
        request.setTitle(title != null ? title : "费用报销申请审批-" + reimburse.getReimburseNo());
        // 移除setDescription方法调用，ApprovalRequest没有这个方法
        
        // 提交审批流程
        ApprovalInstance approvalInstance = approvalFlowService.startApprovalInstance(
            request.getBusinessType(), 
            request.getBusinessId(), 
            request.getApplicantId(), 
            request.getTitle(), 
            "费用报销申请审批流程"
        );
        
        // 更新报销申请状态为审批中
        reimburse.setStatus(1); // 审批中
        reimburse.setApprovalStatus(1); // 审批中
        expenseReimburseRepository.save(reimburse);
        
        return approvalInstance;
    }
    
    @Override
    public void withdrawExpenseReimburseApproval(Long reimburseId, String remarks) {
        ExpenseReimburse reimburse = getExpenseReimburseById(reimburseId);
        if (reimburse == null) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        // 撤回审批流程
        approvalFlowService.withdrawApprovalInstance(reimburseId.toString(), remarks);
        
        // 更新报销申请状态为草稿
        reimburse.setStatus(0); // 草稿
        reimburse.setApprovalStatus(0); // 待提交
        expenseReimburseRepository.save(reimburse);
    }
    
    @Override
    public ApprovalStatus getApprovalStatus(Long reimburseId) {
        // 查找关联的审批实例
        Optional<ApprovalInstance> instanceOpt = approvalInstanceRepository.findByBusinessId(reimburseId.toString());
        if (instanceOpt.isPresent()) {
            ApprovalInstance instance = instanceOpt.get();
            return ApprovalStatus.valueOf(instance.getStatus());
        }
        
        // 如果没有审批实例，获取报销申请自己的状态
        ExpenseReimburse reimburse = getExpenseReimburseById(reimburseId);
        if (reimburse == null) {
            return ApprovalStatus.REJECTED; // 默认拒绝状态
        }
        
        // 映射内部状态到审批状态
        return mapInternalStatusToApprovalStatus(reimburse.getApprovalStatus());
    }
    
    @Override
    public List<Map<String, Object>> getApprovalRecords(Long reimburseId) {
        List<Map<String, Object>> records = new ArrayList<>();
        
        // 获取审批记录
        List<ApprovalRecord> approvalRecords = approvalRecordRepository.findByBusinessId(reimburseId.toString());
        for (ApprovalRecord record : approvalRecords) {
            Map<String, Object> recordMap = new HashMap<>();
            recordMap.put("approverId", record.getApproverId());
            recordMap.put("action", ApprovalAction.valueOf(record.getAction()));
            recordMap.put("remarks", record.getRemarks());
            recordMap.put("approvalTime", record.getApprovalTime());
            records.add(recordMap);
        }
        
        return records;
    }
    
    @Override
    public boolean isUnderApproval(Long reimburseId) {
        ApprovalStatus status = getApprovalStatus(reimburseId);
        return status == ApprovalStatus.PENDING || status == ApprovalStatus.IN_PROGRESS;
    }
    
    @Override
    public void onApprovalCompleted(Long reimburseId, ApprovalStatus finalStatus) {
        ExpenseReimburse reimburse = getExpenseReimburseById(reimburseId);
        if (reimburse == null) {
            throw new RuntimeException("费用报销申请不存在");
        }
        
        // 根据最终审批状态更新报销申请状态
        reimburse.setStatus(mapApprovalStatusToInternalStatus(finalStatus));
        reimburse.setApprovalStatus(mapApprovalStatusToInternalStatus(finalStatus));
        
        // 设置审批完成时间
        reimburse.setApprovedTime(LocalDateTime.now());
        
        expenseReimburseRepository.save(reimburse);
        
        // TODO: 发送通知，更新相关状态等
    }
    
    /**
     * 映射内部状态到审批状态
     */
    private ApprovalStatus mapInternalStatusToApprovalStatus(Integer internalStatus) {
        if (internalStatus == null) return ApprovalStatus.REJECTED;
        
        switch (internalStatus) {
            case 0: return ApprovalStatus.REJECTED; // 待提交
            case 1: return ApprovalStatus.IN_PROGRESS; // 审批中
            case 2: return ApprovalStatus.APPROVED; // 审批通过
            case 3: return ApprovalStatus.REJECTED; // 审批拒绝
            default: return ApprovalStatus.REJECTED;
        }
    }
    
    /**
     * 映射审批状态到内部状态
     */
    private Integer mapApprovalStatusToInternalStatus(ApprovalStatus approvalStatus) {
        switch (approvalStatus) {
            case APPROVED: return 2; // 审批通过
            case REJECTED: return 3; // 审批拒绝
            case WITHDRAWN: return 0; // 已撤回（恢复为草稿）
            case CANCELLED: return 4; // 已取消
            default: return 1; // 审批中
        }
    }
    
    // ========== Controller需要的额外方法 ==========
    
    @Override
    public Page<ExpenseReimburse> getExpenseReimburseList(int pageNum, int pageSize, Long userId, Long deptId, Integer status, Integer paymentStatus) {
        // 简化实现 - 返回空分页数据
        return new PageImpl<>(new ArrayList<ExpenseReimburse>(), PageRequest.of(pageNum, pageSize), 0);
    }
    
    public boolean checkPermission(String token, String permission) {
        // 简化权限检查
        return true;
    }
    
    public boolean deleteExpenseReimburse(Long reimburseId) {
        try {
            expenseReimburseRepository.deleteById(reimburseId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean approveExpenseReimburse(Long reimburseId, Long approverId, Integer action, String comment) {
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isPresent()) {
            ExpenseReimburse reimburse = reimburseOpt.get();
            
            // 验证当前状态是否可以审批
            if (reimburse.getApprovalStatus() != 1) { // 非审批中状态
                throw new RuntimeException("只有审批中的报销申请才能进行审批操作");
            }
            
            if (action == 1) { // 通过
                reimburse.setApprovalStatus(2); // 已审批
                reimburse.setStatus(2); // 审批通过
                reimburse.setApprovedUserId(approverId);
                reimburse.setApprovedTime(LocalDateTime.now());
                reimburse.setApprovalComment(comment);
            } else if (action == 2) { // 拒绝
                reimburse.setApprovalStatus(3); // 已拒绝
                reimburse.setStatus(3); // 审批拒绝
                reimburse.setApprovedUserId(approverId);
                reimburse.setApprovedTime(LocalDateTime.now());
                reimburse.setApprovalComment(comment);
            } else if (action == 3) { // 退回修改
                reimburse.setApprovalStatus(0); // 待提交
                reimburse.setStatus(0); // 草稿状态
                reimburse.setApprovalComment("审批人建议修改: " + comment);
            } else {
                throw new RuntimeException("无效的审批操作");
            }
            
            expenseReimburseRepository.save(reimburse);
            return true;
        }
        return false;
    }
    
    public boolean linkToExpenseApply(Long reimburseId, Long applyId) {
        try {
            linkExpenseApply(reimburseId, applyId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean processPayment(Long reimburseId, Long payerId, String paymentMethod, String voucherNumber) {
        Integer method = null;
        if (paymentMethod != null) {
            method = paymentMethod.hashCode() % 5; // 简化处理支付方式
        }
        processReimbursePayment(reimburseId, method, voucherNumber);
        return true;
    }
    
    public Map<String, Object> getExpenseReimburseStatistics(Long userId, Long deptId, Integer year) {
        // 简化统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAmount", 0);
        stats.put("approvedAmount", 0);
        stats.put("pendingAmount", 0);
        stats.put("rejectedAmount", 0);
        
        // 添加年度统计信息
        stats.put("year", year);
        if (year != null) {
            // 模拟根据年份的统计数据
            stats.put("monthlyStats", new HashMap<String, Object>());
        }
        
        stats.put("totalCount", 0);
        stats.put("approvedCount", 0);
        stats.put("pendingCount", 0);
        stats.put("rejectedCount", 0);
        
        return stats;
    }
    
    public Object getStatistics(Long userId, String startDate, String endDate, Integer deptId) {
        // 简化统计信息
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAmount", 0);
        stats.put("approvedAmount", 0);
        stats.put("pendingAmount", 0);
        stats.put("rejectedAmount", 0);
        return stats;
    }
    
    public boolean uploadVoucher(Long reimburseId, String voucherType, String fileName, String fileUrl) {
        // 上传凭证处理（简化实现）
        ReimburseAttachment attachment = new ReimburseAttachment();
        attachment.setExpenseReimburseId(reimburseId);
        attachment.setAttachmentName(fileName);
        attachment.setAttachmentUrl(fileUrl);
        attachment.setAttachmentType(voucherType);
        addReimburseAttachment(attachment);
        return true;
    }
    
    public String getVoucherDownloadUrl(Long attachmentId) {
        return "/api/file/download/" + attachmentId;
    }
    
    public boolean processRefund(Long reimburseId, Long operatorId, Double refundAmount, String reason) {
        // 退款处理（简化实现）
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isPresent()) {
            ExpenseReimburse reimburse = reimburseOpt.get();
            reimburse.setStatus(5); // 已退款
            expenseReimburseRepository.save(reimburse);
            return true;
        }
        return false;
    }
    
    public List<Map<String, Object>> getStatusTracking(Long reimburseId) {
        // 状态跟踪（简化实现）
        List<Map<String, Object>> tracking = new ArrayList<>();
        Map<String, Object> track = new HashMap<>();
        track.put("status", "created");
        track.put("time", java.time.LocalDateTime.now());
        track.put("operator", "system");
        tracking.add(track);
        return tracking;
    }
    
    public byte[] exportExpenseReimburses(Map<String, Object> exportParams) {
        // 导出功能（简化实现）
        return new byte[0];
    }
    
    public Object analyzeExpenseReimburses(Map<String, Object> analyzeParams) {
        // 分析功能（简化实现）
        Map<String, Object> analysis = new HashMap<>();
        analysis.put("totalAmount", 0);
        analysis.put("categoryStats", new HashMap<>());
        analysis.put("timeStats", new HashMap<>());
        return analysis;
    }
    
    public boolean urgentProcessing(Long reimburseId, Long operatorId, String reason) {
        // 加急处理（简化实现）
        Optional<ExpenseReimburse> reimburseOpt = expenseReimburseRepository.findById(reimburseId);
        if (reimburseOpt.isPresent()) {
            ExpenseReimburse reimburse = reimburseOpt.get();
            reimburse.setUrgentFlag(true);
            expenseReimburseRepository.save(reimburse);
            return true;
        }
        return false;
    }
}