package com.ccms.service.loan;

import com.ccms.entity.expense.LoanMain;
import com.ccms.repository.loan.LoanApplyRepository;
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
 * 借款服务实现类（loan包下）
 */
@Service
@Transactional
public class LoanServiceImpl implements LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanServiceImpl.class);
    
    private final LoanApplyRepository loanApplyRepository;

    @Autowired
    public LoanServiceImpl(LoanApplyRepository loanApplyRepository) {
        this.loanApplyRepository = loanApplyRepository;
    }

    @Override
    public LoanApply createLoanApply(LoanApply loanApply) {
        // 参数校验
        validateLoanApply(loanApply);
        
        // 生成借款单号
        String loanNo = generateLoanNo();
        loanApply.setLoanNo(loanNo);
        
        // 设置默认值
        loanApply.setStatus(0); // 草稿状态
        loanApply.setRepaymentStatus(0); // 未还款
        loanApply.setRemainingBalance(loanApply.getLoanAmount());
        
        // 检查借款限制
        if (!canUserBorrow(loanApply.getApplicantId(), loanApply.getLoanAmount())) {
            throw new RuntimeException("用户已达到借款限制，无法申请新借款");
        }
        
        LoanApply saved = loanApplyRepository.save(loanApply);
        
        logger.info("借款申请创建成功，借款单号：{}", loanNo);
        
        return saved;
    }

    @Override
    public LoanApply updateLoanApply(LoanApply loanApply) {
        // 校验借款单是否存在
        LoanApply existing = getLoanApplyById(loanApply.getId());
        
        // 只有草稿状态的借款单可以修改
        if (existing.getStatus() != 0) {
            throw new RuntimeException("只有草稿状态的借款单可以修改");
        }
        
        // 更新基本信息
        existing.setTitle(loanApply.getTitle());
        existing.setLoanAmount(loanApply.getLoanAmount());
        existing.setPurpose(loanApply.getPurpose());
        existing.setExpectedRepayDate(loanApply.getExpectedRepayDate());
        existing.setRemark(loanApply.getRemark());
        
        return loanApplyRepository.save(existing);
    }

    @Override
    public LoanApply getLoanApplyById(Long id) {
        return loanApplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("借款单不存在"));
    }

    @Override
    public LoanApply approveLoan(Long id, BigDecimal approvedAmount, String remark) {
        LoanApply loanApply = getLoanApplyById(id);
        
        // 只有审批中状态的借款单可以审批
        if (loanApply.getStatus() != 1) {
            throw new RuntimeException("只有审批中状态的借款单可以审批");
        }
        
        loanApply.setStatus(2); // 已批准
        loanApply.setApprovedAmount(approvedAmount);
        loanApply.setActualLoanAmount(approvedAmount);
        loanApply.setRemainingBalance(approvedAmount);
        
        LoanApply approved = loanApplyRepository.save(loanApply);
        
        logger.info("借款单审批通过，借款单ID：{}，审批金额：{}", id, approvedAmount);
        
        return approved;
    }

    @Override
    public LoanApply rejectLoan(Long id, String rejectReason) {
        LoanApply loanApply = getLoanApplyById(id);
        
        // 只有审批中状态的借款单可以驳回
        if (loanApply.getStatus() != 1) {
            throw new RuntimeException("只有审批中状态的借款单可以驳回");
        }
        
        loanApply.setStatus(3); // 已驳回
        loanApply.setRejectReason(rejectReason);
        
        LoanApply rejected = loanApplyRepository.save(loanApply);
        
        logger.info("借款单审批驳回，借款单ID：{}，驳回原因：{}", id, rejectReason);
        
        return rejected;
    }

    @Override
    public LoanApply disburseLoan(Long id, BigDecimal actualAmount) {
        LoanApply loanApply = getLoanApplyById(id);
        
        // 只有已批准状态的借款单可以放款
        if (loanApply.getStatus() != 2) {
            throw new RuntimeException("只有已批准状态的借款单可以放款");
        }
        
        loanApply.setStatus(4); // 已放款
        loanApply.setActualLoanAmount(actualAmount);
        loanApply.setRemainingBalance(actualAmount);
        loanApply.setLoanDate(LocalDate.now());
        
        LoanApply disbursed = loanApplyRepository.save(loanApply);
        
        logger.info("借款单放款成功，借款单ID：{}，放款金额：{}", id, actualAmount);
        
        return disbursed;
    }

    @Override
    public LoanApply repayLoan(Long id, BigDecimal repayAmount) {
        LoanApply loanApply = getLoanApplyById(id);
        
        // 检查是否可以还款
        if (!loanApply.isRepayable()) {
            throw new RuntimeException("当前借款单状态不允许还款");
        }
        
        BigDecimal remainingBalance = loanApply.getRemainingBalance();
        
        // 验证还款金额不超过待还金额
        if (repayAmount.compareTo(remainingBalance) > 0) {
            throw new RuntimeException("还款金额不能超过剩余待还金额");
        }
        
        // 更新还款信息
        loanApply.setRemainingBalance(remainingBalance.subtract(repayAmount));
        
        // 更新还款状态
        if (loanApply.getRemainingBalance().compareTo(BigDecimal.ZERO) == 0) {
            loanApply.setRepaymentStatus(1); // 已还清
        } else {
            loanApply.setRepaymentStatus(2); // 部分还款
        }
        
        LoanApply repaid = loanApplyRepository.save(loanApply);
        
        logger.info("借款还款成功，借款单ID：{}，还款金额：{}，剩余金额：{}", 
            id, repayAmount, repaid.getRemainingBalance());
        
        return repaid;
    }

    @Override
    public WriteOffResult autoWriteOffForReimburse(Long reimburseId, BigDecimal reimburseAmount, Long applicantId) {
        // 获取用户所有未还清的借款
        List<LoanApply> unpaidLoans = getUnpaidLoansByApplicant(applicantId);
        
        if (unpaidLoans.isEmpty()) {
            return new WriteOffResult(false, BigDecimal.ZERO, BigDecimal.ZERO, "用户没有未还清的借款");
        }
        
        BigDecimal totalWriteOffAmount = BigDecimal.ZERO;
        BigDecimal remainingReimburseAmount = reimburseAmount;
        
        // 按照创建时间顺序核销
        for (LoanApply loan : unpaidLoans) {
            if (remainingReimburseAmount.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            
            // 计算本次可核销金额
            BigDecimal availableWriteOff = loan.getRemainingBalance()
                .min(remainingReimburseAmount);
            
            if (availableWriteOff.compareTo(BigDecimal.ZERO) > 0) {
                // 执行核销
                loan.setRemainingBalance(loan.getRemainingBalance().subtract(availableWriteOff));
                
                // 更新还款状态
                if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) == 0) {
                    loan.setRepaymentStatus(1); // 已还清
                } else {
                    loan.setRepaymentStatus(2); // 部分还款
                }
                
                loanApplyRepository.save(loan);
                
                // 记录核销流水
                createWriteOffRecord(loan.getId(), reimburseId, availableWriteOff);
                
                totalWriteOffAmount = totalWriteOffAmount.add(availableWriteOff);
                remainingReimburseAmount = remainingReimburseAmount.subtract(availableWriteOff);
                
                logger.info("自动核销成功，借款单ID：{}，核销金额：{}，报销单ID：{}", 
                    loan.getId(), availableWriteOff, reimburseId);
            }
        }
        
        String message;
        if (totalWriteOffAmount.compareTo(BigDecimal.ZERO) > 0) {
            message = String.format("成功核销%s元，剩余待还款金额%s元", 
                totalWriteOffAmount, calculateTotalUnpaidAmount(applicantId));
        } else {
            message = "没有可核销的借款金额";
        }
        
        return new WriteOffResult(totalWriteOffAmount.compareTo(BigDecimal.ZERO) > 0, 
            totalWriteOffAmount, remainingReimburseAmount, message);
    }

    @Override
    public Page<LoanApply> getLoanList(LoanQueryCondition condition, Pageable pageable) {
        return loanApplyRepository.findByConditions(condition, pageable);
    }

    @Override
    public List<LoanApply> getUnpaidLoansByApplicant(Long applicantId) {
        return loanApplyRepository.findByApplicantIdAndRepaymentStatusIn(
            applicantId, List.of(0, 2) // 未还款和部分还款
        );
    }

    @Override
    public BigDecimal calculateTotalUnpaidAmount(Long applicantId) {
        BigDecimal total = loanApplyRepository.sumRemainingBalanceByApplicantId(applicantId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    public boolean hasOverdueLoans(Long applicantId) {
        List<LoanApply> loans = loanApplyRepository.findByApplicantId(applicantId);
        return loans.stream().anyMatch(LoanApply::isOverdue);
    }

    @Override
    public void generateRepaymentPlan(Long loanId) {
        // 这里可以生成具体的还款计划，当前简化处理
        LoanApply loanApply = getLoanApplyById(loanId);
        
        logger.info("为借款单ID：{}生成还款计划，金额：{}，还款日期：{}", 
            loanId, loanApply.getActualLoanAmount(), loanApply.getExpectedRepayDate());
        
        // 实现还款计划的生成逻辑
        // 可以调用相关的还款计划服务或直接在数据库中创建还款计划记录
    }
    
    // 私有辅助方法
    
    /**
     * 验证借款申请参数
     */
    private void validateLoanApply(LoanApply loanApply) {
        if (loanApply.getLoanAmount() == null || loanApply.getLoanAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("借款金额必须大于0");
        }
        
        if (loanApply.getApplicantId() == null) {
            throw new RuntimeException("借款人ID不能为空");
        }
        
        if (loanApply.getApplicantName() == null || loanApply.getApplicantName().trim().isEmpty()) {
            throw new RuntimeException("借款人姓名不能为空");
        }
        
        if (loanApply.getTitle() == null || loanApply.getTitle().trim().isEmpty()) {
            throw new RuntimeException("借款标题不能为空");
        }
        
        if (loanApply.getExpectedRepayDate() == null || 
            loanApply.getExpectedRepayDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("预期还款日期不能早于当前日期");
        }
    }
    
    /**
     * 检查用户是否可以借款
     */
    private boolean canUserBorrow(Long applicantId, BigDecimal amount) {
        // 检查是否有逾期借款
        if (hasOverdueLoans(applicantId)) {
            return false;
        }
        
        // 检查单笔借款限额（示例：10000元）
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            return false;
        }
        
        // 检查月度借款限额（示例：50000元）
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        
        BigDecimal monthlyLoanAmount = getMonthlyLoanAmount(applicantId, startOfMonth, endOfMonth);
        if (monthlyLoanAmount.add(amount).compareTo(new BigDecimal("50000")) > 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取月度借款金额
     */
    private BigDecimal getMonthlyLoanAmount(Long applicantId, LocalDate startDate, LocalDate endDate) {
        List<LoanApply> monthlyLoans = loanApplyRepository.findByApplicantIdAndCreateTimeBetween(
            applicantId, startDate.atStartOfDay(), endDate.atTime(23, 59, 59)
        );
        
        return monthlyLoans.stream()
            .map(loan -> loan.getLoanAmount() != null ? loan.getLoanAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 生成借款单号
     */
    private String generateLoanNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(8);
        return "LOAN" + dateStr + timestamp;
    }
    
    /**
     * 创建核销记录
     */
    private void createWriteOffRecord(Long loanId, Long reimburseId, BigDecimal amount) {
        // 这里应该调用核销记录服务创建记录
        logger.info("创建核销记录，借款单ID：{}，报销单ID：{}，核销金额：{}", 
            loanId, reimburseId, amount);
    }
}