package com.ccms.service.impl;

import com.ccms.entity.expense.LoanMain;
import com.ccms.entity.expense.RepaymentRecord;
import com.ccms.repository.expense.LoanMainRepository;
import com.ccms.repository.expense.RepaymentRecordRepository;
import com.ccms.service.LoanService;
import com.ccms.service.RepaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 还款服务实现类
 */
@Service
@Transactional
public class RepaymentServiceImpl implements RepaymentService {

    private static final Logger logger = LoggerFactory.getLogger(RepaymentServiceImpl.class);
    
    private final RepaymentRecordRepository repaymentRecordRepository;
    private final LoanMainRepository loanMainRepository;
    private final LoanService loanService;

    @Autowired
    public RepaymentServiceImpl(RepaymentRecordRepository repaymentRecordRepository,
                              LoanMainRepository loanMainRepository,
                              LoanService loanService) {
        this.repaymentRecordRepository = repaymentRecordRepository;
        this.loanMainRepository = loanMainRepository;
        this.loanService = loanService;
    }

    @Override
    public RepaymentRecord createRepayment(RepaymentRecord repayment) {
        validateRepayment(repayment.getLoanId(), repayment.getRepayAmount());
        
        // 设置还款日期（如果未设置则使用当前日期）
        if (repayment.getRepayDate() == null) {
            repayment.setRepayDate(LocalDate.now());
        }
        
        RepaymentRecord savedRepayment = repaymentRecordRepository.save(repayment);
        
        // 更新借款单的已还金额
        updateLoanRepaidAmount(repayment.getLoanId());
        
        logger.info("还款记录创建成功，借款单ID：{}，还款金额：{}", 
            repayment.getLoanId(), repayment.getRepayAmount());
        
        return savedRepayment;
    }

    @Override
    public RepaymentRecord getRepaymentById(Long id) {
        return repaymentRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("还款记录不存在"));
    }

    @Override
    public List<RepaymentRecord> getRepaymentsByLoanId(Long loanId) {
        return repaymentRecordRepository.findByLoanId(loanId);
    }

    @Override
    public Page<RepaymentRecord> getRepaymentsByLoanId(Long loanId, Pageable pageable) {
        return repaymentRecordRepository.findByLoanId(loanId, pageable);
    }

    @Override
    public List<RepaymentRecord> getRepaymentsByReimburseId(Long reimburseId) {
        return repaymentRecordRepository.findByReimburseId(reimburseId);
    }

    @Override
    public BigDecimal getTotalRepaymentAmount(Long loanId) {
        BigDecimal total = repaymentRecordRepository.sumRepayAmountByLoanId(loanId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public Long getRepaymentCountByLoanId(Long loanId) {
        return repaymentRecordRepository.countRepaymentByLoanId(loanId);
    }

    @Override
    public boolean repayByCash(Long loanId, BigDecimal amount, String remark) {
        return createSpecifiedRepayment(loanId, amount, 1, null, remark);
    }

    @Override
    public boolean repayByTransfer(Long loanId, BigDecimal amount, String remark) {
        return createSpecifiedRepayment(loanId, amount, 3, null, remark);
    }

    @Override
    public boolean repayByReimburse(Long loanId, Long reimburseId, BigDecimal amount) {
        return createSpecifiedRepayment(loanId, amount, 2, reimburseId, "报销抵扣还款");
    }

    @Override
    public boolean autoDeductLoan(Long reimburseId, Long userId, BigDecimal reimburseAmount) {
        // 获取用户未还清的借款单
        List<LoanMain> unpaidLoans = loanService.getUnpaidLoansByUserId(userId);
        
        if (unpaidLoans.isEmpty()) {
            logger.info("用户{}没有未还清的借款，无需自动抵扣", userId);
            return false;
        }
        
        BigDecimal remainingAmount = reimburseAmount;
        boolean hasDeducted = false;
        
        for (LoanMain loan : unpaidLoans) {
            if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            BigDecimal needToRepay = loan.getLoanAmount().subtract(loan.getRepaidAmount());
            BigDecimal deductAmount = remainingAmount.min(needToRepay);
            
            if (deductAmount.compareTo(BigDecimal.ZERO) > 0) {
                // 创建还款记录
                boolean success = repayByReimburse(loan.getId(), reimburseId, deductAmount);
                if (success) {
                    remainingAmount = remainingAmount.subtract(deductAmount);
                    hasDeducted = true;
                    logger.info("自动抵扣成功，借款单ID：{}，抵扣金额：{}", loan.getId(), deductAmount);
                }
            }
        }
        
        return hasDeducted;
    }

    @Override
    public boolean cancelRepayment(Long repaymentId) {
        RepaymentRecord repayment = getRepaymentById(repaymentId);
        
        // 只能取消报销抵扣类型的还款记录
        if (repayment.getRepayType() != 2) {
            throw new RuntimeException("只能取消报销抵扣类型的还款记录");
        }
        
        repaymentRecordRepository.delete(repayment);
        
        // 重新计算借款单的已还金额
        updateLoanRepaidAmount(repayment.getLoanId());
        
        logger.info("还款记录已取消，还款记录ID：{}", repaymentId);
        
        return true;
    }

    @Override
    public List<RepaymentRecord> getRepaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return repaymentRecordRepository.findByRepayDateRange(startDate, endDate);
    }

    @Override
    public BigDecimal getUserReimburseDeductionAmount(Long userId) {
        BigDecimal deductionAmount = repaymentRecordRepository.sumReimburseDeductionByUserId(userId);
        return deductionAmount != null ? deductionAmount : BigDecimal.ZERO;
    }

    @Override
    public List<RepaymentRecord> getRecentRepaymentsByLoanId(Long loanId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return repaymentRecordRepository.findRecentRepaymentsByLoanId(loanId, pageable);
    }

    @Override
    public boolean batchRepayment(List<Long> loanIds, BigDecimal amount, Integer repayType, String remark) {
        if (loanIds == null || loanIds.isEmpty()) {
            throw new RuntimeException("借款单ID列表不能为空");
        }
        
        boolean allSuccess = true;
        
        for (Long loanId : loanIds) {
            try {
                createSpecifiedRepayment(loanId, amount, repayType, null, remark);
            } catch (Exception e) {
                allSuccess = false;
                logger.error("批量还款失败，借款单ID：{}，错误：{}", loanId, e.getMessage());
            }
        }
        
        return allSuccess;
    }

    @Override
    public boolean validateRepayment(Long loanId, BigDecimal amount) {
        LoanMain loan = loanService.getLoanById(loanId);
        
        // 检查借款单状态
        if (loan.getStatus() != 2 && loan.getStatus() != 3) {
            throw new RuntimeException("只有已放款或部分还款状态的借款单可以还款");
        }
        
        // 检查还款金额是否超过未还金额
        BigDecimal unpaidAmount = loan.getLoanAmount().subtract(loan.getRepaidAmount());
        if (amount.compareTo(unpaidAmount) > 0) {
            throw new RuntimeException("还款金额不能超过未还金额");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("还款金额必须大于0");
        }
        
        return true;
    }

    @Override
    public String generateRepaymentNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "RP" + dateStr + timestamp;
    }

    @Override
    public RepaymentRecord updateRepayment(RepaymentRecord repayment) {
        RepaymentRecord existingRepayment = getRepaymentById(repayment.getId());
        
        // 只能修改报销抵扣类型的还款记录
        if (existingRepayment.getRepayType() != 2) {
            throw new RuntimeException("只能修改报销抵扣类型的还款记录");
        }
        
        // 更新基本信息
        existingRepayment.setRepayAmount(repayment.getRepayAmount());
        existingRepayment.setRepayDate(repayment.getRepayDate());
        existingRepayment.setRemark(repayment.getRemark());
        
        RepaymentRecord updatedRepayment = repaymentRecordRepository.save(existingRepayment);
        
        // 重新计算借款单的已还金额
        updateLoanRepaidAmount(repayment.getLoanId());
        
        logger.info("还款记录更新成功，还款记录ID：{}", repayment.getId());
        
        return updatedRepayment;
    }

    /**
     * 创建指定类型的还款记录
     */
    private boolean createSpecifiedRepayment(Long loanId, BigDecimal amount, 
                                           Integer repayType, Long reimburseId, String remark) {
        validateRepayment(loanId, amount);
        
        RepaymentRecord repayment = new RepaymentRecord();
        repayment.setLoanId(loanId);
        repayment.setReimburseId(reimburseId);
        repayment.setRepayAmount(amount);
        repayment.setRepayType(repayType);
        repayment.setRepayDate(LocalDate.now());
        repayment.setRepayBy(1L); // TODO: 从当前登录用户获取
        repayment.setRemark(remark);
        
        createRepayment(repayment);
        
        return true;
    }

    /**
     * 更新借款单的已还金额和状态
     */
    private void updateLoanRepaidAmount(Long loanId) {
        LoanMain loan = loanService.getLoanById(loanId);
        BigDecimal totalRepaid = getTotalRepaymentAmount(loanId);
        
        loan.setRepaidAmount(totalRepaid);
        
        // 更新借款单状态
        if (totalRepaid.compareTo(loan.getLoanAmount()) >= 0) {
            loan.setStatus(4); // 已还清
        } else if (totalRepaid.compareTo(BigDecimal.ZERO) > 0) {
            loan.setStatus(3); // 部分还款
        }
        
        loanMainRepository.save(loan);
    }

    /**
     * 删除还款记录
     */
    @Override
    public void deleteRepayment(Long id) {
        RepaymentRecord repayment = getRepaymentById(id);
        repaymentRecordRepository.delete(repayment);
    }

    /**
     * 分页查询还款记录
     */
    @Override
    public Page<RepaymentRecord> findRepayments(Pageable pageable) {
        return repaymentRecordRepository.findAll(pageable);
    }

    /**
     * 根据状态查询还款记录
     */
    @Override
    public List<RepaymentRecord> findRepaymentsByStatus(Integer status) {
        // 还款记录没有状态字段，返回所有记录
        return repaymentRecordRepository.findAll();
    }

    /**
     * 还款确认
     */
    @Override
    public RepaymentRecord confirmRepayment(Long id) {
        RepaymentRecord repayment = getRepaymentById(id);
        // 还款确认逻辑 - 这里简单返回原记录
        return repayment;
    }



    /**
     * 批量确认还款
     */
    @Override
    public void batchConfirmRepayments(List<Long> repaymentIds) {
        // 批量确认还款逻辑 - 这里简单记录日志
        logger.info("批量确认还款，记录数量：{}", repaymentIds.size());
    }

    /**
     * 获取用户还款记录
     */
    @Override
    public List<RepaymentRecord> findRepaymentsByUserId(Long userId) {
        // 根据还款人ID查询还款记录
        return repaymentRecordRepository.findByRepayBy(userId);
    }

    /**
     * 自动还款核销
     */
    @Override
    public RepaymentRecord autoWriteOff(Long loanId) {
        LoanMain loan = loanService.getLoanById(loanId);
        BigDecimal unpaidAmount = loan.getLoanAmount().subtract(loan.getRepaidAmount());
        
        // 创建一笔全额还款记录
        RepaymentRecord repayment = new RepaymentRecord();
        repayment.setLoanId(loanId);
        repayment.setRepayAmount(unpaidAmount);
        repayment.setRepayType(3); // 银行转账
        repayment.setRepayDate(LocalDate.now());
        repayment.setRepayBy(1L); // TODO: 从当前登录用户获取
        repayment.setRemark("系统自动还款核销");
        
        return createRepayment(repayment);
    }

    /**
     * 借款未还金额统计
     */
    @Override
    public Object getLoanRepaymentStats(Long loanId) {
        LoanMain loan = loanService.getLoanById(loanId);
        BigDecimal totalRepaid = getTotalRepaymentAmount(loanId);
        BigDecimal unpaidAmount = loan.getLoanAmount().subtract(totalRepaid);
        final BigDecimal loanAmountVal = loan.getLoanAmount();
        final BigDecimal repaidAmountVal = totalRepaid;
        final BigDecimal unpaidAmountVal = unpaidAmount;
        final Long repaymentCountVal = getRepaymentCountByLoanId(loanId);
        
        return new Object() {
            public final BigDecimal loanAmount = loanAmountVal;
            public final BigDecimal repaidAmount = repaidAmountVal;
            public final BigDecimal unpaidAmount = unpaidAmountVal;
            public final Long repaymentCount = repaymentCountVal;
        };
    }

    /**
     * 用户未还金额统计
     */
    @Override
    public Object getUserRepaymentStats(Long userId) {
        // 获取用户的所有借款单
        List<LoanMain> userLoans = loanMainRepository.findByUserId(userId);
        
        BigDecimal totalLoanAmount = BigDecimal.ZERO;
        BigDecimal totalRepaidAmount = BigDecimal.ZERO;
        
        for (LoanMain loan : userLoans) {
            totalLoanAmount = totalLoanAmount.add(loan.getLoanAmount());
            totalRepaidAmount = totalRepaidAmount.add(loan.getRepaidAmount());
        }
        
        BigDecimal totalUnpaidAmount = totalLoanAmount.subtract(totalRepaidAmount);
        final BigDecimal loanAmountVal = totalLoanAmount;
        final BigDecimal repaidAmountVal = totalRepaidAmount;
        final BigDecimal unpaidAmountVal = totalUnpaidAmount;
        final int loanCountVal = userLoans.size();
        
        return new Object() {
            public final BigDecimal loanAmount = loanAmountVal;
            public final BigDecimal repaidAmount = repaidAmountVal;
            public final BigDecimal unpaidAmount = unpaidAmountVal;
            public final int loanCount = loanCountVal;
        };
    }
}