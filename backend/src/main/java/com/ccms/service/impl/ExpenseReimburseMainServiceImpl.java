package com.ccms.service.impl;

import com.ccms.entity.expense.ExpenseReimburseMain;
import com.ccms.entity.expense.ExpenseReimburseDetail;
import com.ccms.repository.expense.ExpenseReimburseMainRepository;
import com.ccms.repository.expense.ExpenseReimburseDetailRepository;
import com.ccms.repository.budget.BudgetDetailRepository;
import com.ccms.service.ExpenseReimburseMainService;
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
import java.util.stream.Collectors;

/**
 * 费用报销主表服务实现类
 * 基于新版数据结构实现
 * 
 * @author 系统生成
 */
@Service
@Transactional
public class ExpenseReimburseMainServiceImpl implements ExpenseReimburseMainService {

    private final ExpenseReimburseMainRepository expenseReimburseMainRepository;
    private final ExpenseReimburseDetailRepository expenseReimburseDetailRepository;
    private final BudgetDetailRepository budgetDetailRepository;

    @Autowired
    public ExpenseReimburseMainServiceImpl(ExpenseReimburseMainRepository expenseReimburseMainRepository,
                                         ExpenseReimburseDetailRepository expenseReimburseDetailRepository,
                                         BudgetDetailRepository budgetDetailRepository) {
        this.expenseReimburseMainRepository = expenseReimburseMainRepository;
        this.expenseReimburseDetailRepository = expenseReimburseDetailRepository;
        this.budgetDetailRepository = budgetDetailRepository;
    }

    @Override
    public ExpenseReimburseMain createReimburseMain(ExpenseReimburseMain reimburseMain) {
        // 设置默认状态
        if (reimburseMain.getStatus() == null) {
            reimburseMain.setStatus(0); // 草稿状态
        }
        if (reimburseMain.getApprovalStatus() == null) {
            reimburseMain.setApprovalStatus(0); // 待提交
        }
        if (reimburseMain.getTotalAmount() == null) {
            reimburseMain.setTotalAmount(BigDecimal.ZERO);
        }
        if (reimburseMain.getLoanDeductTotal() == null) {
            reimburseMain.setLoanDeductTotal(BigDecimal.ZERO);
        }
        if (reimburseMain.getActualAmount() == null) {
            reimburseMain.setActualAmount(BigDecimal.ZERO);
        }
        if (reimburseMain.getInvoiceTotal() == null) {
            reimburseMain.setInvoiceTotal(BigDecimal.ZERO);
        }
        
        // 生成报销单号
        if (reimburseMain.getReimburseNo() == null || reimburseMain.getReimburseNo().isEmpty()) {
            reimburseMain.setReimburseNo(generateReimburseNo());
        }
        
        return expenseReimburseMainRepository.save(reimburseMain);
    }

    @Override
    public ExpenseReimburseMain updateReimburseMain(ExpenseReimburseMain reimburseMain) {
        Optional<ExpenseReimburseMain> existingOpt = expenseReimburseMainRepository.findById(reimburseMain.getId());
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("报销单不存在");
        }
        
        ExpenseReimburseMain existing = existingOpt.get();
        
        // 检查是否允许修改（草稿状态才允许修改）
        if (existing.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的报销单不允许修改");
        }
        
        // 更新可修改的字段
        existing.setReason(reimburseMain.getReason());
        existing.setPeriodStart(reimburseMain.getPeriodStart());
        existing.setPeriodEnd(reimburseMain.getPeriodEnd());
        existing.setBankInfo(reimburseMain.getBankInfo());
        existing.setPaymentMethod(reimburseMain.getPaymentMethod());
        
        return expenseReimburseMainRepository.save(existing);
    }

    @Override
    public ExpenseReimburseMain getReimburseMainById(Long reimburseId) {
        return expenseReimburseMainRepository.findById(reimburseId).orElse(null);
    }

    @Override
    public boolean deleteReimburseMain(Long reimburseId) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            return false;
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有草稿状态才能删除
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的报销单不允许删除");
        }
        
        // 删除关联的明细
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburseId);
        if (!details.isEmpty()) {
            expenseReimburseDetailRepository.deleteAll(details);
        }
        
        expenseReimburseMainRepository.deleteById(reimburseId);
        return true;
    }

    @Override
    public ExpenseReimburseMain submitForApproval(Long reimburseId, Long submitUserId) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("报销单不存在");
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有草稿状态才能提交审批
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("报销单状态不允许提交审批");
        }
        
        // 验证报销单完整性
        if (!validateReimburseCompleteness(reimburseId)) {
            throw new RuntimeException("报销单信息不完整，请检查明细和必要信息");
        }
        
        // 检查预算可用性
        if (!checkBudgetAvailability(reimburseId)) {
            throw new RuntimeException("预算额度不足，无法提交审批");
        }
        
        // 计算并更新金额
        BigDecimal totalAmount = calculateTotalAmount(reimburseId);
        BigDecimal actualAmount = calculateActualAmount(reimburseId);
        BigDecimal invoiceTotal = calculateInvoiceTotal(reimburseId);
        
        reimburse.setTotalAmount(totalAmount);
        reimburse.setActualAmount(actualAmount);
        reimburse.setInvoiceTotal(invoiceTotal);
        
        // 更新状态为审批中
        reimburse.setStatus(1); // 审批中
        reimburse.setApprovalStatus(1); // 审批中
        reimburse.setSubmitUserId(submitUserId);
        reimburse.setSubmitTime(LocalDateTime.now());
        
        return expenseReimburseMainRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburseMain withdrawSubmission(Long reimburseId) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("报销单不存在");
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有审批中状态才能撤回
        if (reimburse.getStatus() != 1) {
            throw new RuntimeException("只有审批中的报销单才能撤回");
        }
        
        // 撤回为草稿状态
        reimburse.setStatus(0); // 草稿
        reimburse.setApprovalStatus(0); // 待提交
        reimburse.setSubmitUserId(null);
        reimburse.setSubmitTime(null);
        
        return expenseReimburseMainRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburseMain approveReimburse(Long reimburseId, Long approverId, Integer action, String comment) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("报销单不存在");
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 验证当前状态是否可以审批
        if (reimburse.getApprovalStatus() != 1) {
            throw new RuntimeException("只有审批中的报销单才能进行审批操作");
        }
        
        if (action == 1) { // 通过
            reimburse.setApprovalStatus(2); // 已审批
            reimburse.setStatus(2); // 审批通过
            reimburse.setApprovedUserId(approverId);
            reimburse.setApprovedTime(LocalDateTime.now());
            reimburse.setApprovalComment(comment);
            reimburse.setCurrentNode("审批完成");
            
        } else if (action == 2) { // 拒绝
            reimburse.setApprovalStatus(3); // 已拒绝
            reimburse.setStatus(3); // 审批拒绝
            reimburse.setApprovedUserId(approverId);
            reimburse.setApprovedTime(LocalDateTime.now());
            reimburse.setApprovalComment(comment);
            reimburse.setCurrentNode("审批拒绝");
            
        } else if (action == 3) { // 退回修改
            reimburse.setApprovalStatus(0); // 待提交
            reimburse.setStatus(0); // 草稿状态
            reimburse.setApprovalComment("审批人建议修改: " + comment);
            reimburse.setCurrentNode("待修改");
            
        } else {
            throw new RuntimeException("无效的审批操作");
        }
        
        return expenseReimburseMainRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburseMain processPayment(Long reimburseId, Integer paymentMethod, String paymentDocNumber) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("报销单不存在");
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有审批通过且未支付的才能支付
        if (reimburse.getStatus() != 2) {
            throw new RuntimeException("只有审批通过的报销单才能进行支付");
        }
        
        // 检查是否已经支付
        if (reimburse.getStatus() == 5 || reimburse.getStatus() == 6) {
            throw new RuntimeException("报销单已支付或已作废");
        }
        
        // 处理支付
        reimburse.setStatus(5); // 已支付
        reimburse.setPaymentMethod(paymentMethod);
        reimburse.setPaymentTime(java.sql.Date.valueOf(java.time.LocalDate.now()));
        reimburse.setCurrentNode("支付完成");
        
        return expenseReimburseMainRepository.save(reimburse);
    }

    @Override
    public ExpenseReimburseMain linkToExpenseApply(Long reimburseId, Long applyId) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            throw new RuntimeException("报销单不存在");
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 只有在草稿状态才能关联申请
        if (reimburse.getStatus() != 0) {
            throw new RuntimeException("非草稿状态的报销单不允许关联申请");
        }
        
        reimburse.setApplyId(applyId);
        return expenseReimburseMainRepository.save(reimburse);
    }

    @Override
    public BigDecimal calculateTotalAmount(Long reimburseId) {
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburseId);
        
        return details.stream()
                .map(ExpenseReimburseDetail::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal calculateActualAmount(Long reimburseId) {
        BigDecimal totalAmount = calculateTotalAmount(reimburseId);
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburseId);
        
        BigDecimal loanDeductTotal = details.stream()
                .map(ExpenseReimburseDetail::getLoanDeductAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 实际报销金额 = 总金额 - 借款抵扣金额
        return totalAmount.subtract(loanDeductTotal);
    }

    @Override
    public BigDecimal calculateInvoiceTotal(Long reimburseId) {
        // 这里简化实现，实际应根据发票记录计算
        // 暂时返回总金额的90%作为发票总额（假设有部分费用无需发票）
        BigDecimal totalAmount = calculateTotalAmount(reimburseId);
        return totalAmount.multiply(new BigDecimal("0.9"));
    }

    @Override
    public List<ExpenseReimburseDetail> getReimburseDetails(Long reimburseId) {
        return expenseReimburseDetailRepository.findByReimburseId(reimburseId);
    }

    @Override
    public List<ExpenseReimburseMain> getReimbursesByStatus(Integer status) {
        return expenseReimburseMainRepository.findByStatus(status);
    }

    @Override
    public List<ExpenseReimburseMain> getReimbursesByUser(Long userId) {
        return expenseReimburseMainRepository.findByReimburseUserId(userId);
    }

    @Override
    public List<ExpenseReimburseMain> getReimbursesByDepartment(Long deptId) {
        return expenseReimburseMainRepository.findByReimburseDeptId(deptId);
    }

    @Override
    public Page<ExpenseReimburseMain> getReimburseList(int pageNum, int pageSize, Map<String, Object> queryParams) {
        // 简化实现 - 实际应根据查询参数进行查询
        List<ExpenseReimburseMain> allReimburses = expenseReimburseMainRepository.findAll();
        
        // 应用过滤条件（简化实现）
        List<ExpenseReimburseMain> filteredReimburses = allReimburses.stream()
                .filter(reimburse -> applyQueryParams(reimburse, queryParams))
                .collect(Collectors.toList());
        
        // 分页处理
        int totalSize = filteredReimburses.size();
        int startIndex = pageNum * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalSize);
        
        List<ExpenseReimburseMain> pageContent = filteredReimburses.subList(startIndex, endIndex);
        
        return new PageImpl<>(pageContent, PageRequest.of(pageNum, pageSize), totalSize);
    }

    @Override
    public Map<String, Object> getReimburseStatistics(Long deptId, Integer year, Integer month) {
        Map<String, Object> stats = new HashMap<>();
        
        // 获取相关报销单
        List<ExpenseReimburseMain> reimburses = getReimbursesForStatistics(deptId, year, month);
        
        // 计算统计信息
        BigDecimal totalAmount = reimburses.stream()
                .map(ExpenseReimburseMain::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal paidAmount = reimburses.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 5)
                .map(ExpenseReimburseMain::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long pendingCount = reimburses.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 1)
                .count();
        
        long approvedCount = reimburses.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 2)
                .count();
        
        long paidCount = reimburses.stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == 5)
                .count();
        
        stats.put("totalAmount", totalAmount);
        stats.put("paidAmount", paidAmount);
        stats.put("pendingCount", pendingCount);
        stats.put("approvedCount", approvedCount);
        stats.put("paidCount", paidCount);
        stats.put("totalCount", reimburses.size());
        
        return stats;
    }

    @Override
    public boolean validateReimburseCompleteness(Long reimburseId) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            return false;
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        
        // 检查必要字段
        if (reimburse.getReimburseUserId() == null || 
            reimburse.getReimburseDeptId() == null ||
            reimburse.getReason() == null || reimburse.getReason().isEmpty()) {
            return false;
        }
        
        // 检查是否有明细
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburseId);
        if (details.isEmpty()) {
            return false;
        }
        
        // 检查明细完整性
        for (ExpenseReimburseDetail detail : details) {
            if (detail.getFeeTypeId() == null || 
                detail.getExpenseDate() == null ||
                detail.getAmount() == null || detail.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return false;
            }
        }
        
        return true;
    }

    @Override
    public boolean checkBudgetAvailability(Long reimburseId) {
        List<ExpenseReimburseDetail> details = expenseReimburseDetailRepository.findByReimburseId(reimburseId);
        
        for (ExpenseReimburseDetail detail : details) {
            if (detail.getBudgetId() != null) {
                // 检查预算额度（简化实现）
                // 预算检查逻辑
                if (detail.getAmount() != null && detail.getAmount().compareTo(new BigDecimal("10000")) > 0) {
                    return false; // 假设预算上限为10000
                }
                
                // 简化预算检查：实际应该查询预算明细表的可用余额
                // 这里暂时跳过详细的预算检查逻辑
            }
        }
        
        return true;
    }

    @Override
    public Map<String, Object> batchApproveReimburses(List<Long> reimburseIds, Long approverId) {
        Map<String, Object> result = new HashMap<>();
        List<Long> successIds = new ArrayList<>();
        List<Map<String, Object>> failedReimburses = new ArrayList<>();
        
        for (Long reimburseId : reimburseIds) {
            try {
                approveReimburse(reimburseId, approverId, 1, "批量审批通过");
                successIds.add(reimburseId);
            } catch (Exception e) {
                Map<String, Object> failed = new HashMap<>();
                failed.put("reimburseId", reimburseId);
                failed.put("error", e.getMessage());
                failedReimburses.add(failed);
            }
        }
        
        result.put("successCount", successIds.size());
        result.put("failedCount", failedReimburses.size());
        result.put("successIds", successIds);
        result.put("failedReimburses", failedReimburses);
        
        return result;
    }

    @Override
    public String generateReimburseNo() {
        return "RB" + System.currentTimeMillis();
    }

    @Override
    public byte[] exportReimburses(Map<String, Object> exportParams) {
        // 简化实现 - 返回空字节数组
        return new byte[0];
    }

    @Override
    public long getPendingCount(Long approverId) {
        // 简化实现 - 查询当前用户需要审批的数量
        return expenseReimburseMainRepository.countByStatusAndApproverId(1, approverId);
    }

    @Override
    public List<Map<String, Object>> getStatusTracking(Long reimburseId) {
        Optional<ExpenseReimburseMain> reimburseOpt = expenseReimburseMainRepository.findById(reimburseId);
        if (reimburseOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        ExpenseReimburseMain reimburse = reimburseOpt.get();
        List<Map<String, Object>> tracking = new ArrayList<>();
        
        // 添加创建记录
        if (reimburse.getCreateTime() != null) {
            Map<String, Object> createTrack = new HashMap<>();
            createTrack.put("status", "created");
            createTrack.put("time", reimburse.getCreateTime());
            createTrack.put("operator", "creator");
            createTrack.put("description", "报销单创建");
            tracking.add(createTrack);
        }
        
        // 添加提交记录
        if (reimburse.getSubmitTime() != null) {
            Map<String, Object> submitTrack = new HashMap<>();
            submitTrack.put("status", "submitted");
            submitTrack.put("time", reimburse.getSubmitTime());
            submitTrack.put("operator", "submitter");
            submitTrack.put("description", "提交审批");
            tracking.add(submitTrack);
        }
        
        // 添加审批记录
        if (reimburse.getApprovedTime() != null) {
            Map<String, Object> approveTrack = new HashMap<>();
            approveTrack.put("status", "approved");
            approveTrack.put("time", reimburse.getApprovedTime());
            approveTrack.put("operator", "approver");
            approveTrack.put("description", "完成审批: " + reimburse.getApprovalComment());
            tracking.add(approveTrack);
        }
        
        // 添加支付记录
        if (reimburse.getPaymentTime() != null) {
            Map<String, Object> paymentTrack = new HashMap<>();
            paymentTrack.put("status", "paid");
            paymentTrack.put("time", reimburse.getPaymentTime());
            paymentTrack.put("operator", "payer");
            paymentTrack.put("description", "完成支付");
            tracking.add(paymentTrack);
        }
        
        return tracking;
    }
    
    /**
     * 应用查询参数过滤报销单
     */
    private boolean applyQueryParams(ExpenseReimburseMain reimburse, Map<String, Object> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return true;
        }
        
        for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            switch (key) {
                case "status":
                    if (reimburse.getStatus() == null || !reimburse.getStatus().equals(value)) {
                        return false;
                    }
                    break;
                case "userId":
                    if (reimburse.getReimburseUserId() == null || !reimburse.getReimburseUserId().equals(value)) {
                        return false;
                    }
                    break;
                case "deptId":
                    if (reimburse.getReimburseDeptId() == null || !reimburse.getReimburseDeptId().equals(value)) {
                        return false;
                    }
                    break;
                case "startDate":
                    if (reimburse.getCreateTime() != null && reimburse.getCreateTime().isBefore((LocalDateTime) value)) {
                        return false;
                    }
                    break;
                case "endDate":
                    if (reimburse.getCreateTime() != null && reimburse.getCreateTime().isAfter((LocalDateTime) value)) {
                        return false;
                    }
                    break;
            }
        }
        
        return true;
    }
    
    /**
     * 获取统计用的报销单列表
     */
    private List<ExpenseReimburseMain> getReimbursesForStatistics(Long deptId, Integer year, Integer month) {
        // 简化实现 - 实际应根据部门和时间范围查询
        List<ExpenseReimburseMain> allReimburses = expenseReimburseMainRepository.findAll();
        
        return allReimburses.stream()
                .filter(reimburse -> {
                    if (deptId != null && !reimburse.getReimburseDeptId().equals(deptId)) {
                        return false;
                    }
                    
                    if (year != null && reimburse.getCreateTime() != null) {
                        if (reimburse.getCreateTime().getYear() != year) {
                            return false;
                        }
                        
                        if (month != null && reimburse.getCreateTime().getMonthValue() != month) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
}